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
| Events API tests | `GET /events` and `POST /events` success, validation, persistence, manual add flow, and user isolation behavior | `python -m pytest tests/test_events.py` from `backend/` | Passing locally on branch `149-finalize-events-manual-add` | `backend/tests/test_events.py` |
| Automated QRTs | Backend quality checks automated in CI: linting, formatting, coverage, and dependency scan | GitHub Actions `Backend CI` workflow | Configured in this branch; protected-branch result pending merge and first run | `.github/workflows/backend-ci.yml` |

## Events API Testing Evidence

This section records maintained Assignment 4 evidence for the events API work.

| Area | Scope | Automated evidence | Manual evidence | Latest result |
|---|---|---|---|---|
| Read timeline | `GET /events?user_id=<id>` returns only the requested user's events in ascending ID order. | `TestEventsApi.test_get_events_returns_only_user_events_in_stable_order` | Call `GET /events?user_id=<id>` after creating events for two users and confirm only that user's events are returned. | Automated test passing locally. |
| Manual add flow | `POST /events?user_id=<id>` saves a valid event and the event appears in the next timeline response. | `TestEventsApi.test_post_event_is_saved_and_uses_default_amount_and_mileage` | Send a valid `POST /events` request, then call `GET /events` for the same user and confirm the new event is present. | Automated test passing locally. |
| Default values | Missing `amount` is stored as `0`; missing `mileage` uses the user's current vehicle mileage. | `TestEventsApi.test_post_event_is_saved_and_uses_default_amount_and_mileage` | Create an event without `amount` and `mileage`, then inspect the response body. | Automated test passing locally. |
| Manual add regression | A new user's timeline starts empty, a valid manual event is saved, whitespace is trimmed from `description`, and the next timeline response returns that event. | `TestEventsApi.test_manual_add_flow_starts_empty_then_returns_created_event` | Create a user, confirm the timeline is empty, submit a valid `POST /events`, then confirm `GET /events` returns the created event. | Automated test passing locally. |
| Error handling | Missing `user_id` returns `422`; unknown `user_id` returns `404`. | `TestEventsApi.test_events_return_expected_errors_for_missing_or_unknown_user` | Call both endpoints without `user_id` and with an unknown `user_id`. | Automated test passing locally. |
| Input validation | Invalid event type, empty `description`, negative `amount`, and negative `mileage` return `422`. | `TestEventsApi.test_post_event_rejects_invalid_payload` | Submit invalid payloads and confirm no valid event is created. | Automated test passing locally. |

Recommended local verification commands:

```bash
cd backend
python -m pytest tests/test_events.py
python -m pytest tests/test_events.py tests/test_chat_parse.py
python -m coverage run -m pytest tests
python -m coverage report --include="app/*" --fail-under=30
```

Traceability:

- `backend/tests/test_events.py` supports `QR-001: Vehicle event data integrity`.
- `GET /events` response checks support `QR-002: Timeline API response time` as functional readiness evidence.
- `docs/api-contract.md` describes the maintained request/response contract for `GET /events` and `POST /events`.
- `README.md` contains repeatable local smoke-check commands for manual backend verification.

Limitations and follow-up:

- Manual evidence does not count as an automated quality requirement test.
- Exact QRT links should be added after `docs/quality-requirement-tests.md` is available on `main`.
- Protected default-branch CI evidence should be linked from the Week 4 report after the branch is merged and the CI run is available.

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
| Events API manual add/read check | Manual verification that a valid `POST /events?user_id=<id>` creates an event and the next `GET /events?user_id=<id>` returns it | Documented as repeatable evidence in this file | Course task `#146`; PBI `#149` |

## Assignment 4 Repository Documentation Status

| Required document | Status in repository workstream | Notes |
|---|---|---|
| `docs/quality-requirements.md` | Pending separate PR | Mentioned by teammate workflow; not owned by Part 8 implementation branch |
| `docs/quality-requirement-tests.md` | Pending teammate work | Full traceability details depend on that file being merged |
| `docs/testing.md` | Implemented in this branch | Current canonical testing status artifact |
| `docs/user-acceptance-tests.md` | Pending teammate work | UAT evidence is outside the scope of Part 8 implementation on this branch |
