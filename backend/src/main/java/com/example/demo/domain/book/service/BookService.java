package com.example.demo.domain.book.service;

import com.example.demo.domain.book.dto.BookDto;
import com.example.demo.domain.book.entity.Book;
import com.example.demo.domain.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * BookService - 도서 비즈니스 로직 서비스
 *
 * 변경 사항:
 *   - BookGenre 파라미터 제거 → kdc(String) 파라미터로 교체
 *   - BookRepository 조회 메서드도 kdc 기반으로 교체
 *
 * @Transactional(readOnly = true):
 *   - 조회 전용 트랜잭션으로 성능 최적화
 *   - Hibernate dirty checking(변경 감지) 비활성화
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    /**
     * 도서 목록 조회
     *
     * @param kdc null이면 전체 조회, 값이 있으면 해당 KDC 코드로 필터링
     *            예: "813" (한국소설), "840" (영미소설), "320" (경제학)
     */
    public List<BookDto.BookResponse> getBooks(String kdc) {
        List<Book> books;

        if (kdc == null || kdc.isBlank()) {
            // KDC 필터 없음 → 전체 조회
            books = bookRepository.findAll();
        } else {
            // KDC 코드 앞자리 매칭으로 장르 필터링
            // 예: kdc="813" → kdc LIKE '813%' 으로 조회
            books = bookRepository.findByKdcStartingWith(kdc);
        }

        return books.stream()
                .map(BookDto.BookResponse::of)
                .toList();
    }

    /**
     * 도서 단건 조회
     *
     * @param id 도서 PK
     * @throws IllegalArgumentException 도서가 없을 때
     */
    public BookDto.BookResponse getBook(String bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "도서를 찾을 수 없습니다: " + bookId));
        return BookDto.BookResponse.of(book);
    }
}