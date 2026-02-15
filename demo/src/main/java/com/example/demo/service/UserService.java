package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
            createUser("admin", "admin", "ROLE_ADMIN", 999999L, null);
            System.out.println("✅ ADMIN CREATED");
        }
    }

    // ТОТ САМЫЙ МЕТОД, КОТОРОГО НЕ ХВАТАЛО
    public void registerNewUser(String username, String password) throws Exception {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new Exception("Пользователь с таким логином уже существует!");
        }
        createUser(username, password, "ROLE_USER", 0L, null);
    }

    public User processSteamLogin(String steamId, String username) {
        return userRepository.findBySteamId(steamId).orElseGet(() ->
                createUser(username, null, "ROLE_USER", 0L, steamId)
        );
    }

    // Универсальный метод создания (оптимизация кода)
    private User createUser(String username, String password, String role, Long balance, String steamId) {
        User user = new User();
        user.setUsername(username);
        if (password != null) user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setBalance(balance);
        user.setSteamId(steamId);
        return userRepository.save(user);
    }
}