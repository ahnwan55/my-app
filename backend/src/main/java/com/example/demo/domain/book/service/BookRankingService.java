package com.example.demo.domain.book.service;

import com.example.demo.domain.book.dto.BookRankingDto;
import com.example.demo.domain.book.entity.MonthlyPopularBook;
import com.example.demo.domain.book.repository.MonthlyPopularBookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookRankingService {

    private final MonthlyPopularBookRepository monthlyPopularBookRepository;

    private static final DateTimeFormatter MONTH_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM");

    @Cacheable(value = "popularBooks", key = "'top10'")
    public BookRankingDto.RankingResponse getRanking() {
        String thisMonth = LocalDate.now().format(MONTH_FMT);

        List<MonthlyPopularBook> rankings =
                monthlyPopularBookRepository.findByYearMonthOrderByRankingAsc(thisMonth);

        if (rankings.isEmpty()) {
            log.warn("[BookRankingService] 이달 랭킹 데이터 없음 - {} → 전월 데이터로 폴백", thisMonth);

            String lastMonth = LocalDate.now().minusMonths(1).format(MONTH_FMT);
            rankings = monthlyPopularBookRepository.findByYearMonthOrderByRankingAsc(lastMonth);

            if (rankings.isEmpty()) {
                log.warn("[BookRankingService] 전월 랭킹 데이터도 없음 - {}", lastMonth);
                return BookRankingDto.RankingResponse.builder()
                        .yearMonth(thisMonth)
                        .items(List.of())
                        .build();
            }

            log.info("[BookRankingService] 전월({}) 랭킹 데이터 {} 건으로 폴백", lastMonth, rankings.size());
            return BookRankingDto.RankingResponse.builder()
                    .yearMonth(lastMonth)
                    .items(extractTop10Unique(rankings))
                    .build();
        }

        return BookRankingDto.RankingResponse.builder()
                .yearMonth(thisMonth)
                .items(extractTop10Unique(rankings))
                .build();
    }

    private List<BookRankingDto.RankingItem> extractTop10Unique(List<MonthlyPopularBook> rankings) {
        Set<String> seenIsbns = new HashSet<>();
        return rankings.stream()
                .filter(r -> seenIsbns.add(r.getBook().getBookId())) // 중복 ISBN 제외 (먼저 들어온 높은 순위만 남김)
                .limit(10) // 정확히 Top 10까지만 자름
                .map(BookRankingDto.RankingItem::of)
                .toList();
    }
}