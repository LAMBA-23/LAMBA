# ADR-001: Use a backend-owned REST API boundary

## Status

Accepted

## Context

LAMBA has a Kotlin Android client for user interaction and a FastAPI backend for vehicle history, statistics, chat parsing, and AI-backed answers. The Android app needs to create and read vehicle data, but direct mobile access to PostgreSQL or the Timeweb Cloud AI Agent API would expose infrastructure details and credentials in the client.

The current implementation already uses Retrofit and JSON request/response models in the Android app, and FastAPI routes with Pydantic schemas in the backend. Architecture views show Android communicating with the backend over HTTP/JSON while the backend owns persistence and AI integration.

## Decision

LAMBA uses a backend-owned REST/JSON boundary between the Android app and server-side capabilities. The Android app calls backend endpoints for authentication, vehicle profile, event history, statistics, chat parsing, and assistant answers. The backend owns database access, validation, business rules, and external AI provider calls.

For MVP v2 this keeps the customer-facing mobile app focused on interaction and local UI state, while keeping data ownership, persistence, and external service configuration on the backend.

## Quality requirements addressed

- QR-001: Vehicle event data integrity
- QR-002: Timeline API response time
- QR-003: Backend regression testability

## Consequences

### Positive

- Backend validation is the single gate before vehicle events are persisted.
- Android, backend, database, and AI provider responsibilities stay visible in the static, dynamic, and deployment views.
- The REST/JSON API can be tested through FastAPI tests and exercised by the Android Retrofit client.
- AI credentials and database connection details stay out of the Android client.

### Negative / Trade-offs

- Android availability depends on backend availability and a correctly configured base URL.
- The current `user_id` query parameter approach is simple for MVP flows but is not a complete authorization model.
- API contract changes require coordinated updates to both Android models and backend schemas.

## Related artifacts

- [docs/architecture/README.md](../README.md)
- [docs/quality-requirements.md](../../quality-requirements.md)
- [docs/api-contract.md](../../api-contract.md)
- [docs/architecture/static-view/component-diagram.puml](../static-view/component-diagram.puml)
- [docs/architecture/dynamic-view/chat-event-sequence.puml](../dynamic-view/chat-event-sequence.puml)
- [app/src/main/java/com/lamba/app/network/LambaApiService.kt](../../../app/src/main/java/com/lamba/app/network/LambaApiService.kt)
- [backend/app/main.py](../../../backend/app/main.py)
