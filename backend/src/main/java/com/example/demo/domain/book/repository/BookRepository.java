package com.example.demo.domain.book.repository;

import com.example.demo.domain.book.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, String> {

    // KDC 코드 앞자리로 장르 필터링
    List<Book> findByKdcStartingWith(String kdc);

    // ISBN-13으로 도서 조회 (정보나루 배치 수집 시 중복 방지용)
    boolean existsByBookId(String bookId);

    // description 없는 도서 조회 (bookDetailStep Reader용)
    Page<Book> findByDescriptionIsNull(Pageable pageable);

    /**
     * 키워드로 도서 검색
     * 제목 또는 저자에 keyword가 포함된 도서를 조회한다.
     * Spring Data JPA가 메서드명을 파싱하여 아래 JPQL을 자동 생성한다.
     * → SELECT b FROM Book b
     *   WHERE b.title LIKE %keyword%
     *   OR b.author LIKE %keyword%
     *
     * ContainingIgnoreCase: 대소문자 구분 없이 부분 일치 검색
     */
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
            String title, String author);
}