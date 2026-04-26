package com.example.demo.domain.persona.entity;

import com.example.demo.auth.entity.User;
import com.example.demo.domain.survey.entity.PersonaAnalysis;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "persona_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonaResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long resultId;

    // 벡터를 보유한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 어느 분석 회차의 벡터인지 추적
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private PersonaAnalysis personaAnalysis;

    // SRoBERTa로 임베딩한 사용자 응답 벡터 (JSON 배열 문자열)
    // 도서 추천 시 book_vector와 코사인 유사도 비교에 사용
    @Column(name = "user_vector", columnDefinition = "TEXT")
    private String userVector;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    public PersonaResult(User user, PersonaAnalysis personaAnalysis, String userVector) {
        this.user = user;
        this.personaAnalysis = personaAnalysis;
        this.userVector = userVector;
        this.createdAt = LocalDateTime.now();
    }
}
