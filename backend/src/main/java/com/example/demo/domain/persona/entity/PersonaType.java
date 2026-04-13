package com.example.demo.domain.persona.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "persona_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class PersonaType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 페르소나 이름 (예: 안정 추구형)
    @Column(nullable = false, unique = true)
    private String name;

    // 페르소나 설명
    @Column(columnDefinition = "TEXT")
    private String description;

    // 위험 성향 레벨
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;

    // 분류 기준 최솟값 점수
    @Column(nullable = false)
    private Integer minScore;

    // 분류 기준 최댓값 점수
    @Column(nullable = false)
    private Integer maxScore;
}