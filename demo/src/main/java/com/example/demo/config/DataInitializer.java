package com.example.demo.config;

import com.example.demo.model.PromoCode;
import com.example.demo.model.User;
import com.example.demo.repository.PromoCodeRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PromoCodeRepository promoCodeRepository;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, PromoCodeRepository promoCodeRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.promoCodeRepository = promoCodeRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Создаем админа
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole("ROLE_ADMIN");
            admin.setBalance(999999L);
            userRepository.save(admin);
            System.out.println("✅ ADMIN CREATED");
        }

        // Создаем тестовый промокод
        if (promoCodeRepository.findByCode("FREE1000").isEmpty()) {
            PromoCode promo = new PromoCode();
            promo.setCode("FREE1000");
            promo.setRewardAmount(1000L); // Даст 1000 монет
            promo.setMaxUses(100);        // На 100 активаций
            promo.setCurrentUses(0);
            promoCodeRepository.save(promo);
            System.out.println("✅ PROMO CODE 'FREE1000' CREATED");
        }
    }
}