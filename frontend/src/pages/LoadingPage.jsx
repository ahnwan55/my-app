import { useState, useEffect } from "react";

/**
 * LoadingPage.jsx — AI 분석 로딩 페이지
 *
 * Props:
 *  surveyAnswers {object}   — SurveyPage에서 전달받은 설문 답변
 *                             { Q1: "...", Q2: "...", ..., Q10: "..." }
 *  onComplete    {Function} — 분석 완료 시 (code, name, scores) 인자로 호출
 *                             App.jsx에서 state 저장 후 navigate("/result") 실행
 *
 * API 연동:
 *  POST /api/surveys/submit
 *  요청: { answers: { Q1: "...", ..., Q10: "..." } }
 *  응답: { personaCode, personaName, personaReason, scores }
 *    - personaCode: 서브 페르소나 코드 (예: "TREND_SURFER")
 *    - personaName: 서브 페르소나 이름 (예: "트렌드 서퍼")
 *    - scores: 6대 지표 점수 Map (Radar Chart용)
 *              { 지적_확장성: 8.5, 분석적_깊이: 4.0, ... }
 *
 * 완료 후 onComplete(personaCode, personaName, scores) 호출
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
  gray500:     "#6b7280",
  gray700:     "#374151",
  gray800:     "#1f2937",
  red:         "#ef4444",
};

const LOADING_MESSAGES = [
  { text: "답변을 분석하고 있어요...",           emoji: "📝" },
  { text: "AI가 독서 유형을 파악하고 있어요...",  emoji: "🤖" },
  { text: "독서 성향을 계산하고 있어요...",       emoji: "📊" },
  { text: "나만의 페르소나를 완성하고 있어요...", emoji: "🎭" },
  { text: "거의 다 됐어요!",                     emoji: "✨" },
];

const MESSAGE_INTERVAL = 1800; // 문구 전환 간격 (ms)

export default function LoadingPage({ surveyAnswers, onComplete }) {
  const [msgIndex, setMsgIndex] = useState(0);
  const [visible,  setVisible]  = useState(true);
  const [progress, setProgress] = useState(0);
  const [apiError, setApiError] = useState(""); // API 실패 시 에러 메시지

  /* ── 문구 순환 ── */
  useEffect(() => {
    const interval = setInterval(() => {
      setVisible(false);
      setTimeout(() => {
        setMsgIndex((prev) => (prev + 1) % LOADING_MESSAGES.length);
        setVisible(true);
      }, 300);
    }, MESSAGE_INTERVAL);
    return () => clearInterval(interval);
  }, []);

  /* ── 진행 바 (API 응답 전까지 95%까지 자동 진행) ── */
  useEffect(() => {
    const interval = setInterval(() => {
      setProgress((prev) => Math.min(prev + 1, 95));
    }, 150);
    return () => clearInterval(interval);
  }, []);

  /* ── POST /api/surveys/submit 호출 ──
   *
   * 응답 구조 (SurveyDto.SubmitResponse):
   *  {
   *    personaCode: "TREND_SURFER",
   *    personaName: "트렌드 서퍼",
   *    personaReason: "...",
   *    scores: {
   *      "지적_확장성": 8.5,
   *      "분석적_깊이": 4.0,
   *      "실용_지향성": 6.0,
   *      "감성_몰입도": 3.5,
   *      "정보_체계화": 5.0,
   *      "사회적_영향도": 4.5
   *    }
   *  }
   */
  useEffect(() => {
    const submitSurvey = async () => {
      try {
        const res = await fetch("/api/surveys/submit", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          credentials: "include",  // httpOnly 쿠키(JWT) 자동 포함
          body: JSON.stringify({ answers: surveyAnswers }),
        });

        if (!res.ok) throw new Error(`HTTP ${res.status}`);

        const data = await res.json();

        // 진행 바 100% 완료 후 결과 페이지로 이동
        setProgress(100);
        setTimeout(() => {
          onComplete(
            data.personaCode,  // 서브 페르소나 코드 (예: TREND_SURFER)
            data.personaName,  // 서브 페르소나 이름 (예: 트렌드 서퍼)
            data.scores        // 6대 지표 점수 Map
          );
        }, 500);

      } catch (e) {
        setApiError("분석 중 오류가 발생했어요. 다시 시도해주세요.");
        setProgress(0);
      }
    };

    submitSurvey();
  }, []);

  const currentMsg = LOADING_MESSAGES[msgIndex];

  /* ── API 에러 화면 ── */
  if (apiError) {
    return (
      <div style={{ ...S.wrap, display: "flex", alignItems: "center", justifyContent: "center" }}>
        <div style={S.bgDecor} aria-hidden="true">
          <div style={{ ...S.blob, top: -96, right: -96, background: "#fbcfe8" }} />
          <div style={{ ...S.blob, bottom: -96, left: -96, background: "#e9d5ff" }} />
        </div>
        <div style={{ position: "relative", zIndex: 1, textAlign: "center", padding: "0 24px" }}>
          <span style={{ fontSize: 48 }}>😥</span>
          <p style={{ fontSize: 15, color: C.gray700, marginTop: 16 }}>{apiError}</p>
          <button onClick={() => window.history.back()} style={S.retryBtn}>
            돌아가기
          </button>
        </div>
      </div>
    );
  }

  return (
    <div style={S.wrap}>
      <div style={S.bgDecor} aria-hidden="true">
        <div style={{ ...S.blob, top: -96, right: -96, background: "#fbcfe8" }} />
        <div style={{ ...S.blob, bottom: -96, left: -96, background: "#e9d5ff" }} />
      </div>

      <div style={S.inner}>

        {/* ── 원형 스피너 ── */}
        <div style={S.spinnerWrap} aria-label="분석 중">
          <div style={S.spinnerOuter} />
          <div style={S.spinnerInner} />
          <div style={S.spinnerCenter}>
            <span style={{ fontSize: 32 }}>📚</span>
          </div>
        </div>

        {/* ── 문구 전환 ── */}
        <div style={S.msgWrap}>
          <span style={{ ...S.msgEmoji, opacity: visible ? 1 : 0, transform: visible ? "translateY(0)" : "translateY(-8px)" }}>
            {currentMsg.emoji}
          </span>
          <p style={{ ...S.msgText, opacity: visible ? 1 : 0, transform: visible ? "translateY(0)" : "translateY(8px)" }}>
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

        <p style={S.notice}>AI가 10개의 지표를 분석하고 있어요.<br />잠시만 기다려주세요.</p>
      </div>

      <style>{`
        @keyframes spin-cw  { from { transform: rotate(0deg);   } to { transform: rotate(360deg);  } }
        @keyframes spin-ccw { from { transform: rotate(0deg);   } to { transform: rotate(-360deg); } }
      `}</style>
    </div>
  );
}

