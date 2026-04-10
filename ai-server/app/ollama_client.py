import httpx
import re
import os

OLLAMA_URL = os.getenv("OLLAMA_URL", "http://localhost:11434/api/chat")
MODEL = "qwen2.5:7b"

async def chat(prompt: str) -> str:
    async with httpx.AsyncClient(timeout=60.0) as client:
        response = await client.post(OLLAMA_URL, json={
            "model": MODEL,
            "messages": [{"role": "user", "content": prompt}],
            "stream": False
        })
        content = response.json()["message"]["content"]
        content = re.sub(r"```json|```", "", content).strip()
        return content