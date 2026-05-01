import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";

/**
 * BookDetailPage.jsx — 도서 상세 페이지
 *
 * [진입 경로]
 *   /books/:bookId
 *   BookResultPage의 "상세 보기" 버튼 클릭 시 이동
 *
 * [API 연동]
 *   1. GET /api/books/{bookId}
 *      → 도서 기본 정보 (제목, 저자, 출판사, 줄거리 등)
 *
 *   2. GET /api/inventory?isbn={bookId}
 *      → 마이페이지 등록 도서관의 재고 현황
 *      → 로그인 상태이므로 libCodes 파라미터 없이 호출하면
 *        서버가 자동으로 내 도서관을 조회 대상에 포함시킨다.
 *
 * [화면 구성]
 *   - 상단: 표지 + 제목/저자/출판사/KDC
 *   - 중단: 줄거리
 *   - 하단: 내 도서관 재고 현황 카드 목록
 */

const C = {
  pink:        "#f472b6",
  pinkDark:    "#ec4899",
  pinkLight:   "#fce7f3",
  pinkBg:      "#fdf2f8",
  purple:      "#a855f7",
  purpleLight: "#ede9fe",
  white:       "#ffffff",
  gray100:     "#f3f4f6",
  gray200:     "#e5e7eb",
  gray400:     "#9ca3af",
  gray500:     "#6b7280",
  gray700:     "#374151",
  gray800:     "#1f2937",
  green:       "#16a34a",
  greenBg:     "#dcfce7",
  greenBorder: "#86efac",
  orange:      "#d97706",
  orangeBg:    "#fef3c7",
  orangeBorder:"#fcd34d",
  red:         "#dc2626",
  redBg:       "#fee2e2",
  redBorder:   "#fca5a5",
};

// 재고 상태별 색상/텍스트 설정
const STATUS_CONFIG = {
  AVAILABLE: { label: "대출 가능", color: C.green,  bg: C.greenBg,  border: C.greenBorder,  emoji: "✅" },
  ON_LOAN:   { label: "대출 중",   color: C.orange, bg: C.orangeBg, border: C.orangeBorder, emoji: "⏳" },
  NOT_HELD:  { label: "미소장",    color: C.gray500,bg: C.gray100,  border: C.gray200,      emoji: "✕"  },
  ERROR:     { label: "조회 실패", color: C.red,    bg: C.redBg,    border: C.redBorder,    emoji: "⚠️" },
};

