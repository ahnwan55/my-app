package com.example.demo.domain.survey.dto;

import com.example.demo.domain.survey.entity.Survey;
import com.example.demo.domain.survey.entity.SurveyQuestion;
import com.example.demo.domain.survey.entity.SurveySession;
import com.example.demo.domain.persona.entity.PersonaCode;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * SurveyDto — 설문 관련 요청/응답 DTO 모음
 *
 * 주관식 자유 서술형으로 변경됨에 따라
 * 기존 options(선택지), scores(점수) 필드를 제거했습니다.
 * 답변은 selectedIndex(숫자) 대신 answerText(텍스트)로 받습니다.
 */
public class SurveyDto {

    // ─────────────────────────────────────────────────────────────
    // 응답 DTO
    // ─────────────────────────────────────────────────────────────

    /**
     * 설문지 전체 응답 DTO
     * GET /api/surveys/active 응답에 사용
     */
    @Getter
    @Builder
    public static class SurveyResponse {
        private Long surveyId;
        private String title;
        private List<QuestionResponse> questions;

        public static SurveyResponse of(Survey survey) {
            return SurveyResponse.builder()
                    .surveyId(survey.getId())
                    .title(survey.getTitle())
                    .questions(
                            survey.getQuestions().stream()
                                    .map(QuestionResponse::of)
                                    .toList()
                    )
                    .build();
        }
    }

    /**
     * 설문 문항 응답 DTO
     * 주관식이므로 questionId, content, orderNum만 반환합니다.
     * options, scores 제거됨
     */
    @Getter
    @Builder
    public static class QuestionResponse {
        private Long questionId;
        private String content;
        private Integer orderNum;

        public static QuestionResponse of(SurveyQuestion question) {
            return QuestionResponse.builder()
                    .questionId(question.getId())
                    .content(question.getContent())
                    .orderNum(question.getOrderNum())
                    .build();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 요청 DTO
    // ─────────────────────────────────────────────────────────────

    /**
     * 설문 시작 요청 DTO
     * POST /api/surveys/sessions 요청 바디에 사용
     */
    @Getter
    public static class StartRequest {
        private String sessionUuid;
    }

    /**
     * 설문 시작 응답 DTO
     * POST /api/surveys/sessions 응답에 사용
     */
    @Getter
    @Builder
    public static class StartResponse {
        private Long sessionId;
        private String sessionUuid;
        private Long surveyId;

        public static StartResponse of(SurveySession session) {
            return StartResponse.builder()
                    .sessionId(session.getId())
                    .sessionUuid(session.getSessionUuid())
                    .surveyId(session.getSurvey().getId())
                    .build();
        }
    }

    /**
     * 설문 답변 제출 요청 DTO
     * POST /api/surveys/sessions/{sessionUuid}/submit 요청 바디에 사용
     *
     * 주관식으로 변경됨에 따라 selectedIndex → answerText로 교체됩니다.
     * 문항 하나당 questionId + answerText(자유 서술 텍스트)를 함께 보냅니다.
     */
    @Getter
    public static class SubmitRequest {
        private List<AnswerItem> answers;

        @Getter
        public static class AnswerItem {
            private Long questionId;
            private String answerText;  // 사용자가 직접 입력한 주관식 답변
        }
    }

    /**
     * 설문 제출 응답 DTO — 페르소나 결과 포함
     * POST /api/surveys/sessions/{sessionUuid}/submit 응답에 사용
     *
     * totalScore 제거 (주관식이라 점수 없음)
     * AI 서버가 분류한 페르소나 코드와 이름을 반환합니다.
     */
    @Getter
    @Builder
    public static class SubmitResponse {
        private String sessionUuid;
        private PersonaCode personaCode;   // 예: EXPLORER
        private String personaName;        // 예: 지적 탐험가
    }
}