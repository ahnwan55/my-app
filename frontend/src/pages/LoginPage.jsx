/**
 * LoginPage.jsx — 카카오 로그인 페이지
 *
 * 카카오 로그인 흐름:
 *  1. "카카오로 시작하기" 버튼 클릭
 *  2. 백엔드 OAuth2 엔드포인트로 리다이렉트
 *     → http://localhost:8080/oauth2/authorization/kakao
 *  3. 카카오 로그인 완료
 *  4. 백엔드가 JWT를 httpOnly 쿠키로 발급 후
 *     http://localhost:3000/ 으로 리다이렉트
 *
 * 쿠키 방식이므로 이후 모든 API 호출에
 * credentials: 'include' 옵션 필요
 *
 * 컬러: 벚꽃 핑크 × 카카오 옐로우(#FEE500)
 */

const C = {
  pink:        "#f472b6",
  pinkDark:    "#ec4899",
  pinkLight:   "#fce7f3",
  pinkBg:      "#fdf2f8",
  purple:      "#a855f7",
  white:       "#ffffff",
  gray400:     "#9ca3af",
  gray500:     "#6b7280",
  gray700:     "#374151",
  gray800:     "#1f2937",
  kakaoYellow: "#FEE500",   // 카카오 공식 브랜드 컬러
  kakaoText:   "#191919",   // 카카오 공식 텍스트 컬러
};

/**
 * handleKakaoLogin
 *
 * 백엔드 Spring Security OAuth2 엔드포인트로 직접 리다이렉트.
 * Vite 프록시를 거치지 않고 8080으로 직접 이동해야
 * 카카오 → 백엔드 → 프론트 쿠키 흐름이 정상 동작함.
 *
 * 로그인 완료 후 백엔드가 accessToken, refreshToken을
 * httpOnly 쿠키로 발급하고 http://localhost:3000/ 으로 리다이렉트.
 */
function handleKakaoLogin() {
  window.location.href = "http://localhost:8080/oauth2/authorization/kakao";
}

export default function LoginPage() {
  return (
    <div style={S.wrap}>

      {/* 배경 블러 오브 */}
      <div style={S.bgDecor} aria-hidden="true">
        <div style={{ ...S.blob, top: -96, right: -96, background: "#fbcfe8" }} />
        <div style={{ ...S.blob, bottom: -96, left: -96, background: "#e9d5ff" }} />
      </div>

      <div style={S.inner}>

        {/* 로고 */}
        <div style={S.logoWrap}>
          <div style={S.logoIcon}>📚</div>
          <h1 style={S.logoText}>LibraryHub</h1>
          <p style={S.logoSub}>독서 페르소나 기반 도서 추천 플랫폼</p>
        </div>

        {/* 일러스트 영역 */}
        <div style={S.illustWrap} aria-hidden="true">
          <div style={S.illustCircle}>
            <span style={{ fontSize: 64 }}>📖</span>
          </div>
          {/* 플로팅 뱃지 */}
          <div style={{ ...S.floatBadge, top: 8, right: 24 }}>🔭 탐험가</div>
          <div style={{ ...S.floatBadge, bottom: 16, left: 16, background: C.purple, boxShadow: "0 4px 12px rgba(168,85,247,0.3)" }}>🌊 다이버</div>
        </div>

        {/* 소개 문구 */}
        <div style={S.descWrap}>
          <h2 style={S.descTitle}>나의 독서 유형을 발견하세요</h2>
          <p style={S.descText}>
            설문 응답 하나로 12가지 독서 페르소나 중<br />
            나에게 꼭 맞는 유형과 도서를 추천받아요.
          </p>
        </div>

        {/* 카카오 로그인 버튼 */}
        <button onClick={handleKakaoLogin} style={S.kakaoBtn}>
          {/* 카카오 공식 로고 심볼 */}
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none" aria-hidden="true">
            <path
              d="M10 2C5.58 2 2 4.91 2 8.5c0 2.28 1.52 4.28 3.82 5.44l-.97 3.53a.25.25 0 0 0 .38.27L9.5 15.1c.16.01.33.02.5.02 4.42 0 8-2.91 8-6.5S14.42 2 10 2z"
              fill={C.kakaoText}
            />
          </svg>
          카카오로 시작하기
        </button>

        {/* 이용약관 안내 */}
        <p style={S.terms}>
          시작하면 <span style={{ color: C.pinkDark, fontWeight: 700 }}>이용약관</span> 및{" "}
          <span style={{ color: C.pinkDark, fontWeight: 700 }}>개인정보처리방침</span>에 동의한 것으로 간주합니다.
        </p>

      </div>
    </div>
  );
}

