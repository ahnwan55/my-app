package com.example.demo.domain.recommendation.repository;

import com.example.demo.domain.persona.entity.PersonaCode;
import com.example.demo.domain.recommendation.entity.RecommendRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * RecommendRuleRepository — 페르소나별 추천 규칙 조회 인터페이스
 *
 * RecommendRule은 "SAFETY_GUARD 페르소나는 복리, 12개월 이상, 금리 3% 이상 상품을 추천"처럼
 * 페르소나별 추천 기준을 DB에 저장해둔 규칙 테이블입니다.
 * 규칙이 바뀌어도 코드를 배포하지 않고 DB만 수정하면 됩니다.
 */
public interface RecommendRuleRepository extends JpaRepository<RecommendRule, Long> {

    /**
     * 특정 페르소나 코드에 해당하는 추천 규칙을 우선순위 순으로 조회합니다.
     *
     * PersonaCode(enum)로 바로 조회하기 위해 JPQL에서 PersonaType을 JOIN합니다.
     * priority 오름차순 = 우선순위 높은 것부터
     *
     * 사용 시나리오:
     *   - 설문 완료 후 페르소나가 결정되면 이 규칙을 조회해서 상품 필터링에 적용
     *
     * @param personaCode 페르소나 코드 (예: SAFETY_GUARD)
     */
    @Query("SELECT r FROM RecommendRule r " +
            "JOIN FETCH r.personaType pt " +
            "WHERE pt.code = :personaCode " +
            "ORDER BY r.priority ASC")
    List<RecommendRule> findByPersonaCodeOrderByPriority(@Param("personaCode") PersonaCode personaCode);
}