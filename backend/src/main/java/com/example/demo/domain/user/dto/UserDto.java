package com.example.demo.domain.user.dto;

import com.example.demo.auth.entity.User;
import com.example.demo.domain.persona.entity.PersonaType;
import com.example.demo.domain.survey.entity.PersonaAnalysis;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * UserDto — 사용자 관련 요청/응답 DTO 모음
 *
 * [포함 항목]
 * - UserInfoRequest  : POST /api/users/info 요청 (닉네임/성별/연령대)
 * - LibraryRequest   : PATCH /api/users/me/libraries 요청 (도서관 코드 3개)
 * - MeResponse       : GET /api/users/me 응답
 * - PersonaResponse  : GET /api/users/me/persona 응답
 * - AnalysisHistory  : GET /api/users/me/analysis-history 응답 단건
 */
public class UserDto {

    // ── 요청 DTO ──────────────────────────────────────────────────

    /**
     * UserInfoPage 제출 데이터
     * POST /api/users/info
     */
    @Getter
    public static class UserInfoRequest {
        private String nickname;   // 2~10자
        private String gender;     // "male" | "female" | "none"
        private String ageGroup;   // "20s" | "30s" | ...
    }

    /**
     * 마이페이지 도서관 등록
     * PATCH /api/users/me/libraries
     *
     * mainLibraryCode  — 필수 (메인 도서관)
     * subLibraryCode1  — 선택
     * subLibraryCode2  — 선택
     */
    @Getter
    public static class LibraryRequest {
        private String mainLibraryCode;
        private String subLibraryCode1;
        private String subLibraryCode2;
    }

    // ── 응답 DTO ──────────────────────────────────────────────────

    /**
     * GET /api/users/me 응답
     * MyPage 프로필 카드에 사용
     */
    @Getter
    @Builder
    public static class MeResponse {
        private Long   userId;
        private String nickname;
        private String profileImage;
        private String gender;
        private String ageGroup;
        private String mainLibraryCode;
        private String subLibraryCode1;
        private String subLibraryCode2;
        private String createdAt;       // "yyyy-MM-dd" 포맷

        public static MeResponse of(User user) {
            return MeResponse.builder()
                    .userId(user.getUserId())
                    .nickname(user.getNickname())
                    .profileImage(user.getProfileImage())
                    .gender(user.getGender())
                    .ageGroup(user.getAgeGroup())
                    .mainLibraryCode(user.getMainLibraryCode())
                    .subLibraryCode1(user.getSubLibraryCode1())
                    .subLibraryCode2(user.getSubLibraryCode2())
                    .createdAt(user.getCreatedAt() != null
                            ? user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            : null)
                    .build();
        }
    }

    /**
     * GET /api/users/me/persona 응답
     * MyPage 현재 페르소나 카드에 사용
     */
    @Getter
    @Builder
    public static class PersonaResponse {
        private String code;
        private String name;
        private String description;
        private String imageUrl;

        public static PersonaResponse of(PersonaType pt) {
            return PersonaResponse.builder()
                    .code(pt.getCode())
                    .name(pt.getName())
                    .description(pt.getDescription())
                    .imageUrl(pt.getImageUrl())
                    .build();
        }
    }

    /**
     * GET /api/users/me/analysis-history 응답 단건
     * MyPage 분석 이력 타임라인에 사용
     */
    @Getter
    @Builder
    public static class AnalysisHistoryItem {
        private Long   analysisId;
        private String code;
        private String name;
        private String analyzedAt;   // "yyyy-MM-dd" 포맷

        public static AnalysisHistoryItem of(PersonaAnalysis analysis) {
            PersonaType pt = analysis.getPersonaType();
            return AnalysisHistoryItem.builder()
                    .analysisId(analysis.getAnalysisId())
                    .code(pt != null ? pt.getCode() : null)
                    .name(pt != null ? pt.getName() : null)
                    .analyzedAt(analysis.getAnalyzedAt() != null
                            ? analysis.getAnalyzedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            : null)
                    .build();
        }
    }
}
