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

Return only valid JSON with exactly these fields:
type, description, amount, fuel_liters, mileage, needs_clarification, clarification_question

Rules:
- Parse exactly one event.
- Use null for unknown fields.
- amount and mileage must be integers when present.
- fuel_liters may be an integer or decimal number when present.
- fuel_liters is only for liters of fuel.
- amount is only money.
- mileage is odometer mileage, except for trip where mileage is traveled distance.
- If the message is about condition/state/inspection without a malfunction, do not create an event.
- If the message is a greeting (привет, здравствуй, добрый день, etc.), thanks (спасибо, благодарю), farewell (пока, до свидания), or a general question not related to adding a vehicle event, set type to null and needs_clarification to false. Do NOT ask what event the user wants to report.
- If the message is ambiguous, set needs_clarification to true with a short Russian question.
""".strip()


MISSING_CONFIGURATION_QUESTION = (
    "\u0421\u0435\u0440\u0432\u0438\u0441 \u0440\u0430\u0441\u043f\u043e\u0437\u043d\u0430\u0432\u0430\u043d\u0438\u044f "
    "\u043f\u043e\u043a\u0430 \u043d\u0435 \u043d\u0430\u0441\u0442\u0440\u043e\u0435\u043d. "
    "\u0414\u043e\u0431\u0430\u0432\u044c\u0442\u0435 TIMEWEB_API_KEY \u0438 TIMEWEB_AGENT_ID."
)
FALLBACK_CLARIFICATION_QUESTION = (
    "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0440\u0430\u0441\u043f\u043e\u0437\u043d\u0430\u0442\u044c \u0437\u0430\u043f\u0438\u0441\u044c. "
    "\u0423\u0442\u043e\u0447\u043d\u0438\u0442\u0435, \u043f\u043e\u0436\u0430\u043b\u0443\u0439\u0441\u0442\u0430, \u0434\u0435\u0442\u0430\u043b\u0438 \u0441\u043e\u0431\u044b\u0442\u0438\u044f."
)
NON_TIMELINE_CONDITION_QUESTION = (
    "\u042d\u0442\u043e \u0437\u0430\u043f\u0440\u043e\u0441 \u043a \u0430\u0441\u0441\u0438\u0441\u0442\u0435\u043d\u0442\u0443, "
    "\u0430 \u043d\u0435 \u0441\u043e\u0431\u044b\u0442\u0438\u0435 \u0434\u043b\u044f \u0438\u0441\u0442\u043e\u0440\u0438\u0438. "
    "\u0417\u0430\u0434\u0430\u0439\u0442\u0435 \u0435\u0433\u043e \u0432 \u0447\u0430\u0442\u0435 \u0430\u0441\u0441\u0438\u0441\u0442\u0435\u043d\u0442\u0430."
)
FALLBACK_CLARIFICATION_QUESTION = (
    "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0440\u0430\u0441\u043f\u043e\u0437\u043d\u0430\u0442\u044c \u0437\u0430\u043f\u0438\u0441\u044c. "
    "\u041f\u043e\u0436\u0430\u043b\u0443\u0439\u0441\u0442\u0430, \u0443\u0442\u043e\u0447\u043d\u0438\u0442\u0435, \u0434\u0435\u0442\u0430\u043b\u0438 \u0441\u043e\u0431\u044b\u0442\u0438\u044f."
)
GREETING_RESPONSE = "\u041f\u0440\u0438\u0432\u0435\u0442! \u042f \u0432\u0430\u0448 \u0430\u0432\u0442\u043e\u043c\u043e\u0431\u0438\u043b\u044c\u043d\u044b\u0439 \u043f\u043e\u043c\u043e\u0449\u043d\u0438\u043a. \u0427\u0435\u043c \u043c\u043e\u0433\u0443 \u043f\u043e\u043c\u043e\u0447\u044c \u0441\u0435\u0433\u043e\u0434\u043d\u044f? \u042d\u043c\u043e\u0434\u0437\u0438 \u043f\u043e\u043c\u043e\u0433\u0443\u0442! \u0423\u0437\u043d\u0430\u044e\u0442\u0435 \u043e \u0432\u0430\u0448\u0435\u043c \u0430\u0432\u0442\u043e\u043c\u043e\u0431\u0438\u043b\u0435 \u0438 \u0438\u0441\u0442\u043e\u0440\u0438\u0438 \u0437\u0430\u043f\u0438\u0441\u0435\u0439."
THANKS_RESPONSE = "\u041f\u043e\u0436\u0430\u043b\u0443\u0439\u0441\u0442\u0430! \u0420\u0430\u0434 \u043f\u043e\u043c\u043e\u0447\u044c! \u0415\u0441\u043b\u0438 \u0431\u0443\u0434\u0443\u0442 \u0432\u043e\u043f\u0440\u043e\u0441\u044b \u043f\u043e \u0430\u0432\u0442\u043e\u043c\u043e\u0431\u0438\u043b\u044e \u2014 \u043e\u0431\u0440\u0430\u0449\u0430\u0439\u0442\u0435\u0441\u044c! \u042d\u043c\u043e\u0434\u0437\u0438 \u043f\u043e\u043c\u043e\u0433\u0443\u0442! \u0423\u0437\u043d\u0430\u044e\u0442 \u0432\u0430\u0448 \u0430\u0432\u0442\u043e\u043c\u043e\u0431\u0438\u043b\u044c \u0438 \u0438\u0441\u0442\u043e\u0440\u0438\u044e \u0437\u0430\u043f\u0438\u0441\u0435\u0439."


def _ru(*points: int) -> str:
    return "".join(chr(point) for point in points)


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


def _contains_any_event_keywords(message: str) -> bool:
    return (
        _contains_fuel_keywords(message)
        or _contains_repair_keywords(message)
        or _contains_trip_keywords(message)
        or _contains_issue_keywords(message)
    )


def _apply_guardrails(message: str, parsed_event: ParsedChatEvent) -> ParsedChatEvent:
    normalized_message = message.lower()

    if _is_greeting(normalized_message) and not _contains_any_event_keywords(
        normalized_message
    ):
        return ParsedChatEvent(
            type=None,
            description=None,
            amount=None,
            fuel_liters=None,
            mileage=None,
            needs_clarification=False,
            clarification_question=GREETING_RESPONSE,
        )

    if _is_thanks(normalized_message) and not _contains_any_event_keywords(
        normalized_message
    ):
        return ParsedChatEvent(
            type=None,
            description=None,
            amount=None,
            fuel_liters=None,
            mileage=None,
            needs_clarification=False,
            clarification_question=THANKS_RESPONSE,
        )

    if _contains_multiple_distinct_events(normalized_message):
        return ParsedChatEvent(
            needs_clarification=True,
            clarification_question=(
                "\u0423\u0442\u043e\u0447\u043d\u0438\u0442\u0435, \u043f\u043e\u0436\u0430\u043b\u0443\u0439\u0441\u0442\u0430, "
                "\u043e\u0434\u043d\u043e \u0441\u043e\u0431\u044b\u0442\u0438\u0435 \u0437\u0430 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435: "
                "\u044d\u0442\u043e \u0431\u044b\u043b\u0430 \u0437\u0430\u043f\u0440\u0430\u0432\u043a\u0430, \u0440\u0435\u043c\u043e\u043d\u0442, \u043f\u043e\u0435\u0437\u0434\u043a\u0430 "
                "\u0438\u043b\u0438 \u043f\u0440\u043e\u0431\u043b\u0435\u043c\u0430?"
            ),
        )

    fuel_liters = _extract_fuel_liters(normalized_message)
    if fuel_liters is not None and _looks_like_fuel_liters_message(normalized_message):
        return ParsedChatEvent(
            type="fuel",
            description=_fuel_description(fuel_liters),
            amount=_extract_money_amount(normalized_message),
            fuel_liters=fuel_liters,
            mileage=parsed_event.mileage,
            needs_clarification=False,
            clarification_question=None,
        )

    if _looks_like_issue_message(normalized_message):
        return ParsedChatEvent(
            type="issue",
            description=message.strip(),
            amount=None,
            fuel_liters=parsed_event.fuel_liters,
            mileage=parsed_event.mileage,
            needs_clarification=False,
            clarification_question=None,
        )

    if _looks_like_condition_message(normalized_message):
        return ParsedChatEvent(
            needs_clarification=True,
            clarification_question=NON_TIMELINE_CONDITION_QUESTION,
        )

    trip_distance_km = _extract_trip_distance_km(normalized_message)
    if trip_distance_km is not None:
        return ParsedChatEvent(
            type="trip",
            description=_trip_description(trip_distance_km),
            amount=None,
            fuel_liters=None,
            mileage=trip_distance_km,
            needs_clarification=False,
            clarification_question=None,
        )

    if _looks_like_trip_with_unclear_units(normalized_message):
        distance_match = re.search(r"\b(\d+)\b", normalized_message)
        distance_value = distance_match.group(1) if distance_match else None
        clarification_question = (
            f"\u0412\u044b \u0438\u043c\u0435\u0435\u0442\u0435 \u0432 \u0432\u0438\u0434\u0443 {distance_value} \u043a\u0438\u043b\u043e\u043c\u0435\u0442\u0440\u043e\u0432 \u0438\u043b\u0438 \u043c\u0438\u043b\u044c?"
            if distance_value
            else "\u0412\u044b \u0438\u043c\u0435\u0435\u0442\u0435 \u0432 \u0432\u0438\u0434\u0443 \u043a\u0438\u043b\u043e\u043c\u0435\u0442\u0440\u044b \u0438\u043b\u0438 \u043c\u0438\u043b\u0438?"
        )
        return ParsedChatEvent(
            type="trip",
            description=message.strip(),
            amount=None,
            fuel_liters=None,
            mileage=None,
            needs_clarification=True,
            clarification_question=clarification_question,
        )

    return parsed_event


def _format_decimal_ru(value: float | int) -> str:
    raw = f"{value:g}"
    return raw.replace(".", ",")


def _fuel_description(fuel_liters: float) -> str:
    return (
        _ru(0x417, 0x430, 0x43F, 0x440, 0x430, 0x432, 0x43A, 0x430)
        + " "
        + _ru(0x43D, 0x430)
        + f" {_format_decimal_ru(fuel_liters)} "
        + _ru(0x43B, 0x438, 0x442, 0x440, 0x43E, 0x432)
    )


def _trip_description(distance_km: float | int) -> str:
    return (
        _ru(0x41F, 0x43E, 0x435, 0x437, 0x434, 0x43A, 0x430)
        + " "
        + _ru(0x43D, 0x430)
        + f" {_format_decimal_ru(distance_km)} "
        + _ru(0x43A, 0x438, 0x43B, 0x43E, 0x43C, 0x435, 0x442, 0x440, 0x43E, 0x432)
    )


def _extract_fuel_liters(message: str) -> float | None:
    units = "|".join(
        re.escape(unit)
        for unit in (
            _ru(0x43B),
            _ru(0x43B) + ".",
            _ru(0x43B, 0x438, 0x442, 0x440),
            _ru(0x43B, 0x438, 0x442, 0x440, 0x430),
            _ru(0x43B, 0x438, 0x442, 0x440, 0x43E, 0x432),
        )
    )
    match = re.search(rf"\b(\d+(?:[.,]\d+)?)\s*(?:{units})\b", message)
    return float(match.group(1).replace(",", ".")) if match else None


def _extract_money_amount(message: str) -> float | int | None:
    units = "|".join(
        re.escape(unit)
        for unit in (
            "₽",
            _ru(0x440, 0x443, 0x431),
            _ru(0x440, 0x443, 0x431) + ".",
            _ru(0x440, 0x443, 0x431, 0x43B, 0x435, 0x439),
            _ru(0x440, 0x443, 0x431, 0x43B, 0x44F),
            _ru(0x440, 0x443, 0x431, 0x43B, 0x44C),
        )
    )
    match = re.search(rf"\b(\d+(?:[.,]\d+)?)\s*(?:{units})\b", message)
    if not match:
        return None
    raw = match.group(1).replace(",", ".")
    try:
        value = float(raw)
        return int(value) if value == int(value) else value
    except ValueError:
        return None


def _extract_trip_distance_km(message: str) -> int | None:
    trip_keywords = [
        _ru(0x43F, 0x440, 0x43E, 0x435, 0x445, 0x430, 0x43B),
        _ru(0x43F, 0x440, 0x43E, 0x435, 0x445, 0x430, 0x43B, 0x430),
        _ru(0x43F, 0x43E, 0x435, 0x437, 0x434, 0x43A),
        _ru(0x43F, 0x43E, 0x435, 0x437, 0x434, 0x438, 0x43B),
        _ru(0x43F, 0x43E, 0x435, 0x437, 0x434, 0x438, 0x43B, 0x430),
        _ru(0x435, 0x445, 0x430, 0x43B),
        _ru(0x435, 0x445, 0x430, 0x43B, 0x430),
        _ru(0x434, 0x43E, 0x435, 0x445, 0x430, 0x43B),
        _ru(0x434, 0x43E, 0x435, 0x445, 0x430, 0x43B, 0x430),
        _ru(0x441, 0x44A, 0x435, 0x437, 0x434, 0x438, 0x43B),
        _ru(0x441, 0x44A, 0x435, 0x437, 0x434, 0x438, 0x43B, 0x430),
        _ru(0x43C, 0x430, 0x440, 0x448, 0x440, 0x443, 0x442),
        _ru(0x434, 0x43E, 0x440, 0x43E, 0x433),
        _ru(0x43F, 0x443, 0x442, 0x44C),
    ]
    if not any(keyword in message for keyword in trip_keywords):
        return None
    if (
        _contains_fuel_keywords(message)
        or _contains_repair_keywords(message)
        or _contains_issue_keywords(message)
    ):
        return None

    units = "|".join(
        re.escape(unit)
        for unit in (
            _ru(0x43A, 0x43C),
            _ru(0x43A, 0x43C) + ".",
            _ru(0x43A, 0x438, 0x43B, 0x43E, 0x43C, 0x435, 0x442, 0x440),
            _ru(0x43A, 0x438, 0x43B, 0x43E, 0x43C, 0x435, 0x442, 0x440, 0x430),
            _ru(0x43A, 0x438, 0x43B, 0x43E, 0x43C, 0x435, 0x442, 0x440, 0x43E, 0x432),
        )
    )
    match = re.search(rf"\b(\d+(?:[.,]\d+)?)\s*(?:{units})\b", message)
    if not match:
        return None
    raw = match.group(1).replace(",", ".")
    try:
        value = float(raw)
        return int(value) if value == int(value) else value
    except ValueError:
        return None


def _looks_like_fuel_liters_message(message: str) -> bool:
    return (
        _extract_fuel_liters(message) is not None
        and _contains_fuel_keywords(message)
        and not _contains_repair_keywords(message)
        and not _contains_issue_keywords(message)
    )


def _contains_multiple_distinct_events(message: str) -> bool:
    return (
        sum(
            (
                _contains_fuel_keywords(message),
                _contains_repair_keywords(message),
                _contains_trip_keywords(message),
                _contains_issue_keywords(message),
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
        unit in message
        for unit in (
            _ru(0x43A, 0x43C),
            _ru(0x43A, 0x438, 0x43B, 0x43E, 0x43C),
            "mile",
            "miles",
            _ru(0x43C, 0x438, 0x43B, 0x44C),
        )
    )
    has_other_event = (
        _contains_fuel_keywords(message)
        or _contains_repair_keywords(message)
        or _contains_issue_keywords(message)
    )
    return has_trip and has_number and not has_known_unit and not has_other_event


def _contains_fuel_keywords(message: str) -> bool:
    return any(
        keyword in message
        for keyword in (
            _ru(0x437, 0x430, 0x43F, 0x440, 0x430, 0x432),
            _ru(0x442, 0x43E, 0x43F, 0x43B, 0x438, 0x432),
            _ru(0x431, 0x435, 0x43D, 0x437, 0x438, 0x43D),
            _ru(0x434, 0x438, 0x437, 0x435, 0x43B),
            _ru(0x430, 0x437, 0x441),
            _ru(0x437, 0x430, 0x43B, 0x438, 0x43B),
            _ru(0x437, 0x430, 0x43B, 0x438, 0x43B, 0x430),
        )
    )


def _contains_repair_keywords(message: str) -> bool:
    return any(
        keyword in message
        for keyword in (
            _ru(0x440, 0x435, 0x43C, 0x43E, 0x43D, 0x442),
            _ru(0x43F, 0x43E, 0x43C, 0x435, 0x43D, 0x44F, 0x43B),
            _ru(0x437, 0x430, 0x43C, 0x435, 0x43D, 0x438, 0x43B),
            _ru(0x437, 0x430, 0x43C, 0x435, 0x43D, 0x430),
            _ru(0x441, 0x435, 0x440, 0x432, 0x438, 0x441),
            " " + _ru(0x441, 0x442, 0x43E),
            _ru(0x442, 0x43E) + " ",
            _ru(0x43C, 0x430, 0x441, 0x43B, 0x43E),
        )
    )


def _contains_trip_keywords(message: str) -> bool:
    return any(
        keyword in message
        for keyword in (
            _ru(0x43F, 0x440, 0x43E, 0x435, 0x445, 0x430, 0x43B),
            _ru(0x43F, 0x43E, 0x435, 0x437, 0x434),
            _ru(0x435, 0x445, 0x430, 0x43B),
            _ru(0x434, 0x43E, 0x435, 0x445, 0x430, 0x43B),
            _ru(0x43C, 0x430, 0x440, 0x448, 0x440, 0x443, 0x442),
            _ru(0x43F, 0x443, 0x442, 0x44C),
            _ru(0x43F, 0x443, 0x442),
        )
    )


def _contains_issue_keywords(message: str) -> bool:
    return any(
        keyword in message
        for keyword in (
            _ru(0x447, 0x435, 0x43A),
            _ru(0x43E, 0x448, 0x438, 0x431, 0x43A),
            _ru(0x43D, 0x435) + " " + _ru(0x437, 0x430, 0x432, 0x43E, 0x434),
            _ru(0x437, 0x430, 0x433, 0x43E, 0x440, 0x435, 0x43B),
            _ru(0x43B, 0x430, 0x43C, 0x43F),
            _ru(0x441, 0x442, 0x443, 0x43A),
            _ru(0x441, 0x43A, 0x440, 0x438, 0x43F),
            _ru(0x43F, 0x440, 0x43E, 0x431, 0x43B, 0x435, 0x43C),
            _ru(0x43F, 0x43E, 0x43B, 0x43E, 0x43C),
            _ru(0x43D, 0x435)
            + " "
            + _ru(0x440, 0x430, 0x431, 0x43E, 0x442, 0x430, 0x435, 0x442),
            "warning",
            "fail",
        )
    )


def _contains_condition_keywords(message: str) -> bool:
    return any(
        keyword in message
        for keyword in (
            _ru(0x43F, 0x440, 0x43E, 0x432, 0x435, 0x440, 0x44C)
            + " "
            + _ru(0x441, 0x43E, 0x441, 0x442, 0x43E, 0x44F, 0x43D, 0x438, 0x435),
            _ru(0x43F, 0x440, 0x43E, 0x432, 0x435, 0x440, 0x438, 0x437, 0x446)
            + " "
            + _ru(0x441, 0x43E, 0x441, 0x442, 0x43E, 0x44F, 0x43D, 0x438, 0x435),
            _ru(0x43F, 0x440, 0x43E, 0x432, 0x435, 0x440, 0x438, 0x43B)
            + " "
            + _ru(0x441, 0x43E, 0x441, 0x442, 0x43E, 0x44F, 0x43D, 0x438, 0x435),
            _ru(
                0x442,
                0x435,
                0x445,
                0x43D,
                0x438,
                0x447,
                0x435,
                0x441,
                0x43A,
                0x43E,
                0x435,
            )
            + " "
            + _ru(0x441, 0x43E, 0x441, 0x442, 0x43E, 0x44F, 0x43D, 0x438, 0x435),
            _ru(0x441, 0x43E, 0x441, 0x442, 0x43E, 0x44F, 0x43D, 0x438, 0x435)
            + " "
            + _ru(0x430, 0x432, 0x442, 0x43E, 0x43C, 0x43E, 0x431, 0x438, 0x43B, 0x44F),
            _ru(0x441, 0x43E, 0x441, 0x442, 0x43E, 0x44F, 0x43D, 0x438, 0x435)
            + " "
            + _ru(0x43C, 0x430, 0x448, 0x438, 0x43D, 0x44B),
            _ru(0x443, 0x440, 0x43E, 0x432, 0x435, 0x43D, 0x44C)
            + " "
            + _ru(0x436, 0x438, 0x434, 0x43A, 0x43E, 0x441, 0x442, 0x438),
            _ru(0x434, 0x430, 0x432, 0x43B, 0x435, 0x43D, 0x438, 0x435)
            + " "
            + _ru(0x432)
            + " "
            + _ru(0x448, 0x438, 0x43D, 0x430, 0x445),
            _ru(0x43F, 0x43E, 0x43A, 0x430, 0x437, 0x430, 0x43D, 0x438, 0x44F)
            + " "
            + _ru(0x43E, 0x434, 0x43E, 0x43C, 0x435, 0x442, 0x440, 0x430),
        )
    )


def _is_greeting(message: str) -> bool:
    greetings = [
        "привет",
        "здравствуй",
        "здравствуйте",
        "добрый день",
        "добрый вечер",
        "доброе утро",
        "хай",
        "хей",
        "йо",
        "здорово",
        "салют",
        "дарова",
        "как сам",
        "как дела",
        "че как",
    ]
    return any(greeting in message for greeting in greetings)


def _is_thanks(message: str) -> bool:
    thanks = ["спасибо", "благодарю", "пасиб", "сенкс", "thanks"]
    return any(thank in message for thank in thanks)


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
