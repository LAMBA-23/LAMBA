# Voice Input Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` to implement task-by-task.

**Goal:** Add microphone-based chat input transcribed and conservatively cleaned by Mistral.

**Architecture:** Android records an app-private M4A file and uploads it to a new multipart backend endpoint. A backend-only Mistral client rotates configured keys after quota/rate-limit responses, transcribes the file, cleans the transcript, and returns text for insertion into the existing input field.

**Tech Stack:** Kotlin/Android `MediaRecorder`, Retrofit multipart, FastAPI, HTTPX, pytest.

## Global Constraints

- Keys are supplied only as comma-separated `MISTRAL_API_KEYS`; never log or commit them.
- The transcript is never auto-sent or persisted.
- Typed chat remains enabled while voice transcription is pending.
- No app-defined duration limit, streaming, playback, or audio storage.

### Task 1: Backend Mistral boundary and endpoint

**Files:** Create `backend/app/mistral_transcription.py`, `backend/tests/test_mistral_transcription.py`; modify `backend/app/main.py`, `backend/app/schemas.py`, `backend/requirements.txt`, `docker-compose.yml`.

- [ ] Write failing unit tests for ordered-key parsing, rotation after status 429, and a cleanup fallback to the raw transcript.
- [ ] Run `python -m pytest tests/test_mistral_transcription.py -q`; verify each fails because the module is absent.
- [ ] Implement a small injected-transport client: `transcribe(audio, filename, content_type) -> str` and `clean(text) -> str`; retry only rate-limit/quota provider responses.
- [ ] Run the focused tests and verify success.
- [ ] Write failing endpoint tests for successful multipart upload, missing file, empty file, and all keys exhausted.
- [ ] Implement `POST /chat/transcribe?user_id=` returning `ChatTranscriptionResponse(text: str)`, including authentication, validation, and stable HTTP errors.
- [ ] Run focused backend tests and then `python -m ruff check app tests` and `python -m ruff format --check app tests`.

### Task 2: Android voice flow

**Files:** Modify `app/src/main/AndroidManifest.xml`, `app/src/main/res/layout/activity_chat.xml`, `app/src/main/java/com/lamba/app/ChatActivity.kt`, `app/src/main/java/com/lamba/app/network/LambaApiService.kt`, `app/src/main/java/com/lamba/app/network/Models.kt`; create focused JVM-testable state/request helper and its test.

- [ ] Write a failing JVM test for start/stop/pending/error transitions and transcript insertion behavior.
- [ ] Run `gradlew.bat testDebugUnitTest --tests <new-test>`; verify the helper is absent.
- [ ] Implement the minimal state helper and make the test pass.
- [ ] Add `RECORD_AUDIO`, runtime permission handling, M4A recording and cleanup, microphone button/status, and Retrofit multipart request.
- [ ] Ensure pending state disables only the microphone, and every error restores it while keeping typed text available.
- [ ] Run `gradlew.bat testDebugUnitTest` and `gradlew.bat assembleDebug`.

### Task 3: Contract and completion verification

**Files:** Modify `docs/api-contract.md`, `README.md`, `docs/testing.md`, `docs/user-acceptance-tests.md`, `CHANGELOG.md` only after user approval for customer-facing documentation.

- [ ] Document the endpoint, non-secret environment variable names, expected failure behavior, and manual microphone verification.
- [ ] Run `git diff --check` and relevant link checks if links change.
- [ ] Run backend, Android, and diff-hygiene checks; report exact results without commit, push, or PR creation.
