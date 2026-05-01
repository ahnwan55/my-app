import { useState, useEffect, useCallback } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import { Search, BookOpen, CheckCircle, XCircle, Clock, AlertTriangle, Plus, X, ChevronLeft } from "lucide-react";

/**
 * 도서관 도서 재고 조회 페이지 (InventoryPage)
 *
 * [진입 경로 2가지]
 * 1. 직접 접근 — /inventory
 *    → ISBN 입력 필드가 비어 있는 상태로 시작
 *
 * 2. 추천 결과 페이지 연동 — /inventory?isbn=9791165920715
 *    → URL 파라미터에서 ISBN을 읽어 자동으로 조회를 실행한다.
 *    → 사용자가 별도로 ISBN을 입력하지 않아도 된다.
 *
 * [동작 흐름]
 * - 로그인 상태: 마이페이지에 등록된 내 도서관이 자동으로 조회된다.
 * - 추가 도서관: 도서관 코드를 직접 입력하여 조회 대상에 추가할 수 있다.
 * - 결과 표시: 도서관별 카드에 AVAILABLE / ON_LOAN / NOT_HELD / ERROR 상태를 시각화한다.
 */
export default function InventoryPage() {
  // ── URL 파라미터 ────────────────────────────────────────────────
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  // 추천 결과 페이지에서 넘어온 경우 ISBN이 URL에 담겨 있다.
  const isbnFromUrl = searchParams.get("isbn") || "";

  // ── 상태 ────────────────────────────────────────────────────────
  const [isbn, setIsbn] = useState(isbnFromUrl);           // ISBN 입력값
  const [extraLibCode, setExtraLibCode] = useState("");    // 추가 도서관 코드 입력값
  const [extraLibCodes, setExtraLibCodes] = useState([]); // 추가된 도서관 코드 목록
  const [results, setResults] = useState([]);              // 조회 결과 리스트
  const [loading, setLoading] = useState(false);           // 로딩 상태
  const [error, setError] = useState(null);                // 전체 오류 메시지

  // ── 조회 실행 함수 ──────────────────────────────────────────────
  /**
   * 백엔드 /api/inventory를 호출하여 결과를 state에 저장한다.
   * useCallback으로 감싸서 useEffect의 의존성 배열에 안전하게 포함시킨다.
   */
  const fetchInventory = useCallback(async (targetIsbn, libCodes) => {
    if (!targetIsbn || targetIsbn.trim().length === 0) return;

    setLoading(true);
    setError(null);
    setResults([]);

    try {
      // 쿼리 파라미터 조합: libCodes가 여러 개인 경우 &libCodes=xxx 형태로 반복한다.
      const params = new URLSearchParams({ isbn: targetIsbn.trim() });
      libCodes.forEach((code) => params.append("libCodes", code));

      const response = await fetch(`/api/inventory?${params.toString()}`, {
        credentials: "include", // 쿠키(세션/JWT)를 포함하여 로그인 상태를 서버에 전달한다.
      });

      if (!response.ok) {
        throw new Error(`서버 오류: ${response.status}`);
      }

      const data = await response.json();
      setResults(data);
    } catch (e) {
      setError("재고 조회 중 오류가 발생하였습니다. 잠시 후 다시 시도해 주세요.");
      console.error("[InventoryPage] fetchInventory 오류:", e);
    } finally {
      setLoading(false);
    }
  }, []);

  // ── 초기 자동 조회 ───────────────────────────────────────────────
  // URL에 isbn 파라미터가 있으면 페이지 진입 즉시 조회를 실행한다.
  // 추천 결과 페이지에서 넘어온 경우가 이에 해당한다.
  useEffect(() => {
    if (isbnFromUrl) {
      fetchInventory(isbnFromUrl, []);
    }
  }, [isbnFromUrl, fetchInventory]);

  // ── 이벤트 핸들러 ────────────────────────────────────────────────

  /** 검색 폼 제출 */
  const handleSearch = (e) => {
    e.preventDefault();
    fetchInventory(isbn, extraLibCodes);
  };

  /** 추가 도서관 코드 입력 후 Enter 또는 + 버튼 */
  const handleAddLibCode = () => {
    const trimmed = extraLibCode.trim();
    if (trimmed && !extraLibCodes.includes(trimmed)) {
      setExtraLibCodes((prev) => [...prev, trimmed]);
    }
    setExtraLibCode("");
  };

  const handleAddLibCodeKeyDown = (e) => {
    if (e.key === "Enter") {
      e.preventDefault();
      handleAddLibCode();
    }
  };

  /** 추가한 도서관 코드 제거 */
  const handleRemoveLibCode = (code) => {
    setExtraLibCodes((prev) => prev.filter((c) => c !== code));
  };

  // ── 렌더링 ──────────────────────────────────────────────────────
  return (
    <div style={styles.page}>
      {/* 뒤로가기 */}
      <button style={styles.backBtn} onClick={() => navigate(-1)}>
        <ChevronLeft size={18} />
        뒤로
      </button>

      {/* 페이지 타이틀 */}
      <header style={styles.header}>
        <BookOpen size={32} style={{ color: "var(--color-primary)" }} />
        <h1 style={styles.title}>도서 재고 조회</h1>
        <p style={styles.subtitle}>
          ISBN을 입력하면 내 도서관의 소장 여부와 대출 가능 여부를 확인할 수 있습니다.
        </p>
      </header>

      {/* 검색 폼 */}
      <form style={styles.form} onSubmit={handleSearch}>
        {/* ISBN 입력 */}
        <div style={styles.inputRow}>
          <div style={styles.inputWrapper}>
            <Search size={16} style={styles.inputIcon} />
            <input
              style={styles.input}
              type="text"
              placeholder="ISBN-13 입력 (예: 9791165920715)"
              value={isbn}
              onChange={(e) => setIsbn(e.target.value)}
              maxLength={13}
              inputMode="numeric"
            />
          </div>
          <button style={styles.searchBtn} type="submit" disabled={loading}>
            {loading ? "조회 중..." : "조회"}
          </button>
        </div>

        {/* 추가 도서관 코드 입력 */}
        <div style={styles.extraSection}>
          <p style={styles.extraLabel}>
            추가 도서관 코드{" "}
            <span style={styles.extraHint}>(정보나루 도서관 코드, 선택)</span>
          </p>
          <div style={styles.inputRow}>
            <input
              style={{ ...styles.input, flex: 1 }}
              type="text"
              placeholder="도서관 코드 입력 후 Enter (예: 111001)"
              value={extraLibCode}
              onChange={(e) => setExtraLibCode(e.target.value)}
              onKeyDown={handleAddLibCodeKeyDown}
            />
            <button
              style={styles.addBtn}
              type="button"
              onClick={handleAddLibCode}
              disabled={!extraLibCode.trim()}
            >
              <Plus size={16} />
            </button>
          </div>

          {/* 추가된 도서관 코드 태그 목록 */}
          {extraLibCodes.length > 0 && (
            <div style={styles.tagList}>
              {extraLibCodes.map((code) => (
                <span key={code} style={styles.tag}>
                  {code}
                  <button
                    style={styles.tagRemoveBtn}
                    type="button"
                    onClick={() => handleRemoveLibCode(code)}
                    aria-label={`${code} 제거`}
                  >
                    <X size={12} />
                  </button>
                </span>
              ))}
            </div>
          )}
        </div>
      </form>

      {/* 전체 오류 메시지 */}
      {error && (
        <div style={styles.errorBox}>
          <AlertTriangle size={16} />
          {error}
        </div>
      )}

      {/* 결과 없음 안내 */}
      {!loading && !error && results.length === 0 && isbn && (
        <p style={styles.emptyMsg}>
          등록된 도서관이 없거나, 해당 ISBN의 소장 정보가 확인되지 않습니다.
          <br />
          마이페이지에서 도서관을 등록하거나 도서관 코드를 직접 입력해 보세요.
        </p>
      )}

      {/* 조회 결과 카드 목록 */}
      {results.length > 0 && (
        <section style={styles.resultSection}>
          <h2 style={styles.resultTitle}>조회 결과 ({results.length}개 도서관)</h2>
          <div style={styles.cardGrid}>
            {results.map((item) => (
              <InventoryCard key={item.libCode} item={item} />
            ))}
          </div>
        </section>
      )}
    </div>
  );
}

