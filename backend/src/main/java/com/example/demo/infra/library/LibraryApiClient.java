package com.example.demo.infra.library;

import com.example.demo.infra.library.dto.LibraryApiResponse;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LibraryApiClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${data-library.api-key:NOT_SET}")
    private String apiKey;

    private static final String BASE_URL = "https://data4library.kr";

    private final XmlMapper xmlMapper = new XmlMapper();

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
        if (response == null || response.getDoc() == null) {
            log.warn("[LibraryApiClient] 응답 데이터가 없습니다.");
            return List.of();
        }
        return response.getDoc();
    }

    private boolean isApiKeyNotSet() {
        if ("NOT_SET".equals(apiKey)) {
            log.warn("[LibraryApiClient] API 키 미설정. LIBRARY_API_KEY 환경변수를 확인하세요.");
            return true;
        }
        return false;
    }

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

        log.info("[LibraryApiClient] 원본 XML: {}", xml);
        LibraryApiResponse parsed = parseXml(xml);
        log.info("[LibraryApiClient] 파싱 결과 doc: {}", parsed != null ? parsed.getDoc() : "null");

        return extractBooks(parsed);
    }

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
                || response.getBook() == null
                || response.getBook().isEmpty()) {
            return null;
        }
        return response.getBook().get(0);
    }

    public LibraryApiResponse getLibraries(int pageNo, int pageSize) {
        if (isApiKeyNotSet()) return null;

        log.info("[LibraryApiClient] 도서관 목록 조회 pageNo={}", pageNo);

        String xml = webClientBuilder.baseUrl(BASE_URL).build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/libSrch")
                        .queryParam("authKey", apiKey)
                        .queryParam("region", "11")
                        .queryParam("pageNo", pageNo)
                        .queryParam("pageSize", pageSize)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return parseXml(xml);
    }
}