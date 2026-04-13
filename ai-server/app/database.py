import chromadb

# 로컬 개발용 인메모리 Chroma 클라이언트
# 운영 환경에서는 PersistentClient로 교체
client = chromadb.EphemeralClient()

# 금융상품 추천을 위한 컬렉션
# 페르소나별 상품 설명 벡터를 저장
collection = client.get_or_create_collection(
    name="financial_products",
    metadata={"hnsw:space": "cosine"}  # 유사도 계산 방식
)

def seed_vector_db():
    """로컬 개발용 샘플 벡터 데이터 초기 적재"""

    # 이미 데이터가 있으면 중복 삽입 방지
    if collection.count() > 0:
        return

    sample_docs = [
        "청년 우대 정기예금: 안정적인 원금 보장, 연 4.5% 금리, 6~24개월, 국민은행, 최소 가입금액 10만원",
        "사회초년생 자유적금: 자유 납입 방식, 연 5.0% 금리, 12~36개월, 신한은행, 최소 가입금액 1만원",
        "직장인 정기적금: 직장인 전용, 연 4.8% 금리, 6~24개월, 우리은행, 최소 가입금액 5만원",
        "청년희망적금: 정부 지원 우대금리, 연 6.0% 금리, 24개월, 기업은행, 청년 전용",
        "첫급여 정기예금: 첫 직장인 전용, 연 4.2% 금리, 12개월, 하나은행, 최소 가입금액 50만원",
    ]

    sample_ids = [
        "product_1",
        "product_2",
        "product_3",
        "product_4",
        "product_5",
    ]

    sample_metadatas = [
        {"bank": "국민은행", "type": "예금", "risk": "LOW"},
        {"bank": "신한은행", "type": "적금", "risk": "LOW"},
        {"bank": "우리은행", "type": "적금", "risk": "LOW"},
        {"bank": "기업은행", "type": "적금", "risk": "MEDIUM"},
        {"bank": "하나은행", "type": "예금", "risk": "LOW"},
    ]

    collection.add(
        documents=sample_docs,
        ids=sample_ids,
        metadatas=sample_metadatas
    )
    print(f"✅ Vector DB 초기 데이터 {len(sample_docs)}개 적재 완료")


def get_collection():
    """컬렉션 반환 (라우터에서 사용)"""
    return collection