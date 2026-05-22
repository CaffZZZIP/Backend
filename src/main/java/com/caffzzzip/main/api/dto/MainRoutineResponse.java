package com.caffzzzip.main.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MainRoutineResponse {

    // 현재 루틴명
    private String routineName;

    // 기상 시간
    private String wakeTime;

    // 취침 시간
    private String sleepTime;
}