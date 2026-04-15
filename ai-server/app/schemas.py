from pydantic import BaseModel
from typing import List, Optional


# ── 기존 LLM 추천용 스키마 ───────────────────────────────────────

class UserProfile(BaseModel):
    """사용자 프로필 (LLM 추천 요청 시 사용)"""
    age: int
    job: str
    income: int       # 월 소득 (만원)
    goal: str         # 재무 목표
    risk_type: str    # 위험 성향: 안정형 / 균형형 / 공격형


class Product(BaseModel):
    """추천 후보 상품 정보"""
    name: str
    bank: str
    interest_rate: float   # 기본 금리 (%)
    period_months: int     # 가입 기간 (개월)


class AnalyzeRequest(BaseModel):
    """프로필 분석 요청 (현재 미사용, 추후 확장용)"""
    profile: UserProfile


class RecommendRequest(BaseModel):
    """
    LLM 추천 요청
    Spring Boot RecommendationService → POST /recommend
    """
    profile: UserProfile
    products: List[Product]   # 룰 기반으로 필터링된 후보 상품 목록


# ── XGBoost 페르소나 분류용 스키마 ──────────────────────────────

class PersonaRequest(BaseModel):
    """
    XGBoost 페르소나 분류 요청
    Spring Boot SurveyService → POST /persona

    설문 완료 후 Spring Boot가 설문 점수를 계산해서 전달
    savings_rate, risk_score는 설문 응답 점수를 0~100으로 정규화한 값
    """
    age: int            # 나이 (세)
    income: int         # 월 소득 (만원)
    savings_rate: int   # 저축 성향 점수 (0~100)
    risk_score: int     # 위험 선호도 점수 (0~100)
    goal_term: int      # 목표 기간 (개월)


class ShapExplanation(BaseModel):
    """
    SHAP 특성 기여도
    각 값의 의미:
      - 양수(+): 해당 특성이 이 페르소나 예측에 긍정적으로 기여
      - 음수(-): 해당 특성이 이 페르소나 예측에 부정적으로 기여
      - 절댓값이 클수록 해당 특성의 영향이 큼
    """
    age: float
    income: float
    savings_rate: float
    risk_score: float
    goal_term: float


class PersonaResponse(BaseModel):
    """
    XGBoost 페르소나 분류 결과
    Spring Boot가 이 결과를 받아 SurveySession에 persona_type 저장

    persona_code는 Spring Boot PersonaCode enum 값과 동일:
      SAFETY_GUARD / STEADY_WORKER / BALANCED_SPENDER /
      RATE_OPTIMIZER / GOAL_ACHIEVER / FUTURE_PLANNER
    """
    persona_code: str                    # 예측된 페르소나 코드
    confidence: float                    # 예측 확률 (0.0 ~ 1.0)
    shap_explanation: ShapExplanation    # 각 특성의 SHAP 기여도