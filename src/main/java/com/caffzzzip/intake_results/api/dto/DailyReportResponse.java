package com.caffzzzip.intake_results.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DailyReportResponse {

    // 총 카페인 섭취량
    private int totalCaffeine;

    // 현재 잔존 카페인량
    private int remainingCaffeine;

    // 위험도
    private String riskLevel;

    // 권장 취침 가능 시간
    private String recommendedSleepTime;

    // 수면 방해 예상도
    private String sleepImpactLevel;

    // 분석 메시지
    private String analysisMessage;

    // 상세 섭취 기록 리스트
    private List<ResultItem> intakeList;
}