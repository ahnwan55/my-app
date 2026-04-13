package com.example.demo.domain.product.entity;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.UpdateTimestamp;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 금융감독원 API 상품 코드 (고유값)
    @Column(nullable = false, unique = true)
    private String finPrdtCd;

    // 은행명
    @Column(nullable = false)
    private String korCoNm;

    // 상품명
    @Column(nullable = false)
    private String finPrdtNm;

    // 상품 유형 (예금/적금)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType productType;

    // 가입 방법
    private String joinWay;

    // 우대 조건
    @Column(columnDefinition = "TEXT")
    private String spclCnd;


    // 가입 제한
    private String joinDeny;

    // 가입 대상
    private String joinMember;

    // 최고 한도
    private Long maxLimit;

    // 공시 월
    private String dclsMonth;

    // 데이터 변경 감지용 해시값
    @UpdateTimestamp        // 자동 갱신
    private String dataHash;

    // 현재 판매 중 여부
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // API에서 마지막으로 가져온 시간
    private LocalDateTime fetchedAt;

    // 기간별 금리 옵션 목록
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ProductOption> options = new ArrayList<>();
}