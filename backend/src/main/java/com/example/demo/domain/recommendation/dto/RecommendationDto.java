package com.example.demo.domain.recommendation.dto;

import com.example.demo.domain.persona.entity.PersonaCode;
import com.example.demo.domain.product.dto.ProductDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * RecommendationDto — 상품 추천 관련 응답 DTO 모음
 */
public class RecommendationDto {

    /**
     * 추천 결과 응답 DTO
     * GET /api/recommendations?sessionUuid=xxx 응답에 사용
     *
     * 구성:
     *   - 어떤 페르소나인지 (코드 + 이름)
     *   - 왜 이 상품들을 추천했는지 (reason)
     *   - 추천된 상품 목록 (ProductResponse 재사용)
     */
    @Getter
    @Builder
    public static class RecommendResponse {
        private PersonaCode personaCode;       // 예: SAFETY_GUARD
        private String personaName;            // 예: 철벽 수비대
        private String reason;                 // 추천 이유 요약 (예: "안정성을 중시하는 성향에 맞게 복리 상품을 추천드립니다.")
        private List<RankedProduct> products;  // 우선순위순 추천 상품 목록
    }

    /**
     * 순위 정보가 포함된 추천 상품 DTO
     *
     * ProductDto.ProductResponse를 그대로 담고 rank(순위)만 추가합니다.
     * 조합(Composition) 패턴: 상속 대신 기존 DTO를 필드로 포함시켜 재사용합니다.
     */
    @Getter
    @Builder
    public static class RankedProduct {
        private Integer rank;                              // 추천 순위 (1부터 시작)
        private ProductDto.ProductResponse product;        // 상품 상세 정보
        private String matchReason;                        // 이 상품이 선택된 이유 (예: "12개월 복리, 기본금리 3.5%")
    }
}