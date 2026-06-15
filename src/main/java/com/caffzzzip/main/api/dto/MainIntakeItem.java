package com.caffzzzip.main.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MainIntakeItem {

    // 메뉴명
    private String menuName;

    // 브랜드명
    private String brand;

    // 카페인량
    private int caffeineMg;

    // 섭취 시간
    private String intakeTime;

    private int quantity;
}