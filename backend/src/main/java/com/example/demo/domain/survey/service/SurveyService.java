package com.example.demo.domain.survey.service;

import com.example.demo.domain.persona.entity.PersonaCode;
import com.example.demo.domain.persona.entity.PersonaType;
import com.example.demo.domain.persona.repository.PersonaTypeRepository;
import com.example.demo.domain.survey.dto.SurveyDto;
import com.example.demo.domain.survey.entity.*;
import com.example.demo.domain.survey.repository.SurveyRepository;
import com.example.demo.domain.survey.repository.SurveySessionRepository;
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
     *   4. 점수 기반으로 페르소나 분류
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

        // 점수 기반 페르소나 분류
        PersonaCode personaCode = classifyPersona(totalScore);

        // 페르소나 타입 Entity 조회
        PersonaType personaType = personaTypeRepository.findByCode(personaCode)
                .orElseThrow(() -> new IllegalStateException("페르소나 타입이 DB에 없습니다: " + personaCode));

        // 세션 완료 처리
        session.completeWithPersona(totalScore, personaType);

        return SurveyDto.SubmitResponse.builder()
                .sessionUuid(sessionUuid)
                .totalScore(totalScore)
                .personaCode(personaCode)
                .personaName(personaType.getName())
                .build();
    }

    /**
     * 점수 기반 페르소나 분류
     *
     * 총점 구간별로 페르소나를 결정함
     * 페르소나 기준 확정 후 구간 수정 필요
     */
    private PersonaCode classifyPersona(int totalScore) {
        if (totalScore <= 10) return PersonaCode.SAFETY_GUARD;
        if (totalScore <= 15) return PersonaCode.STEADY_WORKER;
        if (totalScore <= 20) return PersonaCode.BALANCED_SPENDER;
        if (totalScore <= 25) return PersonaCode.RATE_OPTIMIZER;
        if (totalScore <= 30) return PersonaCode.GOAL_ACHIEVER;
        return PersonaCode.FUTURE_PLANNER;
    }
}