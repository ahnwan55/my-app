package com.example.demo.domain.recommendation.entity;

import com.example.demo.domain.persona.entity.PersonaType;
import com.example.demo.domain.product.entity.ProductType;
import com.example.demo.domain.product.entity.RateType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "recommend_rule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class RecommendRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 페르소나에 적용되는 규칙인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_type_id", nullable = false)
    private PersonaType personaType;

    // 추천 상품 유형 (예금/적금)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType productType;

    // 금리 유형 (단리/복리)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RateType rateType;

    // 추천 최소 기간 (개월)
    @Column(nullable = false)
    private Integer minTermMonths;

    // 추천 최대 기간 (개월)
    @Column(nullable = false)
    private Integer maxTermMonths;

    // 최소 금리 기준
    @Column(precision = 5, scale = 2)
    private BigDecimal minRate;

    // 추천 우선순위 (숫자 낮을수록 높은 우선순위)
    @Column(nullable = false)
    private Integer priority;

    public boolean isValidTermRange() {
        return minTermMonths <= maxTermMonths;
    }

}