package com.example.demo.domain.recommendation.controller;

import com.example.demo.domain.recommendation.dto.RecommendationDto;
import com.example.demo.domain.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * RecommendationController - 도서 추천 API 컨트롤러
 *
 * 변경 사항:
 *   - sessionUuid 파라미터 제거
 *   - JWT 인증된 사용자의 userId 기반으로 최신 페르소나 추천으로 교체
 *
 * GET /api/recommendations
 *   → 로그인한 사용자의 최신 페르소나 분석 결과를 기반으로 도서를 추천한다.
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * 페르소나 기반 도서 추천 조회
     * JWT 필터에서 인증된 사용자 정보를 @AuthenticationPrincipal로 주입받는다.
     * UserDetails.getUsername()은 JwtFilter에서 kakaoId(String)로 설정되어 있다.
     */
    @GetMapping
    public ResponseEntity<RecommendationDto.RecommendResponse> getRecommendations(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(recommendationService.getRecommendations(userId));
    }
}
