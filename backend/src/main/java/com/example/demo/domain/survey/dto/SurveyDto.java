package com.example.demo.domain.survey.dto;

import com.example.demo.domain.persona.entity.PersonaCode;
import com.example.demo.domain.survey.constant.SurveyQuestions;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * SurveyDto — 설문 관련 요청/응답 DTO 모음
 *
 * 변경 사항:
 *   - Survey, SurveyQuestion, SurveySession 엔티티 제거
 *     → SurveyQuestions 상수 클래스 기반으로 교체
 *   - StartRequest, StartResponse 제거 (세션 개념 없음)
 *   - 설문 흐름: 문항 조회 → 답변 제출 → 페르소나 반환
 */
public class SurveyDto {

    // ── 응답 DTO ───────────────────────────────────────────────────────────

    /**
     * 설문 문항 목록 응답 DTO
     * GET /api/surveys/questions 응답에 사용
     * SurveyQuestions 상수에서 문항 번호(1~N)와 내용을 반환한다.
     */
    @Getter
    @Builder
    public static class QuestionResponse {
        private Integer questionNo;  // 문항 번호 (1부터 시작)
        private String content;      // 문항 내용

        // SurveyQuestions 상수 리스트 → QuestionResponse 리스트 변환
        public static List<QuestionResponse> fromConstants() {
            List<String> questions = SurveyQuestions.QUESTIONS;
            return IntStream.range(0, questions.size())
                    .mapToObj(i -> QuestionResponse.builder()
                            .questionNo(i + 1)
                            .content(questions.get(i))
                            .build())
                    .toList();
        }
    }

    /**
     * 설문 답변 제출 응답 DTO — 페르소나 결과 포함
     * POST /api/surveys/submit 응답에 사용
     */
    @Getter
    @Builder
    public static class SubmitResponse {
        private PersonaCode personaCode;   // 예: TREND_SURFER
        private String personaName;        // 예: 트렌드 서퍼
        private String personaReason;      // Bedrock이 반환한 판정 이유
    }

    // ── 요청 DTO ───────────────────────────────────────────────────────────

    /**
     * 설문 답변 제출 요청 DTO
     * POST /api/surveys/submit 요청 바디에 사용
     *
     * answers: 문항 번호(Q1~QN) → 서술형 답변 텍스트 Map
     * 예: { "Q1": "조용하고 감성적인 분위기의 책을 좋아해요.", "Q2": "..." }
     *
     * Map 구조를 쓰는 이유:
     *   - answers_json으로 그대로 직렬화하여 PersonaAnalysis에 저장 가능
     *   - Bedrock 프롬프트에 바로 넘길 수 있어 변환 과정이 단순해짐
     */
    @Getter
    public static class SubmitRequest {
        private Map<String, String> answers;  // key: "Q1"~"Q10", value: 서술형 답변
    }
}
