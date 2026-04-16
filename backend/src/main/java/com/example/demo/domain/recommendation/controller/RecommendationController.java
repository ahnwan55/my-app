package com.example.demo.domain.recommendation.controller;

import com.example.demo.domain.recommendation.dto.RecommendationDto;
import com.example.demo.domain.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * RecommendationController - 도서 추천 API 컨트롤러
 *
 * GET /api/recommendations?sessionUuid=xxx
 *   → 설문 완료 후 sessionUuid를 기반으로 페르소나에 맞는 도서를 추천합니다.
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * 페르소나 기반 도서 추천 조회
     *
     * @param sessionUuid 설문 세션 UUID (설문 완료 후 발급)
     */
    @GetMapping
    public ResponseEntity<RecommendationDto.RecommendResponse> getRecommendations(
            @RequestParam String sessionUuid) {
        return ResponseEntity.ok(recommendationService.getRecommendations(sessionUuid));
    }
}