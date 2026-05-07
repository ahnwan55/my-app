package com.example.demo.infra.library.batch;

import com.example.demo.infra.library.LibraryApiClient;
import com.example.demo.infra.library.dto.LibraryApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

    private List<LibraryApiResponse.LibItem> fetchAll() {
        List<LibraryApiResponse.LibItem> result = new ArrayList<>();
        int pageNo = 1;

        while (true) {
            LibraryApiResponse response =
                    libraryApiClient.getLibraries(pageNo, PAGE_SIZE);

            if (response == null
                    || response.getLib() == null
                    || response.getLib().isEmpty()) {
                break;
            }

            result.addAll(response.getLib());
            log.info("[LibrarySyncItemReader] pageNo={} {}건 수집", pageNo, response.getLib().size());

            if (response.getLib().size() < PAGE_SIZE) {
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