import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

/**
 * UserInfoPage.jsx — 사용자 정보 입력 페이지
 *
 * 카카오 로그인 완료 후 최초 1회 진입.
 * (App.jsx에서 gender가 null인 경우 이 페이지로 리다이렉트한다.)
 *
 * [단계]
 *   1. 닉네임 입력
 *   2. 성별 선택
 *   3. 연령대 선택
 *   4. 도서관 선택 (메인 필수, 서브 선택)
 *
 * [Props]
 *   onComplete {Function} — 저장 완료 시 App.jsx에서 needsProfile을 false로 바꾸는 콜백
 *
 * [API 연동]
 *   POST  /api/users/info          — 닉네임/성별/연령대 저장
 *   PATCH /api/users/me/libraries  — 도서관 코드 저장
 *   GET   /api/libraries?keyword=  — 도서관 이름 검색
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
  red:         "#ef4444",
};

const GENDER_OPTIONS = [
  { value: "male",   label: "남성",     emoji: "👨" },
  { value: "female", label: "여성",     emoji: "👩" },
  { value: "none",   label: "선택 안함", emoji: "🙂" },
];

const AGE_OPTIONS = [
  { value: "elementary", label: "초등학생"  },
  { value: "middle",     label: "중학생"    },
  { value: "high",       label: "고등학생"  },
  { value: "20s",        label: "20대"      },
  { value: "30s",        label: "30대"      },
  { value: "40s",        label: "40대"      },
  { value: "50s+",       label: "50대 이상" },
];

const STEPS = ["닉네임", "성별", "연령대", "도서관"];

function validateNickname(value) {
  if (!value.trim()) return "닉네임을 입력해주세요.";
  if (value.trim().length < 2) return "닉네임은 2자 이상이어야 합니다.";
  if (value.trim().length > 10) return "닉네임은 10자 이하여야 합니다.";
  if (/[^가-힣a-zA-Z0-9_]/.test(value.trim())) return "특수문자는 사용할 수 없습니다.";
  return "";
}

export default function UserInfoPage({ onComplete }) {
  const navigate = useNavigate();

  const [nickname,      setNickname]      = useState("");
  const [gender,        setGender]        = useState("");
  const [ageGroup,      setAgeGroup]      = useState("");
  const [nicknameError, setNicknameError] = useState("");

  const [libKeyword,   setLibKeyword]   = useState("");
  const [libResults,   setLibResults]   = useState([]);
  const [libSearching, setLibSearching] = useState(false);
  const [mainLib,      setMainLib]      = useState(null);
  const [subLib,       setSubLib]       = useState(null);
  const [activeSlot,   setActiveSlot]   = useState(null);

  const [submitting,  setSubmitting]  = useState(false);
  const [submitError, setSubmitError] = useState("");

  const stepDone = [
    !!(nickname.trim() && !nicknameError),
    !!gender,
    !!ageGroup,
    !!mainLib,
  ];
  const isComplete = stepDone.every(Boolean);

  // 도서관 검색 (500ms 디바운스)
  useEffect(() => {
    if (!libKeyword.trim()) { setLibResults([]); return; }

    const timer = setTimeout(async () => {
      setLibSearching(true);
      try {
        const res = await fetch(
          `/api/libraries?keyword=${encodeURIComponent(libKeyword.trim())}`,
          { credentials: "include" }
        );
        if (!res.ok) throw new Error();
        setLibResults(await res.json());
      } catch {
        setLibResults([]);
      } finally {
        setLibSearching(false);
      }
    }, 500);

    return () => clearTimeout(timer);
  }, [libKeyword]);

  const handleSelectLib = (lib) => {
    if (activeSlot === "main") setMainLib(lib);
    if (activeSlot === "sub")  setSubLib(lib);
    setLibKeyword("");
    setLibResults([]);
    setActiveSlot(null);
  };

  const handleSubmit = async () => {
    const error = validateNickname(nickname);
    if (error) { setNicknameError(error); return; }
    if (!gender)   { alert("성별을 선택해주세요."); return; }
    if (!ageGroup) { alert("연령대를 선택해주세요."); return; }
    if (!mainLib)  { alert("메인 도서관을 선택해주세요."); return; }

    setSubmitting(true);
    setSubmitError("");

    try {
      // 1. 닉네임/성별/연령대 저장
      const infoRes = await fetch("/api/users/info", {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ nickname: nickname.trim(), gender, ageGroup }),
      });
      if (!infoRes.ok) throw new Error("사용자 정보 저장 실패");

      // 2. 도서관 저장
      const libRes = await fetch("/api/users/me/libraries", {
        method: "PATCH",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          mainLibraryCode: mainLib.libraryCode,
          subLibraryCode:  subLib?.libraryCode ?? null,
        }),
      });
      if (!libRes.ok) throw new Error("도서관 저장 실패");

      // App.jsx의 needsProfile을 false로 바꾸고 메인으로 이동
      if (onComplete) onComplete();
      navigate("/main");
    } catch (e) {
      setSubmitError(e.message || "저장 중 오류가 발생했어요. 다시 시도해주세요.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div style={S.wrap}>
      <div style={S.bgDecor} aria-hidden="true">
        <div style={{ ...S.blob, top: -96, right: -96, background: "#fbcfe8" }} />
        <div style={{ ...S.blob, bottom: -96, left: -96, background: "#e9d5ff" }} />
      </div>

      <div style={S.inner}>

        {/* 헤더 */}
        <div style={S.header}>
          <div style={S.headerIcon}>👤</div>
          <p style={S.headerLabel}>프로필 설정</p>
          <h1 style={S.headerTitle}>나를 소개해주세요</h1>
          <p style={S.headerDesc}>정보는 맞춤 도서 추천 및 재고 조회에 활용됩니다.</p>
        </div>

        {/* 진행 단계 (4단계) */}
        <div style={S.stepRow}>
          {STEPS.map((label, i) => (
            <div key={label} style={S.stepItem}>
              <div style={{
                ...S.stepDot,
                background: stepDone[i]
                  ? `linear-gradient(135deg, ${C.pink}, ${C.purple})`
                  : C.gray200,
              }}>
                {stepDone[i] ? "✓" : i + 1}
              </div>
              <span style={{ fontSize: 10, color: stepDone[i] ? C.pinkDark : C.gray400, fontWeight: stepDone[i] ? 700 : 400 }}>
                {label}
              </span>
            </div>
          ))}
        </div>

        {/* 1. 닉네임 */}
        <div style={S.fieldWrap}>
          <label style={S.fieldLabel}>닉네임</label>
          <div style={S.inputWrap}>
            <input
              value={nickname}
              onChange={(e) => { setNickname(e.target.value); setNicknameError(validateNickname(e.target.value)); }}
              placeholder="2~10자, 특수문자 제외"
              maxLength={10}
              style={{ ...S.input, borderColor: nicknameError ? C.red : nickname && !nicknameError ? C.pink : C.gray200 }}
            />
            <span style={S.inputCount}>{nickname.length}/10</span>
          </div>
          {nicknameError && <p style={S.errorMsg}>{nicknameError}</p>}
          {nickname && !nicknameError && <p style={S.successMsg}>✓ 사용 가능한 닉네임입니다.</p>}
        </div>

        {/* 2. 성별 */}
        <div style={S.fieldWrap}>
          <label style={S.fieldLabel}>성별</label>
          <div style={S.genderGrid}>
            {GENDER_OPTIONS.map(({ value, label, emoji }) => {
              const selected = gender === value;
              return (
                <button key={value} onClick={() => setGender(value)} style={{
                  ...S.genderBtn,
                  background: selected ? `linear-gradient(135deg, ${C.pinkLight}, ${C.purpleLight})` : C.white,
                  border:     `1.5px solid ${selected ? C.pink : C.gray200}`,
                  color:      selected ? C.pinkDark : C.gray700,
                  fontWeight: selected ? 800 : 400,
                  boxShadow:  selected ? `0 4px 12px rgba(244,114,182,0.2)` : "none",
                }}>
                  <span style={{ fontSize: 22 }}>{emoji}</span>
                  <span style={{ fontSize: 13 }}>{label}</span>
                </button>
              );
            })}
          </div>
        </div>

        {/* 3. 연령대 */}
        <div style={S.fieldWrap}>
          <label style={S.fieldLabel}>연령대</label>
          <div style={S.ageGrid}>
            {AGE_OPTIONS.map(({ value, label }) => {
              const selected = ageGroup === value;
              return (
                <button key={value} onClick={() => setAgeGroup(value)} style={{
                  ...S.ageBtn,
                  background: selected ? `linear-gradient(135deg, ${C.pink}, ${C.purple})` : C.white,
                  border:     `1.5px solid ${selected ? C.pink : C.gray200}`,
                  color:      selected ? C.white : C.gray700,
                  fontWeight: selected ? 800 : 400,
                  boxShadow:  selected ? `0 4px 12px rgba(244,114,182,0.3)` : "none",
                }}>
                  {label}
                </button>
              );
            })}
          </div>
        </div>

        {/* 4. 도서관 */}
        <div style={S.fieldWrap}>
          <label style={S.fieldLabel}>
            내 도서관
            <span style={{ fontSize: 11, color: C.gray400, fontWeight: 400, marginLeft: 6 }}>
              (재고 조회에 사용됩니다)
            </span>
          </label>

          <div style={{ display: "flex", flexDirection: "column", gap: 10, marginBottom: 12 }}>
            {[
              { key: "main", label: "메인 도서관", value: mainLib, required: true  },
              { key: "sub",  label: "서브 도서관", value: subLib,  required: false },
            ].map(({ key, label, value, required }) => (
              <div key={key}>
                <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 6 }}>
                  <span style={{ fontSize: 12, fontWeight: 700, color: C.gray700 }}>
                    {label}{required && <span style={{ color: C.pink }}> *</span>}
                  </span>
                  {value && (
                    <button style={{ fontSize: 11, color: C.gray400, background: "none", border: "none", cursor: "pointer" }}
                      onClick={() => key === "main" ? setMainLib(null) : setSubLib(null)}>
                      ✕
                    </button>
                  )}
                </div>

                {value ? (
                  <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", background: C.pinkLight, borderRadius: 12, padding: "10px 14px", border: `1.5px solid ${C.pink}` }}>
                    <span style={{ fontSize: 13, fontWeight: 700, color: C.pinkDark }}>{value.name}</span>
                    <span style={{ fontSize: 11, color: C.pink }}>{value.libraryCode}</span>
                  </div>
                ) : (
                  <button
                    style={{
                      width: "100%", padding: "10px 14px", textAlign: "left",
                      border: `1.5px dashed ${activeSlot === key ? C.pink : C.gray200}`,
                      borderRadius: 12, background: C.gray100, fontSize: 13, cursor: "pointer",
                      color: activeSlot === key ? C.pinkDark : C.gray400,
                      fontFamily: "'Noto Sans KR', sans-serif",
                    }}
                    onClick={() => setActiveSlot(activeSlot === key ? null : key)}
                  >
                    + 도서관 선택
                  </button>
                )}
              </div>
            ))}
          </div>

          {/* 검색창 */}
          {activeSlot && (
            <div style={{ marginBottom: 8 }}>
              <div style={S.inputWrap}>
                <input
                  style={{ ...S.input, borderColor: C.pink, paddingRight: 16 }}
                  type="text"
                  placeholder="도서관 이름 검색 (예: 노원)"
                  value={libKeyword}
                  onChange={(e) => setLibKeyword(e.target.value)}
                  autoFocus
                />
              </div>
              {libSearching && (
                <p style={{ fontSize: 12, color: C.gray400, textAlign: "center", margin: "6px 0" }}>검색 중...</p>
              )}
              {!libSearching && libResults.length > 0 && (
                <div style={{ border: `1px solid ${C.gray200}`, borderRadius: 12, overflow: "hidden", maxHeight: 180, overflowY: "auto", marginTop: 6 }}>
                  {libResults.map((lib) => (
                    <button key={lib.libraryCode}
                      style={{ width: "100%", padding: "10px 14px", textAlign: "left", background: C.white, border: "none", borderBottom: `1px solid ${C.gray100}`, cursor: "pointer", display: "flex", flexDirection: "column", gap: 2, fontFamily: "'Noto Sans KR', sans-serif" }}
                      onClick={() => handleSelectLib(lib)}>
                      <span style={{ fontSize: 13, fontWeight: 700, color: C.gray800 }}>{lib.name}</span>
                      <span style={{ fontSize: 11, color: C.gray400 }}>{lib.address}</span>
                    </button>
                  ))}
                </div>
              )}
              {!libSearching && libKeyword && libResults.length === 0 && (
                <p style={{ fontSize: 12, color: C.gray400, textAlign: "center", margin: "6px 0" }}>검색 결과가 없어요.</p>
              )}
            </div>
          )}
        </div>

        {/* 에러 메시지 */}
        {submitError && (
          <p style={{ ...S.errorMsg, textAlign: "center", marginBottom: 12 }}>{submitError}</p>
        )}

        {/* 완료 버튼 */}
        <button
          onClick={handleSubmit}
          disabled={!isComplete || submitting}
          style={{
            ...S.submitBtn,
            background:  isComplete && !submitting ? `linear-gradient(135deg, ${C.pink}, ${C.purple})` : C.gray100,
            color:       isComplete && !submitting ? C.white : C.gray400,
            boxShadow:   isComplete && !submitting ? "0 8px 24px rgba(244,114,182,0.4)" : "none",
            cursor:      isComplete && !submitting ? "pointer" : "not-allowed",
          }}
        >
          {submitting ? "저장 중..." : isComplete ? "시작하기 →" : "모든 항목을 입력해주세요"}
        </button>

      </div>
    </div>
  );
}

