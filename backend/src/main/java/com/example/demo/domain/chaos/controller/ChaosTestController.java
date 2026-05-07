package com.example.demo.domain.chaos.controller;

import com.example.demo.domain.recommendation.dto.RecommendationDto;
import com.example.demo.domain.recommendation.service.RecommendationService;
import com.example.demo.domain.survey.dto.SurveyDto;
import com.example.demo.domain.survey.service.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ChaosTestController - Chaos Engineering 테스트 목적의 인증 우회 컨트롤러
 *
 * 외부 의존성(Bedrock 등) 장애 실험 시 OAuth 로그인 등의 문제를 제거하기 위해 제공.
 * 이 컨트롤러의 엔드포인트는 인증 없이 호출 가능하며, 내부 로직은 기존 Service를 그대로 재사용함.
 */
@RestController
@RequestMapping("/chaos-test")
@RequiredArgsConstructor
@Tag(name = "Chaos Engineering", description = "Chaos Mesh/k6 부하 테스트용 인증 우회 API")
public class ChaosTestController {

    private final RecommendationService recommendationService;
    private final SurveyService surveyService;

    @Operation(summary = "도서 추천 조회 (테스트용)", description = "인증 없이 userId를 파라미터로 받아 페르소나 기반 추천 조회")
    @GetMapping("/recommendations")
    public ResponseEntity<RecommendationDto.RecommendResponse> getRecommendations(
            @RequestParam(defaultValue = "1") Long userId) {
        return ResponseEntity.ok(recommendationService.getRecommendations(userId));
    }

    @Operation(summary = "설문 답변 제출 (테스트용)", description = "인증 없이 userId를 파라미터로 받아 설문 제출 및 페르소나 분석")
    @PostMapping("/surveys/submit")
    public ResponseEntity<SurveyDto.SubmitResponse> submitSurvey(
            @RequestParam(defaultValue = "1") Long userId,
            @RequestBody SurveyDto.SubmitRequest request) {
        return ResponseEntity.ok(surveyService.submit(userId, request));
    }
}
