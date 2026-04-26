package com.caffzzzip.kakao.api;

import com.caffzzzip.common.error.SuccessCode;
import com.caffzzzip.common.template.ApiResTemplate;
import com.caffzzzip.kakao.api.dto.KakaoLoginResponse;
import com.caffzzzip.kakao.application.KakaoAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    @GetMapping("/api/auth/kakao/callback")
    public ApiResTemplate<KakaoLoginResponse> kakaoCallback(@RequestParam String code) {
        KakaoLoginResponse response = kakaoAuthService.login(code);

        return ApiResTemplate.successResponse(
                SuccessCode.LOGIN_SUCCESS,
                response
        );
    }
}