package com.example.demo.infra.library;

import com.example.demo.infra.library.dto.LibraryApiResponse;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * LibraryApiClient - 정보나루 API 호출 클라이언트
 *
 * 변경 사항:
 *   - @Value 키를 ${data-library.api-key}로 통일 (application.yml과 일치)
 *   - getMonthlyPopular() 추가 → 이달의 인기대출도서 Top10 조회
 *   - 기존 getLoanItems, getRisingBooks 등 유지
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LibraryApiClient {

    private final WebClient.Builder webClientBuilder;

    // application.yml: data-library.api-key → .env: LIBRARY_API_KEY
    @Value("${data-library.api-key:NOT_SET}")
    private String apiKey;

    private static final String BASE_URL = "https://data4library.kr";
    private static final int DEFAULT_PAGE_SIZE = 10;

    // XmlMapper는 스레드 안전하므로 필드로 공유
    private final XmlMapper xmlMapper = new XmlMapper();

    // ── 공통 유틸 ──────────────────────────────────────────────────────────

    private LibraryApiResponse parseXml(String xml) {
        if (xml == null || xml.isBlank()) return null;
        try {
            return xmlMapper.readValue(xml, LibraryApiResponse.class);
        } catch (Exception e) {
            log.error("[LibraryApiClient] XML 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    private List<LibraryApiResponse.BookItem> extractBooks(LibraryApiResponse response) {
        if (response == null || response.getDocs() == null) {
            log.warn("[LibraryApiClient] 응답 데이터가 없습니다.");
            return List.of();
        }
        return response.getDocs();
    }

    private boolean isApiKeyNotSet() {
        if ("NOT_SET".equals(apiKey)) {
            log.warn("[LibraryApiClient] API 키 미설정. LIBRARY_API_KEY 환경변수를 확인하세요.");
            return true;
        }
        return false;
    }

    // ── API 호출 메서드 ────────────────────────────────────────────────────

    /**
     * 이달의 인기대출도서 Top N 조회 (monthly_popular_book 적재 전용)
     * GET /api/loanItemSrch
     *
     * @param startDt  조회 시작일 (yyyy-MM-dd)
     * @param endDt    조회 종료일 (yyyy-MM-dd)
     * @param pageSize 조회 건수 (기본 10)
     */
    public List<LibraryApiResponse.BookItem> getMonthlyPopular(
            String startDt, String endDt, int pageSize) {
        if (isApiKeyNotSet()) return List.of();

        log.info("[LibraryApiClient] 이달 인기대출도서 조회 {} ~ {} Top{}", startDt, endDt, pageSize);

        String xml = webClientBuilder.baseUrl(BASE_URL).build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/loanItemSrch")
                        .queryParam("authKey", apiKey)
                        .queryParam("startDt", startDt)
                        .queryParam("endDt", endDt)
                        .queryParam("pageSize", pageSize)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractBooks(parseXml(xml));
    }

    /**
     * 인기 대출 도서 조회
     * GET /api/loanItemSrch
     */
    public List<LibraryApiResponse.BookItem> getLoanItems(String startDt, String endDt) {
        if (isApiKeyNotSet()) return List.of();

        log.info("[LibraryApiClient] 인기대출도서 조회 {} ~ {}", startDt, endDt);

        String xml = webClientBuilder.baseUrl(BASE_URL).build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/loanItemSrch")
                        .queryParam("authKey", apiKey)
                        .queryParam("startDt", startDt)
                        .queryParam("endDt", endDt)
                        .queryParam("pageSize", DEFAULT_PAGE_SIZE)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractBooks(parseXml(xml));
    }

    /**
     * 대출 급상승 도서 조회
     * GET /api/loanItemSrch?ascending=true
     */
    public List<LibraryApiResponse.BookItem> getRisingBooks(String startDt, String endDt) {
        if (isApiKeyNotSet()) return List.of();

        log.info("[LibraryApiClient] 대출 급상승 도서 조회 {} ~ {}", startDt, endDt);

        String xml = webClientBuilder.baseUrl(BASE_URL).build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/loanItemSrch")
                        .queryParam("authKey", apiKey)
                        .queryParam("startDt", startDt)
                        .queryParam("endDt", endDt)
                        .queryParam("ascending", "true")
                        .queryParam("pageSize", DEFAULT_PAGE_SIZE)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractBooks(parseXml(xml));
    }

    /**
     * 도서 상세 조회 (description 수집 전용)
     * GET /api/srchDtlList
     */
    public LibraryApiResponse.BookDetail getBookDetail(String isbn13) {
        if (isApiKeyNotSet()) return null;

        log.info("[LibraryApiClient] 도서 상세 조회 ISBN: {}", isbn13);

        String xml = webClientBuilder.baseUrl(BASE_URL).build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/srchDtlList")
                        .queryParam("authKey", apiKey)
                        .queryParam("isbn13", isbn13)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        LibraryApiResponse response = parseXml(xml);

        if (response == null
                || response.getDetail() == null
                || response.getDetail().isEmpty()) {
            return null;
        }
        return response.getDetail().get(0);
    }

    /**
     * 연령대/성별 조건 기반 인기 대출 도서 조회
     * GET /api/loanItemSrch
     */
    public List<LibraryApiResponse.BookItem> getLoanItemsByCondition(
            String startDt, String endDt, Integer ageGroup, Integer gender) {
        if (isApiKeyNotSet()) return List.of();

        log.info("[LibraryApiClient] 조건부 랭킹 조회 {} ~ {} 연령:{} 성별:{}",
                startDt, endDt, ageGroup, gender);

        String xml = webClientBuilder.baseUrl(BASE_URL).build()
                .get()
                .uri(uriBuilder -> {
                    uriBuilder
                            .path("/api/loanItemSrch")
                            .queryParam("authKey", apiKey)
                            .queryParam("startDt", startDt)
                            .queryParam("endDt", endDt)
                            .queryParam("pageSize", 20);
                    if (ageGroup != null) uriBuilder.queryParam("age", ageGroup);
                    if (gender != null)   uriBuilder.queryParam("gender", gender);
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractBooks(parseXml(xml));
    }
}
