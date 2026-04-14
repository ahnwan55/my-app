package com.example.demo.infra.fss;

import com.example.demo.domain.product.entity.Product;
import com.example.demo.domain.product.entity.ProductOption;
import com.example.demo.domain.product.entity.ProductType;
import com.example.demo.domain.product.entity.RateType;
import com.example.demo.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * FssApiClient — 금융감독원(FSS) Open API 연동 클라이언트
 *
 * ─────────────────────────────────────────────────────────────────
 * [현재 상태] API 키 발급 대기 중 → 실제 HTTP 호출 코드는 주석 처리됨
 *   API 키 발급 후 아래 순서로 활성화:
 *   1. application.yml에 fss.api-key: 발급받은_키 추가
 *   2. fetchAndSaveDeposits() / fetchAndSaveSavings() 내 주석 해제
 *   3. FssScheduler.java 의 @Scheduled 주석 해제
 * ─────────────────────────────────────────────────────────────────
 *
 * 금융감독원 금융상품통합비교공시 API:
 *   - 예금: https://finlife.fss.or.kr/finlifeapi/depositProductsSearch.json
 *   - 적금: https://finlife.fss.or.kr/finlifeapi/savingProductsSearch.json
 *   - 문서: https://finlife.fss.or.kr/main/main.do
 *
 * @Slf4j: Lombok이 log 필드를 자동 생성합니다. log.info(), log.error() 등으로 사용합니다.
 * @Component: @Service와 유사하게 Bean으로 등록됩니다.
 *   인프라 계층(외부 API 호출)이므로 @Service 대신 @Component를 사용합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FssApiClient {

    private final ProductRepository productRepository;
    private final WebClient.Builder webClientBuilder;

    /**
     * application.yml의 fss.api-key 값을 주입합니다.
     * 키가 없으면 기본값 "NOT_SET"이 들어옵니다.
     *
     * application.yml에 추가해야 할 내용:
     * ---
     * fss:
     *   api-key: 여기에_발급받은_API_키_입력
     * ---
     *
     * ⚠️ 이 값을 application.yml에 하드코딩하면 GitHub에 노출됩니다.
     *    반드시 아래 중 하나를 사용하세요:
     *    (1) 환경변수: export FSS_API_KEY=키값
     *        application.yml: fss.api-key: ${FSS_API_KEY}
     *    (2) AWS Secrets Manager (운영 환경 권장)
     *        — 이미 KMS/Secrets Manager가 아키텍처에 포함되어 있습니다
     */
    @Value("${fss.api-key:NOT_SET}")
    private String apiKey;

    // 금융감독원 API 베이스 URL
    private static final String FSS_BASE_URL = "https://finlife.fss.or.kr/finlifeapi";

    // 은행권 금융회사 코드 (020000: 은행)
    private static final String TOPFIN_CD = "020000";

    /**
     * 예금 상품을 금융감독원 API에서 가져와 DB에 저장합니다.
     *
     * TODO: API 키 발급 후 주석 해제
     */
    public void fetchAndSaveDeposits() {
        if ("NOT_SET".equals(apiKey)) {
            log.warn("[FssApiClient] API 키가 설정되지 않았습니다. 예금 상품 수집을 건너뜁니다.");
            // 개발 중 테스트용 더미 데이터 삽입
            saveDummyProducts(ProductType.DEPOSIT);
            return;
        }

        log.info("[FssApiClient] 예금 상품 수집 시작");

        /* ── API 키 발급 후 아래 주석 해제 ──────────────────────────────
        WebClient client = webClientBuilder.baseUrl(FSS_BASE_URL).build();

        FssDepositResponse response = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/depositProductsSearch.json")
                        .queryParam("auth", apiKey)
                        .queryParam("topFinGrpNo", TOPFIN_CD)
                        .queryParam("pageNo", 1)
                        .build())
                .retrieve()
                .bodyToMono(FssDepositResponse.class)
                .block();  // 동기 호출 (스케줄러에서 호출하므로 block() 사용)

        if (response == null || response.getResult() == null) {
            log.error("[FssApiClient] 예금 API 응답이 없습니다.");
            return;
        }

        // 응답 데이터를 Product + ProductOption Entity로 변환 후 upsert
        for (FssDepositResponse.BaseList item : response.getResult().getBaseList()) {
            upsertProduct(item, response.getResult().getOptionList(), ProductType.DEPOSIT);
        }

        log.info("[FssApiClient] 예금 상품 수집 완료");
        ─────────────────────────────────────────────────────────────── */
    }

    /**
     * 적금 상품을 금융감독원 API에서 가져와 DB에 저장합니다.
     *
     * TODO: API 키 발급 후 주석 해제
     */
    public void fetchAndSaveSavings() {
        if ("NOT_SET".equals(apiKey)) {
            log.warn("[FssApiClient] API 키가 설정되지 않았습니다. 적금 상품 수집을 건너뜁니다.");
            saveDummyProducts(ProductType.SAVING);
            return;
        }

        log.info("[FssApiClient] 적금 상품 수집 시작");
        // TODO: 예금과 동일한 패턴으로 구현
    }

    /**
     * 상품을 upsert(있으면 update, 없으면 insert)합니다.
     *
     * finPrdtCd(금감원 상품 코드)를 기준으로 중복을 판별합니다.
     *
     * TODO: API 키 발급 후 실제 응답 DTO에 맞게 파라미터 수정
     */
    private void upsertProduct(Object baseItem, Object optionList, ProductType productType) {
        // TODO: 실제 금감원 API 응답 구조에 맞게 구현
        // finPrdtCd로 기존 상품 조회 → 없으면 신규 저장, 있으면 업데이트
    }

    /**
     * 개발/테스트용 더미 상품 데이터를 DB에 삽입합니다.
     *
     * API 키 없이도 추천 로직을 테스트할 수 있도록 샘플 데이터를 제공합니다.
     * 실제 서비스에서는 사용하지 않습니다.
     *
     * 이미 더미 데이터가 있으면 중복 삽입을 건너뜁니다 (finPrdtCd 기준).
     */
    private void saveDummyProducts(ProductType type) {
        String prefix = type == ProductType.DEPOSIT ? "DEPOSIT" : "SAVING";

        List<DummyProductSpec> specs = switch (type) {
            case DEPOSIT -> List.of(
                    new DummyProductSpec(prefix + "_001", "국민은행", "KB Star 정기예금", 12, new BigDecimal("3.50"), new BigDecimal("4.00"), RateType.S),
                    new DummyProductSpec(prefix + "_002", "신한은행", "신한 My Dream 정기예금", 24, new BigDecimal("3.20"), new BigDecimal("3.80"), RateType.M),
                    new DummyProductSpec(prefix + "_003", "하나은행", "하나 정기예금", 6, new BigDecimal("3.70"), new BigDecimal("4.10"), RateType.S)
            );
            case SAVING -> List.of(
                    new DummyProductSpec(prefix + "_001", "국민은행", "KB 직장인 우대 적금", 12, new BigDecimal("4.00"), new BigDecimal("5.00"), RateType.M),
                    new DummyProductSpec(prefix + "_002", "우리은행", "우리 첫거래 우대 적금", 6, new BigDecimal("3.80"), new BigDecimal("4.50"), RateType.S),
                    new DummyProductSpec(prefix + "_003", "농협은행", "NH 올원 e적금", 24, new BigDecimal("3.50"), new BigDecimal("4.20"), RateType.M)
            );
        };

        for (DummyProductSpec spec : specs) {
            // 이미 존재하면 스킵 (중복 방지)
            if (productRepository.findByFinPrdtCd(spec.finPrdtCd()).isPresent()) {
                log.debug("[FssApiClient] 더미 상품 이미 존재, 스킵: {}", spec.finPrdtCd());
                continue;
            }

            Product product = Product.builder()
                    .finPrdtCd(spec.finPrdtCd())
                    .korCoNm(spec.korCoNm())
                    .finPrdtNm(spec.finPrdtNm())
                    .productType(type)
                    .joinWay("인터넷, 영업점")
                    .spclCnd("급여이체 시 우대금리 0.2%p 추가")
                    .joinDeny("1")
                    .joinMember("실명의 개인")
                    .maxLimit(100_000_000L)
                    .isActive(true)
                    .fetchedAt(LocalDateTime.now())
                    .build();

            Product saved = productRepository.save(product);

            ProductOption option = ProductOption.builder()
                    .product(saved)
                    .saveTrm(spec.saveTrm())
                    .intrRateType(spec.rateType())
                    .intrRate(spec.intrRate())
                    .intrRate2(spec.intrRate2())
                    .build();

            // ProductOption은 Product.options에 추가 후 cascade로 저장
            // 또는 별도 ProductOptionRepository를 주입해서 직접 저장
            // 여기서는 간단하게 product.getOptions().add(option) 패턴 사용
            saved.getOptions().add(option);
            productRepository.save(saved);

            log.info("[FssApiClient] 더미 상품 저장: {} - {}", spec.korCoNm(), spec.finPrdtNm());
        }
    }

    /** 더미 데이터 스펙 레코드 */
    private record DummyProductSpec(
            String finPrdtCd,
            String korCoNm,
            String finPrdtNm,
            int saveTrm,
            BigDecimal intrRate,
            BigDecimal intrRate2,
            RateType rateType
    ) {}
}