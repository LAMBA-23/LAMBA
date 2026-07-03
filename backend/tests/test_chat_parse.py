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
            description="Fuel refill",
            amount=2500,
            fuel_liters=None,
            mileage=125300,
            needs_clarification=False,
            clarification_question=None,
        )

    monkeypatch.setattr(main_module, "parse_chat_message", fake_parser)

    response = client.post(
        "/chat/parse-event",
        json={"message": "Refilled fuel for 2500"},
    )

    assert response.status_code == 200
    assert response.json() == {
        "status": "parsed",
        "parsed_event": {
            "type": "fuel",
            "description": "Fuel refill",
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
            description="Р—Р°РїСЂР°РІРєР° РЅР° 10 Р»РёС‚СЂРѕРІ",
            amount=None,
            fuel_liters=10,
            mileage=None,
            needs_clarification=False,
            clarification_question=None,
        )

    monkeypatch.setattr(main_module, "parse_chat_message", fake_parser)

    response = client.post(
        "/chat/parse-event",
        json={"message": "Р·Р°РїСЂР°РІРёР»Р°СЃСЊ РЅР° 10 Р»РёС‚СЂРѕРІ"},
    )

    assert response.status_code == 200
    assert response.json() == {
        "status": "parsed",
        "parsed_event": {
            "type": "fuel",
            "description": "Р—Р°РїСЂР°РІРєР° РЅР° 10 Р»РёС‚СЂРѕРІ",
            "amount": None,
            "fuel_liters": 10,
            "mileage": None,
        },
        "clarification_question": None,
    }


def test_parse_event_returns_clarification(monkeypatch) -> None:
    question = "Please clarify the vehicle event details."

    def fake_parser(_: str) -> ParsedChatEvent:
        return ParsedChatEvent(
            type=None,
            description=None,
            amount=None,
            mileage=None,
            needs_clarification=True,
            clarification_question=question,
        )

    monkeypatch.setattr(main_module, "parse_chat_message", fake_parser)

    response = client.post(
        "/chat/parse-event",
        json={"message": "Spent 3000"},
    )

    assert response.status_code == 200
    assert response.json() == {
        "status": "clarification_needed",
        "parsed_event": None,
        "clarification_question": question,
    }


def test_parse_event_rejects_invalid_negative_amount(monkeypatch) -> None:
    def fake_parser(_: str) -> ParsedChatEvent:
        return ParsedChatEvent(
            type="repair",
            description="Oil change",
            amount=-1,
            mileage=125300,
            needs_clarification=False,
            clarification_question=None,
        )

    monkeypatch.setattr(main_module, "parse_chat_message", fake_parser)

    response = client.post(
        "/chat/parse-event",
        json={"message": "Changed oil"},
    )

    assert response.status_code == 200
    assert response.json()["status"] == "clarification_needed"
    assert response.json()["parsed_event"] is None


def test_parse_event_does_not_return_condition_as_timeline_event(monkeypatch) -> None:
    question = "This is an assistant request, not a timeline event."

    def fake_parser(_: str) -> ParsedChatEvent:
        return ParsedChatEvent(
            needs_clarification=True,
            clarification_question=question,
        )

    monkeypatch.setattr(main_module, "parse_chat_message", fake_parser)

    response = client.post(
        "/chat/parse-event",
        json={"message": "Check vehicle condition"},
    )

    assert response.status_code == 200
    assert response.json()["status"] == "clarification_needed"
    assert response.json()["parsed_event"] is None
    assert response.json()["clarification_question"] == question


def test_condition_event_is_rejected_and_not_visible_in_timeline() -> None:
    user_id = client.post(
        "/auth/register",
        json={"username": "us04-user", "password": "password123"},
    ).json()["user_id"]

    create_response = client.post(
        f"/events?user_id={user_id}",
        json={
            "type": "condition",
            "description": "Technical condition is good",
        },
    )
    timeline_response = client.get(f"/events?user_id={user_id}")

    assert create_response.status_code == 422
    assert timeline_response.status_code == 200
    assert timeline_response.json() == []
