package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.DepositService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/deposit")
public class DepositController {

    private final UserRepository userRepository;
    private final DepositService depositService;

    public DepositController(UserRepository userRepository, DepositService depositService) {
        this.userRepository = userRepository;
        this.depositService = depositService;
    }

    @PostMapping("/create")
    public Map<String, Object> createDeposit(@RequestParam Long amountCoins,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return Map.of("error", "Пользователь не найден");

        // --- ПРОВЕРКА НА БАН ---
        if (user.isBanned()) {
            return Map.of("error", "ВАШ АККАУНТ ЗАБЛОКИРОВАН ЗА МОШЕННИЧЕСТВО!");
        }

        if (depositService.getPendingDeposits().containsKey(username)) {
            return Map.of("error", "У вас уже есть активная заявка. Дождитесь проверки!");
        }

        // Создаем НАСТОЯЩУЮ заявку
        depositService.createDeposit(username, amountCoins);

        return Map.of("success", true);
    }

    @GetMapping("/status")
    public Map<String, Object> checkStatus(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        String status = depositService.getStatus(username);

        if ("ACCEPTED".equals(status)) {
            depositService.clearStatus(username);
            User user = userRepository.findByUsername(username).orElseThrow();
            return Map.of("status", "ACCEPTED", "newBalance", user.getBalance());
        } else if ("BANNED".equals(status)) {
            depositService.clearStatus(username);
            return Map.of("status", "BANNED");
        }

        return Map.of("status", status);
    }
}