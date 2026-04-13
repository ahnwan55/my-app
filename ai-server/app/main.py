from fastapi import FastAPI
from app.routers import router
from app.database import seed_vector_db

app = FastAPI(title="재무 상담 AI 서버")

app.include_router(router)

@app.on_event("startup")
async def startup_event():
    """앱 시작 시 벡터 DB 초기 데이터 적재"""
    seed_vector_db()

@app.get("/health")
def health():
    return {"status": "ok"}