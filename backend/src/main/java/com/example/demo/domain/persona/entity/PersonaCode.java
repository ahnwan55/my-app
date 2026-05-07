package com.example.demo.domain.persona.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 독서 페르소나 서브 12종 enum.
 *
 * 분류 체계:
 *   6가지 대분류(계열) × 각 2종 서브 페르소나 = 총 12종
 *
 * Bedrock Claude가 반환하는 persona_code 문자열과 1:1 매핑된다.
 * data.sql의 persona_type 테이블 code 컬럼값과 반드시 일치해야 한다.
 *
 * 분류 근거: 독서 페르소나 세부 설계서 v2 (2026.04.20)
 */
@Getter
@RequiredArgsConstructor
public enum PersonaCode {

    // ── EXPLORER 계열 (지적 탐험가형) ─────────────────────────────────────
    // 대표 지표: 지적 확장성(High)

    TREND_SURFER(
        "EXPLORER",
        "트렌드 서퍼",
        "새로운 트렌드 분야를 빠르게 탐색하는 독서가",
        "최신성 민감도 > 분석적 깊이"
    ),
    POLYMATH_SEEKER(
        "EXPLORER",
        "박학다식형",
        "다양한 분야를 깊게 파고들어 연결하는 독서가",
        "분석적 깊이 > 최신성 민감도"
    ),

    // ── CURATOR 계열 (큐레이터형) ─────────────────────────────────────────
    // 대표 지표: 정보 체계화(High)

    AESTHETIC_COLLECTOR(
        "CURATOR",
        "미학적 수집가",
        "심미적 관점으로 다양한 장르를 선별하는 독서가",
        "독서 다양성 > 독서 지속성"
    ),
    KNOWLEDGE_EDITOR(
        "CURATOR",
        "지식 편집자",
        "정보를 체계적으로 정리하고 공유하는 독서가",
        "독서 지속성 > 독서 다양성"
    ),

    // ── NAVIGATOR 계열 (네비게이터형) ────────────────────────────────────
    // 대표 지표: 실용 지향성(High)

    FAST_SOLVER(
        "NAVIGATOR",
        "해결사",
        "필요한 부분만 발췌독하여 문제를 즉시 해결하는 독서가",
        "독서 지속성 < 전체 평균"
    ),
    CAREER_STRATEGIST(
        "NAVIGATOR",
        "커리어 전략가",
        "계획적으로 자기계발을 위해 정독하는 독서가",
        "독서 지속성 ≥ 전체 평균 AND 분석적 깊이 ≥ 전체 평균"
    ),

    // ── DWELLER 계열 (드웰러형) ───────────────────────────────────────────
    // 대표 지표: 감성 몰입도(High)

    EMOTIONAL_SYNCHRO(
        "DWELLER",
        "감성 동기화형",
        "주인공에 몰입하여 감정을 깊이 공유하는 독서가",
        "독서 지속성 > 상호작용 빈도"
    ),
    CASUAL_RESTER(
        "DWELLER",
        "가벼운 휴식자",
        "베스트셀러로 일상의 쉼을 찾는 독서가",
        "상호작용 빈도 > 독서 지속성"
    ),

    // ── ANALYST 계열 (분석가형) ───────────────────────────────────────────
    // 대표 지표: 분석적 깊이(High) + 독서 다양성 ≥ 전체 평균

    COLD_CRITIC(
        "ANALYST",
        "냉철한 비평가",
        "논리적 허점을 찾아 토론하며 지적 쾌감을 느끼는 독서가",
        "상호작용 빈도 ≥ 전체 평균"
    ),
    SILENT_RESEARCHER(
        "ANALYST",
        "은둔형 연구자",
        "혼자 조용히 텍스트의 이면을 파고드는 독서가",
        "상호작용 빈도 < 전체 평균"
    ),

    // ── DIVER 계열 (다이버형) ─────────────────────────────────────────────
    // 대표 지표: 분석적 깊이(High) + 독서 다양성 < 전체 평균

    CONTEMPLATIVE_MONK(
        "DIVER",
        "사유하는 수행자",
        "한 권을 오래 붙잡고 인생의 본질을 고민하는 독서가",
        "독서 지속성 ≥ 전체 평균"
    ),
    OBSESSIVE_FANDOM(
        "DIVER",
        "지독한 덕후",
        "한 작가·시리즈를 끝까지 파고드는 독서가",
        "독서 지속성 < 전체 평균"
    );

    private final String parentCode;    // 대분류 계열 코드
    private final String displayName;   // 화면 표시용 한국어 이름
    private final String description;  // 유형 설명
    private final String condition;    // 분류 조건 (참고용)
}
