import json
import os
import re
from typing import Any
from urllib import error, request

from .schemas import ParsedChatEvent


TIMEWEB_API_URL_TEMPLATE = (
    "https://agent.timeweb.cloud/api/v1/cloud-ai/agents/{agent_id}/v1/chat/completions"
)
TIMEWEB_API_KEY_ENV = "TIMEWEB_API_KEY"
TIMEWEB_AGENT_ID_ENV = "TIMEWEB_AGENT_ID"
TIMEWEB_MODEL = os.getenv("TIMEWEB_MODEL", "deepseek-chat")
REQUEST_TIMEOUT_SECONDS = float(os.getenv("TIMEWEB_TIMEOUT_SECONDS", "20"))

SYSTEM_PROMPT = """
You parse Russian user chat messages about vehicle events into structured JSON.

Supported event types:
- fuel
- repair
- trip
- issue
- condition (a technical condition/status update without a malfunction)

Return only valid JSON with exactly these fields:
type, description, amount, mileage, needs_clarification, clarification_question

General rules:
- Parse exactly one vehicle event per message.
- If the message can be confidently parsed, set needs_clarification to false.
- If the message is ambiguous, incomplete, inconsistent, unsupported, or too vague, set needs_clarification to true.
- Do not invent facts that are not explicitly stated or strongly implied.
- Use null for unknown fields.
- clarification_question must be a short Russian question.
- description must be concise and in Russian when available.
- amount and mileage must be integers when present.

Interpretation rules:
- Do not assume fuel, repair, trip, issue, or condition unless the text clearly indicates it.
- If the message explicitly indicates a problem, malfunction, warning light, damage, failure, error, or check-engine symptom, classify it as issue unless the text clearly says a repair was performed.
- If the message reports a normal technical state, inspection result, fluid level, tyre pressure, or odometer update without a malfunction, classify it as condition.
- If the message explicitly says the user drove, traveled, completed a route, or covered a distance, classify it as trip unless other words clearly indicate another type.
- Treat amount as money spent only when the wording clearly indicates price, payment, cost, or currency.
- Treat mileage as odometer mileage only when the wording clearly indicates current vehicle mileage or odometer reading.
- Treat trip distance as traveled distance only when the wording clearly indicates driving distance.
- Ignore date extraction in this baseline. If date or time is mentioned, do not add extra fields and do not ask follow-up questions only about date or time.

You must ask for clarification if any of the following is true:
- the event type is unclear;
- the message contains more than one distinct event;
- a number is present but its meaning is unclear;
- a distance is present but the unit is unclear;
- the message is too vague to produce a reliable description;
- the message is not clearly about a supported vehicle event.

Clarification behavior:
- If the event type is unclear, ask whether it was fuel, repair, trip, issue, or a technical condition update.
- If a number could mean amount, distance, mileage, fuel volume, or another metric, ask what the number refers to.
- If the event is clearly a trip and only the distance unit is unclear, keep the event as trip and ask whether the distance is kilometers or miles.
- If the message contains multiple events, ask the user to send one event at a time.
- Do not ask for optional details unless they are required to understand the meaning of the message.
- If the message explicitly describes an issue symptom, do not ask to confirm the event type.

Examples:
Input: "Заправился на 2500 рублей, пробег 125300"
Output: {"type":"fuel","description":"Заправка на 2500 рублей","amount":2500,"mileage":125300,"needs_clarification":false,"clarification_question":null}

Input: "Сегодня я проехал 1500"
Output: {"type":"trip","description":"Поездка на 1500","amount":null,"mileage":null,"needs_clarification":true,"clarification_question":"Вы имеете в виду 1500 километров или миль?"}

Input: "Поменял масло за 8000"
Output: {"type":"repair","description":"Замена масла","amount":8000,"mileage":null,"needs_clarification":false,"clarification_question":null}

Input: "Загорелся чек двигателя"
Output: {"type":"issue","description":"Загорелся чек двигателя","amount":null,"mileage":null,"needs_clarification":false,"clarification_question":null}

Input: "Машина не заводится"
Output: {"type":"issue","description":"Машина не заводится","amount":null,"mileage":null,"needs_clarification":false,"clarification_question":null}

Input: "Техническое состояние хорошее, пробег 125500"
Output: {"type":"condition","description":"Техническое состояние хорошее","amount":null,"mileage":125500,"needs_clarification":false,"clarification_question":null}

Input: "Заправился на 2500 и поменял масло за 8000"
Output: {"type":null,"description":null,"amount":null,"mileage":null,"needs_clarification":true,"clarification_question":"Уточните, пожалуйста, одно событие за сообщение: это была заправка или ремонт?"}

Input: "Потратил 3000"
Output: {"type":null,"description":null,"amount":null,"mileage":null,"needs_clarification":true,"clarification_question":"Уточните, пожалуйста, это была заправка, ремонт, поездка или проблема?"}

Input: "Пробег 125300, заправился"
Output: {"type":"fuel","description":"Заправка","amount":null,"mileage":125300,"needs_clarification":false,"clarification_question":null}
""".strip()

