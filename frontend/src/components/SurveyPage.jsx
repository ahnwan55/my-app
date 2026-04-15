import { useState } from "react";

const questions = [
  { id: 1, text: "나는 책을 읽을 때 한 권을 끝까지 읽고 다음 책으로 넘어가는 편이다." },
  { id: 2, text: "나는 소설보다 실용서나 자기계발서를 더 자주 읽는다." },
  { id: 3, text: "나는 책을 고를 때 베스트셀러 목록을 참고하는 편이다." },
  { id: 4, text: "나는 한 달에 2권 이상의 책을 읽는다." },
  { id: 5, text: "나는 책을 읽은 후 내용을 메모하거나 정리하는 편이다." },
];

const SCALE_COUNT = 6;

export default function SurveyPage({ onSubmit }) {
  const [answers, setAnswers] = useState({});

  const handleSelect = (questionId, value) => {
    setAnswers((prev) => ({ ...prev, [questionId]: value }));
  };

  const allAnswered = questions.every((q) => answers[q.id] !== undefined);

  return (
    <div style={styles.page}>
      <div style={styles.header}>
        <span style={styles.headerTitle}>페르소나 타입</span>
        <span style={styles.headerSub}>설문 문항 {questions.length}개</span>
      </div>
      <div style={styles.scrollArea}>
        {questions.map((q, idx) => (
          <QuestionItem
            key={q.id}
            index={idx + 1}
            question={q}
            selected={answers[q.id]}
            onSelect={(val) => handleSelect(q.id, val)}
            isLast={idx === questions.length - 1}
          />
        ))}
        <div style={styles.buttonWrapper}>
          <button
            style={{ ...styles.button, opacity: allAnswered ? 1 : 0.35, cursor: allAnswered ? "pointer" : "not-allowed" }}
            onClick={() => allAnswered && onSubmit && onSubmit(answers)}
            disabled={!allAnswered}
            onMouseEnter={(e) => { if (allAnswered) { e.currentTarget.style.background = "#000"; e.currentTarget.style.color = "#fff"; }}}
            onMouseLeave={(e) => { e.currentTarget.style.background = "#fff"; e.currentTarget.style.color = "#000"; }}
          >
            결과 보기
          </button>
        </div>
      </div>
    </div>
  );
}

function QuestionItem({ index, question, selected, onSelect, isLast }) {
  return (
    <div style={{ ...styles.questionBlock, borderBottom: isLast ? "none" : "1px solid #000" }}>
      <p style={styles.questionText}>
        <span style={styles.questionNum}>{index}.</span> {question.text}
      </p>
      <div style={styles.scaleLabels}>
        <span style={styles.scaleLabel}>그렇다</span>
        <span style={styles.scaleLabel}>아니다</span>
      </div>
      <div style={styles.arrowRow}>
        <div style={styles.arrowLine} />
        <span style={styles.arrowRight}>▶</span>
      </div>
      <div style={styles.optionRow}>
        {Array.from({ length: SCALE_COUNT }).map((_, i) => (
          <button
            key={i}
            onClick={() => onSelect(i)}
            style={{ ...styles.optionBtn, background: selected === i ? "#000" : "#fff" }}
            onMouseEnter={(e) => { if (selected !== i) e.currentTarget.style.background = "#ddd"; }}
            onMouseLeave={(e) => { if (selected !== i) e.currentTarget.style.background = "#fff"; }}
          />
        ))}
      </div>
    </div>
  );
}

const styles = {
  page: {
    minHeight: "100dvh",
    background: "#fff",
    fontFamily: "'Noto Serif KR', 'Georgia', serif",
    display: "flex",
    flexDirection: "column",
  },
  header: {
    position: "sticky",
    top: 0,
    background: "#fff",
    borderBottom: "2px solid #000",
    padding: "clamp(14px, 3.5vw, 18px) clamp(16px, 4vw, 24px)",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    zIndex: 10,
  },
  headerTitle: {
    fontSize: "clamp(14px, 3.8vw, 16px)",
    fontWeight: "700",
    letterSpacing: "0.05em",
    color: "#000",
  },
  headerSub: {
    fontSize: "clamp(11px, 2.8vw, 12px)",
    color: "#555",
    letterSpacing: "0.08em",
  },
  scrollArea: {
    flex: 1,
    overflowY: "auto",
    padding: "0 clamp(16px, 4vw, 24px)",
    maxWidth: "420px",
    width: "100%",
    margin: "0 auto",
    boxSizing: "border-box",
  },
  questionBlock: {
    padding: "clamp(24px, 6vw, 32px) 0",
  },
  questionText: {
    fontSize: "clamp(13px, 3.5vw, 15px)",
    lineHeight: "1.7",
    color: "#000",
    marginBottom: "clamp(16px, 4vw, 20px)",
    wordBreak: "keep-all",
  },
  questionNum: {
    fontWeight: "700",
    marginRight: "6px",
  },
  scaleLabels: {
    display: "flex",
    justifyContent: "space-between",
    marginBottom: "4px",
  },
  scaleLabel: {
    fontSize: "clamp(10px, 2.5vw, 11px)",
    color: "#555",
    letterSpacing: "0.06em",
  },
  arrowRow: {
    display: "flex",
    alignItems: "center",
    marginBottom: "10px",
    gap: "4px",
  },
  arrowLine: {
    flex: 1,
    height: "1px",
    background: "#000",
  },
  arrowRight: {
    fontSize: "10px",
    color: "#000",
    lineHeight: 1,
  },
  optionRow: {
    display: "flex",
    justifyContent: "space-between",
    gap: "clamp(4px, 1.5vw, 8px)",
  },
  optionBtn: {
    // clamp로 360px~430px 범위에서 자동 조절
    width: "clamp(30px, 8vw, 38px)",
    height: "clamp(30px, 8vw, 38px)",
    borderRadius: "50%",
    border: "2px solid #000",
    cursor: "pointer",
    transition: "background 0.15s ease",
    flexShrink: 0,
    touchAction: "manipulation",
  },
  buttonWrapper: {
    padding: "clamp(24px, 6vw, 32px) 0 clamp(36px, 9vw, 48px)",
  },
  button: {
    width: "100%",
    padding: "clamp(12px, 3.5vw, 14px)",
    border: "2px solid #000",
    borderRadius: "4px",
    background: "#fff",
    color: "#000",
    fontSize: "clamp(14px, 3.8vw, 15px)",
    fontWeight: "700",
    letterSpacing: "0.08em",
    cursor: "pointer",
    transition: "background 0.2s ease, color 0.2s ease",
    fontFamily: "inherit",
    touchAction: "manipulation",
  },
};
