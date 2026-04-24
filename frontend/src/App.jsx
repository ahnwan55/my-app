import { BrowserRouter, Routes, Route, useNavigate } from "react-router-dom";
import { useState } from "react";

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

    return (
        <BrowserRouter>
            <Routes>
                {/* 인증 */}
                <Route path="/login"     element={<LoginPage />} />
                <Route path="/user-info" element={<UserInfoPage />} />

                {/* 메인 */}
                <Route path="/" element={<Main />} />

                {/* 페르소나 찾기 플로우 */}
                <Route path="/survey"
                    element={<Survey setSurveyAnswers={setSurveyAnswers} />}
                />
                <Route path="/loading"
                    element={
                        <Loading
                            surveyAnswers={surveyAnswers}
                            setPersonaCode={setPersonaCode}
                            setPersonaName={setPersonaName}
                        />
                    }
                />
                <Route path="/result"
                    element={<Result personaCode={personaCode} />}
                />
                <Route path="/book-loading" element={<BookLoading personaName={personaName} />} />
                <Route path="/books"        element={<Books personaName={personaName} />} />

                {/* 도서 서비스 */}
                <Route path="/ranking" element={<RankingPage />} />
                <Route path="/search"  element={<SearchPage />} />

                {/* 기타 */}
                <Route path="/mypage" element={<MyPage />} />
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
