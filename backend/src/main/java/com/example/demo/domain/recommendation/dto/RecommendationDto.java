package com.example.demo.domain.recommendation.dto;

import com.example.demo.domain.persona.entity.PersonaCode;
import com.example.demo.domain.product.dto.ProductDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class RecommendationDto {

    @Getter
    @Builder
    public static class RecommendResponse {
        private PersonaCode personaCode;
        private String personaName;
        private String reason;
        private String aiComment;   // FastAPI AI 추천 코멘트 (null이면 AI 서버 미응답)
        private List<RankedProduct> products;
    }

    @Getter
    @Builder
    public static class RankedProduct {
        private Integer rank;
        private ProductDto.ProductResponse product;
        private String matchReason;
    }
}