package com.example.demo.infra.library.batch;

import com.example.demo.infra.library.LibraryApiClient;
import com.example.demo.infra.library.dto.LibraryApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 이달의 인기대출도서 Top10 읽기
 * monthly_popular_book + books 적재에 사용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyPopularItemReader implements ItemReader<LibraryApiResponse.BookItem> {

    private final LibraryApiClient libraryApiClient;

    private List<LibraryApiResponse.BookItem> buffer;
    private int cursor = 0;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int TOP_N = 10;

    @Override
    public LibraryApiResponse.BookItem read() {
        if (buffer == null) {
            buffer = fetchItems();
            cursor = 0;
            log.info("[MonthlyPopularItemReader] {}건 로드 완료", buffer.size());
        }
        if (cursor < buffer.size()) {
            return buffer.get(cursor++);
        }
        return null;
    }

    private List<LibraryApiResponse.BookItem> fetchItems() {
        LocalDate now = LocalDate.now();
        String startDt = now.withDayOfMonth(1).format(DATE_FMT);
        String endDt = now.format(DATE_FMT);
        return libraryApiClient.getMonthlyPopular(startDt, endDt, TOP_N);
    }

    public void reset() {
        this.buffer = null;
        this.cursor = 0;
    }
}