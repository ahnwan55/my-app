package com.example.demo.infra.library.batch;

import com.example.demo.domain.book.entity.Book;
import com.example.demo.domain.book.repository.BookRepository;
import com.example.demo.infra.library.LibraryApiClient;
import com.example.demo.infra.library.dto.LibraryApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * description 없는 books → 상세조회 API → description 업데이트
 *
 * BookDetailStep 전용 Writer
 * Reader는 BookRepository에서 description이 null인 Book을 직접 읽음
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookDetailItemWriter implements ItemWriter<Book> {

    private final LibraryApiClient libraryApiClient;
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public void write(Chunk<? extends Book> chunk) {
        int updated = 0, skipped = 0;

        for (Book book : chunk.getItems()) {
            try {
                LibraryApiResponse.BookDetail detail =
                        libraryApiClient.getBookDetail(book.getBookId());

                if (detail == null || detail.getDescription() == null
                        || detail.getDescription().isBlank()) {
                    log.debug("[BookDetailItemWriter] description 없음 스킵: {}",
                            book.getBookId());
                    skipped++;
                    continue;
                }

                book.update(
                        book.getTitle(),
                        book.getAuthor(),
                        book.getPublisher(),
                        book.getPubYear(),
                        book.getKdc(),
                        book.getCoverUrl(),
                        detail.getDescription()
                );
                updated++;

            } catch (Exception e) {
                log.warn("[BookDetailItemWriter] 상세 조회 실패 isbn={}: {}",
                        book.getBookId(), e.getMessage());
                skipped++;
            }
        }

        log.info("[BookDetailItemWriter] 청크 처리 완료 - 업데이트: {}건, 스킵: {}건",
                updated, skipped);
    }
}