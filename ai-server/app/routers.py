from fastapi import APIRouter
from app.schemas import EmbedRequest, EmbedResponse, EmbedBatchRequest, EmbedBatchResponse
from sentence_transformers import SentenceTransformer

router = APIRouter()

# SRoBERTa 모델 로드 (jhgan/ko-sroberta-multitask)
# 주의: 처음 실행 시 허깅페이스에서 모델을 다운로드하므로 약간의 시간이 소요됩니다.
model = SentenceTransformer('jhgan/ko-sroberta-multitask')

@router.post("/embed", response_model=EmbedResponse)
async def embed(req: EmbedRequest):
    """단일 텍스트 임베딩 생성 (768차원 벡터)"""
    vector = model.encode(req.text).tolist()
    return EmbedResponse(embedding=vector)

@router.post("/embed/batch", response_model=EmbedBatchResponse)
async def embed_batch(req: EmbedBatchRequest):
    """다중 텍스트 배치 임베딩 생성"""
    vectors = model.encode(req.texts).tolist()
    return EmbedBatchResponse(embeddings=vectors)