from fastapi import FastAPI
from app.routers import router

app = FastAPI(title="재무 페르소나 AI 서버")
app.include_router(router)

@app.get("/health")
def health():
    return {"status": "ok"}