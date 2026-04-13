package com.example.demo.domain.survey.entity;

import jakarta.persistence.*;
import lombok.*;

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

    // 선택한 답변 인덱스 (0부터 시작)
    @Column(nullable = false)
    private Integer selectedIndex;

    // 해당 답변의 점수
    @Column(nullable = false)
    private Integer score;
}