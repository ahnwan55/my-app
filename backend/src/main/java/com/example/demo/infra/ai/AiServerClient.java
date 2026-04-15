package com.example.demo.infra.ai;

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
 *   - 기존 RestTemplate은 동기 블로킹 방식이라 AI 서버 응답 대기 중 스레드가 묶임
 *   - WebClient는 논블로킹 방식으로 응답 대기 중 다른 요청 처리 가능
 *   - AI 추론은 수 초 걸릴 수 있어서 WebClient가 적합
 *
 * 호출 흐름:
 *   RecommendationService → AiServerClient.recommend() → FastAPI /recommend → 결과 반환
 *   SurveyService → AiServerClient.classifyPersona() → FastAPI /persona → 페르소나 반환
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
     * FastAPI /recommend 엔드포인트 호출
     *
     * @param profile  사용자 프로필 (나이, 직업, 소득, 목표, 위험성향)
     * @param products 추천 후보 상품 목록
     * @return AI 추천 결과 문자열 (JSON 형식)
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
                    // AI 서버 장애 시 빈 결과로 폴백 - AI 서버 다운돼도 룰 기반 추천은 동작해야 함
                    .onErrorResume(e -> {
                        log.warn("[AiServerClient] FastAPI 호출 실패, 폴백 처리: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block();

            if (response == null) {
                return null;
            }

            return response.path("result").asText(null);

        } catch (Exception e) {
            log.error("[AiServerClient] FastAPI 호출 중 오류: {}", e.getMessage());
            return null;
        }
    }

    /**
     * FastAPI /persona 엔드포인트 호출 - XGBoost 페르소나 분류
     *
     * 호출 흐름:
     *   Spring Boot SurveyService (설문 완료 시)
     *     → AiServerClient.classifyPersona()
     *     → FastAPI /persona
     *     → XGBoost 분류 + SHAP 설명 반환
     *
     * 실패 시 null 반환 → SurveyService에서 기존 룰 기반 페르소나로 폴백
     *
     * @param age         나이
     * @param income      월 소득 (만원)
     * @param savingsRate 저축 성향 점수 (0~100)
     * @param riskScore   위험 선호도 점수 (0~100)
     * @param goalTerm    목표 기간 (개월)
     * @return 페르소나 코드 문자열 (예: "SAFETY_GUARD"), 실패 시 null
     */
    public String classifyPersona(int age, int income, int savingsRate,
                                  int riskScore, int goalTerm) {
        try {
            WebClient client = webClientBuilder.baseUrl(aiServerUrl).build();

            Map<String, Object> requestBody = Map.of(
                    "age", age,
                    "income", income,
                    "savings_rate", savingsRate,
                    "risk_score", riskScore,
                    "goal_term", goalTerm
            );

            JsonNode response = client.post()
                    .uri("/persona")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(TIMEOUT)
                    .onErrorResume(e -> {
                        log.warn("[AiServerClient] /persona 호출 실패, 폴백 처리: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block();

            if (response == null) {
                return null;
            }

            // persona_code만 추출해서 반환
            // shap_explanation은 추후 프론트엔드 XAI 표시용으로 확장 가능
            return response.path("persona_code").asText(null);

        } catch (Exception e) {
            log.error("[AiServerClient] /persona 호출 중 오류: {}", e.getMessage());
            return null;
        }
    }
}