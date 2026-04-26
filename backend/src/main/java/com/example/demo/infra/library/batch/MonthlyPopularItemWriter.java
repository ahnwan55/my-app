package com.example.demo.infra.library.batch;

import com.example.demo.domain.book.entity.Book;
import com.example.demo.domain.book.entity.MonthlyPopularBook;
import com.example.demo.domain.book.repository.BookRepository;
import com.example.demo.domain.book.repository.MonthlyPopularBookRepository;
import com.example.demo.infra.library.dto.LibraryApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 인기대출도서 → books + monthly_popular_book upsert
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyPopularItemWriter implements ItemWriter<LibraryApiResponse.BookItem> {

    private final BookRepository bookRepository;
    private final MonthlyPopularBookRepository monthlyPopularBookRepository;

    private static final DateTimeFormatter MONTH_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM");

    private int rankingCounter = 1;

    @Override
    @Transactional
    public void write(Chunk<? extends LibraryApiResponse.BookItem> chunk) {
        String yearMonth = LocalDate.now().format(MONTH_FMT);

        // 첫 청크 시작 시 기존 이달 랭킹 삭제
        if (rankingCounter == 1) {
            monthlyPopularBookRepository.deleteByYearMonth(yearMonth);
            log.info("[MonthlyPopularItemWriter] 기존 {}월 랭킹 삭제", yearMonth);
        }

        for (LibraryApiResponse.BookItem item : chunk.getItems()) {

            // isbn13 없으면 스킵
            if (item.getIsbn13() == null || item.getIsbn13().isBlank()) {
                log.warn("[MonthlyPopularItemWriter] isbn13 없음 스킵: {}", item.getBookname());
                continue;
            }

            // books upsert
            Book book = bookRepository.findById(item.getIsbn13())
                    .orElseGet(() -> bookRepository.save(
                            Book.builder()
                                    .bookId(item.getIsbn13().trim())
                                    .title(item.getBookname())
                                    .author(item.getAuthors())
                                    .publisher(item.getPublisher())
                                    .pubYear(item.getPublicationYear())
                                    .kdc(item.getClassNo())
                                    .coverUrl(item.getBookImageURL())
                                    .description(null)
                                    .build()
                    ));

            // monthly_popular_book 저장
            monthlyPopularBookRepository.save(
                    MonthlyPopularBook.builder()
                            .yearMonth(yearMonth)
                            .book(book)
                            .ranking(rankingCounter++)
                            .loanCount(parseLoanCount(item.getLoanCount()))
                            .build()
            );
        }

        log.info("[MonthlyPopularItemWriter] {}월 {}건 적재 완료", yearMonth, rankingCounter - 1);
    }

    public void resetCounter() {
        this.rankingCounter = 1;
    }

    private Integer parseLoanCount(String loanCount) {
        if (loanCount == null || loanCount.isBlank()) return null;
        try {
            return Integer.parseInt(loanCount.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}