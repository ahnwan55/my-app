package com.example.demo.domain.book.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book {

    // ISBN-13을 PK로 직접 사용 (auto increment 없음)
    @Id
    @Column(name = "book_id", length = 13)
    private String bookId;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "author", length = 200)
    private String author;

    @Column(name = "publisher", length = 100)
    private String publisher;

    @Column(name = "pub_year", length = 10)
    private String pubYear;

    // 한국십진분류법 코드 (검색 시 장르 필터링에 활용)
    @Column(name = "kdc", length = 20)
    private String kdc;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    // SRoBERTa 임베딩의 입력값 (도서 추천 핵심 필드)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // 정보나루 API 마지막 캐시 시각
    @Column(name = "cached_at")
    private LocalDateTime cachedAt;

    @Builder
    public Book(String bookId, String title, String author, String publisher,
                String pubYear, String kdc, String coverUrl, String description) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.pubYear = pubYear;
        this.kdc = kdc;
        this.coverUrl = coverUrl;
        this.description = description;
        this.cachedAt = LocalDateTime.now();
    }
    public void update(String title, String author, String publisher,
                       String pubYear, String kdc, String coverUrl,
                       String description) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.pubYear = pubYear;
        this.kdc = kdc;
        this.coverUrl = coverUrl;
        this.description = description;
        this.cachedAt = java.time.LocalDateTime.now();
    }
}
