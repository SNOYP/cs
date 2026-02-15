package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/crash")
public class CrashController {

    private final CrashService crashService;
    private final UserRepository userRepository;

    public CrashController(CrashService crashService, UserRepository userRepository) {
        this.crashService = crashService;
        this.userRepository = userRepository;
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> status = crashService.getStatus(userDetails.getUsername());

        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        // Создаем новый HashMap на основе статуса и добавляем баланс
        Map<String, Object> response = new HashMap<>(status);
        response.put("balance", user.getBalance());

        return response;
    }

    @PostMapping("/bet")
    public Map<String, Object> bet(@RequestParam Long amount, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            crashService.placeBet(userDetails.getUsername(), amount);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    @PostMapping("/cashout")
    public Map<String, Object> cashout(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            crashService.cashOut(userDetails.getUsername());
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }
}