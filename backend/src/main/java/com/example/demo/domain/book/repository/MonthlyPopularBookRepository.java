package com.example.demo.domain.book.repository;

import com.example.demo.domain.book.entity.MonthlyPopularBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonthlyPopularBookRepository extends JpaRepository<MonthlyPopularBook, Long> {

    // 특정 연월의 랭킹 목록 순위 순 조회 (메인 페이지 Top10 노출용)
    List<MonthlyPopularBook> findByYearMonthOrderByRankingAsc(String yearMonth);

    // 특정 연월 데이터 전체 삭제 (배치 수집 시 기존 데이터 교체용)
    void deleteByYearMonth(String yearMonth);
}
