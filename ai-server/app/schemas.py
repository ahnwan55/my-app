from pydantic import BaseModel
from typing import List

class UserProfile(BaseModel):
    age: int
    job: str
    income: int
    goal: str
    risk_type: str  # 안정형 / 중립형 / 공격형

class Product(BaseModel):
    name: str
    bank: str
    interest_rate: float
    period_months: int

class AnalyzeRequest(BaseModel):
    profile: UserProfile

class RecommendRequest(BaseModel):
    profile: UserProfile
    products: List[Product]