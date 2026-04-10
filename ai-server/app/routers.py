from fastapi import APIRouter
from app.schemas import AnalyzeRequest, RecommendRequest
from app.ollama_client import chat

router = APIRouter()


@router.post("/recommend")
async def recommend(req: RecommendRequest):
    product_list = "\n".join([
        f"- {p.bank} {p.name} / 금리 {p.interest_rate}% / {p.period_months}개월"
        for p in req.products
    ])
    prompt = f"""
    아래 [상품 목록]에 있는 상품만 추천해.
    목록에 없는 상품명은 절대 사용하지 마. 상품명을 바꾸거나 새로 만들지 마.
    반드시 목록에 있는 은행명과 상품명을 그대로 사용해.

    [상품 목록] (이 목록에서만 선택)
    {product_list}

    [사용자 정보]
    - 나이: {req.profile.age}세
    - 직업: {req.profile.job}
    - 월수입: {req.profile.income}만원
    - 저축목표: {req.profile.goal}
    - 리스크성향: {req.profile.risk_type}

    반드시 위 상품 목록에서 1~3개만 골라서 아래 JSON 형식으로만 답해줘:
    {{
        "recommendations": [
            {{
                "bank": "목록에 있는 은행명 그대로",
                "name": "목록에 있는 상품명 그대로",
                "reason": "사회초년생 눈높이로 추천 이유 설명"
            }}
        ],
        "summary": "전체 추천 요약 한 줄"
    }}
    """
    result = await chat(prompt)
    return {"result": result}