from __future__ import annotations


def _register_and_get_user_id(client, username: str) -> int:
    response = client.post(
        "/auth/register", json={"username": username, "password": "password123"}
    )
    return response.json()["user_id"]


def test_chat_transcribe_returns_cleaned_transcript_for_existing_user(
    client, monkeypatch
):
    user_id = _register_and_get_user_id(client, "transcribe-user")
    captured: dict[str, object] = {}

    def fake_transcribe(audio: bytes, filename: str, content_type: str | None) -> str:
        captured.update(audio=audio, filename=filename, content_type=content_type)
        return "Заправился на 30 литров"

    monkeypatch.setattr(client.app.state, "transcribe_audio", fake_transcribe)

    response = client.post(
        f"/chat/transcribe?user_id={user_id}",
        files={"audio": ("voice.ogg", b"audio-bytes", "audio/ogg")},
    )

    assert response.status_code == 200
    assert response.json() == {"text": "Заправился на 30 литров"}
    assert captured == {
        "audio": b"audio-bytes",
        "filename": "voice.ogg",
        "content_type": "audio/ogg",
    }


def test_chat_transcribe_rejects_empty_audio(client):
    user_id = _register_and_get_user_id(client, "empty-transcribe-user")

    response = client.post(
        f"/chat/transcribe?user_id={user_id}",
        files={"audio": ("voice.ogg", b"", "audio/ogg")},
    )

    assert response.status_code == 400
    assert response.json()["detail"] == "Audio file must not be empty"


def test_chat_transcribe_requires_audio_file(client):
    user_id = _register_and_get_user_id(client, "missing-transcribe-user")

    response = client.post(f"/chat/transcribe?user_id={user_id}")

    assert response.status_code == 400
    assert response.json()["detail"] == "Audio file is required"


def test_chat_transcribe_rejects_audio_larger_than_configured_limit(client):
    user_id = _register_and_get_user_id(client, "oversized-transcribe-user")

    response = client.post(
        f"/chat/transcribe?user_id={user_id}",
        files={"audio": ("voice.ogg", b"x" * (5 * 1024 * 1024 + 1), "audio/ogg")},
    )

    assert response.status_code == 413
    assert response.json()["detail"] == "Audio file exceeds the configured 5 MB limit"


def test_chat_transcribe_is_rate_limited_by_client_ip(client, monkeypatch):
    user_id = _register_and_get_user_id(client, "rate-limited-transcribe-user")
    monkeypatch.setattr(client.app.state, "transcribe_audio", lambda *_: "text")
    headers = {"X-Forwarded-For": "203.0.113.30"}

    for _ in range(10):
        response = client.post(
            f"/chat/transcribe?user_id={user_id}",
            files={"audio": ("voice.ogg", b"audio", "audio/ogg")},
            headers=headers,
        )
        assert response.status_code == 200

    response = client.post(
        f"/chat/transcribe?user_id={user_id}",
        files={"audio": ("voice.ogg", b"audio", "audio/ogg")},
        headers=headers,
    )

    assert response.status_code == 429
    assert response.json()["detail"] == "Too many transcription requests"


def test_chat_transcribe_returns_stable_error_when_keys_are_exhausted(
    client, monkeypatch
):
    from app.mistral_transcription import TranscriptionKeysExhausted

    user_id = _register_and_get_user_id(client, "exhausted-transcribe-user")

    def fail_transcription(*_: object) -> str:
        raise TranscriptionKeysExhausted()

    monkeypatch.setattr(client.app.state, "transcribe_audio", fail_transcription)
    response = client.post(
        f"/chat/transcribe?user_id={user_id}",
        files={"audio": ("voice.ogg", b"audio", "audio/ogg")},
    )

    assert response.status_code == 503
    assert response.json()["detail"] == "All Mistral API keys are currently exhausted"
