import { useState, useEffect } from "react";

const LOADING_STEPS = [
  "페르소나 유형을 확인하고 있습니다...",
  "도서 데이터베이스를 탐색하고 있습니다...",
  "AI가 최적의 도서를 선별하고 있습니다...",
  "추천 목록을 완성하고 있습니다...",
];

export default function BookLoadingPage({ personaName = "목표 달성자", onComplete }) {
  const [stepIndex, setStepIndex] = useState(0);
  const [dotCount, setDotCount] = useState(0);
  const [barWidth, setBarWidth] = useState(0);
  const [done, setDone] = useState(false);

  useEffect(() => {
    const t = setInterval(() => setDotCount((p) => (p + 1) % 4), 400);
    return () => clearInterval(t);
  }, []);

  useEffect(() => {
    const t = setInterval(() => {
      setStepIndex((p) => { if (p < LOADING_STEPS.length - 1) return p + 1; clearInterval(t); return p; });
    }, 1200);
    return () => clearInterval(t);
  }, []);

  useEffect(() => {
    const target = ((stepIndex + 1) / LOADING_STEPS.length) * 100;
    const t = setTimeout(() => setBarWidth(target), 100);
    return () => clearTimeout(t);
  }, [stepIndex]);

  useEffect(() => {
    if (stepIndex === LOADING_STEPS.length - 1) {
      const t = setTimeout(() => { setDone(true); setTimeout(() => onComplete && onComplete(), 600); }, 1400);
      return () => clearTimeout(t);
    }
  }, [stepIndex, onComplete]);

  return (
    <div style={styles.page}>
      <div style={{ ...styles.card, opacity: done ? 0 : 1, transition: "opacity 0.6s ease" }}>
        <p style={styles.subtitle}>도서 추천 중</p>
        <div style={styles.personaBadge}>
          <span style={styles.personaLabel}>페르소나</span>
          <span style={styles.personaName}>{personaName}</span>
        </div>
        <div style={styles.iconArea}>
          <BookStackSpinner step={stepIndex} />
        </div>
        <p style={styles.stepText}>
          {LOADING_STEPS[stepIndex]}
          <span style={styles.dots}>{".".repeat(dotCount)}</span>
        </p>
        <div style={styles.divider} />
        <div style={styles.progressWrapper}>
          <div style={styles.progressTrack}>
            <div style={{ ...styles.progressBar, width: `${barWidth}%`, transition: "width 0.8s ease" }} />
          </div>
          <div style={styles.progressFooter}>
            <span style={styles.progressLabel}>추천 진행률</span>
            <span style={styles.progressPct}>{Math.round(barWidth)}%</span>
          </div>
        </div>
        <div style={styles.stepIndicator}>
          {LOADING_STEPS.map((_, i) => (
            <div key={i} style={{ ...styles.stepDot, background: i <= stepIndex ? "#000" : "#ddd", transition: "background 0.4s ease" }} />
          ))}
        </div>
      </div>
    </div>
  );
}

function BookStackSpinner({ step }) {
  const books = [{ y: 68 }, { y: 50 }, { y: 32 }];
  return (
    <div style={styles.svgWrapper}>
      <svg width="90" height="90" viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
        <style>{`@keyframes float { 0%, 100% { transform: translateY(0px); } 50% { transform: translateY(-4px); } }`}</style>
        {books.map((book, i) => (
          <g key={i} opacity={i <= step - 1 ? 1 : 0.15} style={{ transition: "opacity 0.6s ease" }}>
            <rect x="22" y={book.y} width="76" height="16" rx="2" stroke="#000" strokeWidth="1.8" fill="#fff" />
            <line x1="34" y1={book.y} x2="34" y2={book.y + 16} stroke="#000" strokeWidth="1.8" />
            <line x1="42" y1={book.y + 6} x2="80" y2={book.y + 6} stroke="#000" strokeWidth="1" />
            <line x1="42" y1={book.y + 10} x2="68" y2={book.y + 10} stroke="#000" strokeWidth="1" />
          </g>
        ))}
        {step >= 3 && (
          <g style={{ animation: "float 1.2s ease-in-out infinite" }}>
            <circle cx="96" cy="28" r="10" stroke="#000" strokeWidth="1.8" fill="#fff" />
            <text x="96" y="33" textAnchor="middle" fontSize="12" fill="#000">✓</text>
          </g>
        )}
      </svg>
    </div>
  );
}

const styles = {
  page: {
    minHeight: "100dvh",
    background: "#fff",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontFamily: "'Noto Serif KR', 'Georgia', serif",
    padding: "24px 16px",
    boxSizing: "border-box",
  },
  card: {
    width: "100%",
    maxWidth: "420px",
    border: "2px solid #000",
    borderRadius: "4px",
    padding: "clamp(28px, 7vw, 40px) clamp(18px, 5vw, 32px)",
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    background: "#fff",
    boxSizing: "border-box",
  },
  subtitle: {
    fontSize: "clamp(11px, 3vw, 13px)",
    letterSpacing: "0.12em",
    color: "#555",
    marginBottom: "clamp(14px, 4vw, 20px)",
    textTransform: "uppercase",
    fontWeight: "700",
  },
  personaBadge: {
    border: "1.5px solid #000",
    borderRadius: "4px",
    padding: "clamp(6px, 2vw, 8px) clamp(14px, 4vw, 20px)",
    display: "flex",
    alignItems: "center",
    gap: "8px",
    marginBottom: "clamp(20px, 5vw, 28px)",
  },
  personaLabel: {
    fontSize: "clamp(9px, 2.3vw, 10px)",
    letterSpacing: "0.12em",
    color: "#555",
    fontWeight: "700",
    textTransform: "uppercase",
  },
  personaName: {
    fontSize: "clamp(12px, 3.3vw, 14px)",
    fontWeight: "700",
    color: "#000",
    letterSpacing: "0.04em",
  },
  iconArea: { marginBottom: "clamp(20px, 5vw, 28px)" },
  svgWrapper: {
    border: "2px solid #000",
    borderRadius: "4px",
    padding: "clamp(12px, 3.5vw, 16px) clamp(16px, 4.5vw, 20px)",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
  },
  stepText: {
    fontSize: "clamp(12px, 3.3vw, 14px)",
    color: "#000",
    letterSpacing: "0.04em",
    marginBottom: "clamp(18px, 5vw, 24px)",
    textAlign: "center",
    lineHeight: 1.6,
    minHeight: "22px",
  },
  dots: { display: "inline-block", width: "20px", textAlign: "left" },
  divider: { width: "100%", height: "1px", background: "#000", marginBottom: "clamp(18px, 5vw, 24px)" },
  progressWrapper: { width: "100%", marginBottom: "clamp(18px, 5vw, 24px)" },
  progressTrack: { width: "100%", height: "3px", background: "#e0e0e0", borderRadius: "2px", overflow: "hidden", marginBottom: "8px" },
  progressBar: { height: "100%", background: "#000", borderRadius: "2px" },
  progressFooter: { display: "flex", justifyContent: "space-between" },
  progressLabel: { fontSize: "clamp(10px, 2.5vw, 11px)", color: "#555", letterSpacing: "0.08em" },
  progressPct: { fontSize: "clamp(10px, 2.5vw, 11px)", color: "#000", fontWeight: "700", letterSpacing: "0.04em" },
  stepIndicator: { display: "flex", gap: "8px" },
  stepDot: { width: "clamp(6px, 2vw, 8px)", height: "clamp(6px, 2vw, 8px)", borderRadius: "50%" },
};
