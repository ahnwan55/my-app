package com.example.demo.domain.recommendation.controller;

import com.example.demo.domain.recommendation.dto.RecommendationDto;
import com.example.demo.domain.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * RecommendationController — 상품 추천 API 엔드포인트
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * 설문 세션 UUID를 기반으로 맞춤 상품 추천 결과를 반환합니다.
     *
     * GET /api/recommendations?sessionUuid=550e8400-e29b-41d4-a716-446655440000
     *
     * 설문 완료 후 프론트엔드에서 sessionUuid를 들고 이 API를 호출합니다.
     * 추천 결과에는 페르소나 정보 + 추천 이유 + 상위 5개 상품이 포함됩니다.
     *
     * @RequestParam: 쿼리 파라미터 (?sessionUuid=xxx) 바인딩
     */
    @GetMapping
    public ResponseEntity<RecommendationDto.RecommendResponse> getRecommendations(
            @RequestParam String sessionUuid) {
        return ResponseEntity.ok(recommendationService.getRecommendations(sessionUuid));
    }
}