export default function BookDetailPage() {
  const { bookId } = useParams();   // URL의 :bookId
  const navigate   = useNavigate();

  // ── 도서 정보 상태 ────────────────────────────────────────────
  const [book,        setBook]        = useState(null);
  const [bookLoading, setBookLoading] = useState(true);
  const [bookError,   setBookError]   = useState("");

  // ── 재고 조회 상태 ────────────────────────────────────────────
  const [inventory,        setInventory]        = useState([]);
  const [inventoryLoading, setInventoryLoading] = useState(true);
  const [inventoryError,   setInventoryError]   = useState("");

  // ── 1. 도서 상세 정보 조회 ─────────────────────────────────────
  useEffect(() => {
    if (!bookId) return;

    const fetchBook = async () => {
      try {
        const res = await fetch(`/api/books/${bookId}`, {
          credentials: "include",
        });
        if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
        const json = await res.json();
        setBook(json);
      } catch (e) {
        setBookError("도서 정보를 불러오는 데 실패했어요.");
        console.error("[BookDetailPage] 도서 조회 실패:", e);
      } finally {
        setBookLoading(false);
      }
    };

    fetchBook();
  }, [bookId]);

  // ── 2. 내 도서관 재고 조회 ─────────────────────────────────────
  // bookId가 ISBN-13이므로 그대로 isbn 파라미터로 전달한다.
  // libCodes를 전달하지 않으면 서버가 마이페이지 등록 도서관을 자동으로 포함시킨다.
  useEffect(() => {
    if (!bookId) return;

    const fetchInventory = async () => {
      try {
        const res = await fetch(`/api/inventory?isbn=${bookId}`, {
          credentials: "include",
        });
        if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
        const json = await res.json();
        setInventory(json);
      } catch (e) {
        setInventoryError("재고 정보를 불러오는 데 실패했어요.");
        console.error("[BookDetailPage] 재고 조회 실패:", e);
      } finally {
        setInventoryLoading(false);
      }
    };

    fetchInventory();
  }, [bookId]);

  // ── 로딩 ──────────────────────────────────────────────────────
  if (bookLoading) {
    return (
      <div style={{ ...S.wrap, display: "flex", alignItems: "center", justifyContent: "center" }}>
        <BgDecor />
        <div style={{ position: "relative", zIndex: 1, textAlign: "center" }}>
          <div style={S.spinner} />
          <p style={{ fontSize: 13, color: C.gray400, marginTop: 16 }}>도서 정보를 불러오는 중...</p>
        </div>
        <style>{SPIN_CSS}</style>
      </div>
    );
  }

  // ── 에러 ──────────────────────────────────────────────────────
  if (bookError) {
    return (
      <div style={{ ...S.wrap, display: "flex", alignItems: "center", justifyContent: "center" }}>
        <BgDecor />
        <div style={{ position: "relative", zIndex: 1, textAlign: "center", padding: "0 24px" }}>
          <span style={{ fontSize: 48 }}>😢</span>
          <p style={{ fontSize: 15, color: C.gray700, margin: "16px 0 20px", fontWeight: 700 }}>{bookError}</p>
          <button style={S.retryBtn} onClick={() => navigate(-1)}>돌아가기</button>
        </div>
      </div>
    );
  }

  return (
    <div style={S.wrap}>
      <BgDecor />

      {/* ── 뒤로가기 ── */}
      <button style={S.backBtn} onClick={() => navigate(-1)}>
        ← 뒤로
      </button>

      <div style={S.inner}>

        {/* ── 표지 + 기본 정보 ── */}
        <div style={S.topSection}>
          <div style={S.coverBox}>
            {book.coverUrl
              ? <img src={book.coverUrl} alt={book.title} style={S.coverImg} />
              : <BookIcon />
            }
          </div>

          <div style={S.infoBox}>
            {book.kdc && <span style={S.kdcTag}>{book.kdc}</span>}
            <h1 style={S.title}>{book.title}</h1>
            <p style={S.author}>{book.author}</p>
            {book.publisher && (
              <p style={S.publisher}>{book.publisher}{book.pubYear ? ` · ${book.pubYear}` : ""}</p>
            )}
          </div>
        </div>

        {/* ── 줄거리 ── */}
        {book.description && (
          <section style={S.section}>
            <h2 style={S.sectionTitle}>📖 줄거리</h2>
            <p style={S.description}>{book.description}</p>
          </section>
        )}

        {/* ── 내 도서관 재고 현황 ── */}
        <section style={S.section}>
          <h2 style={S.sectionTitle}>📍 내 도서관 재고 현황</h2>

          {/* 재고 로딩 중 */}
          {inventoryLoading && (
            <div style={S.inventoryLoading}>
              <div style={{ ...S.spinner, width: 24, height: 24, borderWidth: 2 }} />
              <p style={{ fontSize: 13, color: C.gray400, margin: 0 }}>재고 확인 중...</p>
              <style>{SPIN_CSS}</style>
            </div>
          )}

          {/* 재고 조회 실패 */}
          {!inventoryLoading && inventoryError && (
            <p style={S.inventoryError}>{inventoryError}</p>
          )}

          {/* 등록된 도서관 없음 */}
          {!inventoryLoading && !inventoryError && inventory.length === 0 && (
            <div style={S.emptyInventory}>
              <p style={{ margin: "0 0 12px", fontSize: 14, color: C.gray700 }}>
                마이페이지에서 도서관을 등록하면 재고를 바로 확인할 수 있어요.
              </p>
              <button style={S.myPageBtn} onClick={() => navigate("/mypage")}>
                도서관 등록하러 가기
              </button>
            </div>
          )}

          {/* 재고 카드 목록 */}
          {!inventoryLoading && inventory.length > 0 && (
            <div style={S.inventoryList}>
              {inventory.map((item) => {
                const cfg = STATUS_CONFIG[item.status] || STATUS_CONFIG.ERROR;
                return (
                  <div
                    key={item.libCode}
                    style={{
                      ...S.inventoryCard,
                      borderColor: cfg.border,
                      background: cfg.bg,
                    }}
                  >
                    {/* 상태 뱃지 */}
                    <span style={{ ...S.statusBadge, color: cfg.color }}>
                      {cfg.emoji} {cfg.label}
                    </span>

                    {/* 도서관 이름 */}
                    <p style={S.libName}>{item.libName}</p>
                    <p style={S.libCode}>{item.libCode}</p>
                  </div>
                );
              })}
            </div>
          )}

          {/* 다른 도서관도 조회하기 — 항상 표시 */}
          {!inventoryLoading && (
            <button
              style={S.moreLibBtn}
              onClick={() => navigate(`/inventory?isbn=${bookId}`)}
            >
              🔍 다른 도서관에서도 찾기
            </button>
          )}
        </section>

      </div>
      <style>{SPIN_CSS}</style>
    </div>
  );
}

// ── 배경 장식 ──────────────────────────────────────────────────────
function BgDecor() {
  return (
    <div style={S.bgDecor} aria-hidden="true">
      <div style={{ ...S.blob, top: -80, right: -80, background: "#fbcfe8" }} />
      <div style={{ ...S.blob, bottom: -80, left: -80, background: "#e9d5ff" }} />
    </div>
  );
}

