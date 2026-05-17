package com.caffzzzip.favorite.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "즐겨찾기 응답")
public record FavoriteResponse(

        @Schema(description = "즐겨찾기 ID", example = "1")
        Long favoriteId,

        @Schema(description = "메뉴 ID", example = "1")
        Long menuId,

        @Schema(description = "메뉴명", example = "아이스 아메리카노")
        String menuName,

        @Schema(description = "브랜드명", example = "Starbucks")
        String brand,

        @Schema(description = "카테고리명", example = "에스프레소")
        String categoryName,

        @Schema(description = "카페인 함량", example = "150")
        Integer caffeineMg
) {
}