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
 *   - 당월 데이터 없을 시 전월 데이터 폴백 로직 추가
 *     (매월 1일 배치 실행 전 또는 API 한도 초과 시에도 서비스 정상 동작)
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
     * 당월 데이터가 없으면 전월 데이터로 폴백한다.
     * 전월 데이터도 없으면 빈 리스트를 반환한다.
     *
     * @return 랭킹 응답 DTO (yearMonth는 실제 데이터 기준 연월)
     */
    public BookRankingDto.RankingResponse getRanking() {
        String thisMonth = LocalDate.now().format(MONTH_FMT);

        // 당월 데이터 조회
        List<MonthlyPopularBook> rankings =
                monthlyPopularBookRepository.findByYearMonthOrderByRankingAsc(thisMonth);

        // 당월 데이터가 없으면 전월 데이터로 폴백
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

            // 전월 데이터로 응답 (yearMonth는 전월 기준으로 표시)
            log.info("[BookRankingService] 전월({}) 랭킹 데이터 {} 건으로 폴백", lastMonth, rankings.size());
            return BookRankingDto.RankingResponse.builder()
                    .yearMonth(lastMonth)
                    .items(rankings.stream()
                            .map(BookRankingDto.RankingItem::of)
                            .toList())
                    .build();
        }

        return BookRankingDto.RankingResponse.builder()
                .yearMonth(thisMonth)
                .items(rankings.stream()
                        .map(BookRankingDto.RankingItem::of)
                        .toList())
                .build();
    }
}