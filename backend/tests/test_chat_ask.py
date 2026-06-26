from __future__ import annotations

import importlib
import os
import sys
from pathlib import Path

from fastapi.testclient import TestClient


TEST_DB_PATH = Path(__file__).resolve().parent / "test_chat_ask.db"
os.environ["DATABASE_URL"] = f"sqlite:///{TEST_DB_PATH.as_posix()}"
os.environ.pop("DEEPSEEK_API_KEY", None)

for module_name in ["app.main", "app.database", "app.models", "app.deepseek_chat"]:
    sys.modules.pop(module_name, None)

main_module = importlib.import_module("app.main")
database_module = importlib.import_module("app.database")

database_module.Base.metadata.create_all(bind=database_module.engine)

client = TestClient(main_module.app)


def teardown_module() -> None:
    client.close()
    database_module.engine.dispose()
    if TEST_DB_PATH.exists():
        TEST_DB_PATH.unlink()


def _register_and_get_user_id(username: str = "ask-user") -> int:
    resp = client.post(
        "/auth/register",
        json={"username": username, "password": "password123"},
    )
    return resp.json()["user_id"]


def test_chat_ask_returns_answer(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-1")

    def fake_ask(message: str, vehicle_context: str | None = None) -> str:
        return f"Ответ на: {message}"

    monkeypatch.setattr(main_module, "ask_deepseek", fake_ask)

    resp = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Какой пробег?"},
    )

    assert resp.status_code == 200
    data = resp.json()
    assert "answer" in data
    assert data["answer"] == "Ответ на: Какой пробег?"


def test_chat_ask_includes_vehicle_context(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-2")

    client.post(
        "/vehicle",
        json={
            "user_id": user_id,
            "brand": "Toyota",
            "model": "Camry",
            "production_year": 2023,
            "current_mileage": 50000,
        },
    )
    client.post(
        f"/events?user_id={user_id}",
        json={"type": "fuel", "description": "Заправка", "amount": 3000, "mileage": 50000},
    )

    captured = {}

    def fake_ask(message: str, vehicle_context: str | None = None) -> str:
        captured["context"] = vehicle_context
        return "OK"

    monkeypatch.setattr(main_module, "ask_deepseek", fake_ask)

    resp = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Сколько потратил на топливо?"},
    )

    assert resp.status_code == 200
    ctx = captured["context"]
    assert "Toyota" in ctx
    assert "Camry" in ctx
    assert "50000" in ctx
    assert "Заправка" in ctx


def test_chat_ask_with_no_events(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-3")

    captured = {}

    def fake_ask(message: str, vehicle_context: str | None = None) -> str:
        captured["context"] = vehicle_context
        return "Нет данных"

    monkeypatch.setattr(main_module, "ask_deepseek", fake_ask)

    resp = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Что было в истории?"},
    )

    assert resp.status_code == 200
    ctx = captured["context"]
    assert "Not set" not in ctx


def test_chat_ask_limits_context_to_50_events(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-limit")

    client.post(
        "/vehicle",
        json={
            "user_id": user_id,
            "brand": "BMW",
            "model": "M4",
            "production_year": 2020,
            "current_mileage": 100000,
        },
    )
    for i in range(60):
        client.post(
            f"/events?user_id={user_id}",
            json={"type": "fuel", "description": f"Заправка {i}", "amount": 1000 + i, "mileage": 100000 + i},
        )

    captured = {}

    def fake_ask(message: str, vehicle_context: str | None = None) -> str:
        captured["context"] = vehicle_context
        return "OK"

    monkeypatch.setattr(main_module, "ask_deepseek", fake_ask)

    resp = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Какие последние заправки?"},
    )

    assert resp.status_code == 200
    ctx = captured["context"]
    assert "Заправка 0" not in ctx
    assert "Заправка 9" not in ctx
    assert "Заправка 59" in ctx
    assert "Заправка 10" in ctx


def test_chat_ask_missing_user_id():
    resp = client.post(
        "/chat/ask",
        json={"message": "Вопрос"},
    )
    assert resp.status_code == 422


def test_chat_ask_missing_message():
    resp = client.post(
        "/chat/ask?user_id=1",
        json={},
    )
    assert resp.status_code == 422


def test_chat_ask_nonexistent_user(monkeypatch):
    def fake_ask(message: str, vehicle_context: str | None = None) -> str:
        return "OK"

    monkeypatch.setattr(main_module, "ask_deepseek", fake_ask)

    resp = client.post(
        "/chat/ask?user_id=99999",
        json={"message": "Вопрос"},
    )
    assert resp.status_code == 404
