package com.example.demo.infra.library.batch;

import com.example.demo.domain.library.entity.Library;
import com.example.demo.domain.library.repository.LibraryRepository;
import com.example.demo.infra.library.LibraryApiClient;
import com.example.demo.infra.library.dto.LibraryApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 도서관별 장서/대출 데이터 읽기
 * book_holdings 테이블 적재에 사용
 *
 * 흐름:
 *   1. libraries 테이블에서 전체 도서관 코드 조회
 *   2. 도서관별로 itemSrch API 호출 (페이징)
 *   3. HoldingItem 단위로 하나씩 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookHoldingSyncItemReader implements ItemReader<LibraryApiResponse.HoldingItem> {

    private final LibraryApiClient libraryApiClient;
    private final LibraryRepository libraryRepository;

    private List<LibraryApiResponse.HoldingItem> buffer;
    private int cursor = 0;

    private static final int PAGE_SIZE = 100;

    @Override
    public LibraryApiResponse.HoldingItem read() {
        if (buffer == null) {
            buffer = fetchAll();
            cursor = 0;
            log.info("[BookHoldingSyncItemReader] 총 {}건 장서 데이터 로드 완료", buffer.size());
        }
        if (cursor < buffer.size()) {
            return buffer.get(cursor++);
        }
        return null;
    }

    /**
     * 전체 도서관 × 페이징으로 장서 데이터 수집
     */
    private List<LibraryApiResponse.HoldingItem> fetchAll() {
        List<LibraryApiResponse.HoldingItem> result = new ArrayList<>();
        List<Library> libraries = libraryRepository.findAll();

        log.info("[BookHoldingSyncItemReader] 대상 도서관 {}개", libraries.size());

        for (Library library : libraries) {
            int pageNo = 1;

            while (true) {
                LibraryApiResponse response = libraryApiClient.getBookHoldings(
                        library.getLibraryCode(), pageNo, PAGE_SIZE);

                if (response == null
                        || response.getDocs() == null
                        || response.getDocs().isEmpty()) {
                    break;
                }

                // BookItem → HoldingItem 변환
                for (LibraryApiResponse.BookItem item : response.getDocs()) {
                    LibraryApiResponse.HoldingItem holding =
                            new LibraryApiResponse.HoldingItem();
                    holding.setIsbn13(item.getIsbn13());
                    holding.setBookname(item.getBookname());
                    holding.setLibCode(library.getLibraryCode());
                    holding.setLoanAvailable("Y"); // itemSrch 기본값
                    result.add(holding);
                }

                log.info("[BookHoldingSyncItemReader] 도서관={} pageNo={} {}건 수집",
                        library.getLibraryCode(), pageNo, response.getDocs().size());

                if (response.getDocs().size() < PAGE_SIZE) break;
                pageNo++;
            }
        }

        return result;
    }

    public void reset() {
        this.buffer = null;
        this.cursor = 0;
    }
}