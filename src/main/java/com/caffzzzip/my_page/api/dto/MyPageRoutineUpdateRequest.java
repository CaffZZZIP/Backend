package com.caffzzzip.my_page.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MyPageRoutineUpdateRequest {
    private String weekdayRoutineName;
    private String weekendRoutineName;
    private String weekdayWakeTime;
    private String weekdaySleepTime;
    private String weekendWakeTime;
    private String weekendSleepTime;
    private String caffeineSensitivity;
    private String intakeFrequency;
}