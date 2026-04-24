import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

/**
 * RankingPage.jsx — 이달의 대출 랭킹 페이지
 *
 * API 연동:
 *  GET /api/books/ranking
 *  credentials: "include" (httpOnly 쿠키 JWT 자동 포함)
 *
 *  응답 구조:
 *  {
 *    yearMonth: "2026-04",
 *    items: [
 *      {
 *        ranking, bookId, title, author,
 *        publisher, pubYear, coverUrl, kdc, loanCount
 *      }
 *    ]
 *  }
 *
 * 구성:
 *  1. 상단 헤더 + 뒤로가기
 *  2. 1~3위 포디움 카드 (골드/실버/브론즈)
 *  3. 4~10위 리스트
 *
 * 컬러: 벚꽃 핑크(#f472b6) × 퍼플(#a855f7)
 */

const C = {
  pink:        "#f472b6",
  pinkDark:    "#ec4899",
  pinkLight:   "#fce7f3",
  pinkBg:      "#fdf2f8",
  purple:      "#a855f7",
  purpleDark:  "#7c3aed",
  purpleLight: "#ede9fe",
  white:       "#ffffff",
  gray50:      "#f9fafb",
  gray100:     "#f3f4f6",
  gray200:     "#e5e7eb",
  gray400:     "#9ca3af",
  gray500:     "#6b7280",
  gray700:     "#374151",
  gray800:     "#1f2937",
  gold:        "#f59e0b",
  silver:      "#9ca3af",
  bronze:      "#c4956a",
};

const MEDAL = {
  1: { color: "#f59e0b", label: "🥇", bg: "#fffbeb", border: "#fde68a" },
  2: { color: "#9ca3af", label: "🥈", bg: "#f9fafb", border: "#e5e7eb" },
  3: { color: "#c4956a", label: "🥉", bg: "#fff7ed", border: "#fed7aa" },
};

