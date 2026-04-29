import { useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";

/**
 * MainPage.jsx — 메인 랜딩 페이지
 *
 * Props:
 *  onStart {Function} — "페르소나 검사 시작" 버튼 클릭 시 호출
 *                       App.jsx에서 () => navigate("/survey") 전달
 *
 * 내부 이동:
 *  /ranking — 이달의 대출 랭킹
 *  /search  — 도서 검색
 *
 * 컬러: 벚꽃 핑크(#f472b6) × 퍼플(#a855f7)
 *
 * 변경 사항:
 *  - 이달의 인기도서 TOP 5 섹션 추가 및 GET /api/books/ranking 연동
 *  - 페르소나 검사 순서 안내 섹션 제거
 *  - 타이틀 폰트 Noto Sans KR로 변경
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
};

const RANK_COLORS = [
  { bg: "linear-gradient(135deg, #f59e0b, #f97316)", color: "#fff" },
  { bg: "linear-gradient(135deg, #94a3b8, #64748b)", color: "#fff" },
  { bg: "linear-gradient(135deg, #cd7c2f, #a05c1a)", color: "#fff" },
  { bg: C.pinkLight,   color: C.pinkDark  },
  { bg: C.purpleLight, color: C.purpleDark },
];

export default function MainPage({ onStart }) {
  const navigate = useNavigate();

  const [books,   setBooks]   = useState([]);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState(null);

  useEffect(() => {
    fetch("/api/books/ranking")
        .then((res) => {
          if (!res.ok) throw new Error("랭킹 데이터를 불러오지 못했습니다.");
          return res.json();
        })
        .then((data) => {
          setBooks((data.items ?? data).slice(0, 5));
          setLoading(false);
        })
        .catch((err) => {
          setError(err.message);
          setLoading(false);
        });
  }, []);

  return (
      <div style={styles.wrap}>

        {/* 배경 블러 오브 */}
        <div style={styles.bgDecor} aria-hidden="true">
          <div style={{ ...styles.blob, top: -96, right: -96, background: "#fbcfe8" }} />
          <div style={{ ...styles.blob, bottom: -96, left: -96, background: "#e9d5ff" }} />
        </div>

        <div style={styles.inner}>

          {/* 로고 배지 */}
          <div style={styles.badgeRow}>
            <div style={styles.badgeIcon}>📚</div>
            <span style={styles.badgeLabel}>북적북적 · 도서관 플랫폼</span>
          </div>

          {/* 타이틀 */}
          <h1 style={styles.title}>
            당신의 독서 취향을<br />
            <em style={styles.titleEm}>정확하게 파악합니다</em>
          </h1>

          <p style={styles.desc}>
            독서 페르소나 분석을 통해 맞춤형 도서를 추천하고,<br />
            이달의 인기 도서와 빠른 검색 서비스를 제공합니다.
          </p>

          {/* 메인 CTA — 페르소나 검사 */}
          <div onClick={onStart} style={styles.ctaMain}>
            <div style={styles.ctaMainDeco} aria-hidden="true" />
            <div style={styles.ctaMainInner}>
              <div>
                <p style={styles.ctaMainSub}>나만의 독서 유형 찾기</p>
                <h2 style={styles.ctaMainTitle}>페르소나 검사 시작</h2>
              </div>
              <div style={styles.ctaMainIcon}>🧭</div>
            </div>
            <p style={styles.ctaMainDesc}>설문 → AI 분석 → 맞춤 도서 추천</p>
            <div style={styles.ctaMainBtn}>
              <span>시작하기 →</span>
            </div>
          </div>

          {/* 서브 CTA 카드 2개 — 랭킹 / 검색 */}
          <div style={styles.subGrid}>
            <div onClick={() => navigate("/ranking")} style={{ ...styles.subCard, border: `1.5px solid ${C.pinkLight}` }}>
              <span style={styles.subIcon}>📊</span>
              <p style={styles.subTitle}>이달의 랭킹</p>
              <p style={styles.subDesc}>인기 도서 TOP 10</p>
            </div>
            <div onClick={() => navigate("/search")} style={{ ...styles.subCard, border: `1.5px solid ${C.purpleLight}` }}>
              <span style={styles.subIcon}>🔍</span>
              <p style={styles.subTitle}>도서 검색</p>
              <p style={styles.subDesc}>키워드로 찾기</p>
            </div>
          </div>

          {/* 이달의 인기도서 TOP 5 */}
          <div style={styles.rankSection}>
            <p style={styles.rankTitle}>🏆 이달의 인기도서 TOP 5</p>

            {loading && (
                <div style={styles.rankPlaceholder}>
                  {[1, 2, 3, 4, 5].map((i) => (
                      <div key={i} style={styles.rankSkeleton} />
                  ))}
                </div>
            )}

            {error && (
                <p style={styles.rankError}>⚠️ {error}</p>
            )}

            {!loading && !error && books.map((book, idx) => (
                <div key={book.bookId ?? idx} style={styles.rankRow}>
                  <div style={{ ...styles.rankBadge, background: RANK_COLORS[idx].bg, color: RANK_COLORS[idx].color }}>
                    {idx + 1}
                  </div>
                  <div style={styles.rankInfo}>
                    <p style={styles.rankBookTitle}>{book.title}</p>
                    <p style={styles.rankBookMeta}>
                      {book.author}
                      {book.loanCount != null && (
                          <span style={styles.rankLibBadge}>대출 {book.loanCount}회</span>
                      )}
                    </p>
                  </div>
                </div>
            ))}
          </div>

        </div>
      </div>
  );
}

