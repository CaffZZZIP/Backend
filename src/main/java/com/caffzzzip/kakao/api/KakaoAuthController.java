package com.caffzzzip.kakao.api;

import com.caffzzzip.kakao.api.dto.KakaoLoginResponse;
import com.caffzzzip.kakao.application.KakaoAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
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
            description = """
                    카카오 인가 코드를 받아 카카오 로그인 처리를 진행합니다.

                    처리 흐름:
                    1. 프론트 또는 브라우저에서 카카오 로그인 후 인가 코드(code)를 전달합니다.
                    2. 백엔드가 카카오 서버에 code를 전달하여 카카오 accessToken을 발급받습니다.
                    3. 카카오 사용자 정보를 조회합니다.
                    4. 서비스 사용자 정보를 저장 또는 조회합니다.
                    5. 서비스 JWT accessToken, refreshToken을 발급합니다.
                    6. 프론트엔드 callback 주소로 302 리다이렉트합니다.

                    이 API는 JSON 응답을 반환하지 않습니다.
                    로그인 성공 시 프론트엔드 redirect-uri로 이동하며,
                    accessToken, refreshToken, isFirstLogin, userId, nickname을 query parameter로 전달합니다.

                    프론트에서는 callback 페이지에서 query parameter를 읽어
                    토큰 저장 및 최초 로그인 여부에 따른 화면 이동을 처리하면 됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "302",
                    description = "카카오 로그인 성공 후 프론트엔드 callback 주소로 리다이렉트"
            )
    })
    @GetMapping("/api/auth/kakao/callback")
    public ResponseEntity<Void> kakaoCallback(@RequestParam String code) {

        KakaoLoginResponse response = kakaoAuthService.login(code);

        String redirectUrl = frontendRedirectUri
                + "?accessToken=" + encode(response.accessToken())
                + "&refreshToken=" + encode(response.refreshToken())
                + "&isFirstLogin=" + response.isFirstLogin()
                + "&userId=" + response.userId()
                + "&nickname=" + encode(response.nickname());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(redirectUrl));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}