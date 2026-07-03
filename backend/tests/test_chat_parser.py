from app.chat_parser import _apply_guardrails
from app.schemas import ParsedChatEvent


def test_guardrails_force_issue_for_clear_issue_message() -> None:
    result = _apply_guardrails(
        "Загорелся чек двигателя",
        ParsedChatEvent(
            needs_clarification=True,
            clarification_question="Уточните тип события.",
        ),
    )

    assert result.type == "issue"
    assert result.description == "Загорелся чек двигателя"
    assert result.needs_clarification is False
    assert result.clarification_question is None


def test_guardrails_force_trip_clarification_for_unclear_units() -> None:
    result = _apply_guardrails(
        "Сегодня я проехал 1500",
        ParsedChatEvent(
            needs_clarification=True,
            clarification_question="Уточните тип события.",
        ),
    )

    assert result.type == "trip"
    assert result.needs_clarification is True
    assert "1500" in (result.clarification_question or "")


def test_guardrails_accept_trip_with_known_kilometer_units() -> None:
    result = _apply_guardrails(
        "поездка 100 километров",
        ParsedChatEvent(
            needs_clarification=True,
            clarification_question="Уточните тип события.",
        ),
    )

    assert result.type == "trip"
    assert result.description == "Поездка на 100 километров"
    assert result.amount is None
    assert result.mileage == 100
    assert result.needs_clarification is False
    assert result.clarification_question is None


def test_guardrails_accept_common_trip_phrases_with_known_units() -> None:
    messages = [
        "поездка на 100 километров",
        "проехал 100 км",
        "съездил 100 км",
    ]

    for message in messages:
        result = _apply_guardrails(
            message,
            ParsedChatEvent(
                needs_clarification=True,
                clarification_question="Уточните тип события.",
            ),
        )

        assert result.type == "trip"
        assert result.mileage == 100
        assert result.needs_clarification is False
        assert result.clarification_question is None


def test_guardrails_force_single_event_question_for_multiple_events() -> None:
    result = _apply_guardrails(
        "Заправился на 2500 и поменял масло за 8000",
        ParsedChatEvent(
            type="fuel",
            description="Заправка",
            amount=2500,
            mileage=None,
            needs_clarification=False,
            clarification_question=None,
        ),
    )

    assert result.needs_clarification is True
    assert result.type is None
    assert "одно событие" in (result.clarification_question or "")


def test_guardrails_recognize_technical_condition_update() -> None:
    result = _apply_guardrails(
        "Техническое состояние хорошее, пробег 125500",
        ParsedChatEvent(
            type="condition",
            description="Техническое состояние хорошее",
            amount=None,
            mileage=125500,
            needs_clarification=False,
            clarification_question=None,
        ),
    )

    assert result.type == "condition"
    assert result.description == "Техническое состояние хорошее, пробег 125500"
    assert result.mileage == 125500
    assert result.needs_clarification is False


def test_guardrails_parse_fuel_liters_without_amount() -> None:
    result = _apply_guardrails(
        "заправилась на 10 литров",
        ParsedChatEvent(
            needs_clarification=True,
            clarification_question="Уточните тип события.",
        ),
    )

    assert result.type == "fuel"
    assert result.description == "Заправка на 10 литров"
    assert result.amount is None
    assert getattr(result, "fuel_liters", None) == 10
    assert result.mileage is None
    assert result.needs_clarification is False
    assert result.clarification_question is None


def test_guardrails_parse_fuel_liters_and_amount_separately() -> None:
    result = _apply_guardrails(
        "заправилась на 10 литров за 1000 рублей",
        ParsedChatEvent(
            needs_clarification=True,
            clarification_question="Уточните тип события.",
        ),
    )

    assert result.type == "fuel"
    assert result.description == "Заправка на 10 литров"
    assert result.amount == 1000
    assert getattr(result, "fuel_liters", None) == 10
    assert result.mileage is None
    assert result.needs_clarification is False
    assert result.clarification_question is None
