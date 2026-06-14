package com.caffzzzip.routine.domain;

import com.caffzzzip.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "user_daily_routine_modes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "target_date"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDailyRoutineMode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoutineType routineType;

    @Builder
    public UserDailyRoutineMode(User user, LocalDate targetDate, RoutineType routineType) {
        this.user = user;
        this.targetDate = targetDate;
        this.routineType = routineType;
    }

    public void updateRoutineType(RoutineType routineType) {
        this.routineType = routineType;
    }
}