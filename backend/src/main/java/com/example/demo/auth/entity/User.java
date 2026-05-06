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

    @Column(name = "kakao_id", nullable = false, unique = true)
    private Long kakaoId;

    @Column(name = "nickname", length = 30)
    private String nickname;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id")
    private PersonaType personaType;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @Column(name = "refresh_token", length = 500)
    private String refreshToken;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "age_group", length = 20)
    private String ageGroup;

    /**
     * 마이페이지 등록 도서관 — 메인 (필수)
     * InventoryService가 이 값을 읽어 재고를 자동 조회한다.
     */
    @Column(name = "main_library_code", length = 20)
    private String mainLibraryCode;

    /**
     * 마이페이지 등록 도서관 — 서브 (선택)
     */
    @Column(name = "sub_library_code", length = 20)
    private String subLibraryCode;

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

    public void updateProfile(String nickname, String profileImage) {
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePersona(PersonaType personaType) {
        this.personaType = personaType;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateUserInfo(String nickname, String gender, String ageGroup) {
        this.nickname = nickname;
        this.gender = gender;
        this.ageGroup = ageGroup;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateLibraries(String mainLibraryCode, String subLibraryCode) {
        this.mainLibraryCode = mainLibraryCode;
        this.subLibraryCode = subLibraryCode;
        this.updatedAt = LocalDateTime.now();
    }
}
