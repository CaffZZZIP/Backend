package com.caffzzzip.kakao.api;

import com.caffzzzip.kakao.api.dto.KakaoLoginResponse;
import com.caffzzzip.kakao.application.KakaoAuthService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@Tag(name = "소셜 로그인", description = "카카오 로그인 API")
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    @Value("${frontend.redirect-uri}")
    private String frontendRedirectUri;

    @Operation(
            summary = "카카오 로그인",
            description = "인가 코드를 받아 카카오 로그인 후 JWT를 발급하고 프론트엔드로 리다이렉트합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "302",
                    description = "카카오 로그인 성공 후 프론트엔드 callback 주소로 리다이렉트"
            )
    })
    @GetMapping("/api/auth/kakao/callback")
    public RedirectView kakaoCallback(@RequestParam String code) {

        KakaoLoginResponse response = kakaoAuthService.login(code);

        String redirectUrl = frontendRedirectUri
                + "?accessToken=" + encode(response.accessToken())
                + "&refreshToken=" + encode(response.refreshToken())
                + "&isFirstLogin=" + response.isFirstLogin()
                + "&userId=" + response.userId()
                + "&nickname=" + encode(response.nickname());

        return new RedirectView(redirectUrl);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}