const styles = {
  wrap: {
    minHeight: "100vh",
    background: `linear-gradient(135deg, ${C.pinkBg}, ${C.white}, #faf5ff)`,
    fontFamily: "'Noto Sans KR', sans-serif",
    position: "relative",
    overflow: "hidden",
  },
  bgDecor: { position: "fixed", inset: 0, pointerEvents: "none", overflow: "hidden", zIndex: 0 },
  blob:    { position: "absolute", width: 384, height: 384, borderRadius: "50%", opacity: 0.2, filter: "blur(60px)" },
  inner: {
    position: "relative", zIndex: 1,
    maxWidth: 480, margin: "0 auto",
    padding: "48px 20px 80px",
  },
  badgeRow:  { display: "flex", alignItems: "center", gap: 10, marginBottom: 12 },
  badgeIcon: {
    width: 40, height: 40, borderRadius: 16,
    background: `linear-gradient(135deg, ${C.pink}, ${C.purple})`,
    display: "flex", alignItems: "center", justifyContent: "center",
    fontSize: 20, boxShadow: "0 8px 20px rgba(244,114,182,0.35)",
  },
  badgeLabel: { fontSize: 11, fontWeight: 700, letterSpacing: "0.15em", color: C.pink, textTransform: "uppercase" },
  title: {
    fontFamily: "'Noto Sans KR', sans-serif",
    fontSize: 28, fontWeight: 800, color: C.gray800,
    margin: "0 0 8px", lineHeight: 1.3,
  },
  titleEm: {
    fontStyle: "normal",
    background: `linear-gradient(135deg, ${C.pinkDark}, ${C.purple})`,
    WebkitBackgroundClip: "text", WebkitTextFillColor: "transparent",
  },
  desc: { fontSize: 13, color: C.gray500, lineHeight: 1.7, margin: "0 0 28px" },
  ctaMain: {
    position: "relative", overflow: "hidden",
    borderRadius: 24,
    background: `linear-gradient(135deg, ${C.pink}, ${C.pinkDark}, ${C.purple})`,
    padding: 24, marginBottom: 16,
    cursor: "pointer",
    boxShadow: "0 16px 40px rgba(244,114,182,0.35)",
  },
  ctaMainDeco: {
    position: "absolute", top: -40, right: -40,
    width: 160, height: 160,
    background: "rgba(255,255,255,0.07)", borderRadius: "50%",
  },
  ctaMainInner: { position: "relative", display: "flex", alignItems: "flex-start", justifyContent: "space-between", marginBottom: 10 },
  ctaMainSub:   { fontSize: 10, letterSpacing: "0.15em", color: "rgba(255,255,255,0.7)", textTransform: "uppercase", margin: "0 0 4px", fontWeight: 700 },
  ctaMainTitle: { fontSize: 20, fontWeight: 800, color: C.white, margin: 0 },
  ctaMainIcon:  { width: 48, height: 48, borderRadius: 16, background: "rgba(255,255,255,0.2)", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 24 },
  ctaMainDesc:  { position: "relative", fontSize: 13, color: "rgba(255,255,255,0.8)", lineHeight: 1.6, marginBottom: 16 },
  ctaMainBtn:   {
    position: "relative",
    display: "inline-flex", alignItems: "center", gap: 8,
    background: "rgba(255,255,255,0.2)", borderRadius: 16, padding: "8px 16px",
    color: C.white, fontSize: 13, fontWeight: 700,
  },
  subGrid: { display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12, marginBottom: 24 },
  subCard: {
    background: "rgba(255,255,255,0.7)",
    backdropFilter: "blur(12px)",
    borderRadius: 20, padding: "18px 16px",
    cursor: "pointer", transition: "all 0.2s",
    display: "flex", flexDirection: "column", gap: 4,
    boxShadow: "0 2px 8px rgba(244,114,182,0.08)",
  },
  subIcon:  { fontSize: 28, marginBottom: 4 },
  subTitle: { fontSize: 14, fontWeight: 800, color: C.gray800, margin: 0 },
  subDesc:  { fontSize: 11, color: C.gray400, margin: 0 },
  rankSection: {
    background: "rgba(255,255,255,0.7)",
    backdropFilter: "blur(12px)",
    borderRadius: 20,
    border: `1px solid ${C.pinkLight}`,
    padding: 20,
    marginBottom: 16,
  },
  rankTitle: { fontSize: 13, fontWeight: 700, color: C.gray700, margin: "0 0 14px" },
  rankRow: {
    display: "flex", alignItems: "center", gap: 12,
    padding: "10px 0",
    borderBottom: `1px solid ${C.gray100}`,
  },
  rankBadge: {
    width: 28, height: 28, borderRadius: 10, flexShrink: 0,
    display: "flex", alignItems: "center", justifyContent: "center",
    fontSize: 12, fontWeight: 800,
  },
  rankInfo:      { flex: 1, minWidth: 0 },
  rankBookTitle: { fontSize: 13, fontWeight: 700, color: C.gray800, margin: "0 0 2px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" },
  rankBookMeta:  { fontSize: 11, color: C.gray400, margin: 0, display: "flex", alignItems: "center", gap: 6 },
  rankLibBadge:  {
    background: C.pinkLight, color: C.pinkDark,
    fontSize: 10, fontWeight: 700,
    padding: "1px 6px", borderRadius: 6,
  },
  rankPlaceholder: { display: "flex", flexDirection: "column", gap: 10 },
  rankSkeleton: {
    height: 36, borderRadius: 10,
    background: `linear-gradient(90deg, ${C.gray100}, ${C.gray200}, ${C.gray100})`,
    backgroundSize: "200% 100%",
    animation: "shimmer 1.4s infinite",
  },
  rankError: { fontSize: 12, color: "#ef4444", textAlign: "center", padding: "12px 0", margin: 0 },
};