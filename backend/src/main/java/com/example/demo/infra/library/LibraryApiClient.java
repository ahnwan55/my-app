package com.example.demo.infra.library;

import com.example.demo.infra.library.dto.LibraryApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * LibraryApiClient - 도서관 정보나루 Open API 클라이언트
 *
 * 도서관 정보나루(https://data4library.kr)에서 제공하는 API를 호출합니다.
 * 인기대출도서 / 마니아 추천 / 다독자 추천 / 급상승 / 상세조회 5종을 지원합니다.
 *
 * ⚠️ GitHub 보안 주의사항:
 *   - api-key는 application-local.yml → ${LIBRARY_API_KEY} 환경변수로 관리
 *   - application-local.yml은 .gitignore에 등록되어 있어 GitHub에 올라가지 않음
 *   - 절대 키 값을 코드에 직접 입력하지 말 것
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LibraryApiClient {

    private final WebClient.Builder webClientBuilder;

    /**
     * application-local.yml의 library.api-key 값을 주입받습니다.
     * 키가 없으면 "NOT_SET"으로 처리하여 더미 데이터를 반환합니다.
     */
    @Value("${library.api-key:NOT_SET}")
    private String apiKey;

    private static final String BASE_URL = "https://data4library.kr";

    // API 응답 최대 도서 수
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 인기 대출 도서 조회
     * GET /api/loanItemSrch
     *
     * @param startDt 시작일 (yyyy-MM-dd)
     * @param endDt   종료일 (yyyy-MM-dd)
     */
    public List<LibraryApiResponse.BookItem> getLoanItems(String startDt, String endDt) {
        if (isApiKeyNotSet()) return List.of();

        log.info("[LibraryApiClient] 인기대출도서 조회 {} ~ {}", startDt, endDt);

        LibraryApiResponse response = webClientBuilder.baseUrl(BASE_URL).build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/loanItemSrch")
                        .queryParam("authKey", apiKey)
                        .queryParam("startDt", startDt)
                        .queryParam("endDt", endDt)
                        .queryParam("pageSize", DEFAULT_PAGE_SIZE)
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .bodyToMono(LibraryApiResponse.class)
                .block();

        return extractBooks(response);
    }

    /**
     * 마니아를 위한 추천도서 조회
     * GET /api/recommandList?maniaType=mania
     *
     * EXPLORER, ANALYST, DIVER 페르소나에 적합
     */
    public List<LibraryApiResponse.BookItem> getManiaRecommendations() {
        if (isApiKeyNotSet()) return List.of();

        log.info("[LibraryApiClient] 마니아 추천도서 조회");

        LibraryApiResponse response = webClientBuilder.baseUrl(BASE_URL).build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/recommandList")
                        .queryParam("authKey", apiKey)
                        .queryParam("maniaType", "mania")
                        .queryParam("pageSize", DEFAULT_PAGE_SIZE)
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .bodyToMono(LibraryApiResponse.class)
                .block();

        return extractBooks(response);
    }

    /**
     * 다독자를 위한 추천도서 조회
     * GET /api/recommandList?maniaType=reader
     *
     * CURATOR, NAVIGATOR 페르소나에 적합
     */
    public List<LibraryApiResponse.BookItem> getReaderRecommendations() {
        if (isApiKeyNotSet()) return List.of();

        log.info("[LibraryApiClient] 다독자 추천도서 조회");

        LibraryApiResponse response = webClientBuilder.baseUrl(BASE_URL).build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/recommandList")
                        .queryParam("authKey", apiKey)
                        .queryParam("maniaType", "reader")
                        .queryParam("pageSize", DEFAULT_PAGE_SIZE)
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .bodyToMono(LibraryApiResponse.class)
                .block();

        return extractBooks(response);
    }

    /**
     * 대출 급상승 도서 조회
     * GET /api/loanItemSrch?ascending=true
     *
     * NAVIGATOR, CURATOR 페르소나에 적합 (트렌드 민감)
     */
    public List<LibraryApiResponse.BookItem> getRisingBooks(String startDt, String endDt) {
        if (isApiKeyNotSet()) return List.of();

        log.info("[LibraryApiClient] 대출 급상승 도서 조회 {} ~ {}", startDt, endDt);

        LibraryApiResponse response = webClientBuilder.baseUrl(BASE_URL).build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/loanItemSrch")
                        .queryParam("authKey", apiKey)
                        .queryParam("startDt", startDt)
                        .queryParam("endDt", endDt)
                        .queryParam("ascending", "true")
                        .queryParam("pageSize", DEFAULT_PAGE_SIZE)
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .bodyToMono(LibraryApiResponse.class)
                .block();

        return extractBooks(response);
    }

    /**
     * 도서 상세 조회
     * GET /api/srchDtlList
     *
     * @param isbn13 ISBN 13자리
     */
    public LibraryApiResponse.BookDetail getBookDetail(String isbn13) {
        if (isApiKeyNotSet()) return null;

        log.info("[LibraryApiClient] 도서 상세 조회 ISBN: {}", isbn13);

        LibraryApiResponse response = webClientBuilder.baseUrl(BASE_URL).build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/srchDtlList")
                        .queryParam("authKey", apiKey)
                        .queryParam("isbn13", isbn13)
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .bodyToMono(LibraryApiResponse.class)
                .block();

        if (response == null
                || response.getResponse() == null
                || response.getResponse().getResult() == null
                || response.getResponse().getResult().getDetail() == null
                || response.getResponse().getResult().getDetail().isEmpty()) {
            return null;
        }

        return response.getResponse().getResult().getDetail().get(0).getBook();
    }

    /**
     * API 응답에서 BookItem 목록을 추출합니다.
     * null 안전 처리 포함
     */
    private List<LibraryApiResponse.BookItem> extractBooks(LibraryApiResponse response) {
        if (response == null
                || response.getResponse() == null
                || response.getResponse().getResult() == null
                || response.getResponse().getResult().getDocs() == null) {
            log.warn("[LibraryApiClient] 응답 데이터가 없습니다.");
            return List.of();
        }

        return response.getResponse().getResult().getDocs().stream()
                .map(LibraryApiResponse.DocItem::getDoc)
                .toList();
    }

    /**
     * API 키 미설정 여부 확인
     */
    private boolean isApiKeyNotSet() {
        if ("NOT_SET".equals(apiKey)) {
            log.warn("[LibraryApiClient] API 키가 설정되지 않았습니다. LIBRARY_API_KEY 환경변수를 확인하세요.");
            return true;
        }
        return false;
    }
}