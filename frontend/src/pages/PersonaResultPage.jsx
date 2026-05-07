import {
  RadarChart, Radar, PolarGrid, PolarAngleAxis,
  PolarRadiusAxis, ResponsiveContainer, Tooltip,
} from "recharts";
import { useState } from "react";

/**
 * PersonaResultPage.jsx — 페르소나 결과 페이지
 *
 * [변경 사항]
 *   - 6대 지표별 설명 텍스트 추가 (AXIS_DESC)
 *   - ScoreBar에 지표 설명 토글 버튼(?) 추가
 *   - 지표 설명 펼침/접힘 애니메이션 처리
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
   Radar Chart 6축 정의 + 지표별 설명
   ──────────────────────────────────────── */
const AXES = [
  {
    key:   "지적_확장성",
    label: "지적 확장성",
    desc:  "얼마나 다양한 분야를 폭넓게 탐구하는지를 나타냅니다. 점수가 높을수록 새로운 지식과 낯선 분야에 대한 호기심이 강합니다.",
  },
  {
    key:   "분석적_깊이",
    label: "분석적 깊이",
    desc:  "텍스트를 얼마나 비판적으로 읽고 논리적으로 분석하는지를 나타냅니다. 점수가 높을수록 주석, 참고문헌, 근거까지 꼼꼼히 따져보는 성향입니다.",
  },
  {
    key:   "실용_지향성",
    label: "실용 지향성",
    desc:  "독서를 통해 얻은 지식을 실제 생활이나 업무에 바로 적용하려는 경향을 나타냅니다. 점수가 높을수록 실전 활용을 중시하는 독서 스타일입니다.",
  },
  {
    key:   "감성_몰입도",
    label: "감성 몰입도",
    desc:  "이야기 속 인물과 감정에 얼마나 깊이 공감하고 빠져드는지를 나타냅니다. 점수가 높을수록 서사와 감정 흐름에 민감하게 반응하는 독자입니다.",
  },
  {
    key:   "정보_체계화",
    label: "정보 체계화",
    desc:  "읽은 내용을 얼마나 잘 정리하고 구조화하는지를 나타냅니다. 점수가 높을수록 메모, 요약, 분류 등으로 지식을 체계적으로 관리하는 성향입니다.",
  },
  {
    key:   "사회적_영향도",
    label: "사회적 영향도",
    desc:  "독서를 통해 얻은 인사이트를 타인과 공유하거나 사회적 담론에 참여하려는 경향을 나타냅니다. 점수가 높을수록 독서를 소통의 도구로 활용합니다.",
  },
];

/* ────────────────────────────────────────
   서브 페르소나 12종 프로파일
   ──────────────────────────────────────── */
const SUB_PERSONA_PROFILE = {
  TREND_SURFER:       { emoji: "🏄", name: "트렌드 서퍼",     series: "EXPLORER",  color: C.pinkDark,   bg: C.pinkLight,   desc: "새로 나온 힙한 분야는 다 건드려봐야 직성이 풀리는 타입. 어제는 양자역학, 오늘은 인류세." },
  POLYMATH_SEEKER:    { emoji: "🔭", name: "박학다식형",       series: "EXPLORER",  color: C.pinkDark,   bg: C.pinkLight,   desc: "전혀 다른 두 분야를 깊게 파서 연결하는 고지능 탐험가." },
  AESTHETIC_COLLECTOR:{ emoji: "🌸", name: "미학적 수집가",    series: "CURATOR",   color: C.purpleDark, bg: C.purpleLight, desc: "책의 내용만큼이나 표지, 굿즈, 서재 배치에 진심인 타입." },
  KNOWLEDGE_EDITOR:   { emoji: "📋", name: "지식 편집자",      series: "CURATOR",   color: C.purpleDark, bg: C.purpleLight, desc: "방대한 정보를 요약하고 정리해서 남들에게 공유하는 '정리의 달인'." },
  FAST_SOLVER:        { emoji: "⚡", name: "해결사",           series: "NAVIGATOR", color: "#2563eb",    bg: "#dbeafe",     desc: "필요한 부분만 발췌독해서 당장의 문제를 해결하는 실전파." },
  CAREER_STRATEGIST:  { emoji: "⚓", name: "커리어 전략가",    series: "NAVIGATOR", color: "#2563eb",    bg: "#dbeafe",     desc: "자기 계발을 위해 로드맵을 짜고 벽돌책도 씹어먹는 전략파." },
  EMOTIONAL_SYNCHRO:  { emoji: "💫", name: "감성 동기화형",    series: "DWELLER",   color: "#d97706",    bg: "#fef3c7",     desc: "주인공에 빙의해서 밤새도록 소설을 읽으며 눈물 콧물 짜는 타입." },
  CASUAL_RESTER:      { emoji: "☕", name: "가벼운 휴식자",    series: "DWELLER",   color: "#d97706",    bg: "#fef3c7",     desc: "남들 다 읽는 베스트셀러 에세이로 퇴근길 가볍게 힐링하는 타입." },
  COLD_CRITIC:        { emoji: "🧊", name: "냉철한 비평가",    series: "ANALYST",   color: "#059669",    bg: "#d1fae5",     desc: "채팅방에서 논리적 허점을 찾아내고 토론하며 지적 쾌감을 느끼는 타입." },
  SILENT_RESEARCHER:  { emoji: "♟",  name: "은둔형 연구자",    series: "ANALYST",   color: "#059669",    bg: "#d1fae5",     desc: "혼자 조용히 텍스트의 이면을 파고드는 타입. 주석과 참고문헌까지 챙겨 읽는다." },
  CONTEMPLATIVE_MONK: { emoji: "🌊", name: "사유하는 수행자",  series: "DIVER",     color: "#4f46e5",    bg: "#e0e7ff",     desc: "고전 한 권을 몇 달 동안 붙잡고 인생의 본질을 고민하는 타입." },
  OBSESSIVE_FANDOM:   { emoji: "🎯", name: "지독한 덕후",      series: "DIVER",     color: "#4f46e5",    bg: "#e0e7ff",     desc: "한 작가의 절판된 초판본까지 싹 다 읽으며 끝을 보는 타입." },
};

