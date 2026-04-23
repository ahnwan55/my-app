package com.example.demo.infra.library;

import com.example.demo.domain.book.entity.Book;
import com.example.demo.domain.book.entity.MonthlyPopularBook;
import com.example.demo.domain.book.repository.BookRepository;
import com.example.demo.domain.book.repository.MonthlyPopularBookRepository;
import com.example.demo.infra.library.dto.LibraryApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * LibraryScheduler - 정보나루 데이터 주기적 갱신 스케줄러
 *
 * 변경 사항:
 *   - BookRanking 제거 → MonthlyPopularBook + Book 기반으로 교체
 *   - refreshMonthlyPopular(): 매월 1일 새벽 3시 이달 Top10 적재
 *   - initialLoad(): prod 환경에서만 앱 시작 후 15초 뒤 최초 1회 실행
 *
 * 스케줄 흐름:
 *   ① 매월 1일 03:00 → refreshMonthlyPopular() 실행
 *      - 이달 인기대출도서 Top10 조회
 *      - books 테이블에 신규 도서 저장 (없는 것만)
 *      - monthly_popular_book 테이블에 랭킹 적재 (기존 데이터 교체)
 *
 * @Profile("!test"): 테스트 환경에서는 스케줄러 전체가 실행되지 않는다.
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class LibraryScheduler {

    private final LibraryApiClient libraryApiClient;
    private final BookRepository bookRepository;
    private final MonthlyPopularBookRepository monthlyPopularBookRepository;
    private final Environment environment;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM");
    private static final int TOP_N = 10;

    /**
     * 매월 1일 새벽 3시에 이달의 인기대출도서 Top10을 갱신한다.
     * cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 3 1 * *")
    @Transactional
    public void refreshMonthlyPopular() {
        log.info("[LibraryScheduler] 이달 인기대출도서 갱신 시작");
        try {
            LocalDate now = LocalDate.now();
            // 이달 1일부터 오늘까지 집계
            String startDt = now.withDayOfMonth(1).format(DATE_FMT);
            String endDt   = now.format(DATE_FMT);
            String yearMonth = now.format(MONTH_FMT);

            // 정보나루 API 호출
            List<LibraryApiResponse.BookItem> items =
                    libraryApiClient.getMonthlyPopular(startDt, endDt, TOP_N);

            if (items.isEmpty()) {
                log.warn("[LibraryScheduler] 인기대출도서 응답 없음");
                return;
            }

            // 기존 이달 랭킹 삭제 후 재적재 (교체 방식)
            monthlyPopularBookRepository.deleteByYearMonth(yearMonth);

            for (int i = 0; i < items.size(); i++) {
                LibraryApiResponse.BookItem item = items.get(i);

                // isbn13 없는 항목 건너뜀
                if (item.getIsbn13() == null || item.getIsbn13().isBlank()) {
                    log.warn("[LibraryScheduler] isbn13 없는 항목 건너뜀: {}", item.getBookname());
                    continue;
                }

                // books 테이블에 없는 도서면 저장
                Book book = bookRepository.findById(item.getIsbn13())
                        .orElseGet(() -> bookRepository.save(
                                Book.builder()
                                        .bookId(item.getIsbn13())
                                        .title(item.getBookname())
                                        .author(item.getAuthors())
                                        .publisher(item.getPublisher())
                                        .pubYear(item.getPublicationYear())
                                        .kdc(item.getClassNo())
                                        .coverUrl(item.getBookImageURL())
                                        .description(null) // description은 별도 배치에서 수집
                                        .build()
                        ));

                // 랭킹 저장
                monthlyPopularBookRepository.save(
                        MonthlyPopularBook.builder()
                                .yearMonth(yearMonth)
                                .book(book)
                                .ranking(i + 1)
                                .loanCount(parseLoanCount(item.getLoanCount()))
                                .build()
                );
            }

            log.info("[LibraryScheduler] 이달 인기대출도서 갱신 완료 - {}월 {}건",
                    yearMonth, items.size());

        } catch (Exception e) {
            log.error("[LibraryScheduler] 이달 인기대출도서 갱신 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 앱 시작 후 15초 뒤 최초 1회 실행.
     * prod 프로파일일 때만 실제로 동작한다.
     */
    @Scheduled(initialDelay = 15_000, fixedDelay = Long.MAX_VALUE)
    public void initialLoad() {
        if (!Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            log.info("[LibraryScheduler] prod 환경이 아니므로 초기 로드 건너뜀");
            return;
        }
        log.info("[LibraryScheduler] 초기 데이터 로드 시작");
        refreshMonthlyPopular();
    }

    // ── 유틸 ──────────────────────────────────────────────────────────────

    /**
     * 대출 횟수 문자열 → Integer 변환.
     * 변환 실패 시 null 반환.
     */
    private Integer parseLoanCount(String loanCount) {
        if (loanCount == null || loanCount.isBlank()) return null;
        try {
            return Integer.parseInt(loanCount.trim());
        } catch (NumberFormatException e) {
            log.warn("[LibraryScheduler] loanCount 변환 실패: {}", loanCount);
            return null;
        }
    }
}
