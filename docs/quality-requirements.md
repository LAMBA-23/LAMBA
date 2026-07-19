# Quality Requirements

This document defines measurable quality requirements for the current LAMBA MVP and the automated quality requirement tests that verify them.

For MVP v3 / Sprint 5, all earlier quality requirements remain active and are extended with Sprint 5 implemented features including profile management, maintenance recommendations, in-app notifications, voice input, vehicle-data Excel export, and dynamic chat style switching.

## Contents

- [QR-001: Vehicle event data integrity](#qr-001-vehicle-event-data-integrity)
- [QR-002: Timeline API response time](#qr-002-timeline-api-response-time)
- [QR-003: Backend regression testability](#qr-003-backend-regression-testability)
- [QR-004: Assistant context correctness](#qr-004-assistant-context-correctness)
- [QR-005: Statistics calculation correctness](#qr-005-statistics-calculation-correctness)
- [QR-006: Critical-module coverage gate](#qr-006-critical-module-coverage-gate)
- [QR-007: Secure password storage](#qr-007-secure-password-storage)
- [QR-008: Login and chat request-rate protection](#qr-008-login-and-chat-request-rate-protection)
- [Architecture Decision Traceability](#architecture-decision-traceability)
- [MVP v3 / Sprint 5 Note](#mvp-v3--sprint-5-note)

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

## QR-007: Secure password storage

**ISO/IEC 25010 sub-characteristic:** Confidentiality

**Scenario:** When a client registers a user account under normal backend operation, the backend authentication component shall persist a salted password hash instead of the raw password and shall still accept the correct password while rejecting an incorrect password within 100% of the tested authentication scenarios.

**Why this matters:** LAMBA stores account credentials for vehicle owners. Raw password storage would expose user credentials if database contents were inspected or leaked, and it would undermine trust in the product before customer trial and handover.

**Linked quality requirement tests:** [QRT-007](quality-requirement-tests.md#qrt-007-secure-password-storage)

**Related ADRs:** [ADR-001: Use a backend-owned REST API boundary](architecture/adr/001-use-backend-owned-rest-api-boundary.md), [ADR-002: Use FastAPI, Pydantic, and SQLAlchemy for backend validation and persistence](architecture/adr/002-use-fastapi-pydantic-sqlalchemy-backend.md), [ADR-003: Use Docker Compose for backend and database deployment](architecture/adr/003-use-docker-compose-for-backend-and-database.md)

**Traceability:** Implemented through `backend/app/security.py` and authentication routes in `backend/app/main.py`; tracked by [#265](https://github.com/LAMBA-23/LAMBA/issues/265); verified by `backend/tests/test_auth.py`.

## QR-008: Login and chat request-rate protection

**ISO/IEC 25010 sub-characteristic:** Availability

**Scenario:** When a remote client sends repeated login or chat requests from one client address under normal backend operation, the backend rate limiter shall allow no more than 5 login requests and no more than 20 chat requests per 60-second window, return HTTP 429 after the configured limit is exceeded, and allow requests again after old attempts leave the configured window.

**Why this matters:** Login and assistant endpoints are public API surfaces that can be abused through repeated requests. Rate limiting protects backend availability and external AI usage by reducing avoidable request floods while preserving normal user interaction.

**Linked quality requirement tests:** [QRT-008](quality-requirement-tests.md#qrt-008-login-and-chat-request-rate-protection)

**Related ADRs:** [ADR-001: Use a backend-owned REST API boundary](architecture/adr/001-use-backend-owned-rest-api-boundary.md), [ADR-002: Use FastAPI, Pydantic, and SQLAlchemy for backend validation and persistence](architecture/adr/002-use-fastapi-pydantic-sqlalchemy-backend.md), [ADR-003: Use Docker Compose for backend and database deployment](architecture/adr/003-use-docker-compose-for-backend-and-database.md)

**Traceability:** Implemented through `backend/app/rate_limit.py` and `backend/app/main.py`; tracked by [#265](https://github.com/LAMBA-23/LAMBA/issues/265); verified by `backend/tests/test_auth.py` and `backend/tests/test_rate_limiting.py`.

## Architecture Decision Traceability

| Quality requirement | Related ADRs |
|---|---|
| [QR-001: Vehicle event data integrity](#qr-001-vehicle-event-data-integrity) | [ADR-001](architecture/adr/001-use-backend-owned-rest-api-boundary.md), [ADR-002](architecture/adr/002-use-fastapi-pydantic-sqlalchemy-backend.md), [ADR-003](architecture/adr/003-use-docker-compose-for-backend-and-database.md) |
| [QR-002: Timeline API response time](#qr-002-timeline-api-response-time) | [ADR-001](architecture/adr/001-use-backend-owned-rest-api-boundary.md), [ADR-002](architecture/adr/002-use-fastapi-pydantic-sqlalchemy-backend.md), [ADR-003](architecture/adr/003-use-docker-compose-for-backend-and-database.md) |
| [QR-003: Backend regression testability](#qr-003-backend-regression-testability) | [ADR-001](architecture/adr/001-use-backend-owned-rest-api-boundary.md), [ADR-002](architecture/adr/002-use-fastapi-pydantic-sqlalchemy-backend.md), [ADR-003](architecture/adr/003-use-docker-compose-for-backend-and-database.md) |
| [QR-004: Assistant context correctness](#qr-004-assistant-context-correctness) | [ADR-001](architecture/adr/001-use-backend-owned-rest-api-boundary.md), [ADR-002](architecture/adr/002-use-fastapi-pydantic-sqlalchemy-backend.md) |
| [QR-005: Statistics calculation correctness](#qr-005-statistics-calculation-correctness) | [ADR-001](architecture/adr/001-use-backend-owned-rest-api-boundary.md), [ADR-002](architecture/adr/002-use-fastapi-pydantic-sqlalchemy-backend.md) |
| [QR-006: Critical-module coverage gate](#qr-006-critical-module-coverage-gate) | [ADR-001](architecture/adr/001-use-backend-owned-rest-api-boundary.md), [ADR-002](architecture/adr/002-use-fastapi-pydantic-sqlalchemy-backend.md), [ADR-003](architecture/adr/003-use-docker-compose-for-backend-and-database.md) |
| [QR-007: Secure password storage](#qr-007-secure-password-storage) | [ADR-001](architecture/adr/001-use-backend-owned-rest-api-boundary.md), [ADR-002](architecture/adr/002-use-fastapi-pydantic-sqlalchemy-backend.md), [ADR-003](architecture/adr/003-use-docker-compose-for-backend-and-database.md) |
| [QR-008: Login and chat request-rate protection](#qr-008-login-and-chat-request-rate-protection) | [ADR-001](architecture/adr/001-use-backend-owned-rest-api-boundary.md), [ADR-002](architecture/adr/002-use-fastapi-pydantic-sqlalchemy-backend.md), [ADR-003](architecture/adr/003-use-docker-compose-for-backend-and-database.md) |

## MVP v3 / Sprint 5 Note

The implemented MVP v3 Sprint 5 repository state includes all Sprint 4 features plus profile management with avatar support, maintenance recommendations via `/recommendations` endpoint, in-app notifications screen, voice input with Mistral transcription, vehicle-data Excel export, dynamic chat style switching (Friendly/Selfish/Pragmatic), vehicle brand and model selection, and history sorting by newest events first. The backend now exposes `/auth`, `/vehicle`, `/events`, `/stats`, `/chat`, `/recommendations`, and `/chat/transcribe` endpoints. External AI services include both Timeweb/DeepSeek for chat and Mistral for voice transcription.

CORS is covered as automated regression evidence in `docs/testing.md` through `backend/tests/test_cors.py`. It is not yet documented as a separate QR because the current automated tests verify the default denied-origin behavior but do not verify a configured allowed origin from `CORS_ALLOWED_ORIGINS`.

US-08 maintenance recommendations and US-09 notifications are now implemented in Sprint 5. Recommendations are served via the `/recommendations` endpoint and notifications are displayed in the in-app notifications screen. These features are covered by existing QR scenarios for data integrity and API response time.
