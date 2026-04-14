package com.example.demo.domain.survey.repository;

import com.example.demo.domain.survey.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * SurveyRepository — 설문지 조회 인터페이스
 *
 * Spring Data JPA가 런타임에 구현체를 자동 생성합니다.
 * JpaRepository<Survey, Long>을 상속받아 기본 CRUD + 페이징을 무료로 사용합니다.
 */
public interface SurveyRepository extends JpaRepository<Survey, Long> {

    /**
     * 현재 활성화된 설문지 1개를 조회합니다.
     *
     * 사용 시나리오: 사용자가 설문 화면에 진입할 때 "지금 사용 중인 설문"을 가져오는 용도.
     * 동시에 isActive=true인 설문이 여러 개면 안 되므로, 비즈니스 규칙으로 1개만 유지해야 합니다.
     *
     * Spring Data JPA 네이밍 규칙:
     *   findBy + 필드명 + 조건 → SELECT * FROM survey WHERE is_active = ?
     */
    Optional<Survey> findByIsActiveTrue();
}