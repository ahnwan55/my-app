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
 * DTO(Data Transfer Object)는 API 요청/응답에서만 사용하는 객체입니다.
 * Entity를 직접 반환하지 않는 이유:
 *   - Entity를 그대로 반환하면 DB 구조가 그대로 외부에 노출됩니다 (보안 위험)
 *   - Lazy Loading 관계가 있으면 직렬화 중 추가 쿼리가 발생하거나 오류가 납니다
 *   - DTO를 별도로 쓰면 API 스펙을 자유롭게 설계할 수 있습니다
 *
 * 내부 static 클래스로 묶는 이유:
 *   - SurveyDto.QuestionResponse 처럼 어떤 도메인 DTO인지 한눈에 파악 가능
 *   - 파일이 너무 많아지는 것을 방지
 */
public class SurveyDto {

    // ─────────────────────────────────────────────────────────────
    // 응답 DTO
    // ─────────────────────────────────────────────────────────────

    /**
     * 설문지 전체 응답 DTO
     * GET /api/surveys/active 응답에 사용
     *
     * Entity → DTO 변환 로직을 of() 정적 팩토리 메서드에 담아둡니다.
     * 서비스 레이어에서 new SurveyResponse(...)로 필드를 나열하는 것보다 훨씬 가독성이 좋습니다.
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
     * SurveyResponse 안에 포함됩니다.
     *
     * getOptionList(), getScoreList() 는 SurveyQuestion 에 이미 정의된
     * JSON 파싱 메서드를 그대로 활용합니다.
     */
    @Getter
    @Builder
    public static class QuestionResponse {
        private Long questionId;
        private String content;
        private Integer orderNum;
        private List<String> options;    // 선택지 목록 ["10% 미만", "10~30%", ...]
        private List<Integer> scores;    // 각 선택지 점수 [1, 2, 3]

        public static QuestionResponse of(SurveyQuestion question) {
            return QuestionResponse.builder()
                    .questionId(question.getId())
                    .content(question.getContent())
                    .orderNum(question.getOrderNum())
                    .options(question.getOptionList())
                    .scores(question.getScoreList())
                    .build();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 요청 DTO
    // ─────────────────────────────────────────────────────────────

    /**
     * 설문 시작 요청 DTO
     * POST /api/surveys/sessions 요청 바디에 사용
     *
     * 프론트엔드에서 UUID를 직접 생성해서 보내는 방식입니다.
     * UUID는 프론트가 생성 → 백엔드가 저장하는 패턴으로,
     * 이후 "내 세션 조회" 시에도 같은 UUID를 사용합니다.
     */
    @Getter
    public static class StartRequest {
        private String sessionUuid;  // 프론트에서 생성한 UUID (예: "550e8400-e29b-41d4-a716-446655440000")
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
     * 문항 하나당 questionId + selectedIndex를 함께 보냅니다.
     * 서버에서 selectedIndex로 해당 점수를 계산합니다.
     */
    @Getter
    public static class SubmitRequest {
        private List<AnswerItem> answers;

        @Getter
        public static class AnswerItem {
            private Long questionId;
            private Integer selectedIndex;  // 0부터 시작 (첫 번째 선택지 = 0)
        }
    }

    /**
     * 설문 제출 응답 DTO — 페르소나 결과 포함
     * POST /api/surveys/sessions/{sessionUuid}/submit 응답에 사용
     */
    @Getter
    @Builder
    public static class SubmitResponse {
        private String sessionUuid;
        private Integer totalScore;
        private PersonaCode personaCode;   // 예: SAFETY_GUARD
        private String personaName;        // 예: 철벽 수비대
    }
}