package com.caffzzzip.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 카카오 고유 사용자 ID
    @Column(nullable = false, unique = true)
    private Long kakaoId;

    // 이메일
    @Column(nullable = false)
    private String email;

    // 닉네임
    @Column(nullable = false)
    private String nickname;

    // 프로필 이미지
    @Column(nullable = false, length = 1000)
    private String profileImageUrl;

    // 최초 설정 완료 여부
    @Column(nullable = false)
    private boolean initialSettingCompleted;

    @Builder
    public User(Long kakaoId,
                String email,
                String nickname,
                String profileImageUrl) {

        this.kakaoId = kakaoId;
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.initialSettingCompleted = false;
    }

    public void completeInitialSetting() {
        this.initialSettingCompleted = true;
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }
}