MISSING_CONFIGURATION_QUESTION = (
    "\u0421\u0435\u0440\u0432\u0438\u0441 \u0440\u0430\u0441\u043f\u043e\u0437\u043d\u0430\u0432\u0430\u043d\u0438\u044f "
    "\u043f\u043e\u043a\u0430 \u043d\u0435 \u043d\u0430\u0441\u0442\u0440\u043e\u0435\u043d. "
    "\u0414\u043e\u0431\u0430\u0432\u044c\u0442\u0435 TIMEWEB_API_KEY \u0438 TIMEWEB_AGENT_ID."
)
FALLBACK_CLARIFICATION_QUESTION = (
    "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0440\u0430\u0441\u043f\u043e\u0437\u043d\u0430\u0442\u044c "
    "\u0437\u0430\u043f\u0438\u0441\u044c. \u0423\u0442\u043e\u0447\u043d\u0438\u0442\u0435, "
    "\u043f\u043e\u0436\u0430\u043b\u0443\u0439\u0441\u0442\u0430, \u0434\u0435\u0442\u0430\u043b\u0438 "
    "\u0441\u043e\u0431\u044b\u0442\u0438\u044f."
)


def parse_chat_message(message: str) -> ParsedChatEvent:
    api_key = os.getenv(TIMEWEB_API_KEY_ENV)
    agent_id = os.getenv(TIMEWEB_AGENT_ID_ENV)
    if not api_key or not agent_id:
        return ParsedChatEvent(
            needs_clarification=True,
            clarification_question=MISSING_CONFIGURATION_QUESTION,
        )

    payload = {
        "model": TIMEWEB_MODEL,
        "messages": [
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": message},
        ],
        "temperature": 0,
    }

    try:
        raw_content = _call_timeweb_agent_api(
            api_key=api_key,
            agent_id=agent_id,
            payload=payload,
        )
        parsed_payload = json.loads(raw_content)
        parsed_event = ParsedChatEvent.model_validate(parsed_payload)
        return _apply_guardrails(message, parsed_event)
    except (ValueError, json.JSONDecodeError):
        return _apply_guardrails(
            message,
            ParsedChatEvent(
                needs_clarification=True,
                clarification_question=FALLBACK_CLARIFICATION_QUESTION,
            ),
        )


