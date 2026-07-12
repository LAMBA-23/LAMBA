from __future__ import annotations

import importlib
import os
import sys
from pathlib import Path

from fastapi.testclient import TestClient


TEST_DB_PATH = Path(__file__).resolve().parent / "test_chat_title_and_context.db"
os.environ["DATABASE_URL"] = f"sqlite:///{TEST_DB_PATH.as_posix()}"
os.environ.pop("TIMEWEB_API_KEY", None)
os.environ.pop("TIMEWEB_AGENT_ID", None)

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


def _register_and_get_user_id(username: str) -> int:
    response = client.post(
        "/auth/register",
        json={"username": username, "password": "password123"},
    )
    return response.json()["user_id"]


def test_chat_ask_includes_selected_chat_context(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-chat-context-1")
    captured = {}

    def fake_ask(
        message: str,
        vehicle_context: str | None = None,
        chat_context: str | None = None,
    ) -> str:
        captured["message"] = message
        captured["vehicle_context"] = vehicle_context
        captured["chat_context"] = chat_context
        return "OK"

    monkeypatch.setattr(main_module, "ask_deepseek", fake_ask)

    response = client.post(
        f"/chat/ask?user_id={user_id}",
        json={
            "message": "А что дальше?",
            "chat_context": [
                {"sender": "user", "text": "Привет"},
                {"sender": "assistant", "text": "Здравствуйте"},
                {"sender": "user", "text": "Покажи расходы"},
                {"sender": "assistant", "text": "Вот последние расходы"},
            ],
        },
    )

    assert response.status_code == 200
    assert captured["message"] == "А что дальше?"
    assert captured["chat_context"] is not None
    assert "user: Привет" in captured["chat_context"]
    assert "assistant: Вот последние расходы" in captured["chat_context"]


def test_chat_title_endpoint_returns_generated_title(monkeypatch):
    user_id = _register_and_get_user_id("ask-user-chat-title-1")

    def fake_generate(first_user_message: str, first_assistant_reply: str) -> str:
        assert first_user_message == "Покажи последние расходы"
        assert first_assistant_reply == "Вот последние расходы за месяц."
        return "Последние расходы"

    monkeypatch.setattr(main_module, "generate_chat_title", fake_generate)

    response = client.post(
        f"/chat/title?user_id={user_id}",
        json={
            "first_user_message": "Покажи последние расходы",
            "first_assistant_reply": "Вот последние расходы за месяц.",
        },
    )

    assert response.status_code == 200
    assert response.json() == {"title": "Последние расходы"}
