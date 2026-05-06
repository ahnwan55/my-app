package com.example.demo.domain.user.dto;

import com.example.demo.auth.entity.User;
import com.example.demo.domain.persona.entity.PersonaType;
import com.example.demo.domain.survey.entity.PersonaAnalysis;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

public class UserDto {

    @Getter
    public static class UserInfoRequest {
        private String nickname;
        private String gender;
        private String ageGroup;
    }

    @Getter
    public static class LibraryRequest {
        private String mainLibraryCode;
        private String subLibraryCode;
    }

    @Getter
    @Builder
    public static class MeResponse {
        private Long   userId;
        private String nickname;
        private String profileImage;
        private String gender;
        private String ageGroup;
        private String mainLibraryCode;
        private String mainLibraryName;  // 추가 — 도서관 이름
        private String subLibraryCode;
        private String subLibraryName;   // 추가 — 도서관 이름
        private String createdAt;

        /**
         * User 엔티티 + 도서관 이름 2개를 받아서 DTO 생성
         * LibraryRepository로 조회한 이름을 서비스에서 주입한다.
         */
        public static MeResponse of(User user, String mainLibraryName, String subLibraryName) {
            return MeResponse.builder()
                    .userId(user.getUserId())
                    .nickname(user.getNickname())
                    .profileImage(user.getProfileImage())
                    .gender(user.getGender())
                    .ageGroup(user.getAgeGroup())
                    .mainLibraryCode(user.getMainLibraryCode())
                    .mainLibraryName(mainLibraryName)
                    .subLibraryCode(user.getSubLibraryCode())
                    .subLibraryName(subLibraryName)
                    .createdAt(user.getCreatedAt() != null
                            ? user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            : null)
                    .build();
        }
    }

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

    @Getter
    @Builder
    public static class AnalysisHistoryItem {
        private Long   analysisId;
        private String code;
        private String name;
        private String analyzedAt;

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
