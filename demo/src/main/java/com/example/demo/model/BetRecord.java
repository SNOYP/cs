package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bet_history")
public class BetRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private Long amount;
    private Double cashoutMultiplier;
    private Long profit;
    private Double crashPoint;
    private LocalDateTime playedAt;

    public BetRecord() {}
    // Геттеры и сеттеры
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public Double getCashoutMultiplier() { return cashoutMultiplier; }
    public void setCashoutMultiplier(Double cashoutMultiplier) { this.cashoutMultiplier = cashoutMultiplier; }
    public Long getProfit() { return profit; }
    public void setProfit(Long profit) { this.profit = profit; }
    public Double getCrashPoint() { return crashPoint; }
    public void setCrashPoint(Double crashPoint) { this.crashPoint = crashPoint; }
    public LocalDateTime getPlayedAt() { return playedAt; }
    public void setPlayedAt(LocalDateTime playedAt) { this.playedAt = playedAt; }
}