package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.SteamService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final SteamService steamService;
    private final UserRepository userRepository;

    public InventoryController(SteamService steamService, UserRepository userRepository) {
        this.steamService = steamService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Map<String, Object>> getInventory(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        // Вызываем загрузку инвентаря по SteamID игрока
        return steamService.getUserInventory(user.getSteamId());
    }
}