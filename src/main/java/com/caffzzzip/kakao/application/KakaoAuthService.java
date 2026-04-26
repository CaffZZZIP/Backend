package com.caffzzzip.kakao.application;

import com.caffzzzip.global.jwt.JwtTokenProvider;
import com.caffzzzip.kakao.api.dto.KakaoLoginResponse;
import com.caffzzzip.kakao.api.dto.KakaoTokenResponse;
import com.caffzzzip.kakao.api.dto.KakaoUserResponse;
import com.caffzzzip.user.domain.User;
import com.caffzzzip.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KakaoAuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${kakao.client-secret:}")
    private String kakaoClientSecret;

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${kakao.token-uri}")
    private String kakaoTokenUri;

    @Value("${kakao.user-info-uri}")
    private String kakaoUserInfoUri;

    @Transactional
    public KakaoLoginResponse login(String code) {
        KakaoTokenResponse tokenResponse = getKakaoToken(code);
        KakaoUserResponse userResponse = getKakaoUserInfo(tokenResponse.accessToken());

        User user = findOrCreateUser(userResponse);

        String accessToken = jwtTokenProvider.generateToken(user);
        Boolean isFirstLogin = !user.isInitialSettingCompleted();

        return new KakaoLoginResponse(
                accessToken,
                isFirstLogin,
                user.getId(),
                user.getNickname()
        );
    }

    private KakaoTokenResponse getKakaoToken(String code) {
        log.info("카카오 토큰 요청 시작");
        log.info("redirectUri = {}", kakaoRedirectUri);
        log.info("code length = {}", code != null ? code.length() : null);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoClientId);
        body.add("redirect_uri", kakaoRedirectUri);
        body.add("code", code);
        if (kakaoClientSecret != null && !kakaoClientSecret.isBlank()) {
            body.add("client_secret", kakaoClientSecret);
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<KakaoTokenResponse> response = restTemplate.postForEntity(
                    kakaoTokenUri,
                    request,
                    KakaoTokenResponse.class
            );

            log.info("카카오 토큰 응답 상태 = {}", response.getStatusCode());
            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("카카오 토큰 요청 실패 상태 = {}", e.getStatusCode());
            log.error("카카오 토큰 요청 실패 바디 = {}", e.getResponseBodyAsString());
            throw e;
        }
    }

    private KakaoUserResponse getKakaoUserInfo(String kakaoAccessToken) {
        log.info("카카오 사용자 정보 요청 시작");

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(kakaoAccessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoUserResponse> response = restTemplate.exchange(
                    kakaoUserInfoUri,
                    HttpMethod.GET,
                    request,
                    KakaoUserResponse.class
            );

            log.info("카카오 사용자 정보 응답 상태 = {}", response.getStatusCode());
            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("카카오 사용자 정보 요청 실패 상태 = {}", e.getStatusCode());
            log.error("카카오 사용자 정보 요청 실패 바디 = {}", e.getResponseBodyAsString());
            throw e;
        }
    }

    private User findOrCreateUser(KakaoUserResponse userResponse) {
        return userRepository.findByKakaoId(userResponse.id())
                .orElseGet(() -> saveNewUser(userResponse));
    }

    private User saveNewUser(KakaoUserResponse userResponse) {
        User user = User.builder()
                .kakaoId(userResponse.id())
                .email(userResponse.getEmail())
                .nickname(userResponse.getNickname())
                .profileImageUrl(userResponse.getProfileImageUrl())
                .build();

        return userRepository.save(user);
    }
}