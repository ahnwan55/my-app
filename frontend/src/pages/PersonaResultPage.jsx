import {
  RadarChart, Radar, PolarGrid, PolarAngleAxis,
  PolarRadiusAxis, ResponsiveContainer, Tooltip,
} from "recharts";

/**
 * PersonaResultPage.jsx — 페르소나 결과 페이지
 *
 * Props:
 *  personaCode {string}   — 서브 페르소나 코드
 *                           예: "TREND_SURFER" | "POLYMATH_SEEKER" |
 *                               "AESTHETIC_COLLECTOR" | "KNOWLEDGE_EDITOR" |
 *                               "FAST_SOLVER" | "CAREER_STRATEGIST" |
 *                               "EMOTIONAL_SYNCHRO" | "CASUAL_RESTER" |
 *                               "COLD_CRITIC" | "SILENT_RESEARCHER" |
 *                               "CONTEMPLATIVE_MONK" | "OBSESSIVE_FANDOM"
 *  personaName {string}   — 서브 페르소나 이름 (예: "트렌드 서퍼")
 *                           LoadingPage → App.jsx → 이 페이지로 전달
 *  scores      {object}   — 6대 지표 점수 Map (Radar Chart용)
 *                           { 지적_확장성: 8.5, 분석적_깊이: 4.0, ... }
 *                           POST /api/surveys/submit 응답에서 추출
 *  onViewBooks {Function} — "맞춤 도서 추천 받기" 클릭 시 호출
 *                           App.jsx에서 () => navigate("/book-loading") 전달
 *
 * scores가 비어 있으면 대분류별 더미 데이터로 폴백 처리
 *
 * 지표 체계 (독서_페르소나_세부_설계서_v2.pdf 기반):
 *  Radar Chart 표시: 지적_확장성 / 분석적_깊이 / 실용_지향성
 *                    감성_몰입도 / 정보_체계화 / 사회적_영향도
 */

const C = {
  pink:        "#f472b6",
  pinkDark:    "#ec4899",
  pinkLight:   "#fce7f3",
  pinkBg:      "#fdf2f8",
  purple:      "#a855f7",
  purpleDark:  "#7c3aed",
  purpleLight: "#ede9fe",
  white:       "#ffffff",
  gray50:      "#f9fafb",
  gray100:     "#f3f4f6",
  gray200:     "#e5e7eb",
  gray400:     "#9ca3af",
  gray500:     "#6b7280",
  gray700:     "#374151",
  gray800:     "#1f2937",
};

/* ────────────────────────────────────────
   Radar Chart 6축 정의
   키는 API 응답 scores의 키와 일치
   ──────────────────────────────────────── */
const AXES = [
  { key: "지적_확장성",   label: "지적 확장성"   },
  { key: "분석적_깊이",   label: "분석적 깊이"   },
  { key: "실용_지향성",   label: "실용 지향성"   },
  { key: "감성_몰입도",   label: "감성 몰입도"   },
  { key: "정보_체계화",   label: "정보 체계화"   },
  { key: "사회적_영향도", label: "사회적 영향도" },
];

/* ────────────────────────────────────────
   서브 페르소나 12종 프로파일
   personaCode: SurveyDto.SubmitResponse.personaCode 값과 일치
   ──────────────────────────────────────── */
