# LAMBA Architecture

This document is the maintained architecture entry point for the current LAMBA product. It explains the system through three views:

- [Static view](static-view/component-diagram.puml): what components exist and how they depend on each other.
- [Dynamic view](dynamic-view/chat-event-sequence.puml): how a non-trivial user workflow moves through the system.
- [Deployment view](deployment-view/deployment-diagram.puml): where the runtime parts execute and how they are connected.

The diagrams are stored as PlantUML source files so architecture changes can be reviewed together with product changes.

## Static View: Component Diagram

Source: [static-view/component-diagram.puml](static-view/component-diagram.puml)

The static view shows LAMBA as a mobile-first system with a Kotlin Android client, a FastAPI backend, a PostgreSQL datastore, and an external AI agent API. The Android app owns user interaction and screen flow. Backend API routes own server-side validation, persistence coordination, statistics calculation, and AI integration. PostgreSQL is the system of record for users, cars, and vehicle events.

Main internal components:

- Android activities and UI adapters for onboarding, vehicle setup, chat, history, and statistics.
- Retrofit API client and repository classes that isolate HTTP calls from UI logic.
- FastAPI route handlers for authentication, vehicle profile, chat parsing, AI answers, events, and statistics.
- Pydantic schemas for request and response validation.
- SQLAlchemy models and database session management for persistence.
- Chat parser and AI answer modules for the conversational vehicle-history workflow.

External systems and platforms:

- End user using the Android application.
- PostgreSQL database used by the backend.
- Timeweb Cloud AI agent API, compatible with chat-completions style requests.
- GitHub Actions for CI checks.

The component structure keeps most user-facing concerns in Android and most data integrity concerns in the backend. The backend is cohesive around vehicle-history APIs, but `backend/app/main.py` currently contains routing plus some business logic for statistics and orchestration. That is acceptable for the MVP size, but future growth may benefit from separating route handlers, services, and repositories.

Maintainability implications:

- The Retrofit service and Pydantic schemas create a clear API contract boundary between Android and backend.
- SQLAlchemy models centralize persistent entities, making data ownership easier to reason about.
- AI integration is isolated in chat-specific modules, which limits the blast radius of external-service changes.
- The current `user_id` query parameter approach is simple for MVP flows but constrains future security hardening because it is not token-based authorization.
- CORS is currently permissive, which simplifies local and MVP integration but should be revisited before production-like deployment.

Quality requirements supported or constrained:

- QR-001, vehicle event data integrity, is supported by backend request validation and database persistence boundaries.
- QR-002, timeline API response time, is supported by the simple backend-to-database event retrieval path.
- QR-003, backend regression testability, is supported by keeping backend behavior accessible through FastAPI routes and pytest/FastAPI TestClient tests.
- Future security quality requirements are constrained by the current plain password storage and `user_id` based access model.

## Dynamic View: Sequence Diagram

Source: [dynamic-view/chat-event-sequence.puml](dynamic-view/chat-event-sequence.puml)

The dynamic view documents the chat event capture workflow. This scenario is important because it connects the main product idea, a conversational car assistant, with durable vehicle history records.

Scenario:

1. The user sends a vehicle event message in the Android chat screen.
2. Android passes the message to `ChatRepository`.
3. `ChatRepository` asks the backend to parse the message through `POST /chat/parse-event`.
4. The backend calls the chat parser. When configured, the parser calls the external AI agent API and then applies local guardrails.
5. If clarification is needed, the backend returns a clarification question and no event is saved.
6. If parsing succeeds, Android sends the parsed event to `POST /events`.
7. The backend validates the event, resolves the user's vehicle, saves the event in PostgreSQL, and returns the saved record.
8. History and statistics screens can then retrieve the updated timeline or aggregate values.

This sequence helps reason about integration boundaries and quality concerns:

- Android does not write directly to the database; all persistence goes through backend validation.
- External AI output is not saved directly. It is converted into a typed response and checked by backend guardrails first.
- The `parse-event` endpoint and `events` endpoint are separate, so the UI can ask for clarification before creating a record.
- The flow supports QR-001 because invalid or ambiguous data should not be persisted.
- The flow supports QR-003 because parsing, event creation, and statistics behavior can be covered independently by automated tests.

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

Architecture Decision Records are maintained separately under `docs/architecture/adr/` when Part 5 decisions are introduced. Relevant ADR links should be added here after those records are created.
