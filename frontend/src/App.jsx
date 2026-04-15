import { BrowserRouter, Routes, Route, useNavigate } from "react-router-dom";
import MainPage from "./components/MainPage";
import SurveyPage from "./components/SurveyPage";
import LoadingPage from "./components/LoadingPage";
import PersonaResult from "./components/PersonaResult";
import BookLoadingPage from "./components/BookLoadingPage";
import BookResultPage from "./components/BookResultPage";

function Main() {
  const navigate = useNavigate();
  return <MainPage onStart={() => navigate("/survey")} />;
}

function Survey() {
  const navigate = useNavigate();
  return <SurveyPage onSubmit={() => navigate("/loading")} />;
}

function Loading() {
  const navigate = useNavigate();
  return <LoadingPage onComplete={() => navigate("/result")} />;
}

function Result() {
  const navigate = useNavigate();
  return (
      <PersonaResult
          personaCode="GOAL_ACHIEVER"
          onViewBooks={() => navigate("/book-loading")}
      />
  );
}

function BookLoading() {
  const navigate = useNavigate();
  return (
      <BookLoadingPage
          personaName="목표 달성자"
          onComplete={() => navigate("/books")}
      />
  );
}

function Books() {
  const navigate = useNavigate();
  return <BookResultPage onBack={() => navigate("/result")} />;
}

export default function App() {
  return (
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Main />} />
          <Route path="/survey" element={<Survey />} />
          <Route path="/loading" element={<Loading />} />
          <Route path="/result" element={<Result />} />
          <Route path="/book-loading" element={<BookLoading />} />
          <Route path="/books" element={<Books />} />
        </Routes>
      </BrowserRouter>
  );
}