const S = {
  wrap:    { minHeight: "100vh", background: `linear-gradient(135deg, ${C.pinkBg}, ${C.white}, #faf5ff)`, fontFamily: "'Noto Sans KR', sans-serif", position: "relative", overflow: "hidden", display: "flex", alignItems: "center", justifyContent: "center" },
  bgDecor: { position: "fixed", inset: 0, pointerEvents: "none", overflow: "hidden", zIndex: 0 },
  blob:    { position: "absolute", width: 384, height: 384, borderRadius: "50%", opacity: 0.2, filter: "blur(60px)" },
  inner:   { position: "relative", zIndex: 1, maxWidth: 400, width: "100%", padding: "48px 24px 64px", display: "flex", flexDirection: "column", alignItems: "center" },

  /* 로고 */
  logoWrap: { display: "flex", flexDirection: "column", alignItems: "center", marginBottom: 32 },
  logoIcon: {
    width: 56, height: 56, borderRadius: 20,
    background: `linear-gradient(135deg, ${C.pink}, ${C.purple})`,
    display: "flex", alignItems: "center", justifyContent: "center",
    fontSize: 28, boxShadow: "0 8px 24px rgba(244,114,182,0.35)",
    marginBottom: 12,
  },
  logoText: { fontFamily: "'Playfair Display', serif", fontSize: 26, fontWeight: 800, color: C.gray800, margin: "0 0 6px" },
  logoSub:  { fontSize: 12, color: C.gray500, margin: 0 },

  /* 일러스트 */
  illustWrap: { position: "relative", width: 180, height: 180, marginBottom: 28 },
  illustCircle: {
    width: 180, height: 180, borderRadius: "50%",
    background: `linear-gradient(135deg, ${C.pinkLight}, #ede9fe)`,
    border: `2px solid ${C.pinkLight}`,
    display: "flex", alignItems: "center", justifyContent: "center",
  },
  floatBadge: {
    position: "absolute",
    background: C.pinkDark,
    color: C.white, fontSize: 11, fontWeight: 800,
    padding: "5px 12px", borderRadius: 20,
    boxShadow: "0 4px 12px rgba(236,72,153,0.3)",
    whiteSpace: "nowrap",
  },

  /* 소개 문구 */
  descWrap:  { textAlign: "center", marginBottom: 32 },
  descTitle: { fontFamily: "'Playfair Display', serif", fontSize: 20, fontWeight: 800, color: C.gray800, margin: "0 0 10px" },
  descText:  { fontSize: 13, color: C.gray500, lineHeight: 1.8, margin: 0 },

  /* 카카오 버튼 */
  kakaoBtn: {
    width: "100%", display: "flex", alignItems: "center", justifyContent: "center", gap: 10,
    padding: "15px 0", borderRadius: 18, border: "none", cursor: "pointer",
    background: C.kakaoYellow,
    color: C.kakaoText, fontSize: 15, fontWeight: 800,
    boxShadow: "0 4px 16px rgba(254,229,0,0.4)",
    fontFamily: "'Noto Sans KR', sans-serif",
    transition: "all 0.2s",
    marginBottom: 16,
  },

  /* 이용약관 */
  terms: { fontSize: 11, color: C.gray400, textAlign: "center", lineHeight: 1.6, margin: 0 },
};