const S = {
  wrap:    { minHeight: "100vh", background: `linear-gradient(135deg, ${C.pinkBg}, ${C.white}, #faf5ff)`, fontFamily: "'Noto Sans KR', sans-serif", position: "relative", overflow: "hidden", display: "flex", alignItems: "center", justifyContent: "center" },
  bgDecor: { position: "fixed", inset: 0, pointerEvents: "none", overflow: "hidden", zIndex: 0 },
  blob:    { position: "absolute", width: 384, height: 384, borderRadius: "50%", opacity: 0.2, filter: "blur(60px)" },
  inner:   { position: "relative", zIndex: 1, maxWidth: 360, width: "100%", padding: "0 24px", display: "flex", flexDirection: "column", alignItems: "center", gap: 32 },
  spinnerWrap:   { position: "relative", width: 140, height: 140, display: "flex", alignItems: "center", justifyContent: "center" },
  spinnerOuter:  { position: "absolute", inset: 0, borderRadius: "50%", border: `4px solid ${C.pinkLight}`, borderTopColor: C.pink, animation: "spin-cw 1.4s linear infinite" },
  spinnerInner:  { position: "absolute", inset: 16, borderRadius: "50%", border: `3px solid ${C.purpleLight}`, borderBottomColor: C.purple, animation: "spin-ccw 1.0s linear infinite" },
  spinnerCenter: { width: 72, height: 72, borderRadius: "50%", background: `linear-gradient(135deg, ${C.pinkLight}, ${C.purpleLight})`, display: "flex", alignItems: "center", justifyContent: "center", boxShadow: "0 4px 16px rgba(244,114,182,0.2)" },
  msgWrap:  { textAlign: "center", minHeight: 64, display: "flex", flexDirection: "column", alignItems: "center", gap: 8 },
  msgEmoji: { fontSize: 28, transition: "opacity 0.3s ease, transform 0.3s ease", display: "block" },
  msgText:  { fontSize: 16, fontWeight: 700, color: C.gray800, margin: 0, transition: "opacity 0.3s ease, transform 0.3s ease", lineHeight: 1.5 },
  progressWrap:  { width: "100%", display: "flex", flexDirection: "column", alignItems: "center", gap: 8 },
  progressTrack: { width: "100%", height: 6, background: C.pinkLight, borderRadius: 4, overflow: "hidden" },
  progressBar:   { height: "100%", background: `linear-gradient(90deg, ${C.pink}, ${C.purple})`, borderRadius: 4, transition: "width 0.15s linear" },
  progressLabel: { fontSize: 12, fontWeight: 700, color: C.pink, margin: 0 },
  notice:   { fontSize: 12, color: C.gray400, textAlign: "center", lineHeight: 1.7, margin: 0 },
  retryBtn: { marginTop: 20, padding: "12px 28px", borderRadius: 14, border: "none", background: `linear-gradient(135deg, ${C.pink}, ${C.purple})`, color: C.white, fontSize: 14, fontWeight: 800, cursor: "pointer", fontFamily: "'Noto Sans KR', sans-serif" },
};
