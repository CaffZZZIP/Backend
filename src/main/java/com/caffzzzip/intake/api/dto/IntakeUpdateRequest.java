package com.caffzzzip.intake.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "카페인 섭취 기록 수정 요청")
public record IntakeUpdateRequest(

        @Schema(description = "섭취 시간", example = "2026-05-01T15:00:00")
        LocalDateTime intakeAt,

        @Schema(description = "섭취 수량", example = "2")
        Integer quantity
) {
}