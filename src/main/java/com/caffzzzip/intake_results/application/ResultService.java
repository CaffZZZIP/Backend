package com.caffzzzip.intake_results.application;

import com.caffzzzip.intake.domain.IntakeLog;
import com.caffzzzip.intake.domain.repository.IntakeLogRepository;
import com.caffzzzip.intake_results.api.dto.DailyReportResponse;
import com.caffzzzip.intake_results.api.dto.ResultItem;
import com.caffzzzip.routine.domain.CaffeineSensitivity;
import com.caffzzzip.routine.domain.Routine;
import com.caffzzzip.routine.domain.RoutineType;
import com.caffzzzip.routine.domain.repository.RoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final IntakeLogRepository intakeLogRepository;
    private final RoutineRepository routineRepository;

    public DailyReportResponse getTodayReport(Long userId) {

        // 사용자 루틴 조회
        Routine routine = routineRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("루틴 정보가 없습니다.")
                );

        // 평일 / 주말 판별
        RoutineType todayType =
                (LocalDate.now().getDayOfWeek().getValue() >= 6)
                        ? RoutineType.WEEKEND
                        : RoutineType.WEEKDAY;

        // 루틴별 취침시간
        String sleepTime =
                todayType == RoutineType.WEEKEND
                        ? routine.getWeekendSleepTime().toString()
                        : routine.getWeekdaySleepTime().toString();

        // 오늘 시작 ~ 내일 시작
        LocalDateTime start =
                LocalDate.now().atStartOfDay();

        LocalDateTime end =
                LocalDate.now().plusDays(1).atStartOfDay();

        // 오늘 섭취 기록 조회
        List<IntakeLog> intakeLogs =
                intakeLogRepository.findByUserIdAndIntakeAtBetween(
                        userId,
                        start,
                        end
                );

        // 응답 리스트 변환
        List<ResultItem> logs = intakeLogs.stream()
                .map(log -> ResultItem.builder()
                        .menuId(log.getMenu().getId())
                        .menuName(log.getMenu().getMenuName())
                        .brand(log.getMenu().getBrand())
                        .caffeineMg(log.getTotalCaffeine())
                        .intakeAt(log.getIntakeAt().toString())
                        .quantity(log.getQuantity())
                        .build())
                .toList();

        // 민감도 기반 반감기 계산
        double sensitivityWeight =
                getSensitivityWeight(
                        routine.getCaffeineSensitivity()
                );

        double halfLifeMinutes =
                300.0 * sensitivityWeight;

        int totalIntake = 0;
        double remainingCaffeine = 0;

        //LocalDateTime now = LocalDateTime.now();

        LocalDateTime now =
                LocalDateTime.of(
                        LocalDate.now(),
                        todayType == RoutineType.WEEKEND
                                ? routine.getWeekendSleepTime()
                                : routine.getWeekdaySleepTime()
                );

        for (ResultItem log : logs) {

            totalIntake += log.getCaffeineMg();

            LocalDateTime intakeTime =
                    LocalDateTime.parse(log.getIntakeAt());

            long minutesElapsed =
                    Duration.between(intakeTime, now).toMinutes();

            if (minutesElapsed > 0) {

                double remaining =
                        log.getCaffeineMg()
                                * Math.pow(
                                0.5,
                                (double) minutesElapsed / halfLifeMinutes
                        );

                remainingCaffeine += remaining;

            } else {

                remainingCaffeine += log.getCaffeineMg();
            }
        }

        // 잔존 카페인 반올림
        int roundedRemaining =
                (int) Math.round(remainingCaffeine);

        // 위험도 계산
        String riskLevel =
                calculateRiskLevel(
                        totalIntake,
                        routine.getCaffeineSensitivity()
                );

        // 분석 메시지
        String message =
                generateAnalysisMessage(
                        logs,
                        remainingCaffeine,
                        sleepTime
                );

        // 최종 응답 반환
        return DailyReportResponse.builder()
                .totalCaffeine(totalIntake)
                .remainingCaffeine(roundedRemaining)
                .riskLevel(riskLevel)
                .recommendedSleepTime(sleepTime)
                .sleepImpactLevel(
                        remainingCaffeine >= 50
                                ? "HIGH"
                                : "LOW"
                )
                .analysisMessage(message)
                .intakeList(logs)
                .build();
    }

    // 민감도별 반감기 가중치
    private double getSensitivityWeight(
            CaffeineSensitivity sensitivity
    ) {

        return switch (sensitivity) {

            case HIGH -> 1.5;
            case LOW -> 0.7;
            default -> 1.0;
        };
    }

    // 위험도 계산
    private String calculateRiskLevel(
            int total,
            CaffeineSensitivity sensitivity
    ) {

        return switch (sensitivity) {

            case HIGH -> {
                if (total <= 150) {
                    yield "SAFE";
                } else if (total <= 250) {
                    yield "CAUTION";
                } else {
                    yield "DANGER";
                }
            }

            case NORMAL -> {
                if (total <= 300) {
                    yield "SAFE";
                } else if (total <= 400) {
                    yield "CAUTION";
                } else {
                    yield "DANGER";
                }
            }

            case LOW -> {
                if (total <= 400) {
                    yield "SAFE";
                } else if (total <= 500) {
                    yield "CAUTION";
                } else {
                    yield "DANGER";
                }
            }
        };
    }

    // 분석 메시지
    private String generateAnalysisMessage(
            List<ResultItem> logs,
            double remainingCaffeine,
            String sleepTime
    ) {

        if (logs.isEmpty()) {
            return "오늘은 카페인을 섭취하지 않았어요.";
        }

        if (remainingCaffeine >= 100) {

            return String.format(
                    "잔존량 %dmg은 취침 시간인 %s까지 해소되기 어렵습니다.",
                    (int) Math.round(remainingCaffeine),
                    sleepTime
            );

        } else {

            return String.format(
                    "현재 잔존 카페인이 %dmg이라 %s까지는 해소될 거예요.",
                    (int) Math.round(remainingCaffeine),
                    sleepTime
            );
        }
    }
}