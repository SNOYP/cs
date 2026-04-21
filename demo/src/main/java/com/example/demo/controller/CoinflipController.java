package com.example.demo.controller;

import com.example.demo.service.CoinflipService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/coinflip")
public class CoinflipController {

    private final CoinflipService coinflipService;

    public CoinflipController(CoinflipService coinflipService) {
        this.coinflipService = coinflipService;
    }

    @PostMapping("/play")
    public Map<String, Object> play(@RequestParam Long amount,
                                    @RequestParam String side,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return coinflipService.play(userDetails.getUsername(), amount, side);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }
}