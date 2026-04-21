package com.example.demo.service;

import com.example.demo.model.PromoCode;
import com.example.demo.model.User;
import com.example.demo.repository.PromoCodeRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromoCodeService {
    private final PromoCodeRepository promoCodeRepository;
    private final UserRepository userRepository;

    public PromoCodeService(PromoCodeRepository promoCodeRepository, UserRepository userRepository) {
        this.promoCodeRepository = promoCodeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void applyPromoCode(String username, String code) throws Exception {
        User user = userRepository.findByUsername(username).orElseThrow();
        PromoCode promo = promoCodeRepository.findByCode(code).orElseThrow(() -> new Exception("Код не найден"));

        if (user.getUsedPromoCodes().contains(promo)) throw new Exception("Уже использовано");
        if (promo.getCurrentUses() >= promo.getMaxUses()) throw new Exception("Лимит исчерпан");

        user.setBalance(user.getBalance() + promo.getRewardAmount());
        user.getUsedPromoCodes().add(promo);
        userRepository.save(user);

        promo.setCurrentUses(promo.getCurrentUses() + 1);
        promoCodeRepository.save(promo);
    }
}