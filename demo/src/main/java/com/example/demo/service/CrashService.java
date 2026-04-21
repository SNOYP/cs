package com.example.demo.service;

import com.example.demo.model.BetRecord;
import com.example.demo.model.CrashGame;
import com.example.demo.model.GameState;
import com.example.demo.model.User;
import com.example.demo.repository.BetRecordRepository;
import com.example.demo.repository.CrashGameRepository;
import com.example.demo.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class CrashService {

    public static class Bet {
        public String username;
        public Long amount;
        public int slot;
        public Double cashoutMultiplier;
        public Long profit;
        public String avatarUrl;

        public Bet(String username, Long amount, int slot, String avatarUrl) {
            this.username = username;
            this.amount = amount;
            this.slot = slot;
            this.cashoutMultiplier = null;
            this.profit = null;
            this.avatarUrl = avatarUrl;
        }
    }

    private final CrashGameRepository crashGameRepository;
    private final UserRepository userRepository;
    private final BetRecordRepository betRecordRepository;
    private final SecureRandom random = new SecureRandom();

    private GameState currentState = GameState.WAITING;
    private double currentCrashPoint = 0.0;
    private long stateStartTime = System.currentTimeMillis();
    private List<CrashGame> cachedHistory;
    private final List<Bet> currentRoundBets = new CopyOnWriteArrayList<>();

    public CrashService(CrashGameRepository crashGameRepository, UserRepository userRepository, BetRecordRepository betRecordRepository) {
        this.crashGameRepository = crashGameRepository;
        this.userRepository = userRepository;
        this.betRecordRepository = betRecordRepository;
    }

    @PostConstruct
    public void init() {
        this.cachedHistory = crashGameRepository.findTop20ByOrderByIdDesc();
    }

    @Scheduled(fixedRate = 100)
    public void gameLoop() {
        long now = System.currentTimeMillis();
        long elapsed = now - stateStartTime;

        switch (currentState) {
            case WAITING -> { if (elapsed >= 10000) startGame(); }
            case RUNNING -> {
                double currentMultiplier = calculateMultiplier(elapsed / 1000.0);
                if (currentMultiplier >= currentCrashPoint) crash();
            }
            case CRASHED -> { if (elapsed >= 3000) resetToWaiting(); }
        }
    }

    private void startGame() {
        double chance = random.nextDouble() * 100;
        double result;
        if (chance < 10.0) result = 1.00;
        else if (chance < 80.0) result = 1.01 + (random.nextDouble() * 0.79);
        else if (chance < 97.0) result = 1.80 + (random.nextDouble() * 1.70);
        else result = 3.50 + (random.nextDouble() * 15.0);

        this.currentCrashPoint = Math.floor(result * 100) / 100.0;
        this.currentState = GameState.RUNNING;
        this.stateStartTime = System.currentTimeMillis();
    }

    private void crash() {
        this.currentState = GameState.CRASHED;
        this.stateStartTime = System.currentTimeMillis();
        crashGameRepository.save(new CrashGame(currentCrashPoint));
        this.cachedHistory = crashGameRepository.findTop20ByOrderByIdDesc();

        LocalDateTime now = LocalDateTime.now();
        for (Bet bet : currentRoundBets) {
            BetRecord record = new BetRecord();
            record.setUsername(bet.username);
            record.setAmount(bet.amount);
            record.setCashoutMultiplier(bet.cashoutMultiplier);
            record.setProfit(bet.profit);
            record.setCrashPoint(currentCrashPoint);
            record.setPlayedAt(now);
            betRecordRepository.save(record);
        }
    }

    private void resetToWaiting() {
        this.currentState = GameState.WAITING;
        this.stateStartTime = System.currentTimeMillis();
        this.currentRoundBets.clear();
    }

    public double calculateMultiplier(double seconds) {
        return Math.pow(1.07, seconds);
    }

    public void placeBet(String username, Long amount, int slot) throws Exception {
        if (currentState != GameState.WAITING) throw new Exception("Раунд уже начался!");
        if (amount < 10) throw new Exception("Мин. ставка 10 монет!");

        boolean alreadyBet = currentRoundBets.stream().anyMatch(b -> b.username.equals(username) && b.slot == slot);
        if (alreadyBet) throw new Exception("Ставка уже сделана!");

        User user = userRepository.findByUsername(username).orElseThrow();
        if (user.getBalance() < amount) throw new Exception("Недостаточно монет!");

        user.setBalance(user.getBalance() - amount);
        userRepository.save(user);
        currentRoundBets.add(new Bet(username, amount, slot, user.getAvatarUrl()));
    }

    // --- НОВЫЙ МЕТОД: ОТМЕНА СТАВКИ ---
    public void cancelBet(String username, int slot) throws Exception {
        if (currentState != GameState.WAITING) throw new Exception("Игра уже началась, отмена невозможна!");

        Bet userBet = currentRoundBets.stream()
                .filter(b -> b.username.equals(username) && b.slot == slot)
                .findFirst()
                .orElseThrow(() -> new Exception("Ставка не найдена!"));

        currentRoundBets.remove(userBet);

        User user = userRepository.findByUsername(username).orElseThrow();
        user.setBalance(user.getBalance() + userBet.amount);
        userRepository.save(user);
    }

    public void cashOut(String username, int slot) throws Exception {
        if (currentState != GameState.RUNNING) throw new Exception("Игра не идет!");
        long now = System.currentTimeMillis();
        double currentMultiplier = calculateMultiplier((now - stateStartTime) / 1000.0);
        if (currentMultiplier >= currentCrashPoint) throw new Exception("Слишком поздно!");

        Bet userBet = currentRoundBets.stream()
                .filter(b -> b.username.equals(username) && b.slot == slot)
                .findFirst().orElseThrow(() -> new Exception("Ставка не найдена!"));

        if (userBet.cashoutMultiplier != null) throw new Exception("Уже забрали!");

        long win = (long) (userBet.amount * currentMultiplier);
        userBet.cashoutMultiplier = currentMultiplier;
        userBet.profit = win;

        User user = userRepository.findByUsername(username).orElseThrow();
        user.setBalance(user.getBalance() + win);
        userRepository.save(user);
    }

    public Map<String, Object> getStatus(String username) {
        long now = System.currentTimeMillis();
        double elapsedSeconds = (now - stateStartTime) / 1000.0;
        double currentMult = (currentState == GameState.RUNNING) ? calculateMultiplier(elapsedSeconds) : (currentState == GameState.CRASHED ? currentCrashPoint : 1.0);

        Map<String, Object> response = new HashMap<>();
        response.put("status", currentState);
        response.put("multiplier", currentMult);
        response.put("elapsed", elapsedSeconds);
        response.put("history", cachedHistory);
        response.put("bets", currentRoundBets);
        return response;
    }
}