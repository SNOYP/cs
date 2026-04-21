package com.example.demo.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

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
    private String avatarUrl;
    private String tradeUrl;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_promo_codes",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "promo_code_id")
    )
    private Set<PromoCode> usedPromoCodes = new HashSet<>();

    public User() {}

    // Геттеры и сеттеры
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
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getTradeUrl() { return tradeUrl; }
    public void setTradeUrl(String tradeUrl) { this.tradeUrl = tradeUrl; }
    public Set<PromoCode> getUsedPromoCodes() { return usedPromoCodes; }
    public void setUsedPromoCodes(Set<PromoCode> usedPromoCodes) { this.usedPromoCodes = usedPromoCodes; }
}