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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SurveyService — 설문 비즈니스 로직
 *
 * 변경 사항:
 *   - Bedrock 응답에서 6대 기본 지표 점수 추출하여 SubmitResponse에 포함
 *   - 추가 4개 지표(독서_지속성 등)는 서브 분류 전용이므로 응답에서 제외
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

    // Radar Chart에 표시할 6대 기본 지표 키 목록 (순서 유지)
    private static final List<String> RADAR_KEYS = List.of(
            "지적_확장성",
            "분석적_깊이",
            "실용_지향성",
            "감성_몰입도",
            "정보_체계화",
            "사회적_영향도"
    );

    /**
     * 설문 답변 제출 및 페르소나 분석
     *
     * @param userId  로그인한 사용자 ID
     * @param request 설문 답변 Map (Q1~Q10)
     * @return 판별된 서브 페르소나 정보 + 6대 지표 점수
     */
    public SurveyDto.SubmitResponse submit(Long userId, SurveyDto.SubmitRequest request) {

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "사용자를 찾을 수 없습니다: " + userId));

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

        // 5. PersonaType 테이블에서 코드로 엔티티 조회
        PersonaType personaType = personaTypeRepository.findByCode(personaCode.name())
                .orElseThrow(() -> new IllegalStateException(
                        "PersonaType을 찾을 수 없습니다. data.sql을 확인하세요: "
                                + personaCode.name()));

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

        // 8. SubmitResponse 반환 (6대 지표 점수 포함)
        return SurveyDto.SubmitResponse.builder()
                .personaCode(personaCode)
                .personaName(personaType.getName())
                .personaReason(result.getPersonaReason())
                .scores(scores)
                .build();
    }

    // ── 유틸 ──────────────────────────────────────────────────────────────

    /**
     * Bedrock 원본 응답 JSON에서 6대 기본 지표 점수만 추출한다.
     *
     * Bedrock 응답 형식:
     * {
     *   "persona_code": "...",
     *   "reason": "...",
     *   "scores": {
     *     "지적_확장성": 8.5, "분석적_깊이": 4.0, ...,
     *     "독서_지속성": 3.5, "독서_다양성": 7.0, ...  ← 서브 분류 전용, 제외
     *   }
     * }
     *
     * @param rawResponse Bedrock 원본 응답 문자열
     * @return 6대 지표 점수 Map (순서 유지). 파싱 실패 시 빈 Map 반환
     */
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

            // RADAR_KEYS 순서대로 6개만 추출
            for (String key : RADAR_KEYS) {
                JsonNode value = scoresNode.path(key);
                if (!value.isMissingNode()) {
                    scores.put(key, value.asDouble());
                } else {
                    log.warn("[SurveyService] scores에 {} 키 없음", key);
                    scores.put(key, 0.0);  // 누락 시 0으로 처리
                }
            }

        } catch (Exception e) {
            log.error("[SurveyService] scores 파싱 실패: {}", e.getMessage());
        }

        return scores;
    }
}
