from __future__ import annotations

import importlib
import os
import sys
from pathlib import Path

from fastapi.testclient import TestClient


TEST_DB_PATH = Path(__file__).resolve().parent / "test_rate_limiting.db"
os.environ["DATABASE_URL"] = f"sqlite:///{TEST_DB_PATH.as_posix()}"
os.environ.pop("TIMEWEB_API_KEY", None)
os.environ.pop("TIMEWEB_AGENT_ID", None)

for module_name in ["app.main", "app.database", "app.models", "app.deepseek_chat"]:
    sys.modules.pop(module_name, None)

main_module = importlib.import_module("app.main")
database_module = importlib.import_module("app.database")
rate_limit_module = importlib.import_module("app.rate_limit")

database_module.Base.metadata.create_all(bind=database_module.engine)

client = TestClient(main_module.app)


def teardown_module() -> None:
    client.close()
    database_module.engine.dispose()
    if TEST_DB_PATH.exists():
        TEST_DB_PATH.unlink()


def _register_and_get_user_id(username: str = "rate-limit-user") -> int:
    resp = client.post(
        "/auth/register",
        json={"username": username, "password": "password123"},
    )
    return resp.json()["user_id"]


def test_chat_ask_is_rate_limited(monkeypatch):
    user_id = _register_and_get_user_id("rate-limit-user-chat")

    def fake_ask(message: str, vehicle_context: str | None = None, style: str | None = None) -> str:
        return "OK"

    monkeypatch.setattr(main_module, "ask_deepseek", fake_ask)

    for _ in range(20):
        response = client.post(
            f"/chat/ask?user_id={user_id}",
            json={"message": "Р’РѕРїСЂРѕСЃ"},
            headers={"X-Forwarded-For": "203.0.113.20"},
        )
        assert response.status_code == 200
        assert response.json()["answer"] == "OK"

    rate_limited_response = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Р’РѕРїСЂРѕСЃ"},
        headers={"X-Forwarded-For": "203.0.113.20"},
    )

    assert rate_limited_response.status_code == 429
    assert rate_limited_response.json()["detail"] == "Too many chat requests"


def test_chat_ask_allows_requests_after_rate_limit_window(monkeypatch):
    current_time = 1000.0

    def fake_monotonic() -> float:
        return current_time

    monkeypatch.setattr(rate_limit_module.time, "monotonic", fake_monotonic)

    user_id = _register_and_get_user_id("rate-limit-user-chat-window")

    def fake_ask(message: str, vehicle_context: str | None = None, style: str | None = None) -> str:
        return "OK"

    monkeypatch.setattr(main_module, "ask_deepseek", fake_ask)

    headers = {"X-Forwarded-For": "203.0.113.21"}

    for _ in range(20):
        response = client.post(
            f"/chat/ask?user_id={user_id}",
            json={"message": "status"},
            headers=headers,
        )
        assert response.status_code == 200

    rate_limited_response = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "status"},
        headers=headers,
    )
    assert rate_limited_response.status_code == 429

    current_time = 1061.0

    recovered_response = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "status"},
        headers=headers,
    )
    assert recovered_response.status_code == 200
    assert recovered_response.json()["answer"] == "OK"
