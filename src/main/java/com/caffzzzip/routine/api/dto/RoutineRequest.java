package com.caffzzzip.routine.api.dto;

import com.caffzzzip.routine.domain.CaffeineSensitivity;
import com.caffzzzip.routine.domain.IntakeFrequency;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

import java.util.List;

@Schema(description = "사용자 초기 루틴 설정 요청")
public record RoutineRequest(

        @Schema(description = "평일 루틴 이름", example = "수업")
        String weekdayRoutineName,

        @Schema(description = "쉬는날로 적용할 요일 목록", example = "[\"FRIDAY\", \"SATURDAY\", \"SUNDAY\"]")
        List<String> restDays,

        @Schema(description = "주말 루틴 이름", example = "공강")
        String weekendRoutineName,

        @Schema(description = "평일 기상 시간", example = "08:00")
        LocalTime weekdayWakeTime,

        @Schema(description = "평일 취침 시간", example = "01:00")
        LocalTime weekdaySleepTime,

        @Schema(description = "주말 기상 시간", example = "10:00")
        LocalTime weekendWakeTime,

        @Schema(description = "주말 취침 시간", example = "02:00")
        LocalTime weekendSleepTime,

        @Schema(description = "카페인 민감도", example = "NORMAL")
        CaffeineSensitivity caffeineSensitivity,

        @Schema(description = "카페인 섭취 빈도", example = "OFTEN")
        IntakeFrequency intakeFrequency
) {
}