from contextlib import asynccontextmanager
from fastapi import FastAPI
from app.routers import router
from app.database import check_pgvector
from prometheus_fastapi_instrumentator import Instrumentator


@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    서버 시작/종료 시 실행되는 lifespan 이벤트 핸들러
    FastAPI 0.93 이상에서 @app.on_event 대신 권장되는 방식

    startup: pgvector 익스텐션 활성화
    shutdown: 필요 시 정리 로직 추가 가능
    """
    # 서버 시작 시 pgvector 익스텐션 확인 및 활성화
    check_pgvector()
    yield
    # 서버 종료 시 실행할 로직 (현재는 없음)


app = FastAPI(
    title="도서 임베딩 AI 서버",
    description="SRoBERTa 모델을 사용하여 도서 텍스트를 pgvector 검색용 768차원 벡터로 변환하는 서버입니다.",
    lifespan=lifespan
)

app.include_router(router)

# Prometheus FastAPI Instrumentator 설정
# HTTP 요청에 대한 latency, throughput, error 메트릭 및 Histogram bucket 자동 활성화
instrumentator = Instrumentator(
    should_group_status_codes=False,
    should_ignore_untemplated=True,
    should_instrument_requests_inprogress=True,
    excluded_handlers=[".*admin.*", "/metrics"],
    inprogress_name="inprogress",
    inprogress_labels=True,
)
instrumentator.instrument(app).expose(app)


@app.get("/health")
def health():
    return {"status": "ok"}