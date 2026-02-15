package com.example.demo.repository;

import com.example.demo.model.CrashGame;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CrashGameRepository extends JpaRepository<CrashGame, Long> {
    // Найти последние 20 игр, сортируя от новых к старым
    List<CrashGame> findTop20ByOrderByIdDesc();
}