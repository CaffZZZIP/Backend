package com.caffzzzip.routine.domain.repository;

import com.caffzzzip.routine.domain.UserDailyRoutineMode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface UserDailyRoutineModeRepository extends JpaRepository<UserDailyRoutineMode, Long> {


    Optional<UserDailyRoutineMode> findByUserIdAndTargetDate(Long userId, LocalDate targetDate);
}