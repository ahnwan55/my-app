package com.example.demo.domain.book.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_vectors")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookVector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 도서 1개당 벡터 1개 (1:1 관계)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false, unique = true)
    private Book book;

    // SRoBERTa로 임베딩한 도서 설명 벡터 (JSON 배열 문자열)
    // 사용자 벡터(user_vector)와 코사인 유사도 비교 대상
    @Column(name = "book_vector", columnDefinition = "TEXT")
    private String bookVector;

    // 마지막 임베딩 시각 (재임베딩 필요 여부 판별용)
    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    @Builder
    public BookVector(Book book, String bookVector) {
        this.book = book;
        this.bookVector = bookVector;
        this.syncedAt = LocalDateTime.now();
    }

    // 도서 설명 변경 시 벡터 재생성에 사용
    public void updateVector(String bookVector) {
        this.bookVector = bookVector;
        this.syncedAt = LocalDateTime.now();
    }
}