const SERIES_NAME = {
  EXPLORER:  "지적 탐험가형",
  CURATOR:   "큐레이터형",
  NAVIGATOR: "네비게이터형",
  DWELLER:   "드웰러형",
  ANALYST:   "분석가형",
  DIVER:     "다이버형",
};

const FALLBACK_SCORES = {
  EXPLORER:  { "지적_확장성":9.0, "분석적_깊이":6.5, "실용_지향성":5.0, "감성_몰입도":4.5, "정보_체계화":6.0, "사회적_영향도":5.5 },
  CURATOR:   { "지적_확장성":6.0, "분析적_깊이":5.5, "실용_지향성":4.5, "감성_몰입도":6.5, "정보_체계화":9.0, "사회적_영향도":6.0 },
  NAVIGATOR: { "지적_확장성":5.5, "분석적_깊이":6.0, "실용_지향성":9.0, "감성_몰입도":3.5, "정보_체계화":7.0, "사회적_영향도":5.0 },
  DWELLER:   { "지적_확장성":4.5, "분析적_깊이":4.0, "실용_지향성":3.5, "감성_몰입도":9.0, "정보_체계화":4.5, "사회적_영향도":6.5 },
  ANALYST:   { "지적_확장성":7.0, "분析적_깊이":9.0, "실용_지향성":5.5, "감성_몰입도":3.5, "정보_체계화":7.5, "사회적_영향도":7.0 },
  DIVER:     { "지적_확장성":6.5, "분析적_깊이":9.0, "실용_지향성":4.0, "감성_몰입도":5.5, "정보_체계화":6.5, "사회적_영향도":3.5 },
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

/**
 * ScoreBar — 지표 점수 바 컴포넌트
 *
 * 지표 이름 옆 [?] 버튼 클릭 시 해당 지표 설명이 펼쳐진다.
 * 한 번에 하나의 설명만 열리도록 openKey를 부모에서 관리한다.
 */
function ScoreBar({ axisKey, label, desc, score, color, isOpen, onToggle }) {
  return (
    <div style={{ marginBottom: 12 }}>
      {/* 라벨 행 */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 4 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
          <span style={{ fontSize: 12, fontWeight: 600, color: C.gray700 }}>{label}</span>
          {/* 설명 토글 버튼 */}
          <button
            onClick={onToggle}
            aria-label={`${label} 설명 ${isOpen ? "닫기" : "보기"}`}
            style={{
              width: 16, height: 16, borderRadius: "50%",
              border: `1.5px solid ${isOpen ? color : C.gray300}`,
              background: isOpen ? color : "transparent",
              color: isOpen ? C.white : C.gray400,
              fontSize: 9, fontWeight: 800,
              cursor: "pointer", lineHeight: 1,
              display: "flex", alignItems: "center", justifyContent: "center",
              padding: 0, flexShrink: 0,
              transition: "all 0.2s ease",
            }}
          >
            ?
          </button>
        </div>
        <span style={{ fontSize: 12, fontWeight: 800, color }}>{Number(score).toFixed(1)}</span>
      </div>

      {/* 프로그레스 바 */}
      <div style={{ height: 6, background: C.gray100, borderRadius: 4, overflow: "hidden" }}>
        <div style={{
          height: "100%",
          width: `${(score / 10) * 100}%`,
          background: `linear-gradient(90deg, ${C.pink}, ${C.purple})`,
          borderRadius: 4,
          transition: "width 0.6s ease",
        }} />
      </div>

      {/* 지표 설명 (펼침) */}
      {isOpen && (
        <div style={{
          marginTop: 8,
          padding: "10px 12px",
          background: `${color}12`,
          border: `1px solid ${color}30`,
          borderRadius: 10,
          fontSize: 12,
          color: C.gray700,
          lineHeight: 1.7,
          wordBreak: "keep-all",
        }}>
          {desc}
        </div>
      )}
    </div>
  );
}

export default function PersonaResultPage({ personaCode, personaName, scores, onViewBooks }) {
  const profile = SUB_PERSONA_PROFILE[personaCode] || SUB_PERSONA_PROFILE.TREND_SURFER;

  const effectiveScores = (scores && Object.keys(scores).length > 0)
    ? scores
    : FALLBACK_SCORES[profile.series] || FALLBACK_SCORES.EXPLORER;

  const chart = buildChartData(effectiveScores);

  // 현재 열린 지표 설명의 key (한 번에 하나만 열림)
  const [openKey, setOpenKey] = useState(null);

  const handleToggle = (key) => {
    setOpenKey(prev => (prev === key ? null : key));
  };

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
              <p style={{ fontSize: 11, fontWeight: 700, color: profile.color, letterSpacing: "0.08em", margin: "0 0 2px" }}>
                {SERIES_NAME[profile.series]} 계열
              </p>
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
          <p style={{ fontSize: 11, color: C.gray400, margin: "0 0 14px" }}>
            지표 이름 옆 <strong style={{ color: C.pink }}>?</strong> 버튼을 누르면 설명을 볼 수 있어요.
          </p>
          {AXES.map(({ key, label, desc }) => (
            <ScoreBar
              key={key}
              axisKey={key}
              label={label}
              desc={desc}
              score={effectiveScores[key] ?? 0}
              color={profile.color}
              isOpen={openKey === key}
              onToggle={() => handleToggle(key)}
            />
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
  wrap:        { minHeight: "100vh", background: `linear-gradient(135deg, ${C.pinkBg}, ${C.white}, #faf5ff)`, fontFamily: "'Noto Sans KR', sans-serif", position: "relative", overflow: "hidden" },
  bgDecor:     { position: "fixed", inset: 0, pointerEvents: "none", overflow: "hidden", zIndex: 0 },
  blob:        { position: "absolute", width: 384, height: 384, borderRadius: "50%", opacity: 0.2, filter: "blur(60px)" },
  inner:       { position: "relative", zIndex: 1, maxWidth: 480, margin: "0 auto", padding: "48px 20px 80px" },
  sectionLabel:{ fontSize: 11, fontWeight: 700, letterSpacing: "0.15em", textTransform: "uppercase", color: C.pink, marginBottom: 16 },
  personaCard: { position: "relative", overflow: "hidden", borderRadius: 24, padding: "24px 20px", marginBottom: 16, boxShadow: "0 4px 20px rgba(244,114,182,0.12)" },
  cardDeco:    { position: "absolute", top: -40, right: -40, width: 160, height: 160, borderRadius: "50%" },
  cardTop:     { display: "flex", alignItems: "center", gap: 16, marginBottom: 12, position: "relative" },
  personaEmoji:{ fontSize: 48, lineHeight: 1 },
  personaName: { fontFamily: "'Playfair Display', serif", fontSize: 22, fontWeight: 800, color: C.gray800, margin: "0 0 2px" },
  personaDesc: { fontSize: 13, color: C.gray700, lineHeight: 1.7, position: "relative", margin: 0 },
  section:     { background: "rgba(255,255,255,0.75)", backdropFilter: "blur(12px)", border: `1px solid ${C.pinkLight}`, borderRadius: 20, padding: "20px", marginBottom: 16 },
  sectionTitle:{ fontSize: 14, fontWeight: 800, color: C.gray800, margin: "0 0 4px" },
  ctaBtn:      { width: "100%", padding: "16px 0", borderRadius: 18, border: "none", cursor: "pointer", background: `linear-gradient(135deg, ${C.pink}, ${C.purple})`, color: C.white, fontSize: 15, fontWeight: 800, boxShadow: "0 8px 24px rgba(244,114,182,0.4)", fontFamily: "'Noto Sans KR', sans-serif" },
};
