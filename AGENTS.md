# AGENTS.md

## Purpose

This document defines the repository workflow, project conventions, verification expectations, documentation responsibilities, and development constraints for changes in the LAMBA repository.

## Scope

These rules apply to all repository changes, including:

- Android application code and resources.
- Backend API, persistence, validation, and business behavior.
- Maintained project documentation.
- Automated and manual testing evidence.
- GitHub Actions workflows and CI evidence.
- Assignment deliverables, reports, and customer-facing evidence packages.

## Project Overview

LAMBA is an Android-first digital twin application for a vehicle owner. The product records vehicle profile data, trip and service history, fuel and repair expenses, statistics, and conversational assistant interactions around the saved vehicle history.

The current repository contains:

- A Kotlin Android application with login, registration, vehicle setup, chat, history, statistics, and manual history-record flows.
- A FastAPI backend that owns authentication, vehicle data, event persistence, statistics calculation, chat parsing, and assistant-answer endpoints.
- PostgreSQL persistence for backend runtime data.
- Docker Compose configuration for local backend and database execution.
- Maintained documentation for API contracts, testing, quality requirements, architecture, roadmap, user stories, and assignment reporting.

## Repository Structure

- `app/` - Android application source, layouts, drawables, launcher assets, networking code, session handling, and JVM unit tests.
- `backend/` - FastAPI backend source, SQLAlchemy models, Pydantic schemas, database setup, chat parsing, assistant integration, Dockerfile, Python dependencies, and pytest suite.
- `docs/` - Maintained project documentation, including API contract, development process, Definition of Done, testing status, quality requirements, user stories, UAT, roadmap, and architecture.
- `docs/architecture/` - Architecture entry point, PlantUML diagrams, and ADRs for frontend/backend boundaries, backend stack, and Docker Compose deployment.
- `reports/` - Assignment reports and evidence organized by week. The current repository contains `week2` through `week5`.
- `.github/ISSUE_TEMPLATE/` - Structured GitHub issue templates for user stories, PBIs, bug reports, and course tasks.
- `.github/workflows/` - CI and publishing workflows for backend checks, Android unit tests, Markdown link checks, and hosted documentation.
- `README.md` - Product overview, setup instructions, smoke checks, release notes, and runnable artifact information.
- `CHANGELOG.md` - Keep a Changelog style release history for user-visible changes.
- `docker-compose.yml` - Local backend and PostgreSQL runtime definition.
- `build.gradle.kts`, `settings.gradle.kts`, `gradle/`, `gradlew`, `gradlew.bat` - Android Gradle build configuration and wrapper.
- `index.md`, `_config.yml`, `_layouts/` - Hosted documentation site entry point and Jekyll configuration.

`CONTRIBUTING.md`, `docs/customer-handover.md`, `reports/week6/`, and `reports/week7/` are not present in the current repository snapshot. Add links to them only after those files or directories exist.

## Technology Stack

- Android: Kotlin, Android Gradle Plugin, XML layouts, AppCompat, Material Components, ConstraintLayout, RecyclerView, coroutines, Retrofit, Gson converter, JUnit.
- Backend: Python 3.12, FastAPI, Uvicorn, SQLAlchemy, Pydantic, PostgreSQL, psycopg2.
- Testing and quality: pytest, FastAPI TestClient, coverage.py, Ruff, Android JVM unit tests, Lychee link checking.
- Deployment and runtime: Docker Compose with PostgreSQL 16 and the backend container.
- CI: GitHub Actions workflows for backend checks, Android unit tests, Markdown link checking, and GitHub Pages documentation publishing.

## Development Workflow

The repository uses a feature-branch workflow with protected `main`, linked GitHub issues, pull requests, CI checks, and review by another team member.

Expected flow:

