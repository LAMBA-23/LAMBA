from __future__ import annotations

import json
from io import BytesIO
from urllib.error import HTTPError, URLError

import pytest

from app import deepseek_chat


@pytest.fixture(autouse=True)
def _clean_env(monkeypatch):
    monkeypatch.delenv("TIMEWEB_API_KEY", raising=False)
    monkeypatch.delenv("TIMEWEB_AGENT_ID", raising=False)
    monkeypatch.delenv("TIMEWEB_MODEL", raising=False)


def test_ask_deepseek_returns_missing_key_message(monkeypatch):
    result = deepseek_chat.ask_deepseek("Привет")
    assert "не настроен" in result
    assert "TIMEWEB_API_KEY" in result
    assert "TIMEWEB_AGENT_ID" in result


def test_ask_deepseek_calls_api_with_context(monkeypatch):
    monkeypatch.setenv("TIMEWEB_API_KEY", "test-key")
    monkeypatch.setenv("TIMEWEB_AGENT_ID", "agent-123")

    captured = {}

    def fake_urlopen(req, timeout=None):
        captured["url"] = req.full_url
        captured["headers"] = dict(req.header_items())
        captured["body"] = json.loads(req.data.decode())

        response_data = {"choices": [{"message": {"content": "Ваш пробег 125000 км"}}]}
        return BytesIO(json.dumps(response_data).encode())

    monkeypatch.setattr("urllib.request.urlopen", fake_urlopen)

    result = deepseek_chat.ask_deepseek(
        "Какой пробег?",
        vehicle_context="Автомобиль: BMW M4, 2020 г., пробег 125000 км.",
    )

    assert result == "Ваш пробег 125000 км"
    assert captured["url"].endswith("/agents/agent-123/v1/chat/completions")
    assert captured["headers"]["Authorization"] == "Bearer test-key"
    assert captured["body"]["model"] == "deepseek-chat"
    assert any(
        "пробег 125000" in message["content"]
        for message in captured["body"]["messages"]
        if message["role"] == "system"
    )
    assert captured["body"]["messages"][-1]["content"] == "Какой пробег?"


def test_ask_deepseek_without_context(monkeypatch):
    monkeypatch.setenv("TIMEWEB_API_KEY", "test-key")
    monkeypatch.setenv("TIMEWEB_AGENT_ID", "agent-123")

    captured = {}

    def fake_urlopen(req, timeout=None):
        captured["body"] = json.loads(req.data.decode())
        response_data = {"choices": [{"message": {"content": "Нет данных"}}]}
        return BytesIO(json.dumps(response_data).encode())

    monkeypatch.setattr("urllib.request.urlopen", fake_urlopen)

    result = deepseek_chat.ask_deepseek("Что-то")

    assert result == "Нет данных"
    user_messages = [m for m in captured["body"]["messages"] if m["role"] == "user"]
    system_messages = [m for m in captured["body"]["messages"] if m["role"] == "system"]
    assert len(user_messages) == 1
    assert len(system_messages) == 1


def test_ask_deepseek_handles_http_error(monkeypatch):
    monkeypatch.setenv("TIMEWEB_API_KEY", "test-key")
    monkeypatch.setenv("TIMEWEB_AGENT_ID", "agent-123")

    def fake_urlopen(req, timeout=None):
        raise HTTPError(
            req.full_url, 500, "Internal Server Error", {}, BytesIO(b"error")
        )

    monkeypatch.setattr("urllib.request.urlopen", fake_urlopen)

    result = deepseek_chat.ask_deepseek("Вопрос")
    assert "Не удалось" in result


def test_ask_deepseek_handles_unreachable_api(monkeypatch):
    monkeypatch.setenv("TIMEWEB_API_KEY", "test-key")
    monkeypatch.setenv("TIMEWEB_AGENT_ID", "agent-123")

    def fake_urlopen(req, timeout=None):
        raise URLError("Connection refused")

    monkeypatch.setattr("urllib.request.urlopen", fake_urlopen)

    result = deepseek_chat.ask_deepseek("Вопрос")
    assert "Не удалось" in result


def test_ask_deepseek_handles_empty_choices(monkeypatch):
    monkeypatch.setenv("TIMEWEB_API_KEY", "test-key")
    monkeypatch.setenv("TIMEWEB_AGENT_ID", "agent-123")

    def fake_urlopen(req, timeout=None):
        response_data = {"choices": []}
        return BytesIO(json.dumps(response_data).encode())

    monkeypatch.setattr("urllib.request.urlopen", fake_urlopen)

    result = deepseek_chat.ask_deepseek("Вопрос")
    assert "Не удалось" in result
