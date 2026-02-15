package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Service
public class CrashService {

    private final SecureRandom random = new SecureRandom();

    public double generateCrashPoint() {
        double houseEdge = 0.04; // Твои 4% преимущества казино
        double rand = random.nextDouble(); // Безопасный рандом от 0.0 до 1.0

        // Твоя формула:
        double result = (1 - houseEdge) / (1 - rand);

        // Ограничение: не даем вылететь больше 10000x и округляем
        if (result > 10000.0) result = 10000.0;
        if (result < 1.0) result = 1.0; // Чтобы не было чисел меньше единицы

        return Math.floor(result * 100) / 100.0;
    }
}