package com.example.demo.domain.survey.controller;

import com.example.demo.domain.survey.dto.SurveyDto;
import com.example.demo.domain.survey.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * SurveyController — 설문 API 엔드포인트
 *
 * @RestController: @Controller + @ResponseBody. 모든 메서드의 반환값이 JSON으로 직렬화됩니다.
 * @RequestMapping("/api/surveys"): 이 컨트롤러의 모든 경로는 /api/surveys로 시작합니다.
 *
 * ResponseEntity<T>를 반환하는 이유:
 *   - HTTP 상태 코드를 명시적으로 제어할 수 있습니다 (200, 201, 400 등)
 *   - 단순히 객체를 반환하면 항상 200이 되지만, ResponseEntity를 쓰면 용도에 맞는 상태를 반환할 수 있습니다
 */
@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    /**
     * 현재 활성화된 설문지와 문항 목록을 반환합니다.
     *
     * GET /api/surveys/active
     *
     * 사용 시나리오: 사용자가 설문 화면에 진입할 때 문항 목록을 불러오는 최초 호출
     */
    @GetMapping("/active")
    public ResponseEntity<SurveyDto.SurveyResponse> getActiveSurvey() {
        return ResponseEntity.ok(surveyService.getActiveSurvey());
    }

    /**
     * 설문 세션을 시작합니다.
     *
     * POST /api/surveys/sessions
     * Body: { "sessionUuid": "550e8400-e29b-41d4-a716-446655440000" }
     *
     * 같은 UUID로 재요청 시 기존 세션을 반환합니다 (멱등성).
     * 201 Created: 새 리소스가 생성되었을 때의 표준 응답 코드입니다.
     */
    @PostMapping("/sessions")
    public ResponseEntity<SurveyDto.StartResponse> startSession(
            @RequestBody SurveyDto.StartRequest request) {
        return ResponseEntity.status(201).body(surveyService.startSession(request));
    }

    /**
     * 설문 답변을 제출하고 페르소나 결과를 반환합니다.
     *
     * POST /api/surveys/sessions/{sessionUuid}/submit
     * PathVariable: sessionUuid — URL 경로에서 세션 UUID를 추출합니다
     * Body: { "answers": [{ "questionId": 1, "selectedIndex": 2 }, ...] }
     *
     * @PathVariable: URL 경로의 {sessionUuid} 부분을 파라미터에 바인딩합니다.
     * @RequestBody: HTTP 요청 바디의 JSON을 DTO로 역직렬화합니다.
     */
    @PostMapping("/sessions/{sessionUuid}/submit")
    public ResponseEntity<SurveyDto.SubmitResponse> submitSurvey(
            @PathVariable String sessionUuid,
            @RequestBody SurveyDto.SubmitRequest request) {
        return ResponseEntity.ok(surveyService.submitSurvey(sessionUuid, request));
    }
}