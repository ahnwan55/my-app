import { useState, useEffect } from "react";

const sampleBooks = [
  { id: 1, title: "어린 왕자", author: "앙투안 드 생텍쥐페리", cover: null, summary: "사막에 불시착한 조종사가 만난 어린 왕자와의 이야기. 어른들이 잃어버린 순수함과 진정한 관계의 의미를 섬세하게 담아낸 시대를 초월한 고전 소설입니다.", genre: "고전문학" },
  { id: 2, title: "데미안", author: "헤르만 헤세", cover: null, summary: "소년 싱클레어가 데미안을 만나며 자아를 찾아가는 성장 이야기. 내면의 목소리에 귀 기울이며 진정한 자신을 발견하는 여정을 그린 헤세의 대표작입니다.", genre: "성장소설" },
  { id: 3, title: "채식주의자", author: "한강", cover: null, summary: "어느 날 고기를 끊기로 결심한 한 여성의 이야기. 평범한 일상 속에 감추어진 폭력과 욕망, 그리고 존재에 대한 질문을 강렬하게 파고드는 한강의 부커상 수상작입니다.", genre: "현대소설" },
];

export default function BookResultPage({ books = sampleBooks, personaName = "목표 달성자", onBack }) {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const t = setTimeout(() => setVisible(true), 100);
    return () => clearTimeout(t);
  }, []);

  return (
    <div style={styles.page}>
      <div style={styles.header}>
        <button style={styles.backBtn} onClick={onBack}>← 돌아가기</button>
        <div style={styles.headerCenter}>
          <span style={styles.headerSub}>{personaName}을 위한</span>
          <span style={styles.headerTitle}>추천 도서</span>
        </div>
        <div style={{ width: "64px" }} />
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
    justifyContent: "space-between",
    zIndex: 10,
  },
  backBtn: {
    background: "none",
    border: "none",
    fontSize: "clamp(11px, 3vw, 13px)",
    cursor: "pointer",
    color: "#000",
    fontFamily: "inherit",
    letterSpacing: "0.04em",
    padding: "0",
    width: "64px",
    textAlign: "left",
    touchAction: "manipulation",
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
    minWidth: 0, // 텍스트 오버플로우 방지
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
