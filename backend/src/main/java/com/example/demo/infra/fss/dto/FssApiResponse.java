package com.example.demo.infra.fss.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * FssApiResponse — 금융감독원 API 응답 DTO
 *
 * 금융감독원 API는 예금/적금 모두 동일한 구조로 응답합니다.
 * result.baseList  → 상품 기본 정보
 * result.optionList → 기간별 금리 옵션 (fin_prdt_cd로 baseList와 연결)
 *
 * @JsonProperty: JSON 필드명(스네이크케이스)을 Java 필드명(카멜케이스)에 매핑합니다.
 * @NoArgsConstructor: Jackson이 역직렬화할 때 기본 생성자가 필요합니다.
 */
@Getter
@NoArgsConstructor
public class FssApiResponse {

    private Result result;

    @Getter
    @NoArgsConstructor
    public static class Result {

        @JsonProperty("err_cd")
        private String errCd;        // 에러 코드 ("000" = 정상)

        @JsonProperty("err_msg")
        private String errMsg;       // 에러 메시지

        @JsonProperty("total_count")
        private String totalCount;

        @JsonProperty("baseList")
        private List<BaseItem> baseList;

        @JsonProperty("optionList")
        private List<OptionItem> optionList;

        public boolean isSuccess() {
            return "000".equals(errCd);
        }
    }

    /**
     * 상품 기본 정보
     */
    @Getter
    @NoArgsConstructor
    public static class BaseItem {

        @JsonProperty("fin_prdt_cd")
        private String finPrdtCd;       // 금융 상품 코드 (고유 식별자)

        @JsonProperty("kor_co_nm")
        private String korCoNm;         // 은행명

        @JsonProperty("fin_prdt_nm")
        private String finPrdtNm;       // 상품명

        @JsonProperty("join_way")
        private String joinWay;         // 가입 방법

        @JsonProperty("spcl_cnd")
        private String spclCnd;         // 우대 조건

        @JsonProperty("join_deny")
        private String joinDeny;        // 가입 제한 (1: 제한없음, 2: 서민전용, 3: 일부제한)

        @JsonProperty("join_member")
        private String joinMember;      // 가입 대상

        @JsonProperty("max_limit")
        private Long maxLimit;          // 최고 한도 (null 가능)

        @JsonProperty("dcls_month")
        private String dclsMonth;       // 공시 월
    }

    /**
     * 금리 옵션 (기간별)
     */
    @Getter
    @NoArgsConstructor
    public static class OptionItem {

        @JsonProperty("fin_prdt_cd")
        private String finPrdtCd;       // 상품 코드 (BaseItem과 연결)

        @JsonProperty("intr_rate_type")
        private String intrRateType;    // 금리 유형 ("S": 단리, "M": 복리)

        @JsonProperty("save_trm")
        private String saveTrm;         // 저축 기간 (문자열, 개월 수)

        @JsonProperty("intr_rate")
        private BigDecimal intrRate;    // 기본 금리

        @JsonProperty("intr_rate2")
        private BigDecimal intrRate2;   // 우대 금리

        @JsonProperty("rsrv_type")
        private String rsrvType;      // 적립 유형 코드 (S: 정액, F: 자유) — 적금 전용, 예금은 null

        @JsonProperty("rsrv_type_nm")
        private String rsrvTypeNm;    // 적립 유형명 — 적금 전용, 예금은 null
    }
}