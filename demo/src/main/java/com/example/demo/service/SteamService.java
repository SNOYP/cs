package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SteamService {

    private static final String STEAM_OPENID_URL = "https://steamcommunity.com/openid/login";
    private static final String CALLBACK_URL = "http://localhost:9090/auth/steam/callback";

    @Value("${steam.api.key}")
    private String steamApiKey;

    public String getLoginUrl() {
        return STEAM_OPENID_URL +
                "?openid.ns=http://specs.openid.net/auth/2.0" +
                "&openid.mode=checkid_setup" +
                "&openid.return_to=" + CALLBACK_URL +
                "&openid.realm=" + "http://localhost:9090" +
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

            URL url = new URL(STEAM_OPENID_URL);
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

    // --- НОВЫЙ МЕТОД: Возвращает Map с данными (ник + аватар) ---
    public Map<String, String> getSteamUserData(String steamId) {
        Map<String, String> data = new HashMap<>();
        data.put("steamId", steamId);
        // Заглушки на случай ошибки
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
                    data.put("avatarfull", player.path("avatarfull").asText()); // Берем аватарку
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка получения данных Steam: " + e.getMessage());
        }
        return data;
    }
}