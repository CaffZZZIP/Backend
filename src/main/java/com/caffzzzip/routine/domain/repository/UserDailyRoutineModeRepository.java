package com.caffzzzip.routine.domain.repository;

import com.caffzzzip.routine.domain.UserDailyRoutineMode;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface UserDailyRoutineModeRepository extends JpaRepository<UserDailyRoutineMode, Long> {

    Optional<UserDailyRoutineMode> findByUserIdAndTargetDate(Long userId, LocalDate targetDate);


    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM UserDailyRoutineMode u WHERE u.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}