import { useState } from "react";
import { useNavigate } from "react-router-dom";

/**
 * SearchPage.jsx — 도서 검색 페이지
 *
 * 변경 사항:
 *  - 더미 데이터 제거 → GET /api/books?keyword={keyword} API 연동
 *  - 초기 진입 시 책 목록 미표시 (검색 후에만 결과 표시)
 *  - 로딩/에러 상태 처리 추가
 *  - 장르 필터는 검색 결과 내에서 프론트 필터링 (추가 API 호출 없음)
 *
 * 검색 흐름:
 *  1. 키워드 입력 → 검색 버튼 클릭 또는 Enter
 *  2. GET /api/books?keyword={keyword} 호출
 *  3. 결과 표시 (제목, 저자, KDC, 커버 이미지)
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
  gray100:     "#f3f4f6",
  gray200:     "#e5e7eb",
  gray400:     "#9ca3af",
  gray500:     "#6b7280",
  gray700:     "#374151",
  gray800:     "#1f2937",
};

export default function SearchPage() {
  const navigate = useNavigate();

  const [keyword, setKeyword] = useState("");   // 입력 중인 키워드
  const [books,   setBooks]   = useState([]);   // 검색 결과
  const [searched, setSearched] = useState(false); // 검색 실행 여부
  const [loading, setLoading] = useState(false);
  const [error,   setError]   = useState(null);

  /**
   * 검색 실행
   * GET /api/books?keyword={keyword} 호출
   * credentials: "include" → httpOnly 쿠키 자동 포함 (인증 유지)
   */
  const handleSearch = async () => {
    const trimmed = keyword.trim();
    if (!trimmed) return;

    setLoading(true);
    setError(null);
    setSearched(true);

    try {
      const res = await fetch(
          `/api/books?keyword=${encodeURIComponent(trimmed)}`,
          { credentials: "include" }
      );
      if (!res.ok) throw new Error("검색 중 오류가 발생했습니다.");
      const data = await res.json();
      setBooks(data);
    } catch (err) {
      setError(err.message);
      setBooks([]);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter") handleSearch();
  };

  return (
      <div style={styles.wrap}>

        {/* 배경 블러 오브 */}
        <div style={styles.bgDecor} aria-hidden="true">
          <div style={{ ...styles.blob, top: -96, right: -96, background: "#fbcfe8" }} />
          <div style={{ ...styles.blob, bottom: -96, left: -96, background: "#e9d5ff" }} />
        </div>

        <div style={styles.inner}>

          {/* 뒤로가기 */}
          <button onClick={() => navigate("/")} style={styles.backBtn}>← 뒤로</button>

          {/* 헤더 */}
          <div style={styles.headerRow}>
            <div style={styles.headerIcon}>🔍</div>
            <span style={styles.headerLabel}>도서 검색</span>
          </div>
          <h1 style={styles.title}>어떤 책을 찾으세요?</h1>

          {/* 검색 인풋 */}
          <div style={styles.searchBox}>
            <input
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="제목 또는 저자를 입력하세요"
                style={styles.searchInput}
            />
            <button onClick={handleSearch} style={styles.searchBtn}>검색</button>
          </div>

          {/* 검색 전 안내 */}
          {!searched && (
              <div style={styles.guide}>
                <span style={{ fontSize: 48 }}>📚</span>
                <p style={styles.guideText}>
                  제목 또는 저자 이름으로 검색해보세요.<br />
                  서울시 전체 공공 도서관 소장 도서를 찾아드립니다.
                </p>
              </div>
          )}

          {/* 로딩 */}
          {loading && (
              <div style={styles.guide}>
                <span style={{ fontSize: 40 }}>⏳</span>
                <p style={styles.guideText}>검색 중입니다...</p>
              </div>
          )}

          {/* 에러 */}
          {error && (
              <p style={styles.errorText}>⚠️ {error}</p>
          )}

          {/* 검색 결과 */}
          {searched && !loading && !error && (
              <>
                <p style={styles.resultCount}>
                  검색 결과 {books.length}권
                </p>

                {books.length === 0 ? (
                    <div style={styles.guide}>
                      <span style={{ fontSize: 40 }}>📭</span>
                      <p style={styles.guideText}>검색 결과가 없어요.</p>
                    </div>
                ) : (
                    <div style={styles.list}>
                      {books.map((book, idx) => (
                          <div key={book.bookId ?? idx} style={styles.bookCard}>

                            {/* 책 커버 */}
                            {book.coverUrl ? (
                                <img
                                    src={book.coverUrl}
                                    alt={book.title}
                                    style={styles.bookCoverImg}
                                    onError={(e) => { e.target.style.display = "none"; }}
                                />
                            ) : (
                                <div style={styles.bookCoverFallback}>📖</div>
                            )}

                            {/* 책 정보 */}
                            <div style={{ flex: 1, minWidth: 0 }}>
                              <p style={styles.bookTitle}>{book.title}</p>
                              <p style={styles.bookAuthor}>{book.author}</p>
                              {book.publisher && (
                                  <p style={styles.bookPublisher}>{book.publisher} · {book.pubYear}</p>
                              )}
                              {book.kdc && (
                                  <span style={styles.kdcTag}>KDC {book.kdc}</span>
                              )}
                            </div>

                          </div>
                      ))}
                    </div>
                )}
              </>
          )}

        </div>
      </div>
  );
}

