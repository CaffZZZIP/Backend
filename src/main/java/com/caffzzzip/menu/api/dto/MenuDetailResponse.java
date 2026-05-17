package com.caffzzzip.menu.api.dto;

import com.caffzzzip.menu.domain.RiskLevel;
import com.caffzzzip.routine.domain.CaffeineSensitivity;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메뉴 상세 응답")
public record MenuDetailResponse(

        @Schema(description = "메뉴 ID", example = "1")
        Long menuId,

        @Schema(description = "메뉴명", example = "아이스 아메리카노")
        String menuName,

        @Schema(description = "브랜드명", example = "Starbucks")
        String brand,

        @Schema(description = "카테고리명", example = "에스프레소")
        String categoryName,

        @Schema(description = "메뉴 1개 기준 카페인 함량", example = "150")
        Integer caffeineMg,

        @Schema(description = "선택 수량", example = "1")
        Integer quantity,

        @Schema(description = "이번에 섭취할 카페인 총량", example = "150")
        Integer intakeCaffeine,

        @Schema(description = "오늘 기존 총 카페인 섭취량", example = "200")
        Integer todayTotalCaffeine,

        @Schema(description = "이번 메뉴까지 포함한 예상 총 카페인 섭취량", example = "350")
        Integer expectedTotalCaffeine,

        @Schema(description = "하루 권장 카페인 기준량", example = "400")
        Integer dailyRecommendedLimit,

        @Schema(description = "위험도", example = "CAUTION")
        RiskLevel riskLevel,

        @Schema(description = "위험도 표시 문구", example = "주의")
        String riskLabel,

        @Schema(description = "사용자 카페인 민감도", example = "NORMAL")
        CaffeineSensitivity caffeineSensitivity,

        @Schema(description = "취침 시간 기준 예상 잔존 카페인량", example = "37")
        Integer expectedRemainingCaffeine,

        @Schema(description = "안내 문구")
        String guideMessage
) {
}