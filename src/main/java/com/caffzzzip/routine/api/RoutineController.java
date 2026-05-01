package com.caffzzzip.routine.api;

import com.caffzzzip.common.error.SuccessCode;
import com.caffzzzip.common.template.ApiResTemplate;
import com.caffzzzip.routine.api.dto.RoutineRequest;
import com.caffzzzip.routine.application.RoutineService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequiredArgsConstructor
@Tag(name = "Routine", description = "사용자 루틴 설정 API")
@RequestMapping("/api/users/me/routine")
public class RoutineController {

    private final RoutineService routineService;

    @Operation(
            summary = "초기 루틴 설정",
            description = "최초 로그인 사용자의 수면 및 카페인 루틴 정보를 저장합니다."
    )
    @SecurityRequirement(name = "JWT")
    @PostMapping
    public ApiResTemplate<?> createRoutine(
            Authentication authentication,
            @RequestBody RoutineRequest request
    ) {
        Long userId = Long.valueOf(authentication.getName());

        routineService.createRoutine(userId, request);

        return ApiResTemplate.successWithNoContent(SuccessCode.SAVE_SUCCESS);
    }
}