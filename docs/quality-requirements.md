# Quality Requirements

This document defines measurable quality requirements for the current LAMBA MVP and the automated quality requirement tests that verify them.

## QR-001: Vehicle event data integrity

**ISO/IEC 25010 sub-characteristic:** Integrity

**Scenario:** When a user sends an event creation request with an invalid event type, empty description, negative amount, negative mileage, or a missing or unknown `user_id` under normal system operation, the system shall reject the event and shall save no new event record for 100% of such invalid requests.

**Why this matters:** LAMBA stores mileage, expenses, and maintenance history. If invalid data is saved, the timeline and statistics become incorrect.

**Traceability:** Supports [docs/api-contract.md](/C:/Users/Vasilisa/Desktop/LAMBA/LAMBA/docs/api-contract.md), [docs/testing.md](/C:/Users/Vasilisa/Desktop/LAMBA/LAMBA/docs/testing.md), US-04, US-05, and US-07.

## QR-002: Timeline API response time

**ISO/IEC 25010 sub-characteristic:** Time behaviour

**Scenario:** When a user requests a vehicle timeline under normal backend operation with the demo dataset, the `GET /events` endpoint shall return a successful response within 2 seconds for 95% of requests.

**Why this matters:** Viewing the timeline is one of the main user actions in LAMBA. Slow responses make the app feel unreliable and harder to use.

**Traceability:** Supports [README.md](/C:/Users/Vasilisa/Desktop/LAMBA/LAMBA/README.md), [docs/api-contract.md](/C:/Users/Vasilisa/Desktop/LAMBA/LAMBA/docs/api-contract.md), US-05, and US-07.

## QR-003: Backend regression testability

**ISO/IEC 25010 sub-characteristic:** Testability

**Scenario:** When a developer changes backend code under the standard test environment, the backend shall provide automated tests for authentication, events, statistics, chat parsing, and vehicle behavior, and the full backend pytest suite shall pass before merge.

**Why this matters:** LAMBA is still changing quickly. Automated tests help the team find broken behavior before changes are merged.

**Traceability:** Supports [backend/tests/test_auth.py](/C:/Users/Vasilisa/Desktop/LAMBA/LAMBA/backend/tests/test_auth.py), [backend/tests/test_events.py](/C:/Users/Vasilisa/Desktop/LAMBA/LAMBA/backend/tests/test_events.py), [backend/tests/test_stats.py](/C:/Users/Vasilisa/Desktop/LAMBA/LAMBA/backend/tests/test_stats.py), [backend/tests/test_chat_parse.py](/C:/Users/Vasilisa/Desktop/LAMBA/LAMBA/backend/tests/test_chat_parse.py), [backend/tests/test_vehicle.py](/C:/Users/Vasilisa/Desktop/LAMBA/LAMBA/backend/tests/test_vehicle.py), US-01, US-02, US-04, US-05, US-06, and US-07.
