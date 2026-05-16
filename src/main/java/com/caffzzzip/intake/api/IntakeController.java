package com.caffzzzip.intake.api;

import com.caffzzzip.common.error.SuccessCode;
import com.caffzzzip.common.template.ApiResTemplate;
import com.caffzzzip.intake.api.dto.IntakeCreateRequest;
import com.caffzzzip.intake.api.dto.IntakeResponse;
import com.caffzzzip.intake.api.dto.IntakeUpdateRequest;
import com.caffzzzip.intake.application.IntakeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/intake")
@Tag(name = "Intake", description = "카페인 섭취 기록 API")
public class IntakeController {

    private final IntakeService intakeService;

    @Operation(
            summary = "카페인 섭취 기록 저장",
            description = "사용자가 섭취한 메뉴와 수량, 시간을 기반으로 카페인 섭취 기록을 저장합니다."
    )
    @SecurityRequirement(name = "JWT")
    @PostMapping
    public ApiResTemplate<IntakeResponse> createIntake(
            Authentication authentication,
            @RequestBody IntakeCreateRequest request
    ) {
        Long userId = Long.valueOf(authentication.getName());

        IntakeResponse response = intakeService.createIntake(userId, request);

        return ApiResTemplate.successResponse(SuccessCode.SAVE_SUCCESS, response);
    }

    @Operation(summary = "오늘 카페인 섭취 기록 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/today")
    public ApiResTemplate<List<IntakeResponse>> getTodayIntakes(
            Authentication authentication
    ) {
        Long userId = Long.valueOf(authentication.getName());

        List<IntakeResponse> response = intakeService.getTodayIntakes(userId);

        return ApiResTemplate.successResponse(SuccessCode.GET_SUCCESS, response);
    }

    @Operation(summary = "카페인 섭취 기록 수정")
    @SecurityRequirement(name = "JWT")
    @PatchMapping("/{intakeId}")
    public ApiResTemplate<IntakeResponse> updateIntake(
            Authentication authentication,
            @PathVariable Long intakeId,
            @RequestBody IntakeUpdateRequest request
    ) {
        Long userId = Long.valueOf(authentication.getName());

        IntakeResponse response = intakeService.updateIntake(userId, intakeId, request);

        return ApiResTemplate.successResponse(SuccessCode.UPDATE_SUCCESS, response);
    }

    @Operation(summary = "카페인 섭취 기록 삭제")
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{intakeId}")
    public ApiResTemplate<?> deleteIntake(
            Authentication authentication,
            @PathVariable Long intakeId
    ) {
        Long userId = Long.valueOf(authentication.getName());

        intakeService.deleteIntake(userId, intakeId);

        return ApiResTemplate.successWithNoContent(SuccessCode.INTAKE_DELETE_SUCCESS);
    }
}