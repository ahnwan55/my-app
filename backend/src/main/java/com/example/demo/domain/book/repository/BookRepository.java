package com.example.demo.domain.book.repository;

import com.example.demo.domain.book.entity.Book;
import com.example.demo.domain.book.entity.BookGenre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * BookRepository - 도서 DB 조회 인터페이스
 *
 * Spring Data JPA가 인터페이스 선언만으로 구현체를 자동 생성합니다.
 * 메서드 이름 규칙으로 쿼리를 자동 생성합니다.
 */
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * ISBN으로 도서 단건 조회
     * 도서관 정보나루 API 데이터 upsert 시 중복 체크에 사용
     */
    Optional<Book> findByIsbn13(String isbn13);

    /**
     * 활성 도서 전체 조회
     */
    List<Book> findByIsActiveTrue();

    /**
     * 장르별 활성 도서 조회
     * GET /api/books?genre=NOVEL 처리에 사용
     */
    List<Book> findByGenreAndIsActiveTrue(BookGenre genre);
}