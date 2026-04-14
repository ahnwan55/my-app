package com.example.demo.domain.product.dto;

import com.example.demo.domain.product.entity.Product;
import com.example.demo.domain.product.entity.ProductOption;
import com.example.demo.domain.product.entity.ProductType;
import com.example.demo.domain.product.entity.RateType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * ProductDto — 금융 상품 관련 요청/응답 DTO 모음
 */
public class ProductDto {

    /**
     * 상품 목록/상세 응답 DTO
     * GET /api/products, GET /api/products/{id} 응답에 사용
     *
     * Product Entity의 민감하지 않은 정보만 노출합니다.
     * (dataHash, fetchedAt 같은 내부 관리 필드는 제외)
     */
    @Getter
    @Builder
    public static class ProductResponse {
        private Long id;
        private String finPrdtCd;       // 금감원 상품 코드
        private String korCoNm;         // 은행명
        private String finPrdtNm;       // 상품명
        private ProductType productType; // DEPOSIT / SAVING
        private String joinWay;          // 가입 방법
        private String spclCnd;          // 우대 조건
        private String joinDeny;         // 가입 제한
        private String joinMember;       // 가입 대상
        private Long maxLimit;           // 최고 한도
        private List<OptionResponse> options;  // 기간별 금리 옵션

        public static ProductResponse of(Product product) {
            return ProductResponse.builder()
                    .id(product.getId())
                    .finPrdtCd(product.getFinPrdtCd())
                    .korCoNm(product.getKorCoNm())
                    .finPrdtNm(product.getFinPrdtNm())
                    .productType(product.getProductType())
                    .joinWay(product.getJoinWay())
                    .spclCnd(product.getSpclCnd())
                    .joinDeny(product.getJoinDeny())
                    .joinMember(product.getJoinMember())
                    .maxLimit(product.getMaxLimit())
                    .options(
                            product.getOptions().stream()
                                    .map(OptionResponse::of)
                                    .toList()
                    )
                    .build();
        }
    }

    /**
     * 금리 옵션 응답 DTO
     * ProductResponse 안에 포함됩니다.
     */
    @Getter
    @Builder
    public static class OptionResponse {
        private Integer saveTrm;          // 저축 기간 (개월)
        private RateType intrRateType;    // S(단리) / M(복리)
        private BigDecimal intrRate;      // 기본 금리 (%)
        private BigDecimal intrRate2;     // 우대 금리 (%)

        public static OptionResponse of(ProductOption option) {
            return OptionResponse.builder()
                    .saveTrm(option.getSaveTrm())
                    .intrRateType(option.getIntrRateType())
                    .intrRate(option.getIntrRate())
                    .intrRate2(option.getIntrRate2())
                    .build();
        }
    }

    /**
     * 상품 목록 조회 요청 파라미터 DTO
     * GET /api/products?type=DEPOSIT 처럼 쿼리 파라미터로 받을 때 사용
     *
     * 직접 Controller 파라미터로 받아도 되지만, 파라미터가 늘어나면
     * DTO로 묶어두는 것이 유지보수에 유리합니다.
     */
    @Getter
    public static class ProductListRequest {
        private ProductType type;   // DEPOSIT / SAVING / null(전체)
    }
}