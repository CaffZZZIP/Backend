package com.caffzzzip.main.application;

import com.caffzzzip.common.error.ErrorCode;
import com.caffzzzip.common.exception.BusinessException;
import com.caffzzzip.intake.domain.IntakeLog;
import com.caffzzzip.intake.domain.repository.IntakeLogRepository;
import com.caffzzzip.intake_results.api.dto.DailyReportResponse;
import com.caffzzzip.intake_results.application.ResultService;
import com.caffzzzip.main.api.dto.*;
import com.caffzzzip.routine.domain.Routine;
import com.caffzzzip.routine.domain.RoutineType;
import com.caffzzzip.routine.domain.repository.RoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainService {

    private final RoutineRepository routineRepository;
    private final IntakeLogRepository intakeLogRepository;
    private final ResultService resultService;

    // 한국 시간
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // 하루 권장 최대 카페인량
    private static final int DAILY_SAFE_LIMIT = 400;

    // 오늘 사용자가 선택한 루틴 모드  저장

    private final Map<Long, RoutineType> todayRoutineModeCache = new ConcurrentHashMap<>();

    public MainRoutineResponse getRoutineInfo(Long userId) {
        Routine routine = findRoutine(userId);

        // 사용자가 오늘 선택한 모드가 있으면 우선 적용
        // 없으면 자동 판단
        RoutineType todayType =
                todayRoutineModeCache.getOrDefault(
                        userId,
                        getTodayRoutineType()
                );

        return buildRoutineResponse(routine, todayType);
    }

    @Transactional
    public void setTodayRoutineMode(Long userId, RoutineType routineType) {
        todayRoutineModeCache.put(userId, routineType);
    }

    public MainCaffeineSummaryResponse getCaffeineSummary(Long userId) {
        DailyReportResponse report = resultService.getTodayReport(userId);

        // 남은 여유 섭취량
        int remainingSafeAmount =
                Math.max(0, DAILY_SAFE_LIMIT - report.getTotalCaffeine());

        return MainCaffeineSummaryResponse.builder()
                .totalCaffeine(report.getTotalCaffeine())
                .remainingCaffeine(report.getRemainingCaffeine())
                .remainingSafeAmount(remainingSafeAmount)
                .riskLevel(report.getRiskLevel())
                .sleepImpactLevel(report.getSleepImpactLevel())
                .build();
    }

    public MainDailyQuoteResponse getDailyQuote(Long userId) {
        DailyReportResponse report = resultService.getTodayReport(userId);
        List<MainIntakeItem> intakePreview = getIntakePreview(userId);

        return MainDailyQuoteResponse.builder()
                .message(createSummaryMessage(
                        report.getRemainingCaffeine(),
                        intakePreview
                ))
                .build();
    }

    public List<MainIntakeItem> getIntakePreview(Long userId) {
        LocalDate today = getTodayKst();

        LocalDateTime start =
                today.atStartOfDay(KST).toLocalDateTime();

        LocalDateTime end =
                today.plusDays(1)
                        .atStartOfDay(KST)
                        .toLocalDateTime();

        return intakeLogRepository
                .findByUserIdAndIntakeAtBetween(userId, start, end)
                .stream()
                .sorted(Comparator.comparing(IntakeLog::getIntakeAt).reversed())
                .map(log -> MainIntakeItem.builder()
                        .menuName(log.getMenu().getMenuName())
                        .brand(log.getMenu().getBrand())
                        .caffeineMg(log.getTotalCaffeine())
                        .intakeTime(
                                formatTime(log.getIntakeAt().toLocalTime())
                        )
                        .build())
                .toList();
    }



    private MainRoutineResponse buildRoutineResponse(
            Routine routine,
            RoutineType type
    ) {

        String routineName = type == RoutineType.WEEKDAY
                ? routine.getWeekdayRoutineName()
                : routine.getWeekendRoutineName();

        LocalTime wakeTime = type == RoutineType.WEEKDAY
                ? routine.getWeekdayWakeTime()
                : routine.getWeekendWakeTime();

        LocalTime sleepTime = type == RoutineType.WEEKDAY
                ? routine.getWeekdaySleepTime()
                : routine.getWeekendSleepTime();

        return MainRoutineResponse.builder()
                .routineName(routineName)
                .wakeTime(formatTime(wakeTime))
                .sleepTime(formatTime(sleepTime))
                .build();
    }

    private Routine findRoutine(Long userId) {
        return routineRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.VALIDATION_ERROR,
                        "루틴 정보가 없습니다. 온보딩을 먼저 완료해주세요."
                ));
    }

    private LocalDate getTodayKst() {
        return ZonedDateTime.now(KST).toLocalDate();
    }


    private RoutineType getTodayRoutineType() {
        DayOfWeek day = getTodayKst().getDayOfWeek();

        return (day == DayOfWeek.SATURDAY
                || day == DayOfWeek.SUNDAY)
                ? RoutineType.WEEKEND
                : RoutineType.WEEKDAY;
    }

    private String createSummaryMessage(
            int remainingCaffeine,
            List<MainIntakeItem> intakeList
    ) {

        if (intakeList.isEmpty()) {
            return "오늘은 아직 카페인을 섭취하지 않았어요.";
        }

        boolean isLateIntake = intakeList.stream()
                .anyMatch(item -> {
                    String time = item.getIntakeTime();

                    return time.contains("오후 7")
                            || time.contains("오후 8")
                            || time.contains("오후 9")
                            || time.contains("오후 10")
                            || time.contains("오후 11");
                });

        if (isLateIntake) {
            return "늦은 시간 카페인 섭취로 수면에 영향을 줄 수 있어요.";
        }

        if (remainingCaffeine >= 150) {
            return "현재 체내 카페인이 높아 수면에 영향을 줄 가능성이 있어요.";
        }

        if (remainingCaffeine >= 70) {
            return "현재 잔존 카페인이 남아있어요. 취침 전에 주의하세요.";
        }

        return "오늘은 비교적 안정적인 카페인 섭취 상태예요.";
    }


    private String formatTime(LocalTime time) {
        if (time == null) {
            return "00:00";
        }

        return time.format(
                DateTimeFormatter.ofPattern(
                        "a hh:mm",
                        Locale.KOREAN
                )
        );
    }
}