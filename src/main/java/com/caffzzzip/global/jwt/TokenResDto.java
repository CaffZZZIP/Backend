package com.caffzzzip.global.jwt;

public record TokenResDto(
        String accessToken,
        Boolean isFirstLogin
) {
}