from __future__ import annotations

import json
import os
from typing import Any
from urllib import error, request

TIMEWEB_API_URL_TEMPLATE = (
    "https://agent.timeweb.cloud/api/v1/cloud-ai/agents/{agent_id}/v1/chat/completions"
)
TIMEWEB_API_KEY_ENV = "TIMEWEB_API_KEY"
TIMEWEB_AGENT_ID_ENV = "TIMEWEB_AGENT_ID"
TIMEWEB_MODEL = os.getenv("TIMEWEB_MODEL", "deepseek-chat")
REQUEST_TIMEOUT_SECONDS = float(os.getenv("TIMEWEB_TIMEOUT_SECONDS", "30"))

SYSTEM_PROMPT = """\
Ты — AI-ассистент для владельца автомобиля. Твоя задача — отвечать на вопросы
пользователя о его автомобиле, используя предоставленную информацию из истории автомобиля.

Правила:
- Отвечай на русском языке.
- Отвечай кратко и по делу.
- Используй только факты из предоставленного контекста.
- Если информации нет в контексте, скажи, что данных недостаточно.
- Не выдумывай информацию, которой нет в истории автомобиля.
"""

TITLE_SYSTEM_PROMPT = """\
Ты создаёшь короткий заголовок для диалога на русском языке.

Правила:
- Верни только заголовок без кавычек и без пояснений.
- Заголовок должен содержать примерно 2-6 слов.
- Заголовок должен быть информативным и естественным.
- Максимальная длина заголовка — 50 символов.
"""

MISSING_CONFIGURATION_ANSWER = (
    "Сервис AI-ассистента не настроен. Добавьте TIMEWEB_API_KEY и TIMEWEB_AGENT_ID."
)
FALLBACK_ANSWER = "Не удалось получить ответ от AI-ассистента. Попробуйте позже."


def ask_deepseek(
    message: str,
    vehicle_context: str | None = None,
    chat_context: str | None = None,
) -> str:
    api_key = os.getenv(TIMEWEB_API_KEY_ENV)
    agent_id = os.getenv(TIMEWEB_AGENT_ID_ENV)
    if not api_key or not agent_id:
        return MISSING_CONFIGURATION_ANSWER

    messages: list[dict[str, str]] = [{"role": "system", "content": SYSTEM_PROMPT}]

    if vehicle_context:
        messages.append(
            {
                "role": "system",
                "content": f"Информация из истории автомобиля:\n{vehicle_context}",
            }
        )

    if chat_context:
        messages.append(
            {
                "role": "system",
                "content": f"Контекст текущего диалога:\n{chat_context}",
            }
        )

    messages.append({"role": "user", "content": message})

    payload = {
        "model": TIMEWEB_MODEL,
        "messages": messages,
        "temperature": 0.3,
        "max_tokens": 512,
    }

    try:
        return _call_timeweb_agent_api(
            api_key=api_key,
            agent_id=agent_id,
            payload=payload,
        )
    except ValueError:
        return FALLBACK_ANSWER


def generate_chat_title(first_user_message: str, first_assistant_reply: str) -> str:
    api_key = os.getenv(TIMEWEB_API_KEY_ENV)
    agent_id = os.getenv(TIMEWEB_AGENT_ID_ENV)
    if not api_key or not agent_id:
        return _fallback_title(first_user_message)

    payload = {
        "model": TIMEWEB_MODEL,
        "messages": [
            {"role": "system", "content": TITLE_SYSTEM_PROMPT},
            {
                "role": "user",
                "content": (
                    f"Первое сообщение пользователя: {first_user_message}\n"
                    f"Первый ответ ассистента: {first_assistant_reply}"
                ),
            },
        ],
        "temperature": 0.2,
        "max_tokens": 64,
    }

    try:
        title = _call_timeweb_agent_api(
            api_key=api_key,
            agent_id=agent_id,
            payload=payload,
        ).strip()
    except ValueError:
        return _fallback_title(first_user_message)

    title = title.strip('"').strip("'").strip()
    if not title:
        return _fallback_title(first_user_message)
    return title[:50]


def _fallback_title(first_user_message: str) -> str:
    normalized = " ".join(first_user_message.strip().split())
    if not normalized:
        return "Новый чат"
    return normalized[:50]


def _call_timeweb_agent_api(
    api_key: str, agent_id: str, payload: dict[str, Any]
) -> str:
    body = json.dumps(payload).encode("utf-8")
    req = request.Request(
        TIMEWEB_API_URL_TEMPLATE.format(agent_id=agent_id),
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
        raise ValueError(f"Timeweb agent API error: {exc.code} {error_body}") from exc
    except error.URLError as exc:
        raise ValueError("Timeweb agent API is unreachable") from exc

    choices = response_payload.get("choices")
    if not choices:
        raise ValueError("Timeweb agent response does not contain choices")

    message = choices[0].get("message", {})
    content = message.get("content")
    if isinstance(content, str):
        return content

    if isinstance(content, list):
        text_parts = [
            part.get("text")
            for part in content
            if isinstance(part, dict) and part.get("type") == "text"
        ]
        combined = "".join(part for part in text_parts if part)
        if combined:
            return combined

    raise ValueError("Timeweb agent response does not contain text content")
