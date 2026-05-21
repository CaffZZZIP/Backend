package com.caffzzzip.intake_results.api;

import com.caffzzzip.common.error.SuccessCode;
import com.caffzzzip.common.template.ApiResTemplate;
import com.caffzzzip.intake_results.api.dto.DailyReportResponse;
import com.caffzzzip.intake_results.application.ResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/report")
@Tag(name = "Intake Result", description = "카페인 분석 결과 API")
public class ResultController {

    private final ResultService resultService;

    @Operation(
            summary = "오늘의 카페인 분석 리포트 조회",
            description = """
                    현재 로그인한 사용자의 오늘 카페인 섭취 결과를 분석하여 반환합니다.
                    
                    이 API는 사용자가 실제로 저장한 섭취 기록을 기반으로
                    오늘 하루의 카페인 상태를 요약해서 보여주는 결과 화면용 API입니다.
                    
                    메뉴 상세 조회 API와의 차이는 다음과 같습니다.
                    
                    - GET /api/menus/{menuId}
                      사용자가 특정 메뉴를 기록하기 전에,
                      해당 메뉴를 마셨을 경우의 예상 카페인 위험도와 잔존 카페인량을 미리 계산합니다.
                      즉, 섭취 전 미리보기 계산용 API입니다.
                    
                    - GET /api/report/today
                      사용자가 실제로 POST /api/intake를 통해 저장한 섭취 기록들을 기반으로
                      오늘의 최종 섭취 결과를 분석합니다.
                      즉, 섭취 후 결과 리포트 조회용 API입니다.
                    
                    조회 기준은 다음과 같습니다.
                    
                    1. 오늘 날짜 기준 섭취 기록 조회
                    - 로그인한 사용자의 오늘 날짜 섭취 기록만 분석합니다.
                    - 오늘 00:00부터 다음 날 00:00 전까지의 intakeAt을 기준으로 계산합니다.
                    
                    2. totalCaffeine
                    - 오늘 실제로 저장된 섭취 기록들의 총 카페인량입니다.
                    - 사용자가 저장하지 않은 메뉴 상세 화면의 예상 섭취량은 포함하지 않습니다.
                    
                    3. remainingCaffeine
                    - 사용자의 취침 시간 기준 예상 잔존 카페인량입니다.
                    - 오늘 저장된 모든 섭취 기록에 대해 카페인 반감기 약 5시간을 적용하여 계산합니다.
                    
                    4. riskLevel
                    - 오늘 총 카페인 섭취량과 사용자의 카페인 민감도 기준으로 계산한 위험도입니다.
                    - SAFE, CAUTION, DANGER 중 하나로 반환됩니다.
                    
                    5. sleepImpactLevel
                    - 취침 시 예상 잔존 카페인량을 기반으로 수면 방해 가능성을 나타냅니다.
                    - LOW, MID, HIGH 등의 값으로 반환될 수 있습니다.
                    
                    6. recommendedSleepTime
                    - 현재 카페인 섭취 상태를 고려했을 때 권장되는 취침 가능 시간입니다.
                    - 카페인 잔존량이 높은 경우 기존 취침 시간보다 늦게 제안될 수 있습니다.
                    
                    프론트 사용 흐름은 다음과 같습니다.
                    
                    1. 사용자가 POST /api/intake로 섭취 기록을 저장합니다.
                    2. 섭취 결과 화면 또는 오늘의 리포트 화면에서 이 API를 호출합니다.
                    3. 응답값을 바탕으로 오늘 총 카페인량, 잔존 카페인량, 위험도, 수면 영향도, 안내 문구를 표시합니다.
                    
                    정리하면 이 API는 섭취 전 예상 계산이 아니라,
                    오늘 실제로 기록된 섭취 데이터를 기반으로 한 결과 리포트 조회용 API입니다.
                    """
    )
    @SecurityRequirement(name = "JWT")
    @GetMapping("/today")
    public ApiResTemplate<DailyReportResponse> getTodayReport(
            Authentication authentication
    ) {

        Long userId = Long.valueOf(authentication.getName());

        DailyReportResponse response =
                resultService.getTodayReport(userId);

        return ApiResTemplate.successResponse(
                SuccessCode.GET_SUCCESS,
                response
        );
    }
}