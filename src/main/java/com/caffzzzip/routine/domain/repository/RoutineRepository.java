package com.caffzzzip.routine.domain.repository;

import com.caffzzzip.routine.domain.Routine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoutineRepository extends JpaRepository<Routine, Long> {

    Optional<Routine> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}