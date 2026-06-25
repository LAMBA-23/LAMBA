# Testing

This document is the current Assignment 4 testing status artifact for the repository. It focuses on the backend CI scope implemented in Part 8. Repository-wide quality-requirement and UAT documents owned by other teammates may still be pending merge into `main`.

## Critical Modules and Coverage

| Critical module | Why critical | Required line coverage | Current line coverage | Evidence |
|---|---|---:|---:|---|
| `backend/app/main.py` | Core API routes and orchestration for authentication, vehicle management, event tracking, chat parsing, and statistics workflows. | 30% | 95% | Local `coverage report` and backend CI workflow |
| `backend/app/chat_parser.py` | Converts user chat messages into structured product events for the main user workflow. | 30% | 59% | Local `coverage report` and backend CI workflow |
| `backend/app/database.py` | Database session and engine configuration underpin all persistent backend behavior. | 30% | 100% | Local `coverage report` and backend CI workflow |

## Automated Test Status

| Test type | Scope | Command or CI check | Latest result | Evidence |
|---|---|---|---|---|
| Unit and API tests | Backend business logic and API behavior in `backend/tests` | `python -m pytest backend/tests` | Passing locally on branch `135-configure-backend-ci` | Local verification before CI |
| Integration tests | FastAPI routes with SQLite-backed persistence through `TestClient` | `python -m pytest backend/tests` | Passing locally on branch `135-configure-backend-ci` | Local verification before CI |
| Automated QRTs | Backend quality checks automated in CI: linting, formatting, coverage, and dependency scan | GitHub Actions `Backend CI` workflow | Configured in this branch; protected-branch result pending merge and first run | `.github/workflows/backend-ci.yml` |

## CI and QA Check Status

| Gate or check | Required for Done? | Latest protected-branch status | Evidence |
|---|---|---|---|
| Linting | Yes | Pending first run after merge | GitHub Actions `Backend CI` workflow |
| Formatting check | Yes | Pending first run after merge | GitHub Actions `Backend CI` workflow |
| Automated tests | Yes | Pending first run after merge | GitHub Actions `Backend CI` workflow |
| Coverage reporting | Yes | Pending first run after merge | GitHub Actions coverage artifact |
| Additional QA check | Yes | Pending first run after merge | GitHub Actions dependency scan step |
| Link checking | Yes | Managed separately and already configured | `.github/workflows/lychee.yml` |

## Additional QA Check Rationale

| QA objective or risk | Additional QA check | Scope | Latest result | Evidence | Limitations or follow-up |
|---|---|---|---|---|---|
| Broken or incompatible Python dependency resolution could silently destabilize backend builds and tests. | `python -m pip check` dependency health check | Installed Python packages resolved from `backend/requirements.txt` | Will run in `Backend CI` on PRs and on `main` | `.github/workflows/backend-ci.yml` | Does not cover Android/Gradle dependencies; that requires a separate follow-up QA check if mobile CI is added later. |

## Manual Evidence That Does Not Count as QRT

| Evidence | Scope | Result | Follow-up PBI or issue |
|---|---|---|---|
| Smoke-check procedure in `README.md` | Manual verification of local backend startup, auth, vehicle, events, stats, and chat parsing endpoints | Available for repeatable manual regression checks | Course task `#135` for CI setup; future Week 4 report issue if separate evidence packaging is needed |

## Assignment 4 Repository Documentation Status

| Required document | Status in repository workstream | Notes |
|---|---|---|
| `docs/quality-requirements.md` | Pending separate PR | Mentioned by teammate workflow; not owned by Part 8 implementation branch |
| `docs/quality-requirement-tests.md` | Pending teammate work | Full traceability details depend on that file being merged |
| `docs/testing.md` | Implemented in this branch | Current canonical testing status artifact |
| `docs/user-acceptance-tests.md` | Pending teammate work | UAT evidence is outside the scope of Part 8 implementation on this branch |
