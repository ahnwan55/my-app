from pydantic import BaseModel
from typing import List


# ── LLM 추천용 스키마 ─────────────────────────────────────────

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


class RecommendRequest(BaseModel):
    """
    LLM 추천 요청
    Spring Boot RecommendationService → POST /recommend
    """
    profile: UserProfile
    products: List[Product]   # 룰 기반으로 필터링된 후보 상품 목록