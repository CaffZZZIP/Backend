package com.caffzzzip.intake_results.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DailyReportResponse {

    private int totalCaffeine;

    // 현재 시점 기준 잔존 카페인량
    private int remainingCaffeine;

    // 취침 시간 기준 예상 잔존 카페인량
    private int bedtimeRemainingCaffeine;

    private String riskLevel;

    private String recommendedSleepTime;

    private String sleepImpactLevel;

    private String analysisMessage;

    private List<ResultItem> intakeList;
}