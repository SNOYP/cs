package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initAdmin() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            createUser("admin", "admin", "ROLE_ADMIN", 999999L, null, null);
            System.out.println("✅ ADMIN CREATED");
        }
    }

    public void registerNewUser(String username, String password) throws Exception {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new Exception("Пользователь существует!");
        }
        createUser(username, password, "ROLE_USER", 0L, null, null);
    }

    // --- ОБНОВЛЕННЫЙ ВХОД ЧЕРЕЗ STEAM ---
    public User processSteamLogin(Map<String, String> steamData) {
        String steamId = steamData.get("steamId");
        String nickname = steamData.get("personaname");
        String avatar = steamData.get("avatarfull");

        Optional<User> existingUser = userRepository.findBySteamId(steamId);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Обновляем данные, если они изменились в стиме (ник или аватарка)
            boolean changed = false;
            if (!user.getUsername().equals(nickname)) {
                user.setUsername(nickname);
                changed = true;
            }
            if (user.getAvatarUrl() == null || !user.getAvatarUrl().equals(avatar)) {
                user.setAvatarUrl(avatar);
                changed = true;
            }
            return changed ? userRepository.save(user) : user;
        } else {
            return createUser(nickname, null, "ROLE_USER", 0L, steamId, avatar);
        }
    }

    // Сохранение ссылки на трейд
    public void updateTradeUrl(String username, String tradeUrl) {
        User user = userRepository.findByUsername(username).orElseThrow();
        user.setTradeUrl(tradeUrl);
        userRepository.save(user);
    }

    private User createUser(String username, String password, String role, Long balance, String steamId, String avatarUrl) {
        User user = new User();
        user.setUsername(username);
        if (password != null) user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setBalance(balance);
        user.setSteamId(steamId);
        user.setAvatarUrl(avatarUrl);
        return userRepository.save(user);
    }
}