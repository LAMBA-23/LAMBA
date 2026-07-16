# LAMBA Architecture

This document is the maintained architecture entry point for the current LAMBA product. It explains the system through three views:

- [Static view](static-view/component-diagram.puml): what components exist and how they depend on each other.
- [Dynamic view](dynamic-view/chat-event-sequence.puml): how a non-trivial user workflow moves through the system.
- [Deployment view](deployment-view/deployment-diagram.puml): where the runtime parts execute and how they are connected.

The diagrams are stored as PlantUML source files so architecture changes can be reviewed together with product changes.

## Static View: Component Diagram

Source: [static-view/component-diagram.puml](static-view/component-diagram.puml)

The static view shows LAMBA as a mobile-first system with a Kotlin Android client, a local Room datastore for recent chat dialogs, a FastAPI backend, a PostgreSQL datastore, and an external AI agent API. The diagram prioritizes readability by grouping implementation details into layers instead of showing every class as a separate box.

Main internal components:

- Android presentation layer: login, registration, vehicle, chat, history, and statistics screens.
- Android state/session layer: `SessionManager`, the current user, and the current local chat id.
- Android repository and local storage layers: `ChatRepository`, `LocalChatHistoryRepository`, `LocalChatDao`, Room entities, and `RoomLocalChatStore`.
- Android network layer: `RetrofitClient`, `LambaApiService`, and request/response models.
- Backend API layer: FastAPI routes for authentication, vehicle profile, events, statistics, and chat.
- Backend security layer: password hashing, fixed-window rate limiting, and CORS origin configuration.
- Backend validation layer: Pydantic schemas for request and response validation.
- Backend service layer: vehicle-history behavior, statistics calculation, chat parsing, AI answers, and safe photo normalization/thumbnail generation.
- Backend photo storage adapter: private local-volume or S3-compatible object access behind the same owner-checked API.
- Backend persistence layer: SQLAlchemy models and database sessions.

External systems and platforms:

- End user using the Android application.
- Room database on the Android device for the last five local chat dialogs per user.
- PostgreSQL database used by the backend.
- Persistent `backend_uploads` volume for the default local photo adapter, or a private S3-compatible bucket selected by runtime configuration.
- Timeweb Cloud AI agent API, compatible with chat-completions style requests.

The frontend and backend are cohesive at the architectural level: Android owns user interaction, client state, and local recent-dialog persistence, while the backend owns validation, business rules, durable vehicle-history persistence, security controls, and external AI integration. Coupling between the two sides is intentionally limited to the REST/JSON API contract. Android does not access PostgreSQL or the AI provider directly, which keeps secrets and server-side persistence concerns out of the mobile client.

The backend is currently implemented with a compact route-centered structure, so some route handlers still contain orchestration and service logic. The diagram presents the intended logical layers inside that implementation: API, validation, service behavior, and persistence. This makes the architecture easier to reason about without inventing separate service classes that do not yet exist.

Maintainability implications:

- The Retrofit service and Pydantic schemas create a clear API contract boundary between Android and backend.
- Room entities and DAOs keep short local chat-history persistence separate from backend vehicle-history persistence.
- SQLAlchemy models centralize persistent entities, making data ownership easier to reason about.
- AI integration is isolated in chat-specific modules, which limits the blast radius of external-service changes.
- Photo decoding and storage are isolated from route orchestration, so local and S3-compatible storage use the same API and database metadata.
- Password hashing, login/chat rate limiting, and restrictive default CORS behavior reduce Sprint 4 security risk, while the current `user_id` query parameter approach still constrains future security hardening because it is not token-based authorization.
- The layered view makes future refactoring paths clear: route handlers can be split into dedicated service modules when the MVP grows.

Quality requirements supported or constrained:

