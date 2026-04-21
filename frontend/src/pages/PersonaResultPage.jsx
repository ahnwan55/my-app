import { useState, useEffect } from "react";

const personas = {
  EXPLORER: {
    icon: "🔭",
    name: "지적 탐험가",
    tagline: "~ 새로운 지식을 파헤치는 지적 탐험가형 ~",
    description: "배경지식 확장에 관심이 많고, 인문·과학·역사 분야를 즐겨 읽습니다. '왜?'라는 질문을 멈추지 않으며, 책을 통해 세상을 더 깊이 이해하려는 탐구형 독자입니다.",
  },
  CURATOR: {
    icon: "🌸",
    name: "감성 수집가",
    tagline: "~ 문장의 온도를 모으는 감성 수집가형 ~",
    description: "분위기, 문장, 감정에 몰입하는 독자입니다. 여운 남는 글을 좋아하고 감정 이입이 깊으며, 소설·시집·에세이에서 삶의 온기를 발견합니다.",
  },
  NAVIGATOR: {
    icon: "⚓",
    name: "현실 항해사",
    tagline: "~ 책에서 해답을 찾는 현실 항해사형 ~",
    description: "실용성을 중시하고 읽은 것을 바로 행동으로 옮기는 목표 지향적 독자입니다. 자기계발서와 경제·경영서에서 삶의 나침반을 찾습니다.",
  },
  DWELLER: {
    icon: "☕",
    name: "안식처 거주자",
    tagline: "~ 책 속에서 쉬어가는 안식처 거주형 ~",
    description: "힐링과 위로 중심의 독서를 즐기는 독자입니다. 편안한 감정을 선호하며, 판타지와 힐링 소설 속에서 일상의 피로를 내려놓습니다.",
  },
  ANALYST: {
    icon: "♟️",
    name: "비판적 관찰자",
    tagline: "~ 숨겨진 의미를 파헤치는 비판적 관찰자형 ~",
    description: "논리 구조를 분석하고 반전과 추리를 즐기는 독자입니다. 사회 이슈에 관심이 많으며, 추리·스릴러·사회비평에서 날카로운 시각을 키웁니다.",
  },
  DIVER: {
    icon: "🌊",
    name: "사색 잠수형",
    tagline: "~ 깊이 사유하는 사색 잠수형 ~",
    description: "천천히 곱씹으며 읽고, 철학적 질문을 즐기는 독자입니다. 혼자 생각하는 시간을 소중히 여기며, 철학·인문 에세이에서 삶의 의미를 탐색합니다.",
  },
};

export default function PersonaResult({ personaCode = "EXPLORER", onViewBooks }) {
  const persona = personas[personaCode] || personas["EXPLORER"];
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
            추천 도서 보러가기
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