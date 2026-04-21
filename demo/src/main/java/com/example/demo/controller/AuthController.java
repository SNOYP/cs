package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.SteamService;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
    public RedirectView login(HttpServletRequest request) {
        // Динамически получаем адрес сайта, с которого зашел игрок (включая порт)
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

        return new RedirectView(steamService.getLoginUrl(baseUrl));
    }

    @GetMapping("/callback")
    public String callback(HttpServletRequest request) {
        String steamId = steamService.verify(request.getParameterMap());

        if (steamId != null) {
            // 1. Получаем полные данные (ник + аватар)
            var steamData = steamService.getSteamUserData(steamId);

            // 2. Регистрируем или обновляем юзера
            User user = userService.processSteamLogin(steamData);

            // 3. Авторизуем в Spring Security
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authToken);
            SecurityContextHolder.setContext(context);

            // Важно: сохраняем сессию
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", context);

            return "redirect:/";
        }

        return "redirect:/login?error";
    }
}