import { useState, useEffect } from "react";

/**
 * BookLoadingPage.jsx — 도서 추천 로딩 페이지
 *
 * Props:
 *  personaName {string}   — 대분류 페르소나 이름 (예: "지적 탐험가")
 *                           App.jsx에서 state로 관리 후 전달
 *  onComplete  {Function} — 추천 완료 시 호출
 *                           App.jsx에서 navigate("/books") 실행
 *
 * 현재 동작:
 *  1. personaName을 포함한 로딩 문구 순서대로 전환
 *  2. 원형 스피너 (LoadingPage와 동일 구조, 책 테마 색상)
 *  3. TOTAL_DURATION 후 onComplete() 호출 (더미)
 *
 * 이후 연동:
 *  - Spring Boot GET /api/books/recommend?persona={personaCode} 호출
 *  - 응답 수신 후 onComplete() 호출
 *  - 도서 목록은 navigate("/books", { state: { books } }) 로 전달
 *
 * 컬러: 벚꽃 핑크(#f472b6) × 퍼플(#a855f7)
 */

const C = {
  pink:        "#f472b6",
  pinkDark:    "#ec4899",
  pinkLight:   "#fce7f3",
  pinkBg:      "#fdf2f8",
  purple:      "#a855f7",
  purpleLight: "#ede9fe",
  white:       "#ffffff",
  gray400:     "#9ca3af",
  gray800:     "#1f2937",
};

const MESSAGE_INTERVAL = 1800; // 문구 전환 간격 (ms)
const TOTAL_DURATION   = 7000; // 전체 로딩 시간 (ms) — API 연동 시 실제 응답 시간으로 대체

/**
 * buildMessages
 * personaName을 받아 로딩 문구 배열을 생성
 * API 연동 후에도 문구만 수정하면 되도록 함수로 분리
 */
function buildMessages(personaName) {
  return [
    { text: `${personaName}의 독서 프로파일을 불러오고 있어요...`, emoji: "🧭" },
    { text: `${personaName}에게 맞는 도서를 탐색하고 있어요...`,   emoji: "🔍" },
    { text: "도서관 데이터베이스를 분석하고 있어요...",             emoji: "📚" },
    { text: `${personaName}만을 위한 큐레이션을 완성하고 있어요...`, emoji: "✨" },
    { text: "거의 다 됐어요!",                                      emoji: "🎉" },
  ];
}

export default function BookLoadingPage({ personaName, onComplete }) {
  const messages = buildMessages(personaName || "탐험가");

  const [msgIndex, setMsgIndex] = useState(0);
  const [visible,  setVisible]  = useState(true);
  const [progress, setProgress] = useState(0);

  /* ── 문구 순환 ── */
  useEffect(() => {
    const interval = setInterval(() => {
      setVisible(false);
      setTimeout(() => {
        setMsgIndex((prev) => (prev + 1) % messages.length);
        setVisible(true);
      }, 300);
    }, MESSAGE_INTERVAL);

    return () => clearInterval(interval);
  }, [messages.length]);

  /* ── 진행 바 ── */
  useEffect(() => {
    const step     = 100 / (TOTAL_DURATION / 100);
    const interval = setInterval(() => {
      setProgress((prev) => Math.min(prev + step, 95));
    }, 100);

    return () => clearInterval(interval);
  }, []);

  /* ── 완료 처리 ──
   * 현재: TOTAL_DURATION 후 onComplete() 호출
   * 이후: Spring Boot 응답 수신 시 즉시 호출로 교체
   *
   * Spring Boot 연동 예시:
   *   const res = await fetch(`/api/books/recommend?persona=${personaCode}`);
   *   const { books } = await res.json();
   *   setProgress(100);
   *   setTimeout(() => onComplete(), 400);
   */
  useEffect(() => {
    const timer = setTimeout(() => {
      setProgress(100);
      setTimeout(() => onComplete(), 400);
    }, TOTAL_DURATION);

    return () => clearTimeout(timer);
  }, [onComplete]);

  const currentMsg = messages[msgIndex];

  return (
    <div style={S.wrap}>

      {/* 배경 블러 오브 */}
      <div style={S.bgDecor} aria-hidden="true">
        <div style={{ ...S.blob, top: -96, right: -96, background: "#fbcfe8" }} />
        <div style={{ ...S.blob, bottom: -96, left: -96, background: "#e9d5ff" }} />
      </div>

      <div style={S.inner}>

        {/* 페르소나 뱃지 */}
        <div style={S.personaBadge}>
          <span style={{ fontSize: 14 }}>🎭</span>
          <span style={S.personaBadgeText}>{personaName || "탐험가"}</span>
        </div>

        {/* ── 원형 스피너 ── */}
        <div style={S.spinnerWrap} aria-label="도서 추천 중">
          <div style={S.spinnerOuter} />
          <div style={S.spinnerInner} />
          {/* 중앙 — 책 스택 아이콘 */}
          <div style={S.spinnerCenter}>
            <span style={{ fontSize: 32 }}>📖</span>
          </div>
        </div>

        {/* ── 문구 전환 ── */}
        <div style={S.msgWrap}>
          <span style={{
            ...S.msgEmoji,
            opacity:   visible ? 1 : 0,
            transform: visible ? "translateY(0)" : "translateY(-8px)",
          }}>
            {currentMsg.emoji}
          </span>
          <p style={{
            ...S.msgText,
            opacity:   visible ? 1 : 0,
            transform: visible ? "translateY(0)" : "translateY(8px)",
          }}>
            {currentMsg.text}
          </p>
        </div>

        {/* ── 진행 바 ── */}
        <div style={S.progressWrap}>
          <div style={S.progressTrack}>
            <div style={{ ...S.progressBar, width: `${progress}%` }} />
          </div>
          <p style={S.progressLabel}>{Math.floor(progress)}%</p>
        </div>

        {/* ── 안내 문구 ── */}
        <p style={S.notice}>
          페르소나 분석 결과를 바탕으로<br />
          맞춤 도서를 선별하고 있어요.
        </p>

      </div>

      {/* 스피너 CSS 애니메이션 */}
      <style>{`
        @keyframes spin-cw  { from { transform: rotate(0deg);   } to { transform: rotate(360deg);  } }
        @keyframes spin-ccw { from { transform: rotate(0deg);   } to { transform: rotate(-360deg); } }
      `}</style>
    </div>
  );
}

