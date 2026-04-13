package com.example.demo.domain.survey.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "survey_question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class SurveyQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 설문에 속하는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    // 문항 내용 (예: 월 소득 중 저축 비율은?)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 문항 순서
    @Column(nullable = false)
    private Integer orderNum;

    // 선택지 JSON (예: ["10% 미만","10~30%","30% 이상"])
    @Column(columnDefinition = "TEXT")
    private String options;

    // 각 선택지의 점수 JSON (예: [1,2,3])
    @Column(columnDefinition = "TEXT")
    private String scores;
}