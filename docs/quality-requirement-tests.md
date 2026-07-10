# Quality Requirement Tests

This document is the canonical detailed QRT artifact. It maps each quality requirement to the automated test or CI check that verifies it.

For MVP v3 / Sprint 4, new QRTs are added only when an implemented or newly important product area introduces a measurable quality scenario with direct automated evidence.

## Contents

- [Evidence Types](#evidence-types)
- [QRT-001: Vehicle event data integrity](#qrt-001-vehicle-event-data-integrity)
- [QRT-002: Timeline API response time](#qrt-002-timeline-api-response-time)
- [QRT-003: Backend regression testability](#qrt-003-backend-regression-testability)
- [QRT-004: Assistant context correctness](#qrt-004-assistant-context-correctness)
- [QRT-005: Statistics calculation correctness](#qrt-005-statistics-calculation-correctness)
- [QRT-006: Critical-module coverage gate](#qrt-006-critical-module-coverage-gate)
- [QRT-007: Secure password storage](#qrt-007-secure-password-storage)
- [QRT-008: Login and chat request-rate protection](#qrt-008-login-and-chat-request-rate-protection)
- [MVP v3 / Sprint 4 QRT Note](#mvp-v3--sprint-4-qrt-note)

## Evidence Types

| Evidence type | What it means | Can it count as QRT? |
|---|---|---|
| Quality requirement | Measurable non-functional or product-quality requirement with QR-NNN ID. | No; it is the requirement being verified. |
| QRT | Automated test or CI check with QRT-NNN ID that directly verifies a measurable QR scenario. | Yes. |
| Unit test | Automated test for isolated product logic or a small module. | Only if linked to a measurable QR scenario. |
| Integration test | Automated test for interaction between product components, such as API plus persistence or UI component plus state/API boundary. | Only if linked to a measurable QR scenario. |
| UAT | Customer-executed end-user scenario. | No. |
| Manual evidence | Observation, review, screenshot, or exploratory check. | No. |

## QRT-001: Vehicle event data integrity

**Linked quality requirement:** [QR-001](quality-requirements.md#qr-001-vehicle-event-data-integrity)

**Verification method:** Automated FastAPI integration tests using `TestClient` and SQLite-backed test persistence.

**Test data, setup, or environment:** Standard backend test environment. Each test creates or uses an isolated user, records event state before an invalid request, sends an invalid `POST /events` request, and verifies that no new event is persisted.

**Automated command or CI check:** `python -m pytest tests/test_quality_requirements.py` in the `Backend CI` workflow.

**Expected measurable result:** Invalid event type, empty description, negative amount, negative mileage, and unknown `user_id` request classes are rejected with the expected status and persist no new event record for 100% of the tested invalid-request classes.

**Evidence location:** `backend/tests/test_quality_requirements.py`; `.github/workflows/backend-ci.yml`; latest protected default-branch Backend CI run linked from `docs/testing.md`.

## QRT-002: Timeline API response time

**Linked quality requirement:** [QR-002](quality-requirements.md#qr-002-timeline-api-response-time)

**Verification method:** Automated FastAPI response-time integration test using `TestClient`.

**Test data, setup, or environment:** Standard backend test environment. The test creates one user with 20 vehicle events and measures wall-clock time for `GET /events`.

**Automated command or CI check:** `python -m pytest tests/test_quality_requirements.py` in the `Backend CI` workflow.

**Expected measurable result:** The tested `GET /events` request returns HTTP 200 in under 2 seconds for the documented 20-event dataset.

**Evidence location:** `backend/tests/test_quality_requirements.py::test_get_events_responds_within_2_seconds`; `.github/workflows/backend-ci.yml`; latest protected default-branch Backend CI run linked from `docs/testing.md`.

## QRT-003: Backend regression testability

**Linked quality requirement:** [QR-003](quality-requirements.md#qr-003-backend-regression-testability)

**Verification method:** Automated CI execution of the full backend pytest suite through the Backend CI coverage step.

**Test data, setup, or environment:** Standard backend CI environment on pull requests and protected default-branch updates, with dependencies installed from `backend/requirements.txt`.

**Automated command or CI check:** `python -m coverage run -m pytest tests` in the `Backend CI` workflow.

**Expected measurable result:** The backend pytest suite completes with 0 failures in the CI run.

**Evidence location:** `.github/workflows/backend-ci.yml`; backend test files under `backend/tests/`; protected default-branch Backend CI run linked from `docs/testing.md`.

## QRT-004: Assistant context correctness

**Linked quality requirement:** [QR-004](quality-requirements.md#qr-004-assistant-context-correctness)

**Verification method:** Automated FastAPI integration tests with a monkeypatched AI adapter.

**Test data, setup, or environment:** Standard backend test environment. The tests create users, vehicles, and event histories, replace `ask_deepseek` with a test double, and inspect the backend-composed `vehicle_context` passed to the AI adapter.

**Automated command or CI check:** `python -m pytest tests/test_chat_ask.py` and the full backend pytest suite in the `Backend CI` workflow.

**Expected measurable result:** The backend passes the saved vehicle profile, omits default placeholder vehicle text when no real vehicle data exists, and limits the forwarded event history to no more than the 50 most recent events for 100% of the tested scenarios.

**Evidence location:** `backend/tests/test_chat_ask.py`; `.github/workflows/backend-ci.yml`; latest local or CI backend pytest output recorded in `docs/testing.md`.

## QRT-005: Statistics calculation correctness

**Linked quality requirement:** [QR-005](quality-requirements.md#qr-005-statistics-calculation-correctness)

**Verification method:** Automated FastAPI integration tests plus isolated statistics helper tests.

**Test data, setup, or environment:** Standard backend test environment. The tests create users, vehicles, and combinations of fuel, repair, trip, and issue events, including period-filtered data and trip-distance delta cases, then assert exact response values from `GET /stats`.

**Automated command or CI check:** `python -m pytest tests/test_stats.py` and the full backend pytest suite in the `Backend CI` workflow.

**Expected measurable result:** The tested scenarios return exact expected values for mileage, fuel expenses, repair expenses, total expenses, trip counts, records count, and fuel liters in 100% of the tested scenarios.

**Evidence location:** `backend/tests/test_stats.py`; `.github/workflows/backend-ci.yml`; latest local or CI backend pytest output recorded in `docs/testing.md`.

## QRT-006: Critical-module coverage gate

**Linked quality requirement:** [QR-006](quality-requirements.md#qr-006-critical-module-coverage-gate)

**Verification method:** Automated CI coverage gate.

**Test data, setup, or environment:** Standard backend CI environment with coverage collected from the full backend pytest suite.

**Automated command or CI check:** `python -m coverage run -m pytest tests` followed by `python -m coverage report --include="app/*" --fail-under=30` in the `Backend CI` workflow.

**Expected measurable result:** The coverage gate exits successfully with the documented 30% threshold, and the coverage report shows the critical backend modules at or above their documented line-coverage expectation.

**Evidence location:** `.github/workflows/backend-ci.yml`; coverage output for `app/*`; critical-module table in `docs/testing.md`.

## QRT-007: Secure password storage

**Linked quality requirement:** [QR-007](quality-requirements.md#qr-007-secure-password-storage)

**Verification method:** Automated FastAPI authentication integration tests using `TestClient` and SQLite-backed test persistence.

**Test data, setup, or environment:** Standard backend test environment. The tests register a new user, inspect the persisted user row, verify that the stored value differs from the submitted password and has the repository's password-hash prefix, then verify successful login with the correct password and failed login with an incorrect password or removed demo credentials.

**Automated command or CI check:** `docker compose run --rm backend pytest tests/test_auth.py -q`; also covered by `python -m coverage run -m pytest tests` in the `Backend CI` workflow.

**Expected measurable result:** The tested registration stores a non-plaintext salted password hash, correct-password login succeeds, wrong-password login fails, removed demo credentials fail, and the command exits with 0 failures.

**Evidence location:** `backend/tests/test_auth.py`; `backend/app/security.py`; `backend/app/main.py`; `.github/workflows/backend-ci.yml`; latest local or CI backend pytest output recorded in `docs/testing.md`.

**Limitations:** The test verifies password hashing behavior and login semantics. It does not independently audit cryptographic strength beyond the implemented salted hash format.

## QRT-008: Login and chat request-rate protection

**Linked quality requirement:** [QR-008](quality-requirements.md#qr-008-login-and-chat-request-rate-protection)

**Verification method:** Automated FastAPI integration tests for login and chat endpoint rate limiting plus isolated fixed-window limiter behavior through the request flow.

**Test data, setup, or environment:** Standard backend test environment. Tests send repeated requests with stable `X-Forwarded-For` client identifiers so the limiter key is deterministic. Login uses nonexistent credentials for the first five allowed attempts. Chat creates a test user, replaces the external AI call with a test double, sends twenty allowed chat requests, and then sends one additional request.

**Automated command or CI check:** `docker compose run --rm backend pytest tests/test_auth.py tests/test_rate_limiting.py tests/test_chat_ask.py -q`; also covered by `python -m coverage run -m pytest tests` in the `Backend CI` workflow.

**Expected measurable result:** The sixth login request from the same client address returns HTTP 429 with `Too many login attempts`, the twenty-first chat request from the same client address returns HTTP 429 with `Too many chat requests`, and the command exits with 0 failures.

**Evidence location:** `backend/tests/test_auth.py`; `backend/tests/test_rate_limiting.py`; `backend/tests/test_chat_ask.py`; `backend/app/rate_limit.py`; `backend/app/main.py`; `.github/workflows/backend-ci.yml`; latest local or CI backend pytest output recorded in `docs/testing.md`.

**Limitations:** The route-level tests verify configured request counts and HTTP 429 behavior. Window expiration is supported by `FixedWindowRateLimiter.allow` pruning old attempts, but current route tests do not sleep for 60 seconds or monkeypatch monotonic time to prove end-to-end recovery after window expiry.

## MVP v3 / Sprint 4 QRT Note

The repository now contains implemented password hashing and request-rate limiting behavior, so QRT-007 and QRT-008 are documented as active automated evidence rather than future placeholders.

CORS behavior is covered by `backend/tests/test_cors.py` as automated regression evidence. It is not documented as a separate QRT because the current automated tests verify default denied-origin behavior but do not yet verify an explicitly allowed origin configured through `CORS_ALLOWED_ORIGINS`.

US-08 maintenance recommendations and US-09 notifications still do not have dedicated implementation behavior in the repository. Add future QRTs for them only when recommendation or notification behavior introduces a measurable quality scenario with direct automated verification.