const S = {
  wrap: {
    minHeight: "100vh",
    background: `linear-gradient(135deg, ${C.pinkBg}, ${C.white}, #faf5ff)`,
    fontFamily: "'Noto Sans KR', sans-serif",
    position: "relative", overflow: "hidden",
    display: "flex", alignItems: "center", justifyContent: "center",
  },
  bgDecor: { position: "fixed", inset: 0, pointerEvents: "none", overflow: "hidden", zIndex: 0 },
  blob:    { position: "absolute", width: 384, height: 384, borderRadius: "50%", opacity: 0.2, filter: "blur(60px)" },
  inner: {
    position: "relative", zIndex: 1,
    maxWidth: 360, width: "100%", padding: "0 24px",
    display: "flex", flexDirection: "column", alignItems: "center", gap: 28,
  },

  /* 페르소나 뱃지 */
  personaBadge: {
    display: "flex", alignItems: "center", gap: 8,
    background: `linear-gradient(135deg, ${C.pinkLight}, ${C.purpleLight})`,
    border: `1.5px solid ${C.pinkLight}`,
    borderRadius: 20, padding: "6px 16px",
  },
  personaBadgeText: { fontSize: 13, fontWeight: 800, color: C.pinkDark },

  /* 스피너 */
  spinnerWrap: {
    position: "relative",
    width: 140, height: 140,
    display: "flex", alignItems: "center", justifyContent: "center",
  },
  spinnerOuter: {
    position: "absolute", inset: 0,
    borderRadius: "50%",
    border: `4px solid ${C.pinkLight}`,
    borderTopColor: C.pink,
    animation: "spin-cw 1.4s linear infinite",
  },
  spinnerInner: {
    position: "absolute", inset: 16,
    borderRadius: "50%",
    border: `3px solid ${C.purpleLight}`,
    borderBottomColor: C.purple,
    animation: "spin-ccw 1.0s linear infinite",
  },
  spinnerCenter: {
    width: 72, height: 72, borderRadius: "50%",
    background: `linear-gradient(135deg, ${C.pinkLight}, ${C.purpleLight})`,
    display: "flex", alignItems: "center", justifyContent: "center",
    boxShadow: "0 4px 16px rgba(244,114,182,0.2)",
  },

  /* 문구 전환 */
  msgWrap:  { textAlign: "center", minHeight: 72, display: "flex", flexDirection: "column", alignItems: "center", gap: 8 },
  msgEmoji: { fontSize: 28, transition: "opacity 0.3s ease, transform 0.3s ease", display: "block" },
  msgText:  { fontSize: 15, fontWeight: 700, color: C.gray800, margin: 0, transition: "opacity 0.3s ease, transform 0.3s ease", lineHeight: 1.6, textAlign: "center" },

  /* 진행 바 */
  progressWrap:  { width: "100%", display: "flex", flexDirection: "column", alignItems: "center", gap: 8 },
  progressTrack: { width: "100%", height: 6, background: C.pinkLight, borderRadius: 4, overflow: "hidden" },
  progressBar:   { height: "100%", background: `linear-gradient(90deg, ${C.pink}, ${C.purple})`, borderRadius: 4, transition: "width 0.1s linear" },
  progressLabel: { fontSize: 12, fontWeight: 700, color: C.pink, margin: 0 },

  /* 안내 문구 */
  notice: { fontSize: 12, color: C.gray400, textAlign: "center", lineHeight: 1.7, margin: 0 },
};
