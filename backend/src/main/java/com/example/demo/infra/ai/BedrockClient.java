package com.example.demo.infra.ai;

import com.example.demo.domain.persona.entity.PersonaCode;
import com.example.demo.infra.ai.dto.BedrockAnalysisResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.List;
import java.util.Map;

/**
 * BedrockClient - AWS Bedrock Claude 호출 클라이언트
 *
 * 역할:
 *   1. 페르소나 판별: 설문 답변 → Bedrock Claude → PersonaCode + 판정 이유 반환
 *   2. 추천 코멘트 생성: 추천 도서 목록 → Bedrock Claude → 추천 코멘트 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BedrockClient {

    private final BedrockRuntimeClient bedrockRuntimeClient;
    private final ObjectMapper objectMapper;

    // application.yml의 aws.bedrock.model-id → .env의 BEDROCK_MODEL_ID 주입
    // 하드코딩 제거: 모델 버전 변경 시 .env만 수정하면 됨
    @Value("${aws.bedrock.model-id}")
    private String modelId;

    /**
     * 페르소나 판별
     *
     * 설문 답변 Map을 받아 Bedrock Claude에게 분석을 요청하고,
     * PersonaAnalysis 저장에 필요한 모든 정보를 BedrockAnalysisResult로 반환한다.
     *
     * @param surveyAnswers 설문 답변 Map (key: 질문, value: 답변)
     * @return BedrockAnalysisResult (personaCode, personaReason, rawResponse, modelId)
     */
    public BedrockAnalysisResult classifyPersona(Map<String, String> surveyAnswers) {
        String prompt = buildPersonaPrompt(surveyAnswers);
        String rawResponse = invokeModel(prompt);

        try {
            JsonNode json = objectMapper.readTree(rawResponse);

            // persona_code 추출 및 enum 매핑
            String code = json.path("persona_code").asText(null);
            if (code == null || code.isBlank()) {
                throw new IllegalStateException("Bedrock 응답에 persona_code가 없습니다.");
            }
            PersonaCode personaCode = PersonaCode.valueOf(code.toUpperCase());

            // persona_reason 추출 (없으면 빈 문자열)
            String personaReason = json.path("reason").asText("");

            return BedrockAnalysisResult.builder()
                    .personaCode(personaCode)
                    .personaReason(personaReason)
                    .rawResponse(rawResponse)
                    .modelId(modelId)
                    .build();

        } catch (Exception e) {
            log.error("[BedrockClient] 페르소나 판별 실패: {}", e.getMessage());
            throw new IllegalStateException("Bedrock 페르소나 판별 실패", e);
        }
    }

    /**
     * 추천 도서 AI 코멘트 생성
     *
     * @param profile 사용자 페르소나 정보 (persona_name 등)
     * @param books   추천 도서 목록 (title, authors 등)
     * @return 추천 코멘트 문자열. 실패 시 null 반환 (추천 자체는 정상 진행)
     */
    public String generateRecommendComment(Map<String, Object> profile,
                                           List<Map<String, Object>> books) {
        String prompt = buildRecommendPrompt(profile, books);
        try {
            return invokeModel(prompt);
        } catch (Exception e) {
            log.warn("[BedrockClient] 추천 코멘트 생성 실패 (추천은 정상 진행): {}", e.getMessage());
            return null;
        }
    }

    // ── Bedrock 모델 호출 ──────────────────────────────────────────────────

    /**
     * Bedrock Claude API 호출 공통 메서드.
     * 응답의 content[0].text 값을 추출하여 반환한다.
     */
    private String invokeModel(String prompt) {
        try {
            // Bedrock Claude Messages API 요청 바디 구성
            String requestBody = objectMapper.writeValueAsString(Map.of(
                "anthropic_version", "bedrock-2023-05-31",
                "max_tokens", 1000,
                "messages", List.of(
                    Map.of("role", "user", "content", prompt)
                )
            ));

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(modelId)
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(requestBody))
                    .build();

            InvokeModelResponse response = bedrockRuntimeClient.invokeModel(request);

            // 응답 파싱: content[0].text 추출
            JsonNode responseJson = objectMapper.readTree(
                    response.body().asUtf8String()
            );
            return responseJson
                    .path("content")
                    .get(0)
                    .path("text")
                    .asText();

        } catch (Exception e) {
            log.error("[BedrockClient] Bedrock 호출 실패 (modelId={}): {}", modelId, e.getMessage());
            throw new IllegalStateException("Bedrock 호출 실패", e);
        }
    }

    // ── 프롬프트 빌더 ──────────────────────────────────────────────────────

    /**
     * 페르소나 판별 프롬프트.
     * Claude에게 JSON만 반환하도록 명시적으로 지시한다.
     * 응답 형식: { "persona_code": "...", "reason": "...", "scores": { ... }, "confidence": 0.0 }
     */
    private String buildPersonaPrompt(Map<String, String> surveyAnswers) {
        StringBuilder sb = new StringBuilder();
        sb.append("다음 독서 성향 설문 답변을 분석하여 아래 6가지 페르소나 중 하나로 분류하고 ");
        sb.append("6대 지표 점수를 0~10점으로 JSON 형식으로만 반환하세요.\n\n");
        sb.append("페르소나 종류: EXPLORER, CURATOR, NAVIGATOR, DWELLER, ANALYST, DIVER\n\n");
        sb.append("설문 답변:\n");

        surveyAnswers.forEach((question, answer) ->
            sb.append("Q: ").append(question).append("\n")
              .append("A: ").append(answer).append("\n\n")
        );

        sb.append("반환 형식 (JSON만 반환, 다른 텍스트 없이):\n");
        sb.append("{\n");
        sb.append("  \"persona_code\": \"EXPLORER\",\n");
        sb.append("  \"reason\": \"지적 호기심이 강하고 다양한 분야를 탐험하려는 성향이 두드러집니다.\",\n");
        sb.append("  \"scores\": {\n");
        sb.append("    \"지적_확장성\": 9.5,\n");
        sb.append("    \"분석적_깊이\": 8.0,\n");
        sb.append("    \"실용_지향성\": 3.0,\n");
        sb.append("    \"감성_몰입도\": 2.0,\n");
        sb.append("    \"정보_체계화\": 5.5,\n");
        sb.append("    \"사회적_영향도\": 1.5\n");
        sb.append("  },\n");
        sb.append("  \"confidence\": 0.87\n");
        sb.append("}");

        return sb.toString();
    }

    /**
     * 추천 코멘트 프롬프트.
     * 페르소나 유형과 추천 도서 목록을 바탕으로 2~3문장의 추천 이유를 생성한다.
     */
    private String buildRecommendPrompt(Map<String, Object> profile,
                                        List<Map<String, Object>> books) {
        StringBuilder sb = new StringBuilder();
        sb.append("독서 페르소나 '").append(profile.get("persona_name")).append("'에게 ");
        sb.append("어울리는 도서 추천 이유를 2~3문장으로 작성해주세요.\n\n");
        sb.append("추천 도서 목록:\n");
        books.forEach(book ->
            sb.append("- ").append(book.get("title"))
              .append(" (").append(book.get("authors")).append(")\n")
        );
        return sb.toString();
    }
}
