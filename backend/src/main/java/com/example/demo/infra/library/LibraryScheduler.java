package com.example.demo.infra.library;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * LibraryScheduler - 배치 Job 트리거 스케줄러
 *
 * 스케줄:
 *   - 도서관 목록    : 매월 1일 새벽 2시 (한 달에 한 번)
 *   - 이달의 랭킹    : 매일 새벽 3시 (하루에 한 번)
 *   - 도서 상세정보  : 매일 새벽 4시 (하루에 한 번)
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class LibraryScheduler {

    private final JobLauncher jobLauncher;
    private final Job librarySyncJob;
    private final Job monthlyPopularSyncJob;
    private final Job bookDetailSyncJob;

    /**
     * 도서관 목록 갱신 - 매월 1일 새벽 2시
     */
    @Scheduled(cron = "0 0 2 1 * *")
    public void syncLibrary() {
        runJob(librarySyncJob, "librarySyncJob");
    }

    /**
     * 이달의 인기대출 랭킹 갱신 - 매일 새벽 3시
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void syncMonthlyPopular() {
        runJob(monthlyPopularSyncJob, "monthlyPopularSyncJob");
    }

    /**
     * 도서 상세정보(description) 갱신 - 매일 새벽 4시
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void syncBookDetail() {
        runJob(bookDetailSyncJob, "bookDetailSyncJob");
    }

    /**
     * 앱 시작 후 15초 뒤 최초 1회 전체 실행
     */
    @Scheduled(initialDelay = 15_000, fixedDelay = Long.MAX_VALUE)
    public void initialSync() {
        runJob(librarySyncJob, "librarySyncJob-initial");
        runJob(monthlyPopularSyncJob, "monthlyPopularSyncJob-initial");
        runJob(bookDetailSyncJob, "bookDetailSyncJob-initial");
    }

    private void runJob(Job job, String jobName) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            log.info("[LibraryScheduler] {} 실행 시작", jobName);
            jobLauncher.run(job, params);
            log.info("[LibraryScheduler] {} 실행 완료", jobName);

        } catch (Exception e) {
            log.error("[LibraryScheduler] {} 실행 실패: {}", jobName, e.getMessage(), e);
        }
    }
}