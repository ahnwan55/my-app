from fastapi import APIRouter
from app.schemas import AnalyzeRequest, RecommendRequest
from app.ollama_client import chat

router = APIRouter()


@router.post("/analyze")
async def analyze(req: AnalyzeRequest):
    prompt = f"""
    너는 재무 상담 AI야. 아래 사용자 정보를 보고 재무 페르소나를 JSON으로 분류해줘.
    존재하는 페르소나: 안정형, 목돈마련형, 단기유동성형

    사용자 정보:
    - 나이: {req.profile.age}
    - 직업: {req.profile.job}
    - 월수입: {req.profile.income}만원
    - 저축목표: {req.profile.goal}
    - 리스크성향: {req.profile.risk_type}

    반드시 JSON 형식으로만 답해줘:
    {{"persona": "페르소나명", "reason": "이유"}}
    """
    result = await chat(prompt)
    return {"result": result}


@router.post("/recommend")
async def recommend(req: RecommendRequest):
    product_list = "\n".join([
        f"- {p.bank} {p.name} / 금리 {p.interest_rate}% / {p.period_months}개월"
        for p in req.products
    ])
    prompt = f"""
    너는 재무 상담 AI야. 아래 상품 목록에서만 추천해. 목록에 없는 상품은 절대 언급하지 마.

    [상품 목록]
    {product_list}

    사용자 정보:
    - 나이: {req.profile.age}
    - 월수입: {req.profile.income}만원
    - 저축목표: {req.profile.goal}
    - 리스크성향: {req.profile.risk_type}

    추천 상품과 이유를 사회초년생 눈높이에 맞게 설명해줘.
    """
    result = await chat(prompt)
    return {"result": result}