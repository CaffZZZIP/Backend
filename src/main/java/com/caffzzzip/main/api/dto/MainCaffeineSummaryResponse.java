package com.caffzzzip.main.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MainCaffeineSummaryResponse {

    // 오늘 총 카페인
    private int totalCaffeine;

    // 현재 잔존 카페인
    private int remainingCaffeine;

    // 남은 여유 섭취량
    private int remainingSafeAmount;

    // 위험도
    private String riskLevel;

    // 수면 영향도
    private String sleepImpactLevel;
}