package com.example.demo.domain.book.dto;

import com.example.demo.domain.book.entity.MonthlyPopularBook;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * BookRankingDto - 이달의 도서 랭킹 API 응답 DTO
 *
 * 변경 사항:
 *   - BookRanking 엔티티 제거 → MonthlyPopularBook 기반으로 교체
 *   - ageGroupLabel, genderLabel 제거 (이달 Top10은 조건 없이 전체 기준)
 *   - RankingItem.of() → MonthlyPopularBook 엔티티에서 변환
 */
public class BookRankingDto {

    /**
     * 이달 랭킹 목록 응답 DTO
     * GET /api/books/ranking 응답에 사용한다.
     */
    @Getter
    @Builder
    public static class RankingResponse {
        private String yearMonth;          // 집계 기준 연월 (예: "2026-04")
        private List<RankingItem> items;   // 랭킹 도서 목록
    }

    /**
     * 개별 랭킹 도서 항목 DTO
     */
    @Getter
    @Builder
    public static class RankingItem {
        private Integer ranking;
        private String bookId;             // ISBN-13
        private String title;
        private String author;
        private String publisher;
        private String pubYear;
        private String coverUrl;
        private String kdc;
        private Integer loanCount;

        /**
         * MonthlyPopularBook 엔티티 → RankingItem DTO 변환
         */
        public static RankingItem of(MonthlyPopularBook popularBook) {
            return RankingItem.builder()
                    .ranking(popularBook.getRanking())
                    .bookId(popularBook.getBook().getBookId())
                    .title(popularBook.getBook().getTitle())
                    .author(popularBook.getBook().getAuthor())
                    .publisher(popularBook.getBook().getPublisher())
                    .pubYear(popularBook.getBook().getPubYear())
                    .coverUrl(popularBook.getBook().getCoverUrl())
                    .kdc(popularBook.getBook().getKdc())
                    .loanCount(popularBook.getLoanCount())
                    .build();
        }
    }
}
