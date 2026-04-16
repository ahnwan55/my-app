package com.example.demo.domain.recommendation.dto;

import com.example.demo.domain.book.dto.BookDto;
import com.example.demo.domain.persona.entity.PersonaCode;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * RecommendationDto - 도서 추천 API 요청/응답 DTO 모음
 *
 * 기존 금융 상품 추천 구조에서 도서 추천 구조로 변경됨:
 *   - ProductDto.ProductResponse → BookDto.BookResponse
 *   - RecommendRule(규칙 기반) 제거 → LLM 기반 추천으로 단순화
 *   - aiComment 추가 (LLM이 생성한 추천 이유 설명)
 */
public class RecommendationDto {

    /**
     * 도서 추천 응답 DTO
     * GET /api/recommendations?sessionUuid=xxx 응답에 사용
     */
    @Getter
    @Builder
    public static class RecommendResponse {
        private PersonaCode personaCode;    // 예: EXPLORER
        private String personaName;         // 예: 지적 탐험가
        private String reason;              // 페르소나 기반 추천 이유 (고정 문구)
        private String aiComment;           // LLM이 생성한 맞춤 추천 코멘트 (null이면 AI 미사용)
        private List<RankedBook> books;     // 순위별 추천 도서 목록
    }

    /**
     * 순위별 추천 도서 DTO
     * RecommendResponse 안에 포함됩니다.
     */
    @Getter
    @Builder
    public static class RankedBook {
        private Integer rank;                       // 추천 순위 (1~5)
        private BookDto.BookResponse book;          // 도서 상세 정보
        private String matchReason;                 // 이 도서가 추천된 이유
    }
}