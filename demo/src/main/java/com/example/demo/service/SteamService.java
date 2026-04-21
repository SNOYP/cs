package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SteamService {

    @Value("${steam.api.key}")
    private String steamApiKey;

    // КЭШ ЦЕН
    private final Map<String, Double> itemPricesCache = new ConcurrentHashMap<>();
    private final HttpClient httpClient;

    public SteamService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    // --- ОБНОВЛЕНИЕ ЦЕН (Запускается при старте сервера и каждый час) ---
    @PostConstruct
    @Scheduled(fixedRate = 3600000)
    public void updatePrices() {
        System.out.println("⏳ Загрузка актуальных цен на скины...");
        boolean success = fetchFromCSGOMarket();
        if (!success) {
            System.out.println("⚠️ CSGOMarket недоступен. Пробуем резервный API (CSGOFast)...");
            fetchFromCSGOFast();
        }
    }

    private boolean fetchFromCSGOMarket() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://market.csgo.com/api/v2/prices/USD.json"))
                    .header("Accept", "application/json")
                    .header("User-Agent", "SkinCasino-Server")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode items = root.path("items");

                if (items.isArray()) {
                    for (JsonNode item : items) {
                        String name = item.path("market_hash_name").asText();
                        double price = item.path("price").asDouble(0.0);
                        if (price > 0) {
                            itemPricesCache.put(name, price);
                        }
                    }
                    System.out.println("✅ Цены успешно загружены с CSGOMarket! В базе " + itemPricesCache.size() + " скинов.");
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка CSGOMarket: " + e.getMessage());
        }
        return false;
    }

    private void fetchFromCSGOFast() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.csgofast.com/price/all"))
                    .header("Accept", "application/json")
                    .header("User-Agent", "SkinCasino-Server")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());

                root.fields().forEachRemaining(entry -> {
                    String name = entry.getKey();
                    double price = entry.getValue().asDouble(0.0);
                    if (price > 0) {
                        itemPricesCache.put(name, price);
                    }
                });
                System.out.println("✅ Цены загружены с CSGOFast! В базе " + itemPricesCache.size() + " скинов.");
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка CSGOFast: " + e.getMessage());
        }
    }

    public String getLoginUrl(String baseUrl) {
        return "https://steamcommunity.com/openid/login" +
                "?openid.ns=http://specs.openid.net/auth/2.0" +
                "&openid.mode=checkid_setup" +
                "&openid.return_to=" + baseUrl + "/auth/steam/callback" +
                "&openid.realm=" + baseUrl +
                "&openid.identity=http://specs.openid.net/auth/2.0/identifier_select" +
                "&openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select";
    }

    public String verify(Map<String, String[]> parameterMap) {
        try {
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue()[0];

                if (key.startsWith("openid.")) {
                    if (postData.length() > 0) postData.append("&");
                    postData.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
                    postData.append("=");

                    if (key.equals("openid.mode")) {
                        postData.append(URLEncoder.encode("check_authentication", StandardCharsets.UTF_8));
                    } else {
                        postData.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
                    }
                }
            }

            URL url = new URL("https://steamcommunity.com/openid/login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(postData.toString());
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = reader.lines().collect(Collectors.joining("\n"));

            if (response.contains("is_valid:true")) {
                String claimedId = parameterMap.get("openid.claimed_id")[0];
                return claimedId.substring(claimedId.lastIndexOf("/") + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, String> getSteamUserData(String steamId) {
        Map<String, String> data = new HashMap<>();
        data.put("steamId", steamId);
        data.put("personaname", "User_" + steamId);
        data.put("avatarfull", "https://avatars.steamstatic.com/fef49e7fa7e1997310d705b2a6158ff8dc1cdfeb_full.jpg");

        try {
            String apiUrl = "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key="
                    + steamApiKey + "&steamids=" + steamId;

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String jsonResponse = reader.lines().collect(Collectors.joining("\n"));

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(jsonResponse);
                JsonNode players = root.path("response").path("players");

                if (players.isArray() && players.size() > 0) {
                    JsonNode player = players.get(0);
                    data.put("personaname", player.path("personaname").asText());
                    data.put("avatarfull", player.path("avatarfull").asText());
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка получения данных Steam: " + e.getMessage());
        }
        return data;
    }

    // --- ИСПРАВЛЕННОЕ ПОЛУЧЕНИЕ ИНВЕНТАРЯ ИЗ STEAM С ЖЕСТКИМ ТАЙМАУТОМ ---
    public List<Map<String, Object>> getUserInventory(String steamId) {
        List<Map<String, Object>> inventory = new ArrayList<>();

        if (steamId == null || steamId.isEmpty()) {
            return getMockInventory();
        }

        try {
            URL url = new URL("https://steamcommunity.com/inventory/" + steamId + "/730/2?l=english&count=100");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            // ВАЖНО: Ставим жесткий лимит в 3 секунды. Если Steam зависнет, мы разорвем соединение!
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String jsonResponse = reader.lines().collect(Collectors.joining("\n"));

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(jsonResponse);

                JsonNode assets = root.path("assets");
                JsonNode descriptions = root.path("descriptions");

                if (assets.isArray() && descriptions.isArray() && assets.size() > 0) {
                    for (JsonNode asset : assets) {
                        String classid = asset.path("classid").asText();
                        String instanceid = asset.path("instanceid").asText();
                        String assetid = asset.path("assetid").asText();

                        for (JsonNode desc : descriptions) {
                            if (desc.path("classid").asText().equals(classid) &&
                                    desc.path("instanceid").asText().equals(instanceid) &&
                                    desc.path("tradable").asInt() == 1) {

                                String name = desc.path("market_hash_name").asText();
                                String iconUrl = "https://community.cloudflare.steamstatic.com/economy/image/" + desc.path("icon_url").asText();

                                double realPriceUsd = itemPricesCache.getOrDefault(name, 0.0);
                                if (realPriceUsd == 0.0) {
                                    realPriceUsd = Math.abs((double) name.hashCode() % 5000) / 100.0 + 0.50;
                                }

                                Map<String, Object> item = new HashMap<>();
                                item.put("id", assetid);
                                item.put("name", name);
                                item.put("priceUsd", Math.round(realPriceUsd * 100.0) / 100.0);
                                item.put("img", iconUrl);

                                inventory.add(item);
                                break;
                            }
                        }
                    }
                    return inventory;
                }
            } else {
                System.err.println("❌ Steam заблокировал запрос (Код: " + conn.getResponseCode() + ")");
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка при обращении к Steam (Таймаут или Блокировка): " + e.getMessage());
        }

        // Если Стим повис, не ответил за 3 секунды или выдал ошибку — отдаем резервный инвентарь!
        System.out.println("⚠️ Выдаем резервный тестовый инвентарь...");
        return getMockInventory();
    }

    // Вспомогательный метод для резервных предметов
    private List<Map<String, Object>> getMockInventory() {
        List<Map<String, Object>> mock = new ArrayList<>();
        String[] names = {"AK-47 | Redline (Field-Tested)", "AWP | Asiimov (Field-Tested)", "USP-S | Cortex (Minimal Wear)", "M4A1-S | Cyrex (Factory New)"};
        String[] imgs = {
                "https://community.cloudflare.steamstatic.com/economy/image/-9a81dlWLwJ2UUGcVs_nsVtzdOEdtWwKGZZLQHTxDZ7I56KU0Zwwo4NUX4oFJZEHLbXH5ApeO4YmlhxYQknCRvCo04DEVlxkKgpot7HxfDhjxszJemkV092lnYmGmOHLPr7Vn35cppQiiOuQpoml3wW18xdtZz3xd9CQdwM_ZlrT-lW-k-u-1sS4vJ3KyXYxvyV05HfcM0D0Q_w/200fx200f",
                "https://community.cloudflare.steamstatic.com/economy/image/-9a81dlWLwJ2UUGcVs_nsVtzdOEdtWwKGZZLQHTxDZ7I56KU0Zwwo4NUX4oFJZEHLbXH5ApeO4YmlhxYQknCRvCo04DEVlxkKgpot621FAR17PLfYQJD_9W7m5a0mvLwOq7c2G1Qv8B1teHE9Jrsxlfl_RVuYGmIcgLIIA85aFzUqVC9wr_p15S5vJrMmyQ36XQm5S3enBGx00sdcKUx0jw/200fx200f",
                "https://community.cloudflare.steamstatic.com/economy/image/-9a81dlWLwJ2UUGcVs_nsVtzdOEdtWwKGZZLQHTxDZ7I56KU0Zwwo4NUX4oFJZEHLbXH5ApeO4YmlhxYQknCRvCo04DEVlxkKgpoo6m1FBRp3_bNdFA09tq7k5O0h-n_MrbQh3tV18h0juDU-MKn2QDjqRFlMmHycNeUcwZoaQuDqVbvl726jZLt6M6YzyNg7CR25XnfnhHk0k4ebbcpg_M/200fx200f",
                "https://community.cloudflare.steamstatic.com/economy/image/-9a81dlWLwJ2UUGcVs_nsVtzdOEdtWwKGZZLQHTxDZ7I56KU0Zwwo4NUX4oFJZEHLbXH5ApeO4YmlhxYQknCRvCo04DEVlxkKgpou-6kejhjxszfjgZ06tW4q42Gl_bnN7vCgH5u5cR-iqr--YXyjwW28xdpN2incNTAcgU-YgvQrwLok-3r1Me-vZmdmCww6XEl5S2InxeyihE-/200fx200f"
        };

        for(int i=0; i<4; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", "mock_" + i);
            item.put("name", names[i]);
            item.put("priceUsd", itemPricesCache.getOrDefault(names[i], 15.00));
            item.put("img", imgs[i]);
            mock.add(item);
        }
        return mock;
    }
}