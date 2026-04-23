package com.example.demo.domain.recommendation.service;

import com.example.demo.auth.entity.User;
import com.example.demo.auth.repository.UserRepository;
import com.example.demo.domain.book.dto.BookDto;
import com.example.demo.domain.book.entity.Book;
import com.example.demo.domain.book.repository.BookRepository;
import com.example.demo.domain.persona.entity.PersonaCode;
import com.example.demo.domain.recommendation.dto.RecommendationDto;
import com.example.demo.domain.survey.entity.PersonaAnalysis;
import com.example.demo.domain.survey.repository.PersonaAnalysisRepository;
import com.example.demo.infra.ai.BedrockClient;
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
 * 변경 사항:
 *   - SurveySession 제거 → userId 기반 최신 PersonaAnalysis 조회로 교체
 *   - BookGenre 제거 → KDC 코드 기반 장르 매핑으로 교체
 *   - PersonaCode 6종 → 12종 switch로 교체
 *   - bedrockClient.recommend() → generateRecommendComment()로 교체
 *
 * 추천 흐름:
 *   1. userId로 사용자 조회
 *   2. 가장 최신 PersonaAnalysis 조회 → 페르소나 확인
 *   3. 페르소나에 맞는 KDC 코드 목록 결정
 *   4. 해당 KDC 도서 DB 조회
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
    private final BookRepository bookRepository;
    private final BedrockClient bedrockClient;

    private static final int MAX_RECOMMENDATIONS = 5;

    /**
     * 사용자 맞춤 도서 추천
     *
     * @param userId 로그인한 사용자 ID
     * @return 페르소나 기반 추천 도서 목록 + AI 코멘트
     */
    public RecommendationDto.RecommendResponse getRecommendations(Long userId) {

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "사용자를 찾을 수 없습니다: " + userId));

        // 2. 가장 최신 PersonaAnalysis 조회
        PersonaAnalysis analysis = personaAnalysisRepository
                .findTopByUserOrderByAnalyzedAtDesc(user)
                .orElseThrow(() -> new IllegalStateException(
                        "설문 분석 결과가 없습니다. 먼저 설문을 완료해주세요."));

        // PersonaType에서 코드와 이름 추출
        String personaCodeStr = analysis.getPersonaType().getCode();
        String personaName = analysis.getPersonaType().getName();
        PersonaCode personaCode = PersonaCode.valueOf(personaCodeStr);

        // 3. 페르소나 → KDC 코드 목록 결정
        List<String> kdcPrefixes = resolveKdcPrefixes(personaCode);

        // 4. KDC 코드별 도서 조회 후 합산
        List<Book> candidates = new ArrayList<>();
        for (String kdc : kdcPrefixes) {
            candidates.addAll(bookRepository.findByKdcStartingWith(kdc));
        }

        // 도서가 없으면 전체 도서에서 추천
        if (candidates.isEmpty()) {
            log.warn("[RecommendationService] 페르소나 {} KDC 도서 없음, 전체 도서에서 추천", personaCode);
            candidates = bookRepository.findAll();
        }

        // 5. 상위 N권 선정
        List<Book> topBooks = candidates.stream()
                .limit(MAX_RECOMMENDATIONS)
                .toList();

        // 6. 추천 도서 DTO 구성
        List<RecommendationDto.RankedBook> rankedBooks = new ArrayList<>();
        for (int i = 0; i < topBooks.size(); i++) {
            Book book = topBooks.get(i);
            rankedBooks.add(RecommendationDto.RankedBook.builder()
                    .rank(i + 1)
                    .book(BookDto.BookResponse.of(book))
                    .matchReason(buildMatchReason(personaCode))
                    .build());
        }

        // 7. Bedrock 추천 코멘트 요청
        // Bedrock 장애 시 aiComment = null로 폴백 — 추천 자체는 항상 동작
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

    // ── KDC 코드 매핑 ──────────────────────────────────────────────────────

    /**
     * 페르소나별 KDC 코드 앞자리 목록 결정.
     * KDC 앞자리로 LIKE 검색하므로 상위 분류 코드를 사용한다.
     * 예: "813" → 한국소설, "840" → 영미소설, "100" → 철학
     */
    private List<String> resolveKdcPrefixes(PersonaCode personaCode) {
        return switch (personaCode) {
            // EXPLORER 계열: 지적 탐험 → 자연과학, 인문, 역사
            case TREND_SURFER      -> List.of("400", "300", "900");
            case POLYMATH_SEEKER   -> List.of("100", "400", "900");

            // CURATOR 계열: 심미적 수집 → 소설, 에세이, 예술
            case AESTHETIC_COLLECTOR -> List.of("813", "814", "600");
            case KNOWLEDGE_EDITOR    -> List.of("020", "300", "813");

            // NAVIGATOR 계열: 실용 지향 → 자기계발, 경영
            case FAST_SOLVER       -> List.of("325", "320", "190");
            case CAREER_STRATEGIST -> List.of("325", "320", "100");

            // DWELLER 계열: 감성 몰입 → 소설, 에세이, 힐링
            case EMOTIONAL_SYNCHRO -> List.of("813", "840", "814");
            case CASUAL_RESTER     -> List.of("814", "813", "810");

            // ANALYST 계열: 분석·비평 → 사회과학, 철학, 역사
            case COLD_CRITIC       -> List.of("300", "100", "900");
            case SILENT_RESEARCHER -> List.of("100", "900", "400");

            // DIVER 계열: 심층 탐구 → 철학, 종교, 역사
            case CONTEMPLATIVE_MONK -> List.of("100", "200", "900");
            case OBSESSIVE_FANDOM   -> List.of("813", "840", "808");
        };
    }

    // ── 추천 이유 생성 ────────────────────────────────────────────────────

    /**
     * 개별 도서 추천 이유 (서브 페르소나 12종 기준)
     */
    private String buildMatchReason(PersonaCode personaCode) {
        return switch (personaCode) {
            case TREND_SURFER       -> "최신 트렌드와 새로운 분야를 빠르게 접할 수 있는 도서입니다.";
            case POLYMATH_SEEKER    -> "여러 분야를 깊게 연결하는 지적 탐구에 적합한 도서입니다.";
            case AESTHETIC_COLLECTOR -> "심미적 감각과 다양한 장르를 즐기는 분께 어울리는 도서입니다.";
            case KNOWLEDGE_EDITOR   -> "정보를 체계적으로 정리하고 공유하는 데 도움이 되는 도서입니다.";
            case FAST_SOLVER        -> "핵심만 빠르게 파악하고 바로 실천할 수 있는 도서입니다.";
            case CAREER_STRATEGIST  -> "커리어와 자기계발을 위한 깊이 있는 인사이트를 담은 도서입니다.";
            case EMOTIONAL_SYNCHRO  -> "깊은 감정 몰입과 서사적 여운을 느낄 수 있는 도서입니다.";
            case CASUAL_RESTER      -> "가볍게 읽으며 일상의 쉼을 찾을 수 있는 도서입니다.";
            case COLD_CRITIC        -> "논리적 구조와 비판적 시각을 키울 수 있는 도서입니다.";
            case SILENT_RESEARCHER  -> "혼자 조용히 텍스트의 이면을 탐구하기 좋은 도서입니다.";
            case CONTEMPLATIVE_MONK -> "깊이 사유하고 성찰할 수 있는 도서입니다.";
            case OBSESSIVE_FANDOM   -> "한 작가·장르를 깊이 파고드는 분께 어울리는 도서입니다.";
        };
    }

    /**
     * 페르소나별 전체 추천 이유 (서브 페르소나 12종 기준)
     */
    private String buildOverallReason(PersonaCode personaCode) {
        return switch (personaCode) {
            case TREND_SURFER       -> "최신 트렌드에 민감하고 새로운 분야를 빠르게 탐색하는 성향으로, 최신 교양서와 트렌드 과학서를 추천드립니다.";
            case POLYMATH_SEEKER    -> "다양한 분야를 깊게 연결하는 성향으로, 학제 간 융합서와 철학적 과학서를 추천드립니다.";
            case AESTHETIC_COLLECTOR -> "심미적 관점으로 다양한 장르를 선별하는 성향으로, 에세이와 예술서를 추천드립니다.";
            case KNOWLEDGE_EDITOR   -> "정보를 체계적으로 정리하는 성향으로, 지식 큐레이션 에세이와 인문 교양서를 추천드립니다.";
            case FAST_SOLVER        -> "실용적이고 빠른 독서 성향으로, 비즈니스 속성 가이드와 핵심 요약서를 추천드립니다.";
            case CAREER_STRATEGIST  -> "계획적인 자기계발 성향으로, 경영전략서와 리더십 도서를 추천드립니다.";
            case EMOTIONAL_SYNCHRO  -> "깊은 감성 몰입 성향으로, 장편 소설과 서사 중심 논픽션을 추천드립니다.";
            case CASUAL_RESTER      -> "가벼운 힐링 독서 성향으로, 베스트셀러 에세이와 짧은 단편 소설을 추천드립니다.";
            case COLD_CRITIC        -> "비판적 분석 성향으로, 사회과학서와 논쟁적 인문서를 추천드립니다.";
            case SILENT_RESEARCHER  -> "혼자 깊이 탐구하는 성향으로, 학술 교양서와 원전 번역서를 추천드립니다.";
            case CONTEMPLATIVE_MONK -> "깊이 사유하는 성향으로, 철학 고전과 종교·사상서를 추천드립니다.";
            case OBSESSIVE_FANDOM   -> "한 작가·시리즈에 집중하는 성향으로, 시리즈물과 작가별 전집을 추천드립니다.";
        };
    }
}
