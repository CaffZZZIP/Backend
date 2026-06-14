package com.caffzzzip.intake.domain.repository;

import com.caffzzzip.intake.domain.IntakeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface IntakeLogRepository extends JpaRepository<IntakeLog, Long> {

    List<IntakeLog> findByUserIdAndIntakeAtBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );


    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM IntakeLog i WHERE i.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}