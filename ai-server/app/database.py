import os
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker, DeclarativeBase

# 환경변수에서 DB 연결 정보를 읽어옴
# .env 파일 또는 Docker Compose environment 블록에서 주입
DB_USER = os.getenv("DB_USER")
DB_PASSWORD = os.getenv("DB_PASSWORD")
DB_URL = os.getenv("DB_URL") # e.g. jdbc:postgresql://host:5432/bookjjeok

if DB_USER and DB_PASSWORD and DB_URL:
    from urllib.parse import urlparse, urlunparse, parse_qsl, urlencode
    
    # jdbc: 접두어 제거
    clean_url = DB_URL.replace("jdbc:", "")
    parsed = urlparse(clean_url)
    
    # 쿼리 파라미터 파싱 및 변환
    params = dict(parse_qsl(parsed.query))
    
    # psycopg2는 'ssl=true' 대신 'sslmode'를 사용함
    if 'ssl' in params:
        ssl_val = params.pop('ssl').lower()
        if ssl_val == 'true':
            params['sslmode'] = 'require'
    
    # 다시 쿼리 스트링으로 조립
    new_query = urlencode(params)
    
    # 사용자 정보 추가하여 새 URL 생성
    # parsed.netloc 에는 host:port 가 들어있음
    new_netloc = f"{DB_USER}:{DB_PASSWORD}@{parsed.netloc}"
    
    DATABASE_URL = urlunparse((
        parsed.scheme,
        new_netloc,
        parsed.path,
        parsed.params,
        new_query,
        parsed.fragment
    ))
else:
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