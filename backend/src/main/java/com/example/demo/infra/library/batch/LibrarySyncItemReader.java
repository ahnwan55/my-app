package com.example.demo.infra.library.batch;

import com.example.demo.infra.library.LibraryApiClient;
import com.example.demo.infra.library.dto.LibraryApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 정보공개 도서관 목록 읽기 (서울 전체 페이징)
 * libraries 테이블 적재에 사용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LibrarySyncItemReader implements ItemReader<LibraryApiResponse.LibItem> {

    private final LibraryApiClient libraryApiClient;

    private List<LibraryApiResponse.LibItem> buffer;
    private int cursor = 0;

    private static final int PAGE_SIZE = 100;

    @Override
    public LibraryApiResponse.LibItem read() {
        if (buffer == null) {
            buffer = fetchAll();
            cursor = 0;
            log.info("[LibrarySyncItemReader] 총 {}개 도서관 로드 완료", buffer.size());
        }
        if (cursor < buffer.size()) {
            return buffer.get(cursor++);
        }
        return null;
    }

    /**
     * 페이징으로 서울 전체 도서관 목록 수집
     */
    private List<LibraryApiResponse.LibItem> fetchAll() {
        List<LibraryApiResponse.LibItem> result = new ArrayList<>();
        int pageNo = 1;

        while (true) {
            LibraryApiResponse response =
                    libraryApiClient.getLibraries(pageNo, PAGE_SIZE);

            if (response == null
                    || response.getLibs() == null
                    || response.getLibs().isEmpty()) {
                break;
            }

            result.addAll(response.getLibs());
            log.info("[LibrarySyncItemReader] pageNo={} {}건 수집", pageNo, response.getLibs().size());

            // 마지막 페이지 확인
            if (response.getLibs().size() < PAGE_SIZE) {
                break;
            }

            pageNo++;
        }

        return result;
    }

    public void reset() {
        this.buffer = null;
        this.cursor = 0;
    }
}