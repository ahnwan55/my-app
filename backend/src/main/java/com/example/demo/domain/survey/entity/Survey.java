package com.example.demo.domain.survey.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "survey")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 설문 제목 (예: 나의 재무 성향 파악하기)
    @Column(nullable = false)
    private String title;

    // 활성화 여부 (여러 버전 중 현재 사용 중인 설문)
    @Column(nullable = false)
    private Boolean isActive;

    // 설문에 속한 문항 목록
    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL)
    @OrderBy("orderNum ASC")
    @Builder.Default
    private List<SurveyQuestion> questions = new ArrayList<>();
}