package com.caffzzzip.intake.api.dto;

import com.caffzzzip.routine.domain.RoutineType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "카페인 섭취 기록 저장 요청")
public record IntakeCreateRequest(

        @Schema(description = "섭취한 메뉴 ID", example = "1")
        Long menuId,

        @Schema(description = "평일/주말 루틴 타입", example = "WEEKDAY")
        RoutineType routineType,

        @Schema(description = "섭취 시간", example = "2026-05-01T14:30:00")
        LocalDateTime intakeAt,

        @Schema(description = "섭취 수량", example = "1")
        Integer quantity
) {
}