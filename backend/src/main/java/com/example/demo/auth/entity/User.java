package com.example.demo.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 카카오 고유 ID (OAuth 로그인 시 사용)
    @Column(name = "kakao_id", unique = true)
    private Long kakaoId;

    // 이메일은 카카오에서 못 받아올 수 있으므로 nullable
    @Column(unique = true)
    private String email;

    // 카카오 로그인 시 패스워드 불필요 → nullable
    @Column
    private String password;

    // 카카오 닉네임
    @Column(nullable = false)
    private String nickname;

    // 프로필 사진 URL (선택 동의)
    @Column(name = "profile_image")
    private String profileImage;

    // FCM 푸시 알림 토큰
    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // FCM 토큰 업데이트
    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    // 프로필 업데이트
    public void updateProfile(String nickname, String profileImage) {
        this.nickname = nickname;
        this.profileImage = profileImage;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        // 카카오 로그인은 kakaoId, 일반 로그인은 email 사용
        return email != null ? email : String.valueOf(kakaoId);
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}