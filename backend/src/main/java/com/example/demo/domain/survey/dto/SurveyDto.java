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
 *   - SubmitResponse에 scores 필드 추가 (Radar Chart용 6대 지표 점수)
 */
public class SurveyDto {

    // ── 응답 DTO ───────────────────────────────────────────────────────────

    /**
     * 설문 문항 목록 응답 DTO
     * GET /api/surveys/questions 응답에 사용
     */
    @Getter
    @Builder
    public static class QuestionResponse {
        private Integer questionNo;
        private String content;

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
     * 설문 답변 제출 응답 DTO
     * POST /api/surveys/submit 응답에 사용
     *
     * scores: Radar Chart에 시각화할 6대 기본 지표 점수
     *   - 지적_확장성, 분석적_깊이, 실용_지향성,
     *     감성_몰입도, 정보_체계화, 사회적_영향도
     * 추가 4개 지표(독서_지속성 등)는 서브 분류 전용이므로 포함하지 않는다.
     */
    @Getter
    @Builder
    public static class SubmitResponse {
        private PersonaCode personaCode;    // 예: TREND_SURFER
        private String personaName;         // 예: 트렌드 서퍼
        private String personaReason;       // Bedrock 판정 이유
        private Map<String, Double> scores; // 6대 지표 점수 (Radar Chart용)
    }

    // ── 요청 DTO ───────────────────────────────────────────────────────────

    /**
     * 설문 답변 제출 요청 DTO
     * POST /api/surveys/submit 요청 바디에 사용
     */
    @Getter
    public static class SubmitRequest {
        private Map<String, String> answers;  // key: "Q1"~"Q10", value: 서술형 답변
    }
}