- QR-001, vehicle event data integrity, is supported by backend request validation and database persistence boundaries.
- QR-002, timeline API response time, is supported by the simple backend-to-database event retrieval path.
- QR-003, backend regression testability, is supported by keeping backend behavior accessible through FastAPI routes and pytest/FastAPI TestClient tests.
- QR-007, secure password storage, is supported by backend-owned authentication and password hashing.
- QR-008, login and chat request-rate protection, is supported by the backend fixed-window rate limiter on login, chat answer, and chat title endpoints.
- Maintainability is supported by explicit frontend/backend boundaries and layered responsibilities.
- Interoperability is supported by the REST/JSON API between Android and FastAPI.
- Reliability is supported by keeping data validation and persistence in the backend rather than duplicating it in the client.
- Security and configuration safety are supported by keeping database access, AI provider credentials, CORS origins, rate-limit values, and password hashes on the backend side, although token-based authorization is still future work.

## Dynamic View: Sequence Diagram

Source: [dynamic-view/chat-event-sequence.puml](dynamic-view/chat-event-sequence.puml)

The dynamic view documents the local chat-dialog workflow. This scenario is important because Sprint 4 added Room-backed persistence for the latest five local dialogs while the assistant still relies on backend vehicle history and external AI access.

Scenario:

1. The user opens chat and Android reads the current user and current chat id from `SessionManager`.
2. If a local dialog exists, `LocalChatHistoryRepository` restores the chat and messages from Room without resending old messages.
3. If the user sends a new message, `ChatRepository` sends the message and recent local context through Retrofit to `POST /chat/ask`.
4. The backend applies CORS and chat rate limiting, loads vehicle profile and event history from PostgreSQL, and builds an assistant answer.
5. The assistant service calls the Timeweb Cloud AI Agent API when needed, then returns an answer to Android.
6. Android stores the user and assistant messages in Room, creates a local dialog if needed, and trims the user history to five dialogs.
7. After the first completed exchange, Android can request `POST /chat/title`; the backend rate-limits that request and returns a generated or fallback title.
8. The user can switch saved dialogs from the side menu; Android loads the selected local dialog from Room and stores it as the current chat id.
9. On logout, Android clears the local session, current chat id, and user chat rows from Room before returning to the welcome screen.

This sequence helps reason about integration boundaries and quality concerns:

- Android does not write directly to PostgreSQL; durable vehicle history still goes through backend validation.
- Local chat dialogs are intentionally device-local Room data and are not stored on the backend.
- External AI output used for assistant answers and titles is requested through backend endpoints, not directly from Android.
- Reliability is supported because reopening or switching local dialogs restores saved messages without duplicate backend requests.
- Privacy and data minimization are supported by keeping only the recent five local dialogs on the device and clearing them on logout.
- Maintainability is supported by separating Android UI/repository/network responsibilities from backend API, parser, event, statistics, and persistence responsibilities.
- Usability is supported because users can continue recent conversations and start a new chat from the side menu.
- Performance is visible through the local Room restore path and bounded five-dialog storage.
- Interoperability is visible at the HTTP/JSON boundary between Retrofit and FastAPI and at the HTTPS boundary to the external AI provider.
- The flow supports QR-003 because chat behavior remains covered by backend and Android automated tests, and it supports QR-008 because chat answer and title requests pass through backend rate limiting.

## Deployment View: Deployment Diagram

Source: [deployment-view/deployment-diagram.puml](deployment-view/deployment-diagram.puml)

The deployment view shows the current MVP v3 runtime shape and keeps it separate from the development and CI path. The Android app runs on a user device or emulator and stores recent chat dialogs in a local Room database. The backend runs as a FastAPI service in a Docker container, PostgreSQL uses the `postgres_data` volume for server-side vehicle history, and the default photo adapter uses the separate `backend_uploads` volume. A private S3-compatible bucket can replace local photo storage through runtime configuration without changing Android routes. The backend communicates with the Timeweb Cloud AI agent over HTTPS when AI credentials are configured.

The selected deployment model was chosen because it is simple, reproducible, and appropriate for the current MVP:

