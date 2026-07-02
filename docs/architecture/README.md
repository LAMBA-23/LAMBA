# LAMBA Architecture

This document is the maintained architecture entry point for the current LAMBA product. It explains the system through three views:

- [Static view](static-view/component-diagram.puml): what components exist and how they depend on each other.
- [Dynamic view](dynamic-view/chat-event-sequence.puml): how a non-trivial user workflow moves through the system.
- [Deployment view](deployment-view/deployment-diagram.puml): where the runtime parts execute and how they are connected.

The diagrams are stored as PlantUML source files so architecture changes can be reviewed together with product changes.

## Static View: Component Diagram

Source: [static-view/component-diagram.puml](static-view/component-diagram.puml)

The static view shows LAMBA as a mobile-first system with a Kotlin Android client, a FastAPI backend, a PostgreSQL datastore, and an external AI agent API. The diagram prioritizes readability by grouping implementation details into layers instead of showing every class as a separate box.

Main internal components:

- Android presentation layer: login, registration, vehicle, chat, history, and statistics screens.
- Android state/session layer: `SessionManager` and screen-level client state.
- Android repository and network layers: `ChatRepository`, `RetrofitClient`, `LambaApiService`, and request/response models.
- Backend API layer: FastAPI routes for authentication, vehicle profile, events, statistics, and chat.
- Backend validation layer: Pydantic schemas for request and response validation.
- Backend service layer: vehicle-history behavior, statistics calculation, chat parsing, and AI answers.
- Backend persistence layer: SQLAlchemy models and database sessions.

External systems and platforms:

- End user using the Android application.
- PostgreSQL database used by the backend.
- Timeweb Cloud AI agent API, compatible with chat-completions style requests.

The frontend and backend are cohesive at the architectural level: Android owns user interaction and client state, while the backend owns validation, business rules, persistence, and external AI integration. Coupling between the two sides is intentionally limited to the REST/JSON API contract. Android does not access PostgreSQL or the AI provider directly, which keeps secrets and persistence concerns out of the mobile client.

The backend is currently implemented with a compact route-centered structure, so some route handlers still contain orchestration and service logic. The diagram presents the intended logical layers inside that implementation: API, validation, service behavior, and persistence. This makes the architecture easier to reason about without inventing separate service classes that do not yet exist.

Maintainability implications:

- The Retrofit service and Pydantic schemas create a clear API contract boundary between Android and backend.
- SQLAlchemy models centralize persistent entities, making data ownership easier to reason about.
- AI integration is isolated in chat-specific modules, which limits the blast radius of external-service changes.
- The current `user_id` query parameter approach is simple for MVP flows but constrains future security hardening because it is not token-based authorization.
- The layered view makes future refactoring paths clear: route handlers can be split into dedicated service modules when the MVP grows.

Quality requirements supported or constrained:

- QR-001, vehicle event data integrity, is supported by backend request validation and database persistence boundaries.
- QR-002, timeline API response time, is supported by the simple backend-to-database event retrieval path.
- QR-003, backend regression testability, is supported by keeping backend behavior accessible through FastAPI routes and pytest/FastAPI TestClient tests.
- Maintainability is supported by explicit frontend/backend boundaries and layered responsibilities.
- Interoperability is supported by the REST/JSON API between Android and FastAPI.
- Reliability is supported by keeping data validation and persistence in the backend rather than duplicating it in the client.
- Security and configuration safety are supported by keeping database access and AI provider credentials on the backend side, although future password hashing and token-based authorization are still needed.

## Dynamic View: Sequence Diagram

Source: [dynamic-view/chat-event-sequence.puml](dynamic-view/chat-event-sequence.puml)

The dynamic view documents the chat event capture workflow. This scenario is important because it connects the main product idea, a conversational car assistant, with durable vehicle history records that later appear in History and Statistics.

Scenario:

1. The user writes a vehicle-related message in the Android chat UI.
2. `ChatRepository` sends the message through the Retrofit API client.
3. The FastAPI API layer receives `POST /chat/parse-event` and delegates parsing to the chat parser / AI service boundary.
4. The parser calls the Timeweb Cloud AI Agent API when configured, then returns either a parsed event draft or a clarification question.
5. If clarification is needed, the UI shows the question and no event is persisted.
6. If parsing succeeds, Android saves the event through `POST /events`.
7. The backend validates the event, stores it through SQLAlchemy persistence, and returns the saved event.
8. The Android UI can refresh History and Statistics through `GET /events` and `GET /stats`.

