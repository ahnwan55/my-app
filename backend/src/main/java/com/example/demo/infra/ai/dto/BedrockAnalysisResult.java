package com.example.demo.infra.ai.dto;

import com.example.demo.domain.persona.entity.PersonaCode;
import lombok.Builder;
import lombok.Getter;

/**
 * Bedrock 페르소나 분석 결과 전달 객체.
 * BedrockClient → PersonaService 로 결과를 전달할 때 사용한다.
 * PersonaAnalysis 엔티티 저장에 필요한 모든 필드를 포함한다.
 */
@Getter
@Builder
public class BedrockAnalysisResult {

    // Bedrock이 판별한 페르소나 유형 코드
    private final PersonaCode personaCode;

    // Bedrock이 반환한 판정 이유/설명 (persona_analysis.persona_reason 저장용)
    private final String personaReason;

    // Bedrock 원본 응답 전체 문자열 (persona_analysis.bedrock_raw_response 저장용)
    private final String rawResponse;

    // 사용한 모델 ID (persona_analysis.bedrock_model_id 저장용)
    private final String modelId;
}
