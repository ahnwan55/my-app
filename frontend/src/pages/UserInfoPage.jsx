import { useState } from "react";
import { useNavigate } from "react-router-dom";

/**
 * UserInfoPage.jsx — 사용자 정보 입력 페이지
 *
 * 카카오 로그인 완료 후 최초 1회 진입.
 * 닉네임 / 성별 / 연령대를 입력받아 서버에 저장한다.
 *
 * 수집 항목:
 *  - 닉네임   {string}  — 2~10자, 특수문자 제외
 *  - 성별     {string}  — "male" | "female" | "none"
 *  - 연령대   {string}  — "elementary" | "middle" | "high" |
 *                         "20s" | "30s" | "40s" | "50s+"
 *
 * 이후 연동:
 *  - POST /api/users/info { nickname, gender, ageGroup }
 *  - 저장 완료 후 navigate("/") 로 이동
 *
 * 현재: 유효성 검사 후 console.log + navigate("/")
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
  gray100:     "#f3f4f6",
  gray200:     "#e5e7eb",
  gray400:     "#9ca3af",
  gray500:     "#6b7280",
  gray700:     "#374151",
  gray800:     "#1f2937",
  red:         "#ef4444",
};

/* ────────────────────────────────────────
   선택지 데이터
   ──────────────────────────────────────── */
const GENDER_OPTIONS = [
  { value: "male",   label: "남성",    emoji: "👨" },
  { value: "female", label: "여성",    emoji: "👩" },
  { value: "none",   label: "선택 안함", emoji: "🙂" },
];

const AGE_OPTIONS = [
  { value: "elementary", label: "초등학생"   },
  { value: "middle",     label: "중학생"     },
  { value: "high",       label: "고등학생"   },
  { value: "20s",        label: "20대"       },
  { value: "30s",        label: "30대"       },
  { value: "40s",        label: "40대"       },
  { value: "50s+",       label: "50대 이상"  },
];

/* ────────────────────────────────────────
   닉네임 유효성 검사
   ──────────────────────────────────────── */
function validateNickname(value) {
  if (!value.trim()) return "닉네임을 입력해주세요.";
  if (value.trim().length < 2) return "닉네임은 2자 이상이어야 합니다.";
  if (value.trim().length > 10) return "닉네임은 10자 이하여야 합니다.";
  if (/[^가-힣a-zA-Z0-9_]/.test(value.trim())) return "특수문자는 사용할 수 없습니다.";
  return "";
}

/* ────────────────────────────────────────
   UserInfoPage — 메인 컴포넌트
   ──────────────────────────────────────── */
export default function UserInfoPage() {
  const navigate = useNavigate();

  // 폼 상태
  const [nickname, setNickname] = useState("");
  const [gender,   setGender]   = useState("");
  const [ageGroup, setAgeGroup] = useState("");

  // 유효성 에러
  const [nicknameError, setNicknameError] = useState("");

  // 전체 완료 여부
  const isComplete = nickname.trim() && !nicknameError && gender && ageGroup;

  /* ── 닉네임 변경 핸들러 ── */
  const handleNicknameChange = (e) => {
    const val = e.target.value;
    setNickname(val);
    setNicknameError(validateNickname(val));
  };

  /* ── 제출 핸들러 ── */
  const handleSubmit = () => {
    const error = validateNickname(nickname);
    if (error) { setNicknameError(error); return; }
    if (!gender)   { alert("성별을 선택해주세요."); return; }
    if (!ageGroup) { alert("연령대를 선택해주세요."); return; }

    // TODO: POST /api/users/info { nickname: nickname.trim(), gender, ageGroup }
    console.log("사용자 정보 저장:", { nickname: nickname.trim(), gender, ageGroup });
    navigate("/");
  };

  return (
    <div style={S.wrap}>

      {/* 배경 블러 오브 */}
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
          <p style={S.headerDesc}>정보는 맞춤 도서 추천에 활용됩니다.</p>
        </div>

        {/* 진행 단계 표시 */}
        <div style={S.stepRow}>
          {["닉네임", "성별", "연령대"].map((label, i) => {
            const done =
              (i === 0 && nickname.trim() && !nicknameError) ||
              (i === 1 && gender) ||
              (i === 2 && ageGroup);
            return (
              <div key={label} style={S.stepItem}>
                <div style={{ ...S.stepDot, background: done ? `linear-gradient(135deg, ${C.pink}, ${C.purple})` : C.gray200 }}>
                  {done ? "✓" : i + 1}
                </div>
                <span style={{ fontSize: 10, color: done ? C.pinkDark : C.gray400, fontWeight: done ? 700 : 400 }}>{label}</span>
              </div>
            );
          })}
        </div>

        {/* ── 닉네임 입력 ── */}
        <div style={S.fieldWrap}>
          <label style={S.fieldLabel}>닉네임</label>
          <div style={S.inputWrap}>
            <input
              value={nickname}
              onChange={handleNicknameChange}
              placeholder="2~10자, 특수문자 제외"
              maxLength={10}
              style={{
                ...S.input,
                borderColor: nicknameError ? C.red : nickname && !nicknameError ? C.pink : C.gray200,
              }}
            />
            <span style={S.inputCount}>{nickname.length}/10</span>
          </div>
          {nicknameError && <p style={S.errorMsg}>{nicknameError}</p>}
          {nickname && !nicknameError && (
            <p style={S.successMsg}>✓ 사용 가능한 닉네임입니다.</p>
          )}
        </div>

        {/* ── 성별 선택 ── */}
        <div style={S.fieldWrap}>
          <label style={S.fieldLabel}>성별</label>
          <div style={S.genderGrid}>
            {GENDER_OPTIONS.map(({ value, label, emoji }) => {
              const selected = gender === value;
              return (
                <button
                  key={value}
                  onClick={() => setGender(value)}
                  style={{
                    ...S.genderBtn,
                    background:   selected ? `linear-gradient(135deg, ${C.pinkLight}, ${C.purpleLight})` : C.white,
                    border:       `1.5px solid ${selected ? C.pink : C.gray200}`,
                    color:        selected ? C.pinkDark : C.gray700,
                    fontWeight:   selected ? 800 : 400,
                    boxShadow:    selected ? `0 4px 12px rgba(244,114,182,0.2)` : "none",
                  }}
                >
                  <span style={{ fontSize: 22 }}>{emoji}</span>
                  <span style={{ fontSize: 13 }}>{label}</span>
                </button>
              );
            })}
          </div>
        </div>

        {/* ── 연령대 선택 ── */}
        <div style={S.fieldWrap}>
          <label style={S.fieldLabel}>연령대</label>
          <div style={S.ageGrid}>
            {AGE_OPTIONS.map(({ value, label }) => {
              const selected = ageGroup === value;
              return (
                <button
                  key={value}
                  onClick={() => setAgeGroup(value)}
                  style={{
                    ...S.ageBtn,
                    background: selected ? `linear-gradient(135deg, ${C.pink}, ${C.purple})` : C.white,
                    border:     `1.5px solid ${selected ? C.pink : C.gray200}`,
                    color:      selected ? C.white : C.gray700,
                    fontWeight: selected ? 800 : 400,
                    boxShadow:  selected ? `0 4px 12px rgba(244,114,182,0.3)` : "none",
                  }}
                >
                  {label}
                </button>
              );
            })}
          </div>
        </div>

        {/* ── 완료 버튼 ── */}
        <button
          onClick={handleSubmit}
          disabled={!isComplete}
          style={{
            ...S.submitBtn,
            background:  isComplete ? `linear-gradient(135deg, ${C.pink}, ${C.purple})` : C.gray100,
            color:       isComplete ? C.white : C.gray400,
            boxShadow:   isComplete ? "0 8px 24px rgba(244,114,182,0.4)" : "none",
            cursor:      isComplete ? "pointer" : "not-allowed",
          }}
        >
          {isComplete ? "시작하기 →" : "모든 항목을 입력해주세요"}
        </button>

      </div>
    </div>
  );
}

