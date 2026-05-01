package com.example.demo.domain.survey.repository;

import com.example.demo.auth.entity.User;
import com.example.demo.domain.survey.entity.PersonaAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonaAnalysisRepository extends JpaRepository<PersonaAnalysis, Long> {

    // 사용자의 가장 최신 분석 결과 조회 (analyzed_at 내림차순 첫 번째)
    // RecommendationService에서 userId 기반 최신 페르소나 추천 시 사용
    Optional<PersonaAnalysis> findTopByUserOrderByAnalyzedAtDesc(User user);

    /**
     * 사용자의 전체 분석 이력 조회 — analyzed_at 내림차순 (최신 순)
     * MyPage 타임라인에 사용된다.
     *
     * Spring Data JPA가 메서드명을 파싱하여 아래 JPQL을 자동 생성한다:
     *   SELECT p FROM PersonaAnalysis p
     *   WHERE p.user.userId = :userId
     *   ORDER BY p.analyzedAt DESC
     */
    List<PersonaAnalysis> findByUser_UserIdOrderByAnalyzedAtDesc(Long userId);
}
