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
import com.caffzzzip.routine.domain.UserDailyRoutineMode;
import com.caffzzzip.routine.domain.repository.UserDailyRoutineModeRepository;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResultService {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final IntakeLogRepository intakeLogRepository;
    private final RoutineRepository routineRepository;
    private final UserDailyRoutineModeRepository userDailyRoutineModeRepository;

    public DailyReportResponse getTodayReport(Long userId) {

        Routine routine = routineRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("루틴 정보가 없습니다.")
                );

        LocalDate today = LocalDate.now(KOREA_ZONE_ID);
        LocalDateTime now = LocalDateTime.now(KOREA_ZONE_ID);

        RoutineType todayType =
                getSelectedRoutineType(
                        userId,
                        today
                );

        LocalTime sleepLocalTime =
                todayType == RoutineType.WEEKEND
                        ? routine.getWeekendSleepTime()
                        : routine.getWeekdaySleepTime();

        String sleepTime = sleepLocalTime.toString();

        LocalDateTime sleepDateTime = LocalDateTime.of(today, sleepLocalTime);

        if (sleepDateTime.isBefore(now) || sleepDateTime.isEqual(now)) {
            sleepDateTime = sleepDateTime.plusDays(1);
        }

        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        List<IntakeLog> intakeLogs =
                intakeLogRepository.findByUserIdAndIntakeAtBetween(
                        userId,
                        start,
                        end
                );

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

        double halfLifeHours = getHalfLifeHours(
                routine.getCaffeineSensitivity()
        );

        int totalIntake = 0;
        double currentRemainingCaffeine = 0;
        double bedtimeRemainingCaffeine = 0;

        for (ResultItem log : logs) {
            totalIntake += log.getCaffeineMg();

            LocalDateTime intakeTime =
                    LocalDateTime.parse(log.getIntakeAt());

            currentRemainingCaffeine += calculateRemainingCaffeine(
                    log.getCaffeineMg(),
                    intakeTime,
                    now,
                    halfLifeHours
            );

            bedtimeRemainingCaffeine += calculateRemainingCaffeine(
                    log.getCaffeineMg(),
                    intakeTime,
                    sleepDateTime,
                    halfLifeHours
            );
        }

        int roundedCurrentRemaining =
                (int) Math.round(currentRemainingCaffeine);

        int roundedBedtimeRemaining =
                (int) Math.round(bedtimeRemainingCaffeine);

        String riskLevel =
                calculateRiskLevel(
                        totalIntake,
                        routine.getCaffeineSensitivity()
                );

        String sleepImpactLevel =
                calculateSleepImpactLevel(roundedBedtimeRemaining);

        String message =
                generateAnalysisMessage(
                        logs,
                        roundedCurrentRemaining,
                        roundedBedtimeRemaining,
                        sleepTime,
                        sleepImpactLevel
                );

        return DailyReportResponse.builder()
                .totalCaffeine(totalIntake)
                .remainingCaffeine(roundedCurrentRemaining)
                .bedtimeRemainingCaffeine(roundedBedtimeRemaining)
                .riskLevel(riskLevel)
                .recommendedSleepTime(sleepTime)
                .sleepImpactLevel(sleepImpactLevel)
                .analysisMessage(message)
                .intakeList(logs)
                .build();
    }

    private double calculateRemainingCaffeine(
            int caffeineMg,
            LocalDateTime intakeTime,
            LocalDateTime targetTime,
            double halfLifeHours
    ) {
        long minutesElapsed =
                Duration.between(intakeTime, targetTime).toMinutes();

        if (minutesElapsed <= 0) {
            return caffeineMg;
        }

        double hoursElapsed = minutesElapsed / 60.0;

        return caffeineMg * Math.pow(
                0.5,
                hoursElapsed / halfLifeHours
        );
    }

    private double getHalfLifeHours(
            CaffeineSensitivity sensitivity
    ) {
        return switch (sensitivity) {
            case LOW -> 4.0;
            case NORMAL -> 5.0;
            case HIGH -> 6.0;
        };
    }

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

    private String calculateSleepImpactLevel(
            int bedtimeRemainingCaffeine
    ) {
        if (bedtimeRemainingCaffeine >= 100) {
            return "HIGH";
        }

        if (bedtimeRemainingCaffeine >= 50) {
            return "MID";
        }

        return "LOW";
    }

    private String generateAnalysisMessage(
            List<ResultItem> logs,
            int currentRemaining,
            int bedtimeRemaining,
            String sleepTime,
            String sleepImpactLevel
    ) {
        if (logs.isEmpty()) {
            return "오늘은 카페인을 섭취하지 않았어요.";
        }

        if ("HIGH".equals(sleepImpactLevel)) {
            return String.format(
                    "현재 잔존 카페인은 약 %dmg이고, 취침 시간인 %s에도 약 %dmg 정도 남아있을 수 있어요. 수면에 영향을 줄 가능성이 높아요.",
                    currentRemaining,
                    sleepTime,
                    bedtimeRemaining
            );
        }

        if ("MID".equals(sleepImpactLevel)) {
            return String.format(
                    "현재 잔존 카페인은 약 %dmg이고, 취침 시간인 %s에는 약 %dmg 정도 남아있을 수 있어요. 늦은 시간 추가 섭취는 주의해주세요.",
                    currentRemaining,
                    sleepTime,
                    bedtimeRemaining
            );
        }

        return String.format(
                "현재 잔존 카페인은 약 %dmg이고, 취침 시간인 %s에는 대부분 해소될 것으로 예상돼요.",
                currentRemaining,
                sleepTime
        );
    }
    private RoutineType getSelectedRoutineType(
            Long userId,
            LocalDate date
    ) {
        return userDailyRoutineModeRepository
                .findByUserIdAndTargetDate(userId, date)
                .map(UserDailyRoutineMode::getRoutineType)
                .orElseGet(() -> {

                    DayOfWeek dayOfWeek = date.getDayOfWeek();

                    if (dayOfWeek == DayOfWeek.SATURDAY
                            || dayOfWeek == DayOfWeek.SUNDAY) {
                        return RoutineType.WEEKEND;
                    }

                    return RoutineType.WEEKDAY;
                });
    }
}