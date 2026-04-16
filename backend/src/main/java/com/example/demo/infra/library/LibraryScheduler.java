package com.example.demo.infra.library;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * LibraryScheduler - 도서관 정보나루 데이터 주기적 갱신 스케줄러
 *
 * 도서관 정보나루 API 데이터는 실시간이 아니므로
 * 주기적으로 캐싱해두고 추천에 활용합니다.
 *
 * @Profile("!test"): 테스트 환경에서는 스케줄러가 실행되지 않습니다.
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class LibraryScheduler {

    private final LibraryApiClient libraryApiClient;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 매일 새벽 3시에 인기대출 / 급상승 도서 데이터를 갱신합니다.
     *
     * cron: "0 0 3 * * *"
     *   초(0) 분(0) 시(3) 일(*) 월(*) 요일(*)
     *   = 매일 03:00:00
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void refreshDailyBooks() {
        log.info("[LibraryScheduler] 도서 데이터 갱신 시작");
        try {
            String endDt = LocalDate.now().format(DATE_FMT);
            String startDt = LocalDate.now().minusDays(7).format(DATE_FMT);

            libraryApiClient.getLoanItems(startDt, endDt);
            libraryApiClient.getRisingBooks(startDt, endDt);

            log.info("[LibraryScheduler] 도서 데이터 갱신 완료");
        } catch (Exception e) {
            log.error("[LibraryScheduler] 도서 데이터 갱신 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 앱 시작 후 15초 뒤 최초 1회 실행
     * DB 연결 안정화 후 실행되도록 initialDelay 적용
     */
    @Scheduled(initialDelay = 15_000, fixedDelay = Long.MAX_VALUE)
    public void initialLoad() {
        log.info("[LibraryScheduler] 초기 도서 데이터 로드 시작");
        refreshDailyBooks();
    }
}