package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "promo_codes")
public class PromoCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String code;
    private Long rewardAmount;
    private int maxUses;
    private int currentUses;

    public PromoCode() {}
    // Геттеры и сеттеры (id, code, rewardAmount, maxUses, currentUses)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Long getRewardAmount() { return rewardAmount; }
    public void setRewardAmount(Long rewardAmount) { this.rewardAmount = rewardAmount; }
    public int getMaxUses() { return maxUses; }
    public void setMaxUses(int maxUses) { this.maxUses = maxUses; }
    public int getCurrentUses() { return currentUses; }
    public void setCurrentUses(int currentUses) { this.currentUses = currentUses; }
}