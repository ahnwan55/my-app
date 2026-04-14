package com.example.demo.domain.recommendation.service;

import com.example.demo.domain.persona.entity.PersonaCode;
import com.example.demo.domain.product.dto.ProductDto;
import com.example.demo.domain.product.entity.Product;
import com.example.demo.domain.product.entity.ProductOption;
import com.example.demo.domain.product.repository.ProductRepository;
import com.example.demo.domain.recommendation.dto.RecommendationDto;
import com.example.demo.domain.recommendation.entity.RecommendRule;
import com.example.demo.domain.recommendation.repository.RecommendRuleRepository;
import com.example.demo.domain.survey.entity.SurveySession;
import com.example.demo.domain.survey.repository.SurveySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * RecommendationService — 상품 추천 비즈니스 로직
 *
 * 추천 흐름:
 *   1. 완료된 세션 조회 → 페르소나 코드 추출
 *   2. 페르소나에 해당하는 RecommendRule 조회 (우선순위 순)
 *   3. 전체 활성 상품 조회
 *   4. 각 규칙 기준으로 상품 필터링 → 점수 계산 → 정렬
 *   5. 상위 N개 반환
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final SurveySessionRepository surveySessionRepository;
    private final RecommendRuleRepository recommendRuleRepository;
    private final ProductRepository productRepository;

    // 최대 추천 상품 수
    private static final int MAX_RECOMMENDATIONS = 5;

    /**
     * 설문 세션 UUID를 기반으로 맞춤 상품을 추천합니다.
     *
     * @param sessionUuid 완료된 설문 세션의 UUID
     * @throws IllegalArgumentException 세션이 없거나 미완료 상태일 때
     * @throws IllegalStateException    페르소나가 결정되지 않은 세션일 때
     */
    public RecommendationDto.RecommendResponse getRecommendations(String sessionUuid) {
        // 1. 완료된 세션만 조회 (미완료 세션으로 추천 요청 시 예외)
        SurveySession session = surveySessionRepository.findCompletedByUuid(sessionUuid)
                .orElseThrow(() -> new IllegalArgumentException(
                        "완료된 세션을 찾을 수 없습니다. 설문을 먼저 완료해주세요: " + sessionUuid));

        if (session.getPersonaType() == null) {
            throw new IllegalStateException("페르소나가 결정되지 않은 세션입니다.");
        }

        PersonaCode personaCode = session.getPersonaType().getCode();
        String personaName = session.getPersonaType().getName();

        // 2. 해당 페르소나의 추천 규칙 조회 (우선순위 오름차순)
        List<RecommendRule> rules = recommendRuleRepository
                .findByPersonaCodeOrderByPriority(personaCode);

        // 3. 전체 활성 상품 조회 (FETCH JOIN으로 N+1 방지)
        List<Product> allProducts = productRepository.findAllActiveWithOptions();

        // 4. 규칙 기반으로 상품 점수 계산 및 필터링
        List<ScoredProduct> scoredProducts = scoreProducts(allProducts, rules);

        // 5. 점수 내림차순 정렬 → 상위 MAX_RECOMMENDATIONS개 추출
        List<ScoredProduct> topProducts = scoredProducts.stream()
                .sorted(Comparator.comparingDouble(ScoredProduct::score).reversed())
                .limit(MAX_RECOMMENDATIONS)
                .toList();

        // 6. 응답 DTO 조립
        List<RecommendationDto.RankedProduct> rankedProducts = new ArrayList<>();
        for (int i = 0; i < topProducts.size(); i++) {
            ScoredProduct sp = topProducts.get(i);
            rankedProducts.add(RecommendationDto.RankedProduct.builder()
                    .rank(i + 1)
                    .product(ProductDto.ProductResponse.of(sp.product()))
                    .matchReason(buildMatchReason(sp))
                    .build());
        }

        return RecommendationDto.RecommendResponse.builder()
                .personaCode(personaCode)
                .personaName(personaName)
                .reason(buildOverallReason(personaCode))
                .products(rankedProducts)
                .build();
    }

    /**
     * 규칙 기반으로 각 상품에 점수를 부여합니다.
     *
     * 점수 계산 방식:
     *   - 각 규칙에 대해 상품이 조건을 만족하면 (1.0 / 규칙 우선순위) 점수 추가
     *   - 우선순위가 높은 규칙(숫자가 낮은)을 만족할수록 더 높은 점수
     *   - 하나의 상품이 여러 규칙을 만족하면 점수 누적
     *   - 어떤 규칙도 만족하지 않는 상품(score=0)은 결과에서 제외
     *
     * @param products 전체 활성 상품
     * @param rules    해당 페르소나의 추천 규칙 목록
     */
    private List<ScoredProduct> scoreProducts(List<Product> products, List<RecommendRule> rules) {
        List<ScoredProduct> result = new ArrayList<>();

        for (Product product : products) {
            double score = 0.0;
            ProductOption bestOption = null;

            for (RecommendRule rule : rules) {
                // 상품 유형 불일치 → 이 규칙 스킵
                if (!product.getProductType().equals(rule.getProductType())) {
                    continue;
                }

                // 상품의 옵션 중 이 규칙에 매칭되는 옵션 탐색
                for (ProductOption option : product.getOptions()) {
                    if (matchesRule(option, rule)) {
                        // 규칙 우선순위(priority)가 낮을수록 높은 점수
                        double ruleScore = 1.0 / rule.getPriority();
                        score += ruleScore;
                        if (bestOption == null) {
                            bestOption = option;  // 첫 번째 매칭 옵션을 "대표 옵션"으로 저장
                        }
                    }
                }
            }

            // 최소 1개 규칙이라도 만족한 상품만 포함
            if (score > 0) {
                result.add(new ScoredProduct(product, bestOption, score));
            }
        }

        return result;
    }

    /**
     * ProductOption이 RecommendRule 조건을 만족하는지 확인합니다.
     *
     * 조건:
     *   - 금리 유형 일치 (단리/복리)
     *   - 기간이 [minTermMonths, maxTermMonths] 범위 이내
     *   - 기본 금리가 minRate 이상
     */
    private boolean matchesRule(ProductOption option, RecommendRule rule) {
        // 금리 유형 확인
        if (!option.getIntrRateType().equals(rule.getRateType())) {
            return false;
        }

        // 기간 범위 확인
        int term = option.getSaveTrm();
        if (term < rule.getMinTermMonths() || term > rule.getMaxTermMonths()) {
            return false;
        }

        // 기본 금리 확인 (null이면 조건 없음으로 통과)
        if (rule.getMinRate() != null && option.getIntrRate() != null) {
            if (option.getIntrRate().compareTo(rule.getMinRate()) < 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * 개별 상품에 대한 매칭 이유 문자열을 생성합니다.
     * 예: "12개월 복리, 기본금리 3.50% / 우대금리 4.00%"
     */
    private String buildMatchReason(ScoredProduct sp) {
        if (sp.bestOption() == null) {
            return "조건에 맞는 상품입니다.";
        }
        ProductOption opt = sp.bestOption();
        String rateTypeLabel = switch (opt.getIntrRateType()) {
            case S -> "단리";
            case M -> "복리";
        };

        StringBuilder sb = new StringBuilder();
        sb.append(opt.getSaveTrm()).append("개월 ").append(rateTypeLabel);

        if (opt.getIntrRate() != null) {
            sb.append(", 기본금리 ").append(opt.getIntrRate()).append("%");
        }
        if (opt.getIntrRate2() != null) {
            sb.append(" / 우대금리 ").append(opt.getIntrRate2()).append("%");
        }
        return sb.toString();
    }

    /**
     * 페르소나별 전체 추천 이유 문구를 반환합니다.
     */
    private String buildOverallReason(PersonaCode code) {
        return switch (code) {
            case SAFETY_GUARD -> "안정성을 최우선으로 하는 성향에 맞게 복리 방식의 안전한 상품을 추천드립니다.";
            case STEADY_WORKER -> "꾸준한 저축을 선호하는 성향에 맞게 장기 정기적금 상품을 추천드립니다.";
            case BALANCED_SPENDER -> "소비와 저축의 균형을 추구하는 성향에 맞게 유연한 가입 조건의 상품을 추천드립니다.";
            case RATE_OPTIMIZER -> "최고 금리를 추구하는 성향에 맞게 우대금리 조건이 유리한 상품을 추천드립니다.";
            case GOAL_ACHIEVER -> "목표 달성을 위한 단기 집중 저축에 적합한 상품을 추천드립니다.";
            case FUTURE_PLANNER -> "장기적인 재무 설계를 위한 복리 효과가 극대화되는 상품을 추천드립니다.";
        };
    }

    /**
     * 점수가 계산된 상품 + 대표 옵션을 담는 내부 레코드
     *
     * Java 16+ record: 불변 데이터 전달 객체를 간결하게 선언하는 방법.
     * Lombok의 @Value와 유사하지만 언어 수준에서 지원됩니다.
     * Service 내부에서만 사용하는 중간 계산 결과이므로 private으로 선언합니다.
     */
    private record ScoredProduct(Product product, ProductOption bestOption, double score) {}
}