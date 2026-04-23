import { useState } from "react";

const questions = [
  { id: 1, text: "평소에 책을 읽는 가장 큰 이유를 자유롭게 이야기해주세요." },
  { id: 2, text: '어떤 책을 읽고 났을 때 "정말 잘 읽었다"는 생각이 드나요?' },
  { id: 3, text: "책을 고를 때 어떤 것에 끌려서 선택하게 되나요?" },
  { id: 4, text: "최근에 읽었거나 인상 깊었던 책을 소개해주세요." },
  { id: 5, text: "책을 읽을 때 본인만의 방식이 있다면 자유롭게 표현해주세요." },
  { id: 6, text: "책을 읽다가 가장 몰입되는 순간을 이야기해주세요." },
  { id: 7, text: "책을 다 읽고 나면 주로 어떻게 하나요?" },
  { id: 8, text: "지금까지 읽은 책 중 가장 기억에 남는 책과 그 이유를 들려주세요." },
  { id: 9, text: "주로 어떤 상황이나 기분일 때 책을 펼치게 되나요?" },
  { id: 10, text: "당신에게 책이란 어떤 존재인가요?" },
];

export default function SurveyPage({ onSubmit }) {
  const [answers, setAnswers] = useState({});

  const handleChange = (questionId, value) => {
    setAnswers((prev) => ({ ...prev, [questionId]: value }));
  };

  // 모든 문항에 공백 제외 1글자 이상 입력했을 때만 제출 가능
  const allAnswered = questions.every(
      (q) => answers[q.id] && answers[q.id].trim().length > 0
  );

  return (
      <div style={styles.page}>
        <div style={styles.header}>
          <span style={styles.headerTitle}>페르소나 설문</span>
          <span style={styles.headerSub}>총 {questions.length}문항</span>
        </div>
        <div style={styles.scrollArea}>
          {questions.map((q, idx) => (
              <QuestionItem
                  key={q.id}
                  index={idx + 1}
                  question={q}
                  value={answers[q.id] || ""}
                  onChange={(val) => handleChange(q.id, val)}
                  isLast={idx === questions.length - 1}
              />
          ))}
          <div style={styles.buttonWrapper}>
            <button
                style={{
                  ...styles.button,
                  opacity: allAnswered ? 1 : 0.35,
                  cursor: allAnswered ? "pointer" : "not-allowed",
                }}
                onClick={() => allAnswered && onSubmit && onSubmit(answers)}
                disabled={!allAnswered}
                onMouseEnter={(e) => {
                  if (allAnswered) {
                    e.currentTarget.style.background = "#000";
                    e.currentTarget.style.color = "#fff";
                  }
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.background = "#fff";
                  e.currentTarget.style.color = "#000";
                }}
            >
              결과 보러가기
            </button>
          </div>
        </div>
      </div>
  );
}

function QuestionItem({ index, question, value, onChange, isLast }) {
  return (
      <div
          style={{
            ...styles.questionBlock,
            borderBottom: isLast ? "none" : "1px solid #000",
          }}
      >
        <p style={styles.questionText}>
          <span style={styles.questionNum}>{index}.</span> {question.text}
        </p>
        <textarea
            style={styles.textarea}
            placeholder="자유롭게 작성해주세요."
            value={value}
            onChange={(e) => onChange(e.target.value)}
            rows={3}
            onFocus={(e) => {
              e.currentTarget.style.borderColor = "#000";
              e.currentTarget.style.outline = "none";
            }}
            onBlur={(e) => {
              e.currentTarget.style.borderColor = "#ccc";
            }}
        />
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
    marginBottom: "clamp(12px, 3vw, 16px)",
    wordBreak: "keep-all",
  },
  questionNum: {
    fontWeight: "700",
    marginRight: "6px",
  },
  textarea: {
    width: "100%",
    padding: "clamp(10px, 3vw, 14px)",
    border: "1.5px solid #ccc",
    borderRadius: "4px",
    fontSize: "clamp(12px, 3.3vw, 14px)",
    lineHeight: "1.7",
    fontFamily: "'Noto Serif KR', 'Georgia', serif",
    color: "#000",
    background: "#fff",
    resize: "vertical",
    boxSizing: "border-box",
    transition: "border-color 0.2s ease",
    minHeight: "80px",
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