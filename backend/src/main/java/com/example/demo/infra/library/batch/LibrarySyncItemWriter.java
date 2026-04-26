package com.example.demo.infra.library.batch;

import com.example.demo.domain.library.entity.Library;
import com.example.demo.domain.library.repository.LibraryRepository;
import com.example.demo.infra.library.dto.LibraryApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 도서관 정보 → libraries upsert
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LibrarySyncItemWriter implements ItemWriter<LibraryApiResponse.LibItem> {

    private final LibraryRepository libraryRepository;

    @Override
    @Transactional
    public void write(Chunk<? extends LibraryApiResponse.LibItem> chunk) {
        int inserted = 0, updated = 0;

        for (LibraryApiResponse.LibItem item : chunk.getItems()) {

            // libCode 없으면 스킵
            if (item.getLibCode() == null || item.getLibCode().isBlank()) {
                log.warn("[LibrarySyncItemWriter] libCode 없음 스킵: {}", item.getLibName());
                continue;
            }

            // upsert
            libraryRepository.findById(item.getLibCode())
                    .ifPresentOrElse(
                            existing -> {
                                existing.updateSyncedAt();
                            },
                            () -> libraryRepository.save(
                                    Library.builder()
                                            .libraryCode(item.getLibCode().trim())
                                            .name(item.getLibName())
                                            .address(item.getAddress())
                                            .tel(item.getTel())
                                            .build()
                            )
                    );

            if (libraryRepository.existsById(item.getLibCode())) {
                updated++;
            } else {
                inserted++;
            }
        }

        log.info("[LibrarySyncItemWriter] 청크 처리 완료 - 신규: {}건, 업데이트: {}건",
                inserted, updated);
    }
}