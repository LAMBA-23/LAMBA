# Quality Requirements

This document defines measurable quality requirements for the current LAMBA MVP and the automated quality requirement tests that verify them.

For MVP v2 / Sprint 3, Assignment 4 quality requirements remain active and are extended with new requirement-driven QR coverage for the implemented manual event, assistant, statistics, and coverage-gate behavior. US-08 maintenance recommendations and US-09 notifications are still planned rather than implemented, so they do not yet justify separate QR sections.

## Contents

- [QR-001: Vehicle event data integrity](#qr-001-vehicle-event-data-integrity)
- [QR-002: Timeline API response time](#qr-002-timeline-api-response-time)
- [QR-003: Backend regression testability](#qr-003-backend-regression-testability)
- [QR-004: Assistant context correctness](#qr-004-assistant-context-correctness)
- [QR-005: Statistics calculation correctness](#qr-005-statistics-calculation-correctness)
- [QR-006: Critical-module coverage gate](#qr-006-critical-module-coverage-gate)
- [Architecture Decision Traceability](#architecture-decision-traceability)
- [MVP v2 / Sprint 3 Note](#mvp-v2--sprint-3-note)

## QR-001: Vehicle event data integrity

**ISO/IEC 25010 sub-characteristic:** Integrity

**Scenario:** When a client submits an invalid vehicle-event creation request under normal backend operation, the `POST /events` API shall reject the request and persist no new event record within 100% of the tested invalid-request classes.

**Why this matters:** LAMBA stores mileage, expenses, and maintenance history that users and future recommendation features rely on. If invalid records are saved, the timeline, statistics, and later maintenance follow-up become untrustworthy for both vehicle owners and the development team.

**Linked quality requirement tests:** [QRT-001](quality-requirement-tests.md#qrt-001-vehicle-event-data-integrity)

**Related ADRs:** [ADR-001: Use a backend-owned REST API boundary](architecture/adr/001-use-backend-owned-rest-api-boundary.md), [ADR-002: Use FastAPI, Pydantic, and SQLAlchemy for backend validation and persistence](architecture/adr/002-use-fastapi-pydantic-sqlalchemy-backend.md), [ADR-003: Use Docker Compose for backend and database deployment](architecture/adr/003-use-docker-compose-for-backend-and-database.md)

## QR-002: Timeline API response time

**ISO/IEC 25010 sub-characteristic:** Time behaviour

**Scenario:** When an authenticated user requests the vehicle timeline under the standard backend test environment with the documented 20-event dataset, the `GET /events` API shall return HTTP 200 within 2 seconds for 100% of the tested requests.

**Why this matters:** Viewing the timeline is a primary user workflow. Slow timeline responses make the app feel unreliable and directly reduce the usefulness of manual history review, assistant follow-up, and future maintenance features for the vehicle owner.

**Linked quality requirement tests:** [QRT-002](quality-requirement-tests.md#qrt-002-timeline-api-response-time)

**Related ADRs:** [ADR-001: Use a backend-owned REST API boundary](architecture/adr/001-use-backend-owned-rest-api-boundary.md), [ADR-002: Use FastAPI, Pydantic, and SQLAlchemy for backend validation and persistence](architecture/adr/002-use-fastapi-pydantic-sqlalchemy-backend.md), [ADR-003: Use Docker Compose for backend and database deployment](architecture/adr/003-use-docker-compose-for-backend-and-database.md)

## QR-003: Backend regression testability

**ISO/IEC 25010 sub-characteristic:** Testability

**Scenario:** When a developer runs the backend regression suite under the standard CI environment, the backend automated test suite shall complete with 0 failing tests in the CI run.

**Why this matters:** LAMBA is still changing quickly. Developers and reviewers need a reliable regression gate so that authentication, event capture, assistant, and statistics changes do not silently break existing product behavior before merge.

**Linked quality requirement tests:** [QRT-003](quality-requirement-tests.md#qrt-003-backend-regression-testability)

**Related ADRs:** [ADR-001: Use a backend-owned REST API boundary](architecture/adr/001-use-backend-owned-rest-api-boundary.md), [ADR-002: Use FastAPI, Pydantic, and SQLAlchemy for backend validation and persistence](architecture/adr/002-use-fastapi-pydantic-sqlalchemy-backend.md), [ADR-003: Use Docker Compose for backend and database deployment](architecture/adr/003-use-docker-compose-for-backend-and-database.md)

## QR-004: Assistant context correctness

**ISO/IEC 25010 sub-characteristic:** Functional correctness

**Scenario:** When an authenticated user submits an assistant question under normal backend operation with saved vehicle and event history, the `POST /chat/ask` backend orchestration shall pass the saved vehicle profile and no more than the 50 most recent events to the AI adapter within 100% of the tested scenarios.

**Why this matters:** The assistant is only useful if its answer is grounded in the correct vehicle history. Users and stakeholders need confidence that the assistant reflects the saved car data rather than placeholders, stale context, or an unbounded event history.

**Linked quality requirement tests:** [QRT-004](quality-requirement-tests.md#qrt-004-assistant-context-correctness)

**Related ADRs:** [ADR-001: Use a backend-owned REST API boundary](architecture/adr/001-use-backend-owned-rest-api-boundary.md), [ADR-002: Use FastAPI, Pydantic, and SQLAlchemy for backend validation and persistence](architecture/adr/002-use-fastapi-pydantic-sqlalchemy-backend.md)

## QR-005: Statistics calculation correctness

**ISO/IEC 25010 sub-characteristic:** Functional correctness

**Scenario:** When an authenticated user requests statistics under normal backend operation with saved fuel, repair, trip, and issue events, the `GET /stats` API shall return mileage, expense, record-count, and fuel-liter totals consistent with the stored history within 100% of the tested scenarios.

**Why this matters:** Users rely on statistics to understand the cost and usage history of their vehicle, and the assistant and future maintenance features rely on the same numbers. Incorrect statistics would mislead the vehicle owner and undermine trust in later recommendation behavior.

**Linked quality requirement tests:** [QRT-005](quality-requirement-tests.md#qrt-005-statistics-calculation-correctness)

**Related ADRs:** [ADR-001: Use a backend-owned REST API boundary](architecture/adr/001-use-backend-owned-rest-api-boundary.md), [ADR-002: Use FastAPI, Pydantic, and SQLAlchemy for backend validation and persistence](architecture/adr/002-use-fastapi-pydantic-sqlalchemy-backend.md)

## QR-006: Critical-module coverage gate

**ISO/IEC 25010 sub-characteristic:** Testability

**Scenario:** When the backend coverage gate runs under the standard CI environment, the critical backend modules listed in `docs/testing.md` shall report line coverage of at least 30%.

**Why this matters:** Maintainers and reviewers need inspectable evidence that the most important backend modules remain exercised by automated tests. Without a coverage floor, regressions can accumulate in exactly the modules that coordinate authentication, timeline, assistant, and statistics behavior.

**Linked quality requirement tests:** [QRT-006](quality-requirement-tests.md#qrt-006-critical-module-coverage-gate)

**Related ADRs:** [ADR-001: Use a backend-owned REST API boundary](architecture/adr/001-use-backend-owned-rest-api-boundary.md), [ADR-002: Use FastAPI, Pydantic, and SQLAlchemy for backend validation and persistence](architecture/adr/002-use-fastapi-pydantic-sqlalchemy-backend.md), [ADR-003: Use Docker Compose for backend and database deployment](architecture/adr/003-use-docker-compose-for-backend-and-database.md)

## Architecture Decision Traceability

| Quality requirement | Related ADRs |
|---|---|
| [QR-001: Vehicle event data integrity](#qr-001-vehicle-event-data-integrity) | [ADR-001](architecture/adr/001-use-backend-owned-rest-api-boundary.md), [ADR-002](architecture/adr/002-use-fastapi-pydantic-sqlalchemy-backend.md), [ADR-003](architecture/adr/003-use-docker-compose-for-backend-and-database.md) |
| [QR-002: Timeline API response time](#qr-002-timeline-api-response-time) | [ADR-001](architecture/adr/001-use-backend-owned-rest-api-boundary.md), [ADR-002](architecture/adr/002-use-fastapi-pydantic-sqlalchemy-backend.md), [ADR-003](architecture/adr/003-use-docker-compose-for-backend-and-database.md) |
| [QR-003: Backend regression testability](#qr-003-backend-regression-testability) | [ADR-001](architecture/adr/001-use-backend-owned-rest-api-boundary.md), [ADR-002](architecture/adr/002-use-fastapi-pydantic-sqlalchemy-backend.md), [ADR-003](architecture/adr/003-use-docker-compose-for-backend-and-database.md) |
| [QR-004: Assistant context correctness](#qr-004-assistant-context-correctness) | [ADR-001](architecture/adr/001-use-backend-owned-rest-api-boundary.md), [ADR-002](architecture/adr/002-use-fastapi-pydantic-sqlalchemy-backend.md) |
| [QR-005: Statistics calculation correctness](#qr-005-statistics-calculation-correctness) | [ADR-001](architecture/adr/001-use-backend-owned-rest-api-boundary.md), [ADR-002](architecture/adr/002-use-fastapi-pydantic-sqlalchemy-backend.md) |
| [QR-006: Critical-module coverage gate](#qr-006-critical-module-coverage-gate) | [ADR-001](architecture/adr/001-use-backend-owned-rest-api-boundary.md), [ADR-002](architecture/adr/002-use-fastapi-pydantic-sqlalchemy-backend.md), [ADR-003](architecture/adr/003-use-docker-compose-for-backend-and-database.md) |

## MVP v2 / Sprint 3 Note

The implemented MVP v2 repository state already includes manual event creation, assistant question handling, and statistics behavior that justify QR-004, QR-005, and QR-006 in addition to the Assignment 4 QR set.

US-08 maintenance recommendations and US-09 notifications themselves are still planned and do not yet have dedicated recommendation or notification workflow logic in the repository. When those features are implemented, this document should be extended only if they introduce new measurable quality scenarios not already covered by QR-001 through QR-006.
