import { useState, useEffect } from "react";

const sampleBooks = [
  { id: 1, title: "채식주의자", author: "한강", cover: null, summary: "평범한 일상을 살아가던 한 여성이 어느 날 육식을 거부하면서 시작되는 이야기. 욕망과 폭력, 자유와 억압에 대한 날카로운 시선을 담은 한강의 대표작입니다.", genre: "소설" },
  { id: 2, title: "82년생 김지영", author: "조남주", cover: null, summary: "1982년에 태어난 평범한 여성의 삶을 통해 한국 사회의 구조적 문제를 담담하게 그려낸 소설입니다.", genre: "소설" },
  { id: 3, title: "불편한 편의점", author: "김호연", cover: null, summary: "서울역 노숙자 독고씨가 편의점 야간 알바를 하며 주변 사람들과 따뜻한 관계를 맺어가는 힐링 소설입니다.", genre: "소설" },
];

export default function BookResultPage({ books = sampleBooks, personaName = "지적 탐험가" }) {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const t = setTimeout(() => setVisible(true), 100);
    return () => clearTimeout(t);
  }, []);

  return (
      <div style={styles.page}>
        <div style={styles.header}>
          <div style={styles.headerCenter}>
            <span style={styles.headerSub}>{personaName}을 위한</span>
            <span style={styles.headerTitle}>추천 도서</span>
          </div>
        </div>
        <div style={styles.listArea}>
          {books.map((book, idx) => (
              <BookCard key={book.id} book={book} index={idx + 1} delay={idx * 120} visible={visible} />
          ))}
        </div>
      </div>
  );
}

function BookCard({ book, index, delay, visible }) {
  return (
      <div style={{ ...styles.card, opacity: visible ? 1 : 0, transform: visible ? "translateY(0)" : "translateY(20px)", transition: `opacity 0.6s ease ${delay}ms, transform 0.6s ease ${delay}ms` }}>
        <div style={styles.indexBadge}>{index}</div>
        <div style={styles.cardInner}>
          <div style={styles.coverWrapper}>
            {book.cover ? (
                <img src={book.cover} alt={book.title} style={styles.coverImg} />
            ) : (
                <div style={styles.coverPlaceholder}>
                  <svg width="32" height="44" viewBox="0 0 36 48" fill="none">
                    <rect x="1" y="1" width="34" height="46" rx="2" stroke="#000" strokeWidth="1.5" fill="#fff" />
                    <line x1="6" y1="1" x2="6" y2="47" stroke="#000" strokeWidth="1.5" />
                    <line x1="12" y1="12" x2="28" y2="12" stroke="#000" strokeWidth="1" />
                    <line x1="12" y1="18" x2="28" y2="18" stroke="#000" strokeWidth="1" />
                    <line x1="12" y1="24" x2="22" y2="24" stroke="#000" strokeWidth="1" />
                  </svg>
                </div>
            )}
          </div>
          <div style={styles.bookInfo}>
            <span style={styles.genreTag}>{book.genre}</span>
            <h2 style={styles.bookTitle}>{book.title}</h2>
            <p style={styles.bookAuthor}>{book.author}</p>
            <div style={styles.divider} />
            <p style={styles.bookSummary}>{book.summary}</p>
          </div>
        </div>
      </div>
  );
}

const styles = {
  page: {
    minHeight: "100dvh",
    background: "#fff",
    fontFamily: "'Noto Serif KR', 'Georgia', serif",
  },
  header: {
    position: "sticky",
    top: 0,
    background: "#fff",
    borderBottom: "2px solid #000",
    padding: "clamp(12px, 3.5vw, 16px) clamp(16px, 4vw, 24px)",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    zIndex: 10,
  },
  headerCenter: {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    gap: "2px",
  },
  headerSub: {
    fontSize: "clamp(10px, 2.5vw, 11px)",
    color: "#555",
    letterSpacing: "0.08em",
  },
  headerTitle: {
    fontSize: "clamp(14px, 3.8vw, 16px)",
    fontWeight: "700",
    color: "#000",
    letterSpacing: "0.05em",
  },
  listArea: {
    padding: "clamp(16px, 4vw, 24px) clamp(16px, 4vw, 20px) clamp(36px, 9vw, 48px)",
    maxWidth: "420px",
    margin: "0 auto",
    display: "flex",
    flexDirection: "column",
    gap: "clamp(14px, 4vw, 20px)",
    boxSizing: "border-box",
  },
  card: {
    border: "2px solid #000",
    borderRadius: "4px",
    padding: "clamp(16px, 4.5vw, 24px)",
    position: "relative",
    background: "#fff",
  },
  indexBadge: {
    position: "absolute",
    top: "-1px",
    left: "20px",
    width: "26px",
    height: "26px",
    background: "#000",
    color: "#fff",
    fontSize: "clamp(11px, 3vw, 13px)",
    fontWeight: "700",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    borderRadius: "0 0 4px 4px",
  },
  cardInner: {
    display: "flex",
    gap: "clamp(14px, 4vw, 20px)",
    marginTop: "8px",
  },
  coverWrapper: {
    flexShrink: 0,
  },
  coverPlaceholder: {
    width: "clamp(56px, 14vw, 72px)",
    height: "clamp(76px, 19vw, 96px)",
    border: "1.5px solid #000",
    borderRadius: "2px",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    background: "#fafafa",
  },
  coverImg: {
    width: "clamp(56px, 14vw, 72px)",
    height: "clamp(76px, 19vw, 96px)",
    objectFit: "cover",
    borderRadius: "2px",
    border: "1.5px solid #000",
  },
  bookInfo: {
    flex: 1,
    display: "flex",
    flexDirection: "column",
    gap: "5px",
    minWidth: 0,
  },
  genreTag: {
    fontSize: "clamp(9px, 2.3vw, 10px)",
    letterSpacing: "0.12em",
    color: "#555",
    fontWeight: "700",
    textTransform: "uppercase",
  },
  bookTitle: {
    fontSize: "clamp(15px, 4.2vw, 18px)",
    fontWeight: "700",
    color: "#000",
    margin: "0",
    lineHeight: 1.3,
    letterSpacing: "0.02em",
    wordBreak: "keep-all",
  },
  bookAuthor: {
    fontSize: "clamp(11px, 2.8vw, 12px)",
    color: "#555",
    margin: "0",
    letterSpacing: "0.04em",
  },
  divider: {
    width: "100%",
    height: "1px",
    background: "#ddd",
    margin: "2px 0",
  },
  bookSummary: {
    fontSize: "clamp(11px, 3vw, 13px)",
    lineHeight: "1.75",
    color: "#333",
    margin: "0",
    wordBreak: "keep-all",
  },
};