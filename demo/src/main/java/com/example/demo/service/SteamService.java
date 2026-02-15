package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SteamService {

    private static final String STEAM_OPENID_URL = "https://steamcommunity.com/openid/login";
    // ВАЖНО: Тут должен быть твой адрес. Пока локалхост - пишем так.
    // Когда зальешь на сервер - поменяешь на свой домен.
    private static final String CALLBACK_URL = "http://localhost:9090/auth/steam/callback";

    // 1. Создаем ссылку, по которой перейдет пользователь
    public String getLoginUrl() {
        return STEAM_OPENID_URL +
                "?openid.ns=http://specs.openid.net/auth/2.0" +
                "&openid.mode=checkid_setup" +
                "&openid.return_to=" + CALLBACK_URL +
                "&openid.realm=" + "http://localhost:9090" +
                "&openid.identity=http://specs.openid.net/auth/2.0/identifier_select" +
                "&openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select";
    }

    // 2. Проверяем ответ от Steam (валидация)
    public String verify(Map<String, String[]> parameterMap) {
        try {
            // 1. Подготавливаем данные для проверки (меняем mode на check_authentication)
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue()[0];
                if (key.equals("openid.mode")) {
                    postData.append("openid.mode=check_authentication&");
                } else {
                    postData.append(key).append("=").append(value).append("&");
                }
            }

            // 2. Настраиваем подключение
            URL url = new URL(STEAM_OPENID_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true); // Разрешаем отправку данных в теле

            // ВАЖНО: Указываем заголовки, чтобы избежать ошибки 411
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

            // 3. Отправляем данные
            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(postDataBytes);
            }

            // 4. Читаем ответ
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = reader.lines().collect(Collectors.joining("\n"));

            if (response.contains("is_valid:true")) {
                String claimedId = parameterMap.get("openid.claimed_id")[0];
                return claimedId.substring(claimedId.lastIndexOf("/") + 1);
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка при верификации Steam: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}