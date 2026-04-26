package com.example.demo.domain.book.repository;

import com.example.demo.domain.book.entity.BookVector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookVectorRepository extends JpaRepository<BookVector, Long> {

    // ISBN-13으로 벡터 조회 (배치 수집 시 중복 방지용)
    Optional<BookVector> findByBook_BookId(String bookId);
}
