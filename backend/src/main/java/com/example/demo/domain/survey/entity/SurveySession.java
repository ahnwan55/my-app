package com.example.demo.domain.survey.entity;

import com.example.demo.domain.persona.entity.PersonaType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "survey_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SurveySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 익명 사용자 식별용 UUID
    @Column(nullable = false, unique = true)
    private String sessionUuid;

    // 어떤 설문을 풀었는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    // 설문 완료 여부
    @Column(nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    // 이미 완료된 세션인지 확인
    public boolean isAlreadyCompleted() {
        return this.isCompleted;
    }

    // 총 점수 (페르소나 분류에 사용)
    private Integer totalScore;

    // 분류된 페르소나
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_type_id")
    private PersonaType personaType;

    // 세션 생성 시간
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // 답변 목록
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    @Builder.Default
    private List<SurveyAnswer> answers = new ArrayList<>();

    // 설문 완료 후 페르소나 결정 메서드
    // totalScore 기반으로 분류된 personaType 저장
    public void completeWithPersona(int totalScore, PersonaType personaType) {
        this.totalScore = totalScore;
        this.personaType = personaType;
        this.isCompleted = true;
    }
}