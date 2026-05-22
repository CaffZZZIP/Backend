package com.caffzzzip.main.api;

import com.caffzzzip.common.error.SuccessCode;
import com.caffzzzip.common.template.ApiResTemplate;
import com.caffzzzip.main.api.dto.*;
import com.caffzzzip.main.application.MainService;
import com.caffzzzip.routine.domain.RoutineType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/main")
@Tag(name = "Main", description = "메인 홈 화면 API")
public class MainController {

    private final MainService mainService;

    @Operation(summary = "오늘 루틴 정보 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/routine")
    public ApiResTemplate<MainRoutineResponse> getRoutine(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return ApiResTemplate.successResponse(SuccessCode.GET_SUCCESS, mainService.getRoutineInfo(userId));
    }

    @Operation(summary = "카페인 요약 정보 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/caffeine-summary")
    public ApiResTemplate<MainCaffeineSummaryResponse> getCaffeineSummary(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return ApiResTemplate.successResponse(SuccessCode.GET_SUCCESS, mainService.getCaffeineSummary(userId));
    }

    @Operation(summary = "오늘의 한줄 요약")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/daily-quote")
    public ApiResTemplate<MainDailyQuoteResponse> getDailyQuote(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return ApiResTemplate.successResponse(SuccessCode.GET_SUCCESS, mainService.getDailyQuote(userId));
    }

    @Operation(summary = "섭취 목록 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/intake-preview")
    public ApiResTemplate<List<MainIntakeItem>> getIntakePreview(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return ApiResTemplate.successResponse(SuccessCode.GET_SUCCESS, mainService.getIntakePreview(userId));
    }


    @Operation(summary = "오늘 루틴 모드 선택 (평일 / 쉬는날)",
            description = "사용자가 오늘 평일 모드인지 쉬는날 모드인지 선택합니다.")
    @SecurityRequirement(name = "JWT")
    @PostMapping("/routine-mode")
    public ApiResTemplate<?> setTodayRoutineMode(
            Authentication authentication,
            @RequestBody MainRoutineModeRequest request
    ) {
        Long userId = Long.valueOf(authentication.getName());
        mainService.setTodayRoutineMode(userId, request.routineType());

        return ApiResTemplate.successWithNoContent(SuccessCode.SAVE_SUCCESS);
    }
}