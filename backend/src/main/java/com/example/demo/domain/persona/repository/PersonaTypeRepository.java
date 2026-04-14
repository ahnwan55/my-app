package com.example.demo.domain.persona.repository;

import com.example.demo.domain.persona.entity.PersonaCode;
import com.example.demo.domain.persona.entity.PersonaType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * PersonaTypeRepository — 페르소나 타입 조회 인터페이스
 *
 * PersonaType은 코드(SAFETY_GUARD 등)와 이름(철벽 수비대)을 담은
 * 마스터 데이터 테이블입니다.
 * 애플리케이션 시작 시 data.sql 또는 Flyway로 미리 INSERT해두어야 합니다.
 */
public interface PersonaTypeRepository extends JpaRepository<PersonaType, Long> {

    /**
     * PersonaCode enum으로 PersonaType Entity를 조회합니다.
     *
     * 사용 시나리오: 설문 완료 후 분류된 PersonaCode를 가지고
     * PersonaType Entity를 조회해서 SurveySession에 연결할 때
     */
    Optional<PersonaType> findByCode(PersonaCode code);
}