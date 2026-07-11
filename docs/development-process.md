# Development Process

This document describes the team's current actual development process for the LAMBA project. It covers the git workflow, issue and review process, configuration management, development environment, and CI pipeline.

Authoritative references:

- Definition of Done: [docs/definition-of-done.md](definition-of-done.md)
- Quality requirements: [docs/quality-requirements.md](quality-requirements.md)
- Testing status: [docs/testing.md](testing.md)
- Roadmap: [docs/roadmap.md](roadmap.md)
- Product backlog: [GitHub Issues](https://github.com/LAMBA-23/LAMBA/issues)
- Sprint board: [GitHub Projects](https://github.com/orgs/LAMBA-23/projects?query=is%3Aopen)

## Git Workflow

The team uses a feature-branch workflow with a protected `main` branch and pull-request reviews.

### Branch Naming

Branches are named using the pattern `<issue-number>-short-description` without tool names or private context:

- `69-parsing-from-chat`
- `67-post-vehicle-endpoint`
- `282-update-part6-maintained-docs`

Prefix conventions:

- Bare issue number for product PBIs and issue-linked Course Tasks (e.g., `69-parsing-from-chat`)
- `docs/` for documentation-only changes (e.g., `docs/week4-llm-report`)
- `fix/` for bug fixes

### Branching and Review Flow

The following Mermaid gitGraph illustrates the team's typical branching, review, and release flow:

```mermaid
gitGraph
    commit id: "initial setup"
    branch 69-parsing-from-chat
    checkout 69-parsing-from-chat
    commit id: "add chat parser"
    commit id: "add parser tests"
    checkout main
    merge 69-parsing-from-chat id: "merge #69" tag: "PR #69"
    branch 67-post-vehicle-endpoint
    checkout 67-post-vehicle-endpoint
    commit id: "add vehicle endpoint"
    commit id: "add validation"
    checkout main
    merge 67-post-vehicle-endpoint id: "merge #67" tag: "PR #67"
    branch 282-update-part6-maintained-docs
    checkout 282-update-part6-maintained-docs
    commit id: "update maintained docs"
    checkout main
    merge 282-update-part6-maintained-docs id: "merge #282" tag: "PR #282"
    tag: "v0.1.0"
```

**What the diagram shows:**

1. All work starts from `main` in a dedicated feature branch.
2. Each branch is linked to a GitHub issue.
3. Changes are submitted through a pull request.
4. The PR goes through review and CI checks before merge.
5. After merge, the branch is removed.
6. Releases are tagged on `main` (e.g., `v0.1.0`).

**How the team uses this workflow:**

- Before starting work, the implementer creates a branch from the relevant issue page.
- The branch is linked to a pull request using `Closes #<issue-number>`.
- The PR must include a summary, testing evidence, acceptance criteria verification, and changelog checklist.
- At least one different team member must approve the PR before merge.
- CI must pass for the affected area, including backend linting, formatting, tests, coverage, dependency health, Android unit tests and debug assembly, and link check where applicable.
- After merge, the issue is automatically closed if the PR includes `Closes #N`.
- The protected `main` branch does not allow direct pushes or force pushes.

## Issue Types and Templates

The team uses GitHub issue templates to ensure consistent PBI structure. Blank issue creation is disabled.

| Template | Purpose | Key Fields |
|---|---|---|
| [User Story](https://github.com/LAMBA-23/LAMBA/issues/new?template=user-story.yml) | User-facing product behavior | User role, desired action, expected value, acceptance criteria, story points, implementer, reviewer |
| [Other PBI](https://github.com/LAMBA-23/LAMBA/issues/new?template=other-pbi.yml) | Technical, infrastructure, testing, or documentation work | Type, description, acceptance criteria, story points, implementer, reviewer |
| [Bug Report](https://github.com/LAMBA-23/LAMBA/issues/new?template=bug-report.yml) | Defects and regressions | Problem description, reproduction steps, expected vs actual behavior, environment |
| [Course Task](https://github.com/LAMBA-23/LAMBA/issues/new?template=course-task.yml) | Course reporting and submission evidence (not a PBI) | Description, expected evidence/deliverable |

Course Task issues are used for Assignment reporting, maintained documentation alignment, submission packaging, and other course evidence work. They are not Product Backlog Items, but they still need clear scope, expected evidence, verification commands, a dedicated branch, review, and CI evidence when repository files change.

### Workflow States

The team uses the following work-status values, tracked via GitHub issue labels or project board columns:

| Status | Meaning |
|---|---|
| **To Do** | The PBI is in the Product Backlog, not currently ready to start |
| **Ready** | Selected for the current Sprint, assigned, estimated, has acceptance criteria, can be started |
| **In Progress** | Work has started on the PBI |
| **Review** | Implementation is ready for review; the PR is open or review is in progress |
| **Done** | Acceptance criteria satisfied, Definition of Done satisfied, PR merged into `main` |

## Pull Request Process

### PR Template

The team uses a standardized PR template ([`.github/pull_request_template.md`](../.github/pull_request_template.md)) that prompts for:

- Summary of changes
- Testing performed (manual and automated)
- Acceptance criteria verification (reviewer and implementer confirm)
- Changelog checklist (exactly one option selected)
- Reviewer checklist (requirements met, no broken links, no secrets, docs updated)
- Related issue (`Closes #N`)

### Review Rules

- The reviewer must be a different team member than the implementer.
- The PR author cannot approve their own PR.
- Branch protection enforces at least one approval before merge.
- Force pushes and branch deletion are disabled on `main`.

### Merge Strategy

- The team uses merge commits (squash and rebase merging are disabled).
- Each PR is focused on one change where practical.

## Boards and Views

### Product Backlog

The Product Backlog is managed through [GitHub Issues](https://github.com/LAMBA-23/LAMBA/issues) with labels for priority and MVP version.

### Sprint Backlog

The Sprint Backlog is managed through [GitHub Projects](https://github.com/orgs/LAMBA-23/projects?query=is%3Aopen) as a Kanban board. Each Sprint has a corresponding GitHub milestone:

- [Sprint 1 - MVP v1 Foundation](https://github.com/LAMBA-23/LAMBA/milestone/1) (2026-06-15 to 2026-06-21)
- [Sprint 2 - Chat Event Capture & Assistant & Statistics](https://github.com/LAMBA-23/LAMBA/milestone/2) (2026-06-22 to 2026-06-28)
- [Sprint 3 - Maintenance Follow-up](https://github.com/LAMBA-23/LAMBA/milestone/3) (2026-06-29 to 2026-07-05)
- [Sprint 4 - Trial Release and Transition Readiness](https://github.com/LAMBA-23/LAMBA/milestone/4) (2026-07-06 to 2026-07-12)
- [Sprint 5 - Final Transition and Delivery](https://github.com/LAMBA-23/LAMBA/milestone/5) (2026-07-13 to 2026-07-19)

Issues assigned to a Sprint milestone are the authoritative Sprint Backlog items.

## Configuration and Secrets Management

### Environment Variables

The backend requires the following configuration:

| Variable | Purpose | Storage |
|---|---|---|
| `DATABASE_URL` | PostgreSQL connection string | Docker Compose / `.env` |
| `TIMEWEB_API_KEY` | LLM API key for AI chat features | GitHub Secrets / `.env` |
| `TIMEWEB_AGENT_ID` | LLM agent identifier | Docker Compose default / `.env` |
| `TIMEWEB_MODEL` | LLM model name | Docker Compose default / `.env` |
| `TIMEWEB_TIMEOUT_SECONDS` | LLM request timeout | Docker Compose default / `.env` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated browser origins allowed by backend CORS middleware | Docker Compose / `.env` |
| `LOGIN_RATE_LIMIT` | Login request limit per client within the configured window | Environment override; code default is 5 |
| `LOGIN_RATE_LIMIT_WINDOW_SECONDS` | Login rate-limit window in seconds | Environment override; code default is 60 |
| `CHAT_RATE_LIMIT` | Chat and chat-title request limit per client within the configured window | Environment override; code default is 20 |
| `CHAT_RATE_LIMIT_WINDOW_SECONDS` | Chat and chat-title rate-limit window in seconds | Environment override; code default is 60 |

### Secrets Handling

- `.env` is gitignored and never committed.
- `.env.*.local` files are gitignored.
- `.secrets/` directory is gitignored.
- A sanitized `.env.example` is committed as a template for developers.
- CI secrets (API keys) are stored in GitHub Secrets (repository Settings → Secrets and variables → Actions) and injected into the CI environment.
- Docker Compose uses `${VAR:-default}` syntax for optional variables with safe defaults.
- Production-like backend configuration must not use wildcard CORS origins.

### Committed Configuration Artifacts

- `.env.example` — sanitized template showing required variables
- `docker-compose.yml` — full service definition with default values
- `.github/workflows/backend-ci.yml` — CI pipeline configuration
- `.github/workflows/android-ci.yml` - Android JVM unit-test and debug APK assembly CI configuration
- `.github/workflows/lychee.yml` — link-check configuration

## Development Environment

### Backend (FastAPI + PostgreSQL)

The backend runs in Docker Compose with two services:

1. **PostgreSQL 16** — data store with health check
2. **FastAPI backend** — Python 3.12 application

Local setup:

```bash
docker compose up --build
```

The backend will be available at `http://localhost:8000`.

### Android (Kotlin)

The Android app is built with Gradle. Requirements:

- Android SDK
- JDK 17+
- Gradle wrapper (`./gradlew`)
- Kotlin kapt for Room annotation processing, configured in `app/build.gradle.kts`
- Room runtime, KTX, and compiler dependencies for local chat-history persistence

Common checks:

```bash
./gradlew :app:testDebugUnitTest --no-daemon
./gradlew :app:assembleDebug --no-daemon
```

### Reproducible Environment

- Python dependencies are pinned in `backend/requirements.txt`.
- Docker images use specific base versions (`postgres:16`, `python:3.12`).
- Gradle wrapper ensures consistent Android build tool versions.
- Room local chat-history code is verified by Android JVM tests and debug APK assembly.

## CI Pipeline

The team uses GitHub Actions for continuous integration. Backend CI, Android CI, and Link Check run on pull requests and pushes to `main`:

### Backend CI (`.github/workflows/backend-ci.yml`)

| Step | What it checks |
|---|---|
| Install dependencies | Python packages from `backend/requirements.txt` |
| Lint | `ruff check app tests` |
| Format check | `ruff format --check app tests` |
| Tests + coverage | `coverage run -m pytest tests` then `coverage report --include="app/*" --fail-under=30` |
| Dependency health | `pip check` |

Because Backend CI runs the full backend pytest suite, it also covers the automated checks behind `QRT-007` secure password storage and `QRT-008` login and chat request-rate protection.

### Android CI (`.github/workflows/android-ci.yml`)

| Step | What it checks |
|---|---|
| Set up JDK and Android SDK | Android build environment for Gradle |
| Run Android unit tests | `./gradlew :app:testDebugUnitTest --no-daemon` |
| Run Android debug build | `./gradlew :app:assembleDebug --no-daemon` |

### Link Check (`.github/workflows/lychee.yml`)

| Step | What it checks |
|---|---|
| Lychee link check | All Markdown files, validates internal and public external links |

There is no automatic deployment workflow in the repository. Deployment is handled separately from CI.

### Branch Protection

The `main` branch is protected:

- Required pull request reviews: 1 approval minimum
- Enforce admins: enabled
- Force pushes: disabled
- Branch deletion: disabled

## Release Process

- The team uses Semantic Versioning with `v` prefix (e.g., `v0.1.0`).
- Releases point to commits on the protected `main` branch.
- Each release links the relevant Sprint milestone, CHANGELOG entry, and product access artifact.
- `CHANGELOG.md` follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) format.
- Unreleased changes are tracked under `## [Unreleased]` and moved to a dated section at release time.

## Deployment

### Local Development

`docker compose up --build` starts PostgreSQL and the FastAPI backend locally.

### Current Deployment

The MVP backend is deployed on the current public server:

- Backend: `http://186.246.27.211:8000`
- Swagger UI: `http://186.246.27.211:8000/docs`

### Deployment Model

- Docker Compose is the primary deployment mechanism.
- The backend Dockerfile builds a Python 3.12 image with the FastAPI application.
- PostgreSQL data is persisted via Docker volumes.
- Environment variables are passed through Docker Compose configuration.
- Password hashing is backend-owned; the database stores salted password hashes instead of plaintext passwords.
- Login and chat rate limits are enforced in the FastAPI backend with configurable fixed-window limits.
- `CORS_ALLOWED_ORIGINS` controls browser origins; production-like deployments must avoid wildcard origins.
- Android Room chat-history data is stored locally on the Android device and is not part of the backend deployment.
- CI does not automatically deploy the backend or publish an Android artifact.

## Maintenance Rules

This document must be updated when:

- The git workflow, branching strategy, or review process changes
- CI pipelines or quality gates are added, removed, or modified
- The deployment model or environment setup changes
- Configuration or secrets-management practices change
- New tools or platforms are adopted for development or delivery
