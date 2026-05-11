package com.caffzzzip.intake.domain;

import com.caffzzzip.menu.domain.Menu;
import com.caffzzzip.routine.domain.RoutineType;
import com.caffzzzip.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "intake_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IntakeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 섭취한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 섭취한 메뉴
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    // 평일/주말 루틴 기준
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoutineType routineType;

    // 실제 섭취 시간
    @Column(nullable = false)
    private LocalDateTime intakeAt;

    // 수량
    @Column(nullable = false)
    private Integer quantity;

    // 총 카페인량 = 메뉴 카페인량 * 수량
    @Column(nullable = false)
    private Integer totalCaffeine;

    @Builder
    public IntakeLog(User user,
                     Menu menu,
                     RoutineType routineType,
                     LocalDateTime intakeAt,
                     Integer quantity,
                     Integer totalCaffeine) {
        this.user = user;
        this.menu = menu;
        this.routineType = routineType;
        this.intakeAt = intakeAt;
        this.quantity = quantity;
        this.totalCaffeine = totalCaffeine;
    }
}