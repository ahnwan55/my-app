import { useState } from "react";
import { useNavigate } from "react-router-dom";

/**
 * SearchPage.jsx — 도서 검색 페이지 (신규)
 *
 * 현재: 더미 데이터로 키워드 필터링 UI 완성
 * 이후: Spring Boot GET /api/books/search?keyword={keyword} 연동
 *
 * 구성:
 *  1. 검색 인풋 + 버튼
 *  2. 장르 필터 칩
 *  3. 검색 결과 카드 리스트
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
};

// 더미 도서 데이터 (API 연동 전)
const DUMMY_BOOKS = [
  { id: 1,  title: "채식주의자",         author: "한강",      genre: "소설",   available: true,  count: 3 },
  { id: 2,  title: "82년생 김지영",      author: "조남주",    genre: "소설",   available: false, count: 0 },
  { id: 3,  title: "아몬드",             author: "손원평",    genre: "소설",   available: true,  count: 2 },
  { id: 4,  title: "달러구트 꿈 백화점", author: "이미예",    genre: "판타지", available: true,  count: 1 },
  { id: 5,  title: "죽고 싶지만 떡볶이는 먹고 싶어", author: "백세희", genre: "에세이", available: true, count: 4 },
  { id: 6,  title: "파친코",             author: "이민진",    genre: "소설",   available: false, count: 0 },
  { id: 7,  title: "불편한 편의점",      author: "김호연",    genre: "소설",   available: true,  count: 2 },
  { id: 8,  title: "트렌드 코리아 2025", author: "김난도 외", genre: "경제",   available: true,  count: 1 },
  { id: 9,  title: "어린 왕자",          author: "생텍쥐페리",genre: "문학",   available: true,  count: 5 },
  { id: 10, title: "미드나잇 라이브러리",author: "매트 헤이그",genre: "소설",  available: false, count: 0 },
  { id: 11, title: "사피엔스",           author: "유발 하라리",genre: "인문",  available: true,  count: 2 },
  { id: 12, title: "총, 균, 쇠",         author: "재레드 다이아몬드", genre: "인문", available: true, count: 1 },
];

const GENRES = ["전체", "소설", "에세이", "인문", "경제", "판타지", "문학"];

export default function SearchPage() {
  const navigate = useNavigate();
  const [keyword, setKeyword] = useState("");
  const [query, setQuery]     = useState("");       // 실제 검색에 사용되는 값
  const [genre, setGenre]     = useState("전체");

  // 검색 실행
  const handleSearch = () => setQuery(keyword.trim());
  const handleKeyDown = (e) => { if (e.key === "Enter") handleSearch(); };

  // 더미 필터링 (API 연동 시 이 로직을 API 호출로 교체)
  const filtered = DUMMY_BOOKS.filter((book) => {
    const matchKeyword = query === "" || book.title.includes(query) || book.author.includes(query);
    const matchGenre   = genre === "전체" || book.genre === genre;
    return matchKeyword && matchGenre;
  });

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

        {/* 장르 필터 칩 */}
        <div style={styles.filterRow}>
          {GENRES.map((g) => (
            <button
              key={g}
              onClick={() => setGenre(g)}
              style={{
                ...styles.filterChip,
                background: genre === g ? `linear-gradient(135deg, ${C.pink}, ${C.purple})` : C.white,
                color:      genre === g ? C.white : C.gray500,
                border:     `1.5px solid ${genre === g ? C.pink : C.gray200}`,
                fontWeight: genre === g ? 700 : 400,
              }}
            >
              {g}
            </button>
          ))}
        </div>

        {/* 검색 결과 카운트 */}
        <p style={styles.resultCount}>
          {query || genre !== "전체"
            ? `검색 결과 ${filtered.length}권`
            : `전체 도서 ${DUMMY_BOOKS.length}권`}
        </p>

        {/* 검색 결과 리스트 */}
        {filtered.length === 0 ? (
          <div style={styles.empty}>
            <span style={{ fontSize: 40 }}>📭</span>
            <p style={{ fontSize: 14, color: C.gray400, marginTop: 12 }}>검색 결과가 없어요.</p>
          </div>
        ) : (
          <div style={styles.list}>
            {filtered.map((book) => (
              <div key={book.id} style={styles.bookCard}>

                {/* 책 커버 (더미 아이콘) */}
                <div style={{
                  ...styles.bookCover,
                  background: book.available
                    ? `linear-gradient(180deg, ${C.pink}, ${C.purple})`
                    : C.gray200,
                }}>
                  📖
                </div>

                {/* 책 정보 */}
                <div style={{ flex: 1, minWidth: 0 }}>
                  <p style={styles.bookTitle}>{book.title}</p>
                  <p style={styles.bookAuthor}>{book.author}</p>
                  <div style={styles.bookTags}>
                    <span style={{ ...styles.tag, background: C.pinkLight, color: C.pinkDark }}>{book.genre}</span>
                    {book.available ? (
                      <span style={{ ...styles.tag, background: "#f0fdf4", color: "#15803d" }}>
                        대출 가능 ({book.count}권)
                      </span>
                    ) : (
                      <span style={{ ...styles.tag, background: C.gray100, color: C.gray400 }}>
                        대출 중
                      </span>
                    )}
                  </div>
                </div>

              </div>
            ))}
          </div>
        )}

        <p style={styles.notice}>* 현재 더미 데이터 표시 중. 실제 대출 현황과 다를 수 있습니다.</p>

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
  searchBox: { display: "flex", gap: 8, marginBottom: 14 },
  searchInput: {
    flex: 1, padding: "13px 16px",
    border: `1.5px solid ${C.pinkLight}`, borderRadius: 14,
    fontSize: 13, color: C.gray800, outline: "none",
    background: C.white, fontFamily: "'Noto Sans KR', sans-serif",
    transition: "border-color 0.2s",
  },
  searchBtn: {
    padding: "13px 20px", borderRadius: 14, border: "none", cursor: "pointer",
    background: `linear-gradient(135deg, ${C.pink}, ${C.purple})`,
    color: C.white, fontSize: 13, fontWeight: 700,
    boxShadow: "0 4px 12px rgba(244,114,182,0.35)",
    fontFamily: "'Noto Sans KR', sans-serif",
    whiteSpace: "nowrap",
  },

  /* 장르 필터 */
  filterRow: { display: "flex", gap: 6, flexWrap: "wrap", marginBottom: 16 },
  filterChip: {
    padding: "6px 14px", borderRadius: 20,
    fontSize: 12, cursor: "pointer",
    transition: "all 0.2s",
    fontFamily: "'Noto Sans KR', sans-serif",
  },

  resultCount: { fontSize: 12, fontWeight: 700, color: C.gray500, marginBottom: 12 },

  /* 빈 결과 */
  empty: { textAlign: "center", padding: "48px 0" },

  /* 책 리스트 */
  list: {
    background: "rgba(255,255,255,0.7)",
    backdropFilter: "blur(12px)",
    borderRadius: 20, border: `1px solid ${C.pinkLight}`,
    overflow: "hidden", marginBottom: 16,
  },
  bookCard: {
    display: "flex", alignItems: "center", gap: 14,
    padding: "14px 16px",
    borderBottom: `1px solid ${C.gray100}`,
  },
  bookCover: {
    width: 44, height: 60, borderRadius: 8, flexShrink: 0,
    display: "flex", alignItems: "center", justifyContent: "center",
    fontSize: 20, boxShadow: "0 4px 10px rgba(244,114,182,0.2)",
  },
  bookTitle:  { fontSize: 14, fontWeight: 700, color: C.gray800, margin: "0 0 3px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" },
  bookAuthor: { fontSize: 12, color: C.gray500, margin: "0 0 6px" },
  bookTags:   { display: "flex", gap: 6, flexWrap: "wrap" },
  tag: { fontSize: 10, fontWeight: 600, padding: "2px 8px", borderRadius: 20 },

  notice: { fontSize: 11, color: C.gray400, textAlign: "center" },
};
