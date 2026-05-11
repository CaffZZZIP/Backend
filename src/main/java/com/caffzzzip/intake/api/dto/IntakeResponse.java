package com.caffzzzip.intake.api.dto;

import com.caffzzzip.routine.domain.RoutineType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "카페인 섭취 기록 응답")
public record IntakeResponse(

        @Schema(description = "섭취 기록 ID", example = "1")
        Long intakeId,

        @Schema(description = "메뉴 ID", example = "1")
        Long menuId,

        @Schema(description = "메뉴명", example = "아메리카노")
        String menuName,

        @Schema(description = "브랜드명", example = "스타벅스")
        String brand,

        @Schema(description = "평일/주말 루틴 타입", example = "WEEKDAY")
        RoutineType routineType,

        @Schema(description = "섭취 시간", example = "2026-05-01T14:30:00")
        LocalDateTime intakeAt,

        @Schema(description = "섭취 수량", example = "1")
        Integer quantity,

        @Schema(description = "총 카페인량", example = "150")
        Integer totalCaffeine
) {
}