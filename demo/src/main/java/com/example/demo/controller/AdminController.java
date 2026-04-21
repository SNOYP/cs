package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
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

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Открываем страницу админки и передаем туда список всех игроков
    @GetMapping
    public String adminPanel(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin";
    }

    // Обработка кнопки "Выдать монеты"
    @PostMapping("/add-balance")
    public String addBalance(@RequestParam String username, @RequestParam Long amount) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new Exception("Игрок с таким ником не найден!"));

            // Прибавляем монеты
            user.setBalance(user.getBalance() + amount);
            userRepository.save(user);

            String msg = "Успешно выдано " + amount + " монет игроку " + username;
            return "redirect:/admin?success=" + URLEncoder.encode(msg, StandardCharsets.UTF_8);

        } catch (Exception e) {
            return "redirect:/admin?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }
}