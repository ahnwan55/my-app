import { useState } from "react";
import { useNavigate } from "react-router-dom";

/**
 * MyPage.jsx — 마이페이지
 *
 * 표시 정보 (ERD 기반):
 *  - USERS          : nickname, profile_image, created_at
 *  - PERSONA_TYPE   : code, name, description, image_url (현재 페르소나)
 *  - PERSONA_ANALYSIS: analyzed_at, persona_id (분석 이력 — 날짜 + 페르소나 변화)
 *
 * 현재: 더미 데이터로 UI 완성
 * 이후 연동 엔드포인트 (Spring Boot):
 *  - GET /api/users/me
 *    → { user_id, nickname, profile_image, created_at, persona_id }
 *  - GET /api/users/me/persona
 *    → { persona_id, code, name, description, image_url }
 *  - GET /api/users/me/analysis-history
 *    → [ { analysis_id, persona_id, code, name, analyzed_at }, ... ]
 *
 * 구성:
 *  1. 프로필 카드  — 닉네임, 프로필 이미지, 가입일, 분석 횟수
 *  2. 현재 페르소나 카드 — 코드, 이름, 설명
 *  3. 분석 이력 타임라인 — 날짜 + 페르소나 변화
 *  4. 로그아웃 버튼
 *
 * 컬러: 벚꽃 핑크(#f472b6) × 퍼플(#a855f7)
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
  red:         "#ef4444",
};

// 페르소나 코드별 이모지 매핑
const PERSONA_EMOJI = {
  EXPLORER:  "🔭",
  CURATOR:   "🌸",
  NAVIGATOR: "⚓",
  DWELLER:   "☕",
  ANALYST:   "♟",
  DIVER:     "🌊",
};

// 페르소나 코드별 컬러 매핑
const PERSONA_COLOR = {
  EXPLORER:  { color: C.pinkDark,  bg: C.pinkLight   },
  CURATOR:   { color: C.purpleDark,bg: C.purpleLight  },
  NAVIGATOR: { color: "#2563eb",   bg: "#dbeafe"      },
  DWELLER:   { color: "#d97706",   bg: "#fef3c7"      },
  ANALYST:   { color: "#059669",   bg: "#d1fae5"      },
  DIVER:     { color: "#4f46e5",   bg: "#e0e7ff"      },
};

/* ────────────────────────────────────────
   더미 데이터 (API 연동 시 교체)
   ──────────────────────────────────────── */

// USERS + PERSONA_TYPE (GET /api/users/me + /api/users/me/persona)
const DUMMY_USER = {
  nickname:      "시완",
  profile_image: null,           // null이면 이니셜 아바타 표시
  created_at:    "2025-03-01",
  persona: {
    code:        "EXPLORER",
    name:        "지적 탐험가",
    description: "새로운 지식·개념에 대한 탐구 욕구가 강하며, 다양한 분야를 폭넓게 탐색합니다.",
  },
};

// PERSONA_ANALYSIS (GET /api/users/me/analysis-history)
// analyzed_at 기준 내림차순 (최신 순)
const DUMMY_HISTORY = [
  { analysis_id: 4, code: "EXPLORER",  name: "지적 탐험가", analyzed_at: "2025-04-20" },
  { analysis_id: 3, code: "ANALYST",   name: "분석가",      analyzed_at: "2025-03-15" },
  { analysis_id: 2, code: "ANALYST",   name: "분석가",      analyzed_at: "2025-02-28" },
  { analysis_id: 1, code: "NAVIGATOR", name: "네비게이터",  analyzed_at: "2025-03-01" },
];

/* ────────────────────────────────────────
   날짜 포맷 유틸
   ──────────────────────────────────────── */
function formatDate(dateStr) {
  const d = new Date(dateStr);
  return `${d.getFullYear()}년 ${d.getMonth() + 1}월 ${d.getDate()}일`;
}

/* ────────────────────────────────────────
   MyPage — 메인 컴포넌트
   ──────────────────────────────────────── */
