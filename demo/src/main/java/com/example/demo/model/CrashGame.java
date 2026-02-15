package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "crash_history")
public class CrashGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double crashPoint;

    private LocalDateTime playedAt;

    public CrashGame() {}

    public CrashGame(Double crashPoint) {
        this.crashPoint = crashPoint;
        this.playedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Double getCrashPoint() { return crashPoint; }
    public LocalDateTime getPlayedAt() { return playedAt; }
}