1. Start from the current `main`.
2. Create a branch linked to the issue using the documented naming pattern, for example `<issue-number>-short-description`.
3. Keep the change focused on the issue scope and acceptance criteria.
4. Update relevant tests and documentation when behavior, setup, API contracts, or evidence requirements change.
5. Run the relevant local checks before review.
6. Submit a pull request only when explicitly requested.
7. Preserve issue, branch, PR, review, CI, and verification traceability.

The project documentation records the full workflow in `docs/development-process.md`.

## Repository Rules

- Change only files required by the current task.
- Preserve the current Android/backend boundary: Android owns user interaction and client state; backend owns validation, persistence, business rules, statistics, and external chat-service access.
- Keep the REST/JSON API contract synchronized between `LambaApiService`, backend schemas/routes, and `docs/api-contract.md`.
- Avoid unrelated refactoring while completing a scoped issue.
- Keep user-visible behavior traceable to issues, acceptance criteria, tests, and maintained documentation.
- Keep release and changelog evidence consistent with the current versioned increment.
- Do not add secrets, credentials, private customer evidence, local environment files, or Moodle-only evidence to the repository.

## Documentation Responsibilities

Documentation must be updated when the change affects the documented behavior or evidence:

- API request/response, validation, or endpoint behavior changes require updates to `docs/api-contract.md`.
- User workflow, customer scenario, or acceptance behavior changes require updates to `docs/user-acceptance-tests.md` when the UAT scenarios are affected.
- Setup, runtime, smoke-check, deployment URL, or runnable artifact changes require updates to `README.md`.
- Backend or Android verification changes require updates to `docs/testing.md` and, when applicable, `docs/quality-requirement-tests.md`.
- New or changed measurable quality expectations require updates to `docs/quality-requirements.md`.
- Architecture, deployment model, major boundaries, or persistence decisions require updates to `docs/architecture/` and relevant ADRs.
- Workflow, branching, CI, release, or configuration-management changes require updates to `docs/development-process.md`.
- Completion-standard changes require updates to `docs/definition-of-done.md`.
- User-visible functionality, fixes, and release-relevant changes require an entry in `CHANGELOG.md` unless the PR explicitly records why it is not applicable.
- Assignment reporting changes require updates to the relevant `reports/week*` directory that exists for the assignment.
- Customer handover documentation must be updated when project transfer, customer-facing setup, or delivery instructions change.

## Documentation Update Checklist

When functionality changes, check whether each document needs an update:

- `README.md`
- `CHANGELOG.md`
- `docs/api-contract.md`
- `docs/user-acceptance-tests.md`
- `docs/testing.md`
- `docs/quality-requirements.md`
- `docs/quality-requirement-tests.md`
- `docs/definition-of-done.md`
- `docs/development-process.md`
- `docs/architecture/README.md`
- `docs/roadmap.md`
- `docs/user-stories.md`
- `reports/week5/README.md`
- `docs/customer-handover.md`
- `reports/week6/`

## Testing Expectations

A change is ready for review only when the relevant completion evidence is in place:

- The changed project area builds successfully.
- Relevant backend, Android, documentation, or link checks pass.
- CI-equivalent checks are run locally when practical, or the reason for not running them is recorded.
- Acceptance criteria from the linked issue are satisfied and have observable evidence.
- Documentation is updated where the change affects setup, behavior, API contracts, verification, process, roadmap, architecture, or assignment evidence.
- The change does not introduce secrets, private evidence, or unsupported public data.

For documentation-only changes, at minimum run `git diff --check` and consider the Lychee Markdown link check if links were added or changed.

## Build and Verification

Use only commands that are documented or configured in the repository.

Backend and database runtime:

```bash
docker compose up --build
docker compose down
```

Backend tests from the Compose environment:

```bash
docker compose run --rm backend pytest tests -q
docker compose run --rm backend pytest tests/test_events.py tests/test_stats.py tests/test_chat_ask.py -q
docker compose run --rm backend sh -lc "coverage run -m pytest tests && coverage report --include='app/*' --fail-under=30"
```

Backend CI commands from `backend/`:

