package com.example.demo.domain.book.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "monthly_popular_book",
    // 같은 달 같은 순위 중복 삽입 방지 (DB 레벨 제약)
    uniqueConstraints = @UniqueConstraint(columnNames = {"year_month", "ranking"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlyPopularBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "popular_id")
    private Long popularId;

    // 집계 기준 연월 (예: "2026-04")
    @Column(name = "year_month", nullable = false, length = 7)
    private String yearMonth;

    // 정보나루 인기대출도서 API 기준 도서
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "ranking", nullable = false)
    private Integer ranking;

    @Column(name = "loan_count")
    private Integer loanCount;

    // 정보나루 API 수집 시각
    @Column(name = "fetched_at")
    private LocalDateTime fetchedAt;

    @Builder
    public MonthlyPopularBook(String yearMonth, Book book,
                               Integer ranking, Integer loanCount) {
        this.yearMonth = yearMonth;
        this.book = book;
        this.ranking = ranking;
        this.loanCount = loanCount;
        this.fetchedAt = LocalDateTime.now();
    }
}
