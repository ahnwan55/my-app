package com.example.demo.domain.book.controller;

import com.example.demo.domain.book.dto.BookDto;
import com.example.demo.domain.book.entity.BookGenre;
import com.example.demo.domain.book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * BookController - 도서 API 컨트롤러
 *
 * GET /api/books          → 전체 도서 목록
 * GET /api/books?genre=NOVEL → 장르별 도서 목록
 * GET /api/books/{id}     → 도서 단건 조회
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * 활성 도서 목록 조회
     *
     * @RequestParam(required = false): 장르 파라미터가 없으면 null → 전체 조회
     */
    @GetMapping
    public ResponseEntity<List<BookDto.BookResponse>> getBooks(
            @RequestParam(required = false) BookGenre genre) {
        return ResponseEntity.ok(bookService.getBooks(genre));
    }

    /**
     * 도서 단건 조회
     *
     * GET /api/books/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookDto.BookResponse> getBook(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBook(id));
    }
}