package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.SteamService;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/auth/steam")
public class AuthController {

    private final SteamService steamService;
    private final UserService userService;
    private final UserDetailsService userDetailsService;

    public AuthController(SteamService steamService, UserService userService, UserDetailsService userDetailsService) {
        this.steamService = steamService;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }

    // 1. Игрок нажимает кнопку -> Летит на сайт Steam
    @GetMapping("/login")
    public RedirectView login() {
        return new RedirectView(steamService.getLoginUrl());
    }

    // 2. Игрок возвращается -> Мы его проверяем и кидаем на главную
    @GetMapping("/callback")
    public String callback(HttpServletRequest request) {
        // Проверяем, настоящий ли это Steam
        String steamId = steamService.verify(request.getParameterMap());

        if (steamId != null) {
            // Если все честно:
            // 1. Создаем игрока в базе (или находим старого)
            String username = "Player_" + steamId.substring(steamId.length() - 5);
            User user = userService.processSteamLogin(steamId, username);

            // 2. Говорим Spring Security: "Это свой, пропусти его"
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authToken);

            // 3. ПЕРЕНАПРАВЛЯЕМ НА ГЛАВНУЮ СТРАНИЦУ (Где игры)
            return "redirect:/";
        }

        // Если ошибка - кидаем обратно на логин
        return "redirect:/login?error";
    }
}