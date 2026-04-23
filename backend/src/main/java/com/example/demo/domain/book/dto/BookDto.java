package com.example.demo.domain.book.dto;

import com.example.demo.domain.book.entity.Book;
import com.example.demo.domain.book.entity.BookGenre;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * BookDto - 도서 API 요청/응답 DTO 모음
 *
 * Entity를 직접 반환하지 않고 DTO로 변환하는 이유:
 *   - DB 구조가 외부에 노출되지 않도록 보호
 *   - API 스펙을 자유롭게 설계 가능
 *   - fetchedAt 등 내부 관리 필드를 응답에서 제외
 */
public class BookDto {

    /**
     * 도서 목록/단건 응답 DTO
     * GET /api/books, GET /api/books/{id} 응답에 사용
     */
    @Getter
    @Builder
    public static class BookResponse {
        private Long id;
        private String isbn13;
        private String title;
        private String authors;
        private String publisher;
        private String publicationYear;
        private String bookImageUrl;
        private String description;
        private BookGenre genre;
        private String classNo;

        /**
         * Book Entity → BookResponse DTO 변환
         * 정적 팩토리 메서드 패턴: 변환 로직을 DTO 안에 캡슐화
         */
        public static BookResponse of(Book book) {
            return BookResponse.builder()
                    .id(book.getId())
                    .isbn13(book.getIsbn13())
                    .title(book.getTitle())
                    .authors(book.getAuthors())
                    .publisher(book.getPublisher())
                    .publicationYear(book.getPublicationYear())
                    .bookImageUrl(book.getBookImageUrl())
                    .description(book.getDescription())
                    .genre(book.getGenre())
                    .classNo(book.getClassNo())
                    .build();
        }
    }

    /**
     * 도서 목록 조회 필터 요청 DTO
     * GET /api/books?genre=NOVEL 형태로 사용
     */
    @Getter
    public static class BookListRequest {
        private BookGenre genre;  // null이면 전체 조회
    }
}