import { BrowserRouter, Routes, Route, useNavigate } from "react-router-dom";
import { useState } from "react";

// 기존 페이지
import MainPage from "./pages/MainPage";
import SurveyPage from "./pages/SurveyPage";
import LoadingPage from "./pages/LoadingPage";
import PersonaResultPage from "./pages/PersonaResultPage";
import BookLoadingPage from "./pages/BookLoadingPage";
import BookResultPage from "./pages/BookResultPage";

// 새 페이지 (아직 생성 전 - 하나씩 만들 예정)
import LoginPage from "./pages/LoginPage";
import UserInfoPage from "./pages/UserInfoPage";
import ChatRoomListPage from "./pages/ChatRoomListPage";
import ChatRoomPage from "./pages/ChatRoomPage";
import MissionPage from "./pages/MissionPage";
import MyPage from "./pages/MyPage";

export default function App() {
    const [surveyAnswers, setSurveyAnswers] = useState({});
    const [personaCode, setPersonaCode] = useState("EXPLORER");
    const [personaName, setPersonaName] = useState("지적 탐험가");

    return (
        <BrowserRouter>
            <Routes>
                {/* 인증 */}
                <Route path="/login" element={<LoginPage />} />
                <Route path="/user-info" element={<UserInfoPage />} />

                {/* 메인 */}
                <Route path="/" element={<Main />} />

                {/* 페르소나 찾기 플로우 */}
                <Route path="/survey" element={<Survey setSurveyAnswers={setSurveyAnswers} />} />
                <Route path="/loading" element={<Loading surveyAnswers={surveyAnswers} setPersonaCode={setPersonaCode} setPersonaName={setPersonaName} />} />
                <Route path="/result" element={<Result personaCode={personaCode} />} />
                <Route path="/book-loading" element={<BookLoading personaName={personaName} />} />
                <Route path="/books" element={<Books personaName={personaName} />} />

                {/* 채팅 */}
                <Route path="/chat" element={<ChatRoomListPage />} />
                <Route path="/chat/:personaId" element={<ChatRoomPage />} />

                {/* 기타 */}
                <Route path="/mission" element={<MissionPage />} />
                <Route path="/mypage" element={<MyPage />} />
            </Routes>
        </BrowserRouter>
    );
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
            onComplete={(code, name) => {
                setPersonaCode(code || "EXPLORER");
                setPersonaName(name || "지적 탐험가");
                navigate("/result");
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