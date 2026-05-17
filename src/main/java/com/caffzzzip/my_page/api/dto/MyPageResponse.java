package com.caffzzzip.my_page.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyPageResponse {

    // 사용자 닉네임
    private String nickname;

    // 카페인 민감도
    private String sensitivity;

    // 현재 루틴 이름
    private String routineType;

    // 주간 통계
    private List<WeeklyStatisticsDto> weeklyStatistics;
}