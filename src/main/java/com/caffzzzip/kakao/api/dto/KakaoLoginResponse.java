package com.caffzzzip.kakao.api.dto;

public record KakaoLoginResponse(
        String accessToken,
        Boolean isFirstLogin,
        Long userId,
        String nickname
) {
}