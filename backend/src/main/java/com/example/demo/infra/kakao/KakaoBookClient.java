package com.example.demo.infra.kakao;

import com.example.demo.domain.book.dto.BookDto;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * KakaoBookClient — 카카오 도서 검색 API 클라이언트
 *
 * [API 정보]
 *   GET https://dapi.kakao.com/v3/search/book
 *   Authorization: KakaoAK {REST_API_KEY}
 *
 * [파라미터]
 *   query  — 검색 키워드 (필수)
 *   sort   — accuracy(정확도순) | latest(최신순)
 *   page   — 페이지 번호 (1~50)
 *   size   — 한 페이지 결과 수 (1~50)
 *
 * [응답 필드]
 *   title, authors[], publisher, datetime,
 *   thumbnail, contents, isbn
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoBookClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${kakao.rest-api-key}")
    private String restApiKey;

    private static final String KAKAO_BOOK_URL = "https://dapi.kakao.com";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    /**
     * 카카오 도서 검색
     *
     * @param query 검색 키워드
     * @param size  최대 결과 수 (최대 50)
     * @return BookDto.BookResponse 리스트
     */
    public List<BookDto.BookResponse> search(String query, int size) {
        WebClient client = webClientBuilder.baseUrl(KAKAO_BOOK_URL).build();

        JsonNode response = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v3/search/book")
                        .queryParam("query", query)
                        .queryParam("sort", "accuracy")
                        .queryParam("size", Math.min(size, 50))
                        .build())
                .header("Authorization", "KakaoAK " + restApiKey)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(TIMEOUT)
                .onErrorResume(e -> {
                    log.warn("[KakaoBookClient] 검색 실패. query={}, error={}", query, e.getMessage());
                    return Mono.empty();
                })
                .block();

        if (response == null) return List.of();

        List<BookDto.BookResponse> result = new ArrayList<>();
        JsonNode documents = response.path("documents");

        for (JsonNode doc : documents) {
            // isbn 필드에서 ISBN-13 추출 (공백으로 구분된 경우 뒤 13자리)
            String isbnRaw = doc.path("isbn").asText("");
            String isbn = extractIsbn13(isbnRaw);

            // authors 배열을 쉼표로 합산
            List<String> authors = new ArrayList<>();
            for (JsonNode author : doc.path("authors")) {
                authors.add(author.asText());
            }
            String author = String.join(", ", authors);

            // datetime에서 연도만 추출 (예: "2023-05-01T00:00:00.000+09:00" → "2023")
            String datetime = doc.path("datetime").asText("");
            String pubYear = datetime.length() >= 4 ? datetime.substring(0, 4) : "";

            result.add(BookDto.BookResponse.builder()
                    .bookId(isbn.isBlank() ? null : isbn)
                    .title(doc.path("title").asText(""))
                    .author(author)
                    .publisher(doc.path("publisher").asText(""))
                    .pubYear(pubYear)
                    .coverUrl(doc.path("thumbnail").asText(""))
                    .description(doc.path("contents").asText(""))
                    .kdc(null)   // 카카오 API는 KDC를 제공하지 않음
                    .build());
        }

        return result;
    }

    /**
     * ISBN 문자열에서 ISBN-13 추출
     * 카카오 API는 "ISBN-10 ISBN-13" 형태로 반환하는 경우가 있다.
     * 예: "8937460076 9788937460074" → "9788937460074"
     */
    private String extractIsbn13(String isbnRaw) {
        if (isbnRaw == null || isbnRaw.isBlank()) return "";
        String[] parts = isbnRaw.trim().split("\\s+");
        for (String part : parts) {
            if (part.length() == 13) return part;
        }
        return parts[parts.length - 1];
    }
}
