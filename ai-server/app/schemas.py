from pydantic import BaseModel
from typing import List

class EmbedRequest(BaseModel):
    text: str

class EmbedResponse(BaseModel):
    embedding: List[float]

class EmbedBatchRequest(BaseModel):
    texts: List[str]

class EmbedBatchResponse(BaseModel):
    embeddings: List[List[float]]