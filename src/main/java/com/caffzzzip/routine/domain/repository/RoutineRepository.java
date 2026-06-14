package com.caffzzzip.routine.domain.repository;

import com.caffzzzip.routine.domain.Routine;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoutineRepository extends JpaRepository<Routine, Long> {

    Optional<Routine> findByUserId(Long userId);

    boolean existsByUserId(Long userId);


    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Routine r WHERE r.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}