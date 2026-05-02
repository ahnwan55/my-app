package com.example.demo.domain.library.controller;

import com.example.demo.domain.library.entity.Library;
import com.example.demo.domain.library.repository.LibraryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * LibraryController — 도서관 검색 API
 *
 * [엔드포인트]
 *   GET /api/libraries?keyword={keyword}
 *
 * [용도]
 *   마이페이지에서 도서관 이름으로 검색 후 선택하여 등록하는 기능에 사용된다.
 *
 * [응답 예시]
 * [
 *   { "libraryCode": "111058", "name": "노원정보도서관", "address": "서울 노원구 ..." },
 *   { "libraryCode": "111099", "name": "노원구립중앙도서관", "address": "..." }
 * ]
 */
@Tag(name = "Library", description = "도서관 검색 API")
@RestController
@RequestMapping("/api/libraries")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryRepository libraryRepository;

    /**
     * 도서관 이름 키워드 검색
     *
     * GET /api/libraries?keyword=노원
     *
     * @param keyword  검색 키워드 (도서관 이름 일부)
     * @return         매칭된 도서관 목록 (최대 10개)
     */
    @Operation(summary = "도서관 검색", description = "도서관 이름으로 검색하여 목록을 반환한다.")
    @GetMapping
    public ResponseEntity<List<LibraryDto>> searchLibraries(
            @RequestParam String keyword
    ) {
        List<LibraryDto> result = libraryRepository
                .findByNameContainingIgnoreCase(keyword)
                .stream()
                .limit(10)   // 검색 결과가 많을 경우 10개로 제한한다.
                .map(LibraryDto::of)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * 도서관 응답 DTO
     * 외부에서 직접 사용할 일이 없어 컨트롤러 내부에 선언한다.
     */
    @lombok.Getter
    @lombok.Builder
    public static class LibraryDto {
        private String libraryCode;
        private String name;
        private String address;

        public static LibraryDto of(Library library) {
            return LibraryDto.builder()
                    .libraryCode(library.getLibraryCode())
                    .name(library.getName())
                    .address(library.getAddress())
                    .build();
        }
    }
}
