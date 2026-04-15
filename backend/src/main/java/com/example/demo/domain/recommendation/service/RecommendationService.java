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
import com.example.demo.infra.ai.AiServerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final SurveySessionRepository surveySessionRepository;
    private final RecommendRuleRepository recommendRuleRepository;
    private final ProductRepository productRepository;
    private final AiServerClient aiServerClient;

    private static final int MAX_RECOMMENDATIONS = 5;

    public RecommendationDto.RecommendResponse getRecommendations(String sessionUuid) {
        // 1. 완료된 세션 조회
        SurveySession session = surveySessionRepository.findCompletedByUuid(sessionUuid)
                .orElseThrow(() -> new IllegalArgumentException(
                        "완료된 세션을 찾을 수 없습니다. 설문을 먼저 완료해주세요: " + sessionUuid));

        if (session.getPersonaType() == null) {
            throw new IllegalStateException("페르소나가 결정되지 않은 세션입니다.");
        }

        PersonaCode personaCode = session.getPersonaType().getCode();
        String personaName = session.getPersonaType().getName();

        // 2. 해당 페르소나의 추천 룰 조회
        List<RecommendRule> rules = recommendRuleRepository
                .findByPersonaCodeOrderByPriority(personaCode);

        // 3. 전체 활성 상품 조회
        List<Product> allProducts = productRepository.findAllActiveWithOptions();

        // 4. 룰 기반 점수 계산 및 정렬
        List<ScoredProduct> scoredProducts = scoreProducts(allProducts, rules);

        // 5. 상위 N개 추출
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

        // 7. FastAPI AI 추천 호출
        // AI 서버 장애 시 aiComment = null로 폴백 - 룰 기반 추천은 항상 동작
        String aiComment = null;
        try {
            Map<String, Object> profile = new HashMap<>();
            profile.put("age", 20);        // 추후 실제 유저 정보로 교체
            profile.put("job", "직장인");
            profile.put("income", 300);
            profile.put("goal", personaName);
            profile.put("risk_type", personaCode.name());

            List<Map<String, Object>> productList = topProducts.stream()
                    .map(sp -> {
                        Map<String, Object> p = new HashMap<>();
                        p.put("name", sp.product().getFinPrdtNm());
                        p.put("bank", sp.product().getKorCoNm());
                        p.put("interest_rate", sp.bestOption() != null ?
                                sp.bestOption().getIntrRate() : 0);
                        p.put("period_months", sp.bestOption() != null ?
                                sp.bestOption().getSaveTrm() : 0);
                        return p;
                    })
                    .toList();

            aiComment = aiServerClient.recommend(profile, productList);
        } catch (Exception e) {
            log.warn("[RecommendationService] AI 추천 호출 실패: {}", e.getMessage());
        }

        return RecommendationDto.RecommendResponse.builder()
                .personaCode(personaCode)
                .personaName(personaName)
                .reason(buildOverallReason(personaCode))
                .aiComment(aiComment)
                .products(rankedProducts)
                .build();
    }

    private List<ScoredProduct> scoreProducts(List<Product> products, List<RecommendRule> rules) {
        List<ScoredProduct> result = new ArrayList<>();

        for (Product product : products) {
            double score = 0.0;
            ProductOption bestOption = null;

            for (RecommendRule rule : rules) {
                if (!product.getProductType().equals(rule.getProductType())) {
                    continue;
                }

                for (ProductOption option : product.getOptions()) {
                    if (matchesRule(option, rule)) {
                        double ruleScore = 1.0 / rule.getPriority();
                        score += ruleScore;
                        if (bestOption == null) {
                            bestOption = option;
                        }
                    }
                }
            }

            if (score > 0) {
                result.add(new ScoredProduct(product, bestOption, score));
            }
        }

        return result;
    }

    private boolean matchesRule(ProductOption option, RecommendRule rule) {
        if (!option.getIntrRateType().equals(rule.getRateType())) {
            return false;
        }

        int term = option.getSaveTrm();
        if (term < rule.getMinTermMonths() || term > rule.getMaxTermMonths()) {
            return false;
        }

        if (rule.getMinRate() != null && option.getIntrRate() != null) {
            if (option.getIntrRate().compareTo(rule.getMinRate()) < 0) {
                return false;
            }
        }

        return true;
    }

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
            sb.append(" / 최고금리 ").append(opt.getIntrRate2()).append("%");
        }
        return sb.toString();
    }

    private String buildOverallReason(PersonaCode code) {
        return switch (code) {
            case SAFETY_GUARD -> "안정성을 중시하는 성향으로 복리 방식의 안전한 상품을 추천드립니다.";
            case STEADY_WORKER -> "꾸준한 저축을 선호하는 성향으로 적립식 예적금 상품을 추천드립니다.";
            case BALANCED_SPENDER -> "소비와 저축의 균형을 원하는 성향으로 적절한 조건의 상품을 추천드립니다.";
            case RATE_OPTIMIZER -> "높은 금리를 원하는 성향으로 최고금리 조건이 좋은 상품을 추천드립니다.";
            case GOAL_ACHIEVER -> "목표 달성을 위한 단기 집중 저축에 맞는 상품을 추천드립니다.";
            case FUTURE_PLANNER -> "장기적인 재무 계획을 위한 복리 효과가 높은 상품을 추천드립니다.";
        };
    }

    private record ScoredProduct(Product product, ProductOption bestOption, double score) {}
}