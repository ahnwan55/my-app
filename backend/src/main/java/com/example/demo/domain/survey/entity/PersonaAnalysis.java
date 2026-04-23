package com.example.demo.domain.survey.entity;

import com.example.demo.auth.entity.User;
import com.example.demo.domain.persona.entity.PersonaType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "persona_analysis")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonaAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id")
    private Long analysisId;

    // 분석을 수행한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // AWS Bedrock이 판정한 페르소나 유형
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private PersonaType personaType;

    // 설문 응답 원본 JSON 문자열 {"Q1": "...", "Q2": "...", ...}
    // Bedrock 프롬프트 입력값으로도 활용
    @Column(name = "answers_json", columnDefinition = "TEXT")
    private String answersJson;

    // Bedrock Claude가 반환한 페르소나 판정 이유/설명
    @Column(name = "persona_reason", columnDefinition = "TEXT")
    private String personaReason;

    // 사용한 Bedrock 모델 ID (예: anthropic.claude-3-5-sonnet-20241022-v2:0)
    @Column(name = "bedrock_model_id", length = 100)
    private String bedrockModelId;

    // Bedrock 원본 응답 전체 (디버깅 및 재분석 대비)
    @Column(name = "bedrock_raw_response", columnDefinition = "TEXT")
    private String bedrockRawResponse;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    @Builder
    public PersonaAnalysis(User user, PersonaType personaType, String answersJson,
                           String personaReason, String bedrockModelId,
                           String bedrockRawResponse) {
        this.user = user;
        this.personaType = personaType;
        this.answersJson = answersJson;
        this.personaReason = personaReason;
        this.bedrockModelId = bedrockModelId;
        this.bedrockRawResponse = bedrockRawResponse;
        this.analyzedAt = LocalDateTime.now();
    }
}
