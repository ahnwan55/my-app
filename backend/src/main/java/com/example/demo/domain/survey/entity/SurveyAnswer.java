package com.example.demo.domain.survey.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * SurveyAnswer - 설문 답변 엔티티
 *
 * 주관식 자유 서술형으로 변경됨에 따라
 * 기존 selectedIndex(선택 인덱스), score(점수) 컬럼을 제거하고
 * answerText(주관식 텍스트 답변) 컬럼으로 교체했습니다.
 */
@Entity
@Table(name = "survey_answer",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"session_id", "question_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class SurveyAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 세션의 답변인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private SurveySession session;

    // 어떤 문항에 대한 답변인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private SurveyQuestion question;

    // 사용자가 직접 입력한 주관식 텍스트 답변
    @Column(nullable = false, columnDefinition = "TEXT")
    private String answerText;
}