# Quality Requirement Tests

This document is the canonical detailed QRT artifact. It maps each quality requirement to the automated test or CI check that verifies it.

For MVP v2 / Sprint 3, Assignment 5 does not require adding a fixed number of new QRTs. New QRTs are added here only when an implemented or newly important product area introduces a measurable quality scenario with direct automated evidence.

## Contents

- [Evidence Types](#evidence-types)
- [QRT-001: Vehicle event data integrity](#qrt-001-vehicle-event-data-integrity)
- [QRT-002: Timeline API response time](#qrt-002-timeline-api-response-time)
- [QRT-003: Backend regression testability](#qrt-003-backend-regression-testability)
- [QRT-004: Assistant context correctness](#qrt-004-assistant-context-correctness)
- [QRT-005: Statistics calculation correctness](#qrt-005-statistics-calculation-correctness)
- [QRT-006: Critical-module coverage gate](#qrt-006-critical-module-coverage-gate)
- [MVP v2 / Sprint 3 QRT Note](#mvp-v2--sprint-3-qrt-note)

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

## MVP v2 / Sprint 3 QRT Note

The repository now contains implemented manual event, assistant, and statistics behavior, so QRT-004 through QRT-006 are documented as active automated evidence rather than future placeholders.

US-08 maintenance recommendations and US-09 notifications still do not have dedicated implementation behavior in the repository. Add future QRTs for them only when recommendation or notification behavior introduces a measurable quality scenario with direct automated verification.
