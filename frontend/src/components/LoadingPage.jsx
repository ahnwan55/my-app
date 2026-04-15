import { useState, useEffect } from "react";

const LOADING_STEPS = [
  "설문 응답을 분석하고 있습니다...",
  "독서 성향을 파악하고 있습니다...",
  "페르소나 유형을 분류하고 있습니다...",
  "맞춤 도서를 선별하고 있습니다...",
];

export default function LoadingPage({ onComplete }) {
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
        <p style={styles.subtitle}>페르소나 분석 중</p>
        <div style={styles.iconArea}>
          <BookSpinner step={stepIndex} />
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
            <span style={styles.progressLabel}>분석 진행률</span>
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

function BookSpinner({ step }) {
  const lines = [[56, 34, 88, 34], [56, 44, 88, 44], [56, 54, 88, 54], [56, 64, 78, 64]];
  return (
    <div style={styles.svgWrapper}>
      <svg width="90" height="90" viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg" style={{ animation: "pulse 1.6s ease-in-out infinite" }}>
        <style>{`@keyframes pulse { 0%, 100% { opacity: 1; transform: scale(1); } 50% { opacity: 0.6; transform: scale(0.96); } }`}</style>
        <rect x="20" y="20" width="80" height="80" rx="4" stroke="#000" strokeWidth="2.5" fill="#fff" />
        <line x1="44" y1="20" x2="44" y2="100" stroke="#000" strokeWidth="2.5" />
        {lines.map(([x1, y1, x2, y2], i) => (
          <line key={i} x1={x1} y1={y1} x2={x2} y2={y2} stroke="#000" strokeWidth="1.5" opacity={i <= step ? 1 : 0.2} style={{ transition: "opacity 0.5s ease" }} />
        ))}
        <circle cx="32" cy="60" r="8" stroke="#000" strokeWidth="2" fill="#fff" opacity={step >= 2 ? 1 : 0.2} style={{ transition: "opacity 0.5s ease" }} />
        <line x1="32" y1="52" x2="32" y2="68" stroke="#000" strokeWidth="1.5" opacity={step >= 2 ? 1 : 0.2} style={{ transition: "opacity 0.5s ease" }} />
        <line x1="24" y1="60" x2="40" y2="60" stroke="#000" strokeWidth="1.5" opacity={step >= 2 ? 1 : 0.2} style={{ transition: "opacity 0.5s ease" }} />
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
    marginBottom: "clamp(24px, 6vw, 32px)",
    textTransform: "uppercase",
    fontWeight: "700",
  },
  iconArea: { marginBottom: "clamp(20px, 5vw, 28px)" },
  svgWrapper: {
    border: "2px solid #000",
    borderRadius: "4px",
    padding: "clamp(14px, 4vw, 20px)",
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
