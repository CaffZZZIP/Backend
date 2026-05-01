package com.caffzzzip.routine.api.dto;

import com.caffzzzip.routine.domain.CaffeineSensitivity;
import com.caffzzzip.routine.domain.IntakeFrequency;

import java.time.LocalTime;

public record RoutineRequest(
        LocalTime weekdayWakeTime,
        LocalTime weekdaySleepTime,
        LocalTime weekendWakeTime,
        LocalTime weekendSleepTime,
        CaffeineSensitivity caffeineSensitivity,
        IntakeFrequency intakeFrequency
) {
}