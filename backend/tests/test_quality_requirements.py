from __future__ import annotations

import importlib
import os
import sys
import time
from pathlib import Path

from fastapi.testclient import TestClient


TEST_DB_PATH = Path(__file__).resolve().parent / "test_quality_requirements.db"
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


def _register_user(username: str = "qrt-user") -> int:
    resp = client.post(
        "/auth/register",
        json={"username": username, "password": "password123"},
    )
    return resp.json()["user_id"]


def test_chat_ask_responds_within_timeout(monkeypatch):
    """QRT-01: Performance Efficiency — Time Behaviour.

    The /chat/ask endpoint must respond within 30 seconds when the
    external API responds promptly.
    """
    user_id = _register_user("qrt-perf-user")

    def fake_ask(message: str, vehicle_context: str | None = None) -> str:
        return "Ответ"

    monkeypatch.setattr(main_module, "ask_deepseek", fake_ask)

    start = time.monotonic()
    resp = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Тест скорости"},
    )
    elapsed = time.monotonic() - start

    assert resp.status_code == 200
    assert elapsed < 30, f"Response took {elapsed:.2f}s, exceeds 30s threshold"


def test_chat_ask_returns_fallback_on_api_failure(monkeypatch):
    """QRT-02: Reliability — Fault Tolerance.

    When the external AI API fails, the endpoint must return HTTP 200
    with a fallback answer, not a 500 error.
    """
    user_id = _register_user("qrt-fault-user")

    def failing_ask(message: str, vehicle_context: str | None = None) -> str:
        raise ValueError("DeepSeek API error: 500")

    monkeypatch.setattr(main_module, "ask_deepseek", failing_ask)

    resp = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Тест отказоустойчивости"},
    )

    assert resp.status_code == 200
    data = resp.json()
    assert "answer" in data
    assert len(data["answer"]) > 0


def test_api_key_not_exposed_in_chat_ask_response(monkeypatch):
    """QRT-03: Security — Confidentiality.

    The DEEPSEEK_API_KEY must never appear in any response body,
    including error/fallback paths.
    """
    test_key = "sk-secret-key-abc123xyz"
    monkeypatch.setenv("DEEPSEEK_API_KEY", test_key)
    user_id = _register_user("qrt-sec-user")

    def fake_ask(message: str, vehicle_context: str | None = None) -> str:
        return "Безопасный ответ"

    monkeypatch.setattr(main_module, "ask_deepseek", fake_ask)

    resp = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Тест безопасности"},
    )

    assert resp.status_code == 200
    assert test_key not in resp.json().get("answer", "")

    def failing_ask(message: str, vehicle_context: str | None = None) -> str:
        raise ValueError(f"API error with key {test_key} in message")

    monkeypatch.setattr(main_module, "ask_deepseek", failing_ask)

    resp_fallback = client.post(
        f"/chat/ask?user_id={user_id}",
        json={"message": "Ещё один тест"},
    )

    assert resp_fallback.status_code == 200
    assert test_key not in resp_fallback.json().get("answer", "")
