from __future__ import annotations

import time
from collections import defaultdict, deque
from threading import Lock


class FixedWindowRateLimiter:
    def __init__(self) -> None:
        self._events: dict[str, deque[float]] = defaultdict(deque)
        self._lock = Lock()

    def reset(self) -> None:
        with self._lock:
            self._events.clear()

    def allow(self, key: str, *, limit: int, window_seconds: int) -> bool:
        now = time.monotonic()
        window_start = now - window_seconds

        with self._lock:
            attempts = self._events[key]
            while attempts and attempts[0] <= window_start:
                attempts.popleft()

            if len(attempts) >= limit:
                return False

            attempts.append(now)
            return True
