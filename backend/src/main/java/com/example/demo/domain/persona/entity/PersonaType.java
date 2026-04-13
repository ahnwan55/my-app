package com.example.demo.domain.persona.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "persona_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class PersonaType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 프론트 API 호출용 의미있는 코드
    // 예: SAFETY_GUARD, GOAL_ACHIEVER 등
    @Column(nullable = false, unique = true)
    private String code;

    // 페르소나 표시 이름 (예: 철벽 수비대)
    @Column(nullable = false)
    private String name;
}