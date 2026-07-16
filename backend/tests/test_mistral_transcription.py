from __future__ import annotations

import pytest

from app import mistral_transcription


def test_transcribe_audio_rotates_keys_only_after_rate_limit(monkeypatch):
    attempted_keys: list[str] = []

    def fake_transcribe(key: str, **_: object) -> str:
        attempted_keys.append(key)
        if key == "first":
            raise mistral_transcription.MistralRequestError(status_code=429)
        return "сырой текст"

    monkeypatch.setenv("MISTRAL_API_KEYS", " first, second ")
    monkeypatch.setattr(mistral_transcription, "_transcribe_with_key", fake_transcribe)
    monkeypatch.setattr(
        mistral_transcription, "_cleanup_with_key", lambda key, text: "Чистый текст"
    )

    assert (
        mistral_transcription.transcribe_audio(b"audio", "voice.ogg", "audio/ogg")
        == "Чистый текст"
    )
    assert attempted_keys == ["first", "second"]


def test_transcribe_audio_does_not_rotate_key_after_non_quota_error(monkeypatch):
    attempted_keys: list[str] = []

    def fake_transcribe(key: str, **_: object) -> str:
        attempted_keys.append(key)
        raise mistral_transcription.MistralRequestError(status_code=500)

    monkeypatch.setenv("MISTRAL_API_KEYS", "first,second")
    monkeypatch.setattr(mistral_transcription, "_transcribe_with_key", fake_transcribe)

    with pytest.raises(mistral_transcription.MistralRequestError):
        mistral_transcription.transcribe_audio(b"audio", "voice.ogg", "audio/ogg")

    assert attempted_keys == ["first"]


def test_transcribe_audio_rotates_keys_after_recognizable_quota_error(monkeypatch):
    attempted_keys: list[str] = []

    def fake_transcribe(key: str, **_: object) -> str:
        attempted_keys.append(key)
        if key == "first":
            raise mistral_transcription.MistralRequestError(
                status_code=400, message="quota exceeded"
            )
        return "сырой текст"

    monkeypatch.setenv("MISTRAL_API_KEYS", "first,second")
    monkeypatch.setattr(mistral_transcription, "_transcribe_with_key", fake_transcribe)
    monkeypatch.setattr(
        mistral_transcription, "_cleanup_with_key", lambda key, text: text
    )

    assert mistral_transcription.transcribe_audio(b"audio", "voice.ogg", "audio/ogg")
    assert attempted_keys == ["first", "second"]


def test_transcribe_audio_returns_raw_transcript_when_cleanup_fails(monkeypatch):
    monkeypatch.setenv("MISTRAL_API_KEYS", "first")
    monkeypatch.setattr(
        mistral_transcription, "_transcribe_with_key", lambda **_: "  сырой текст  "
    )
    monkeypatch.setattr(
        mistral_transcription,
        "_cleanup_with_key",
        lambda *_: (_ for _ in ()).throw(RuntimeError("cleanup unavailable")),
    )

    assert (
        mistral_transcription.transcribe_audio(b"audio", "voice.ogg", "audio/ogg")
        == "сырой текст"
    )


def test_transcribe_audio_rotates_keys_when_cleanup_is_rate_limited(monkeypatch):
    cleanup_keys: list[str] = []
    monkeypatch.setenv("MISTRAL_API_KEYS", "first,second")
    monkeypatch.setattr(
        mistral_transcription, "_transcribe_with_key", lambda **_: "сырой текст"
    )

    def fake_cleanup(key: str, transcript: str) -> str:
        cleanup_keys.append(key)
        if key == "first":
            raise mistral_transcription.MistralRequestError(status_code=429)
        return "чистый текст"

    monkeypatch.setattr(mistral_transcription, "_cleanup_with_key", fake_cleanup)

    assert (
        mistral_transcription.transcribe_audio(b"audio", "voice.ogg", "audio/ogg")
        == "чистый текст"
    )
    assert cleanup_keys == ["first", "second"]


def test_transcribe_audio_has_stable_error_when_all_keys_are_rate_limited(monkeypatch):
    monkeypatch.setenv("MISTRAL_API_KEYS", "first,second")
    monkeypatch.setattr(
        mistral_transcription,
        "_transcribe_with_key",
        lambda **_: (_ for _ in ()).throw(
            mistral_transcription.MistralRequestError(status_code=429)
        ),
    )

    with pytest.raises(mistral_transcription.TranscriptionKeysExhausted) as exc_info:
        mistral_transcription.transcribe_audio(b"audio", "voice.ogg", "audio/ogg")

    assert str(exc_info.value) == mistral_transcription.ALL_KEYS_EXHAUSTED_ERROR