export default function MyPage() {
  const navigate = useNavigate();
  const [showLogoutConfirm, setShowLogoutConfirm] = useState(false);

  const user    = DUMMY_USER;
  const history = DUMMY_HISTORY;
  const pc      = PERSONA_COLOR[user.persona.code] || PERSONA_COLOR.EXPLORER;

  // 로그아웃 처리
  // 이후: 카카오 SDK 로그아웃 + 서버 세션 삭제 후 navigate("/login")
  const handleLogout = () => {
    console.log("로그아웃");
    navigate("/login");
  };

  return (
    <div style={S.wrap}>

      {/* 배경 블러 오브 */}
      <div style={S.bgDecor} aria-hidden="true">
        <div style={{ ...S.blob, top: -96, right: -96, background: "#fbcfe8" }} />
        <div style={{ ...S.blob, bottom: -96, left: -96, background: "#e9d5ff" }} />
      </div>

      <div style={S.inner}>

        {/* ── 헤더 ── */}
        <div style={S.headerRow}>
          <button onClick={() => navigate("/")} style={S.backBtn}>← 홈으로</button>
          <p style={S.pageLabel}>마이페이지</p>
        </div>

        {/* ── 1. 프로필 카드 ── */}
        <div style={S.profileCard}>
          {/* 배경 장식 */}
          <div style={S.profileCardDeco} aria-hidden="true" />

          <div style={S.profileTop}>
            {/* 아바타 */}
            {user.profile_image ? (
              <img src={user.profile_image} alt="프로필" style={S.avatar} />
            ) : (
              /* profile_image가 null이면 닉네임 첫 글자로 이니셜 아바타 표시 */
              <div style={S.avatarInitial}>
                {user.nickname.charAt(0)}
              </div>
            )}

            {/* 사용자 정보 */}
            <div>
              <h1 style={S.nickname}>{user.nickname}</h1>
              <p style={S.joinDate}>가입일 {formatDate(user.created_at)}</p>
            </div>
          </div>

          {/* 통계 행 */}
          <div style={S.statsRow}>
            <div style={S.statItem}>
              <span style={S.statValue}>{history.length}</span>
              <span style={S.statLabel}>총 분석 횟수</span>
            </div>
            <div style={S.statDivider} />
            <div style={S.statItem}>
              {/* 가장 많이 나온 페르소나 코드 계산 */}
              <span style={S.statValue}>
                {PERSONA_EMOJI[
                  Object.entries(
                    history.reduce((acc, h) => {
                      acc[h.code] = (acc[h.code] || 0) + 1;
                      return acc;
                    }, {})
                  ).sort((a, b) => b[1] - a[1])[0]?.[0]
                ] || "🔭"}
              </span>
              <span style={S.statLabel}>주요 페르소나</span>
            </div>
            <div style={S.statDivider} />
            <div style={S.statItem}>
              <span style={S.statValue}>
                {/* 최근 분석으로부터 경과 일수 */}
                {Math.floor((new Date() - new Date(history[0]?.analyzed_at)) / (1000 * 60 * 60 * 24))}일
              </span>
              <span style={S.statLabel}>마지막 분석</span>
            </div>
          </div>
        </div>

        {/* ── 2. 현재 페르소나 카드 ── */}
        <div style={{ ...S.personaCard, background: `linear-gradient(135deg, ${pc.bg}, ${C.purpleLight})`, border: `1.5px solid ${pc.color}20` }}>
          <div style={S.personaCardHeader}>
            <span style={{ fontSize: 11, fontWeight: 700, color: pc.color, letterSpacing: "0.1em" }}>현재 페르소나</span>
            <button onClick={() => navigate("/survey")} style={{ ...S.retakeBtn, color: pc.color, borderColor: `${pc.color}40` }}>
              재검사 →
            </button>
          </div>
          <div style={S.personaCardBody}>
            <span style={S.personaEmoji}>{PERSONA_EMOJI[user.persona.code]}</span>
            <div>
              <p style={{ fontSize: 11, color: pc.color, fontWeight: 700, margin: "0 0 2px", letterSpacing: "0.08em" }}>
                {user.persona.code}
              </p>
              <h2 style={S.personaName}>{user.persona.name}</h2>
              <p style={S.personaDesc}>{user.persona.description}</p>
            </div>
          </div>
        </div>

        {/* ── 3. 분석 이력 타임라인 ── */}
        <div style={S.historySection}>
          <p style={S.sectionTitle}>📋 페르소나 분석 이력</p>
          <p style={S.sectionSubtitle}>총 {history.length}회 분석 · 최신순</p>

          <div style={S.timeline}>
            {history.map((item, idx) => {
              const itemPc    = PERSONA_COLOR[item.code] || PERSONA_COLOR.EXPLORER;
              const isLatest  = idx === 0;
              // 이전 분석과 페르소나가 바뀐 경우 강조 표시
              const isChanged = idx > 0 && item.code !== history[idx - 1].code;

              return (
                <div key={item.analysis_id} style={S.timelineItem}>

                  {/* 타임라인 라인 */}
                  {idx < history.length - 1 && <div style={S.timelineLine} />}

                  {/* 도트 */}
                  <div style={{
                    ...S.timelineDot,
                    background: isLatest
                      ? `linear-gradient(135deg, ${C.pink}, ${C.purple})`
                      : itemPc.bg,
                    border: `2px solid ${isLatest ? C.pink : itemPc.color}`,
                  }}>
                    <span style={{ fontSize: 12 }}>{PERSONA_EMOJI[item.code]}</span>
                  </div>

                  {/* 내용 */}
                  <div style={S.timelineContent}>
                    <div style={S.timelineTop}>
                      {/* 페르소나 이름 뱃지 */}
                      <span style={{
                        ...S.timelineBadge,
                        background: itemPc.bg,
                        color: itemPc.color,
                        border: `1px solid ${itemPc.color}30`,
                      }}>
                        {item.name}
                      </span>

                      {/* 변화 표시 */}
                      {isChanged && (
                        <span style={S.changedBadge}>페르소나 변화</span>
                      )}
                      {isLatest && (
                        <span style={S.latestBadge}>최신</span>
                      )}
                    </div>

                    {/* 날짜 */}
                    <p style={S.timelineDate}>{formatDate(item.analyzed_at)}</p>
                  </div>

                </div>
              );
            })}
          </div>
        </div>

        {/* ── 4. 로그아웃 버튼 ── */}
        {!showLogoutConfirm ? (
          <button onClick={() => setShowLogoutConfirm(true)} style={S.logoutBtn}>
            로그아웃
          </button>
        ) : (
          /* 로그아웃 확인 */
          <div style={S.logoutConfirm}>
            <p style={S.logoutConfirmText}>정말 로그아웃 하시겠어요?</p>
            <div style={S.logoutConfirmBtns}>
              <button onClick={() => setShowLogoutConfirm(false)} style={S.cancelBtn}>취소</button>
              <button onClick={handleLogout} style={S.confirmBtn}>로그아웃</button>
            </div>
          </div>
        )}

      </div>
    </div>
  );
}

