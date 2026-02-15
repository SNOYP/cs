package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    private final UserRepository userRepository;

    public MainController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // Достаем актуальные данные юзера из БД
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        model.addAttribute("user", user);
        return "index";
    }
    // Переход на страницу краша
    @GetMapping("/game/crash")
    public String crashPage() {
        return "crash"; // Имя файла в папке templates (crash.html)
    }
    // Консолька админа (доступна только с ролью ADMIN)
    @GetMapping("/admin")
    public String adminPanel() {
        return "admin";
    }
}