const styles = {
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
  title: { fontFamily: "'Playfair Display', serif", fontSize: 26, fontWeight: 800, color: C.gray800, margin: "0 0 20px" },

  /* 검색 박스 */
  searchBox: { display: "flex", gap: 8, marginBottom: 20 },
  searchInput: {
    flex: 1, padding: "13px 16px",
    border: `1.5px solid ${C.pinkLight}`, borderRadius: 14,
    fontSize: 13, color: C.gray800, outline: "none",
    background: C.white, fontFamily: "'Noto Sans KR', sans-serif",
  },
  searchBtn: {
    padding: "13px 20px", borderRadius: 14, border: "none", cursor: "pointer",
    background: `linear-gradient(135deg, ${C.pink}, ${C.purple})`,
    color: C.white, fontSize: 13, fontWeight: 700,
    boxShadow: "0 4px 12px rgba(244,114,182,0.35)",
    fontFamily: "'Noto Sans KR', sans-serif",
    whiteSpace: "nowrap",
  },

  /* 안내 영역 */
  guide:     { textAlign: "center", padding: "48px 0" },
  guideText: { fontSize: 13, color: C.gray400, lineHeight: 1.8, marginTop: 12 },
  errorText: { fontSize: 12, color: "#ef4444", textAlign: "center", padding: "12px 0" },

  resultCount: { fontSize: 12, fontWeight: 700, color: C.gray500, marginBottom: 12 },

  /* 책 리스트 */
  list: {
    background: "rgba(255,255,255,0.7)",
    backdropFilter: "blur(12px)",
    borderRadius: 20, border: `1px solid ${C.pinkLight}`,
    overflow: "hidden",
  },
  bookCard: {
    display: "flex", alignItems: "center", gap: 14,
    padding: "14px 16px",
    borderBottom: `1px solid ${C.gray100}`,
  },
  bookCoverImg: {
    width: 44, height: 60, borderRadius: 8, flexShrink: 0,
    objectFit: "cover", boxShadow: "0 4px 10px rgba(244,114,182,0.2)",
  },
  bookCoverFallback: {
    width: 44, height: 60, borderRadius: 8, flexShrink: 0,
    background: `linear-gradient(180deg, ${C.pink}, ${C.purple})`,
    display: "flex", alignItems: "center", justifyContent: "center",
    fontSize: 20, boxShadow: "0 4px 10px rgba(244,114,182,0.2)",
  },
  bookTitle:     { fontSize: 14, fontWeight: 700, color: C.gray800, margin: "0 0 3px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" },
  bookAuthor:    { fontSize: 12, color: C.gray500, margin: "0 0 3px" },
  bookPublisher: { fontSize: 11, color: C.gray400, margin: "0 0 4px" },
  kdcTag: {
    fontSize: 10, fontWeight: 600, padding: "2px 8px", borderRadius: 20,
    background: C.purpleLight, color: C.purpleDark,
  },
};