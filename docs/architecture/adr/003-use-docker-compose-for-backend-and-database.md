# ADR-003: Use Docker Compose for backend and database deployment

## Status

Accepted

## Context

LAMBA needs a repeatable way for developers, reviewers, and course evaluators to run the backend and database together. The backend depends on PostgreSQL for durable vehicle history and on environment variables for database and Timeweb Cloud AI Agent configuration.

The repository already includes a `docker-compose.yml` file that starts PostgreSQL 16 with a named `postgres_data` volume and builds the FastAPI backend from `backend/Dockerfile`. The deployment view documents this Compose host as the current MVP v2 runtime shape for the backend stack.

## Decision

LAMBA uses Docker Compose to run the FastAPI backend and PostgreSQL database together for MVP v2 development, review, and runnable artifact evidence. PostgreSQL is a separate container with persistent volume storage, and backend runtime configuration is provided through environment variables.

For MVP v2 this gives the team one documented backend startup path while keeping the Android APK, backend service, database, and external AI provider as separate deployment concerns.

## Quality requirements addressed

- QR-001: Vehicle event data integrity
- QR-002: Timeline API response time
- QR-003: Backend regression testability

## Consequences

### Positive

- Reviewers can run backend and database dependencies with one Compose command.
- PostgreSQL state persists across container restarts through the named volume.
- Backend configuration for database and AI integration is centralized in the runtime environment.
- The deployment view matches the runnable repository artifact.

### Negative / Trade-offs

- The Compose file contains development defaults and is not a complete production security configuration.
- Backend startup depends on PostgreSQL health in the current Compose setup.
- Android still needs the correct backend base URL for local, review, or deployed environments.

## Related artifacts

- [docs/architecture/README.md](../README.md)
- [docs/quality-requirements.md](../../quality-requirements.md)
- [docs/architecture/deployment-view/deployment-diagram.puml](../deployment-view/deployment-diagram.puml)
- [docker-compose.yml](../../../docker-compose.yml)
- [backend/Dockerfile](../../../backend/Dockerfile)
- [README.md](../../../README.md)
