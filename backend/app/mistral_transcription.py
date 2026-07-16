from __future__ import annotations

import os
from io import BytesIO
from typing import Any

from mistralai import Mistral

MISTRAL_API_KEYS_ENV = "MISTRAL_API_KEYS"
MISTRAL_TRANSCRIPTION_MODEL = os.getenv(
    "MISTRAL_TRANSCRIPTION_MODEL", "voxtral-mini-latest"
)
MISTRAL_CLEANUP_MODEL = os.getenv("MISTRAL_CLEANUP_MODEL", "mistral-small-latest")
ALL_KEYS_EXHAUSTED_ERROR = "All Mistral API keys are currently exhausted"

_CLEANUP_SYSTEM_PROMPT = (
    "Correct only obvious speech-recognition errors: punctuation, capitalization, "
    "and typos. Remove filler words, hesitation sounds, and accidental repetitions. "
    "Do not add facts or change meaning; return only the cleaned text in its original language."
)


class MistralRequestError(RuntimeError):
    def __init__(self, status_code: int | None = None, message: str = "") -> None:
        self.status_code = status_code
        self.message = message
        super().__init__(message or f"Mistral request failed with status {status_code}")


class TranscriptionKeysExhausted(RuntimeError):
    def __init__(self) -> None:
        super().__init__(ALL_KEYS_EXHAUSTED_ERROR)


def transcribe_audio(audio: bytes, filename: str, content_type: str | None) -> str:
    keys = _configured_api_keys()
    if not keys:
        raise TranscriptionKeysExhausted()

    for key in keys:
        try:
            transcript = _transcribe_with_key(
                key=key,
                audio=audio,
                filename=filename,
                content_type=content_type,
            ).strip()
        except MistralRequestError as exc:
            if _is_quota_error(exc):
                continue
            raise

        if not transcript:
            return transcript

        return _cleanup_transcript(keys, keys.index(key), transcript)

    raise TranscriptionKeysExhausted()


def _configured_api_keys() -> list[str]:
    return [
        key.strip()
        for key in os.getenv(MISTRAL_API_KEYS_ENV, "").split(",")
        if key.strip()
    ]


def _cleanup_transcript(keys: list[str], start_index: int, transcript: str) -> str:
    for key in keys[start_index:]:
        try:
            cleaned = _cleanup_with_key(key, transcript).strip()
        except MistralRequestError as exc:
            if _is_quota_error(exc):
                continue
            return transcript
        except Exception:
            return transcript
        return cleaned or transcript
    raise TranscriptionKeysExhausted()


def _transcribe_with_key(
    key: str, audio: bytes, filename: str, content_type: str | None
) -> str:
    file = BytesIO(audio)
    file.name = filename or "audio"
    if content_type:
        file.content_type = content_type
    try:
        response = Mistral(api_key=key).audio.transcriptions.complete(
            model=MISTRAL_TRANSCRIPTION_MODEL,
            file=file,
        )
    except Exception as exc:
        raise _as_mistral_request_error(exc) from exc
    text = getattr(response, "text", None)
    if not isinstance(text, str):
        raise MistralRequestError()
    return text


def _cleanup_with_key(key: str, transcript: str) -> str:
    try:
        response = Mistral(api_key=key).chat.complete(
            model=MISTRAL_CLEANUP_MODEL,
            temperature=0,
            messages=[
                {"role": "system", "content": _CLEANUP_SYSTEM_PROMPT},
                {"role": "user", "content": transcript},
            ],
        )
    except Exception as exc:
        raise _as_mistral_request_error(exc) from exc
    return _chat_content(response)


def _chat_content(response: Any) -> str:
    choices = getattr(response, "choices", None)
    if not choices:
        raise MistralRequestError()
    content = getattr(getattr(choices[0], "message", None), "content", None)
    if isinstance(content, str):
        return content
    raise MistralRequestError()


def _as_mistral_request_error(exc: Exception) -> MistralRequestError:
    return MistralRequestError(
        status_code=getattr(exc, "status_code", None), message=str(exc)
    )


def _is_quota_error(exc: MistralRequestError) -> bool:
    return exc.status_code == 429 or "quota" in exc.message.lower()
