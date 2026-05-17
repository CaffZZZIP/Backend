package com.caffzzzip.intake_results.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResultItem {

    // 메뉴 ID
    private Long menuId;

    // 메뉴명
    private String menuName;

    // 브랜드명
    private String brand;

    // 실제 섭취 카페인량
    private int caffeineMg;

    // 섭취 시간
    private String intakeAt;

    // 수량
    private Integer quantity;
}