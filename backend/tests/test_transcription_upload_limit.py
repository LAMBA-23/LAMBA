from __future__ import annotations

import asyncio

import pytest
from fastapi import HTTPException

from app.main import _read_audio_with_limit


class ChunkedAudio:
    def __init__(self) -> None:
        self.chunks = [b"abc", b"def", b"ignored"]
        self.read_sizes: list[int] = []

    async def read(self, size: int) -> bytes:
        self.read_sizes.append(size)
        return self.chunks.pop(0) if self.chunks else b""


def test_chunked_audio_reader_stops_when_configured_limit_is_exceeded():
    audio = ChunkedAudio()

    with pytest.raises(HTTPException) as exc_info:
        asyncio.run(_read_audio_with_limit(audio, max_bytes=5))

    assert exc_info.value.status_code == 413
    assert exc_info.value.detail == "Audio file exceeds the configured 5-byte limit"
    assert len(audio.read_sizes) == 2
