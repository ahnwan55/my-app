package com.example.demo.domain.survey.service;

import com.example.demo.domain.persona.entity.PersonaCode;
import com.example.demo.domain.persona.entity.PersonaType;
import com.example.demo.domain.persona.repository.PersonaTypeRepository;
import com.example.demo.domain.survey.dto.SurveyDto;
import com.example.demo.domain.survey.entity.*;
import com.example.demo.domain.survey.repository.SurveyRepository;
import com.example.demo.domain.survey.repository.SurveySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SurveyService — 설문 비즈니스 로직
 *
 * @Service: Spring이 이 클래스를 Bean으로 등록합니다.
 * @RequiredArgsConstructor: final 필드를 생성자 주입으로 자동 처리합니다.
 *   (직접 @Autowired 쓰는 것보다 테스트하기 쉽고, 불변성이 보장됩니다)
 * @Transactional(readOnly = true): 기본적으로 읽기 전용 트랜잭션을 사용합니다.
 *   읽기 전용이면 DB가 불필요한 변경 감지(Dirty Checking)를 수행하지 않아 성능이 향상됩니다.
 *   쓰기가 필요한 메서드는 @Transactional을 별도로 선언합니다.
 */
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
     * @Transactional: 읽기 + 쓰기가 모두 필요하므로 readOnly를 덮어씁니다.
     *   트랜잭션이 없으면 save() 후 예외가 발생해도 DB에 데이터가 남습니다.
     *   트랜잭션이 있으면 예외 시 자동으로 롤백됩니다.
     */
    @Transactional
    public SurveyDto.StartResponse startSession(SurveyDto.StartRequest request) {
        // 이미 같은 UUID로 세션이 있으면 기존 세션 반환 (멱등성 보장)
        // 멱등성: 같은 요청을 여러 번 보내도 결과가 동일한 성질
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
     *   2. questionId → SurveyQuestion 매핑 (Map으로 빠르게 탐색)
     *   3. 각 답변의 점수 합산
     *   4. 총점으로 페르소나 결정 (classifyPersona)
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

        // questionId를 키로 하는 Map 생성 → O(1) 탐색
        Map<Long, SurveyQuestion> questionMap = session.getSurvey().getQuestions()
                .stream()
                .collect(Collectors.toMap(SurveyQuestion::getId, q -> q));

        int totalScore = 0;
        List<SurveyAnswer> answers = new ArrayList<>();

        for (SurveyDto.SubmitRequest.AnswerItem item : request.getAnswers()) {
            SurveyQuestion question = questionMap.get(item.getQuestionId());
            if (question == null) {
                throw new IllegalArgumentException("존재하지 않는 문항 ID: " + item.getQuestionId());
            }

            List<Integer> scores = question.getScoreList();
            int selectedIndex = item.getSelectedIndex();

            // 선택 인덱스가 선택지 범위를 벗어나면 예외
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

        // 총점으로 페르소나 코드 결정
        PersonaCode personaCode = classifyPersona(totalScore);

        // 페르소나 타입 Entity 조회
        PersonaType personaType = personaTypeRepository.findByCode(personaCode)
                .orElseThrow(() -> new IllegalStateException("페르소나 타입이 DB에 없습니다: " + personaCode));

        // 세션 완료 처리 (Entity의 비즈니스 메서드 활용)
        session.completeWithPersona(totalScore, personaType);

        return SurveyDto.SubmitResponse.builder()
                .sessionUuid(sessionUuid)
                .totalScore(totalScore)
                .personaCode(personaCode)
                .personaName(personaType.getName())
                .build();
    }

    /**
     * 총점으로 페르소나를 분류합니다.
     *
     * 현재는 단순 점수 구간으로 분류합니다.
     * 추후 XGBoost 모델로 교체 예정 (FastAPI 연동).
     * XGBoost 교체 시 이 메서드만 수정하면 되므로 별도 메서드로 분리해두었습니다.
     *
     * TODO: FastAPI /predict 엔드포인트 연동으로 교체
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