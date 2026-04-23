package com.example.demo.infra.library.batch;

import com.example.demo.domain.book.entity.Book;
import com.example.demo.domain.book.repository.BookRepository;
import com.example.demo.domain.library.entity.BookHolding;
import com.example.demo.domain.library.entity.Library;
import com.example.demo.domain.library.repository.BookHoldingRepository;
import com.example.demo.domain.library.repository.LibraryRepository;
import com.example.demo.infra.library.dto.LibraryApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookHoldingSyncItemWriter implements ItemWriter<LibraryApiResponse.HoldingItem> {

    private final BookRepository bookRepository;
    private final LibraryRepository libraryRepository;
    private final BookHoldingRepository bookHoldingRepository;

    @Override
    @Transactional
    public void write(Chunk<? extends LibraryApiResponse.HoldingItem> chunk) {
        int inserted = 0, updated = 0;

        for (LibraryApiResponse.HoldingItem item : chunk.getItems()) {

            if (item.getIsbn13() == null || item.getIsbn13().isBlank()
                    || item.getLibCode() == null || item.getLibCode().isBlank()) {
                log.warn("[BookHoldingSyncItemWriter] 필수 필드 없음 스킵: isbn={} libCode={}",
                        item.getIsbn13(), item.getLibCode());
                continue;
            }

            Optional<Book> bookOpt = bookRepository.findById(item.getIsbn13().trim());
            if (bookOpt.isEmpty()) {
                log.debug("[BookHoldingSyncItemWriter] books 테이블에 없는 도서 스킵: {}",
                        item.getIsbn13());
                continue;
            }

            Optional<Library> libraryOpt =
                    libraryRepository.findById(item.getLibCode().trim());
            if (libraryOpt.isEmpty()) {
                log.debug("[BookHoldingSyncItemWriter] libraries 테이블에 없는 도서관 스킵: {}",
                        item.getLibCode());
                continue;
            }

            Book book = bookOpt.get();
            Library library = libraryOpt.get();
            boolean available = "Y".equalsIgnoreCase(item.getLoanAvailable());

            // 기존 메서드명에 맞게 수정
            Optional<BookHolding> existing =
                    bookHoldingRepository.findByBook_BookIdAndLibrary_LibraryCode(
                            book.getBookId(), library.getLibraryCode());

            if (existing.isPresent()) {
                existing.get().updateAvailability(available);
                updated++;
            } else {
                bookHoldingRepository.save(
                        BookHolding.builder()
                                .book(book)
                                .library(library)
                                .available(available)
                                .build()
                );
                inserted++;
            }
        }

        log.info("[BookHoldingSyncItemWriter] 청크 처리 완료 - 신규: {}건, 업데이트: {}건",
                inserted, updated);
    }
}