const SUB_PERSONA_PROFILE = {
  // EXPLORER 계열
  TREND_SURFER:      { emoji: "🏄", name: "트렌드 서퍼",     series: "EXPLORER", color: C.pinkDark,  bg: C.pinkLight,   desc: "새로 나온 힙한 분야는 다 건드려봐야 직성이 풀리는 타입. 어제는 양자역학, 오늘은 인류세." },
  POLYMATH_SEEKER:   { emoji: "🔭", name: "박학다식형",       series: "EXPLORER", color: C.pinkDark,  bg: C.pinkLight,   desc: "전혀 다른 두 분야를 깊게 파서 연결하는 고지능 탐험가." },
  // CURATOR 계열
  AESTHETIC_COLLECTOR:{ emoji: "🌸", name: "미학적 수집가",  series: "CURATOR",  color: C.purpleDark, bg: C.purpleLight, desc: "책의 내용만큼이나 표지, 굿즈, 서재 배치에 진심인 타입." },
  KNOWLEDGE_EDITOR:  { emoji: "📋", name: "지식 편집자",      series: "CURATOR",  color: C.purpleDark, bg: C.purpleLight, desc: "방대한 정보를 요약하고 정리해서 남들에게 공유하는 '정리의 달인'." },
  // NAVIGATOR 계열
  FAST_SOLVER:       { emoji: "⚡", name: "해결사",           series: "NAVIGATOR",color: "#2563eb",    bg: "#dbeafe",     desc: "필요한 부분만 발췌독해서 당장의 문제를 해결하는 실전파." },
  CAREER_STRATEGIST: { emoji: "⚓", name: "커리어 전략가",    series: "NAVIGATOR",color: "#2563eb",    bg: "#dbeafe",     desc: "자기 계발을 위해 로드맵을 짜고 벽돌책도 씹어먹는 전략파." },
  // DWELLER 계열
  EMOTIONAL_SYNCHRO: { emoji: "💫", name: "감성 동기화형",    series: "DWELLER",  color: "#d97706",    bg: "#fef3c7",     desc: "주인공에 빙의해서 밤새도록 소설을 읽으며 눈물 콧물 짜는 타입." },
  CASUAL_RESTER:     { emoji: "☕", name: "가벼운 휴식자",    series: "DWELLER",  color: "#d97706",    bg: "#fef3c7",     desc: "남들 다 읽는 베스트셀러 에세이로 퇴근길 가볍게 힐링하는 타입." },
  // ANALYST 계열
  COLD_CRITIC:       { emoji: "🧊", name: "냉철한 비평가",    series: "ANALYST",  color: "#059669",    bg: "#d1fae5",     desc: "채팅방에서 논리적 허점을 찾아내고 토론하며 지적 쾌감을 느끼는 타입." },
  SILENT_RESEARCHER: { emoji: "♟",  name: "은둔형 연구자",    series: "ANALYST",  color: "#059669",    bg: "#d1fae5",     desc: "혼자 조용히 텍스트의 이면을 파고드는 타입. 주석과 참고문헌까지 챙겨 읽는다." },
  // DIVER 계열
  CONTEMPLATIVE_MONK:{ emoji: "🌊", name: "사유하는 수행자",  series: "DIVER",    color: "#4f46e5",    bg: "#e0e7ff",     desc: "고전 한 권을 몇 달 동안 붙잡고 인생의 본질을 고민하는 타입." },
  OBSESSIVE_FANDOM:  { emoji: "🎯", name: "지독한 덕후",      series: "DIVER",    color: "#4f46e5",    bg: "#e0e7ff",     desc: "한 작가의 절판된 초판본까지 싹 다 읽으며 끝을 보는 타입." },
};

// 대분류 계열 이름
const SERIES_NAME = {
  EXPLORER:  "지적 탐험가형",
  CURATOR:   "큐레이터형",
  NAVIGATOR: "네비게이터형",
  DWELLER:   "드웰러형",
  ANALYST:   "분석가형",
  DIVER:     "다이버형",
};

/* ────────────────────────────────────────
   scores 폴백 더미 (API scores가 비었을 때)
   대분류 계열별로 적용
   ──────────────────────────────────────── */
const FALLBACK_SCORES = {
  EXPLORER:  { "지적_확장성":9.0, "분석적_깊이":6.5, "실용_지향성":5.0, "감성_몰입도":4.5, "정보_체계화":6.0, "사회적_영향도":5.5 },
  CURATOR:   { "지적_확장성":6.0, "분석적_깊이":5.5, "실용_지향성":4.5, "감성_몰입도":6.5, "정보_체계화":9.0, "사회적_영향도":6.0 },
  NAVIGATOR: { "지적_확장성":5.5, "분석적_깊이":6.0, "실용_지향성":9.0, "감성_몰입도":3.5, "정보_체계화":7.0, "사회적_영향도":5.0 },
  DWELLER:   { "지적_확장성":4.5, "분석적_깊이":4.0, "실용_지향성":3.5, "감성_몰입도":9.0, "정보_체계화":4.5, "사회적_영향도":6.5 },
  ANALYST:   { "지적_확장성":7.0, "분석적_깊이":9.0, "실용_지향성":5.5, "감성_몰입도":3.5, "정보_체계화":7.5, "사회적_영향도":7.0 },
  DIVER:     { "지적_확장성":6.5, "분석적_깊이":9.0, "실용_지향성":4.0, "감성_몰입도":5.5, "정보_체계화":6.5, "사회적_영향도":3.5 },
};

