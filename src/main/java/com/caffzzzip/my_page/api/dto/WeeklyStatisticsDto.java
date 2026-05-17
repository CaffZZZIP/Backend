package com.caffzzzip.my_page.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeeklyStatisticsDto {

    // 요일
    private String dayOfWeek;

    // 총 카페인 섭취량
    private int totalCaffeine;

    // 위험도
    private String riskLevel;
}