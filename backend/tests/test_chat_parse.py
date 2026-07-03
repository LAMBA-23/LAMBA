import importlib
import os
import sys
from pathlib import Path

from fastapi.testclient import TestClient


TEST_DB_PATH = Path(__file__).resolve().parent / "test_chat_parse.db"
os.environ["DATABASE_URL"] = f"sqlite:///{TEST_DB_PATH.as_posix()}"

for module_name in ["app.main", "app.database", "app.models", "app.chat_parser"]:
    sys.modules.pop(module_name, None)

main_module = importlib.import_module("app.main")
database_module = importlib.import_module("app.database")
schemas_module = importlib.import_module("app.schemas")

database_module.Base.metadata.create_all(bind=database_module.engine)

ParsedChatEvent = schemas_module.ParsedChatEvent
client = TestClient(main_module.app)


def teardown_module() -> None:
    client.close()
    database_module.engine.dispose()
    if TEST_DB_PATH.exists():
        TEST_DB_PATH.unlink()


def test_parse_event_returns_structured_payload(monkeypatch) -> None:
    def fake_parser(_: str) -> ParsedChatEvent:
        return ParsedChatEvent(
            type="fuel",
            description="Заправка на 2500 рублей",
            amount=2500,
            fuel_liters=None,
            mileage=125300,
            needs_clarification=False,
            clarification_question=None,
        )

    monkeypatch.setattr(main_module, "parse_chat_message", fake_parser)

    response = client.post(
        "/chat/parse-event",
        json={"message": "Заправился на 2500 рублей, пробег 125300"},
    )

    assert response.status_code == 200
    assert response.json() == {
        "status": "parsed",
        "parsed_event": {
            "type": "fuel",
            "description": "Заправка на 2500 рублей",
            "amount": 2500,
            "fuel_liters": None,
            "mileage": 125300,
        },
        "clarification_question": None,
    }


def test_parse_event_returns_fuel_liters(monkeypatch) -> None:
    def fake_parser(_: str) -> ParsedChatEvent:
        return ParsedChatEvent(
            type="fuel",
            description="Заправка на 10 литров",
            amount=None,
            fuel_liters=10,
            mileage=None,
            needs_clarification=False,
            clarification_question=None,
        )

    monkeypatch.setattr(main_module, "parse_chat_message", fake_parser)

    response = client.post(
        "/chat/parse-event",
        json={"message": "заправилась на 10 литров"},
    )

    assert response.status_code == 200
    assert response.json() == {
        "status": "parsed",
        "parsed_event": {
            "type": "fuel",
            "description": "Заправка на 10 литров",
            "amount": None,
            "fuel_liters": 10,
            "mileage": None,
        },
        "clarification_question": None,
    }


def test_parse_event_returns_clarification(monkeypatch) -> None:
    def fake_parser(_: str) -> ParsedChatEvent:
        return ParsedChatEvent(
            type=None,
            description=None,
            amount=None,
            mileage=None,
            needs_clarification=True,
            clarification_question="Вы имеете в виду 1500 километров пробега?",
        )

    monkeypatch.setattr(main_module, "parse_chat_message", fake_parser)

    response = client.post(
        "/chat/parse-event",
        json={"message": "Сегодня я проехал 1500"},
    )

    assert response.status_code == 200
    assert response.json() == {
        "status": "clarification_needed",
        "parsed_event": None,
        "clarification_question": "Вы имеете в виду 1500 километров пробега?",
    }


def test_parse_event_rejects_invalid_negative_amount(monkeypatch) -> None:
    def fake_parser(_: str) -> ParsedChatEvent:
        return ParsedChatEvent(
            type="repair",
            description="Замена масла",
            amount=-1,
            mileage=125300,
            needs_clarification=False,
            clarification_question=None,
        )

    monkeypatch.setattr(main_module, "parse_chat_message", fake_parser)

    response = client.post(
        "/chat/parse-event",
        json={"message": "Замена масла"},
    )

    assert response.status_code == 200
    assert response.json()["status"] == "clarification_needed"
    assert response.json()["parsed_event"] is None


def test_parse_event_accepts_technical_condition_update(monkeypatch) -> None:
    def fake_parser(_: str) -> ParsedChatEvent:
        return ParsedChatEvent(
            type="condition",
            description="Техническое состояние хорошее",
            amount=None,
            fuel_liters=None,
            mileage=125500,
            needs_clarification=False,
            clarification_question=None,
        )

    monkeypatch.setattr(main_module, "parse_chat_message", fake_parser)

    response = client.post(
        "/chat/parse-event",
        json={"message": "Техническое состояние хорошее, пробег 125500"},
    )

    assert response.status_code == 200
    assert response.json()["parsed_event"] == {
        "type": "condition",
        "description": "Техническое состояние хорошее",
        "amount": None,
        "fuel_liters": None,
        "mileage": 125500,
    }


def test_parsed_event_is_saved_for_user_and_visible_in_timeline(monkeypatch) -> None:
    def fake_parser(_: str) -> ParsedChatEvent:
        return ParsedChatEvent(
            type="condition",
            description="Техническое состояние хорошее",
            amount=None,
            fuel_liters=None,
            mileage=125500,
            needs_clarification=False,
            clarification_question=None,
        )

    monkeypatch.setattr(main_module, "parse_chat_message", fake_parser)
    user_id = client.post(
        "/auth/register",
        json={"username": "us04-user", "password": "password123"},
    ).json()["user_id"]

    parse_response = client.post(
        "/chat/parse-event",
        json={"message": "Техническое состояние хорошее, пробег 125500"},
    )
    parsed_event = parse_response.json()["parsed_event"]
    create_response = client.post(
        f"/events?user_id={user_id}",
        json=parsed_event,
    )
    timeline_response = client.get(f"/events?user_id={user_id}")

    assert create_response.status_code == 200
    assert create_response.json()["type"] == "condition"
    assert timeline_response.status_code == 200
    assert timeline_response.json() == [create_response.json()]
