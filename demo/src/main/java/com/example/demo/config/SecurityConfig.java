package com.example.demo.config;

import com.example.demo.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Отключаем лишние защиты для разработки
                .csrf(csrf -> csrf.disable())

                // 2. Настраиваем доступы (КТО КУДА МОЖЕТ ЗАХОДИТЬ)
                .authorizeHttpRequests(auth -> auth
                        // ВАЖНО: Разрешаем всем вход, регистрацию, картинки и СТРАНИЦУ ОШИБОК (/error)
                        .requestMatchers("/login", "/register", "/auth/**", "/css/**", "/js/**", "/images/**", "/error").permitAll()

                        // В админку - только админ
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // На все остальные страницы - только после входа
                        .anyRequest().authenticated()
                )

                // 3. Настройка формы входа
                .formLogin(form -> form
                        .loginPage("/login") // Указываем адрес нашей страницы
                        .defaultSuccessUrl("/", true) // Куда кидать после успеха
                        .permitAll() // ВАЖНО: Разрешаем доступ к самой форме всем!
                )

                // 4. Настройка выхода
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .permitAll()
                );

        return http.build();
    }

    // --- СТАНДАРТНЫЕ НАСТРОЙКИ (Не меняем) ---

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepo) {
        return username -> userRepo.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword() != null ? user.getPassword() : "")
                        .roles(user.getRole().replace("ROLE_", ""))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(encoder);
        return provider;
    }
}