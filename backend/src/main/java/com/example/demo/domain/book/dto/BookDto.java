package com.example.demo.domain.book.dto;

import com.example.demo.domain.book.entity.Book;
import lombok.Builder;
import lombok.Getter;

/**
 * BookDto - 도서 API 요청/응답 DTO 모음
 *
 * 변경 사항:
 *   - BookGenre 제거 → kdc(String) 필드로 교체
 *   - Book 엔티티 필드명과 일치하도록 전면 수정
 *     (isbn13 → bookId, authors → author, publicationYear → pubYear,
 *      bookImageUrl → coverUrl, genre → kdc, classNo 제거)
 *   - BookListRequest 제거 (컨트롤러에서 @RequestParam String kdc로 직접 처리)
 *
 * Entity를 직접 반환하지 않고 DTO로 변환하는 이유:
 *   - DB 구조가 외부에 노출되지 않도록 보호
 *   - API 스펙을 자유롭게 설계 가능
 *   - cachedAt 등 내부 관리 필드를 응답에서 제외
 */
public class BookDto {

    /**
     * 도서 목록/단건 응답 DTO
     * GET /api/books, GET /api/books/{id} 응답에 사용
     */
    @Getter
    @Builder
    public static class BookResponse {
        private String bookId;        // ISBN-13 (PK)
        private String title;
        private String author;
        private String publisher;
        private String pubYear;
        private String coverUrl;
        private String description;
        private String kdc;           // 한국십진분류법 코드 (장르 식별용)

        /**
         * Book Entity → BookResponse DTO 변환
         * 정적 팩토리 메서드 패턴: 변환 로직을 DTO 안에 캡슐화
         */
        public static BookResponse of(Book book) {
            return BookResponse.builder()
                    .bookId(book.getBookId())
                    .title(book.getTitle())
                    .author(book.getAuthor())
                    .publisher(book.getPublisher())
                    .pubYear(book.getPubYear())
                    .coverUrl(book.getCoverUrl())
                    .description(book.getDescription())
                    .kdc(book.getKdc())
                    .build();
        }
    }
}
