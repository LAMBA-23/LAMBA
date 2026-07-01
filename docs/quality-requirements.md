# Quality Requirements

This document defines measurable quality requirements for the current LAMBA MVP and the automated quality requirement tests that verify them.

For MVP v2 / Sprint 3, these Assignment 4 quality requirements remain active because maintenance recommendations and notifications depend on reliable vehicle history, timeline access, and backend regression safety. New quality requirements should be added when US-08 maintenance recommendations or US-09 notifications introduce implemented behavior with measurable quality risks.

## QR-001: Vehicle event data integrity

**ISO/IEC 25010 sub-characteristic:** Integrity

**Scenario:** When a user sends an event creation request with an invalid event type, empty description, negative amount, negative mileage, or a missing or unknown `user_id` under normal backend operation, the `POST /events` API shall reject the request and shall save no new event record for 100% of such invalid requests.

**Why this matters:** LAMBA stores mileage, expenses, and maintenance history. If invalid data is saved, the timeline, statistics, and future maintenance recommendations become incorrect.

**Linked quality requirement tests:** [QRT-001](quality-requirement-tests.md#qrt-001-vehicle-event-data-integrity)

**Related ADRs:** No ADR artifact is currently introduced for this requirement. Add the related ADR link here when Assignment 5 architecture documentation defines the event validation or persistence decision.

**Traceability:** Supports [docs/api-contract.md](api-contract.md), [docs/testing.md](testing.md), US-04, US-05, US-07, and future US-08 maintenance recommendations.

## QR-002: Timeline API response time

**ISO/IEC 25010 sub-characteristic:** Time behaviour

**Scenario:** When a user requests a vehicle timeline under normal backend operation with the demo dataset, the `GET /events` API shall return a successful response within 2 seconds for 95% of requests.

**Why this matters:** Viewing the timeline is one of the main user actions in LAMBA. Slow responses make the app feel unreliable and make maintenance follow-up harder to inspect.

**Linked quality requirement tests:** [QRT-002](quality-requirement-tests.md#qrt-002-timeline-api-response-time)

**Related ADRs:** No ADR artifact is currently introduced for this requirement. Add the related ADR link here when Assignment 5 architecture documentation defines timeline retrieval, backend performance, or persistence decisions.

**Traceability:** Supports [README.md](../README.md), [docs/api-contract.md](api-contract.md), US-05, US-07, and future US-08/US-09 workflows that depend on recorded history.

## QR-003: Backend regression testability

**ISO/IEC 25010 sub-characteristic:** Testability

**Scenario:** When a developer changes backend code under the standard test environment, the backend shall provide automated tests for authentication, events, statistics, chat parsing, and vehicle behavior, and the full backend pytest suite shall pass before merge.

**Why this matters:** LAMBA is still changing quickly. Automated tests help the team find broken behavior before changes are merged and keep future MVP v2 work from regressing core vehicle-history behavior.

**Linked quality requirement tests:** [QRT-003](quality-requirement-tests.md#qrt-003-backend-regression-testability)

**Related ADRs:** No ADR artifact is currently introduced for this requirement. Add the related ADR link here when Assignment 5 architecture documentation defines backend module boundaries, CI strategy, or testing architecture.

**Traceability:** Supports [backend/tests/test_auth.py](../backend/tests/test_auth.py), [backend/tests/test_events.py](../backend/tests/test_events.py), [backend/tests/test_stats.py](../backend/tests/test_stats.py), [backend/tests/test_chat_parse.py](../backend/tests/test_chat_parse.py), [backend/tests/test_vehicle.py](../backend/tests/test_vehicle.py), US-01, US-02, US-04, US-05, US-06, and US-07.

## MVP v2 maintenance note

The current Sprint 3 scope is US-08 maintenance recommendations and US-09 notifications. As of this update, those features are planned but do not yet have dedicated backend endpoints, persistence models, or Android workflow logic in the repository.

Until that implementation exists, the active quality requirements remain QR-001, QR-002, and QR-003. When Sprint 3 product behavior is implemented, this document should be updated if the new behavior creates measurable quality requirements such as recommendation correctness, notification delivery reliability, or notification response time.
