package com.caffzzzip.intake.api;

import com.caffzzzip.common.error.SuccessCode;
import com.caffzzzip.common.template.ApiResTemplate;
import com.caffzzzip.intake.api.dto.IntakeCreateRequest;
import com.caffzzzip.intake.api.dto.IntakeResponse;
import com.caffzzzip.intake.application.IntakeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
}