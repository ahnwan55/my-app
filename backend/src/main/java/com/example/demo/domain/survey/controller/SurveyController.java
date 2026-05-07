package com.example.demo.domain.survey.controller;

import com.example.demo.domain.survey.dto.SurveyDto;
import com.example.demo.domain.survey.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SurveyController — 설문 API 컨트롤러
 *
 * 변경 사항:
 *   - 세션 기반 엔드포인트 제거
 *   - GET  /api/surveys/questions → 설문 문항 목록 조회
 *   - POST /api/surveys/submit    → 답변 제출 + 페르소나 분석
 */
@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    /**
     * 설문 문항 목록 조회
     * 고정 문항이므로 DB 조회 없이 상수에서 반환한다.
     */
    @GetMapping("/questions")
    public ResponseEntity<List<SurveyDto.QuestionResponse>> getQuestions() {
        return ResponseEntity.ok(SurveyDto.QuestionResponse.fromConstants());
    }

    /**
     * 설문 답변 제출 및 페르소나 분석
     * JWT 인증된 사용자의 답변을 Bedrock Claude에 전달하여 페르소나를 판별한다.
     */
    @PostMapping("/submit")
    public ResponseEntity<SurveyDto.SubmitResponse> submit(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody SurveyDto.SubmitRequest request) {

        Long kakaoId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(surveyService.submit(kakaoId, request));
    }
}
