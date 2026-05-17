package com.caffzzzip.my_page.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPageRoutineResponse {
    private String nickname;
    private String weekdayRoutineName;
    private String weekendRoutineName;
    private String sensitivity;
    private String weekdayWakeTime;
    private String weekdaySleepTime;
    private String weekendWakeTime;
    private String weekendSleepTime;
}