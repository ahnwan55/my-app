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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    private static final List<String> RADAR_KEYS = List.of(
            "지적_확장성",
            "분석적_깊이",
            "실용_지향성",
            "감성_몰입도",
            "정보_체계화",
            "사회적_영향도"
    );

    public SurveyDto.SubmitResponse submit(Long userId, SurveyDto.SubmitRequest request) {

        // 1. 사용자 조회
        User user = userRepository.findByKakaoId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // 2. answers Map → JSON 문자열 직렬화
        String answersJson;
        try {
            answersJson = objectMapper.writeValueAsString(request.getAnswers());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("답변 직렬화 실패", e);
        }

        // 3. Bedrock Claude 호출 → 서브 페르소나 12종 중 1개 판별
        BedrockAnalysisResult result = bedrockClient.classifyPersona(request.getAnswers());
        PersonaCode personaCode = result.getPersonaCode();

        // 4. Bedrock 원본 응답에서 6대 지표 점수 추출
        Map<String, Double> scores = extractRadarScores(result.getRawResponse());

        // 5. PersonaType 테이블에서 코드로 엔티티 조회 (캐싱 적용)
        PersonaType personaType = getPersonaType(personaCode.name());

        // 6. PersonaAnalysis 저장
        PersonaAnalysis analysis = PersonaAnalysis.builder()
                .user(user)
                .personaType(personaType)
                .answersJson(answersJson)
                .personaReason(result.getPersonaReason())
                .bedrockModelId(result.getModelId())
                .bedrockRawResponse(result.getRawResponse())
                .build();
        personaAnalysisRepository.save(analysis);

        // 7. User.personaType 업데이트
        user.updatePersona(personaType);

        log.info("[SurveyService] 페르소나 판별 완료 userId={} personaCode={}",
                userId, personaCode.name());

        // 8. SubmitResponse 반환
        return SurveyDto.SubmitResponse.builder()
                .personaCode(personaCode)
                .personaName(personaType.getName())
                .personaReason(result.getPersonaReason())
                .scores(scores)
                .build();
    }

    @Cacheable(value = "persona", key = "#code")
    public PersonaType getPersonaType(String code) {
        return personaTypeRepository.findByCode(code)
                .orElseThrow(() -> new IllegalStateException(
                        "PersonaType을 찾을 수 없습니다. data.sql을 확인하세요: " + code));
    }

    private Map<String, Double> extractRadarScores(String rawResponse) {
        Map<String, Double> scores = new LinkedHashMap<>();

        if (rawResponse == null || rawResponse.isBlank()) {
            return scores;
        }

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode scoresNode = root.path("scores");

            if (scoresNode.isMissingNode() || scoresNode.isNull()) {
                log.warn("[SurveyService] Bedrock 응답에 scores 필드 없음");
                return scores;
            }

            for (String key : RADAR_KEYS) {
                JsonNode value = scoresNode.path(key);
                if (!value.isMissingNode()) {
                    scores.put(key, value.asDouble());
                } else {
                    log.warn("[SurveyService] scores에 {} 키 없음", key);
                    scores.put(key, 0.0);
                }
            }

        } catch (Exception e) {
            log.error("[SurveyService] scores 파싱 실패: {}", e.getMessage());
        }

        return scores;
    }
}