package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.SteamService;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession; // Импорт сессии
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
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

    @GetMapping("/login")
    public RedirectView login() {
        return new RedirectView(steamService.getLoginUrl());
    }

    @GetMapping("/callback")
    public String callback(HttpServletRequest request) {
        String steamId = steamService.verify(request.getParameterMap());

        if (steamId != null) {
            String nickname = steamService.getSteamPersonaName(steamId);
            User user = userService.processSteamLogin(steamId, nickname);

            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // Создаем контекст безопасности
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authToken);
            SecurityContextHolder.setContext(context);

            // ВАЖНО: Сохраняем контекст в сессию вручную, чтобы не терялся при редиректе
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", context);

            return "redirect:/";
        }

        return "redirect:/login?error";
    }
}