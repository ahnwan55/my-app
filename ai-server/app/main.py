from fastapi import FastAPI
from app.routers import router

app = FastAPI(
    title="도서 임베딩 AI 서버",
    description="SRoBERTa 모델을 사용하여 도서 텍스트를 pgvector 검색용 768차원 벡터로 변환하는 서버입니다."
)

app.include_router(router)

@app.get("/health")
def health():
    return {"status": "ok"}