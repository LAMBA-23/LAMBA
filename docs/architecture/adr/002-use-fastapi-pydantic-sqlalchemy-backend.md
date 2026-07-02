# ADR-002: Use FastAPI, Pydantic, and SQLAlchemy for backend validation and persistence

## Status

Accepted

## Context

LAMBA stores vehicle profile data and vehicle events that feed the timeline, statistics, chat context, and future maintenance features. Invalid event type, empty description, negative amount, negative mileage, or unknown user data must not become durable history.

The backend currently exposes FastAPI routes, validates payloads with Pydantic schemas, and persists users, cars, and events with SQLAlchemy models and sessions. Backend tests use FastAPI `TestClient` and SQLite-backed test persistence to verify API behavior.

## Decision

LAMBA uses FastAPI as the backend API framework, Pydantic schemas as the request/response validation boundary, and SQLAlchemy as the persistence abstraction for the users, cars, and events data model.

For MVP v2, backend route handlers remain compact, but the architecture treats them as API orchestration over validation, service behavior, and persistence responsibilities. This matches the current code while making future service extraction possible when the product grows.

## Quality requirements addressed

- QR-001: Vehicle event data integrity
- QR-002: Timeline API response time
- QR-003: Backend regression testability

## Consequences

### Positive

- Invalid event payloads are rejected by typed backend schemas before persistence.
- Vehicle timeline and statistics queries use one backend-owned data model.
- The same FastAPI endpoints used by Android are covered by automated backend tests.
- SQLAlchemy keeps production PostgreSQL and SQLite-backed tests behind one persistence API.

### Negative / Trade-offs

- Some route handlers currently combine orchestration and service logic, so the backend needs disciplined tests while it remains compact.
- ORM behavior and database schema changes must be reviewed carefully because timeline and statistics features share the same event data.
- Password storage and authorization are still MVP-level and need future hardening outside this ADR.

## Related artifacts

- [docs/architecture/README.md](../README.md)
- [docs/quality-requirements.md](../../quality-requirements.md)
- [docs/quality-requirement-tests.md](../../quality-requirement-tests.md)
- [backend/app/main.py](../../../backend/app/main.py)
- [backend/app/schemas.py](../../../backend/app/schemas.py)
- [backend/app/models.py](../../../backend/app/models.py)
- [backend/app/database.py](../../../backend/app/database.py)
- [backend/tests/test_quality_requirements.py](../../../backend/tests/test_quality_requirements.py)
