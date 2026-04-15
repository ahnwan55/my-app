import { useState, useEffect } from "react";

const personas = {
  SAFETY_GUARD: {
    icon: "🛡️",
    name: "안전 수호자",
    tagline: "~ 안정 속에서 피어나는 독서가 ~",
    description: "당신은 깊고 신중한 독서가입니다. 검증된 고전과 묵직한 인문학 서적을 즐기며, 한 권을 읽더라도 충분히 곱씹고 이해하는 스타일입니다.",
  },
  STEADY_WORKER: {
    icon: "📚",
    name: "꾸준한 탐독가",
    tagline: "~ 매일 한 페이지, 쌓이는 지혜 ~",
    description: "당신은 습관의 힘을 믿는 독서가입니다. 바쁜 일상 속에서도 꾸준히 책을 손에 잡고, 자기계발서와 실용서를 통해 성장하는 것을 즐깁니다.",
  },
  BALANCED_SPENDER: {
    icon: "⚖️",
    name: "균형 탐험가",
    tagline: "~ 장르의 경계를 자유롭게 ~",
    description: "당신은 다양한 장르를 넘나드는 독서가입니다. 소설과 비소설을 번갈아 읽으며 균형 잡힌 시각을 키웁니다.",
  },
  RATE_OPTIMIZER: {
    icon: "⚡",
    name: "속독 최적화자",
    tagline: "~ 더 많이, 더 빠르게, 더 깊게 ~",
    description: "당신은 효율을 추구하는 독서가입니다. 핵심을 빠르게 파악하고 많은 책을 읽는 것을 목표로 합니다.",
  },
  GOAL_ACHIEVER: {
    icon: "🎯",
    name: "목표 달성자",
    tagline: "~ 읽는 것이 곧 전략이다 ~",
    description: "당신은 목적 지향적인 독서가입니다. 현재 목표와 직결된 책을 선택하고, 독서를 통해 구체적인 성과를 얻고자 합니다.",
  },
  FUTURE_PLANNER: {
    icon: "🔭",
    name: "미래 설계자",
    tagline: "~ 오늘의 독서가 내일의 나를 만든다 ~",
    description: "당신은 미래를 바라보는 독서가입니다. 트렌드와 미래 기술, 사회 변화를 다루는 책에 관심이 많습니다.",
  },
};

export default function PersonaResult({ personaCode = "GOAL_ACHIEVER", onViewBooks }) {
  const persona = personas[personaCode] || personas["GOAL_ACHIEVER"];
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const t = setTimeout(() => setVisible(true), 100);
    return () => clearTimeout(t);
  }, []);

  return (
    <div style={styles.page}>
      <div style={{ ...styles.card, opacity: visible ? 1 : 0, transform: visible ? "translateY(0)" : "translateY(24px)", transition: "opacity 0.6s ease, transform 0.6s ease" }}>
        <p style={styles.subtitle}>당신의 페르소나는...</p>
        <div style={styles.iconWrapper}>
          <div style={styles.iconBox}>
            <span style={styles.icon}>{persona.icon}</span>
          </div>
          <p style={styles.personaName}>{persona.name}</p>
        </div>
        <p style={styles.tagline}>{persona.tagline}</p>
        <div style={styles.divider} />
        <div style={styles.descBox}>
          <p style={styles.descTitle}>페르소나 설명</p>
          <p style={styles.descText}>{persona.description}</p>
        </div>
        <button
          style={styles.button}
          onClick={onViewBooks}
          onMouseEnter={(e) => { e.currentTarget.style.background = "#000"; e.currentTarget.style.color = "#fff"; }}
          onMouseLeave={(e) => { e.currentTarget.style.background = "#fff"; e.currentTarget.style.color = "#000"; }}
        >
          추천 도서 보기
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
    padding: "clamp(28px, 7vw, 40px) clamp(18px, 5vw, 32px)",
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    background: "#fff",
    boxSizing: "border-box",
  },
  subtitle: {
    fontSize: "clamp(13px, 3.5vw, 15px)",
    fontWeight: "400",
    color: "#000",
    letterSpacing: "0.05em",
    marginBottom: "clamp(18px, 5vw, 24px)",
    textAlign: "center",
  },
  iconWrapper: {
    border: "2px solid #000",
    borderRadius: "4px",
    width: "100%",
    padding: "clamp(18px, 5vw, 24px) 16px",
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    gap: "10px",
    marginBottom: "14px",
    boxSizing: "border-box",
  },
  iconBox: {
    width: "clamp(64px, 16vw, 80px)",
    height: "clamp(64px, 16vw, 80px)",
    border: "2px solid #000",
    borderRadius: "4px",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
  },
  icon: {
    fontSize: "clamp(28px, 8vw, 36px)",
    lineHeight: 1,
  },
  personaName: {
    fontSize: "clamp(18px, 5vw, 22px)",
    fontWeight: "700",
    color: "#000",
    letterSpacing: "0.02em",
    margin: "0",
  },
  tagline: {
    fontSize: "clamp(11px, 3vw, 13px)",
    color: "#555",
    letterSpacing: "0.08em",
    marginBottom: "clamp(14px, 4vw, 20px)",
    textAlign: "center",
    fontStyle: "italic",
  },
  divider: {
    width: "100%",
    height: "1px",
    background: "#000",
    marginBottom: "clamp(14px, 4vw, 20px)",
  },
  descBox: {
    border: "2px solid #000",
    borderRadius: "4px",
    width: "100%",
    padding: "clamp(14px, 4vw, 20px)",
    marginBottom: "clamp(18px, 5vw, 24px)",
    boxSizing: "border-box",
  },
  descTitle: {
    fontSize: "clamp(10px, 2.8vw, 12px)",
    fontWeight: "700",
    letterSpacing: "0.12em",
    textTransform: "uppercase",
    color: "#000",
    marginBottom: "8px",
  },
  descText: {
    fontSize: "clamp(12px, 3.3vw, 14px)",
    lineHeight: "1.8",
    color: "#222",
    margin: "0",
    wordBreak: "keep-all",
  },
  button: {
    width: "100%",
    padding: "clamp(12px, 3.5vw, 14px)",
    border: "2px solid #000",
    borderRadius: "4px",
    background: "#fff",
    color: "#000",
    fontSize: "clamp(13px, 3.8vw, 15px)",
    fontWeight: "700",
    letterSpacing: "0.08em",
    cursor: "pointer",
    transition: "background 0.2s ease, color 0.2s ease",
    fontFamily: "inherit",
    touchAction: "manipulation",
  },
};
