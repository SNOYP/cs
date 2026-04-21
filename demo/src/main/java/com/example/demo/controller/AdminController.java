package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.DepositService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final DepositService depositService;

    public AdminController(UserRepository userRepository, DepositService depositService) {
        this.userRepository = userRepository;
        this.depositService = depositService;
    }

    @GetMapping
    public String adminPanel(Model model) {
        model.addAttribute("users", userRepository.findAll());
        // Передаем активные трейды в админку
        model.addAttribute("pendingDeposits", depositService.getPendingDeposits());
        return "admin";
    }

    @PostMapping("/add-balance")
    public String addBalance(@RequestParam String username, @RequestParam Long amount) {
        try {
            User user = userRepository.findByUsername(username).orElseThrow(() -> new Exception("Игрок не найден!"));
            user.setBalance(user.getBalance() + amount);
            userRepository.save(user);
            return "redirect:/admin?success=" + URLEncoder.encode("Выдано " + amount + " монет", StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "redirect:/admin?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }

    // --- ОДОБРЕНИЕ ТРЕЙДА ---
    @PostMapping("/approve-deposit")
    public String approveDeposit(@RequestParam String username) {
        Long amount = depositService.getPendingDeposits().get(username);
        if (amount != null) {
            User user = userRepository.findByUsername(username).orElseThrow();
            user.setBalance(user.getBalance() + amount);
            userRepository.save(user);

            depositService.removeDeposit(username);
            depositService.setStatus(username, "ACCEPTED");
            return "redirect:/admin?success=" + URLEncoder.encode("Трейд одобрен, монеты начислены!", StandardCharsets.UTF_8);
        }
        return "redirect:/admin?error=" + URLEncoder.encode("Заявка не найдена", StandardCharsets.UTF_8);
    }

    // --- БАН ПОЛЬЗОВАТЕЛЯ ЗА СКАМ ---
    @PostMapping("/ban-user")
    public String banUser(@RequestParam String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        user.setBanned(true); // НАВСЕГДА В БАН
        userRepository.save(user);

        depositService.removeDeposit(username);
        depositService.setStatus(username, "BANNED");
        return "redirect:/admin?success=" + URLEncoder.encode("ПОЛЬЗОВАТЕЛЬ " + username + " ЗАБАНЕН", StandardCharsets.UTF_8);
    }
}