export default function RankingPage() {
  const navigate = useNavigate();

  const [loading,   setLoading]   = useState(true);
  const [error,     setError]     = useState("");
  const [yearMonth, setYearMonth] = useState("");
  const [items,     setItems]     = useState([]);

  /* ── GET /api/books/ranking 호출 ── */
  useEffect(() => {
    const fetchRanking = async () => {
      try {
        const res = await fetch("/api/books/ranking", {
          credentials: "include",  // httpOnly 쿠키(JWT) 자동 포함
        });
        if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
        const data = await res.json();
        setYearMonth(data.yearMonth ?? "");
        setItems(data.items ?? []);
      } catch (e) {
        setError("랭킹 데이터를 불러오는 데 실패했어요. 다시 시도해주세요.");
      } finally {
        setLoading(false);
      }
    };
    fetchRanking();
  }, []);

  const top3 = items.slice(0, 3);
  const rest = items.slice(3);

  // yearMonth "2026-04" → "4월"
  const monthLabel = yearMonth
    ? `${parseInt(yearMonth.split("-")[1])}월`
    : "";

  /* ── 로딩 ── */
  if (loading) {
    return (
      <div style={{ ...S.wrap, display: "flex", alignItems: "center", justifyContent: "center" }}>
        <BgDecor />
        <div style={{ position: "relative", zIndex: 1, textAlign: "center" }}>
          <div style={S.spinner} />
          <p style={{ fontSize: 13, color: C.gray400, marginTop: 16 }}>랭킹을 불러오고 있어요...</p>
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

  /* ── 데이터 없음 ── */
  if (items.length === 0) {
    return (
      <div style={{ ...S.wrap, display: "flex", alignItems: "center", justifyContent: "center" }}>
        <BgDecor />
        <div style={{ position: "relative", zIndex: 1, textAlign: "center", padding: "0 24px" }}>
          <span style={{ fontSize: 48 }}>📚</span>
          <p style={{ fontSize: 15, color: C.gray700, margin: "16px 0 4px", fontWeight: 700 }}>이달의 랭킹 데이터가 없어요.</p>
          <p style={{ fontSize: 13, color: C.gray400, margin: "0 0 20px" }}>매월 1일 갱신됩니다.</p>
          <button onClick={() => navigate("/")} style={S.retryBtn}>홈으로</button>
        </div>
      </div>
    );
  }

  return (
    <div style={S.wrap}>
      <BgDecor />

      <div style={S.inner}>

        {/* 뒤로가기 */}
        <button onClick={() => navigate("/")} style={S.backBtn}>← 뒤로</button>

        {/* 헤더 */}
        <div style={S.headerRow}>
          <div style={S.headerIcon}>📊</div>
          <span style={S.headerLabel}>이달의 대출 랭킹</span>
        </div>
        <h1 style={S.title}>{monthLabel}의 인기 도서</h1>
        <p style={S.desc}>이번 달 가장 많이 대출된 도서 TOP {items.length}</p>

        {/* 1~3위 포디움 */}
        {top3.length >= 3 && (
          <div style={S.podium}>
            {[top3[1], top3[0], top3[2]].map((book) => {
              const medal = MEDAL[book.ranking];
              const isFirst = book.ranking === 1;
              return (
                <div key={book.ranking} style={{
                  ...S.podiumCard,
                  background: medal.bg,
                  border: `1.5px solid ${medal.border}`,
                  transform: isFirst ? "scale(1.04)" : "scale(1)",
                  boxShadow: isFirst ? "0 8px 24px rgba(245,158,11,0.2)" : "0 2px 8px rgba(0,0,0,0.06)",
                }}>
                  {/* 표지 */}
                  <div style={S.podiumCover}>
                    {book.coverUrl
                      ? <img src={book.coverUrl} alt={book.title} style={S.podiumCoverImg} />
                      : <span style={{ fontSize: 20 }}>{medal.label}</span>
                    }
                  </div>
                  <p style={{ fontSize: 10, fontWeight: 700, color: medal.color, margin: "6px 0 2px", letterSpacing: "0.05em" }}>{book.ranking}위</p>
                  <p style={{ fontSize: 12, fontWeight: 800, color: C.gray800, margin: "0 0 2px", lineHeight: 1.3, textAlign: "center", wordBreak: "keep-all" }}>{book.title}</p>
                  <p style={{ fontSize: 10, color: C.gray400, margin: "0 0 6px" }}>{book.author}</p>
                  {book.loanCount && (
                    <div style={{ display: "inline-flex", alignItems: "center", gap: 3, background: C.pinkLight, borderRadius: 20, padding: "2px 8px" }}>
                      <span style={{ fontSize: 10, fontWeight: 700, color: C.pinkDark }}>{book.loanCount}회</span>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}

        {/* 4~10위 리스트 */}
        {rest.length > 0 && (
          <div style={S.list}>
            {rest.map((book, idx) => (
              <div key={book.bookId} style={{
                ...S.listItem,
                borderBottom: idx < rest.length - 1 ? `1px solid ${C.gray100}` : "none",
              }}>
                <span style={S.listRank}>{book.ranking}</span>
                <div style={S.listCover}>
                  {book.coverUrl
                    ? <img src={book.coverUrl} alt={book.title} style={S.listCoverImg} />
                    : <span style={{ fontSize: 18 }}>📖</span>
                  }
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <p style={S.listTitle}>{book.title}</p>
                  <p style={S.listAuthor}>{book.author}</p>
                </div>
                <div style={{ textAlign: "right", flexShrink: 0 }}>
                  {book.kdc && (
                    <span style={S.listKdc}>{book.kdc}</span>
                  )}
                  {book.loanCount && (
                    <p style={S.listCount}>{book.loanCount}회</p>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}

        {/* 하단 안내 */}
        <p style={S.notice}>* 매월 1일 갱신됩니다.</p>

      </div>

      <style>{`@keyframes spin-cw { from{transform:rotate(0deg)} to{transform:rotate(360deg)} }`}</style>
    </div>
  );
}

/* ── 배경 장식 ── */
function BgDecor() {
  return (
    <div style={S.bgDecor} aria-hidden="true">
      <div style={{ ...S.blob, top: -96, right: -96, background: "#fbcfe8" }} />
      <div style={{ ...S.blob, bottom: -96, left: -96, background: "#e9d5ff" }} />
    </div>
  );
}

/* ── 스타일 ── */
const S = {
  wrap:    { minHeight: "100vh", background: `linear-gradient(135deg, ${C.pinkBg}, ${C.white}, #faf5ff)`, fontFamily: "'Noto Sans KR', sans-serif", position: "relative", overflow: "hidden" },
  bgDecor: { position: "fixed", inset: 0, pointerEvents: "none", overflow: "hidden", zIndex: 0 },
  blob:    { position: "absolute", width: 384, height: 384, borderRadius: "50%", opacity: 0.2, filter: "blur(60px)" },
  inner:   { position: "relative", zIndex: 1, maxWidth: 480, margin: "0 auto", padding: "48px 20px 80px" },

  backBtn: { display: "flex", alignItems: "center", gap: 6, fontSize: 13, color: C.gray400, background: "none", border: "none", cursor: "pointer", marginBottom: 20, padding: 0, fontFamily: "'Noto Sans KR', sans-serif" },

  headerRow:  { display: "flex", alignItems: "center", gap: 10, marginBottom: 8 },
  headerIcon: {
    width: 40, height: 40, borderRadius: 16,
    background: `linear-gradient(135deg, ${C.pink}, ${C.purple})`,
    display: "flex", alignItems: "center", justifyContent: "center",
    fontSize: 20, boxShadow: "0 8px 20px rgba(244,114,182,0.35)",
  },
  headerLabel: { fontSize: 11, fontWeight: 700, letterSpacing: "0.15em", color: C.pink, textTransform: "uppercase" },
  title: { fontSize: 26, fontWeight: 800, color: C.gray800, margin: "0 0 4px" },
  desc:  { fontSize: 13, color: C.gray500, margin: "0 0 24px" },

  /* 포디움 */
  podium: { display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 10, marginBottom: 16 },
  podiumCard: {
    borderRadius: 20, padding: "14px 10px",
    textAlign: "center",
    display: "flex", flexDirection: "column", alignItems: "center",
    transition: "all 0.2s",
  },
  podiumCover: {
    width: 52, height: 68, borderRadius: 8,
    background: C.pinkLight,
    display: "flex", alignItems: "center", justifyContent: "center",
    overflow: "hidden",
  },
  podiumCoverImg: { width: "100%", height: "100%", objectFit: "cover" },

  /* 리스트 */
  list: {
    background: "rgba(255,255,255,0.7)",
    backdropFilter: "blur(12px)",
    borderRadius: 20, border: `1px solid ${C.pinkLight}`,
    overflow: "hidden", marginBottom: 16,
  },
  listItem: {
    display: "flex", alignItems: "center", gap: 12,
    padding: "13px 16px",
  },
  listRank: { fontSize: 14, fontWeight: 800, color: C.gray400, width: 20, textAlign: "center", flexShrink: 0 },
  listCover: {
    width: 36, height: 48, borderRadius: 6, flexShrink: 0,
    background: C.pinkLight,
    display: "flex", alignItems: "center", justifyContent: "center",
    overflow: "hidden",
  },
  listCoverImg: { width: "100%", height: "100%", objectFit: "cover" },
  listTitle:  { fontSize: 13, fontWeight: 700, color: C.gray800, margin: "0 0 2px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" },
  listAuthor: { fontSize: 11, color: C.gray400, margin: 0 },
  listKdc:    { fontSize: 10, fontWeight: 600, padding: "2px 8px", borderRadius: 20, background: C.pinkLight, color: C.pinkDark, display: "inline-block", marginBottom: 3 },
  listCount:  { fontSize: 12, fontWeight: 700, color: C.purple, margin: 0 },

  notice: { fontSize: 11, color: C.gray400, textAlign: "center" },

  spinner: {
    width: 40, height: 40, borderRadius: "50%",
    margin: "0 auto",
    border: `3px solid ${C.pinkLight}`,
    borderTopColor: C.pink,
    animation: "spin-cw 1s linear infinite",
  },
  retryBtn: {
    padding: "12px 28px", borderRadius: 18, border: "none",
    cursor: "pointer",
    background: `linear-gradient(135deg, ${C.pink}, ${C.purple})`,
    color: C.white, fontSize: 14, fontWeight: 800,
    fontFamily: "'Noto Sans KR', sans-serif",
  },
};
