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
 *   - keyword 파라미터 추가 → 제목/저자 키워드 검색 지원
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
     * 우선순위:
     *  1. keyword가 있으면 제목/저자 키워드 검색
     *  2. kdc가 있으면 KDC 코드 필터링
     *  3. 둘 다 없으면 전체 조회
     *
     * @param keyword 검색 키워드 (제목 또는 저자 부분 일치)
     * @param kdc     KDC 코드 앞자리 (예: "813", "840", "320")
     */
    public List<BookDto.BookResponse> getBooks(String keyword, String kdc) {
        List<Book> books;

        if (keyword != null && !keyword.isBlank()) {
            // 키워드 검색: 제목 또는 저자에 keyword가 포함된 도서 조회
            // 같은 keyword를 title, author 양쪽에 전달 (OR 조건)
            books = bookRepository
                    .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
                            keyword, keyword);
        } else if (kdc != null && !kdc.isBlank()) {
            // KDC 코드 앞자리 매칭으로 장르 필터링
            books = bookRepository.findByKdcStartingWith(kdc);
        } else {
            // 필터 없음 → 전체 조회
            books = bookRepository.findAll();
        }

        return books.stream()
                .map(BookDto.BookResponse::of)
                .toList();
    }

    /**
     * 도서 단건 조회
     *
     * @param bookId 도서 PK (ISBN-13)
     * @throws IllegalArgumentException 도서가 없을 때
     */
    public BookDto.BookResponse getBook(String bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "도서를 찾을 수 없습니다: " + bookId));
        return BookDto.BookResponse.of(book);
    }
}