// ── 도서관 재고 카드 컴포넌트 ──────────────────────────────────────────

/**
 * 도서관 단건 재고 카드
 *
 * status 값에 따라 색상과 아이콘이 달라진다.
 *   AVAILABLE → 녹색 / 체크 아이콘 / "대출 가능"
 *   ON_LOAN   → 주황색 / 시계 아이콘 / "대출 중"
 *   NOT_HELD  → 회색 / X 아이콘 / "미소장"
 *   ERROR     → 빨간색 / 경고 아이콘 / "조회 실패"
 */
function InventoryCard({ item }) {
  const config = STATUS_CONFIG[item.status] || STATUS_CONFIG.ERROR;

  return (
    <div style={{ ...styles.card, borderColor: config.borderColor }}>
      {/* 상태 뱃지 */}
      <div style={{ ...styles.badge, backgroundColor: config.bgColor, color: config.color }}>
        <config.Icon size={14} />
        <span>{config.label}</span>
      </div>

      {/* 도서관 정보 */}
      <p style={styles.libName}>{item.libName}</p>
      <p style={styles.libCode}>{item.libCode}</p>

      {/* 오류가 아닌 경우 소장/대출 가능 여부를 텍스트로도 표시 */}
      {!item.error && (
        <div style={styles.detailRow}>
          <span style={styles.detailItem}>
            소장: <strong>{item.hasBook ? "있음" : "없음"}</strong>
          </span>
          {item.hasBook && (
            <span style={styles.detailItem}>
              대출: <strong>{item.loanAvail ? "가능" : "불가"}</strong>
            </span>
          )}
        </div>
      )}
    </div>
  );
}

