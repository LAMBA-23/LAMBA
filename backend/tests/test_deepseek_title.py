from __future__ import annotations

from urllib.error import URLError

import pytest

from app import deepseek_chat


@pytest.fixture(autouse=True)
def _clean_env(monkeypatch):
    monkeypatch.delenv("TIMEWEB_API_KEY", raising=False)
    monkeypatch.delenv("TIMEWEB_AGENT_ID", raising=False)
    monkeypatch.delenv("TIMEWEB_MODEL", raising=False)


def test_generate_chat_title_returns_fallback_without_configuration():
    result = deepseek_chat.generate_chat_title(
        "Show me service expenses for this month",
        "Here are the service expenses.",
    )

    assert result == "Show me service expenses for this month"


def test_generate_chat_title_trims_failed_api_fallback(monkeypatch):
    monkeypatch.setenv("TIMEWEB_API_KEY", "test-key")
    monkeypatch.setenv("TIMEWEB_AGENT_ID", "agent-123")

    def fake_urlopen(req, timeout=None):
        raise URLError("Connection refused")

    monkeypatch.setattr("urllib.request.urlopen", fake_urlopen)

    result = deepseek_chat.generate_chat_title(
        "  This is a very long first user message that must be shortened for title fallback  ",
        "Assistant reply",
    )

    assert result == "This is a very long first user message that must b"
    assert len(result) == 50
