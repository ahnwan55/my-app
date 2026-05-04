package com.example.demo.domain.recommendation.service;

import com.example.demo.auth.entity.User;
import com.example.demo.auth.repository.UserRepository;
import com.example.demo.domain.book.dto.BookDto;
import com.example.demo.domain.persona.entity.PersonaCode;
import com.example.demo.domain.recommendation.dto.RecommendationDto;
import com.example.demo.domain.survey.entity.PersonaAnalysis;
import com.example.demo.domain.survey.repository.PersonaAnalysisRepository;
import com.example.demo.infra.ai.AiServerClient;
import com.example.demo.infra.ai.BedrockClient;
import com.example.demo.infra.kakao.KakaoBookClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RecommendationService - 도서 추천 비즈니스 로직
 *
 * [변경 사항]
 *   - KDC 코드 기반 DB 조회 → 카카오 도서 검색 API 기반으로 교체
 *   - 페르소나별 검색 키워드를 카카오 API에 전달해 도서 목록을 실시간으로 가져온다.
 *   - DB에 도서 데이터가 부족해도 항상 풍부한 추천 결과를 제공할 수 있다.
 *
 * [추천 흐름]
 *   1. userId로 사용자 조회
 *   2. 가장 최신 PersonaAnalysis 조회 → 페르소나 확인
 *   3. 페르소나 → 카카오 검색 키워드 결정
 *   4. KakaoBookClient로 도서 목록 조회
 *   5. 상위 N권 선정
 *   6. Bedrock 추천 코멘트 요청 (실패 시 null로 폴백)
 *   7. 응답 DTO 구성
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final UserRepository userRepository;
    private final PersonaAnalysisRepository personaAnalysisRepository;
    private final KakaoBookClient kakaoBookClient;
    private final BedrockClient bedrockClient;
    private final AiServerClient aiServerClient;

    private static final int MAX_RECOMMENDATIONS = 5;

    public RecommendationDto.RecommendResponse getRecommendations(Long userId) {

        // 1. 사용자 조회
        User user = userRepository.findByKakaoId(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "사용자를 찾을 수 없습니다: " + userId));

        // 2. 가장 최신 PersonaAnalysis 조회
        PersonaAnalysis analysis = personaAnalysisRepository
                .findTopByUserOrderByAnalyzedAtDesc(user)
                .orElseThrow(() -> new IllegalStateException(
                        "설문 분석 결과가 없습니다. 먼저 설문을 완료해주세요."));

        String personaCodeStr = analysis.getPersonaType().getCode();
        String personaName    = analysis.getPersonaType().getName();
        PersonaCode personaCode = PersonaCode.valueOf(personaCodeStr);

        // 3. 페르소나 → 카카오 검색 키워드 결정
        String searchQuery = resolveSearchQuery(personaCode);
        log.info("[RecommendationService] 페르소나={}, 검색키워드={}", personaCode, searchQuery);

        // 4. 카카오 도서 검색 API 호출 (후보군을 넉넉히 20권으로 확대)
        List<BookDto.BookResponse> books = kakaoBookClient.search(searchQuery, MAX_RECOMMENDATIONS * 4);

        // 카카오 API 실패 시 빈 리스트 처리
        if (books.isEmpty()) {
            log.warn("[RecommendationService] 카카오 API 결과 없음. persona={}, query={}", personaCode, searchQuery);
        }

        // 5. SRoBERTa 임베딩 및 유사도 기반 재정렬 (Re-ranking)
        List<BookDto.BookResponse> topBooks;
        try {
            // 사용자 벡터 계산 (페르소나 판정 이유 텍스트를 활용)
            String userContext = analysis.getPersonaReason() != null ? analysis.getPersonaReason() : personaName;
            List<Float> userVector = aiServerClient.embed(userContext);

            // 후보 도서 텍스트 구성 및 일괄 임베딩
            List<String> bookTexts = books.stream()
                    .map(b -> b.getTitle() + " " + b.getAuthor() + " " + b.getDescription())
                    .toList();
            List<List<Float>> bookVectors = aiServerClient.embedBatch(bookTexts);

            // 코사인 유사도 계산 및 정렬
            record BookWithScore(BookDto.BookResponse book, double score) {}
            List<BookWithScore> scoredBooks = new ArrayList<>();
            for (int i = 0; i < books.size(); i++) {
                double score = cosineSimilarity(userVector, bookVectors.get(i));
                scoredBooks.add(new BookWithScore(books.get(i), score));
            }

            // 유사도 내림차순 정렬 후 상위 5권 추출
            scoredBooks.sort((b1, b2) -> Double.compare(b2.score(), b1.score()));
            topBooks = scoredBooks.stream()
                    .limit(MAX_RECOMMENDATIONS)
                    .map(BookWithScore::book)
                    .toList();

        } catch (Exception e) {
            log.warn("[RecommendationService] SRoBERTa 임베딩/유사도 계산 실패. 기존 순서대로 반환: {}", e.getMessage());
            topBooks = books.stream().limit(MAX_RECOMMENDATIONS).toList();
        }

        // 6. 추천 도서 DTO 구성
        List<RecommendationDto.RankedBook> rankedBooks = new ArrayList<>();
        for (int i = 0; i < topBooks.size(); i++) {
            rankedBooks.add(RecommendationDto.RankedBook.builder()
                    .rank(i + 1)
                    .book(topBooks.get(i))
                    .matchReason(buildMatchReason(personaCode))
                    .build());
        }

        // 7. Bedrock 추천 코멘트 요청 (실패 시 null로 폴백)
        String aiComment = null;
        try {
            Map<String, Object> profile = new HashMap<>();
            profile.put("persona_code", personaCode.name());
            profile.put("persona_name", personaName);

            List<Map<String, Object>> bookList = topBooks.stream()
                    .map(book -> {
                        Map<String, Object> b = new HashMap<>();
                        b.put("title", book.getTitle());
                        b.put("authors", book.getAuthor());
                        return b;
                    })
                    .toList();

            aiComment = bedrockClient.generateRecommendComment(profile, bookList);
        } catch (Exception e) {
            log.warn("[RecommendationService] Bedrock 추천 코멘트 요청 실패: {}", e.getMessage());
        }

        return RecommendationDto.RecommendResponse.builder()
                .personaCode(personaCode)
                .personaName(personaName)
                .reason(buildOverallReason(personaCode))
                .aiComment(aiComment)
                .books(rankedBooks)
                .build();
    }

    // ── 페르소나 → 카카오 검색 키워드 매핑 ──────────────────────────────

    private String resolveSearchQuery(PersonaCode personaCode) {
        return switch (personaCode) {
            case TREND_SURFER        -> "트렌드 교양 과학";
            case POLYMATH_SEEKER     -> "융합 인문 철학";
            case AESTHETIC_COLLECTOR -> "에세이 예술 감성";
            case KNOWLEDGE_EDITOR    -> "지식 큐레이션 인문";
            case FAST_SOLVER         -> "비즈니스 실용 자기계발";
            case CAREER_STRATEGIST   -> "경영 리더십 전략";
            case EMOTIONAL_SYNCHRO   -> "감성 소설 문학";
            case CASUAL_RESTER       -> "힐링 에세이 단편소설";
            case COLD_CRITIC         -> "사회과학 비판 논쟁";
            case SILENT_RESEARCHER   -> "학술 교양 고전";
            case CONTEMPLATIVE_MONK  -> "철학 사상 명상";
            case OBSESSIVE_FANDOM    -> "시리즈 소설 판타지";
        };
    }

    // ── 추천 이유 생성 ────────────────────────────────────────────────────

    private String buildMatchReason(PersonaCode personaCode) {
        return switch (personaCode) {
            case TREND_SURFER        -> "최신 트렌드와 새로운 분야를 빠르게 접할 수 있는 도서입니다.";
            case POLYMATH_SEEKER     -> "여러 분야를 깊게 연결하는 지적 탐구에 적합한 도서입니다.";
            case AESTHETIC_COLLECTOR -> "심미적 감각과 다양한 장르를 즐기는 분께 어울리는 도서입니다.";
            case KNOWLEDGE_EDITOR    -> "정보를 체계적으로 정리하고 공유하는 데 도움이 되는 도서입니다.";
            case FAST_SOLVER         -> "핵심만 빠르게 파악하고 바로 실천할 수 있는 도서입니다.";
            case CAREER_STRATEGIST   -> "커리어와 자기계발을 위한 깊이 있는 인사이트를 담은 도서입니다.";
            case EMOTIONAL_SYNCHRO   -> "깊은 감정 몰입과 서사적 여운을 느낄 수 있는 도서입니다.";
            case CASUAL_RESTER       -> "가볍게 읽으며 일상의 쉼을 찾을 수 있는 도서입니다.";
            case COLD_CRITIC         -> "논리적 구조와 비판적 시각을 키울 수 있는 도서입니다.";
            case SILENT_RESEARCHER   -> "혼자 조용히 텍스트의 이면을 탐구하기 좋은 도서입니다.";
            case CONTEMPLATIVE_MONK  -> "깊이 사유하고 성찰할 수 있는 도서입니다.";
            case OBSESSIVE_FANDOM    -> "한 작가·장르를 깊이 파고드는 분께 어울리는 도서입니다.";
        };
    }

    private String buildOverallReason(PersonaCode personaCode) {
        return switch (personaCode) {
            case TREND_SURFER        -> "최신 트렌드에 민감하고 새로운 분야를 빠르게 탐색하는 성향으로, 최신 교양서와 트렌드 과학서를 추천드립니다.";
            case POLYMATH_SEEKER     -> "다양한 분야를 깊게 연결하는 성향으로, 학제 간 융합서와 철학적 과학서를 추천드립니다.";
            case AESTHETIC_COLLECTOR -> "심미적 관점으로 다양한 장르를 선별하는 성향으로, 에세이와 예술서를 추천드립니다.";
            case KNOWLEDGE_EDITOR    -> "정보를 체계적으로 정리하는 성향으로, 지식 큐레이션 에세이와 인문 교양서를 추천드립니다.";
            case FAST_SOLVER         -> "실용적이고 빠른 독서 성향으로, 비즈니스 속성 가이드와 핵심 요약서를 추천드립니다.";
            case CAREER_STRATEGIST   -> "계획적인 자기계발 성향으로, 경영전략서와 리더십 도서를 추천드립니다.";
            case EMOTIONAL_SYNCHRO   -> "깊은 감성 몰입 성향으로, 장편 소설과 서사 중심 논픽션을 추천드립니다.";
            case CASUAL_RESTER       -> "가벼운 힐링 독서 성향으로, 베스트셀러 에세이와 짧은 단편 소설을 추천드립니다.";
            case COLD_CRITIC         -> "비판적 분석 성향으로, 사회과학서와 논쟁적 인문서를 추천드립니다.";
            case SILENT_RESEARCHER   -> "혼자 깊이 탐구하는 성향으로, 학술 교양서와 원전 번역서를 추천드립니다.";
            case CONTEMPLATIVE_MONK  -> "깊이 사유하는 성향으로, 철학 고전과 종교·사상서를 추천드립니다.";
            case OBSESSIVE_FANDOM    -> "한 작가·시리즈에 집중하는 성향으로, 시리즈물과 작가별 전집을 추천드립니다.";
        };
    }

    // ── 코사인 유사도 계산 ────────────────────────────────────────────────
    
    private double cosineSimilarity(List<Float> vectorA, List<Float> vectorB) {
        if (vectorA == null || vectorB == null || vectorA.size() != vectorB.size()) return 0.0;
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }
        if (normA == 0.0 || normB == 0.0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
