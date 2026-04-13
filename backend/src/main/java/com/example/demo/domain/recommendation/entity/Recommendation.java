package com.example.demo.domain.recommendation.entity;

import com.example.demo.domain.survey.entity.SurveySession;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "recommendation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 세션(익명 사용자)의 추천인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private SurveySession session;

    // 외부 API의 상품 식별자
    @Column(nullable = false)
    private String productCode;

    // 상품명
    @Column(nullable = false)
    private String productName;

    // 은행명
    private String bankName;

    // 상품 유형 (예금/적금)
    @Enumerated(EnumType.STRING)
    private ProductType productType;

    // 금리
    private Double interestRate;

    // 추천 순위 (1~3위)
    private Integer rank;

    // AI가 생성한 추천 이유
    @Column(columnDefinition = "TEXT")
    private String recommendReason;

    // 추천 생성 시간
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}