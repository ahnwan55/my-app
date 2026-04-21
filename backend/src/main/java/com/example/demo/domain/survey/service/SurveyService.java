package com.example.demo.domain.survey.service;

import com.example.demo.domain.persona.entity.PersonaCode;
import com.example.demo.domain.persona.entity.PersonaType;
import com.example.demo.domain.persona.repository.PersonaTypeRepository;
import com.example.demo.domain.survey.dto.SurveyDto;
import com.example.demo.domain.survey.entity.*;
import com.example.demo.domain.survey.repository.SurveyRepository;
import com.example.demo.domain.survey.repository.SurveySessionRepository;
import com.example.demo.infra.ai.BedrockClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final SurveySessionRepository surveySessionRepository;
    private final PersonaTypeRepository personaTypeRepository;
    private final BedrockClient bedrockClient;

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
     * 같은 UUID로 세션이 있으면 기존 세션 반환 (멱등성 보장)
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
     *   2. questionId → 문항 내용 매핑
     *   3. 문항 내용 + 답변 텍스트 Map 구성
     *   4. AI 서버(FastAPI)에 전달 → 페르소나 코드 반환
     *   5. 페르소나 타입 Entity 조회
     *   6. SurveyAnswer 저장 + 세션 완료 처리
     */
    @Transactional
    public SurveyDto.SubmitResponse submitSurvey(String sessionUuid,
                                                 SurveyDto.SubmitRequest request) {
        SurveySession session = surveySessionRepository.findBySessionUuid(sessionUuid)
                .orElseThrow(() -> new IllegalArgumentException(
                        "세션을 찾을 수 없습니다: " + sessionUuid));

        if (session.isAlreadyCompleted()) {
            throw new IllegalStateException("이미 완료된 세션입니다.");
        }

        // questionId → SurveyQuestion Map (O(1) 조회)
        Map<Long, SurveyQuestion> questionMap = session.getSurvey().getQuestions()
                .stream()
                .collect(Collectors.toMap(SurveyQuestion::getId, q -> q));

        // 문항 순서 유지를 위해 LinkedHashMap 사용
        // { "평소에 책을 읽는 가장 큰 이유는?" : "새로운 것을 배우고 싶어서요" }
        Map<String, String> surveyAnswers = new LinkedHashMap<>();
        List<SurveyAnswer> answers = new java.util.ArrayList<>();

        for (SurveyDto.SubmitRequest.AnswerItem item : request.getAnswers()) {
            SurveyQuestion question = questionMap.get(item.getQuestionId());
            if (question == null) {
                throw new IllegalArgumentException(
                        "존재하지 않는 질문 ID: " + item.getQuestionId());
            }

            surveyAnswers.put(question.getContent(), item.getAnswerText());

            // 주관식 답변 저장 (SurveyAnswer에 answerText 저장)
            answers.add(SurveyAnswer.builder()
                    .session(session)
                    .question(question)
                    .answerText(item.getAnswerText())
                    .build());
        }

        // AI 서버에 설문 답변 전달 → 페르소나 코드 반환
        // AI 서버 장애 시 기본값 EXPLORER로 폴백
        final PersonaCode personaCode = resolvePersonaCode(surveyAnswers);

        PersonaType personaType = personaTypeRepository.findByCode(personaCode)
                .orElseThrow(() -> new IllegalStateException(
                        "페르소나 타입이 DB에 없습니다: " + personaCode));

        // 세션 완료 처리 (totalScore 0 — 주관식이라 점수 없음)
        session.completeWithPersona(0, personaType);

        return SurveyDto.SubmitResponse.builder()
                .sessionUuid(sessionUuid)
                .personaCode(personaCode)
                .personaName(personaType.getName())
                .build();
    }
    /**
     * AI 서버로 페르소나 분류 요청
     * 실패 시 기본값 EXPLORER로 폴백
     */
    private PersonaCode resolvePersonaCode(Map<String, String> surveyAnswers) {
        try {
            return bedrockClient.classifyPersona(surveyAnswers);
        } catch (Exception e) {
            log.warn("[SurveyService] Bedrock 페르소나 분류 실패, 기본값 사용: {}", e.getMessage());
            return PersonaCode.EXPLORER;
        }
    }
}