- Docker Compose starts the backend and database together for local development and review.
- Room stores recent chat dialogs locally on the Android device and is cleared for the user during logout.
- PostgreSQL is isolated as stateful infrastructure instead of being embedded in application code.
- Environment variables configure database, AI integration, CORS origins, and rate-limit values on the backend without committing secrets or exposing them to the Android client.
- Event photos are returned through owner-checked backend routes; local paths and private S3 object URLs are not exposed to Android.
- GitHub Actions verifies backend CI, Android unit tests, Android debug assembly, and Markdown links as a separate development path, not as part of the customer runtime path.

How the deployment supports the product:

- Reviewers can run the backend and database locally or on a review server with one Compose command.
- Backend API documentation is available through FastAPI Swagger UI at `/docs` on port 8000.
- The Android app communicates with the backend over HTTP/JSON, matching the API contract in `docs/api-contract.md`.
- Room preserves recent local dialogs on the device, while PostgreSQL preserves vehicle history across container restarts.
- GitHub and GitHub Actions are shown separately from the customer runtime path because CI verifies changes before merge but does not serve Android, backend, database, or AI traffic at runtime.

Deployment constraints and operational considerations:

- AI-backed endpoints require `TIMEWEB_API_KEY` and `TIMEWEB_AGENT_ID`; without them, the backend returns fallback clarification or unavailable-service responses.
- Browser origins are controlled by `CORS_ALLOWED_ORIGINS`; the default empty value does not grant arbitrary browser cross-origin access.
- Login and chat rate-limit values are backend runtime configuration with safe defaults in code.
- The current Docker Compose file contains development defaults and should not be treated as a production security configuration.
- Public deployments must use sanitized demo data and must not expose private credentials.
- The Android base URL must match the deployed or local backend address for the app to reach the API.
- PostgreSQL availability is a hard dependency for backend startup in the current Compose setup.
- Photo storage defaults to the persistent `backend_uploads` volume; S3 mode additionally requires a private bucket, endpoint/region configuration, and credentials supplied outside the repository.
- The current deployed backend URL for maintained documentation is `http://186.246.27.211:8000`.
- Automatic deployment from CI is not part of the current repository setup; deployment remains a manual Docker Compose based operation.

## Architecture Traceability

The architecture views explain the current implementation structure and support Assignment 6 maintained documentation:

- Static view: component boundaries, dependencies, local/server data ownership, backend security controls, and maintainability risks.
- Dynamic view: Room-backed local chat workflow, backend answer/title requests, rate limiting, and logout cleanup.
- Deployment view: Android Room storage, backend/PostgreSQL runtime containers, external services, runtime configuration, and access path.

## Architecture Decision Records

Architecture Decision Records document why key architecture choices were made. The static, dynamic, and deployment views show the resulting structure and behavior, while [quality requirements](../quality-requirements.md) link the measurable quality goals back to the relevant ADRs.

| ADR | Decision | Related quality requirements |
|---|---|---|
| [ADR-001](adr/001-use-backend-owned-rest-api-boundary.md) | Use a backend-owned REST API boundary | QR-001, QR-002, QR-003, QR-007, QR-008 |
| [ADR-002](adr/002-use-fastapi-pydantic-sqlalchemy-backend.md) | Use FastAPI, Pydantic, and SQLAlchemy for backend validation and persistence | QR-001, QR-002, QR-003, QR-007, QR-008 |
| [ADR-003](adr/003-use-docker-compose-for-backend-and-database.md) | Use Docker Compose for backend and database deployment | QR-001, QR-002, QR-003, QR-007, QR-008 |

ADR-001 explains the Android-to-backend boundary shown in the static, dynamic, and deployment views, including why backend-side authentication, password handling, CORS, and rate limiting stay behind the API boundary. ADR-002 explains why validation, API behavior, authentication behavior, rate limiting, and persistence are owned by the FastAPI backend shown in the static and dynamic views. ADR-003 explains the backend and database runtime model shown in the deployment view, including backend runtime configuration for CORS, AI credentials, and rate-limit values.
