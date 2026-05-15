package com.caffzzzip.menu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "브랜드 목록 응답")
public record BrandResponse(

        @Schema(description = "브랜드명", example = "Starbucks")
        String brand
) {
}