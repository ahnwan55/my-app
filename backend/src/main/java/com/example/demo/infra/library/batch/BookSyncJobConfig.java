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
 * Job 구조:
 *   bookSyncJob
 *     ├── Step1: monthlyPopularStep  → 인기대출도서 → books + monthly_popular_book
 *     ├── Step2: bookDetailStep      → description 없는 books → 상세조회 → 업데이트
 *     ├── Step3: librarySyncStep     → 도서관 목록 → libraries
 *     └── Step4: bookHoldingSyncStep → 장서/대출 → book_holdings
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

    private final BookHoldingSyncItemReader bookHoldingSyncItemReader;
    private final BookHoldingSyncItemWriter bookHoldingSyncItemWriter;

    private final BookDetailItemWriter bookDetailItemWriter;

    private final BookRepository bookRepository;

    private static final int CHUNK_SIZE = 10;

    // ── Job ───────────────────────────────────────────────────────────────

    @Bean
    public Job bookSyncJob() {
        return new JobBuilder("bookSyncJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(monthlyPopularStep())
                .next(bookDetailStep())
                .next(librarySyncStep())
                .next(bookHoldingSyncStep())
                .build();
    }

    // ── Step 1: 인기대출도서 적재 ──────────────────────────────────────────

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

    // ── Step 2: 도서 상세(description) 수집 ───────────────────────────────

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

    /**
     * description이 null인 Book만 읽어오는 Reader
     * RepositoryItemReader: Spring Data의 Pageable 기반으로 청크 단위 조회
     */
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

    // ── Step 3: 도서관 목록 적재 ───────────────────────────────────────────

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

    // ── Step 4: 장서/대출 현황 적재 ───────────────────────────────────────

    @Bean
    public Step bookHoldingSyncStep() {
        return new StepBuilder("bookHoldingSyncStep", jobRepository)
                .<LibraryApiResponse.HoldingItem, LibraryApiResponse.HoldingItem>chunk(
                        CHUNK_SIZE, transactionManager)
                .reader(bookHoldingSyncItemReader)
                .writer(bookHoldingSyncItemWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(50)
                .build();
    }
}