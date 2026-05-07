package com.example.demo.domain.persona.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "persona_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonaType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "persona_id")
    private Long personaId;

    // 페르소나 유형 코드 (예: "EMOTIVE", "ANALYTICAL") - UNIQUE 제약
    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    // 페르소나 유형 이름 (예: "감성적 몰입형")
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // 유형 대표 이미지 URL
    @Column(name = "image_url", length = 500)
    private String imageUrl;
}
