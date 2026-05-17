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
            description = "현재 로그인한 사용자의 오늘 자 카페인 분석 데이터를 반환합니다."
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