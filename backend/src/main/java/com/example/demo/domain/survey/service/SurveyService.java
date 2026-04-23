package com.example.demo.domain.survey.service;

import com.example.demo.auth.entity.User;
import com.example.demo.auth.repository.UserRepository;
import com.example.demo.domain.persona.entity.PersonaCode;
import com.example.demo.domain.persona.entity.PersonaType;
import com.example.demo.domain.persona.repository.PersonaTypeRepository;
import com.example.demo.domain.survey.dto.SurveyDto;
import com.example.demo.domain.survey.entity.PersonaAnalysis;
import com.example.demo.domain.survey.repository.PersonaAnalysisRepository;
import com.example.demo.infra.ai.BedrockClient;
import com.example.demo.infra.ai.dto.BedrockAnalysisResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SurveyService — 설문 비즈니스 로직
 *
 * submit() 흐름:
 *   1. 사용자 조회
 *   2. answers Map → JSON 문자열 직렬화
 *   3. Bedrock Claude 호출 → 서브 페르소나 판별
 *   4. PersonaType 조회 (code로 매핑)
 *   5. PersonaAnalysis 저장
 *   6. User.personaType 업데이트 (최신 페르소나 갱신)
 *   7. SubmitResponse 반환
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SurveyService {

    private final UserRepository userRepository;
    private final PersonaTypeRepository personaTypeRepository;
    private final PersonaAnalysisRepository personaAnalysisRepository;
    private final BedrockClient bedrockClient;
    private final ObjectMapper objectMapper;

    /**
     * 설문 답변 제출 및 페르소나 분석
     *
     * @param userId  로그인한 사용자 ID
     * @param request 설문 답변 Map (Q1~Q10)
     * @return 판별된 서브 페르소나 정보
     */
    public SurveyDto.SubmitResponse submit(Long userId, SurveyDto.SubmitRequest request) {

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "사용자를 찾을 수 없습니다: " + userId));

        // 2. answers Map → JSON 문자열 직렬화 (DB 저장 및 Bedrock 프롬프트 입력용)
        String answersJson;
        try {
            answersJson = objectMapper.writeValueAsString(request.getAnswers());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("답변 직렬화 실패", e);
        }

        // 3. Bedrock Claude 호출 → 서브 페르소나 12종 중 1개 판별
        BedrockAnalysisResult result = bedrockClient.classifyPersona(request.getAnswers());
        PersonaCode personaCode = result.getPersonaCode();

        // 4. PersonaType 테이블에서 코드로 엔티티 조회
        // PersonaCode.name()과 persona_type.code 컬럼값이 일치해야 함
        PersonaType personaType = personaTypeRepository.findByCode(personaCode.name())
                .orElseThrow(() -> new IllegalStateException(
                        "PersonaType을 찾을 수 없습니다. data.sql을 확인하세요: "
                                + personaCode.name()));

        // 5. PersonaAnalysis 저장 (분석 이력)
        PersonaAnalysis analysis = PersonaAnalysis.builder()
                .user(user)
                .personaType(personaType)
                .answersJson(answersJson)
                .personaReason(result.getPersonaReason())
                .bedrockModelId(result.getModelId())
                .bedrockRawResponse(result.getRawResponse())
                .build();
        personaAnalysisRepository.save(analysis);

        // 6. User.personaType 업데이트 (최신 페르소나 갱신)
        user.updatePersona(personaType);

        log.info("[SurveyService] 페르소나 판별 완료 userId={} personaCode={}",
                userId, personaCode.name());

        // 7. SubmitResponse 반환
        return SurveyDto.SubmitResponse.builder()
                .personaCode(personaCode)
                .personaName(personaType.getName())
                .personaReason(result.getPersonaReason())
                .build();
    }
}
