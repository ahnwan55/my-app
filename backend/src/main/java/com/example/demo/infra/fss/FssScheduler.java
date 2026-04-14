package com.example.demo.infra.fss;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * FssScheduler — 금융감독원 상품 데이터 정기 수집 스케줄러
 *
 * @Profile("!test"): 테스트 환경에서는 스케줄러가 실행되지 않도록 합니다.
 *   테스트 실행 시 application-test.yml에 spring.profiles.active=test 설정이 있으면
 *   이 Bean은 등록되지 않습니다.
 *
 * [현재 상태] API 키 대기 중 → @Scheduled 주석 처리됨
 *   API 키 발급 후 아래 순서로 활성화:
 *   1. 각 메서드의 @Scheduled 주석 해제
 *   2. application.yml에 fss.api-key 추가
 *   3. main 클래스에 @EnableScheduling 추가 (없으면 스케줄러 동작 안 함)
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class FssScheduler {

    private final FssApiClient fssApiClient;

    /**
     * 매일 새벽 2시에 예금/적금 상품 데이터를 최신화합니다.
     *
     * cron 표현식: "0 0 2 * * *"
     *   초(0) 분(0) 시(2) 일(*) 월(*) 요일(*)
     *   = 매일 02:00:00
     *
     * 금융감독원 데이터는 매월 업데이트되지만,
     * 매일 체크해서 변경이 있으면 upsert하는 방식으로 구현합니다.
     *
     * TODO: API 키 발급 후 @Scheduled 주석 해제
     */
    // @Scheduled(cron = "0 0 2 * * *")
    public void collectFinancialProducts() {
        log.info("[FssScheduler] 금융 상품 데이터 수집 시작");
        try {
            fssApiClient.fetchAndSaveDeposits();
            fssApiClient.fetchAndSaveSavings();
            log.info("[FssScheduler] 금융 상품 데이터 수집 완료");
        } catch (Exception e) {
            // 수집 실패해도 서비스는 계속 운영되어야 하므로 예외를 삼킵니다.
            // 실제 운영에서는 Slack 알림 또는 메트릭 카운터 증가로 연동하세요.
            log.error("[FssScheduler] 금융 상품 데이터 수집 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 애플리케이션 시작 시 1회 즉시 실행 (최초 데이터 적재용)
     *
     * @PostConstruct 대신 @Scheduled(initialDelay)를 쓰는 이유:
     *   - @PostConstruct는 Bean 초기화 시점에 실행되어 트랜잭션이 없음
     *   - @Scheduled는 컨테이너가 완전히 뜬 후에 실행되어 안전함
     *   - initialDelay=10000: 앱 시작 10초 후 실행 (DB 연결 안정화 여유)
     *
     * TODO: API 키 발급 후 @Scheduled 주석 해제
     */
    // @Scheduled(initialDelay = 10_000, fixedDelay = Long.MAX_VALUE)
    public void initialLoad() {
        log.info("[FssScheduler] 초기 금융 상품 데이터 적재 시작");
        collectFinancialProducts();
    }
}