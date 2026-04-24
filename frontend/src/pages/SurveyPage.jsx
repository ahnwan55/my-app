import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

/**
 * SurveyPage.jsx — 독서 페르소나 설문 페이지
 *
 * Props:
 *  onSubmit {Function} — 제출 완료 시 answers 객체를 인자로 호출
 *                        App.jsx에서 setSurveyAnswers 후 navigate("/loading") 실행
 *
 * API 연동:
 *  GET  /api/surveys/questions
 *    응답: [{ questionNo: 1, content: "..." }, ...] (10개)
 *
 *  POST /api/surveys/submit  ← SurveyPage에서 직접 호출하지 않음
 *    LoadingPage에서 surveyAnswers를 받아 호출
 *    요청: { answers: { "Q1": "답변", ..., "Q10": "답변" } }
 *    응답: { personaCode, personaName, personaReason, scores }
 *          scores 키: "지적_확장성", "분석적_깊이", "실용_지향성",
 *                     "감성_몰입도", "정보_체계화", "사회적_영향도"
 *
 * 흐름:
 *  1. 마운트 시 GET /api/surveys/questions 호출 → 질문 목록 수신
 *  2. 질문 1개씩 단계별 표시 (1/10 → 2/10 ...)
 *  3. 각 질문에 서술형 텍스트 입력
 *  4. 마지막 질문 완료 시 onSubmit(answers) 호출
 *     → App.jsx에서 setSurveyAnswers → navigate("/loading")
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

// 최소 답변 글자 수
const MIN_LENGTH = 10;

export default function SurveyPage({ onSubmit }) {
  const navigate = useNavigate();

  // 질문 목록 상태
  const [questions, setQuestions] = useState([]);   // [{ questionNo, content }]
  const [loading,   setLoading]   = useState(true);
  const [error,     setError]     = useState("");

  // 진행 상태
  const [step,    setStep]    = useState(0);         // 현재 질문 인덱스 (0-based)
  const [answers, setAnswers] = useState({});        // { Q1: "...", Q2: "..." }
  const [current, setCurrent] = useState("");        // 현재 입력 중인 답변
  const [fadeIn,  setFadeIn]  = useState(true);      // 질문 전환 애니메이션

  /* ── 질문 목록 API 호출 ──
   * GET /api/surveys/questions
   * 고정 문항이므로 한 번만 호출
   */
  useEffect(() => {
    const fetchQuestions = async () => {
      try {
        const res = await fetch("/api/surveys/questions", {
          credentials: "include",  // httpOnly 쿠키(JWT) 자동 포함
        });
        if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
        const data = await res.json();
        setQuestions(data);
      } catch (e) {
        setError("질문을 불러오는 데 실패했어요. 다시 시도해주세요.");
      } finally {
        setLoading(false);
      }
    };
    fetchQuestions();
  }, []);

  /* ── 현재 질문 변경 시 입력창 초기화 ── */
  useEffect(() => {
    const key = `Q${step + 1}`;
    setCurrent(answers[key] || "");
  }, [step]);

  /* ── 다음 질문으로 이동 ── */
  const handleNext = () => {
    const key = `Q${step + 1}`;
    const updated = { ...answers, [key]: current.trim() };
    setAnswers(updated);

    if (step < questions.length - 1) {
      // 페이드 아웃 → 다음 질문 → 페이드 인
      setFadeIn(false);
      setTimeout(() => {
        setStep((prev) => prev + 1);
        setFadeIn(true);
      }, 250);
    } else {
      // 마지막 질문 완료 → onSubmit 호출
      onSubmit(updated);
    }
  };

  /* ── 이전 질문으로 이동 ── */
  const handlePrev = () => {
    if (step === 0) { navigate("/"); return; }
    setFadeIn(false);
    setTimeout(() => {
      setStep((prev) => prev - 1);
      setFadeIn(true);
    }, 250);
  };

  const isLast      = questions.length > 0 && step === questions.length - 1;
  const canProceed  = current.trim().length >= MIN_LENGTH;
  const progress    = questions.length > 0 ? ((step + 1) / questions.length) * 100 : 0;

  /* ────────────────────────────────────────
     로딩 상태
     ──────────────────────────────────────── */
  if (loading) {
    return (
      <div style={{ ...S.wrap, display: "flex", alignItems: "center", justifyContent: "center" }}>
        <div style={S.bgDecor} aria-hidden="true">
          <div style={{ ...S.blob, top: -96, right: -96, background: "#fbcfe8" }} />
          <div style={{ ...S.blob, bottom: -96, left: -96, background: "#e9d5ff" }} />
        </div>
        <div style={{ position: "relative", zIndex: 1, textAlign: "center" }}>
          <div style={S.spinnerSmall} />
          <p style={{ fontSize: 13, color: C.gray400, marginTop: 16 }}>질문을 불러오고 있어요...</p>
        </div>
        <style>{`@keyframes spin-cw { from{transform:rotate(0deg)} to{transform:rotate(360deg)} }`}</style>
      </div>
    );
  }

  /* ────────────────────────────────────────
     에러 상태
     ──────────────────────────────────────── */
  if (error) {
    return (
      <div style={{ ...S.wrap, display: "flex", alignItems: "center", justifyContent: "center" }}>
        <div style={S.bgDecor} aria-hidden="true">
          <div style={{ ...S.blob, top: -96, right: -96, background: "#fbcfe8" }} />
          <div style={{ ...S.blob, bottom: -96, left: -96, background: "#e9d5ff" }} />
        </div>
        <div style={{ position: "relative", zIndex: 1, textAlign: "center", padding: "0 24px" }}>
          <span style={{ fontSize: 48 }}>😢</span>
          <p style={{ fontSize: 15, fontWeight: 700, color: C.gray800, margin: "16px 0 8px" }}>{error}</p>
          <button onClick={() => window.location.reload()} style={S.retryBtn}>다시 시도</button>
        </div>
      </div>
    );
  }

  const q = questions[step];

  /* ────────────────────────────────────────
     메인 렌더링
     ──────────────────────────────────────── */
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
          <button onClick={handlePrev} style={S.backBtn}>
            {step === 0 ? "← 홈으로" : "← 이전"}
          </button>
          <span style={S.stepLabel}>{step + 1} / {questions.length}</span>
        </div>

        {/* ── 진행 바 ── */}
        <div style={S.progressTrack}>
          <div style={{ ...S.progressBar, width: `${progress}%` }} />
        </div>

        {/* ── 질문 카드 ── */}
        <div style={{
          ...S.questionCard,
          opacity:   fadeIn ? 1 : 0,
          transform: fadeIn ? "translateY(0)" : "translateY(12px)",
          transition: "opacity 0.25s ease, transform 0.25s ease",
        }}>

          {/* 질문 번호 뱃지 */}
          <div style={S.qBadge}>
            <span style={S.qBadgeText}>Q{q.questionNo}</span>
          </div>

          {/* 질문 내용 */}
          <p style={S.qContent}>{q.content}</p>

          {/* 서술형 텍스트 입력 */}
          <textarea
            value={current}
            onChange={(e) => setCurrent(e.target.value)}
            placeholder="자유롭게 작성해주세요. (최소 10자)"
            rows={5}
            style={{
              ...S.textarea,
              borderColor: current.trim().length > 0 && !canProceed
                ? C.red
                : current.trim().length >= MIN_LENGTH
                ? C.pink
                : C.gray200,
            }}
          />

          {/* 글자 수 / 최소 글자 안내 */}
          <div style={S.charRow}>
            {current.trim().length > 0 && !canProceed && (
              <p style={S.charHint}>최소 {MIN_LENGTH}자 이상 입력해주세요.</p>
            )}
            <span style={{ ...S.charCount, color: canProceed ? C.pink : C.gray400, marginLeft: "auto" }}>
              {current.trim().length}자
            </span>
          </div>
        </div>

        {/* ── 다음/완료 버튼 ── */}
        <button
          onClick={handleNext}
          disabled={!canProceed}
          style={{
            ...S.nextBtn,
            background:  canProceed ? `linear-gradient(135deg, ${C.pink}, ${C.purple})` : C.gray100,
            color:       canProceed ? C.white : C.gray400,
            boxShadow:   canProceed ? "0 8px 24px rgba(244,114,182,0.4)" : "none",
            cursor:      canProceed ? "pointer" : "not-allowed",
          }}
        >
          {isLast ? "✨ 분석 시작하기" : "다음 질문 →"}
        </button>

        {/* ── 진행 인디케이터 (점) ── */}
        <div style={S.dotRow}>
          {questions.map((_, i) => (
            <div key={i} style={{
              ...S.dot,
              background: i < step
                ? `linear-gradient(135deg, ${C.pink}, ${C.purple})`
                : i === step
                ? C.pink
                : C.gray200,
              width:  i === step ? 20 : 8,
              transition: "all 0.3s ease",
            }} />
          ))}
        </div>

      </div>

      <style>{`@keyframes spin-cw { from{transform:rotate(0deg)} to{transform:rotate(360deg)} }`}</style>
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
  headerRow: { display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 16 },
  backBtn:   { fontSize: 13, color: C.gray400, background: "none", border: "none", cursor: "pointer", padding: 0, fontFamily: "'Noto Sans KR', sans-serif" },
  stepLabel: { fontSize: 12, fontWeight: 800, color: C.pink },

  /* 진행 바 */
  progressTrack: { height: 4, background: C.pinkLight, borderRadius: 4, overflow: "hidden", marginBottom: 32 },
  progressBar:   { height: "100%", background: `linear-gradient(90deg, ${C.pink}, ${C.purple})`, borderRadius: 4, transition: "width 0.4s ease" },

  /* 질문 카드 */
  questionCard: {
    background: "rgba(255,255,255,0.8)", backdropFilter: "blur(12px)",
    border: `1px solid ${C.pinkLight}`, borderRadius: 24,
    padding: "24px 20px", marginBottom: 20,
  },
  qBadge:     { display: "inline-flex", alignItems: "center", background: `linear-gradient(135deg, ${C.pink}, ${C.purple})`, borderRadius: 20, padding: "4px 14px", marginBottom: 14 },
  qBadgeText: { fontSize: 12, fontWeight: 800, color: C.white },
  qContent:   { fontSize: 16, fontWeight: 700, color: C.gray800, lineHeight: 1.6, margin: "0 0 20px" },

  /* 텍스트 에어리어 */
  textarea: {
    width: "100%", padding: "14px 16px",
    border: "1.5px solid", borderRadius: 16,
    fontSize: 14, color: C.gray800,
    fontFamily: "'Noto Sans KR', sans-serif",
    lineHeight: 1.7, resize: "none", outline: "none",
    background: C.gray50, transition: "border-color 0.2s",
    boxSizing: "border-box",
  },
  charRow:  { display: "flex", alignItems: "center", marginTop: 8 },
  charHint: { fontSize: 11, color: C.red, margin: 0, fontWeight: 600 },
  charCount:{ fontSize: 11, fontWeight: 700 },

  /* 다음 버튼 */
  nextBtn: {
    width: "100%", padding: "16px 0", borderRadius: 18, border: "none",
    fontSize: 15, fontWeight: 800,
    fontFamily: "'Noto Sans KR', sans-serif",
    transition: "all 0.2s", marginBottom: 24,
  },

  /* 진행 점 */
  dotRow: { display: "flex", alignItems: "center", justifyContent: "center", gap: 6 },
  dot:    { height: 8, borderRadius: 4 },

  /* 로딩 스피너 (소형) */
  spinnerSmall: {
    width: 40, height: 40, borderRadius: "50%", margin: "0 auto",
    border: `3px solid ${C.pinkLight}`,
    borderTopColor: C.pink,
    animation: "spin-cw 1s linear infinite",
  },

  /* 에러 재시도 버튼 */
  retryBtn: {
    padding: "12px 28px", borderRadius: 18, border: "none", cursor: "pointer",
    background: `linear-gradient(135deg, ${C.pink}, ${C.purple})`,
    color: C.white, fontSize: 14, fontWeight: 800,
    fontFamily: "'Noto Sans KR', sans-serif",
    marginTop: 16,
  },
};