def _apply_guardrails(message: str, parsed_event: ParsedChatEvent) -> ParsedChatEvent:
    normalized_message = message.lower()

    if _contains_multiple_distinct_events(normalized_message):
        return ParsedChatEvent(
            needs_clarification=True,
            clarification_question=(
                "\u0423\u0442\u043e\u0447\u043d\u0438\u0442\u0435, "
                "\u043f\u043e\u0436\u0430\u043b\u0443\u0439\u0441\u0442\u0430, "
                "\u043e\u0434\u043d\u043e \u0441\u043e\u0431\u044b\u0442\u0438\u0435 "
                "\u0437\u0430 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435: "
                "\u044d\u0442\u043e \u0431\u044b\u043b\u0430 "
                "\u0437\u0430\u043f\u0440\u0430\u0432\u043a\u0430, "
                "\u0440\u0435\u043c\u043e\u043d\u0442, "
                "\u043f\u043e\u0435\u0437\u0434\u043a\u0430 "
                "\u0438\u043b\u0438 \u043f\u0440\u043e\u0431\u043b\u0435\u043c\u0430?"
            ),
        )

    if _looks_like_issue_message(normalized_message):
        return ParsedChatEvent(
            type="issue",
            description=message.strip(),
            amount=None,
            mileage=parsed_event.mileage,
            needs_clarification=False,
            clarification_question=None,
        )

    if _looks_like_condition_message(normalized_message):
        return ParsedChatEvent(
            type="condition",
            description=message.strip(),
            amount=None,
            mileage=parsed_event.mileage,
            needs_clarification=False,
            clarification_question=None,
        )

    if _looks_like_trip_with_unclear_units(normalized_message):
        distance_match = re.search(r"\b(\d+)\b", normalized_message)
        distance_value = distance_match.group(1) if distance_match else None
        clarification_question = (
            f"\u0412\u044b \u0438\u043c\u0435\u0435\u0442\u0435 \u0432 \u0432\u0438\u0434\u0443 "
            f"{distance_value} \u043a\u0438\u043b\u043e\u043c\u0435\u0442\u0440\u043e\u0432 "
            f"\u0438\u043b\u0438 \u043c\u0438\u043b\u044c?"
            if distance_value
            else (
                "\u0412\u044b \u0438\u043c\u0435\u0435\u0442\u0435 \u0432 \u0432\u0438\u0434\u0443 "
                "\u043a\u0438\u043b\u043e\u043c\u0435\u0442\u0440\u044b \u0438\u043b\u0438 "
                "\u043c\u0438\u043b\u0438?"
            )
        )
        return ParsedChatEvent(
            type="trip",
            description=message.strip(),
            amount=None,
            mileage=None,
            needs_clarification=True,
            clarification_question=clarification_question,
        )

    return parsed_event


def _contains_multiple_distinct_events(message: str) -> bool:
    return (
        sum(
            (
                _contains_fuel_keywords(message),
                _contains_repair_keywords(message),
                _contains_trip_keywords(message),
                _contains_issue_keywords(message),
                _contains_condition_keywords(message),
            )
        )
        > 1
    )


def _looks_like_issue_message(message: str) -> bool:
    return _contains_issue_keywords(message) and not _contains_repair_keywords(message)


def _looks_like_condition_message(message: str) -> bool:
    return _contains_condition_keywords(message) and not (
        _contains_fuel_keywords(message)
        or _contains_repair_keywords(message)
        or _contains_trip_keywords(message)
        or _contains_issue_keywords(message)
    )


def _looks_like_trip_with_unclear_units(message: str) -> bool:
    has_trip = _contains_trip_keywords(message)
    has_number = re.search(r"\b\d+\b", message) is not None
    has_known_unit = any(
        unit in message for unit in ("км", "килом", "mile", "miles", "миль")
    )
    has_other_event = (
        _contains_fuel_keywords(message)
        or _contains_repair_keywords(message)
        or _contains_issue_keywords(message)
    )
    return has_trip and has_number and not has_known_unit and not has_other_event


def _contains_fuel_keywords(message: str) -> bool:
    return any(
        keyword in message for keyword in ("заправ", "топлив", "бензин", "дизел", "азс")
    )


def _contains_repair_keywords(message: str) -> bool:
    return any(
        keyword in message
        for keyword in (
            "ремонт",
            "поменял",
            "заменил",
            "замена",
            "сервис",
            " сто",
            "то ",
            "масло",
        )
    )


def _contains_trip_keywords(message: str) -> bool:
    return any(
        keyword in message
        for keyword in ("проехал", "поезд", "ехал", "доехал", "маршрут", "пут")
    )


def _contains_issue_keywords(message: str) -> bool:
    return any(
        keyword in message
        for keyword in (
            "чек",
            "ошибк",
            "не завод",
            "загорел",
            "ламп",
            "стук",
            "скрип",
            "проблем",
            "полом",
            "не работает",
            "warning",
            "fail",
        )
    )


def _contains_condition_keywords(message: str) -> bool:
    return any(
        keyword in message
        for keyword in (
            "техническое состояние",
            "состояние автомобиля",
            "состояние машины",
            "уровень жидкости",
            "давление в шинах",
            "показания одометра",
        )
    )


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
