package com.caffzzzip.main.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MainIntakeItem {

    // 메뉴명
    private String menuName;

    // 카페인량
    private int caffeineMg;

    // 섭취 시간
    private String intakeTime;
}