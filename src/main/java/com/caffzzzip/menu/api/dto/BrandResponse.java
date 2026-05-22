package com.caffzzzip.menu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "브랜드 목록 응답")
public record BrandResponse(

        @Schema(description = "브랜드명", example = "스타벅스")
        String brand,

        @Schema(
                description = "브랜드 로고 이미지 URL",
                example = "/images/brands/starbucks.png"
        )
        String logoUrl
) {
}