const S = {
  wrap:    { minHeight: "100vh", background: `linear-gradient(135deg, ${C.pinkBg}, ${C.white}, #faf5ff)`, fontFamily: "'Noto Sans KR', sans-serif", position: "relative", overflow: "hidden" },
  bgDecor: { position: "fixed", inset: 0, pointerEvents: "none", overflow: "hidden", zIndex: 0 },
  blob:    { position: "absolute", width: 384, height: 384, borderRadius: "50%", opacity: 0.2, filter: "blur(60px)" },
  inner:   { position: "relative", zIndex: 1, maxWidth: 480, margin: "0 auto", padding: "48px 20px 80px" },

  header:      { marginBottom: 24 },
  headerIcon:  { fontSize: 36, marginBottom: 10 },
  headerLabel: { fontSize: 11, fontWeight: 700, letterSpacing: "0.15em", textTransform: "uppercase", color: C.pink, margin: "0 0 6px" },
  headerTitle: { fontFamily: "'Playfair Display', serif", fontSize: 26, fontWeight: 800, color: C.gray800, margin: "0 0 6px" },
  headerDesc:  { fontSize: 13, color: C.gray500, margin: 0 },

  stepRow:  { display: "flex", gap: 12, alignItems: "center", marginBottom: 28 },
  stepItem: { display: "flex", flexDirection: "column", alignItems: "center", gap: 4 },
  stepDot:  { width: 28, height: 28, borderRadius: 10, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 11, fontWeight: 800, color: C.white, transition: "all 0.2s" },

  fieldWrap:  { marginBottom: 24 },
  fieldLabel: { display: "block", fontSize: 13, fontWeight: 800, color: C.gray800, marginBottom: 10 },

  inputWrap:  { position: "relative" },
  input: {
    width: "100%", padding: "13px 48px 13px 16px",
    border: "1.5px solid", borderRadius: 14,
    fontSize: 14, color: C.gray800, outline: "none",
    background: C.white, fontFamily: "'Noto Sans KR', sans-serif",
    transition: "border-color 0.2s", boxSizing: "border-box",
  },
  inputCount: { position: "absolute", right: 14, top: "50%", transform: "translateY(-50%)", fontSize: 11, color: C.gray400 },
  errorMsg:   { fontSize: 11, color: C.red, margin: "6px 0 0", fontWeight: 600 },
  successMsg: { fontSize: 11, color: "#16a34a", margin: "6px 0 0", fontWeight: 600 },

  genderGrid: { display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 10 },
  genderBtn:  { display: "flex", flexDirection: "column", alignItems: "center", gap: 6, padding: "14px 0", borderRadius: 16, cursor: "pointer", fontFamily: "'Noto Sans KR', sans-serif", transition: "all 0.2s" },

  ageGrid: { display: "grid", gridTemplateColumns: "1fr 1fr", gap: 10 },
  ageBtn:  { padding: "12px 0", borderRadius: 14, cursor: "pointer", fontFamily: "'Noto Sans KR', sans-serif", fontSize: 13, transition: "all 0.2s" },

  submitBtn: { width: "100%", padding: "16px 0", borderRadius: 18, border: "none", fontSize: 15, fontWeight: 800, fontFamily: "'Noto Sans KR', sans-serif", transition: "all 0.2s", marginTop: 8 },
};
