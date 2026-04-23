package com.example.demo.domain.library.repository;

import com.example.demo.domain.library.entity.BookHolding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookHoldingRepository extends JpaRepository<BookHolding, Long> {

    // 특정 도서의 소장 도서관 목록 조회 (도서 상세 페이지용)
    List<BookHolding> findByBook_BookId(String bookId);

    // 특정 도서관의 특정 도서 소장 여부 조회 (배치 중복 방지용)
    Optional<BookHolding> findByBook_BookIdAndLibrary_LibraryCode(
            String bookId, String libraryCode);
}
