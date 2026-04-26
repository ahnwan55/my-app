package com.example.demo.domain.book.service;

import com.example.demo.domain.book.dto.BookRankingDto;
import com.example.demo.domain.book.entity.MonthlyPopularBook;
import com.example.demo.domain.book.repository.MonthlyPopularBookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * BookRankingService - 이달의 도서 랭킹 서비스
 *
 * 변경 사항:
 *   - BookRanking, BookRankingRepository 제거
 *     → MonthlyPopularBook, MonthlyPopularBookRepository 기반으로 교체
 *   - 연령대/성별 조건 제거 (이달 Top10 단순 조회)
 *   - LibraryApiClient 직접 호출 제거
 *     → 랭킹 데이터는 LibraryScheduler가 매월 1일 적재하므로
 *        서비스는 DB 조회만 담당한다
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookRankingService {

    private final MonthlyPopularBookRepository monthlyPopularBookRepository;

    private static final DateTimeFormatter MONTH_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * 이달의 인기대출도서 Top10 반환
     *
     * monthly_popular_book 테이블에서 당월 데이터를 순위 순으로 조회한다.
     * LibraryScheduler가 매월 1일 적재하므로 서비스는 DB 조회만 수행한다.
     *
     * @return 이달 랭킹 응답 DTO
     */
    public BookRankingDto.RankingResponse getRanking() {
        String yearMonth = LocalDate.now().format(MONTH_FMT);

        List<MonthlyPopularBook> rankings =
                monthlyPopularBookRepository.findByYearMonthOrderByRankingAsc(yearMonth);

        if (rankings.isEmpty()) {
            log.warn("[BookRankingService] 이달 랭킹 데이터 없음 - {}", yearMonth);
        }

        return BookRankingDto.RankingResponse.builder()
                .yearMonth(yearMonth)
                .items(rankings.stream()
                        .map(BookRankingDto.RankingItem::of)
                        .toList())
                .build();
    }
}