function buildChartData(scores) {
  return AXES.map(({ key, label }) => ({ axis: label, score: scores[key] ?? 0, fullMark: 10 }));
}

function CustomTooltip({ active, payload }) {
  if (!active || !payload?.length) return null;
  const { axis, score } = payload[0].payload;
  return (
    <div style={{ background: C.white, border: `1px solid ${C.pinkLight}`, borderRadius: 12, padding: "8px 14px", boxShadow: "0 4px 16px rgba(244,114,182,0.15)" }}>
      <p style={{ fontSize: 12, fontWeight: 700, color: C.gray800, margin: "0 0 2px" }}>{axis}</p>
      <p style={{ fontSize: 14, fontWeight: 800, color: C.pink, margin: 0 }}>{Number(score).toFixed(1)} / 10</p>
    </div>
  );
}

function ScoreBar({ label, score, color }) {
  return (
    <div style={{ marginBottom: 10 }}>
      <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 4 }}>
        <span style={{ fontSize: 12, fontWeight: 600, color: C.gray700 }}>{label}</span>
        <span style={{ fontSize: 12, fontWeight: 800, color }}>{Number(score).toFixed(1)}</span>
      </div>
      <div style={{ height: 6, background: C.gray100, borderRadius: 4, overflow: "hidden" }}>
        <div style={{ height: "100%", width: `${(score / 10) * 100}%`, background: `linear-gradient(90deg, ${C.pink}, ${C.purple})`, borderRadius: 4 }} />
      </div>
    </div>
  );
}

export default function PersonaResultPage({ personaCode, personaName, scores, onViewBooks }) {
  // 프로파일 조회 (없으면 TREND_SURFER 폴백)
  const profile = SUB_PERSONA_PROFILE[personaCode] || SUB_PERSONA_PROFILE.TREND_SURFER;

  // scores가 비었으면 대분류 계열별 더미로 폴백
  const effectiveScores = (scores && Object.keys(scores).length > 0)
    ? scores
    : FALLBACK_SCORES[profile.series] || FALLBACK_SCORES.EXPLORER;

  const chart = buildChartData(effectiveScores);

  return (
    <div style={S.wrap}>
      <div style={S.bgDecor} aria-hidden="true">
        <div style={{ ...S.blob, top: -96, right: -96, background: "#fbcfe8" }} />
        <div style={{ ...S.blob, bottom: -96, left: -96, background: "#e9d5ff" }} />
      </div>

      <div style={S.inner}>
        <p style={S.sectionLabel}>독서 페르소나 결과</p>

        {/* ── 서브 페르소나 카드 ── */}
        <div style={{ ...S.personaCard, background: `linear-gradient(135deg, ${profile.bg}, ${C.purpleLight})`, border: `1.5px solid ${profile.color}30` }}>
          <div style={{ ...S.cardDeco, background: `${profile.color}10` }} aria-hidden="true" />
          <div style={S.cardTop}>
            <span style={S.personaEmoji}>{profile.emoji}</span>
            <div>
              {/* 대분류 계열 */}
              <p style={{ fontSize: 11, fontWeight: 700, color: profile.color, letterSpacing: "0.08em", margin: "0 0 2px" }}>
                {SERIES_NAME[profile.series]} 계열
              </p>
              {/* 서브 페르소나 이름 — API 응답 personaName 우선, 없으면 프로파일 name */}
              <h1 style={S.personaName}>{personaName || profile.name}</h1>
              <p style={{ fontSize: 11, color: profile.color, fontWeight: 600, margin: 0, letterSpacing: "0.05em" }}>
                {personaCode}
              </p>
            </div>
          </div>
          <p style={S.personaDesc}>{profile.desc}</p>
        </div>

        {/* ── Radar Chart ── */}
        <div style={S.section}>
          <p style={S.sectionTitle}>📊 독서 성향 분석</p>
          <p style={{ fontSize: 11, color: C.gray400, margin: "0 0 12px" }}>6대 지표 기준 (0~10점)</p>
          <ResponsiveContainer width="100%" height={280}>
            <RadarChart data={chart} margin={{ top: 10, right: 30, bottom: 10, left: 30 }}>
              <PolarGrid stroke={C.gray200} />
              <PolarAngleAxis
                dataKey="axis"
                tick={{ fontSize: 11, fontWeight: 600, fill: C.gray700, fontFamily: "'Noto Sans KR', sans-serif" }}
              />
              <PolarRadiusAxis angle={90} domain={[0, 10]} tickCount={6} tick={{ fontSize: 9, fill: C.gray400 }} axisLine={false} />
              <Radar
                name="독서 성향" dataKey="score"
                stroke={C.pinkDark} fill={C.pink} fillOpacity={0.25}
                strokeWidth={2} dot={{ fill: C.pinkDark, r: 4, strokeWidth: 0 }}
              />
              <Tooltip content={<CustomTooltip />} />
            </RadarChart>
          </ResponsiveContainer>
        </div>

        {/* ── 6대 지표 점수 바 ── */}
        <div style={S.section}>
          <p style={S.sectionTitle}>📈 지표별 점수</p>
          {AXES.map(({ key, label }) => (
            <ScoreBar key={key} label={label} score={effectiveScores[key] ?? 0} color={profile.color} />
          ))}
        </div>

        {/* ── CTA ── */}
        <button onClick={onViewBooks} style={S.ctaBtn}>
          📚 맞춤 도서 추천 받기
        </button>
      </div>
    </div>
  );
}