// ── 책 아이콘 (표지 없을 때 fallback) ──────────────────────────────
function BookIcon() {
  return (
    <svg width={60} height={78} viewBox="0 0 36 48" fill="none">
      <rect x="1" y="1" width="34" height="46" rx="3" fill={C.pinkLight} stroke={C.pink} strokeWidth="1.5" />
      <line x1="7" y1="1" x2="7" y2="47" stroke={C.pink} strokeWidth="1.5" />
      <line x1="13" y1="12" x2="29" y2="12" stroke={C.pink} strokeWidth="1" opacity="0.6" />
      <line x1="13" y1="18" x2="29" y2="18" stroke={C.pink} strokeWidth="1" opacity="0.6" />
      <line x1="13" y1="24" x2="23" y2="24" stroke={C.pink} strokeWidth="1" opacity="0.6" />
    </svg>
  );
}

const SPIN_CSS = `@keyframes spin-cw { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }`;

// ── 스타일 ──────────────────────────────────────────────────────────
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
  backBtn: {
    position: "relative", zIndex: 1,
    display: "block",
    background: "none", border: "none",
    color: C.gray500, fontSize: 14, cursor: "pointer",
    padding: "20px 20px 0",
    fontFamily: "'Noto Sans KR', sans-serif",
  },
  inner: {
    position: "relative", zIndex: 1,
    maxWidth: 480, margin: "0 auto",
    padding: "16px 20px",
    display: "flex", flexDirection: "column", gap: 24,
  },

  // 표지 + 기본 정보
  topSection: {
    display: "flex", gap: 20, alignItems: "flex-start",
    background: "rgba(255,255,255,0.85)",
    backdropFilter: "blur(12px)",
    border: `1.5px solid ${C.pinkLight}`,
    borderRadius: 20,
    padding: "20px",
  },
  coverBox: {
    flexShrink: 0,
    width: 90, height: 120, borderRadius: 10,
    background: C.pinkLight,
    display: "flex", alignItems: "center", justifyContent: "center",
    overflow: "hidden",
    border: `1.5px solid ${C.pinkLight}`,
  },
  coverImg: { width: "100%", height: "100%", objectFit: "cover" },
  infoBox: {
    flex: 1, minWidth: 0,
    display: "flex", flexDirection: "column", gap: 5,
  },
  kdcTag: {
    fontSize: 10, fontWeight: 800, color: C.purple,
    letterSpacing: "0.08em",
  },
  title: {
    fontSize: 18, fontWeight: 900, color: C.gray800,
    margin: 0, lineHeight: 1.35, wordBreak: "keep-all",
  },
  author: {
    fontSize: 13, color: C.gray500, margin: 0,
  },
  publisher: {
    fontSize: 11, color: C.gray400, margin: 0,
  },

  // 섹션 공통
  section: {
    background: "rgba(255,255,255,0.85)",
    backdropFilter: "blur(12px)",
    border: `1.5px solid ${C.pinkLight}`,
    borderRadius: 20,
    padding: "20px",
  },
  sectionTitle: {
    fontSize: 15, fontWeight: 800, color: C.gray800,
    margin: "0 0 14px",
  },

  // 줄거리
  description: {
    fontSize: 14, lineHeight: 1.85,
    color: C.gray700, margin: 0,
    wordBreak: "keep-all",
  },

  // 재고 조회
  inventoryLoading: {
    display: "flex", alignItems: "center", gap: 10,
    padding: "8px 0",
  },
  inventoryError: {
    fontSize: 13, color: C.red, margin: 0,
  },
  emptyInventory: {
    textAlign: "center", padding: "8px 0 4px",
  },
  inventoryList: {
    display: "flex", flexDirection: "column", gap: 10,
    marginBottom: 14,
  },
  inventoryCard: {
    border: "1.5px solid",
    borderRadius: 12,
    padding: "12px 16px",
  },
  statusBadge: {
    fontSize: 12, fontWeight: 800,
    display: "block", marginBottom: 6,
  },
  libName: {
    fontSize: 14, fontWeight: 700, color: C.gray800,
    margin: "0 0 2px",
  },
  libCode: {
    fontSize: 11, color: C.gray400, margin: 0,
  },

  // 버튼들
  moreLibBtn: {
    width: "100%", padding: "12px 0",
    borderRadius: 14,
    border: `2px solid ${C.pink}`,
    background: C.white,
    color: C.pinkDark, fontSize: 14, fontWeight: 800,
    cursor: "pointer",
    fontFamily: "'Noto Sans KR', sans-serif",
    marginTop: 4,
  },
  myPageBtn: {
    padding: "10px 20px",
    borderRadius: 12, border: "none",
    background: `linear-gradient(135deg, ${C.pink}, ${C.purple})`,
    color: C.white, fontSize: 13, fontWeight: 800,
    cursor: "pointer",
    fontFamily: "'Noto Sans KR', sans-serif",
  },
  retryBtn: {
    padding: "12px 28px", borderRadius: 18, border: "none",
    cursor: "pointer",
    background: `linear-gradient(135deg, ${C.pink}, ${C.purple})`,
    color: C.white, fontSize: 14, fontWeight: 800,
    fontFamily: "'Noto Sans KR', sans-serif",
  },
  spinner: {
    width: 40, height: 40, borderRadius: "50%",
    border: `3px solid ${C.pinkLight}`,
    borderTopColor: C.pink,
    animation: "spin-cw 1s linear infinite",
  },
};
