import { BrowserRouter, Routes, Route, useNavigate } from "react-router-dom";
import { useState } from "react";
import MainPage from "./components/MainPage";
import SurveyPage from "./components/SurveyPage";
import LoadingPage from "./components/LoadingPage";
import PersonaResult from "./components/PersonaResult";
import BookLoadingPage from "./components/BookLoadingPage";
import BookResultPage from "./components/BookResultPage";

export default function App() {
  // 설문 답변과 페르소나 결과를 전역 state로 관리
  // SurveyPage → LoadingPage → PersonaResult → BookLoadingPage → BookResultPage 순서로 전달
  const [surveyAnswers, setSurveyAnswers] = useState({});
  const [personaCode, setPersonaCode] = useState("EXPLORER");
  const [personaName, setPersonaName] = useState("지적 탐험가");

  return (
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Main />} />
          <Route
              path="/survey"
              element={<Survey setSurveyAnswers={setSurveyAnswers} />}
          />
          <Route
              path="/loading"
              element={
                <Loading
                    surveyAnswers={surveyAnswers}
                    setPersonaCode={setPersonaCode}
                    setPersonaName={setPersonaName}
                />
              }
          />
          <Route
              path="/result"
              element={<Result personaCode={personaCode} />}
          />
          <Route
              path="/book-loading"
              element={<BookLoading personaName={personaName} />}
          />
          <Route
              path="/books"
              element={<Books personaName={personaName} />}
          />
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
            // 현재는 더미 - FastAPI 연동 후 실제 페르소나 코드 받아서 설정
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
      <PersonaResult
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