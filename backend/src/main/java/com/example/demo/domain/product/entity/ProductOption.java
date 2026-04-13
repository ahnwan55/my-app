package com.example.demo.domain.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_option")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class ProductOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 상품의 옵션인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 금리 유형 (단리/복리)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RateType intrRateType;


    // 저축 기간 (개월)
    @Column(nullable = false)
    private Integer saveTrm;

    // 기본 금리
    @Column(precision = 5, scale = 2)
    private BigDecimal intrRate;

    // 우대 금리
    @Column(precision = 5, scale = 2)
    private BigDecimal intrRate2;
}