const S = {
  wrap:    { minHeight: "100vh", background: `linear-gradient(135deg, ${C.pinkBg}, ${C.white}, #faf5ff)`, fontFamily: "'Noto Sans KR', sans-serif", position: "relative", overflow: "hidden" },
  bgDecor: { position: "fixed", inset: 0, pointerEvents: "none", overflow: "hidden", zIndex: 0 },
  blob:    { position: "absolute", width: 384, height: 384, borderRadius: "50%", opacity: 0.2, filter: "blur(60px)" },
  inner:   { position: "relative", zIndex: 1, maxWidth: 480, margin: "0 auto", padding: "48px 20px 80px" },
  sectionLabel: { fontSize: 11, fontWeight: 700, letterSpacing: "0.15em", textTransform: "uppercase", color: C.pink, marginBottom: 16 },
  personaCard:  { position: "relative", overflow: "hidden", borderRadius: 24, padding: "24px 20px", marginBottom: 16, boxShadow: "0 4px 20px rgba(244,114,182,0.12)" },
  cardDeco:     { position: "absolute", top: -40, right: -40, width: 160, height: 160, borderRadius: "50%" },
  cardTop:      { display: "flex", alignItems: "center", gap: 16, marginBottom: 12, position: "relative" },
  personaEmoji: { fontSize: 48, lineHeight: 1 },
  personaName:  { fontFamily: "'Playfair Display', serif", fontSize: 22, fontWeight: 800, color: C.gray800, margin: "0 0 2px" },
  personaDesc:  { fontSize: 13, color: C.gray700, lineHeight: 1.7, position: "relative", margin: 0 },
  section:      { background: "rgba(255,255,255,0.75)", backdropFilter: "blur(12px)", border: `1px solid ${C.pinkLight}`, borderRadius: 20, padding: "20px", marginBottom: 16 },
  sectionTitle: { fontSize: 14, fontWeight: 800, color: C.gray800, margin: "0 0 4px" },
  ctaBtn:       { width: "100%", padding: "16px 0", borderRadius: 18, border: "none", cursor: "pointer", background: `linear-gradient(135deg, ${C.pink}, ${C.purple})`, color: C.white, fontSize: 15, fontWeight: 800, boxShadow: "0 8px 24px rgba(244,114,182,0.4)", fontFamily: "'Noto Sans KR', sans-serif" },
};
