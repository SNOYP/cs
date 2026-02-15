package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CrashService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController // Используем RestController для AJAX-запросов
@RequestMapping("/api/crash")
public class CrashController {

    private final CrashService crashService;
    private final UserRepository userRepository;

    public CrashController(CrashService crashService, UserRepository userRepository) {
        this.crashService = crashService;
        this.userRepository = userRepository;
    }

    @PostMapping("/play")
    public Map<String, Object> play(@RequestParam Long bet, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        if (user.getBalance() < bet) {
            return Map.of("error", "Недостаточно монет!");
        }

        // Снимаем ставку сразу
        user.setBalance(user.getBalance() - bet);
        userRepository.save(user);

        // Генерируем точку краша
        double crashPoint = crashService.generateCrashPoint();

        return Map.of(
                "crashPoint", crashPoint,
                "newBalance", user.getBalance()
        );
    }
}