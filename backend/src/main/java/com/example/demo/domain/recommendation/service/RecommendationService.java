package com.example.demo.domain.recommendation.service;

import com.example.demo.domain.book.dto.BookDto;
import com.example.demo.domain.book.entity.Book;
import com.example.demo.domain.book.entity.BookGenre;
import com.example.demo.domain.book.repository.BookRepository;
import com.example.demo.domain.persona.entity.PersonaCode;
import com.example.demo.domain.recommendation.dto.RecommendationDto;
import com.example.demo.domain.survey.entity.SurveySession;
import com.example.demo.domain.survey.repository.SurveySessionRepository;
import com.example.demo.infra.ai.AiServerClient;
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
 * 기존 점수/규칙 기반 추천에서 페르소나 기반 장르 매핑 + LLM 코멘트 구조로 변경됨:
 *
 * 추천 흐름:
 *   1. 완료된 세션 조회 → 페르소나 확인
 *   2. 페르소나에 맞는 장르 목록 결정
 *   3. 해당 장르 도서 DB 조회
 *   4. 상위 N권 선정
 *   5. FastAPI에 추천 코멘트 요청 (실패 시 null로 폴백)
 *   6. 응답 DTO 구성
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final SurveySessionRepository surveySessionRepository;
    private final BookRepository bookRepository;
    private final AiServerClient aiServerClient;

    private static final int MAX_RECOMMENDATIONS = 5;

    public RecommendationDto.RecommendResponse getRecommendations(String sessionUuid) {

        // 1. 완료된 세션 조회
        SurveySession session = surveySessionRepository.findCompletedByUuid(sessionUuid)
                .orElseThrow(() -> new IllegalArgumentException(
                        "완료된 세션을 찾을 수 없습니다. 먼저 설문을 완료해주세요: " + sessionUuid));

        if (session.getPersonaType() == null) {
            throw new IllegalStateException("페르소나가 결정되지 않은 세션입니다.");
        }

        PersonaCode personaCode = session.getPersonaType().getCode();
        String personaName = session.getPersonaType().getName();

        // 2. 페르소나 → 추천 장르 목록 결정
        List<BookGenre> genres = resolveGenres(personaCode);

        // 3. 장르별 도서 조회 후 합산
        List<Book> candidates = new ArrayList<>();
        for (BookGenre genre : genres) {
            candidates.addAll(bookRepository.findByGenreAndIsActiveTrue(genre));
        }

        // 도서가 없으면 전체 활성 도서에서 추천
        if (candidates.isEmpty()) {
            log.warn("[RecommendationService] 페르소나 {} 장르 도서 없음, 전체 도서에서 추천", personaCode);
            candidates = bookRepository.findByIsActiveTrue();
        }

        // 4. 상위 N권 선정 (현재는 순서대로, 추후 정렬 로직 추가 가능)
        List<Book> topBooks = candidates.stream()
                .limit(MAX_RECOMMENDATIONS)
                .toList();

        // 5. 추천 도서 DTO 구성
        List<RecommendationDto.RankedBook> rankedBooks = new ArrayList<>();
        for (int i = 0; i < topBooks.size(); i++) {
            Book book = topBooks.get(i);
            rankedBooks.add(RecommendationDto.RankedBook.builder()
                    .rank(i + 1)
                    .book(BookDto.BookResponse.of(book))
                    .matchReason(buildMatchReason(book, personaCode))
                    .build());
        }

        // 6. FastAPI AI 추천 코멘트 요청
        // AI 서버 장애 시 aiComment = null로 대체 - 추천 자체는 항상 동작
        String aiComment = null;
        try {
            Map<String, Object> profile = new HashMap<>();
            profile.put("persona_code", personaCode.name());
            profile.put("persona_name", personaName);

            List<Map<String, Object>> bookList = topBooks.stream()
                    .map(book -> {
                        Map<String, Object> b = new HashMap<>();
                        b.put("title", book.getTitle());
                        b.put("authors", book.getAuthors());
                        b.put("genre", book.getGenre().name());
                        return b;
                    })
                    .toList();

            aiComment = aiServerClient.recommend(profile, bookList);
        } catch (Exception e) {
            log.warn("[RecommendationService] AI 추천 코멘트 요청 실패: {}", e.getMessage());
        }

        return RecommendationDto.RecommendResponse.builder()
                .personaCode(personaCode)
                .personaName(personaName)
                .reason(buildOverallReason(personaCode))
                .aiComment(aiComment)
                .books(rankedBooks)
                .build();
    }

    /**
     * 페르소나별 추천 장르 목록 결정
     *
     * 각 페르소나의 독서 성향에 맞는 장르를 우선순위 순으로 반환합니다.
     */
    private List<BookGenre> resolveGenres(PersonaCode personaCode) {
        return switch (personaCode) {
            case EXPLORER  -> List.of(BookGenre.HUMANITIES, BookGenre.SCIENCE, BookGenre.HISTORY);
            case CURATOR   -> List.of(BookGenre.NOVEL, BookGenre.ESSAY, BookGenre.POETRY);
            case NAVIGATOR -> List.of(BookGenre.SELF_HELP, BookGenre.BUSINESS);
            case DWELLER   -> List.of(BookGenre.FANTASY, BookGenre.HEALING, BookGenre.NOVEL);
            case ANALYST   -> List.of(BookGenre.MYSTERY, BookGenre.SOCIAL, BookGenre.PHILOSOPHY);
            case DIVER     -> List.of(BookGenre.PHILOSOPHY, BookGenre.HUMANITIES, BookGenre.ESSAY);
        };
    }

    /**
     * 개별 도서 추천 이유 생성
     */
    private String buildMatchReason(Book book, PersonaCode personaCode) {
        return switch (personaCode) {
            case EXPLORER  -> "새로운 지식과 시각을 넓혀줄 도서입니다.";
            case CURATOR   -> "감성과 문장의 깊이를 느낄 수 있는 도서입니다.";
            case NAVIGATOR -> "실생활에 바로 적용할 수 있는 인사이트를 담은 도서입니다.";
            case DWELLER   -> "편안하게 읽으며 위로받을 수 있는 도서입니다.";
            case ANALYST   -> "논리적 구조와 숨겨진 의미를 탐구할 수 있는 도서입니다.";
            case DIVER     -> "깊이 사유하고 성찰할 수 있는 도서입니다.";
        };
    }

    /**
     * 페르소나별 전체 추천 이유 생성
     */
    private String buildOverallReason(PersonaCode code) {
        return switch (code) {
            case EXPLORER  -> "새로운 지식을 탐구하는 성향으로, 깊이 있는 교양서와 과학서를 추천드립니다.";
            case CURATOR   -> "감성과 문장에 몰입하는 성향으로, 소설과 에세이를 추천드립니다.";
            case NAVIGATOR -> "실용적인 목표 지향 독서 성향으로, 자기계발서와 경제경영서를 추천드립니다.";
            case DWELLER   -> "편안한 휴식형 독서 성향으로, 힐링 소설과 판타지를 추천드립니다.";
            case ANALYST   -> "논리적 구조를 분석하는 성향으로, 추리소설과 사회비평서를 추천드립니다.";
            case DIVER     -> "깊이 사유하는 성향으로, 철학서와 인문 에세이를 추천드립니다.";
        };
    }
}