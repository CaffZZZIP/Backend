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

    @Operation(
            summary = "오늘 루틴 정보 조회",
            description = """
                오늘 적용 중인 루틴 정보를 조회합니다.

                루틴 적용 우선순위는 다음과 같습니다.

                1. 사용자가 메인 화면에서 직접 선택한 오늘 루틴 모드
                2. 선택된 값이 없으면 현재 날짜 기준 자동 판단

                예시)

                - 금요일 + 사용자가 '쉬는날' 선택
                  → WEEKEND 루틴 사용

                - 토요일 + 별도 선택 없음
                  → WEEKEND 루틴 자동 사용

                - 화요일 + 별도 선택 없음
                  → WEEKDAY 루틴 자동 사용

                반환되는 루틴명, 기상시간, 취침시간은
                최종 적용된 루틴 기준으로 계산됩니다.

                이 값은 메뉴 상세 계산, 섭취 기록 저장,
                섭취 결과 분석에서도 동일하게 사용됩니다.
                """
    )
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


    @Operation(
            summary = "오늘 루틴 모드 선택 (평일 / 쉬는날)",
            description = """
                사용자가 오늘 적용할 루틴 모드를 선택합니다.

                선택된 값은 오늘 날짜 기준으로 저장되며,
                이후 다음 기능들에서 공통으로 사용됩니다.

                - 메인 루틴 조회
                - 메뉴 상세 예상 카페인 계산
                - 섭취 기록 저장
                - 섭취 결과 분석

                예시)

                오늘이 금요일이어도

                {
                  "routineType": "WEEKEND"
                }

                를 선택하면
                오늘 하루는 쉬는날 루틴 기준으로 동작합니다.

                저장된 값이 없을 경우에는
                실제 요일 기준으로 평일/주말을 자동 판단합니다.
                """
    )
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