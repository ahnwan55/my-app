from fastapi import APIRouter
from app.schemas import AnalyzeRequest, RecommendRequest, PersonaRequest, PersonaResponse
from app.ollama_client import chat
from app.ml_model import predict_persona

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


@router.post("/persona", response_model=PersonaResponse)
def classify_persona(req: PersonaRequest):
    """
    XGBoost 페르소나 분류 엔드포인트

    호출 흐름:
      Spring Boot SurveyService (설문 완료 시)
        → POST /persona
        → XGBoost 모델로 6개 페르소나 중 1개 분류
        → SHAP으로 "왜 이 페르소나인지" 근거 계산
        → PersonaResponse 반환
        → Spring Boot가 SurveySession.personaType에 저장

    SHAP 활용:
      프론트엔드에서 "당신이 SAFETY_GUARD인 이유는 저축 성향이 높기 때문입니다"
      같은 XAI(설명 가능한 AI) 메시지 표시에 사용
    """
    result = predict_persona(
        age=req.age,
        income=req.income,
        savings_rate=req.savings_rate,
        risk_score=req.risk_score,
        goal_term=req.goal_term,
    )
    return PersonaResponse(**result)