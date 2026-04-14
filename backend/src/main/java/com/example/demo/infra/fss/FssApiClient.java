package com.example.demo.infra.fss;

import com.example.demo.domain.product.entity.Product;
import com.example.demo.domain.product.entity.ProductOption;
import com.example.demo.domain.product.entity.ProductType;
import com.example.demo.domain.product.entity.RateType;
import com.example.demo.domain.product.repository.ProductRepository;
import com.example.demo.infra.fss.dto.FssApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FssApiClient — 금융감독원(FSS) Open API 연동 클라이언트
 *
 * 예금/적금 상품 데이터를 금융감독원 API에서 가져와 DB에 저장합니다.
 * fin_prdt_cd(상품 코드)를 기준으로 upsert(있으면 update, 없으면 insert)합니다.
 *
 * API 엔드포인트:
 *   예금: GET /finlifeapi/depositProductsSearch.json
 *   적금: GET /finlifeapi/savingProductsSearch.json
 *
 * @Profile("!test"): 테스트 환경에서는 이 Bean이 등록되지 않습니다.
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class FssApiClient {

    private final ProductRepository productRepository;
    private final WebClient.Builder webClientBuilder;

    /**
     * application-local.yml의 fss.api-key 값을 주입합니다.
     *
     * ⚠️ 보안 주의사항:
     *   - application.yml에 키를 직접 입력하면 GitHub에 노출됩니다.
     *   - application-local.yml은 .gitignore에 등록되어 있으므로 여기에 입력하세요.
     *   - 운영 환경: AWS Secrets Manager 또는 K8s Secret → 환경변수로 주입
     *               application.yml: fss.api-key: ${FSS_API_KEY}
     */
    @Value("${fss.api-key:NOT_SET}")
    private String apiKey;

    private static final String FSS_BASE_URL = "https://finlife.fss.or.kr/finlifeapi";
    private static final String TOPFIN_CD = "020000";  // 은행권 코드

    /**
     * 예금 상품을 수집하여 DB에 저장합니다.
     */
    public void fetchAndSaveDeposits() {
        if ("NOT_SET".equals(apiKey)) {
            log.warn("[FssApiClient] API 키가 설정되지 않았습니다. 예금 상품 수집을 건너뜁니다.");
            saveDummyProducts(ProductType.DEPOSIT);
            return;
        }
        log.info("[FssApiClient] 예금 상품 수집 시작");
        fetchAndSave("depositProductsSearch.json", ProductType.DEPOSIT);
    }

    /**
     * 적금 상품을 수집하여 DB에 저장합니다.
     */
    public void fetchAndSaveSavings() {
        if ("NOT_SET".equals(apiKey)) {
            log.warn("[FssApiClient] API 키가 설정되지 않았습니다. 적금 상품 수집을 건너뜁니다.");
            saveDummyProducts(ProductType.SAVING);
            return;
        }
        log.info("[FssApiClient] 적금 상품 수집 시작");
        fetchAndSave("savingProductsSearch.json", ProductType.SAVING);
    }

    /**
     * 금융감독원 API를 호출하고 응답 데이터를 DB에 저장합니다.
     *
     * @param path        API 경로
     * @param productType 상품 유형 (DEPOSIT / SAVING)
     */
    private void fetchAndSave(String path, ProductType productType) {
        try {
            WebClient client = webClientBuilder.baseUrl(FSS_BASE_URL).build();

            // block(): 스케줄러에서 호출하므로 동기 방식 사용
            FssApiResponse response = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/" + path)
                            .queryParam("auth", apiKey)
                            .queryParam("topFinGrpNo", TOPFIN_CD)
                            .queryParam("pageNo", 1)
                            .build())
                    .retrieve()
                    .bodyToMono(FssApiResponse.class)
                    .block();

            if (response == null || response.getResult() == null) {
                log.error("[FssApiClient] {} API 응답이 null입니다.", path);
                return;
            }

            if (!response.getResult().isSuccess()) {
                log.error("[FssApiClient] {} API 오류: {} - {}",
                        path, response.getResult().getErrCd(), response.getResult().getErrMsg());
                return;
            }

            List<FssApiResponse.BaseItem> baseList = response.getResult().getBaseList();
            List<FssApiResponse.OptionItem> optionList = response.getResult().getOptionList();

            if (baseList == null || baseList.isEmpty()) {
                log.warn("[FssApiClient] {} 응답에 상품 데이터가 없습니다.", path);
                return;
            }

            // fin_prdt_cd → 옵션 목록 Map 생성 (O(1) 탐색)
            // 하나의 상품에 여러 기간 옵션이 있으므로 groupingBy 사용
            Map<String, List<FssApiResponse.OptionItem>> optionMap = optionList.stream()
                    .collect(Collectors.groupingBy(FssApiResponse.OptionItem::getFinPrdtCd));

            int savedCount = 0;
            int updatedCount = 0;

            for (FssApiResponse.BaseItem base : baseList) {
                List<FssApiResponse.OptionItem> options =
                        optionMap.getOrDefault(base.getFinPrdtCd(), List.of());

                boolean isNew = productRepository.findByFinPrdtCd(base.getFinPrdtCd()).isEmpty();
                upsertProduct(base, options, productType);

                if (isNew) savedCount++;
                else updatedCount++;
            }

            log.info("[FssApiClient] {} 수집 완료 — 신규: {}개, 업데이트: {}개",
                    productType, savedCount, updatedCount);

        } catch (Exception e) {
            log.error("[FssApiClient] {} 수집 중 오류: {}", path, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 상품을 upsert합니다.
     * fin_prdt_cd가 이미 있으면 기존 Product의 옵션만 갱신합니다.
     * ProductOption은 매번 초기화 후 재삽입하여 금리 변경을 반영합니다.
     */
    private void upsertProduct(FssApiResponse.BaseItem base,
                               List<FssApiResponse.OptionItem> optionItems,
                               ProductType productType) {
        Product product = productRepository.findByFinPrdtCd(base.getFinPrdtCd())
                .orElseGet(() -> Product.builder()
                        .finPrdtCd(base.getFinPrdtCd())
                        .korCoNm(base.getKorCoNm())
                        .finPrdtNm(base.getFinPrdtNm())
                        .productType(productType)
                        .joinWay(base.getJoinWay())
                        .spclCnd(base.getSpclCnd())
                        .joinDeny(base.getJoinDeny())
                        .joinMember(base.getJoinMember())
                        .maxLimit(base.getMaxLimit())
                        .dclsMonth(base.getDclsMonth())
                        .isActive(true)
                        .fetchedAt(LocalDateTime.now())
                        .build());

        // 기존 옵션 제거 후 재삽입 (금리 변경 반영)
        product.getOptions().clear();

        for (FssApiResponse.OptionItem optionItem : optionItems) {
            RateType rateType = "M".equals(optionItem.getIntrRateType()) ? RateType.M : RateType.S;

            // save_trm이 문자열로 오므로 Integer로 변환
            int saveTrm;
            try {
                saveTrm = Integer.parseInt(optionItem.getSaveTrm());
            } catch (NumberFormatException e) {
                log.warn("[FssApiClient] 잘못된 저축 기간 값: {} (상품: {})",
                        optionItem.getSaveTrm(), base.getFinPrdtCd());
                continue;
            }

            ProductOption option = ProductOption.builder()
                    .product(product)
                    .intrRateType(rateType)
                    .saveTrm(saveTrm)
                    .intrRate(optionItem.getIntrRate())
                    .intrRate2(optionItem.getIntrRate2())
                    .build();

            product.getOptions().add(option);
        }

        productRepository.save(product);
        log.debug("[FssApiClient] 상품 저장: {} - {}", product.getKorCoNm(), product.getFinPrdtNm());
    }

    /**
     * 개발/테스트용 더미 상품 데이터를 DB에 삽입합니다.
     * API 키 없이도 추천 로직을 테스트할 수 있도록 샘플 데이터를 제공합니다.
     */
    private void saveDummyProducts(ProductType type) {
        record DummySpec(String code, String bank, String name, int term,
                         BigDecimal rate, BigDecimal rate2, RateType rateType) {}

        List<DummySpec> specs = switch (type) {
            case DEPOSIT -> List.of(
                    new DummySpec("DEPOSIT_001", "국민은행", "KB Star 정기예금",       12, new BigDecimal("3.50"), new BigDecimal("4.00"), RateType.S),
                    new DummySpec("DEPOSIT_002", "신한은행", "신한 My Dream 정기예금", 24, new BigDecimal("3.20"), new BigDecimal("3.80"), RateType.M),
                    new DummySpec("DEPOSIT_003", "하나은행", "하나 정기예금",            6, new BigDecimal("3.70"), new BigDecimal("4.10"), RateType.S)
            );
            case SAVING -> List.of(
                    new DummySpec("SAVING_001", "국민은행", "KB 직장인 우대 적금",   12, new BigDecimal("4.00"), new BigDecimal("5.00"), RateType.M),
                    new DummySpec("SAVING_002", "우리은행", "우리 첫거래 우대 적금",  6, new BigDecimal("3.80"), new BigDecimal("4.50"), RateType.S),
                    new DummySpec("SAVING_003", "농협은행", "NH 올원 e적금",         24, new BigDecimal("3.50"), new BigDecimal("4.20"), RateType.M)
            );
        };

        for (DummySpec spec : specs) {
            if (productRepository.findByFinPrdtCd(spec.code()).isPresent()) {
                log.debug("[FssApiClient] 더미 상품 이미 존재, 스킵: {}", spec.code());
                continue;
            }

            Product product = Product.builder()
                    .finPrdtCd(spec.code())
                    .korCoNm(spec.bank())
                    .finPrdtNm(spec.name())
                    .productType(type)
                    .joinWay("인터넷, 영업점")
                    .spclCnd("급여이체 시 우대금리 0.2%p 추가")
                    .joinDeny("1")
                    .joinMember("실명의 개인")
                    .maxLimit(100_000_000L)
                    .isActive(true)
                    .fetchedAt(LocalDateTime.now())
                    .build();

            product = productRepository.save(product);

            ProductOption option = ProductOption.builder()
                    .product(product)
                    .saveTrm(spec.term())
                    .intrRateType(spec.rateType())
                    .intrRate(spec.rate())
                    .intrRate2(spec.rate2())
                    .build();

            product.getOptions().add(option);
            productRepository.save(product);

            log.info("[FssApiClient] 더미 상품 저장: {} - {}", spec.bank(), spec.name());
        }
    }
}