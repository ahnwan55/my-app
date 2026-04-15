from fastapi import APIRouter
from app.schemas import RecommendRequest
from app.ollama_client import chat

router = APIRouter()


@router.post("/recommend")
async def recommend(req: RecommendRequest):
    """
    Ollama LLM 기반 AI 추천 코멘트 생성

    호출 흐름:
      Spring Boot RecommendationService
        → POST /recommend
        → Ollama (Qwen 2.5) LLM 호출
        → JSON 형태 추천 결과 반환

    역할:
      룰 기반 추천 결과를 보완하는 AI 코멘트 생성
      LLM이 자연어로 "왜 이 상품이 좋은지" 설명해줌
      AI 서버 장애 시 Spring Boot는 룰 기반 결과만 반환 (폴백 처리)
    """
    product_list = "\n".join([
        f"- {p.bank} {p.name} / 금리 {p.interest_rate}% / {p.period_months}개월"
        for p in req.products
    ])
    prompt = f"""
    다음 [상품 목록]에서 사용자에게 가장 적합한 상품을 추천해줘.
    목록에 없는 상품이나 정보는 절대 사용하지 말고 반드시 새로 만들지 말 것.
    반드시 목록에 있는 은행명과 상품명을 그대로 사용할 것.

    [상품 목록] (이 목록에서만 선택)
    {product_list}

    [사용자 정보]
    - 나이: {req.profile.age}세
    - 직업: {req.profile.job}
    - 소득: {req.profile.income}만원
    - 목표: {req.profile.goal}
    - 위험성향: {req.profile.risk_type}

    반드시 위 상품 목록에서 1~3개만 골라 아래 JSON 형태로만 답해줘:
    {{
        "recommendations": [
            {{
                "bank": "목록에 있는 은행명",
                "name": "목록에 있는 상품명",
                "reason": "사회초년생에게 추천하는 구체적인 이유"
            }}
        ],
        "summary": "전체 추천 요약 한 줄"
    }}
    """
    result = await chat(prompt)
    return {"result": result}