This sequence helps reason about integration boundaries and quality concerns:

- Android does not write directly to the database; all persistence goes through backend validation.
- External AI output is not saved directly. It is converted into a typed response and checked by backend guardrails first.
- The `parse-event` endpoint and `events` endpoint are separate, so the UI can ask for clarification before creating a record.
- Reliability is supported because ambiguous AI output follows the clarification path instead of becoming saved history.
- Maintainability is supported by separating Android UI/repository/network responsibilities from backend API, parser, event, statistics, and persistence responsibilities.
- Usability is supported because the user receives either a saved-event confirmation or a clarification question.
- Performance is visible through the timeline/statistics refresh path after saving an event.
- Interoperability is visible at the HTTP/JSON boundary between Retrofit and FastAPI and at the HTTPS boundary to the external AI provider.
- The flow supports QR-001 because invalid or ambiguous data should not be persisted, and QR-003 because parsing, event creation, and statistics behavior can be covered independently by automated tests.

## Deployment View: Deployment Diagram

Source: [deployment-view/deployment-diagram.puml](deployment-view/deployment-diagram.puml)

The deployment view shows the current runnable product shape. The Android app runs on a user device or emulator. The backend runs as a FastAPI service in a Docker container. PostgreSQL runs as a separate Docker container with a named volume for persistent data. The backend communicates with the Timeweb Cloud AI agent over HTTPS when AI credentials are configured.

The selected deployment model was chosen because it is simple, reproducible, and appropriate for the current MVP:

- Docker Compose starts the backend and database together for local development and review.
- PostgreSQL is isolated as stateful infrastructure instead of being embedded in application code.
- Environment variables configure database and AI integration values without committing secrets.
- GitHub Actions verifies backend linting, formatting, tests, coverage, dependency health, and Markdown links before protected-branch updates.

How the deployment supports the product:

- Reviewers can run the backend and database locally with one Compose command.
- Backend API documentation is available through FastAPI Swagger UI at `/docs`.
- The Android app communicates with the backend over HTTP/JSON, matching the API contract in `docs/api-contract.md`.
- The database volume preserves vehicle history across container restarts.

Deployment constraints and operational considerations:

- AI-backed endpoints require `TIMEWEB_API_KEY` and `TIMEWEB_AGENT_ID`; without them, the backend returns fallback clarification or unavailable-service responses.
- The current Docker Compose file contains development defaults and should not be treated as a production security configuration.
- Public deployments must use sanitized demo data and must not expose private credentials.
- The Android base URL must match the deployed or local backend address for the app to reach the API.
- PostgreSQL availability is a hard dependency for backend startup in the current Compose setup.

## Architecture Traceability

The architecture views explain the current implementation structure and support later Assignment 5 reporting:

- Static view: component boundaries, dependencies, data ownership, and maintainability risks.
- Dynamic view: chat-to-event workflow, validation order, and AI integration boundary.
- Deployment view: runtime containers, external services, stateful storage, and access path.

## Architecture Decision Records

Architecture Decision Records document why key architecture choices were made. The static, dynamic, and deployment views show the resulting structure and behavior, while [quality requirements](../quality-requirements.md) link the measurable quality goals back to the relevant ADRs.

| ADR | Decision | Related quality requirements |
|---|---|---|
| [ADR-001](adr/001-use-backend-owned-rest-api-boundary.md) | Use a backend-owned REST API boundary | QR-001, QR-002, QR-003 |
| [ADR-002](adr/002-use-fastapi-pydantic-sqlalchemy-backend.md) | Use FastAPI, Pydantic, and SQLAlchemy for backend validation and persistence | QR-001, QR-002, QR-003 |
| [ADR-003](adr/003-use-docker-compose-for-backend-and-database.md) | Use Docker Compose for backend and database deployment | QR-001, QR-002, QR-003 |

ADR-001 explains the Android-to-backend boundary shown in the static, dynamic, and deployment views. ADR-002 explains why validation, API behavior, and persistence are owned by the FastAPI backend shown in the static and dynamic views. ADR-003 explains the backend and database runtime model shown in the deployment view.
