package com.example.demo.domain.library.entity;

import com.example.demo.domain.book.entity.Book;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_holdings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소장 도서
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    // 소장 도서관
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_code", nullable = false)
    private Library library;

    // 현재 대출 가능 여부 (정보나루 bookExist API 기준)
    @Column(name = "available", nullable = false)
    private Boolean available;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public BookHolding(Book book, Library library, Boolean available) {
        this.book = book;
        this.library = library;
        this.available = available;
        this.updatedAt = LocalDateTime.now();
    }

    // 배치 동기화 시 대출 가능 여부 갱신
    public void updateAvailability(Boolean available) {
        this.available = available;
        this.updatedAt = LocalDateTime.now();
    }
}
