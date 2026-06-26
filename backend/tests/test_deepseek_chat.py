from __future__ import annotations

import json
from urllib.error import HTTPError, URLError
from io import BytesIO

import pytest

from app import deepseek_chat


@pytest.fixture(autouse=True)
def _clean_env(monkeypatch):
    monkeypatch.delenv("DEEPSEEK_API_KEY", raising=False)
    monkeypatch.delenv("DEEPSEEK_API_URL", raising=False)
    monkeypatch.delenv("DEEPSEEK_MODEL", raising=False)


def test_ask_deepseek_returns_missing_key_message(monkeypatch):
    result = deepseek_chat.ask_deepseek("Привет")
    assert "не настроен" in result
    assert "DEEPSEEK_API_KEY" in result


def test_ask_deepseek_calls_api_with_context(monkeypatch):
    monkeypatch.setenv("DEEPSEEK_API_KEY", "test-key")

    captured = {}

    def fake_urlopen(req, timeout=None):
        captured["url"] = req.full_url
        captured["headers"] = dict(req.header_items())
        body = json.loads(req.data.decode())
        captured["body"] = body

        response_data = {"choices": [{"message": {"content": "Ваш пробег 125000 км"}}]}
        return BytesIO(json.dumps(response_data).encode())

    monkeypatch.setattr("urllib.request.urlopen", fake_urlopen)

    result = deepseek_chat.ask_deepseek(
        "Какой пробег?",
        vehicle_context="Автомобиль: BMW M4, 2020 г., пробег 125000 км.",
    )

    assert result == "Ваш пробег 125000 км"
    assert "Authorization" in captured["headers"]
    assert captured["headers"]["Authorization"] == "Bearer test-key"
    assert captured["body"]["model"] == "deepseek-chat"
    assert any(m["role"] == "system" for m in captured["body"]["messages"])
    assert any(
        "пробег 125000" in m["content"]
        for m in captured["body"]["messages"]
        if m["role"] == "system"
    )
    assert captured["body"]["messages"][-1]["content"] == "Какой пробег?"


def test_ask_deepseek_without_context(monkeypatch):
    monkeypatch.setenv("DEEPSEEK_API_KEY", "test-key")

    captured = {}

    def fake_urlopen(req, timeout=None):
        captured["body"] = json.loads(req.data.decode())
        response_data = {"choices": [{"message": {"content": "Нет данных"}}]}
        return BytesIO(json.dumps(response_data).encode())

    monkeypatch.setattr("urllib.request.urlopen", fake_urlopen)

    result = deepseek_chat.ask_deepseek("Что-то")

    assert result == "Нет данных"
    user_messages = [m for m in captured["body"]["messages"] if m["role"] == "user"]
    assert len(user_messages) == 1
    system_messages = [m for m in captured["body"]["messages"] if m["role"] == "system"]
    assert len(system_messages) == 1


def test_ask_deepseek_handles_http_error(monkeypatch):
    monkeypatch.setenv("DEEPSEEK_API_KEY", "test-key")

    def fake_urlopen(req, timeout=None):
        raise HTTPError(
            req.full_url, 500, "Internal Server Error", {}, BytesIO(b"error")
        )

    monkeypatch.setattr("urllib.request.urlopen", fake_urlopen)

    result = deepseek_chat.ask_deepseek("Вопрос")
    assert "Не удалось" in result


def test_ask_deepseek_handles_unreachable_api(monkeypatch):
    monkeypatch.setenv("DEEPSEEK_API_KEY", "test-key")

    def fake_urlopen(req, timeout=None):
        raise URLError("Connection refused")

    monkeypatch.setattr("urllib.request.urlopen", fake_urlopen)

    result = deepseek_chat.ask_deepseek("Вопрос")
    assert "Не удалось" in result


def test_ask_deepseek_handles_empty_choices(monkeypatch):
    monkeypatch.setenv("DEEPSEEK_API_KEY", "test-key")

    def fake_urlopen(req, timeout=None):
        response_data = {"choices": []}
        return BytesIO(json.dumps(response_data).encode())

    monkeypatch.setattr("urllib.request.urlopen", fake_urlopen)

    result = deepseek_chat.ask_deepseek("Вопрос")
    assert "Не удалось" in result
