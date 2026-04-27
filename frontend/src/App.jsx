import { BrowserRouter, Routes, Route, useNavigate, Navigate } from "react-router-dom";
import { useState, useEffect } from "react";

import MainPage from "./pages/MainPage";
import SurveyPage from "./pages/SurveyPage";
import LoadingPage from "./pages/LoadingPage";
import PersonaResultPage from "./pages/PersonaResultPage";
import BookLoadingPage from "./pages/BookLoadingPage";
import BookResultPage from "./pages/BookResultPage";
import LoginPage from "./pages/LoginPage";
import UserInfoPage from "./pages/UserInfoPage";
import MyPage from "./pages/MyPage";
import RankingPage from "./pages/RankingPage";
import SearchPage from "./pages/SearchPage";

/**
 * App.jsx — 라우팅 설정
 *
 * state 흐름:
 *  surveyAnswers → LoadingPage → POST /api/surveys/submit
 *  personaCode   → PersonaResultPage (서브 페르소나 코드, 예: TREND_SURFER)
 *  personaName   → PersonaResultPage, BookLoadingPage, BookResultPage
 *  scores        → PersonaResultPage (Radar Chart용 6대 지표 점수)
 *                  { 지적_확장성: 8.5, 분석적_깊이: 4.0, ... }
 *
 * 인증 흐름:
 *  마운트 시 POST /api/auth/refresh 호출
 *  → 200: 로그인 상태 → 정상 라우팅
 *  → 401: 미로그인   → /login 리다이렉트
 *
 * 라우트 목록:
 *  /              → MainPage
 *  /login         → LoginPage
 *  /user-info     → UserInfoPage
 *  /survey        → SurveyPage
 *  /loading       → LoadingPage  (POST /api/surveys/submit 호출)
 *  /result        → PersonaResultPage
 *  /book-loading  → BookLoadingPage
 *  /books         → BookResultPage
 *  /ranking       → RankingPage
 *  /search        → SearchPage
 *  /mypage        → MyPage
 */
export default function App() {
    const [surveyAnswers, setSurveyAnswers] = useState({});
    const [personaCode,   setPersonaCode]   = useState("EXPLORER");
    const [personaName,   setPersonaName]   = useState("지적 탐험가");
    // scores는 LoadingPage → navigate("/result", { state: { scores } }) 로 전달
    // PersonaResultPage에서 useLocation().state?.scores 로 수신하므로 App state 불필요

    // ── 인증 상태 ──────────────────────────────────────────────────────
    const [authChecked, setAuthChecked] = useState(false); // 인증 확인 완료 여부
    const [isLoggedIn,  setIsLoggedIn]  = useState(false); // 로그인 상태 여부

    /**
     * 마운트 시 인증 상태 확인
     * POST /api/auth/refresh 호출:
     *  - 200: accessToken 쿠키 갱신 성공 → 로그인 상태
     *  - 401: refreshToken 없거나 만료   → 미로그인 → /login 리다이렉트
     */
    useEffect(() => {
        const checkAuth = async () => {
            try {
                const res = await fetch("/api/auth/refresh", {
                    method: "POST",
                    credentials: "include", // httpOnly 쿠키 자동 포함
                });
                setIsLoggedIn(res.ok);
            } catch {
                // 네트워크 오류 등 예외 → 미로그인으로 처리
                setIsLoggedIn(false);
            } finally {
                setAuthChecked(true);
            }
        };
        checkAuth();
    }, []);

    // ── 인증 확인 전 — 로딩 스피너 표시 ──────────────────────────────
    if (!authChecked) {
        return (
            <div style={S.loadingWrap}>
                <div style={S.spinner} />
                <style>{`
                    @keyframes spin-cw {
                        from { transform: rotate(0deg); }
                        to   { transform: rotate(360deg); }
                    }
                `}</style>
            </div>
        );
    }

    return (
        <BrowserRouter>
            <Routes>
                {/* 인증 — 항상 접근 가능 */}
                <Route path="/login"     element={<LoginPage />} />
                <Route path="/user-info" element={<UserInfoPage />} />

                {/* 보호 라우트 — 미로그인 시 /login으로 리다이렉트 */}
                <Route path="/"
                       element={isLoggedIn ? <Main /> : <Navigate to="/login" replace />}
                />
                <Route path="/survey"
                       element={isLoggedIn
                           ? <Survey setSurveyAnswers={setSurveyAnswers} />
                           : <Navigate to="/login" replace />}
                />
                <Route path="/loading"
                       element={isLoggedIn
                           ? <Loading
                               surveyAnswers={surveyAnswers}
                               setPersonaCode={setPersonaCode}
                               setPersonaName={setPersonaName}
                           />
                           : <Navigate to="/login" replace />}
                />
                <Route path="/result"
                       element={isLoggedIn
                           ? <Result personaCode={personaCode} />
                           : <Navigate to="/login" replace />}
                />
                <Route path="/book-loading"
                       element={isLoggedIn
                           ? <BookLoading personaName={personaName} />
                           : <Navigate to="/login" replace />}
                />
                <Route path="/books"
                       element={isLoggedIn
                           ? <Books personaName={personaName} />
                           : <Navigate to="/login" replace />}
                />
                <Route path="/ranking"
                       element={isLoggedIn ? <RankingPage /> : <Navigate to="/login" replace />}
                />
                <Route path="/search"
                       element={isLoggedIn ? <SearchPage />  : <Navigate to="/login" replace />}
                />
                <Route path="/mypage"
                       element={isLoggedIn ? <MyPage />      : <Navigate to="/login" replace />}
                />
            </Routes>
        </BrowserRouter>
    );
}

