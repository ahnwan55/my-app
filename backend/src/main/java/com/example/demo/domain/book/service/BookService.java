package com.example.demo.domain.book.service;

import com.example.demo.domain.book.dto.BookDto;
import com.example.demo.domain.book.entity.Book;
import com.example.demo.domain.book.entity.BookGenre;
import com.example.demo.domain.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * BookService - 도서 비즈니스 로직 서비스
 *
 * @Transactional(readOnly = true):
 *   - 조회 전용 트랜잭션으로 성능 최적화
 *   - Hibernate dirty checking(변경 감지) 비활성화
 *   - 읽기 전용 DB 연결 사용 가능
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    /**
     * 활성 도서 목록 조회
     *
     * @param genre null이면 전체, 값이 있으면 해당 장르만 반환
     */
    public List<BookDto.BookResponse> getBooks(BookGenre genre) {
        List<Book> books;

        if (genre == null) {
            // 장르 필터 없음 → 전체 조회
            books = bookRepository.findByIsActiveTrue();
        } else {
            // 장르 필터 적용
            books = bookRepository.findByGenreAndIsActiveTrue(genre);
        }

        // Entity → DTO 변환
        // stream().map().toList(): 각 Book을 BookResponse로 변환하는 Java Stream 파이프라인
        return books.stream()
                .map(BookDto.BookResponse::of)
                .toList();
    }

    /**
     * 도서 단건 조회
     *
     * @param id 도서 PK
     * @throws IllegalArgumentException 도서가 없거나 비활성 상태일 때
     */
    public BookDto.BookResponse getBook(Long id) {
        Book book = bookRepository.findById(id)
                .filter(Book::getIsActive)
                .orElseThrow(() -> new IllegalArgumentException(
                        "도서를 찾을 수 없습니다: " + id));

        return BookDto.BookResponse.of(book);
    }
}