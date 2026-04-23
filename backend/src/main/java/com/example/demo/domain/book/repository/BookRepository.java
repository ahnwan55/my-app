package com.example.demo.domain.book.repository;

import com.example.demo.domain.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, String> {

    // KDC 코드 앞자리로 장르 필터링
    List<Book> findByKdcStartingWith(String kdc);

    // ISBN-13으로 도서 조회 (정보나루 배치 수집 시 중복 방지용)
    boolean existsByBookId(String bookId);
}