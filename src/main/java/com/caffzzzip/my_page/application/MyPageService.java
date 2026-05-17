package com.caffzzzip.my_page.application;

import com.caffzzzip.intake.domain.IntakeLog;
import com.caffzzzip.intake.domain.repository.IntakeLogRepository;
import com.caffzzzip.my_page.api.dto.MyPageResponse;
import com.caffzzzip.my_page.api.dto.MyPageRoutineResponse;
import com.caffzzzip.my_page.api.dto.WeeklyStatisticsDto;
import com.caffzzzip.routine.api.dto.RoutineRequest;
import com.caffzzzip.routine.domain.CaffeineSensitivity;
import com.caffzzzip.routine.domain.Routine;
import com.caffzzzip.routine.domain.repository.RoutineRepository;
import com.caffzzzip.user.domain.User;
import com.caffzzzip.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final IntakeLogRepository intakeLogRepository;
    private final UserRepository userRepository;
    private final RoutineRepository routineRepository;

    // 마이페이지 메인 & 주간 통계 조회 로직
    @Transactional(readOnly = true)
    public MyPageResponse getMyPageInfo(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("해당 유저가 없습니다.")
                );

        Routine routine =
                routineRepository.findByUserId(userId)
                        .orElse(null);

        return MyPageResponse.builder()
                .nickname(user.getNickname())

                .sensitivity(
                        routine != null
                                ? routine.getCaffeineSensitivity().name()
                                : null
                )

                // 평일 루틴명 기준 출력
                .routineType(
                        routine != null
                                ? routine.getWeekdayRoutineName()
                                : "루틴 미설정"
                )

                .weeklyStatistics(
                        calculateWeeklyStats(
                                userId,
                                routine
                        )
                )

                .build();
    }

    // 사용자 루틴 조회
    @Transactional(readOnly = true)
    public MyPageRoutineResponse getMyRoutineInfo(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("해당 유저가 없습니다.")
                );

        Routine routine = routineRepository.findByUserId(userId)
                .orElse(null);

        return MyPageRoutineResponse.builder()
                .nickname(user.getNickname())
                .weekdayRoutineName(routine != null ? routine.getWeekdayRoutineName() : "루틴 미설정")
                .weekendRoutineName(routine != null ? routine.getWeekendRoutineName() : "루틴 미설정")
                .sensitivity(
                        routine != null && routine.getCaffeineSensitivity() != null
                                ? routine.getCaffeineSensitivity().name()
                                : "NORMAL"
                )
                .weekdayWakeTime(
                        routine != null && routine.getWeekdayWakeTime() != null
                                ? routine.getWeekdayWakeTime().toString()
                                : "00:00"
                )
                .weekdaySleepTime(
                        routine != null && routine.getWeekdaySleepTime() != null
                                ? routine.getWeekdaySleepTime().toString()
                                : "00:00"
                )
                .weekendWakeTime(
                        routine != null && routine.getWeekendWakeTime() != null
                                ? routine.getWeekendWakeTime().toString()
                                : "00:00"
                )
                .weekendSleepTime(
                        routine != null && routine.getWeekendSleepTime() != null
                                ? routine.getWeekendSleepTime().toString()
                                : "00:00"
                )
                .build();
    }


    @Transactional
    public void updateMyRoutine(Long userId, RoutineRequest request) {

        Routine routine = routineRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("설정된 루틴이 없습니다."));


        String weekdayName = request.weekdayRoutineName() == null || request.weekdayRoutineName().isBlank() ? "평소" : request.weekdayRoutineName();
        String weekendName = request.weekendRoutineName() == null || request.weekendRoutineName().isBlank() ? "쉬는 날" : request.weekendRoutineName();


        try {
            setField(routine, "weekdayRoutineName", weekdayName);
            setField(routine, "weekendRoutineName", weekendName);
            setField(routine, "weekdayWakeTime", request.weekdayWakeTime());
            setField(routine, "weekdaySleepTime", request.weekdaySleepTime());
            setField(routine, "weekendWakeTime", request.weekendWakeTime());
            setField(routine, "weekendSleepTime", request.weekendSleepTime());
            setField(routine, "caffeineSensitivity", request.caffeineSensitivity());
            setField(routine, "intakeFrequency", request.intakeFrequency());
        } catch (Exception e) {
            throw new RuntimeException("루틴 수정 반영 중 오류가 발생했습니다.", e);
        }


    }


    private void setField(Object target, String fieldName, Object value) throws Exception {
        if (value != null) {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        }
    }

    // 주간 통계 계산 내부 메서드
    private List<WeeklyStatisticsDto> calculateWeeklyStats(Long userId, Routine routine) {
        List<WeeklyStatisticsDto> list = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate targetDate = today.minusDays(i);
            LocalDateTime start = targetDate.atStartOfDay();
            LocalDateTime end = targetDate.plusDays(1).atStartOfDay();

            List<IntakeLog> logs = intakeLogRepository.findByUserIdAndIntakeAtBetween(userId, start, end);
            int totalCaffeine = logs.stream().mapToInt(IntakeLog::getTotalCaffeine).sum();

            String riskLevel = calculateRiskLevel(totalCaffeine, routine != null ? routine.getCaffeineSensitivity() : CaffeineSensitivity.NORMAL);

            list.add(WeeklyStatisticsDto.builder()
                    .dayOfWeek(getDayOfWeekKorean(targetDate.getDayOfWeek()))
                    .totalCaffeine(totalCaffeine)
                    .riskLevel(riskLevel)
                    .build());
        }
        return list;
    }

    // 위험도 계산 내부 메서드
    private String calculateRiskLevel(int total, CaffeineSensitivity sensitivity) {
        return switch (sensitivity) {
            case HIGH -> {
                if (total <= 150) yield "SAFE";
                else if (total <= 250) yield "CAUTION";
                else yield "DANGER";
            }
            case NORMAL -> {
                if (total <= 300) yield "SAFE";
                else if (total <= 400) yield "CAUTION";
                else yield "DANGER";
            }
            case LOW -> {
                if (total <= 400) yield "SAFE";
                else if (total <= 500) yield "CAUTION";
                else yield "DANGER";
            }
        };
    }

    // 요일 한글 변환 내부 메서드
    private String getDayOfWeekKorean(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
    }

    // 로그아웃
    public void logout() {
        System.out.println("로그아웃 처리가 요청되었습니다.");
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다."));

        routineRepository.findByUserId(userId)
                .ifPresent(routine -> routineRepository.delete(routine));

        List<IntakeLog> logs = intakeLogRepository.findByUserIdAndIntakeAtBetween(userId, LocalDateTime.MIN, LocalDateTime.MAX);
        if (!logs.isEmpty()) {
            intakeLogRepository.deleteAll(logs);
        }

        userRepository.deleteById(userId);
    }
}