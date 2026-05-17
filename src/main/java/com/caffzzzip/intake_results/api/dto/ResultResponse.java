package com.caffzzzip.intake_results.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResultResponse {

    // 분석 리포트 데이터
    private DailyReportResponse reportData;
}