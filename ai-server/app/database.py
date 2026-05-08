import os
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker, DeclarativeBase

# 환경변수에서 DB 연결 정보를 읽어옴
# .env 파일 또는 Docker Compose environment 블록에서 주입
DB_USER = os.getenv("DB_USER")
DB_PASSWORD = os.getenv("DB_PASSWORD")
DB_URL = os.getenv("DB_URL") # e.g. jdbc:postgresql://host:5432/bookjjeok

from urllib.parse import urlparse, urlunparse, parse_qsl, urlencode

def clean_database_url(url: str, user=None, password=None):
    if not url:
        return url
    # jdbc: 접두어 제거
    clean_url = url.replace("jdbc:", "")
    parsed = urlparse(clean_url)
    
    # 쿼리 파라미터 파싱 및 필터링
    raw_params = parse_qsl(parsed.query)
    params = {}
    for k, v in raw_params:
        key_lower = k.lower()
        if key_lower == 'ssl':
            if v.lower() == 'true':
                params['sslmode'] = 'require'
        elif key_lower in ['sslfactory', 'targetservertype', 'sslmode', 'currentschema']:
            if key_lower == 'sslmode':
                params['sslmode'] = v
            continue
        else:
            params[k] = v

    new_query = urlencode(params)
    netloc = parsed.netloc
    if user and password:
        # 기존 netloc에 이미 user:pass가 포함되어 있을 수 있으므로 분리 후 재조립
        host_port = netloc.split('@')[-1]
        netloc = f"{user}:{password}@{host_port}"
    
    return urlunparse((
        parsed.scheme if parsed.scheme else "postgresql",
        netloc,
        parsed.path,
        parsed.params,
        new_query,
        parsed.fragment
    ))

if DB_USER and DB_PASSWORD and DB_URL:
    DATABASE_URL = clean_database_url(DB_URL, DB_USER, DB_PASSWORD)
else:
    raw_url = os.getenv("DATABASE_URL", "postgresql://postgres:postgres@localhost:5432/bookjjeok")
    DATABASE_URL = clean_database_url(raw_url)

# SQLAlchemy 엔진 생성
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