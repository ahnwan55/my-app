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
 * 기존 @Scheduled 직접 처리 방식 → JobLauncher로 배치 Job 실행으로 교체
 * 실행 이력은 JobRepository가 RDS에 자동 저장
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class LibraryScheduler {

    private final JobLauncher jobLauncher;
    private final Job bookSyncJob;

    /**
     * 매월 1일 새벽 3시 실행
     */
    @Scheduled(cron = "0 0 3 1 * *")
    public void scheduledSync() {
        runJob("scheduled");
    }

    /**
     * 앱 시작 후 15초 뒤 최초 1회 실행
     */
    @Scheduled(initialDelay = 15_000, fixedDelay = Long.MAX_VALUE)
    public void initialSync() {
        runJob("initial");
    }

    private void runJob(String trigger) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("trigger", trigger)
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            log.info("[LibraryScheduler] bookSyncJob 실행 시작 - trigger: {}", trigger);
            jobLauncher.run(bookSyncJob, params);
            log.info("[LibraryScheduler] bookSyncJob 실행 완료");

        } catch (Exception e) {
            log.error("[LibraryScheduler] bookSyncJob 실행 실패: {}", e.getMessage(), e);
        }
    }
}