/* ────────────────────────────────────────
   인라인 스타일
   ──────────────────────────────────────── */
const S = {
  wrap:    { minHeight: "100vh", background: `linear-gradient(135deg, ${C.pinkBg}, ${C.white}, #faf5ff)`, fontFamily: "'Noto Sans KR', sans-serif", position: "relative", overflow: "hidden" },
  bgDecor: { position: "fixed", inset: 0, pointerEvents: "none", overflow: "hidden", zIndex: 0 },
  blob:    { position: "absolute", width: 384, height: 384, borderRadius: "50%", opacity: 0.2, filter: "blur(60px)" },
  inner:   { position: "relative", zIndex: 1, maxWidth: 480, margin: "0 auto", padding: "48px 20px 80px" },

  /* 헤더 */
  header:      { marginBottom: 24 },
  headerIcon:  { fontSize: 36, marginBottom: 10 },
  headerLabel: { fontSize: 11, fontWeight: 700, letterSpacing: "0.15em", textTransform: "uppercase", color: C.pink, margin: "0 0 6px" },
  headerTitle: { fontFamily: "'Playfair Display', serif", fontSize: 26, fontWeight: 800, color: C.gray800, margin: "0 0 6px" },
  headerDesc:  { fontSize: 13, color: C.gray500, margin: 0 },

  /* 진행 단계 */
  stepRow:  { display: "flex", gap: 16, alignItems: "center", marginBottom: 28 },
  stepItem: { display: "flex", flexDirection: "column", alignItems: "center", gap: 4 },
  stepDot:  { width: 28, height: 28, borderRadius: 10, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 11, fontWeight: 800, color: C.white, transition: "all 0.2s" },

  /* 필드 공통 */
  fieldWrap:  { marginBottom: 24 },
  fieldLabel: { display: "block", fontSize: 13, fontWeight: 800, color: C.gray800, marginBottom: 10 },

  /* 닉네임 인풋 */
  inputWrap: { position: "relative" },
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

  /* 성별 버튼 */
  genderGrid: { display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 10 },
  genderBtn: {
    display: "flex", flexDirection: "column", alignItems: "center", gap: 6,
    padding: "14px 0", borderRadius: 16, cursor: "pointer",
    fontFamily: "'Noto Sans KR', sans-serif", transition: "all 0.2s",
  },

  /* 연령대 버튼 */
  ageGrid: { display: "grid", gridTemplateColumns: "1fr 1fr", gap: 10 },
  ageBtn: {
    padding: "12px 0", borderRadius: 14, cursor: "pointer",
    fontFamily: "'Noto Sans KR', sans-serif",
    fontSize: 13, transition: "all 0.2s",
  },

  /* 완료 버튼 */
  submitBtn: {
    width: "100%", padding: "16px 0", borderRadius: 18, border: "none",
    fontSize: 15, fontWeight: 800,
    fontFamily: "'Noto Sans KR', sans-serif",
    transition: "all 0.2s", marginTop: 8,
  },
};
