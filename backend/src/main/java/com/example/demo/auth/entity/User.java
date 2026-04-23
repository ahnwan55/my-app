package com.example.demo.auth.entity;

import com.example.demo.domain.persona.entity.PersonaType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    // 카카오가 부여하는 고유 식별자. Long 타입 유지 (UserRepository와 통일)
    @Column(name = "kakao_id", nullable = false, unique = true)
    private Long kakaoId;

    // 카카오 선택 동의 or 서비스 내 직접 설정. nullable 허용
    @Column(name = "nickname", length = 30)
    private String nickname;

    // 현재 페르소나 (최신 분석 결과). LAZY: 필요할 때만 조회
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id")
    private PersonaType personaType;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    // JWT 리프레시 토큰. 재발급 검증 시 DB값과 비교
    @Column(name = "refresh_token", length = 500)
    private String refreshToken;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public User(Long kakaoId, String nickname, String profileImage) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 카카오 재로그인 시 닉네임·프로필 최신화
    public void updateProfile(String nickname, String profileImage) {
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.updatedAt = LocalDateTime.now();
    }

    // 서비스 내 닉네임 직접 설정
    public void updateNickname(String nickname) {
        this.nickname = nickname;
        this.updatedAt = LocalDateTime.now();
    }

    // 분석 완료 후 현재 페르소나 갱신
    public void updatePersona(PersonaType personaType) {
        this.personaType = personaType;
        this.updatedAt = LocalDateTime.now();
    }

    // 로그인/재발급 시 리프레시 토큰 갱신. 로그아웃 시 null 전달
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        this.updatedAt = LocalDateTime.now();
    }
}
