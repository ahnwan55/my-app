package com.example.demo.domain.book.entity;

/**
 * BookGenre - 도서 장르 분류 enum
 *
 * 도서관 정보나루 KDC(한국십진분류법) 기반으로 설계했습니다.
 * 페르소나별 추천 장르 매핑에 활용됩니다.
 *
 * 페르소나 - 장르 연관:
 *   EXPLORER  → HUMANITIES, SCIENCE, HISTORY
 *   CURATOR   → NOVEL, ESSAY, POETRY
 *   NAVIGATOR → SELF_HELP, BUSINESS
 *   DWELLER   → FANTASY, HEALING
 *   ANALYST   → MYSTERY, SOCIAL
 *   DIVER     → PHILOSOPHY, HUMANITIES
 */
public enum BookGenre {
    NOVEL,      // 소설
    ESSAY,      // 에세이
    POETRY,     // 시/시집
    SELF_HELP,  // 자기계발
    BUSINESS,   // 경제/경영
    HUMANITIES, // 인문
    SCIENCE,    // 과학
    HISTORY,    // 역사
    PHILOSOPHY, // 철학
    FANTASY,    // 판타지
    HEALING,    // 힐링
    MYSTERY,    // 추리/스릴러
    SOCIAL,     // 사회/비평
    OTHER       // 기타
}