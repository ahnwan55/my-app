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

    /**
     * 성별
     * UserInfoPage에서 수집: "male" | "female" | "none"
     */
    @Column(name = "gender", length = 10)
    private String gender;

    /**
     * 연령대
     * UserInfoPage에서 수집: "elementary" | "middle" | "high" |
     *                        "20s" | "30s" | "40s" | "50s+"
     */
    @Column(name = "age_group", length = 20)
    private String ageGroup;

    /**
     * 마이페이지 등록 도서관 — 메인 (필수)
     * 정보나루 도서관 코드 (예: "111001")
     * InventoryService가 이 값을 읽어 재고를 자동 조회한다.
     */
    @Column(name = "main_library_code", length = 20)
    private String mainLibraryCode;

    /**
     * 마이페이지 등록 도서관 — 서브 1 (선택)
     */
    @Column(name = "sub_library_code1", length = 20)
    private String subLibraryCode1;

    /**
     * 마이페이지 등록 도서관 — 서브 2 (선택)
     */
    @Column(name = "sub_library_code2", length = 20)
    private String subLibraryCode2;

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

    /**
     * 최초 로그인 후 UserInfoPage에서 수집한 정보 저장
     * nickname은 카카오에서 받은 값을 덮어쓴다.
     */
    public void updateUserInfo(String nickname, String gender, String ageGroup) {
        this.nickname = nickname;
        this.gender = gender;
        this.ageGroup = ageGroup;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 마이페이지에서 등록한 도서관 코드 저장
     * subLibraryCode1, subLibraryCode2는 null 허용 (선택 항목)
     */
    public void updateLibraries(String mainLibraryCode, String subLibraryCode1, String subLibraryCode2) {
        this.mainLibraryCode = mainLibraryCode;
        this.subLibraryCode1 = subLibraryCode1;
        this.subLibraryCode2 = subLibraryCode2;
        this.updatedAt = LocalDateTime.now();
    }
}
