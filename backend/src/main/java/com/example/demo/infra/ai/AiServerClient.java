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
 *   - RestTemplate은 동기 블로킹 방식이라 AI 서버 응답 대기 중 스레드가 묶임
 *   - WebClient는 논블로킹 방식으로 응답 대기 중 다른 요청 처리 가능
 *   - AI 추론은 수 초 걸릴 수 있어서 WebClient가 적합
 *
 * 호출 흐름:
 *   RecommendationService → AiServerClient.recommend() → FastAPI /recommend → 결과 반환
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
     * @return AI 추천 결과 문자열 (JSON 형식), 실패 시 null
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
}