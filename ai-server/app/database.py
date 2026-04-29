import os
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker, DeclarativeBase

# 환경변수에서 DB 연결 정보를 읽어옴
# .env 파일 또는 Docker Compose environment 블록에서 주입
DATABASE_URL = os.getenv(
    "DATABASE_URL",
    "postgresql://postgres:postgres@localhost:5432/bookjjeok"
)

# SQLAlchemy 엔진 생성
# pool_pre_ping: 커넥션 유효성을 쿼리 전에 확인하여 끊긴 커넥션 방지
engine = create_engine(DATABASE_URL, pool_pre_ping=True)

# 세션 팩토리
# autocommit/autoflush 비활성화 → 명시적 commit/rollback 사용
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# SQLAlchemy 2.x 스타일 Base 클래스
class Base(DeclarativeBase):
    pass


def get_db():
    """
    FastAPI 의존성 주입용 DB 세션 제공 함수
    요청마다 새 세션을 열고, 응답 후 자동으로 닫음

    사용 예시:
        @router.post("/embed")
        async def embed(req: EmbedRequest, db: Session = Depends(get_db)):
            ...
    """
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def check_pgvector():
    """
    pgvector 익스텐션 설치 여부 확인 및 자동 활성화
    서버 시작 시 호출하여 vector 타입 사용 가능 여부를 보장
    """
    with engine.connect() as conn:
        # pgvector 익스텐션이 없으면 자동 생성
        conn.execute(text("CREATE EXTENSION IF NOT EXISTS vector"))
        conn.commit()
    print("✅ pgvector 익스텐션 활성화 완료")