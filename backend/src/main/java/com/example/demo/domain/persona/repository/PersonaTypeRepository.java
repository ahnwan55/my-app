package com.example.demo.domain.persona.repository;

import com.example.demo.domain.persona.entity.PersonaType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonaTypeRepository extends JpaRepository<PersonaType, Long> {

    // Bedrock 반환 코드로 PersonaType 조회
    // PersonaService에서 PersonaAnalysis 저장 시 사용
    Optional<PersonaType> findByCode(String code);
}
