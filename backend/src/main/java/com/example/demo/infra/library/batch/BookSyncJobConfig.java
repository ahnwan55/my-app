package com.example.demo.infra.library.batch;

import com.example.demo.domain.book.entity.Book;
import com.example.demo.domain.book.repository.BookRepository;
import com.example.demo.infra.library.dto.LibraryApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

/**
 * BookSyncJobConfig - 도서 데이터 수집 배치 Job 설정
 *
 * Job 3개로 분리:
 *   librarySyncJob       → 매월 1일 02:00 → libraries 적재
 *   monthlyPopularSyncJob → 매일 03:00    → books + monthly_popular_book 적재
 *   bookDetailSyncJob    → 매일 04:00     → books.description 업데이트
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class BookSyncJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final MonthlyPopularItemReader monthlyPopularItemReader;
    private final MonthlyPopularItemWriter monthlyPopularItemWriter;

    private final LibrarySyncItemReader librarySyncItemReader;
    private final LibrarySyncItemWriter librarySyncItemWriter;

    private final BookDetailItemWriter bookDetailItemWriter;
    private final BookRepository bookRepository;

    private static final int CHUNK_SIZE = 10;

    // ── Job 1: 도서관 목록 적재 (매월 1일 02:00) ──────────────────────────

    @Bean
    public Job librarySyncJob() {
        return new JobBuilder("librarySyncJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(librarySyncStep())
                .build();
    }

    @Bean
    public Step librarySyncStep() {
        return new StepBuilder("librarySyncStep", jobRepository)
                .<LibraryApiResponse.LibItem, LibraryApiResponse.LibItem>chunk(
                        CHUNK_SIZE, transactionManager)
                .reader(librarySyncItemReader)
                .writer(librarySyncItemWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(10)
                .build();
    }

    // ── Job 2: 이달의 인기대출 랭킹 적재 (매일 03:00) ─────────────────────

    @Bean
    public Job monthlyPopularSyncJob() {
        return new JobBuilder("monthlyPopularSyncJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(monthlyPopularStep())
                .build();
    }

    @Bean
    public Step monthlyPopularStep() {
        return new StepBuilder("monthlyPopularStep", jobRepository)
                .<LibraryApiResponse.BookItem, LibraryApiResponse.BookItem>chunk(
                        CHUNK_SIZE, transactionManager)
                .reader(monthlyPopularItemReader)
                .writer(monthlyPopularItemWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(10)
                .build();
    }

    // ── Job 3: 도서 상세정보 수집 (매일 04:00) ────────────────────────────

    @Bean
    public Job bookDetailSyncJob() {
        return new JobBuilder("bookDetailSyncJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(bookDetailStep())
                .build();
    }

    @Bean
    public Step bookDetailStep() {
        return new StepBuilder("bookDetailStep", jobRepository)
                .<Book, Book>chunk(CHUNK_SIZE, transactionManager)
                .reader(bookDetailItemReader())
                .writer(bookDetailItemWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(20)
                .build();
    }

    @Bean
    public RepositoryItemReader<Book> bookDetailItemReader() {
        return new RepositoryItemReaderBuilder<Book>()
                .name("bookDetailItemReader")
                .repository(bookRepository)
                .methodName("findByDescriptionIsNull")
                .pageSize(CHUNK_SIZE)
                .sorts(Map.of("bookId", Sort.Direction.ASC))
                .build();
    }
}