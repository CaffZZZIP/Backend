package com.caffzzzip.intake.domain.domain.repository;

import com.caffzzzip.intake.domain.IntakeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface IntakeLogRepository extends JpaRepository<IntakeLog, Long> {

    List<IntakeLog> findByUserIdAndIntakeAtBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );
}