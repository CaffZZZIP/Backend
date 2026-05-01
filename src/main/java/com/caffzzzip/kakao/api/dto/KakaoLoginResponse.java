package com.caffzzzip.kakao.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카카오 로그인 응답")
public record KakaoLoginResponse(

        @Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "최초 로그인 여부", example = "true")
        Boolean isFirstLogin,

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "사용자 닉네임", example = "연진")
        String nickname
) {
}