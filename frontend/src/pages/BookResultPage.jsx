import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

/**
 * BookResultPage.jsx — 도서 추천 결과 페이지
 *
 * Props:
 *  personaName {string} — 페르소나 이름 (App.jsx에서 전달)
 *
 * API 연동:
 *  GET /api/recommendations
 *  credentials: "include" (httpOnly 쿠키 JWT 자동 포함)
 *
 *  응답 구조:
 *  {
 *    personaCode: "CASUAL_RESTER",
 *    personaName: "가벼운 휴식자",
 *    reason: "...",
 *    aiComment: "...",
 *    books: [
 *      {
 *        rank: 1,
 *        book: {
 *          bookId, title, author, publisher,
 *          pubYear, coverUrl, description, kdc
 *        },
 *        matchReason: "..."
 *      }
 *    ]
 *  }
 *
 * 컬러: 벚꽃 핑크(#f472b6) × 퍼플(#a855f7)
 */

const C = {
  pink:        "#f472b6",
  pinkDark:    "#ec4899",
  pinkLight:   "#fce7f3",
  pinkBg:      "#fdf2f8",
  purple:      "#a855f7",
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

export default function BookResultPage({ personaName: propPersonaName = "지적 탐험가" }) {
  const navigate = useNavigate();

  const [loading,     setLoading]     = useState(true);
  const [error,       setError]       = useState("");
  const [data,        setData]        = useState(null);   // RecommendResponse 전체
  const [visible,     setVisible]     = useState(false);
  const [selected,    setSelected]    = useState(null);   // 모달용 선택 도서

  /* ── GET /api/recommendations 호출 ── */
  useEffect(() => {
    const fetchRecommendations = async () => {
      try {
        const res = await fetch("/api/recommendations", {
          credentials: "include",  // httpOnly 쿠키(JWT) 자동 포함
        });
        if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
        const json = await res.json();
        setData(json);
        setTimeout(() => setVisible(true), 100);
      } catch (e) {
        setError("추천 도서를 불러오는 데 실패했어요. 다시 시도해주세요.");
      } finally {
        setLoading(false);
      }
    };
    fetchRecommendations();
  }, []);

  const personaName = data?.personaName ?? propPersonaName;
  const books       = data?.books ?? [];

  /* ── 로딩 ── */
  if (loading) {
    return (
      <div style={{ ...S.wrap, display: "flex", alignItems: "center", justifyContent: "center" }}>
        <BgDecor />
        <div style={{ position: "relative", zIndex: 1, textAlign: "center" }}>
          <div style={S.spinner} />
          <p style={{ fontSize: 13, color: C.gray400, marginTop: 16 }}>
            맞춤 도서를 찾고 있어요...
          </p>
        </div>
        <style>{`@keyframes spin-cw { from{transform:rotate(0deg)} to{transform:rotate(360deg)} }`}</style>
      </div>
    );
  }

  /* ── 에러 ── */
  if (error) {
    return (
      <div style={{ ...S.wrap, display: "flex", alignItems: "center", justifyContent: "center" }}>
        <BgDecor />
        <div style={{ position: "relative", zIndex: 1, textAlign: "center", padding: "0 24px" }}>
          <span style={{ fontSize: 48 }}>😢</span>
          <p style={{ fontSize: 15, color: C.gray700, margin: "16px 0 20px", fontWeight: 700 }}>{error}</p>
          <button onClick={() => window.location.reload()} style={S.retryBtn}>다시 시도</button>
        </div>
      </div>
    );
  }

  return (
    <div style={S.wrap}>
      <BgDecor />

      {/* ── 헤더 ── */}
      <div style={S.header}>
        <p style={S.headerSub}>✨ {personaName}을 위한</p>
        <h1 style={S.headerTitle}>맞춤 도서 추천</h1>

        {/* AI 코멘트 */}
        {data?.aiComment && (
          <div style={S.aiComment}>
            <span style={S.aiCommentIcon}>🤖</span>
            <p style={S.aiCommentText}>{data.aiComment}</p>
          </div>
        )}

        {/* 추천 이유 */}
        {data?.reason && !data?.aiComment && (
          <p style={S.reason}>{data.reason}</p>
        )}

        <p style={S.headerDesc}>도서를 탭하면 상세 정보를 볼 수 있어요</p>
      </div>

      {/* ── 도서 목록 (가로 한 줄씩) ── */}
      <div style={S.list}>
        {books.map((item, idx) => (
          <BookCard
            key={item.book.bookId}
            book={item.book}
            rank={item.rank}
            matchReason={item.matchReason}
            delay={idx * 80}
            visible={visible}
            onClick={() => setSelected(item)}
          />
        ))}
      </div>

      {/* ── 상세 모달 (바텀시트) ── */}
      {selected && (
        <div style={S.overlay} onClick={() => setSelected(null)}>
          <div style={S.modal} onClick={e => e.stopPropagation()}>

            <div style={S.handle} />
            <button style={S.closeBtn} onClick={() => setSelected(null)}>✕</button>

            {/* 표지 + 기본 정보 */}
            <div style={S.modalTop}>
              <div style={S.modalCover}>
                {selected.book.coverUrl
                  ? <img src={selected.book.coverUrl} alt={selected.book.title} style={S.modalCoverImg} />
                  : <BookIcon size={40} />
                }
              </div>
              <div style={S.modalInfo}>
                <span style={S.modalKdc}>{selected.book.kdc ?? "도서"}</span>
                <h2 style={S.modalTitle}>{selected.book.title}</h2>
                <p style={S.modalAuthor}>{selected.book.author}</p>
                {selected.book.publisher && (
                  <p style={S.modalPublisher}>{selected.book.publisher} · {selected.book.pubYear}</p>
                )}
              </div>
            </div>

            <div style={S.divider} />

            {/* 추천 이유 */}
            {selected.matchReason && (
              <div style={S.matchReasonBox}>
                <span style={S.matchReasonLabel}>💡 추천 이유</span>
                <p style={S.matchReasonText}>{selected.matchReason}</p>
              </div>
            )}

            {/* 줄거리 */}
            {selected.book.description && (
              <p style={S.modalSummary}>{selected.book.description}</p>
            )}

            {/* 상세 보기 버튼 */}
            <button
              style={S.detailBtn}
              onClick={() => navigate(`/books/${selected.book.bookId}`)}
            >
              📖 상세 보기
            </button>

          </div>
        </div>
      )}

      <style>{`@keyframes spin-cw { from{transform:rotate(0deg)} to{transform:rotate(360deg)} }`}</style>
    </div>
  );
}

/* ── 도서 카드 (가로 한 줄) ── */
function BookCard({ book, rank, matchReason, delay, visible, onClick }) {
  const [hover, setHover] = useState(false);

  return (
    <div
      style={{
        ...S.card,
        opacity:   visible ? 1 : 0,
        transform: visible
          ? hover ? "translateX(4px)" : "translateX(0)"
          : "translateY(16px)",
        transition: `opacity 0.5s ease ${delay}ms, transform 0.25s ease`,
        boxShadow: hover
          ? "0 8px 24px rgba(244,114,182,0.2)"
          : "0 2px 10px rgba(244,114,182,0.08)",
        cursor: "pointer",
      }}
      onClick={onClick}
      onMouseEnter={() => setHover(true)}
      onMouseLeave={() => setHover(false)}
    >
      <div style={S.badge}>{rank}</div>

      <div style={S.coverBox}>
        {book.coverUrl
          ? <img src={book.coverUrl} alt={book.title} style={S.coverImg} />
          : <BookIcon size={26} />
        }
      </div>

      <div style={S.cardBody}>
        <span style={S.genreTag}>{book.kdc ?? "도서"}</span>
        <p style={S.cardTitle}>{book.title}</p>
        <p style={S.cardAuthor}>{book.author}</p>
      </div>

      <div style={S.arrow}>→</div>
    </div>
  );
}

/* ── 배경 장식 ── */
function BgDecor() {
  return (
    <div style={S.bgDecor} aria-hidden="true">
      <div style={{ ...S.blob, top: -80, right: -80, background: "#fbcfe8" }} />
      <div style={{ ...S.blob, bottom: -80, left: -80, background: "#e9d5ff" }} />
    </div>
  );
}

/* ── 책 아이콘 ── */
function BookIcon({ size = 32 }) {
  return (
    <svg width={size} height={size * 1.3} viewBox="0 0 36 48" fill="none">
      <rect x="1" y="1" width="34" height="46" rx="3" fill={C.pinkLight} stroke={C.pink} strokeWidth="1.5" />
      <line x1="7" y1="1" x2="7" y2="47" stroke={C.pink} strokeWidth="1.5" />
      <line x1="13" y1="12" x2="29" y2="12" stroke={C.pink} strokeWidth="1" opacity="0.6" />
      <line x1="13" y1="18" x2="29" y2="18" stroke={C.pink} strokeWidth="1" opacity="0.6" />
      <line x1="13" y1="24" x2="23" y2="24" stroke={C.pink} strokeWidth="1" opacity="0.6" />
    </svg>
  );
}

/* ── 스타일 ── */
const S = {
  wrap: {
    minHeight: "100vh",
    background: `linear-gradient(135deg, ${C.pinkBg}, ${C.white}, #faf5ff)`,
    fontFamily: "'Noto Sans KR', sans-serif",
    position: "relative",
    overflow: "hidden",
    paddingBottom: 60,
  },
  bgDecor: {
    position: "fixed", inset: 0,
    pointerEvents: "none", overflow: "hidden", zIndex: 0,
  },
  blob: {
    position: "absolute", width: 320, height: 320,
    borderRadius: "50%", opacity: 0.2, filter: "blur(60px)",
  },

  /* 헤더 */
  header: {
    position: "relative", zIndex: 1,
    padding: "40px 20px 20px",
    maxWidth: 480, margin: "0 auto",
    textAlign: "center",
  },
  headerSub: {
    fontSize: 13, color: C.purple, fontWeight: 700,
    margin: "0 0 6px", letterSpacing: "0.05em",
  },
  headerTitle: {
    fontSize: 24, fontWeight: 900, margin: "0 0 12px",
    background: `linear-gradient(135deg, ${C.pink}, ${C.purple})`,
    WebkitBackgroundClip: "text", WebkitTextFillColor: "transparent",
  },
  aiComment: {
    background: "rgba(255,255,255,0.8)",
    border: `1.5px solid ${C.pinkLight}`,
    borderRadius: 16, padding: "12px 16px",
    margin: "0 0 10px", display: "flex", gap: 10,
    alignItems: "flex-start", textAlign: "left",
    backdropFilter: "blur(8px)",
  },
  aiCommentIcon: { fontSize: 18, flexShrink: 0 },
  aiCommentText: {
    fontSize: 13, lineHeight: 1.7,
    color: C.gray700, margin: 0, wordBreak: "keep-all",
  },
  reason: {
    fontSize: 13, color: C.gray500, lineHeight: 1.6,
    margin: "0 0 10px", wordBreak: "keep-all",
  },
  headerDesc: {
    fontSize: 11, color: C.gray400, margin: 0,
  },

  /* 목록 */
  list: {
    position: "relative", zIndex: 1,
    maxWidth: 480, margin: "0 auto",
    padding: "0 16px",
    display: "flex", flexDirection: "column", gap: 10,
  },

  /* 카드 */
  card: {
    background: "rgba(255,255,255,0.85)",
    backdropFilter: "blur(12px)",
    border: `1.5px solid ${C.pinkLight}`,
    borderRadius: 18,
    padding: "14px 16px",
    display: "flex", alignItems: "center", gap: 14,
    position: "relative",
  },
  badge: {
    flexShrink: 0,
    width: 24, height: 24, borderRadius: "50%",
    background: `linear-gradient(135deg, ${C.pink}, ${C.purple})`,
    color: C.white, fontSize: 11, fontWeight: 800,
    display: "flex", alignItems: "center", justifyContent: "center",
  },
  coverBox: {
    flexShrink: 0,
    width: 52, height: 68, borderRadius: 8,
    background: C.pinkLight,
    display: "flex", alignItems: "center", justifyContent: "center",
    overflow: "hidden",
  },
  coverImg: { width: "100%", height: "100%", objectFit: "cover" },
  cardBody: { flex: 1, minWidth: 0 },
  genreTag: {
    fontSize: 9, fontWeight: 800, color: C.purple,
    letterSpacing: "0.1em", textTransform: "uppercase",
  },
  cardTitle: {
    fontSize: 14, fontWeight: 800, color: C.gray800,
    margin: "3px 0 2px", lineHeight: 1.3, wordBreak: "keep-all",
    overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap",
  },
  cardAuthor: { fontSize: 12, color: C.gray400, margin: 0 },
  arrow: { flexShrink: 0, fontSize: 14, color: C.pink, fontWeight: 700 },

  /* 모달 */
  overlay: {
    position: "fixed", inset: 0, zIndex: 100,
    background: "rgba(0,0,0,0.4)", backdropFilter: "blur(4px)",
    display: "flex", alignItems: "flex-end", justifyContent: "center",
  },
  modal: {
    width: "100%", maxWidth: 480,
    background: C.white, borderRadius: "28px 28px 0 0",
    padding: "20px 24px 40px", position: "relative",
    maxHeight: "80vh", overflowY: "auto",
    display: "flex", flexDirection: "column", gap: 0,
  },
  handle: {
    width: 36, height: 4, borderRadius: 2,
    background: C.gray200, margin: "0 auto 16px",
  },
  closeBtn: {
    position: "absolute", top: 16, right: 16,
    width: 32, height: 32, borderRadius: "50%",
    border: "none", background: C.gray100,
    color: C.gray500, fontSize: 13, cursor: "pointer",
    display: "flex", alignItems: "center", justifyContent: "center",
    fontFamily: "'Noto Sans KR', sans-serif",
  },
  modalTop: {
    display: "flex", gap: 16, alignItems: "flex-start", marginBottom: 16,
  },
  modalCover: {
    flexShrink: 0,
    width: 72, height: 96, borderRadius: 10,
    background: C.pinkLight,
    display: "flex", alignItems: "center", justifyContent: "center",
    overflow: "hidden", border: `1.5px solid ${C.pinkLight}`,
  },
  modalCoverImg: { width: "100%", height: "100%", objectFit: "cover" },
  modalInfo: {
    flex: 1, minWidth: 0,
    display: "flex", flexDirection: "column", gap: 4, paddingTop: 4,
  },
  modalKdc: {
    fontSize: 10, fontWeight: 800, color: C.purple,
    letterSpacing: "0.1em", textTransform: "uppercase",
  },
  modalTitle: {
    fontSize: 17, fontWeight: 900, color: C.gray800,
    margin: 0, lineHeight: 1.3, wordBreak: "keep-all",
  },
  modalAuthor: { fontSize: 13, color: C.gray500, margin: 0 },
  modalPublisher: { fontSize: 11, color: C.gray400, margin: 0 },
  divider: {
    width: "100%", height: 1,
    background: C.pinkLight, margin: "0 0 14px",
  },
  matchReasonBox: {
    background: C.purpleLight, borderRadius: 12,
    padding: "10px 14px", marginBottom: 12,
  },
  matchReasonLabel: {
    fontSize: 11, fontWeight: 800, color: C.purple,
    display: "block", marginBottom: 4,
  },
  matchReasonText: {
    fontSize: 13, color: C.gray700, margin: 0,
    lineHeight: 1.6, wordBreak: "keep-all",
  },
  modalSummary: {
    fontSize: 14, lineHeight: 1.85,
    color: C.gray700, margin: "0 0 20px",
    wordBreak: "keep-all",
  },
  detailBtn: {
    width: "100%", padding: "14px 0",
    borderRadius: 16, border: "none",
    background: `linear-gradient(135deg, ${C.pink}, ${C.purple})`,
    color: C.white, fontSize: 15, fontWeight: 800,
    cursor: "pointer",
    fontFamily: "'Noto Sans KR', sans-serif",
    boxShadow: "0 8px 20px rgba(244,114,182,0.35)",
  },

  /* 로딩 스피너 */
  spinner: {
    width: 40, height: 40, borderRadius: "50%",
    margin: "0 auto",
    border: `3px solid ${C.pinkLight}`,
    borderTopColor: C.pink,
    animation: "spin-cw 1s linear infinite",
  },

  /* 에러 재시도 버튼 */
  retryBtn: {
    padding: "12px 28px", borderRadius: 18, border: "none",
    cursor: "pointer",
    background: `linear-gradient(135deg, ${C.pink}, ${C.purple})`,
    color: C.white, fontSize: 14, fontWeight: 800,
    fontFamily: "'Noto Sans KR', sans-serif",
  },
};
