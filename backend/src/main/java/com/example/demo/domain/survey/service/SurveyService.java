package com.example.demo.domain.survey.service;

import com.example.demo.domain.persona.entity.PersonaCode;
import com.example.demo.domain.persona.entity.PersonaType;
import com.example.demo.domain.persona.repository.PersonaTypeRepository;
import com.example.demo.domain.survey.dto.SurveyDto;
import com.example.demo.domain.survey.entity.*;
import com.example.demo.domain.survey.repository.SurveyRepository;
import com.example.demo.domain.survey.repository.SurveySessionRepository;
import com.example.demo.infra.ai.AiServerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final SurveySessionRepository surveySessionRepository;
    private final PersonaTypeRepository personaTypeRepository;
    private final AiServerClient aiServerClient; // FastAPI XGBoost 연동

    /**
     * 현재 활성화된 설문지를 조회합니다.
     *
     * @throws IllegalStateException 활성화된 설문이 없을 때
     */
    public SurveyDto.SurveyResponse getActiveSurvey() {
        Survey survey = surveyRepository.findByIsActiveTrue()
                .orElseThrow(() -> new IllegalStateException("활성화된 설문이 없습니다."));
        return SurveyDto.SurveyResponse.of(survey);
    }

    /**
     * 설문 세션을 시작(생성)합니다.
     *
     * @Transactional: 읽기 + 쓰기가 모두 필요하여 readOnly를 덮어씁니다.
     *   같은 UUID로 세션이 있으면 기존 세션 반환 (멱등성 보장)
     */
    @Transactional
    public SurveyDto.StartResponse startSession(SurveyDto.StartRequest request) {
        return surveySessionRepository.findBySessionUuid(request.getSessionUuid())
                .map(SurveyDto.StartResponse::of)
                .orElseGet(() -> {
                    Survey activeSurvey = surveyRepository.findByIsActiveTrue()
                            .orElseThrow(() -> new IllegalStateException("활성화된 설문이 없습니다."));

                    SurveySession session = SurveySession.builder()
                            .sessionUuid(request.getSessionUuid())
                            .survey(activeSurvey)
                            .build();

                    return SurveyDto.StartResponse.of(surveySessionRepository.save(session));
                });
    }

    /**
     * 설문 답변을 제출하고 페르소나를 결정합니다.
     *
     * 처리 순서:
     *   1. 세션 조회 및 완료 여부 확인
     *   2. questionId → SurveyQuestion 매핑 (Map으로 O(1) 조회)
     *   3. 각 답변의 점수 합산
     *   4. XGBoost로 페르소나 분류 (FastAPI 호출) → 실패 시 점수 기반 폴백
     *   5. SurveyAnswer 일괄 저장
     *   6. 세션 완료 처리 (completeWithPersona)
     */
    @Transactional
    public SurveyDto.SubmitResponse submitSurvey(String sessionUuid,
                                                 SurveyDto.SubmitRequest request) {
        SurveySession session = surveySessionRepository.findBySessionUuid(sessionUuid)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + sessionUuid));

        if (session.isAlreadyCompleted()) {
            throw new IllegalStateException("이미 완료된 세션입니다.");
        }

        // questionId를 키로 하는 Map 생성 → O(1) 조회
        Map<Long, SurveyQuestion> questionMap = session.getSurvey().getQuestions()
                .stream()
                .collect(Collectors.toMap(SurveyQuestion::getId, q -> q));

        int totalScore = 0;
        List<SurveyAnswer> answers = new ArrayList<>();

        for (SurveyDto.SubmitRequest.AnswerItem item : request.getAnswers()) {
            SurveyQuestion question = questionMap.get(item.getQuestionId());
            if (question == null) {
                throw new IllegalArgumentException("존재하지 않는 질문 ID: " + item.getQuestionId());
            }

            List<Integer> scores = question.getScoreList();
            int selectedIndex = item.getSelectedIndex();

            if (selectedIndex < 0 || selectedIndex >= scores.size()) {
                throw new IllegalArgumentException(
                        "유효하지 않은 선택 인덱스: questionId=" + item.getQuestionId()
                                + ", selectedIndex=" + selectedIndex);
            }

            int score = scores.get(selectedIndex);
            totalScore += score;

            answers.add(SurveyAnswer.builder()
                    .session(session)
                    .question(question)
                    .selectedIndex(selectedIndex)
                    .score(score)
                    .build());
        }

        // XGBoost로 페르소나 분류 (FastAPI 호출)
        // 실패 시 기존 점수 기반 분류로 폴백 → AI 서버 다운돼도 서비스 중단 없음
        PersonaCode personaCode = classifyPersonaWithAI(totalScore);

        // 페르소나 타입 Entity 조회
        PersonaType personaType = personaTypeRepository.findByCode(personaCode)
                .orElseThrow(() -> new IllegalStateException("페르소나 타입이 DB에 없습니다: " + personaCode));

        // 세션 완료 처리 (Entity의 비즈니스 메서드 호출)
        session.completeWithPersona(totalScore, personaType);

        return SurveyDto.SubmitResponse.builder()
                .sessionUuid(sessionUuid)
                .totalScore(totalScore)
                .personaCode(personaCode)
                .personaName(personaType.getName())
                .build();
    }

    /**
     * XGBoost로 페르소나 분류 (FastAPI /persona 호출)
     *
     * 호출 흐름:
     *   1. 설문 총점에서 savingsRate, riskScore 계산
     *   2. FastAPI /persona 호출 → XGBoost 분류
     *   3. 성공 시 AI 결과 사용, 실패 시 점수 기반 폴백
     *
     * 점수 → 특성 변환 방식:
     *   - savingsRate: 총점을 0~100으로 정규화 (총점이 높을수록 저축 성향 강함)
     *   - riskScore  : 총점을 반전 (총점이 높을수록 위험 선호도 낮음으로 가정)
     *
     * TODO: 실제 설문 문항이 확정되면 각 문항별 savingsRate, riskScore를
     *       직접 계산하도록 수정 필요
     *
     * @param totalScore 설문 총점
     * @return 분류된 PersonaCode
     */
    private PersonaCode classifyPersonaWithAI(int totalScore) {
        try {
            // 총점(0~30 범위 가정)을 0~100으로 정규화
            int savingsRate = Math.min(100, totalScore * 100 / 30);
            int riskScore = Math.max(0, 100 - savingsRate);

            // FastAPI /persona 호출 (나이, 소득은 추후 실제 유저 정보로 교체)
            String personaCodeStr = aiServerClient.classifyPersona(
                    25,          // age - 추후 실제 유저 나이로 교체
                    300,         // income - 추후 실제 유저 소득으로 교체
                    savingsRate,
                    riskScore,
                    12           // goalTerm - 추후 설문 항목으로 교체
            );

            if (personaCodeStr != null) {
                log.info("[SurveyService] XGBoost 페르소나 분류 성공: {}", personaCodeStr);
                return PersonaCode.valueOf(personaCodeStr);
            }
        } catch (Exception e) {
            log.warn("[SurveyService] XGBoost 분류 실패, 점수 기반 폴백: {}", e.getMessage());
        }

        // FastAPI 실패 시 기존 점수 기반 분류로 폴백
        log.info("[SurveyService] 점수 기반 페르소나 분류: totalScore={}", totalScore);
        return classifyPersonaByScore(totalScore);
    }

    /**
     * 점수 기반 페르소나 분류 (폴백용)
     *
     * XGBoost 호출 실패 시 사용하는 규칙 기반 분류
     * 총점 구간별로 페르소나를 결정함
     */
    private PersonaCode classifyPersonaByScore(int totalScore) {
        if (totalScore <= 10) return PersonaCode.SAFETY_GUARD;
        if (totalScore <= 15) return PersonaCode.STEADY_WORKER;
        if (totalScore <= 20) return PersonaCode.BALANCED_SPENDER;
        if (totalScore <= 25) return PersonaCode.RATE_OPTIMIZER;
        if (totalScore <= 30) return PersonaCode.GOAL_ACHIEVER;
        return PersonaCode.FUTURE_PLANNER;
    }
}