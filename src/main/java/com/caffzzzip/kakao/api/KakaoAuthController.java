package com.caffzzzip.kakao.api;

import com.caffzzzip.common.error.SuccessCode;
import com.caffzzzip.common.template.ApiResTemplate;
import com.caffzzzip.kakao.api.dto.KakaoLoginResponse;
import com.caffzzzip.kakao.application.KakaoAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@Tag(name = "소셜 로그인", description = "카카오 로그인 API")
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;
    @Operation(
            summary = "카카오 로그인",
            description = "인가 코드를 받아 카카오 로그인 후 JWT를 발급합니다."
    )
    @GetMapping("/api/auth/kakao/callback")
    public ApiResTemplate<KakaoLoginResponse> kakaoCallback(@RequestParam String code) {
        KakaoLoginResponse response = kakaoAuthService.login(code);

        return ApiResTemplate.successResponse(
                SuccessCode.LOGIN_SUCCESS,
                response
        );
    }
}