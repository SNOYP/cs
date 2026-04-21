package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CrashService;
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

        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                status.put("balance", user.getBalance());
            }
        }

        return status;
    }

    @PostMapping("/bet")
    public Map<String, Object> bet(@RequestParam Long amount,
                                   @RequestParam int slot,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        try {
            crashService.placeBet(userDetails.getUsername(), amount, slot);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    @PostMapping("/cashout")
    public Map<String, Object> cashout(@RequestParam int slot,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        try {
            crashService.cashOut(userDetails.getUsername(), slot);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }
}