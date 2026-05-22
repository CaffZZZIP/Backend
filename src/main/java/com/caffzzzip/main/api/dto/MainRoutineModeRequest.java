package com.caffzzzip.main.api.dto;

import com.caffzzzip.routine.domain.RoutineType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "오늘 루틴 모드 선택 요청")
public record MainRoutineModeRequest(

        @Schema(description = "오늘 적용할 루틴 타입", example = "WEEKDAY")
        RoutineType routineType
) {
}
