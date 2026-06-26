from __future__ import annotations

import json
import os
from typing import Any
from urllib import error, request

DEEPSEEK_API_URL = os.getenv("DEEPSEEK_API_URL", "https://agent.timeweb.cloud/api/v1/cloud-ai/agents/fc0c89ec-4868-408c-99cd-dc6d5ea764c8/v1/chat/completions")
DEEPSEEK_MODEL = os.getenv("DEEPSEEK_MODEL", "deepseek-chat")
DEEPSEEK_API_KEY_ENV = "DEEPSEEK_API_KEY"
REQUEST_TIMEOUT_SECONDS = float(os.getenv("DEEPSEEK_TIMEOUT_SECONDS", "30"))

SYSTEM_PROMPT = """\
Ты — AI-ассистент для владельца автомобиля. Твоя задача — отвечать на вопросы \
пользователя о его автомобиле, используя предоставленную информацию из истории автомобиля.

Правила:
- Отвечай на русском языке.
- Отвечай кратко и по делу.
- Используй только факты из предоставленного контекста.
- Если информации нет в контексте, скажи, что данных недостаточно.
- Не выдумывай информацию, которой нет в истории автомобиля.
"""

MISSING_API_KEY_ANSWER = (
    "Сервис AI-ассистента не настроен. Добавьте DEEPSEEK_API_KEY."
)
FALLBACK_ANSWER = (
    "Не удалось получить ответ от AI-ассистента. Попробуйте позже."
)


def ask_deepseek(message: str, vehicle_context: str | None = None) -> str:
    api_key = os.getenv(DEEPSEEK_API_KEY_ENV)
    if not api_key:
        return MISSING_API_KEY_ANSWER

    messages: list[dict[str, str]] = [
        {"role": "system", "content": SYSTEM_PROMPT},
    ]

    if vehicle_context:
        messages.append({
            "role": "system",
            "content": f"Информация из истории автомобиля:\n{vehicle_context}",
        })

    messages.append({"role": "user", "content": message})

    payload = {
        "model": DEEPSEEK_MODEL,
        "messages": messages,
        "temperature": 0.3,
        "max_tokens": 512,
    }

    try:
        return _call_deepseek_api(api_key=api_key, payload=payload)
    except (ValueError, Exception):
        return FALLBACK_ANSWER


def _call_deepseek_api(api_key: str, payload: dict[str, Any]) -> str:
    body = json.dumps(payload).encode("utf-8")
    req = request.Request(
        DEEPSEEK_API_URL,
        data=body,
        headers={
            "Authorization": f"Bearer {api_key}",
            "Content-Type": "application/json",
            "Accept": "application/json",
        },
        method="POST",
    )

    try:
        with request.urlopen(req, timeout=REQUEST_TIMEOUT_SECONDS) as response:
            response_payload = json.loads(response.read().decode("utf-8"))
    except error.HTTPError as exc:
        error_body = exc.read().decode("utf-8", errors="ignore")
        raise ValueError(f"DeepSeek API error: {exc.code} {error_body}") from exc
    except error.URLError as exc:
        raise ValueError("DeepSeek API is unreachable") from exc

    choices = response_payload.get("choices")
    if not choices:
        raise ValueError("DeepSeek response does not contain choices")

    msg = choices[0].get("message", {})
    content = msg.get("content")
    if isinstance(content, str):
        return content

    raise ValueError("DeepSeek response does not contain text content")
