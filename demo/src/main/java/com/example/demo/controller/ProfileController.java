package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.BetRecordRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.PromoCodeService;
import com.example.demo.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class ProfileController {
    private final UserRepository userRepository;
    private final UserService userService;
    private final PromoCodeService promoCodeService;
    private final BetRecordRepository betRecordRepository;

    public ProfileController(UserRepository userRepository, UserService userService,
                             PromoCodeService promoCodeService, BetRecordRepository betRecordRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.promoCodeService = promoCodeService;
        this.betRecordRepository = betRecordRepository;
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("totalGames", betRecordRepository.countByUsername(user.getUsername()));
        model.addAttribute("recentGames", betRecordRepository.findTop10ByUsernameOrderByPlayedAtDesc(user.getUsername()));
        return "profile";
    }

    @PostMapping("/profile/promo")
    public String applyPromo(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String code) {
        try {
            promoCodeService.applyPromoCode(userDetails.getUsername(), code);
            return "redirect:/profile?promoSuccess";
        } catch (Exception e) {
            return "redirect:/profile?promoError=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }

    @PostMapping("/profile/trade-url")
    public String updateTradeUrl(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String tradeUrl) {
        userService.updateTradeUrl(userDetails.getUsername(), tradeUrl);
        return "redirect:/profile?success";
    }
}