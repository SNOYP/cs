package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DepositService {
    // Хранит Никнейм -> Сумма монет
    private final Map<String, Long> pendingDeposits = new ConcurrentHashMap<>();
    // Хранит Никнейм -> Статус ("WAITING", "ACCEPTED", "BANNED")
    private final Map<String, String> tradeStatuses = new ConcurrentHashMap<>();

    public void createDeposit(String username, Long amount) {
        pendingDeposits.put(username, amount);
        tradeStatuses.put(username, "WAITING");
    }

    public Map<String, Long> getPendingDeposits() {
        return pendingDeposits;
    }

    public String getStatus(String username) {
        return tradeStatuses.getOrDefault(username, "NONE");
    }

    public void setStatus(String username, String status) {
        tradeStatuses.put(username, status);
    }

    public void removeDeposit(String username) {
        pendingDeposits.remove(username);
    }

    public void clearStatus(String username) {
        tradeStatuses.remove(username);
    }
}