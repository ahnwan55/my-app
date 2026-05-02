package com.example.demo.domain.inventory.service;

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

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final RestClient restClient;
    private final UserRepository userRepository;
    private final LibraryRepository libraryRepository;

    @Value("${LIBRARY_API_KEY}")
    private String apiKey;

    private static final String BOOK_EXIST_URL = "http://data4library.kr/api/bookExist";

    public List<InventoryResponseDto> getInventory(
            String isbn,
            List<String> libCodes,
            Long userId
    ) {
        List<String> targetLibCodes = new ArrayList<>(libCodes);

        userRepository.findById(userId).ifPresent(user -> {
            addIfAbsent(targetLibCodes, user.getMainLibraryCode());
            addIfAbsent(targetLibCodes, user.getSubLibraryCode());
        });

        if (targetLibCodes.isEmpty()) {
            log.warn("[InventoryService] 조회 대상 도서관 코드 없음. isbn={}, userId={}", isbn, userId);
            return List.of();
        }

        List<InventoryResponseDto> results = new ArrayList<>();
        for (String libCode : targetLibCodes) {
            try {
                results.add(callBookExistApi(isbn, libCode));
            } catch (Exception e) {
                log.error("[InventoryService] bookExist API 실패. libCode={}, isbn={}, error={}",
                        libCode, isbn, e.getMessage());
                results.add(InventoryResponseDto.error(libCode, isbn));
            }
        }
        return results;
    }

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

    private void addIfAbsent(List<String> list, String value) {
        if (value != null && !value.isBlank() && !list.contains(value)) {
            list.add(value);
        }
    }

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
