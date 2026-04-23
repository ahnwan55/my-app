package com.example.demo.domain.book.controller;

import com.example.demo.domain.book.dto.BookDto;
import com.example.demo.domain.book.dto.BookRankingDto;
import com.example.demo.domain.book.service.BookRankingService;
import com.example.demo.domain.book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * BookController - 도서 API 컨트롤러
 *
 * 변경 사항:
 *   - BookGenre 제거 → kdc(한국십진분류법 코드) 기반 장르 필터로 교체
 *     예: kdc=813 (한국소설), kdc=840 (영미소설), kdc=320 (경제학)
 *
 * 엔드포인트 목록:
 *   GET /api/books                → 전체/kdc별 도서 목록
 *   GET /api/books/{id}           → 도서 단건 조회
 *   GET /api/books/ranking        → 연령대/성별 인기 대출 랭킹
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final BookRankingService bookRankingService;

    /**
     * 도서 목록 조회
     * GET /api/books
     * GET /api/books?kdc=813   → KDC 코드로 장르 필터링
     */
    @GetMapping
    public ResponseEntity<List<BookDto.BookResponse>> getBooks(
            @RequestParam(required = false) String kdc) {
        return ResponseEntity.ok(bookService.getBooks(kdc));
    }

    /**
     * 도서 단건 조회
     * GET /api/books/{id}
     */
    @GetMapping("/{bookId}")
    public ResponseEntity<BookDto.BookResponse> getBook(@PathVariable String bookId) {
        return ResponseEntity.ok(bookService.getBook(bookId));
    }

    /**
     * 연령대/성별 인기 대출 도서 랭킹 조회
     *
     * ⚠️ /ranking이 /{id}보다 먼저 선언되어야 "ranking" 문자열이
     *    PathVariable로 해석되는 것을 방지한다.
     *
     * GET /api/books/ranking
     * GET /api/books/ranking?ageGroup=20
     * GET /api/books/ranking?gender=1
     * GET /api/books/ranking?ageGroup=20&gender=1
     *
     * ageGroup - 연령대 코드 (생략 시 전체)
     *            0: 영유아, 6: 초등, 14: 중등, 18: 고등
     *            20: 20대, 30: 30대, 40: 40대, 50: 50대, 60: 60대 이상
     * gender   - 성별 코드 (생략 시 전체) 1: 남성, 2: 여성
     */
    @GetMapping("/ranking")
    public ResponseEntity<BookRankingDto.RankingResponse> getRanking() {
        return ResponseEntity.ok(bookRankingService.getRanking());
    }
}