// ── 상태 설정 맵 ────────────────────────────────────────────────────────

const STATUS_CONFIG = {
  AVAILABLE: {
    label: "대출 가능",
    Icon: CheckCircle,
    color: "#16a34a",
    bgColor: "#dcfce7",
    borderColor: "#86efac",
  },
  ON_LOAN: {
    label: "대출 중",
    Icon: Clock,
    color: "#d97706",
    bgColor: "#fef3c7",
    borderColor: "#fcd34d",
  },
  NOT_HELD: {
    label: "미소장",
    Icon: XCircle,
    color: "#6b7280",
    bgColor: "#f3f4f6",
    borderColor: "#d1d5db",
  },
  ERROR: {
    label: "조회 실패",
    Icon: AlertTriangle,
    color: "#dc2626",
    bgColor: "#fee2e2",
    borderColor: "#fca5a5",
  },
};

// ── 스타일 ───────────────────────────────────────────────────────────────
// 기존 프로젝트의 CSS 변수(--color-primary 등)를 활용하여 디자인 일관성을 유지한다.
// CSS 변수가 없는 경우를 대비해 fallback 값을 함께 지정한다.

const styles = {
  page: {
    maxWidth: "780px",
    margin: "0 auto",
    padding: "2rem 1.5rem 4rem",
    fontFamily: "'Pretendard', 'Noto Sans KR', sans-serif",
    color: "#1f2937",
  },
  backBtn: {
    display: "flex",
    alignItems: "center",
    gap: "4px",
    background: "none",
    border: "none",
    cursor: "pointer",
    color: "#6b7280",
    fontSize: "0.875rem",
    padding: "0",
    marginBottom: "1.5rem",
  },
  header: {
    textAlign: "center",
    marginBottom: "2rem",
  },
  title: {
    fontSize: "1.75rem",
    fontWeight: "700",
    margin: "0.5rem 0 0.25rem",
    color: "var(--color-primary, #1e3a5f)",
  },
  subtitle: {
    fontSize: "0.9rem",
    color: "#6b7280",
    margin: 0,
  },
  form: {
    background: "#fff",
    border: "1px solid #e5e7eb",
    borderRadius: "12px",
    padding: "1.5rem",
    marginBottom: "1.5rem",
    boxShadow: "0 1px 4px rgba(0,0,0,0.06)",
  },
  inputRow: {
    display: "flex",
    gap: "0.5rem",
    alignItems: "center",
  },
  inputWrapper: {
    flex: 1,
    position: "relative",
    display: "flex",
    alignItems: "center",
  },
  inputIcon: {
    position: "absolute",
    left: "12px",
    color: "#9ca3af",
    pointerEvents: "none",
  },
  input: {
    width: "100%",
    padding: "0.6rem 0.75rem 0.6rem 2.25rem",
    border: "1px solid #d1d5db",
    borderRadius: "8px",
    fontSize: "0.9rem",
    outline: "none",
    boxSizing: "border-box",
  },
  searchBtn: {
    padding: "0.6rem 1.25rem",
    background: "var(--color-primary, #1e3a5f)",
    color: "#fff",
    border: "none",
    borderRadius: "8px",
    fontSize: "0.9rem",
    fontWeight: "600",
    cursor: "pointer",
    whiteSpace: "nowrap",
  },
  addBtn: {
    padding: "0.6rem 0.75rem",
    background: "#f3f4f6",
    color: "#374151",
    border: "1px solid #d1d5db",
    borderRadius: "8px",
    cursor: "pointer",
    display: "flex",
    alignItems: "center",
  },
  extraSection: {
    marginTop: "1rem",
  },
  extraLabel: {
    fontSize: "0.85rem",
    fontWeight: "600",
    color: "#374151",
    marginBottom: "0.5rem",
  },
  extraHint: {
    fontWeight: "400",
    color: "#9ca3af",
    fontSize: "0.8rem",
  },
  tagList: {
    display: "flex",
    flexWrap: "wrap",
    gap: "0.4rem",
    marginTop: "0.5rem",
  },
  tag: {
    display: "flex",
    alignItems: "center",
    gap: "4px",
    background: "#eff6ff",
    color: "#1d4ed8",
    border: "1px solid #bfdbfe",
    borderRadius: "9999px",
    padding: "0.2rem 0.6rem",
    fontSize: "0.8rem",
  },
  tagRemoveBtn: {
    background: "none",
    border: "none",
    cursor: "pointer",
    color: "#93c5fd",
    padding: "0",
    display: "flex",
    alignItems: "center",
  },
  errorBox: {
    display: "flex",
    alignItems: "center",
    gap: "8px",
    background: "#fee2e2",
    color: "#dc2626",
    border: "1px solid #fca5a5",
    borderRadius: "8px",
    padding: "0.75rem 1rem",
    fontSize: "0.875rem",
    marginBottom: "1rem",
  },
  emptyMsg: {
    textAlign: "center",
    color: "#9ca3af",
    fontSize: "0.9rem",
    lineHeight: "1.6",
    marginTop: "2rem",
  },
  resultSection: {
    marginTop: "0.5rem",
  },
  resultTitle: {
    fontSize: "1rem",
    fontWeight: "600",
    color: "#374151",
    marginBottom: "1rem",
  },
  cardGrid: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fill, minmax(220px, 1fr))",
    gap: "1rem",
  },
  card: {
    background: "#fff",
    border: "1.5px solid",
    borderRadius: "10px",
    padding: "1rem 1.25rem",
    boxShadow: "0 1px 3px rgba(0,0,0,0.05)",
  },
  badge: {
    display: "inline-flex",
    alignItems: "center",
    gap: "4px",
    padding: "0.2rem 0.6rem",
    borderRadius: "9999px",
    fontSize: "0.78rem",
    fontWeight: "600",
    marginBottom: "0.75rem",
  },
  libName: {
    fontSize: "0.95rem",
    fontWeight: "600",
    color: "#111827",
    margin: "0 0 0.2rem",
  },
  libCode: {
    fontSize: "0.78rem",
    color: "#9ca3af",
    margin: "0 0 0.75rem",
  },
  detailRow: {
    display: "flex",
    gap: "0.75rem",
    borderTop: "1px solid #f3f4f6",
    paddingTop: "0.6rem",
  },
  detailItem: {
    fontSize: "0.8rem",
    color: "#6b7280",
  },
};
