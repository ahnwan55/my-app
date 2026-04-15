import { useState, useEffect } from "react";

export default function MainPage({ onStart }) {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const t = setTimeout(() => setVisible(true), 100);
    return () => clearTimeout(t);
  }, []);

  return (
    <div style={styles.page}>
      <div style={{ ...styles.card, opacity: visible ? 1 : 0, transform: visible ? "translateY(0)" : "translateY(20px)", transition: "opacity 0.7s ease, transform 0.7s ease" }}>
        <div style={styles.titleBlock}>
          <h1 style={styles.titleLine1}>도서 페르소나</h1>
          <h1 style={styles.titleLine2}>테스트</h1>
        </div>
        <div style={styles.logoWrapper}>
          <svg width="90" height="90" viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="20" y="20" width="80" height="80" rx="4" stroke="#000" strokeWidth="2.5" fill="#fff" />
            <line x1="44" y1="20" x2="44" y2="100" stroke="#000" strokeWidth="2.5" />
            <line x1="56" y1="34" x2="88" y2="34" stroke="#000" strokeWidth="1.5" />
            <line x1="56" y1="44" x2="88" y2="44" stroke="#000" strokeWidth="1.5" />
            <line x1="56" y1="54" x2="88" y2="54" stroke="#000" strokeWidth="1.5" />
            <line x1="56" y1="64" x2="78" y2="64" stroke="#000" strokeWidth="1.5" />
            <circle cx="32" cy="60" r="8" stroke="#000" strokeWidth="2" fill="#fff" />
            <line x1="32" y1="52" x2="32" y2="68" stroke="#000" strokeWidth="1.5" />
            <line x1="24" y1="60" x2="40" y2="60" stroke="#000" strokeWidth="1.5" />
          </svg>
          <div style={styles.logoLabel}>BOOK PERSONA</div>
        </div>
        <button
          style={styles.startBtn}
          onClick={onStart}
          onMouseEnter={(e) => { e.currentTarget.style.background = "#000"; e.currentTarget.style.color = "#fff"; }}
          onMouseLeave={(e) => { e.currentTarget.style.background = "#fff"; e.currentTarget.style.color = "#000"; }}
        >
          시작하기
        </button>
      </div>
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
    padding: "clamp(28px, 7vw, 48px) clamp(18px, 5vw, 32px)",
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    background: "#fff",
    boxSizing: "border-box",
  },
  titleBlock: {
    textAlign: "center",
    marginBottom: "clamp(28px, 7vw, 44px)",
  },
  titleLine1: {
    fontSize: "clamp(22px, 6.5vw, 32px)",
    fontWeight: "700",
    color: "#000",
    letterSpacing: "0.04em",
    margin: "0 0 4px 0",
    lineHeight: 1.2,
  },
  titleLine2: {
    fontSize: "clamp(22px, 6.5vw, 32px)",
    fontWeight: "400",
    color: "#000",
    letterSpacing: "0.1em",
    margin: "0",
    lineHeight: 1.2,
  },
  logoWrapper: {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    gap: "10px",
    border: "2px solid #000",
    borderRadius: "4px",
    padding: "clamp(18px, 5vw, 28px) clamp(24px, 7vw, 36px)",
    marginBottom: "clamp(36px, 9vw, 52px)",
    width: "100%",
    boxSizing: "border-box",
  },
  logoLabel: {
    fontSize: "clamp(9px, 2.5vw, 11px)",
    letterSpacing: "0.2em",
    color: "#000",
    fontWeight: "700",
  },
  startBtn: {
    width: "100%",
    padding: "clamp(13px, 3.5vw, 18px)",
    border: "2px solid #000",
    borderRadius: "4px",
    background: "#fff",
    color: "#000",
    fontSize: "clamp(14px, 4vw, 18px)",
    fontWeight: "700",
    letterSpacing: "0.12em",
    cursor: "pointer",
    transition: "background 0.2s ease, color 0.2s ease",
    fontFamily: "inherit",
    touchAction: "manipulation",
  },
};
