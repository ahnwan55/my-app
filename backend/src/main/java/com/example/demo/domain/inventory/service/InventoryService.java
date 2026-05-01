package com.example.demo.domain.inventory.service;

import com.example.demo.auth.entity.User;
import com.example.demo.auth.repository.UserRepository;
import com.example.demo.domain.inventory.dto.response.InventoryResponseDto;
import com.example.demo.domain.library.entity.Library;
import com.example.demo.domain.library.repository.LibraryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 도서관 도서 재고 조회 서비스
 *
 * 정보나루 Open API의 bookExist 엔드포인트를 호출하여
 * 특정 도서가 도서관에 소장되어 있는지, 현재 대출 가능한지를 반환한다.
 *
 * [흐름]
 * 1. 조회 대상 도서관 코드 목록 결정
 *    - 로그인 사용자 → 마이페이지 등록 도서관(메인 + 서브) 자동 포함
 *    - 추가 도서관 → libCodes 파라미터로 직접 전달
 * 2. 각 도서관 코드마다 정보나루 API 호출
 * 3. 결과 취합 후 InventoryResponseDto 리스트로 반환
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final RestClient restClient;
    private final UserRepository userRepository;
    private final LibraryRepository libraryRepository;

    // application.yml에서 주입 — GitHub에 올리지 않고 K8s Secret으로 관리
    @Value("${LIBRARY_API_KEY}")
    private String apiKey;

    private static final String BOOK_EXIST_URL = "http://data4library.kr/api/bookExist";

    /**
     * 재고 조회 메인 메서드
     *
     * @param isbn      ISBN-13
     * @param libCodes  추가로 조회할 도서관 코드 목록 (없으면 빈 리스트)
     * @param userId    로그인 사용자 ID
     * @return          도서관별 재고 현황 리스트
     */
    public List<InventoryResponseDto> getInventory(
            String isbn,
            List<String> libCodes,
            Long userId
    ) {
        // ── 1. 조회 대상 도서관 코드 수집 ──────────────────────────
        List<String> targetLibCodes = new ArrayList<>(libCodes);

        // 마이페이지에 등록된 도서관 코드를 자동으로 추가한다.
        // User 엔티티의 mainLibraryCode, subLibraryCode1, subLibraryCode2를 읽는다.
        userRepository.findById(userId).ifPresent(user -> {
            addIfAbsent(targetLibCodes, user.getMainLibraryCode());
            addIfAbsent(targetLibCodes, user.getSubLibraryCode1());
            addIfAbsent(targetLibCodes, user.getSubLibraryCode2());
        });

        if (targetLibCodes.isEmpty()) {
            log.warn("[InventoryService] 조회 대상 도서관 코드 없음. isbn={}, userId={}", isbn, userId);
            return List.of();
        }

        // ── 2. 도서관별 정보나루 API 호출 ─────────────────────────
        List<InventoryResponseDto> results = new ArrayList<>();

        for (String libCode : targetLibCodes) {
            try {
                results.add(callBookExistApi(isbn, libCode));
            } catch (Exception e) {
                // 특정 도서관 조회 실패 시 전체가 중단되지 않도록 개별 처리한다.
                log.error("[InventoryService] bookExist API 실패. libCode={}, isbn={}, error={}",
                        libCode, isbn, e.getMessage());
                results.add(InventoryResponseDto.error(libCode, isbn));
            }
        }

        return results;
    }

    /**
     * 정보나루 bookExist API 단건 호출
     *
     * 요청 URL 예시:
     *   http://data4library.kr/api/bookExist
     *     ?authKey={KEY}&libCode=111001&isbn13=9791165920715&format=json
     *
     * 응답 필드:
     *   hasBook   : "Y" / "N" (소장 여부)
     *   loanAvail : "Y" / "N" (현재 대출 가능 여부)
     */
    private InventoryResponseDto callBookExistApi(String isbn, String libCode) {
        String url = UriComponentsBuilder.fromHttpUrl(BOOK_EXIST_URL)
                .queryParam("authKey", apiKey)
                .queryParam("libCode", libCode)
                .queryParam("isbn13", isbn)
                .queryParam("format", "json")
                .toUriString();

        BookExistApiResponse apiResponse = restClient.get()
                .uri(url)
                .retrieve()
                .body(BookExistApiResponse.class);

        if (apiResponse == null || apiResponse.getResponse() == null) {
            throw new RuntimeException("정보나루 API 응답이 비어 있음");
        }

        BookExistApiResponse.Result result = apiResponse.getResponse().getResult();

        String libName = libraryRepository.findById(libCode)
                .map(Library::getName)
                .orElse(libCode);

        return InventoryResponseDto.of(
                libCode,
                libName,
                isbn,
                "Y".equals(result.getHasBook()),
                "Y".equals(result.getLoanAvail())
        );
    }

    // ── 내부 유틸 ──────────────────────────────────────────────────

    /** null이 아니고 아직 목록에 없는 경우에만 추가한다. */
    private void addIfAbsent(List<String> list, String value) {
        if (value != null && !value.isBlank() && !list.contains(value)) {
            list.add(value);
        }
    }

    // ── 정보나루 API 응답 매핑용 내부 DTO ─────────────────────────

    @lombok.Getter
    @lombok.NoArgsConstructor
    static class BookExistApiResponse {
        private Response response;

        @lombok.Getter
        @lombok.NoArgsConstructor
        static class Response {
            private Result result;
        }

        @lombok.Getter
        @lombok.NoArgsConstructor
        static class Result {
            private String hasBook;
            private String loanAvail;
        }
    }
}
