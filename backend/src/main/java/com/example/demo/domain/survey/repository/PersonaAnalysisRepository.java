package com.example.demo.domain.survey.repository;

import com.example.demo.auth.entity.User;
import com.example.demo.domain.survey.entity.PersonaAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonaAnalysisRepository extends JpaRepository<PersonaAnalysis, Long> {

    // 사용자의 가장 최신 분석 결과 조회 (analyzed_at 내림차순 첫 번째)
    // RecommendationService에서 userId 기반 최신 페르소나 추천 시 사용
    Optional<PersonaAnalysis> findTopByUserOrderByAnalyzedAtDesc(User user);
}
