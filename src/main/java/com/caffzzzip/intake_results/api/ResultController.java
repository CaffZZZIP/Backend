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
                오늘 하루의 카페인 상태를 요약해서 보여주는 섭취 결과 화면용 API입니다.

                메뉴 상세 조회 API와의 차이는 다음과 같습니다.

                - GET /api/menus/{menuId}
                  사용자가 특정 메뉴를 기록하기 전에,
                  해당 메뉴를 마셨을 경우의 예상 카페인 위험도와
                  취침 시간 기준 예상 잔존 카페인량을 미리 계산합니다.
                  즉, 섭취 전 미리보기 계산용 API입니다.

                - GET /api/report/today
                  사용자가 실제로 POST /api/intake를 통해 저장한 섭취 기록들을 기반으로
                  오늘의 실제 섭취 결과를 분석합니다.
                  즉, 섭취 후 결과 리포트 조회용 API입니다.

                조회 기준은 다음과 같습니다.

                1. 오늘 날짜 기준 섭취 기록 조회
                - 로그인한 사용자의 오늘 날짜 섭취 기록만 분석합니다.
                - 오늘 00:00부터 다음 날 00:00 전까지의 intakeAt을 기준으로 계산합니다.
                - 서버 시간 차이를 방지하기 위해 Asia/Seoul 기준 날짜와 시간을 사용합니다.

                2. totalCaffeine
                - 오늘 실제로 저장된 섭취 기록들의 총 카페인량입니다.
                - 사용자가 메뉴 상세 화면에서 확인만 하고 저장하지 않은 예상 섭취량은 포함하지 않습니다.

                3. remainingCaffeine
                - 현재 시점 기준 몸에 남아있는 예상 잔존 카페인량입니다.
                - 오늘 저장된 모든 섭취 기록에 대해
                  섭취 시간부터 현재 시간까지의 경과 시간을 계산하여 산출합니다.
                - 화면의 "잔존 카페인" 값으로 사용합니다.

                4. bedtimeRemainingCaffeine
                - 사용자가 설정한 취침 시간 기준 예상 잔존 카페인량입니다.
                - 오늘 저장된 모든 섭취 기록에 대해
                  섭취 시간부터 취침 시간까지의 경과 시간을 계산하여 산출합니다.
                - 이 값은 수면 방해 가능성 판단 기준으로 사용합니다.

                5. riskLevel
                - 오늘 총 카페인 섭취량과 사용자의 카페인 민감도 기준으로 계산한 위험도입니다.
                - SAFE, CAUTION, DANGER 중 하나로 반환됩니다.
                - 하루 권장량 대비 위험도 표시 영역에서 사용할 수 있습니다.

                6. recommendedSleepTime
                - 사용자가 루틴 설정에서 입력한 오늘 기준 취침 시간입니다.
                - 평일이면 weekdaySleepTime, 주말이면 weekendSleepTime을 사용합니다.
                - 현재는 별도 추천 계산 시간이 아니라 사용자의 목표 취침 시간 기준입니다.

                7. sleepImpactLevel
                - bedtimeRemainingCaffeine을 기준으로 계산한 수면 방해 가능성입니다.
                - LOW, MID, HIGH 중 하나로 반환됩니다.
                - 예시 기준:
                  LOW: 취침 시간 잔존 카페인량이 낮아 수면 영향 가능성이 낮음
                  MID: 취침 시간 잔존 카페인량이 어느 정도 남아 주의 필요
                  HIGH: 취침 시간 잔존 카페인량이 높아 수면 영향 가능성이 높음

                8. analysisMessage
                - 현재 잔존 카페인량과 취침 시간 기준 잔존 카페인량을 바탕으로 생성되는 안내 문구입니다.
                - 예:
                  "현재 잔존 카페인은 약 142mg이고, 취침 시간인 23:30에는 약 63mg 정도 남아있을 수 있어요. 늦은 시간 추가 섭취는 주의해주세요."

                9. intakeList
                - 오늘 저장된 섭취 기록 목록입니다.
                - 각 항목에는 menuId, menuName, brand, caffeineMg, intakeAt, quantity가 포함됩니다.
                - caffeineMg는 해당 기록의 총 카페인량입니다.
                  예: 메뉴 1개 카페인 150mg × 수량 2개 = 300mg

                반감기 기준은 사용자 카페인 민감도에 따라 다르게 적용합니다.

                - LOW: 약 4시간
                - NORMAL: 약 5시간
                - HIGH: 약 6시간

                프론트 사용 흐름은 다음과 같습니다.

                1. 사용자가 POST /api/intake로 섭취 기록을 저장합니다.
                2. 섭취 결과 화면 또는 오늘의 리포트 화면에서 이 API를 호출합니다.
                3. 응답값을 바탕으로 오늘 총 카페인량, 현재 잔존 카페인량,
                   취침 시간 기준 잔존 카페인량, 위험도, 수면 영향도, 안내 문구를 표시합니다.

                화면 적용 방식:
                - 하루 권장량 대비 위험도 바: totalCaffeine 기준
                - 잔존 카페인 카드: remainingCaffeine 표시
                - 취침 시간까지 남을 카페인 판단: bedtimeRemainingCaffeine 사용
                - 수면 방해 가능성: sleepImpactLevel 표시
                - 안내 문구: analysisMessage 표시
                - 오늘 섭취 목록: intakeList 표시

                정리:
                - remainingCaffeine은 현재 시점 기준 잔존 카페인량입니다.
                - bedtimeRemainingCaffeine은 사용자의 취침 시간 기준 예상 잔존 카페인량입니다.
                - sleepImpactLevel은 bedtimeRemainingCaffeine을 기준으로 판단합니다.
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