package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String steamId;
    private Long balance = 0L;
    private String role;

    // --- НОВЫЕ ПОЛЯ ---
    private String avatarUrl; // Ссылка на картинку из Steam
    private String tradeUrl;  // Ссылка на обмен
    // ------------------

    public User() {
    }

    // Геттеры и Сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getSteamId() { return steamId; }
    public void setSteamId(String steamId) { this.steamId = steamId; }

    public Long getBalance() { return balance; }
    public void setBalance(Long balance) { this.balance = balance; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // --- НОВЫЕ ГЕТТЕРЫ/СЕТТЕРЫ ---
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getTradeUrl() { return tradeUrl; }
    public void setTradeUrl(String tradeUrl) { this.tradeUrl = tradeUrl; }
}