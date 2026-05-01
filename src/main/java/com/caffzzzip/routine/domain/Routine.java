package com.caffzzzip.routine.domain;

import com.caffzzzip.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Getter
@Table(name = "routines")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Routine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 루틴을 설정한 사용자
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // 평일 기상 시간
    @Column(nullable = false)
    private LocalTime weekdayWakeTime;

    // 평일 취침 시간
    @Column(nullable = false)
    private LocalTime weekdaySleepTime;

    // 주말 기상 시간
    @Column(nullable = false)
    private LocalTime weekendWakeTime;

    // 주말 취침 시간
    @Column(nullable = false)
    private LocalTime weekendSleepTime;

    // 카페인 민감도
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaffeineSensitivity caffeineSensitivity;

    // 카페인 섭취 빈도
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IntakeFrequency intakeFrequency;

    @Builder
    public Routine(User user,
                   LocalTime weekdayWakeTime,
                   LocalTime weekdaySleepTime,
                   LocalTime weekendWakeTime,
                   LocalTime weekendSleepTime,
                   CaffeineSensitivity caffeineSensitivity,
                   IntakeFrequency intakeFrequency) {
        this.user = user;
        this.weekdayWakeTime = weekdayWakeTime;
        this.weekdaySleepTime = weekdaySleepTime;
        this.weekendWakeTime = weekendWakeTime;
        this.weekendSleepTime = weekendSleepTime;
        this.caffeineSensitivity = caffeineSensitivity;
        this.intakeFrequency = intakeFrequency;
    }
}