```bash
python -m ruff check app tests
python -m ruff format --check app tests
python -m coverage run -m pytest tests
python -m coverage report --include="app/*" --fail-under=30
python -m pip check
```

Android checks:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

Markdown and diff hygiene:

```bash
git diff --check
```

GitHub Actions currently run backend CI, Android unit tests, Markdown link checks, and hosted documentation publishing through `.github/workflows/`.

## Coding Guidelines

- Keep Android UI text consistent with the existing Russian-language screens, labels, validation messages, and content descriptions.
- Reuse the existing Activity-based Android structure, XML layout approach, drawable resources, Retrofit API service, models, repositories, and session helpers.
- Preserve the current navigation between welcome, login, registration, vehicle setup, main, chat, history, and statistics screens unless the issue explicitly changes navigation.
- Keep manual history-record behavior aligned across `HistoryActivity`, `HistoryRecordEventMapper`, backend event routes, statistics responses, and Android tests.
- Keep backend route behavior compatible with the documented API contract and existing Android client models.
- Prefer extending existing pytest and Android JVM test coverage for changed behavior over adding unrelated test scaffolding.
- Preserve the compact backend structure unless a scoped issue requires a documented architecture change.
- Avoid duplicate UI, duplicate API models, and parallel event/statistics mapping logic when an existing component already owns that behavior.

## Safety Rules

- Never commit credentials, tokens, API keys, `.env` files, local secret stores, or private customer evidence.
- Use placeholders for deployment-only or secret values in committed documentation.
- Keep Timeweb, database, and deployment credentials outside the Android client and outside committed files.
- Do not publish customer-identifying information or private Moodle evidence in repository documentation.
- Confirm before deleting files, replacing evidence, modifying workflow rules, or changing customer-facing documentation.

## Before Making Repository Changes

Always:

- Inspect the existing implementation and documentation for the changed area.
- Check whether the issue affects Android, backend, API contracts, tests, CI, architecture, assignment reports, or customer evidence.
- Keep the change limited to the requested scope.
- Preserve consistency between implementation, tests, documentation, and changelog entries.
- Record TODOs only when the repository does not provide enough information to state a verified command, document path, or process.

## Changes Requiring Confirmation

The following actions require explicit confirmation before execution:

- Creating issues.
- Creating pull requests.
- Creating commits.
- Creating releases.
- Modifying milestones.
- Modifying repository workflow.
- Editing GitHub workflow configuration.
- Changing architecture.
- Changing API contracts.
- Deleting files.
- Deleting documentation.
- Changing deployment instructions.
- Modifying customer-facing documentation.
- Modifying UAT scenarios.
- Modifying customer handover documentation.
- Modifying CI configuration.

## Assignment Documentation

Current Assignment-related documentation in this repository:

- [Week 5 report](reports/week5/README.md)
- [Testing status](docs/testing.md)
- [User acceptance tests](docs/user-acceptance-tests.md)
- [Definition of Done](docs/definition-of-done.md)
- [Development process](docs/development-process.md)

## References

- [README](README.md)
- [CHANGELOG](CHANGELOG.md)
- [Development process](docs/development-process.md)
- [Definition of Done](docs/definition-of-done.md)
- [Testing](docs/testing.md)
- [API contract](docs/api-contract.md)
- [Quality requirements](docs/quality-requirements.md)
- [Quality requirement tests](docs/quality-requirement-tests.md)
- [User acceptance tests](docs/user-acceptance-tests.md)
- [User stories](docs/user-stories.md)
- [Roadmap](docs/roadmap.md)
- [Architecture](docs/architecture/README.md)
- [Week 5 report](reports/week5/README.md)
- [Pull request template](.github/pull_request_template.md)
- [Backend CI workflow](.github/workflows/backend-ci.yml)
- [Android CI workflow](.github/workflows/android-ci.yml)
- [Link Check workflow](.github/workflows/lychee.yml)