/* ────────────────────────────────────────
   인라인 스타일
   ──────────────────────────────────────── */
const S = {
  wrap:    { minHeight: "100vh", background: `linear-gradient(135deg, ${C.pinkBg}, ${C.white}, #faf5ff)`, fontFamily: "'Noto Sans KR', sans-serif", position: "relative", overflow: "hidden" },
  bgDecor: { position: "fixed", inset: 0, pointerEvents: "none", overflow: "hidden", zIndex: 0 },
  blob:    { position: "absolute", width: 384, height: 384, borderRadius: "50%", opacity: 0.2, filter: "blur(60px)" },
  inner:   { position: "relative", zIndex: 1, maxWidth: 480, margin: "0 auto", padding: "48px 20px 80px" },

  /* 헤더 */
  headerRow: { display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 24 },
  backBtn:   { fontSize: 13, color: C.gray400, background: "none", border: "none", cursor: "pointer", padding: 0, fontFamily: "'Noto Sans KR', sans-serif" },
  pageLabel: { fontSize: 13, fontWeight: 800, color: C.gray800, margin: 0 },

  /* 프로필 카드 */
  profileCard: {
    position: "relative", overflow: "hidden",
    background: `linear-gradient(135deg, ${C.pink}, ${C.pinkDark}, ${C.purple})`,
    borderRadius: 24, padding: "24px 20px", marginBottom: 16,
    boxShadow: "0 8px 32px rgba(244,114,182,0.3)",
  },
  profileCardDeco: { position: "absolute", top: -40, right: -40, width: 180, height: 180, borderRadius: "50%", background: "rgba(255,255,255,0.08)" },
  profileTop:  { display: "flex", alignItems: "center", gap: 16, marginBottom: 20, position: "relative" },
  avatar:      { width: 60, height: 60, borderRadius: "50%", border: "3px solid rgba(255,255,255,0.4)", objectFit: "cover" },
  avatarInitial: {
    width: 60, height: 60, borderRadius: "50%",
    background: "rgba(255,255,255,0.25)",
    border: "3px solid rgba(255,255,255,0.4)",
    display: "flex", alignItems: "center", justifyContent: "center",
    fontSize: 24, fontWeight: 800, color: C.white,
    flexShrink: 0,
  },
  nickname:  { fontFamily: "'Playfair Display', serif", fontSize: 22, fontWeight: 800, color: C.white, margin: "0 0 4px" },
  joinDate:  { fontSize: 11, color: "rgba(255,255,255,0.7)", margin: 0 },

  /* 통계 행 */
  statsRow:    { display: "flex", alignItems: "center", background: "rgba(255,255,255,0.15)", borderRadius: 16, padding: "12px 16px", position: "relative" },
  statItem:    { flex: 1, display: "flex", flexDirection: "column", alignItems: "center", gap: 2 },
  statValue:   { fontSize: 18, fontWeight: 800, color: C.white },
  statLabel:   { fontSize: 10, color: "rgba(255,255,255,0.7)", fontWeight: 500 },
  statDivider: { width: 1, height: 32, background: "rgba(255,255,255,0.2)" },

  /* 현재 페르소나 카드 */
  personaCard: { borderRadius: 24, padding: "20px", marginBottom: 16, boxShadow: "0 4px 16px rgba(244,114,182,0.08)" },
  personaCardHeader: { display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 14 },
  retakeBtn:   { fontSize: 11, fontWeight: 700, background: "transparent", border: "1px solid", borderRadius: 20, padding: "4px 12px", cursor: "pointer", fontFamily: "'Noto Sans KR', sans-serif" },
  personaCardBody: { display: "flex", alignItems: "flex-start", gap: 14 },
  personaEmoji:{ fontSize: 44, lineHeight: 1 },
  personaName: { fontFamily: "'Playfair Display', serif", fontSize: 20, fontWeight: 800, color: C.gray800, margin: "0 0 6px" },
  personaDesc: { fontSize: 12, color: C.gray500, lineHeight: 1.6, margin: 0 },

  /* 분석 이력 */
  historySection: { background: "rgba(255,255,255,0.75)", backdropFilter: "blur(12px)", border: `1px solid ${C.pinkLight}`, borderRadius: 24, padding: "20px", marginBottom: 16 },
  sectionTitle:   { fontSize: 14, fontWeight: 800, color: C.gray800, margin: "0 0 2px" },
  sectionSubtitle:{ fontSize: 11, color: C.gray400, margin: "0 0 20px" },

  /* 타임라인 */
  timeline:       { display: "flex", flexDirection: "column", gap: 0 },
  timelineItem:   { display: "flex", alignItems: "flex-start", gap: 14, position: "relative", paddingBottom: 20 },
  timelineLine:   { position: "absolute", left: 19, top: 40, bottom: 0, width: 2, background: C.gray100 },
  timelineDot:    { width: 40, height: 40, borderRadius: 14, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0, zIndex: 1 },
  timelineContent:{ flex: 1, paddingTop: 8 },
  timelineTop:    { display: "flex", alignItems: "center", gap: 6, flexWrap: "wrap", marginBottom: 4 },
  timelineBadge:  { fontSize: 12, fontWeight: 700, padding: "3px 10px", borderRadius: 20 },
  changedBadge:   { fontSize: 10, fontWeight: 700, color: C.purple, background: C.purpleLight, padding: "2px 8px", borderRadius: 20 },
  latestBadge:    { fontSize: 10, fontWeight: 700, color: C.white, background: `linear-gradient(135deg, ${C.pink}, ${C.purple})`, padding: "2px 8px", borderRadius: 20 },
  timelineDate:   { fontSize: 11, color: C.gray400, margin: 0 },

  /* 로그아웃 */
  logoutBtn:     { width: "100%", padding: "14px 0", borderRadius: 18, border: `1px solid ${C.gray200}`, background: C.white, color: C.gray500, fontSize: 14, fontWeight: 700, cursor: "pointer", fontFamily: "'Noto Sans KR', sans-serif", transition: "all 0.2s" },
  logoutConfirm: { background: "rgba(255,255,255,0.9)", backdropFilter: "blur(12px)", border: `1px solid ${C.gray200}`, borderRadius: 20, padding: "20px", textAlign: "center" },
  logoutConfirmText: { fontSize: 14, fontWeight: 700, color: C.gray800, margin: "0 0 16px" },
  logoutConfirmBtns: { display: "flex", gap: 10 },
  cancelBtn:  { flex: 1, padding: "12px 0", borderRadius: 14, border: `1px solid ${C.gray200}`, background: C.white, color: C.gray500, fontSize: 14, fontWeight: 700, cursor: "pointer", fontFamily: "'Noto Sans KR', sans-serif" },
  confirmBtn: { flex: 1, padding: "12px 0", borderRadius: 14, border: "none", background: C.red, color: C.white, fontSize: 14, fontWeight: 700, cursor: "pointer", fontFamily: "'Noto Sans KR', sans-serif" },
};
