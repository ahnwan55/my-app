package com.example.demo.infra.ai;

import com.example.demo.domain.persona.entity.PersonaCode;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * AiServerClient - Spring Boot → FastAPI 내부 HTTP 통신 클라이언트
 *
 * WebClient를 사용하는 이유:
 *   - RestTemplate은 동기 블로킹 방식이라 AI 서버 응답 대기 중 스레드가 묶임
 *   - WebClient는 논블로킹 방식으로 응답 대기 중 다른 요청 처리 가능
 *   - AI 추론은 수 초 걸릴 수 있어서 WebClient가 적합
 *
 * 호출 흐름:
 *   SurveyService    → AiServerClient.classifyPersona() → FastAPI /persona  → PersonaCode 반환
 *   RecommendationService → AiServerClient.recommend()  → FastAPI /recommend → 추천 결과 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiServerClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${ai-server.url}")
    private String aiServerUrl;

    // AI 서버 응답 최대 대기 시간 (LLM 추론은 오래 걸릴 수 있음)
    private static final Duration TIMEOUT = Duration.ofSeconds(60);

    /**
     * FastAPI /persona 엔드포인트 호출 - 페르소나 분류
     *
     * 설문 10문항의 주관식 답변을 FastAPI로 전달하면
     * LLM이 분석하여 6개 페르소나 중 하나의 코드를 반환합니다.
     *
     * 요청 형식:
     *   { "answers": { "문항 내용": "사용자 답변", ... } }
     *
     * 응답 형식:
     *   { "persona_code": "EXPLORER" }
     *
     * @param surveyAnswers 문항 내용(key) → 사용자 답변(value) Map
     * @return PersonaCode (분류 실패 시 예외 발생 → 호출부에서 폴백 처리)
     */
    public PersonaCode classifyPersona(Map<String, String> surveyAnswers) {
        WebClient client = webClientBuilder.baseUrl(aiServerUrl).build();

        Map<String, Object> requestBody = Map.of("answers", surveyAnswers);

        JsonNode response = client.post()
                .uri("/persona")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(TIMEOUT)
                .onErrorResume(e -> {
                    log.warn("[AiServerClient] FastAPI /persona 호출 실패: {}", e.getMessage());
                    return Mono.empty();
                })
                .block();

        if (response == null) {
            throw new IllegalStateException("FastAPI /persona 응답이 없습니다.");
        }

        String code = response.path("persona_code").asText(null);
        if (code == null || code.isBlank()) {
            throw new IllegalStateException("FastAPI /persona 응답에 persona_code가 없습니다.");
        }

        try {
            return PersonaCode.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("알 수 없는 페르소나 코드: " + code);
        }
    }

    /**
     * FastAPI /recommend 엔드포인트 호출 - 도서 추천 코멘트 생성
     *
     * @param profile  사용자 프로필 (페르소나, 독서 성향 등)
     * @param products 추천 후보 도서 목록
     * @return AI 추천 코멘트 문자열, 실패 시 null
     */
    public String recommend(Map<String, Object> profile, List<Map<String, Object>> products) {
        try {
            WebClient client = webClientBuilder.baseUrl(aiServerUrl).build();

            Map<String, Object> requestBody = Map.of(
                    "profile", profile,
                    "products", products
            );

            JsonNode response = client.post()
                    .uri("/recommend")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(TIMEOUT)
                    .onErrorResume(e -> {
                        log.warn("[AiServerClient] FastAPI /recommend 호출 실패, 폴백 처리: {}",
                                e.getMessage());
                        return Mono.empty();
                    })
                    .block();

            if (response == null) {
                return null;
            }

            return response.path("result").asText(null);

        } catch (Exception e) {
            log.error("[AiServerClient] FastAPI /recommend 호출 중 오류: {}", e.getMessage());
            return null;
        }
    }
}