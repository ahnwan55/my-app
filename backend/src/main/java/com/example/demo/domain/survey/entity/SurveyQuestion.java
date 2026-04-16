package com.example.demo.domain.survey.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * SurveyQuestion - 설문 문항 엔티티
 *
 * 주관식 자유 서술형으로 변경됨에 따라
 * 기존 선택지(options), 점수(scores) 컬럼을 제거했습니다.
 * 문항 내용(content)과 순서(orderNum)만 관리합니다.
 */
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

    // 문항 내용 (예: 평소에 책을 읽는 가장 큰 이유를 자유롭게 이야기해주세요.)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 문항 순서 (1~10)
    @Column(nullable = false)
    private Integer orderNum;
}