package com.caffzzzip.main.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MainDailyQuoteResponse {

    // 오늘 한줄 요약
    private String message;
}