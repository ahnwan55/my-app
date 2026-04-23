import { useNavigate } from "react-router-dom";
import { GoogleOAuthProvider, GoogleLogin } from "@react-oauth/google";

// Google OAuth Client ID는 .env 파일에서 가져옴
// .env 파일에 VITE_GOOGLE_CLIENT_ID=your_client_id 추가 필요
const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID;

export default function LoginPage() {
    const navigate = useNavigate();

    // 구글 로그인 성공 시 처리
    // credentialResponse: 구글에서 반환한 JWT 토큰 포함 응답
    const handleLoginSuccess = (credentialResponse) => {
        console.log("로그인 성공:", credentialResponse);
        // TODO: 백엔드 API로 토큰 전달 후 JWT 발급 받기
        // 현재는 바로 사용자 정보 입력 페이지로 이동
        navigate("/user-info");
    };

    // 구글 로그인 실패 시 처리
    const handleLoginError = () => {
        console.error("로그인 실패");
        alert("로그인에 실패했습니다. 다시 시도해주세요.");
    };

    return (
        <GoogleOAuthProvider clientId={GOOGLE_CLIENT_ID}>
            <div style={styles.container}>
                {/* 배경 그라디언트 */}
                <div style={styles.bgTexture} />

                <div style={styles.inner}>

                    {/* 로고 영역 */}
                    <div style={styles.logoArea}>
                        <div style={styles.logoIcon}>📚</div>
                        <div style={styles.logoName}>BookPersona</div>
                        <div style={styles.logoSub}>
                            나의 독서 성향을 발견하고<br />비슷한 독자들과 연결되세요
                        </div>
                    </div>

                    {/* 로그인 카드 */}
                    <div style={styles.card}>
                        <div style={styles.cardTitle}>시작하기</div>
                        <div style={styles.cardDesc}>
                            소셜 계정으로 간편하게 로그인하고<br />나만의 독서 페르소나를 찾아보세요
                        </div>

                        <div style={styles.divider}>
                            <div style={styles.dividerLine} />
                            <div style={styles.dividerText}>소셜 로그인</div>
                            <div style={styles.dividerLine} />
                        </div>

                        {/* 구글 로그인 버튼 */}
                        <div style={styles.googleBtnWrap}>
                            <GoogleLogin
                                onSuccess={handleLoginSuccess}
                                onError={handleLoginError}
                                text="continue_with"
                                shape="rectangular"
                                logo_alignment="left"
                                width="100%"
                            />
                        </div>

                        <div style={styles.notice}>
                            로그인 시 <span style={styles.noticeLink}>서비스 이용약관</span> 및{" "}
                            <span style={styles.noticeLink}>개인정보 처리방침</span>에<br />
                            동의하는 것으로 간주됩니다.
                        </div>
                    </div>

                    {/* 서비스 핵심 기능 3가지 */}
                    <div style={styles.features}>
                        <div style={styles.featureItem}>
                            <div style={styles.featureIcon}>🧭</div>
                            <div style={styles.featureLabel}>AI 페르소나<br />분석</div>
                        </div>
                        <div style={styles.featureItem}>
                            <div style={styles.featureIcon}>📖</div>
                            <div style={styles.featureLabel}>맞춤 도서<br />추천</div>
                        </div>
                        <div style={styles.featureItem}>
                            <div style={styles.featureIcon}>💬</div>
                            <div style={styles.featureLabel}>독서 커뮤니티<br />채팅</div>
                        </div>
                    </div>

                    <div style={styles.footer}>
                        BookPersona · 독서 페르소나 기반 도서 추천 서비스
                    </div>

                </div>
            </div>
        </GoogleOAuthProvider>
    );
}

// 인라인 스타일 정의
// 추후 Tailwind 클래스로 교체 가능
const styles = {
    container: {
        minHeight: "100vh",
        background: "#F5F0E8",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        position: "relative",
        overflow: "hidden",
    },
    bgTexture: {
        position: "fixed",
        inset: 0,
        background: `
      radial-gradient(ellipse at 20% 20%, rgba(200,134,42,0.08) 0%, transparent 50%),
      radial-gradient(ellipse at 80% 80%, rgba(28,43,74,0.06) 0%, transparent 50%)
    `,
        pointerEvents: "none",
        zIndex: 0,
    },
    inner: {
        position: "relative",
        zIndex: 1,
        width: "100%",
        maxWidth: "420px",
        padding: "24px",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
    },
    logoArea: {
        textAlign: "center",
        marginBottom: "48px",
    },
    logoIcon: {
        width: "64px",
        height: "64px",
        background: "#1C2B4A",
        borderRadius: "18px",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        fontSize: "28px",
        margin: "0 auto 16px",
        boxShadow: "0 8px 24px rgba(28,43,74,0.2)",
    },
    logoName: {
        fontFamily: "'Noto Serif KR', serif",
        fontSize: "24px",
        fontWeight: "700",
        color: "#1C2B4A",
        marginBottom: "6px",
    },
    logoSub: {
        fontSize: "13px",
        color: "#5A6478",
        fontWeight: "300",
        lineHeight: "1.6",
    },
    card: {
        width: "100%",
        background: "rgba(255,255,255,0.75)",
        backdropFilter: "blur(16px)",
        border: "1px solid rgba(28,43,74,0.1)",
        borderRadius: "24px",
        padding: "36px 32px",
        marginBottom: "20px",
    },
    cardTitle: {
        fontFamily: "'Noto Serif KR', serif",
        fontSize: "18px",
        fontWeight: "700",
        color: "#1C2B4A",
        textAlign: "center",
        marginBottom: "6px",
    },
    cardDesc: {
        fontSize: "13px",
        color: "#5A6478",
        textAlign: "center",
        fontWeight: "300",
        marginBottom: "28px",
        lineHeight: "1.6",
    },
    divider: {
        display: "flex",
        alignItems: "center",
        gap: "12px",
        marginBottom: "20px",
    },
    dividerLine: {
        flex: 1,
        height: "1px",
        background: "rgba(28,43,74,0.1)",
    },
    dividerText: {
        fontSize: "11px",
        color: "#8A95A8",
        letterSpacing: "0.1em",
        whiteSpace: "nowrap",
    },
    googleBtnWrap: {
        marginBottom: "16px",
        display: "flex",
        justifyContent: "center",
    },
    notice: {
        fontSize: "11px",
        color: "#8A95A8",
        textAlign: "center",
        lineHeight: "1.6",
    },
    noticeLink: {
        color: "#C8862A",
        cursor: "pointer",
    },
    features: {
        width: "100%",
        display: "grid",
        gridTemplateColumns: "1fr 1fr 1fr",
        gap: "12px",
        marginBottom: "20px",
    },
    featureItem: {
        background: "rgba(255,255,255,0.5)",
        border: "1px solid rgba(28,43,74,0.1)",
        borderRadius: "16px",
        padding: "16px 12px",
        textAlign: "center",
    },
    featureIcon: {
        fontSize: "22px",
        marginBottom: "8px",
    },
    featureLabel: {
        fontSize: "11px",
        color: "#5A6478",
        lineHeight: "1.4",
    },
    footer: {
        fontSize: "11px",
        color: "#8A95A8",
        textAlign: "center",
    },
};