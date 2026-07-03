from app.chat_parser import _apply_guardrails
from app.schemas import ParsedChatEvent


def test_guardrails_force_issue_for_clear_issue_message() -> None:
    message = "\u0417\u0430\u0433\u043e\u0440\u0435\u043b\u0441\u044f \u0447\u0435\u043a \u0434\u0432\u0438\u0433\u0430\u0442\u0435\u043b\u044f"
    result = _apply_guardrails(
        message,
        ParsedChatEvent(
            needs_clarification=True,
            clarification_question="\u0423\u0442\u043e\u0447\u043d\u0438\u0442\u0435 \u0442\u0438\u043f \u0441\u043e\u0431\u044b\u0442\u0438\u044f.",
        ),
    )

    assert result.type == "issue"
    assert result.description == message
    assert result.needs_clarification is False
    assert result.clarification_question is None


def test_guardrails_force_trip_clarification_for_unclear_units() -> None:
    result = _apply_guardrails(
        "\u0421\u0435\u0433\u043e\u0434\u043d\u044f \u044f \u043f\u0440\u043e\u0435\u0445\u0430\u043b 1500",
        ParsedChatEvent(
            needs_clarification=True,
            clarification_question="\u0423\u0442\u043e\u0447\u043d\u0438\u0442\u0435 \u0442\u0438\u043f \u0441\u043e\u0431\u044b\u0442\u0438\u044f.",
        ),
    )

    assert result.type == "trip"
    assert result.needs_clarification is True
    assert "1500" in (result.clarification_question or "")


def test_guardrails_accept_trip_with_known_kilometer_units() -> None:
    result = _apply_guardrails(
        "\u043f\u043e\u0435\u0437\u0434\u043a\u0430 100 \u043a\u0438\u043b\u043e\u043c\u0435\u0442\u0440\u043e\u0432",
        ParsedChatEvent(
            needs_clarification=True,
            clarification_question="\u0423\u0442\u043e\u0447\u043d\u0438\u0442\u0435 \u0442\u0438\u043f \u0441\u043e\u0431\u044b\u0442\u0438\u044f.",
        ),
    )

    assert result.type == "trip"
    assert (
        result.description
        == "\u041f\u043e\u0435\u0437\u0434\u043a\u0430 \u043d\u0430 100 \u043a\u0438\u043b\u043e\u043c\u0435\u0442\u0440\u043e\u0432"
    )
    assert result.amount is None
    assert result.mileage == 100
    assert result.needs_clarification is False
    assert result.clarification_question is None


def test_guardrails_accept_common_trip_phrases_with_known_units() -> None:
    messages = [
        "\u043f\u043e\u0435\u0437\u0434\u043a\u0430 \u043d\u0430 100 \u043a\u0438\u043b\u043e\u043c\u0435\u0442\u0440\u043e\u0432",
        "\u043f\u0440\u043e\u0435\u0445\u0430\u043b 100 \u043a\u043c",
        "\u0441\u044a\u0435\u0437\u0434\u0438\u043b 100 \u043a\u043c",
    ]

    for message in messages:
        result = _apply_guardrails(
            message,
            ParsedChatEvent(
                needs_clarification=True,
                clarification_question="\u0423\u0442\u043e\u0447\u043d\u0438\u0442\u0435 \u0442\u0438\u043f \u0441\u043e\u0431\u044b\u0442\u0438\u044f.",
            ),
        )

        assert result.type == "trip"
        assert result.mileage == 100
        assert result.needs_clarification is False
        assert result.clarification_question is None


def test_guardrails_force_single_event_question_for_multiple_events() -> None:
    result = _apply_guardrails(
        "\u0417\u0430\u043f\u0440\u0430\u0432\u0438\u043b\u0441\u044f \u043d\u0430 2500 \u0438 \u043f\u043e\u043c\u0435\u043d\u044f\u043b \u043c\u0430\u0441\u043b\u043e \u0437\u0430 8000",
        ParsedChatEvent(
            type="fuel",
            description="\u0417\u0430\u043f\u0440\u0430\u0432\u043a\u0430",
            amount=2500,
            mileage=None,
            needs_clarification=False,
            clarification_question=None,
        ),
    )

    assert result.needs_clarification is True
    assert result.type is None
    assert result.clarification_question is not None


def test_guardrails_do_not_create_timeline_event_for_condition_request() -> None:
    result = _apply_guardrails(
        "\u041f\u0440\u043e\u0432\u0435\u0440\u044c \u0441\u043e\u0441\u0442\u043e\u044f\u043d\u0438\u0435 \u043c\u0430\u0448\u0438\u043d\u044b",
        ParsedChatEvent(
            needs_clarification=True,
            clarification_question="\u0423\u0442\u043e\u0447\u043d\u0438\u0442\u0435 \u0442\u0438\u043f \u0441\u043e\u0431\u044b\u0442\u0438\u044f.",
        ),
    )

    assert result.type is None
    assert result.needs_clarification is True
    assert result.clarification_question is not None


def test_guardrails_parse_fuel_liters_without_amount() -> None:
    result = _apply_guardrails(
        "\u0437\u0430\u043f\u0440\u0430\u0432\u0438\u043b\u0430\u0441\u044c \u043d\u0430 10 \u043b\u0438\u0442\u0440\u043e\u0432",
        ParsedChatEvent(
            needs_clarification=True,
            clarification_question="\u0423\u0442\u043e\u0447\u043d\u0438\u0442\u0435 \u0442\u0438\u043f \u0441\u043e\u0431\u044b\u0442\u0438\u044f.",
        ),
    )

    assert result.type == "fuel"
    assert (
        result.description
        == "\u0417\u0430\u043f\u0440\u0430\u0432\u043a\u0430 \u043d\u0430 10 \u043b\u0438\u0442\u0440\u043e\u0432"
    )
    assert result.amount is None
    assert getattr(result, "fuel_liters", None) == 10
    assert result.mileage is None
    assert result.needs_clarification is False
    assert result.clarification_question is None


def test_guardrails_parse_fuel_liters_and_amount_separately() -> None:
    result = _apply_guardrails(
        "\u0437\u0430\u043f\u0440\u0430\u0432\u0438\u043b\u0430\u0441\u044c \u043d\u0430 10 \u043b\u0438\u0442\u0440\u043e\u0432 \u0437\u0430 1000 \u0440\u0443\u0431\u043b\u0435\u0439",
        ParsedChatEvent(
            needs_clarification=True,
            clarification_question="\u0423\u0442\u043e\u0447\u043d\u0438\u0442\u0435 \u0442\u0438\u043f \u0441\u043e\u0431\u044b\u0442\u0438\u044f.",
        ),
    )

    assert result.type == "fuel"
    assert (
        result.description
        == "\u0417\u0430\u043f\u0440\u0430\u0432\u043a\u0430 \u043d\u0430 10 \u043b\u0438\u0442\u0440\u043e\u0432"
    )
    assert result.amount == 1000
    assert getattr(result, "fuel_liters", None) == 10
    assert result.mileage is None
    assert result.needs_clarification is False
    assert result.clarification_question is None