/* ────────────────────────────────────────
   래퍼 함수
   ──────────────────────────────────────── */
function Main() {
    const navigate = useNavigate();
    return <MainPage onStart={() => navigate("/survey")} />;
}

function Survey({ setSurveyAnswers }) {
    const navigate = useNavigate();
    return (
        <SurveyPage
            onSubmit={(answers) => {
                setSurveyAnswers(answers);
                navigate("/loading");
            }}
        />
    );
}

/**
 * Loading 래퍼
 * LoadingPage에서 POST /api/surveys/submit 응답 수신 후
 * onComplete(code, name, scores) 형태로 호출
 * scores는 navigate state로 PersonaResultPage에 전달
 * → PersonaResultPage에서 useLocation().state?.scores 로 수신
 */
function Loading({ surveyAnswers, setPersonaCode, setPersonaName }) {
    const navigate = useNavigate();
    return (
        <LoadingPage
            surveyAnswers={surveyAnswers}
            onComplete={(code, name, scores) => {
                setPersonaCode(code || "EXPLORER");
                setPersonaName(name || "지적 탐험가");
                navigate("/result", { state: { scores } });
            }}
        />
    );
}

function Result({ personaCode }) {
    const navigate = useNavigate();
    return (
        <PersonaResultPage
            personaCode={personaCode}
            onViewBooks={() => navigate("/book-loading")}
        />
    );
}

function BookLoading({ personaName }) {
    const navigate = useNavigate();
    return (
        <BookLoadingPage
            personaName={personaName}
            onComplete={() => navigate("/books")}
        />
    );
}

function Books({ personaName }) {
    return <BookResultPage personaName={personaName} />;
}

/* ────────────────────────────────────────
   인증 확인 중 로딩 스피너 스타일
   ──────────────────────────────────────── */
const S = {
    loadingWrap: {
        minHeight: "100vh",
        background: "linear-gradient(135deg, #fdf2f8, #ffffff, #faf5ff)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
    },
    spinner: {
        width: 40,
        height: 40,
        borderRadius: "50%",
        border: "3px solid #fce7f3",
        borderTopColor: "#f472b6",
        animation: "spin-cw 1s linear infinite",
    },
};