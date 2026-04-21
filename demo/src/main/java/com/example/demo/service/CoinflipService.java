package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;

@Service
public class CoinflipService {

    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();

    public CoinflipService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Map<String, Object> play(String username, Long amount, String chosenSide) throws Exception {
        if (amount < 10) throw new Exception("Минимальная ставка 10 монет!");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new Exception("Пользователь не найден"));

        if (user.getBalance() < amount) throw new Exception("Недостаточно монет!");

        // 1. Сразу списываем ставку
        user.setBalance(user.getBalance() - amount);

        // 2. Генерируем шанс победы. Игрок побеждает с вероятностью 45% (преимущество казино)
        boolean playerWins = random.nextDouble() * 100 < 45.0;

        String winningSide;
        Long profit = 0L;

        if (playerWins) {
            winningSide = chosenSide;
            profit = amount * 2; // Удвоение ставки
            user.setBalance(user.getBalance() + profit);
        } else {
            // Если игрок проиграл, значит выпала противоположная сторона
            winningSide = chosenSide.equals("CT") ? "T" : "CT";
            profit = -amount;
        }

        userRepository.save(user);

        // Возвращаем результат на клиент
        return Map.of(
                "success", true,
                "winningSide", winningSide,
                "profit", profit,
                "newBalance", user.getBalance()
        );
    }
}