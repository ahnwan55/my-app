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
import InventoryPage from "./pages/InventoryPage";
import BookDetailPage from "./pages/BookDetailPage";

export default function App() {
    const [surveyAnswers, setSurveyAnswers] = useState({});
    const [personaCode,   setPersonaCode]   = useState("EXPLORER");
    const [personaName,   setPersonaName]   = useState("지적 탐험가");

    const [authChecked,    setAuthChecked]    = useState(false);
    const [isLoggedIn,     setIsLoggedIn]     = useState(false);
    const [profileChecked, setProfileChecked] = useState(false);
    const [needsProfile,   setNeedsProfile]   = useState(false);

    useEffect(() => {
        const checkAuth = async () => {
            try {
                const res = await fetch("/api/auth/refresh", {
                    method: "POST",
                    credentials: "include",
                });

                if (res.ok) {
                    setIsLoggedIn(true);
                    try {
                        const meRes = await fetch("/api/users/me", {
                            credentials: "include",
                        });
                        if (meRes.ok) {
                            const me = await meRes.json();
                            setNeedsProfile(!me.gender);
                        } else {
                            setNeedsProfile(true);
                        }
                    } catch {
                        setNeedsProfile(true);
                    } finally {
                        setProfileChecked(true);
                    }
                } else {
                    setIsLoggedIn(false);
                    setProfileChecked(true);
                }
            } catch {
                setIsLoggedIn(false);
                setProfileChecked(true);
            } finally {
                setAuthChecked(true);
            }
        };
        checkAuth();
    }, []);

    if (!authChecked || !profileChecked) {
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
                {/* 인증 없이 접근 가능 */}
                <Route path="/"      element={<LoginPage />} />
                <Route path="/login" element={<LoginPage />} />

                {/* 최초 프로필 설정 */}
                <Route path="/user-info" element={
                    !isLoggedIn   ? <Navigate to="/login" replace /> :
                        !needsProfile ? <Navigate to="/main"  replace /> :
                            <UserInfoPage onComplete={() => setNeedsProfile(false)} />
                } />

                {/* 보호 라우트 */}
                <Route path="/main"       element={<Protected isLoggedIn={isLoggedIn} needsProfile={needsProfile}><Main /></Protected>} />
                <Route path="/survey"     element={<Protected isLoggedIn={isLoggedIn} needsProfile={needsProfile}><Survey setSurveyAnswers={setSurveyAnswers} /></Protected>} />
                <Route path="/loading"    element={<Protected isLoggedIn={isLoggedIn} needsProfile={needsProfile}><Loading surveyAnswers={surveyAnswers} setPersonaCode={setPersonaCode} setPersonaName={setPersonaName} /></Protected>} />
                <Route path="/result"     element={<Protected isLoggedIn={isLoggedIn} needsProfile={needsProfile}><Result personaCode={personaCode} /></Protected>} />
                <Route path="/book-loading" element={<Protected isLoggedIn={isLoggedIn} needsProfile={needsProfile}><BookLoading personaName={personaName} /></Protected>} />
                <Route path="/books"      element={<Protected isLoggedIn={isLoggedIn} needsProfile={needsProfile}><Books personaName={personaName} /></Protected>} />
                <Route path="/books/:bookId" element={<Protected isLoggedIn={isLoggedIn} needsProfile={needsProfile}><BookDetailPage /></Protected>} />
                <Route path="/ranking"    element={<Protected isLoggedIn={isLoggedIn} needsProfile={needsProfile}><RankingPage /></Protected>} />
                <Route path="/search"     element={<Protected isLoggedIn={isLoggedIn} needsProfile={needsProfile}><SearchPage /></Protected>} />
                <Route path="/mypage"     element={<Protected isLoggedIn={isLoggedIn} needsProfile={needsProfile}><MyPage /></Protected>} />
                <Route path="/inventory"  element={<Protected isLoggedIn={isLoggedIn} needsProfile={needsProfile}><InventoryPage /></Protected>} />
            </Routes>
        </BrowserRouter>
    );
}

function Protected({ isLoggedIn, needsProfile, children }) {
    if (!isLoggedIn)  return <Navigate to="/login"     replace />;
    if (needsProfile) return <Navigate to="/user-info" replace />;
    return children;
}

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