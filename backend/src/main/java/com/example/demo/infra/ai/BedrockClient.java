package com.example.demo.infra.ai;

import com.example.demo.domain.persona.entity.PersonaCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.Map;

/**
 * BedrockClient - AWS Bedrock LLM 호출 클라이언트
 *
 * 역할:
 *   1. 페르소나 판단: 설문 10개 답변 → Bedrock LLM → 페르소나 코드 + 6대 지표 JSON 반환
 *   2. AI 코멘트 생성: 추천 도서 목록 → Bedrock LLM → 추천 코멘트 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BedrockClient {

    private final BedrockRuntimeClient bedrockRuntimeClient;
    private final ObjectMapper objectMapper;

    // 사용할 Bedrock 모델 ID (Claude 3 Sonnet)
    private static final String MODEL_ID = "anthropic.claude-3-sonnet-20240229-v1:0";

    /**
     * 페르소나 판단 및 6대 지표 스코어링
     *
     * 요청: 설문 10개 답변
     * 응답: { "persona_code": "EXPLORER", "scores": { ... }, "confidence": 0.87 }
     */
    public PersonaCode classifyPersona(Map<String, String> surveyAnswers) {
        String prompt = buildPersonaPrompt(surveyAnswers);
        String response = invokeModel(prompt);

        try {
            JsonNode json = objectMapper.readTree(response);
            String code = json.path("persona_code").asText(null);

            if (code == null || code.isBlank()) {
                throw new IllegalStateException("Bedrock 응답에 persona_code가 없습니다.");
            }
            return PersonaCode.valueOf(code.toUpperCase());

        } catch (Exception e) {
            log.error("[BedrockClient] 페르소나 판단 실패: {}", e.getMessage());
            throw new IllegalStateException("Bedrock 페르소나 판단 실패", e);
        }
    }

    /**
     * 추천 도서 AI 코멘트 생성
     */
    public String recommend(Map<String, Object> profile, java.util.List<Map<String, Object>> books) {
        String prompt = buildRecommendPrompt(profile, books);
        try {
            return invokeModel(prompt);
        } catch (Exception e) {
            log.warn("[BedrockClient] 추천 코멘트 생성 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Bedrock 모델 호출
     */
    private String invokeModel(String prompt) {
        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "anthropic_version", "bedrock-2023-05-31",
                    "max_tokens", 1000,
                    "messages", java.util.List.of(
                            Map.of("role", "user", "content", prompt)
                    )
            ));

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(MODEL_ID)
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(requestBody))
                    .build();

            InvokeModelResponse response = bedrockRuntimeClient.invokeModel(request);
            JsonNode responseJson = objectMapper.readTree(
                    response.body().asUtf8String()
            );

            return responseJson
                    .path("content")
                    .get(0)
                    .path("text")
                    .asText();

        } catch (Exception e) {
            log.error("[BedrockClient] Bedrock 호출 실패: {}", e.getMessage());
            throw new IllegalStateException("Bedrock 호출 실패", e);
        }
    }

    /**
     * 페르소나 판단 프롬프트 생성
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
     * 추천 코멘트 프롬프트 생성
     */
    private String buildRecommendPrompt(Map<String, Object> profile,
                                        java.util.List<Map<String, Object>> books) {
        StringBuilder sb = new StringBuilder();
        sb.append("독서 페르소나 ").append(profile.get("persona_name"));
        sb.append("에게 어울리는 도서 추천 이유를 2~3문장으로 작성해주세요.\n\n");
        sb.append("추천 도서 목록:\n");
        books.forEach(book ->
                sb.append("- ").append(book.get("title"))
                        .append(" (").append(book.get("authors")).append(")\n")
        );
        return sb.toString();
    }
}