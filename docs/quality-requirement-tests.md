# Quality Requirement Tests

This document maps each quality requirement to its automated test.
Each QRT is implemented as a pytest test in the normal test location.

## QRT-001: Vehicle event data integrity

**Linked quality requirement:** QR-001 (Integrity)

**Test file:** [`backend/tests/test_quality_requirements.py`](../backend/tests/test_quality_requirements.py)

**Evidence type:** Automated unit tests

**Tests:**
- `test_invalid_event_type_rejected_and_not_saved` — invalid event type returns 422, no record saved
- `test_empty_description_rejected_and_not_saved` — empty description returns 422, no record saved
- `test_negative_amount_rejected_and_not_saved` — negative amount returns 422, no record saved
- `test_negative_mileage_rejected_and_not_saved` — negative mileage returns 422, no record saved
- `test_unknown_user_rejected_and_not_saved` — unknown user_id returns 404, no record saved

**How it works:** Each test sends an invalid event creation request, asserts the expected error status code, then verifies that no new event was persisted by comparing the event count before and after.

---

## QRT-002: Timeline API response time

**Linked quality requirement:** QR-002 (Time behaviour)

**Test file:** [`backend/tests/test_quality_requirements.py::test_get_events_responds_within_2_seconds`](../backend/tests/test_quality_requirements.py)

**Evidence type:** Automated unit test

**Description:** Verifies that `GET /events` returns a response within 2 seconds under normal operation with a realistic dataset.

**How it works:** Creates a user with 20 events, measures wall-clock time of the GET request, asserts elapsed time is under 2 seconds.

---

## QRT-003: Backend regression testability

**Linked quality requirement:** QR-003 (Testability)

**Test file:** [`backend/tests/test_quality_requirements.py::test_full_backend_pytest_suite_passes`](../backend/tests/test_quality_requirements.py)

**Evidence type:** Automated suite-level evidence

**Description:** The full backend pytest suite provides regression testability evidence. This QRT documents that the entire suite must pass before merge. CI enforces this gate.

**How it works:** The test itself always passes — the real evidence is that CI runs the full `pytest` suite and all tests must be green before a PR can be merged.
