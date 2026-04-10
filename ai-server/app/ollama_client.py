import httpx
import re
import os

OLLAMA_URL = os.getenv("OLLAMA_URL", "http://localhost:11434/api/chat")
MODEL = "qwen2.5:7b"

SYSTEM_PROMPT = """
너는 사회초년생 전문 재무 상담 AI야.
다음 규칙을 반드시 지켜:
1. 주어진 상품 목록에 없는 상품은 절대 언급하지 마.
2. 모든 응답은 한국어로 해.
3. 전문 용어는 쉽게 풀어서 설명해.
4. JSON 응답 시 마크다운 코드블록 없이 순수 JSON만 반환해.
5. 페르소나는 반드시 [안정형, 목돈마련형, 단기유동성형] 중 하나만 선택해.
"""

async def chat(prompt: str) -> str:
    async with httpx.AsyncClient(timeout=60.0) as client:
        response = await client.post(OLLAMA_URL, json={
            "model": MODEL,
            "messages": [
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": prompt}
            ],
            "stream": False
        })
        content = response.json()["message"]["content"]
        content = re.sub(r"```json|```", "", content).strip()
        return content