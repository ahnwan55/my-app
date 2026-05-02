import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

/**
 * MyPage.jsx — 마이페이지
 *
 * [도서관 등록 흐름]
 *   1. 슬롯 선택 (메인 / 서브)
 *   2. 도서관 이름 입력 → GET /api/libraries?keyword={keyword} 자동 검색
 *   3. 결과 선택 → 슬롯에 등록
 *   4. 저장하기 → PATCH /api/users/me/libraries
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

const PERSONA_EMOJI = {
  EXPLORER:  "🔭",
  CURATOR:   "🌸",
  NAVIGATOR: "⚓",
  DWELLER:   "☕",
  ANALYST:   "♟",
  DIVER:     "🌊",
};

const PERSONA_COLOR = {
  EXPLORER:  { color: C.pinkDark,  bg: C.pinkLight   },
  CURATOR:   { color: C.purpleDark,bg: C.purpleLight  },
  NAVIGATOR: { color: "#2563eb",   bg: "#dbeafe"      },
  DWELLER:   { color: "#d97706",   bg: "#fef3c7"      },
  ANALYST:   { color: "#059669",   bg: "#d1fae5"      },
  DIVER:     { color: "#4f46e5",   bg: "#e0e7ff"      },
};

const DUMMY_USER = {
  nickname:      "시완",
  profile_image: null,
  created_at:    "2025-03-01",
  persona: {
    code:        "EXPLORER",
    name:        "지적 탐험가",
    description: "새로운 지식·개념에 대한 탐구 욕구가 강하며, 다양한 분야를 폭넓게 탐색합니다.",
  },
};

const DUMMY_HISTORY = [
  { analysis_id: 4, code: "EXPLORER",  name: "지적 탐험가", analyzed_at: "2025-04-20" },
  { analysis_id: 3, code: "ANALYST",   name: "분석가",      analyzed_at: "2025-03-15" },
  { analysis_id: 2, code: "ANALYST",   name: "분석가",      analyzed_at: "2025-02-28" },
  { analysis_id: 1, code: "NAVIGATOR", name: "네비게이터",  analyzed_at: "2025-03-01" },
];

function formatDate(dateStr) {
  const d = new Date(dateStr);
  return `${d.getFullYear()}년 ${d.getMonth() + 1}월 ${d.getDate()}일`;
}

export default function MyPage() {
  const navigate = useNavigate();
  const [showLogoutConfirm, setShowLogoutConfirm] = useState(false);

  const user    = DUMMY_USER;
  const history = DUMMY_HISTORY;
  const pc      = PERSONA_COLOR[user.persona.code] || PERSONA_COLOR.EXPLORER;

  // ── 도서관 등록 상태 ────────────────────────────────────────
  const [libKeyword,   setLibKeyword]   = useState("");
  const [libResults,   setLibResults]   = useState([]);
  const [libSearching, setLibSearching] = useState(false);
  const [activeSlot,   setActiveSlot]   = useState(null);   // "main" | "sub" | null
  const [mainLib,      setMainLib]      = useState(null);
  const [subLib,       setSubLib]       = useState(null);
  const [libSaving,    setLibSaving]    = useState(false);
  const [libSaveMsg,   setLibSaveMsg]   = useState("");

  // 500ms 디바운스 검색
  useEffect(() => {
    if (!libKeyword.trim()) { setLibResults([]); return; }

    const timer = setTimeout(async () => {
      setLibSearching(true);
      try {
        const res = await fetch(
          `/api/libraries?keyword=${encodeURIComponent(libKeyword.trim())}`,
          { credentials: "include" }
        );
        if (!res.ok) throw new Error();
        setLibResults(await res.json());
      } catch {
        setLibResults([]);
      } finally {
        setLibSearching(false);
      }
    }, 500);

    return () => clearTimeout(timer);
  }, [libKeyword]);

  const handleSelectLib = (lib) => {
    if (activeSlot === "main") setMainLib(lib);
    if (activeSlot === "sub")  setSubLib(lib);
    setLibKeyword("");
    setLibResults([]);
    setActiveSlot(null);
  };

  const handleSaveLibraries = async () => {
    if (!mainLib) { setLibSaveMsg("메인 도서관은 필수입니다."); return; }

    setLibSaving(true);
    setLibSaveMsg("");
    try {
      const res = await fetch("/api/users/me/libraries", {
        method: "PATCH",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          mainLibraryCode: mainLib.libraryCode,
          subLibraryCode:  subLib?.libraryCode ?? null,
        }),
      });
      if (!res.ok) throw new Error();
      setLibSaveMsg("저장되었습니다.");
    } catch {
      setLibSaveMsg("저장에 실패했어요. 다시 시도해주세요.");
    } finally {
      setLibSaving(false);
    }
  };

  const handleLogout = () => {
    navigate("/login");
  };

  return (
    <div style={S.wrap}>
      <div style={S.bgDecor} aria-hidden="true">
        <div style={{ ...S.blob, top: -96, right: -96, background: "#fbcfe8" }} />
        <div style={{ ...S.blob, bottom: -96, left: -96, background: "#e9d5ff" }} />
      </div>

      <div style={S.inner}>

        {/* 헤더 */}
        <div style={S.headerRow}>
          <button onClick={() => navigate("/")} style={S.backBtn}>← 홈으로</button>
          <p style={S.pageLabel}>마이페이지</p>
        </div>

        {/* 1. 프로필 카드 */}
        <div style={S.profileCard}>
          <div style={S.profileCardDeco} aria-hidden="true" />
          <div style={S.profileTop}>
            {user.profile_image
              ? <img src={user.profile_image} alt="프로필" style={S.avatar} />
              : <div style={S.avatarInitial}>{user.nickname.charAt(0)}</div>
            }
            <div>
              <h1 style={S.nickname}>{user.nickname}</h1>
              <p style={S.joinDate}>가입일 {formatDate(user.created_at)}</p>
            </div>
          </div>
          <div style={S.statsRow}>
            <div style={S.statItem}>
              <span style={S.statValue}>{history.length}</span>
              <span style={S.statLabel}>총 분석 횟수</span>
            </div>
            <div style={S.statDivider} />
            <div style={S.statItem}>
              <span style={S.statValue}>
                {PERSONA_EMOJI[
                  Object.entries(
                    history.reduce((acc, h) => { acc[h.code] = (acc[h.code] || 0) + 1; return acc; }, {})
                  ).sort((a, b) => b[1] - a[1])[0]?.[0]
                ] || "🔭"}
              </span>
              <span style={S.statLabel}>주요 페르소나</span>
            </div>
            <div style={S.statDivider} />
            <div style={S.statItem}>
              <span style={S.statValue}>
                {Math.floor((new Date() - new Date(history[0]?.analyzed_at)) / (1000 * 60 * 60 * 24))}일
              </span>
              <span style={S.statLabel}>마지막 분석</span>
            </div>
          </div>
        </div>

        {/* 2. 현재 페르소나 카드 */}
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

        {/* 3. 도서관 등록 섹션 */}
        <div style={S.libSection}>
          <p style={S.sectionTitle}>📍 내 도서관 등록</p>
          <p style={S.sectionSubtitle}>등록한 도서관의 재고를 자동으로 확인할 수 있어요.</p>

          <div style={S.libSlotList}>
            {[
              { key: "main", label: "메인 도서관",  value: mainLib, required: true  },
              { key: "sub",  label: "서브 도서관",   value: subLib,  required: false },
            ].map(({ key, label, value, required }) => (
              <div key={key} style={S.libSlot}>
                <div style={S.libSlotHeader}>
                  <span style={S.libSlotLabel}>
                    {label}{required && <span style={{ color: C.pink }}> *</span>}
                  </span>
                  {value && (
                    <button style={S.libSlotClear} onClick={() => key === "main" ? setMainLib(null) : setSubLib(null)}>
                      ✕
                    </button>
                  )}
                </div>

                {value ? (
                  <div style={S.libSelected}>
                    <span style={S.libSelectedName}>{value.name}</span>
                    <span style={S.libSelectedCode}>{value.libraryCode}</span>
                  </div>
                ) : (
                  <button
                    style={{
                      ...S.libSlotBtn,
                      borderColor: activeSlot === key ? C.pink : C.gray200,
                      color: activeSlot === key ? C.pinkDark : C.gray400,
                    }}
                    onClick={() => setActiveSlot(activeSlot === key ? null : key)}
                  >
                    + 도서관 선택
                  </button>
                )}
              </div>
            ))}
          </div>

          {/* 검색창 */}
          {activeSlot && (
            <div style={S.libSearchBox}>
              <input
                style={S.libSearchInput}
                type="text"
                placeholder="도서관 이름 검색 (예: 노원)"
                value={libKeyword}
                onChange={(e) => setLibKeyword(e.target.value)}
                autoFocus
              />
              {libSearching && <p style={S.libSearchMsg}>검색 중...</p>}
              {!libSearching && libResults.length > 0 && (
                <div style={S.libResultList}>
                  {libResults.map((lib) => (
                    <button key={lib.libraryCode} style={S.libResultItem} onClick={() => handleSelectLib(lib)}>
                      <span style={S.libResultName}>{lib.name}</span>
                      <span style={S.libResultAddr}>{lib.address}</span>
                    </button>
                  ))}
                </div>
              )}
              {!libSearching && libKeyword && libResults.length === 0 && (
                <p style={S.libSearchMsg}>검색 결과가 없어요.</p>
              )}
            </div>
          )}

          <button
            style={{ ...S.libSaveBtn, opacity: libSaving ? 0.6 : 1, cursor: libSaving ? "not-allowed" : "pointer" }}
            onClick={handleSaveLibraries}
            disabled={libSaving}
          >
            {libSaving ? "저장 중..." : "저장하기"}
          </button>

          {libSaveMsg && (
            <p style={{ ...S.libSaveMsg, color: libSaveMsg === "저장되었습니다." ? "#16a34a" : C.red }}>
              {libSaveMsg}
            </p>
          )}
        </div>

        {/* 4. 분석 이력 타임라인 */}
        <div style={S.historySection}>
          <p style={S.sectionTitle}>📋 페르소나 분석 이력</p>
          <p style={S.sectionSubtitle}>총 {history.length}회 분석 · 최신순</p>
          <div style={S.timeline}>
            {history.map((item, idx) => {
              const itemPc    = PERSONA_COLOR[item.code] || PERSONA_COLOR.EXPLORER;
              const isLatest  = idx === 0;
              const isChanged = idx > 0 && item.code !== history[idx - 1].code;
              return (
                <div key={item.analysis_id} style={S.timelineItem}>
                  {idx < history.length - 1 && <div style={S.timelineLine} />}
                  <div style={{ ...S.timelineDot, background: isLatest ? `linear-gradient(135deg, ${C.pink}, ${C.purple})` : itemPc.bg, border: `2px solid ${isLatest ? C.pink : itemPc.color}` }}>
                    <span style={{ fontSize: 12 }}>{PERSONA_EMOJI[item.code]}</span>
                  </div>
                  <div style={S.timelineContent}>
                    <div style={S.timelineTop}>
                      <span style={{ ...S.timelineBadge, background: itemPc.bg, color: itemPc.color, border: `1px solid ${itemPc.color}30` }}>{item.name}</span>
                      {isChanged && <span style={S.changedBadge}>페르소나 변화</span>}
                      {isLatest  && <span style={S.latestBadge}>최신</span>}
                    </div>
                    <p style={S.timelineDate}>{formatDate(item.analyzed_at)}</p>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* 5. 로그아웃 */}
        {!showLogoutConfirm ? (
          <button onClick={() => setShowLogoutConfirm(true)} style={S.logoutBtn}>로그아웃</button>
        ) : (
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

const S = {
  wrap:    { minHeight: "100vh", background: `linear-gradient(135deg, ${C.pinkBg}, ${C.white}, #faf5ff)`, fontFamily: "'Noto Sans KR', sans-serif", position: "relative", overflow: "hidden" },
  bgDecor: { position: "fixed", inset: 0, pointerEvents: "none", overflow: "hidden", zIndex: 0 },
  blob:    { position: "absolute", width: 384, height: 384, borderRadius: "50%", opacity: 0.2, filter: "blur(60px)" },
  inner:   { position: "relative", zIndex: 1, maxWidth: 480, margin: "0 auto", padding: "48px 20px 80px" },

  headerRow: { display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 24 },
  backBtn:   { fontSize: 13, color: C.gray400, background: "none", border: "none", cursor: "pointer", padding: 0, fontFamily: "'Noto Sans KR', sans-serif" },
  pageLabel: { fontSize: 13, fontWeight: 800, color: C.gray800, margin: 0 },

  profileCard: { position: "relative", overflow: "hidden", background: `linear-gradient(135deg, ${C.pink}, ${C.pinkDark}, ${C.purple})`, borderRadius: 24, padding: "24px 20px", marginBottom: 16, boxShadow: "0 8px 32px rgba(244,114,182,0.3)" },
  profileCardDeco: { position: "absolute", top: -40, right: -40, width: 180, height: 180, borderRadius: "50%", background: "rgba(255,255,255,0.08)" },
  profileTop:  { display: "flex", alignItems: "center", gap: 16, marginBottom: 20, position: "relative" },
  avatar:      { width: 60, height: 60, borderRadius: "50%", border: "3px solid rgba(255,255,255,0.4)", objectFit: "cover" },
  avatarInitial: { width: 60, height: 60, borderRadius: "50%", background: "rgba(255,255,255,0.25)", border: "3px solid rgba(255,255,255,0.4)", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 24, fontWeight: 800, color: C.white, flexShrink: 0 },
  nickname:  { fontFamily: "'Playfair Display', serif", fontSize: 22, fontWeight: 800, color: C.white, margin: "0 0 4px" },
  joinDate:  { fontSize: 11, color: "rgba(255,255,255,0.7)", margin: 0 },
  statsRow:    { display: "flex", alignItems: "center", background: "rgba(255,255,255,0.15)", borderRadius: 16, padding: "12px 16px" },
  statItem:    { flex: 1, display: "flex", flexDirection: "column", alignItems: "center", gap: 2 },
  statValue:   { fontSize: 18, fontWeight: 800, color: C.white },
  statLabel:   { fontSize: 10, color: "rgba(255,255,255,0.7)", fontWeight: 500 },
  statDivider: { width: 1, height: 32, background: "rgba(255,255,255,0.2)" },

  personaCard: { borderRadius: 24, padding: "20px", marginBottom: 16, boxShadow: "0 4px 16px rgba(244,114,182,0.08)" },
  personaCardHeader: { display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 14 },
  retakeBtn:   { fontSize: 11, fontWeight: 700, background: "transparent", border: "1px solid", borderRadius: 20, padding: "4px 12px", cursor: "pointer", fontFamily: "'Noto Sans KR', sans-serif" },
  personaCardBody: { display: "flex", alignItems: "flex-start", gap: 14 },
  personaEmoji:{ fontSize: 44, lineHeight: 1 },
  personaName: { fontFamily: "'Playfair Display', serif", fontSize: 20, fontWeight: 800, color: C.gray800, margin: "0 0 6px" },
  personaDesc: { fontSize: 12, color: C.gray500, lineHeight: 1.6, margin: 0 },

  libSection: { background: "rgba(255,255,255,0.75)", backdropFilter: "blur(12px)", border: `1px solid ${C.pinkLight}`, borderRadius: 24, padding: "20px", marginBottom: 16 },
  sectionTitle:    { fontSize: 14, fontWeight: 800, color: C.gray800, margin: "0 0 2px" },
  sectionSubtitle: { fontSize: 11, color: C.gray400, margin: "0 0 16px" },

  libSlotList: { display: "flex", flexDirection: "column", gap: 10, marginBottom: 14 },
  libSlot:     { display: "flex", flexDirection: "column", gap: 6 },
  libSlotHeader: { display: "flex", alignItems: "center", justifyContent: "space-between" },
  libSlotLabel:  { fontSize: 12, fontWeight: 700, color: C.gray700 },
  libSlotClear:  { fontSize: 11, color: C.gray400, background: "none", border: "none", cursor: "pointer" },
  libSlotBtn: { width: "100%", padding: "10px 14px", textAlign: "left", border: "1.5px dashed", borderRadius: 12, background: C.gray50, fontSize: 13, cursor: "pointer", fontFamily: "'Noto Sans KR', sans-serif" },
  libSelected: { display: "flex", alignItems: "center", justifyContent: "space-between", background: C.pinkLight, borderRadius: 12, padding: "10px 14px", border: `1.5px solid ${C.pink}` },
  libSelectedName: { fontSize: 13, fontWeight: 700, color: C.pinkDark },
  libSelectedCode: { fontSize: 11, color: C.pink },

  libSearchBox:   { marginBottom: 14 },
  libSearchInput: { width: "100%", padding: "10px 14px", borderRadius: 12, border: `1.5px solid ${C.pink}`, fontSize: 13, outline: "none", boxSizing: "border-box", fontFamily: "'Noto Sans KR', sans-serif", marginBottom: 6 },
  libSearchMsg:   { fontSize: 12, color: C.gray400, textAlign: "center", margin: "8px 0" },
  libResultList:  { border: `1px solid ${C.gray200}`, borderRadius: 12, overflow: "hidden", maxHeight: 200, overflowY: "auto" },
  libResultItem:  { width: "100%", padding: "10px 14px", textAlign: "left", background: C.white, border: "none", borderBottom: `1px solid ${C.gray100}`, cursor: "pointer", display: "flex", flexDirection: "column", gap: 2, fontFamily: "'Noto Sans KR', sans-serif" },
  libResultName:  { fontSize: 13, fontWeight: 700, color: C.gray800 },
  libResultAddr:  { fontSize: 11, color: C.gray400 },

  libSaveBtn: { width: "100%", padding: "13px 0", borderRadius: 16, border: "none", background: `linear-gradient(135deg, ${C.pink}, ${C.purple})`, color: C.white, fontSize: 14, fontWeight: 800, fontFamily: "'Noto Sans KR', sans-serif", boxShadow: "0 4px 12px rgba(244,114,182,0.3)" },
  libSaveMsg: { fontSize: 12, textAlign: "center", margin: "8px 0 0", fontWeight: 600 },

  historySection: { background: "rgba(255,255,255,0.75)", backdropFilter: "blur(12px)", border: `1px solid ${C.pinkLight}`, borderRadius: 24, padding: "20px", marginBottom: 16 },
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

  logoutBtn:     { width: "100%", padding: "14px 0", borderRadius: 18, border: `1px solid ${C.gray200}`, background: C.white, color: C.gray500, fontSize: 14, fontWeight: 700, cursor: "pointer", fontFamily: "'Noto Sans KR', sans-serif" },
  logoutConfirm: { background: "rgba(255,255,255,0.9)", backdropFilter: "blur(12px)", border: `1px solid ${C.gray200}`, borderRadius: 20, padding: "20px", textAlign: "center" },
  logoutConfirmText: { fontSize: 14, fontWeight: 700, color: C.gray800, margin: "0 0 16px" },
  logoutConfirmBtns: { display: "flex", gap: 10 },
  cancelBtn:  { flex: 1, padding: "12px 0", borderRadius: 14, border: `1px solid ${C.gray200}`, background: C.white, color: C.gray500, fontSize: 14, fontWeight: 700, cursor: "pointer", fontFamily: "'Noto Sans KR', sans-serif" },
  confirmBtn: { flex: 1, padding: "12px 0", borderRadius: 14, border: "none", background: C.red, color: C.white, fontSize: 14, fontWeight: 700, cursor: "pointer", fontFamily: "'Noto Sans KR', sans-serif" },
};
