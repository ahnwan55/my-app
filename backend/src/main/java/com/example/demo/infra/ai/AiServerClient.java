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
 * AiServerClient - Spring Boot → FastAPI (sroberta) 임베딩 클라이언트
 *
 * 역할:
 *   - 텍스트를 768차원 벡터로 변환 (jhgan/ko-sroberta-multitask)
 *   - 도서 설명 사전 임베딩 (배치)
 *   - 사용자 답변 실시간 임베딩 (pgvector 유사도 검색용)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiServerClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${ai-server.url}")
    private String aiServerUrl;

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    /**
     * 텍스트 임베딩 변환
     *
     * 요청 형식:
     *   { "text": "책 제목 + 저자 + 책 소개" }
     *
     * 응답 형식:
     *   { "embedding": [0.1, 0.2, ..., 0.9] }  (768차원)
     *
     * @param text 임베딩할 텍스트
     * @return 768차원 float 벡터
     */
    public List<Float> embed(String text) {
        WebClient client = webClientBuilder.baseUrl(aiServerUrl).build();

        Map<String, Object> requestBody = Map.of("text", text);

        JsonNode response = client.post()
                .uri("/embed")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(TIMEOUT)
                .onErrorResume(e -> {
                    log.warn("[AiServerClient] FastAPI /embed 호출 실패: {}", e.getMessage());
                    return Mono.empty();
                })
                .block();

        if (response == null) {
            throw new IllegalStateException("FastAPI /embed 응답이 없습니다.");
        }

        JsonNode embeddingNode = response.path("embedding");
        if (embeddingNode.isMissingNode() || !embeddingNode.isArray()) {
            throw new IllegalStateException("FastAPI /embed 응답에 embedding이 없습니다.");
        }

        List<Float> embedding = new java.util.ArrayList<>();
        for (JsonNode value : embeddingNode) {
            embedding.add((float) value.asDouble());
        }

        return embedding;
    }

    /**
     * 도서 배치 임베딩 (최초 1회 도서 DB 구축 시 사용)
     *
     * 요청 형식:
     *   { "texts": ["책1 정보", "책2 정보", ...] }
     *
     * 응답 형식:
     *   { "embeddings": [[0.1, ...], [0.2, ...], ...] }
     *
     * @param texts 임베딩할 텍스트 목록
     * @return 768차원 벡터 목록
     */
    public List<List<Float>> embedBatch(List<String> texts) {
        WebClient client = webClientBuilder.baseUrl(aiServerUrl).build();

        Map<String, Object> requestBody = Map.of("texts", texts);

        JsonNode response = client.post()
                .uri("/embed/batch")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(120))  // 배치는 더 오래 걸릴 수 있음
                .onErrorResume(e -> {
                    log.warn("[AiServerClient] FastAPI /embed/batch 호출 실패: {}", e.getMessage());
                    return Mono.empty();
                })
                .block();

        if (response == null) {
            throw new IllegalStateException("FastAPI /embed/batch 응답이 없습니다.");
        }

        JsonNode embeddingsNode = response.path("embeddings");
        if (embeddingsNode.isMissingNode() || !embeddingsNode.isArray()) {
            throw new IllegalStateException("FastAPI /embed/batch 응답에 embeddings가 없습니다.");
        }

        List<List<Float>> result = new java.util.ArrayList<>();
        for (JsonNode embedding : embeddingsNode) {
            List<Float> vector = new java.util.ArrayList<>();
            for (JsonNode value : embedding) {
                vector.add((float) value.asDouble());
            }
            result.add(vector);
        }

        return result;
    }
}