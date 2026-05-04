package com.example.demo.domain.book.dto;

import com.example.demo.domain.book.entity.MonthlyPopularBook;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

public class BookRankingDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankingResponse {
        private String yearMonth;
        private List<RankingItem> items;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankingItem {
        private Integer ranking;
        private String bookId;
        private String title;
        private String author;
        private String publisher;
        private String pubYear;
        private String coverUrl;
        private String kdc;
        private Integer loanCount;

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