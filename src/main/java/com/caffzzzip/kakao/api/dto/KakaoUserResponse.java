package com.caffzzzip.kakao.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserResponse(

        Long id,

        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
) {

    public String getEmail() {
        return kakaoAccount.email();
    }

    public String getNickname() {
        return kakaoAccount.profile().nickname();
    }

    public String getProfileImageUrl() {
        return kakaoAccount.profile().profileImageUrl();
    }

    public record KakaoAccount(
            String email,
            Profile profile
    ) {
    }

    public record Profile(
            String nickname,

            @JsonProperty("profile_image_url")
            String profileImageUrl
    ) {
    }
}