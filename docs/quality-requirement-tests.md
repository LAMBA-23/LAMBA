# Quality Requirement Tests

This document is the canonical detailed QRT artifact. It maps each quality requirement to the automated test or CI check that verifies it.

For MVP v2 / Sprint 3, Assignment 5 does not require adding a fixed number of new QRTs. New QRTs should be added when changed or newly important product areas introduce measurable quality requirements that are not already covered here.

## Contents

- [Evidence Types](#evidence-types)
- [QRT-001: Vehicle event data integrity](#qrt-001-vehicle-event-data-integrity)
- [QRT-002: Timeline API response time](#qrt-002-timeline-api-response-time)
- [QRT-003: Backend regression testability](#qrt-003-backend-regression-testability)
- [MVP v2 / Sprint 3 QRT follow-up](#mvp-v2--sprint-3-qrt-follow-up)

## Evidence Types

| Evidence type | What it means | Can it count as QRT? |
|---|---|---|
| Quality requirement | Measurable non-functional product requirement with QR-NNN ID. | No; it is the requirement being verified. |
| QRT | Automated test or CI check with QRT-NNN ID that directly verifies a measurable QR scenario. | Yes. |
| Unit test | Automated test for isolated product logic or a small module. | Only if linked to a measurable QR scenario. |
| Integration test | Automated test for interaction between product components, such as API plus persistence or UI component plus state/API boundary. | Only if linked to a measurable QR scenario. |
| UAT | Customer-executed end-user scenario. | No. |
| Manual evidence | Observation, review, screenshot, or exploratory check. | No. |

## QRT-001: Vehicle event data integrity

**Linked quality requirement:** [QR-001](quality-requirements.md#qr-001-vehicle-event-data-integrity)

**Verification method:** Automated FastAPI tests using `TestClient` and SQLite-backed test persistence.

**Test data, setup, or environment:** Standard backend test environment. Each test registers or uses an isolated test user, records the event count before the invalid request, sends an invalid `POST /events` request, and checks that the event count is unchanged.

**Automated command or CI check:** `python -m pytest tests/test_quality_requirements.py` as part of the `Backend CI` workflow.

**Expected measurable result:** Invalid event type, empty description, negative amount, negative mileage, and unknown `user_id` requests are rejected with the expected error status and save no new event record for 100% of tested invalid requests.

**Evidence location:** `backend/tests/test_quality_requirements.py`; `.github/workflows/backend-ci.yml`; latest protected default-branch Backend CI run linked from `docs/testing.md`.

**Automated tests:**
- `test_invalid_event_type_rejected_and_not_saved`
- `test_empty_description_rejected_and_not_saved`
- `test_negative_amount_rejected_and_not_saved`
- `test_negative_mileage_rejected_and_not_saved`
- `test_unknown_user_rejected_and_not_saved`

## QRT-002: Timeline API response time

**Linked quality requirement:** [QR-002](quality-requirements.md#qr-002-timeline-api-response-time)

**Verification method:** Automated FastAPI response-time test using `TestClient`.

**Test data, setup, or environment:** Standard backend test environment. The test creates a user with 20 vehicle events, then measures wall-clock time for `GET /events`.

**Automated command or CI check:** `python -m pytest tests/test_quality_requirements.py` as part of the `Backend CI` workflow.

**Expected measurable result:** `GET /events` returns HTTP 200 in under 2 seconds for the documented test dataset.

**Evidence location:** `backend/tests/test_quality_requirements.py::test_get_events_responds_within_2_seconds`; `.github/workflows/backend-ci.yml`; latest protected default-branch Backend CI run linked from `docs/testing.md`.

## QRT-003: Backend regression testability

**Linked quality requirement:** [QR-003](quality-requirements.md#qr-003-backend-regression-testability)

**Verification method:** Automated CI execution of the full backend pytest suite.

**Test data, setup, or environment:** Standard backend CI environment on pull requests and protected default-branch updates, with dependencies installed from `backend/requirements.txt`.

**Automated command or CI check:** `python -m coverage run -m pytest tests` in the `Backend CI` workflow.

**Expected measurable result:** The full backend pytest suite completes with 0 failures before merge and on protected default-branch updates.

**Evidence location:** `.github/workflows/backend-ci.yml`; latest protected default-branch Backend CI run linked from `docs/testing.md`; local test files under `backend/tests/`.

## MVP v2 / Sprint 3 QRT follow-up

Sprint 3 is scoped around US-08 maintenance recommendations and US-09 notifications. As of this update, the repository does not yet contain dedicated implementation behavior for those features, so no speculative QRTs are added here.

When Sprint 3 behavior is implemented, add QRTs only if there is a measurable quality requirement to verify. Likely candidates include recommendation correctness, user-specific recommendation isolation, notification trigger reliability, and notification state consistency.

Current QRTs remain active because recommendation and notification behavior will depend on reliable vehicle event data, responsive timeline access, and backend regression safety.
