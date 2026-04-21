package com.example.demo.repository;

import com.example.demo.model.BetRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BetRecordRepository extends JpaRepository<BetRecord, Long> {
    List<BetRecord> findTop10ByUsernameOrderByPlayedAtDesc(String username);
    long countByUsername(String username);
}