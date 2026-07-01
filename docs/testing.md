# Testing

This document is the maintained testing status artifact for the repository. It preserves the Assignment 4 testing and CI gates and records how those gates continue to apply to MVP v2 / Sprint 3 work.

## Contents

- [Critical Modules and Coverage](#critical-modules-and-coverage)
- [Automated Test Status](#automated-test-status)
- [Quality Requirement Test Evidence](#quality-requirement-test-evidence)
- [MVP v2 / Sprint 3 Extension Status](#mvp-v2--sprint-3-extension-status)
- [Events API Testing Evidence](#events-api-testing-evidence)
- [CI and QA Check Status](#ci-and-qa-check-status)
- [CI and Protected Branch Evidence](#ci-and-protected-branch-evidence)
- [Branch Protection and Rules Evidence](#branch-protection-and-rules-evidence)
- [Additional QA Checks](#additional-qa-checks)
- [Manual Evidence That Does Not Count as QRT](#manual-evidence-that-does-not-count-as-qrt)
- [Assignment 4 Repository Documentation Status](#assignment-4-repository-documentation-status)
- [Assignment 4 Gates Active for Later Work](#assignment-4-gates-active-for-later-work)

## Critical Modules and Coverage

| Critical module | Why critical | Required line coverage | Current line coverage | Evidence |
|---|---|---:|---:|---|
| `backend/app/main.py` | Core API routes and orchestration for authentication, vehicle management, event tracking, chat parsing, and statistics workflows. | 30% | 94% | Coverage report and backend CI workflow |
| `backend/app/chat_parser.py` | Converts user chat messages into structured product events for the main user workflow. | 30% | 59% | Coverage report and backend CI workflow |
| `backend/app/database.py` | Database session and engine configuration underpin all persistent backend behavior. | 30% | 100% | Coverage report and backend CI workflow |

## Automated Test Status

| Test type | Scope | Command or CI check | Latest result | Evidence |
|---|---|---|---|---|
| Unit and API tests | Backend business logic and API behavior in `backend/tests` | `python -m pytest backend/tests` | Current branch pre-PR verification: 55 passed; CI reruns this gate on PRs and `main` | Test output and backend CI |
| Integration tests | FastAPI routes with SQLite-backed persistence through `TestClient` | `python -m pytest backend/tests` | Current branch pre-PR verification: 55 passed; CI reruns this gate on PRs and `main` | Test output and backend CI |
| Events API tests | `GET /events` and `POST /events` success, validation, persistence, manual add flow, and user isolation behavior | `python -m pytest tests/test_events.py` from `backend/` | Covered by the backend pytest suite and backend CI | `backend/tests/test_events.py` |
| Automated QRTs | Backend quality checks automated in CI: linting, formatting, coverage, and dependency scan | GitHub Actions `Backend CI` workflow | Active in CI; current branch pre-PR verification passed | `.github/workflows/backend-ci.yml`; `backend/tests/test_quality_requirements.py` |
| Android unit tests | Android repository/network behavior covered by JVM unit tests | `.\gradlew.bat test` | Current branch pre-PR verification passed | `app/src/test/java/com/lamba/app/network/ChatRepositoryTest.kt` |

## Quality Requirement Test Evidence

The canonical detailed QRT artifact is `docs/quality-requirement-tests.md`.

| QRT evidence source | Purpose |
|---|---|
| `docs/quality-requirement-tests.md` | Defines the detailed mapping between quality requirements, automated QRTs, and test evidence |
| `docs/quality-requirements.md` | Defines the measurable quality requirements that QRTs verify |
| `backend/tests/test_quality_requirements.py` | Implements the automated QRT checks |
| `.github/workflows/backend-ci.yml` | Runs the backend test suite, including QRTs, before merge and on `main` |

## MVP v2 / Sprint 3 Extension Status

Sprint 3 is scoped around maintenance follow-up: US-08 maintenance recommendations and US-09 notifications. As of this testing update, the repository has Sprint 3 planning documentation, but it does not yet contain new backend endpoints, persistence models, or Android business logic for maintenance recommendations or notifications.

Because there is no implemented Sprint 3 product behavior yet, the current MVP v2 testing extension is documentation and evidence maintenance rather than adding speculative tests. New or changed tests must be added when US-08 or US-09 implementation work introduces product behavior.

Assignment 5 does not require fixed numeric growth such as a specific number of new unit or integration tests. Test growth is requirement-driven: automated verification should be extended when changed or newly important MVP v2 product areas need credible coverage. For the current repository state, the appropriate action is to preserve active Assignment 4 gates, document Sprint 3 coverage gaps, and add US-08/US-09 tests only after those features have concrete implementation behavior.

| MVP v2 / Sprint 3 area | Current repository state | Current verification evidence | Follow-up required when implemented |
|---|---|---|---|
| Maintenance recommendations (US-08) | Planned in `docs/roadmap.md` and `docs/user-stories.md`; no dedicated backend or Android implementation yet | Existing event, statistics, chat, and regression tests remain active | Add automated tests for recommendation rules, thresholds, data isolation, and API/UI behavior |
| Notifications (US-09) | Planned in `docs/roadmap.md` and `docs/user-stories.md`; notification icon assets exist, but no notification workflow logic is implemented yet | Android unit test suite and backend CI remain active | Add automated tests for notification trigger rules, unread/read state, and user-specific delivery behavior |
| Existing MVP event/history/statistics/chat areas | Implemented and still important for MVP v2 because maintenance follow-up depends on recorded vehicle history | Backend test suite, QRTs, coverage gate, Android unit tests, and CI workflow | Extend tests when Sprint 3 work changes these existing contracts |

## Events API Testing Evidence

This section records maintained Assignment 4 evidence for the events API work.

| Area | Scope | Automated evidence | Manual evidence | Latest result |
|---|---|---|---|---|
| Read timeline | `GET /events?user_id=<id>` returns only the requested user's events in ascending ID order. | `TestEventsApi.test_get_events_returns_only_user_events_in_stable_order` | Call `GET /events?user_id=<id>` after creating events for two users and confirm only that user's events are returned. | Covered by automated backend tests. |
| Manual add flow | `POST /events?user_id=<id>` saves a valid event and the event appears in the next timeline response. | `TestEventsApi.test_post_event_is_saved_and_uses_default_amount_and_mileage` | Send a valid `POST /events` request, then call `GET /events` for the same user and confirm the new event is present. | Covered by automated backend tests. |
| Default values | Missing `amount` is stored as `0`; missing `mileage` uses the user's current vehicle mileage. | `TestEventsApi.test_post_event_is_saved_and_uses_default_amount_and_mileage` | Create an event without `amount` and `mileage`, then inspect the response body. | Covered by automated backend tests. |
| Manual add regression | A new user's timeline starts empty, a valid manual event is saved, whitespace is trimmed from `description`, and the next timeline response returns that event. | `TestEventsApi.test_manual_add_flow_starts_empty_then_returns_created_event` | Create a user, confirm the timeline is empty, submit a valid `POST /events`, then confirm `GET /events` returns the created event. | Covered by automated backend tests. |
| Error handling | Missing `user_id` returns `422`; unknown `user_id` returns `404`. | `TestEventsApi.test_events_return_expected_errors_for_missing_or_unknown_user` | Call both endpoints without `user_id` and with an unknown `user_id`. | Covered by automated backend tests. |
| Input validation | Invalid event type, empty `description`, negative `amount`, and negative `mileage` return `422`. | `TestEventsApi.test_post_event_rejects_invalid_payload` | Submit invalid payloads and confirm no valid event is created. | Covered by automated backend tests. |

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
- Sprint 3 US-08 and US-09 automated tests should be added after those features have concrete implementation behavior.
- Protected default-branch CI evidence should be linked from the current Sprint report after the branch is merged and the CI run is available.

## CI and QA Check Status

| Gate or check | Required for Done? | Latest protected-branch status | Evidence |
|---|---|---|---|
| Linting | Yes | Passing in Backend CI run `28509027660` on `main` | [Backend CI runs](https://github.com/LAMBA-23/LAMBA/actions/workflows/backend-ci.yml?query=branch%3Amain) |
| Formatting check | Yes | Passing in Backend CI run `28509027660` on `main` | [Backend CI runs](https://github.com/LAMBA-23/LAMBA/actions/workflows/backend-ci.yml?query=branch%3Amain) |
| Type checking | Yes, where configured | Kotlin/Android type checking is covered by Gradle Kotlin compilation during `.\gradlew.bat test`; dedicated Python static type checking is not currently configured | Gradle test output; follow-up required if the team adopts mypy or pyright |
| Automated tests | Yes | Passing in Backend CI run `28509027660` on `main` | [Backend CI runs](https://github.com/LAMBA-23/LAMBA/actions/workflows/backend-ci.yml?query=branch%3Amain) |
| Coverage reporting | Yes | Passing in Backend CI run `28509027660` on `main`; current branch pre-PR verification reported 89% total backend coverage | Backend CI coverage artifact |
| Additional QA check | Yes | Passing in Backend CI run `28509027660` on `main` | `python -m pip check` step in [Backend CI runs](https://github.com/LAMBA-23/LAMBA/actions/workflows/backend-ci.yml?query=branch%3Amain) |
| Link checking | Yes | Passing in Link Check run `28509027693` on `main` | [Link Check runs](https://github.com/LAMBA-23/LAMBA/actions/workflows/lychee.yml?query=branch%3Amain) |

## CI and Protected Branch Evidence

| Evidence | Link or reference | Latest protected-default-branch result |
|---|---|---|
| Backend CI workflow | `.github/workflows/backend-ci.yml`; [Backend CI runs](https://github.com/LAMBA-23/LAMBA/actions/workflows/backend-ci.yml?query=branch%3Amain) | `success` for run `28509027660` on `main`, commit `90d4d1c`, 2026-07-01T09:52:58Z |
| Link Check workflow | `.github/workflows/lychee.yml`; [Link Check runs](https://github.com/LAMBA-23/LAMBA/actions/workflows/lychee.yml?query=branch%3Amain) | `success` for run `28509027693` on `main`, commit `90d4d1c`, 2026-07-01T09:52:58Z |
| All GitHub Actions workflows | [Repository Actions](https://github.com/LAMBA-23/LAMBA/actions) | Use for the latest PR and protected-default-branch status after this branch is merged |

## Branch Protection and Rules Evidence

The protected default branch is `main`.

| Rule or evidence | Current documented status | Evidence |
|---|---|---|
| Required pull request review | Enabled | `reports/week4/images/branch-protection-review-required.png` |
| Branch protection rules list | Available as screenshot evidence | `reports/week4/images/branch-protection-rules-list.png` |
| Additional branch protection settings | Available as screenshot evidence | `reports/week4/images/branch-protection-rules-extra.png` |
| Repository branch overview | Public repository branch view | [Repository branches](https://github.com/LAMBA-23/LAMBA/branches) |

The latest branch protection status should be rechecked in GitHub repository settings if rules are changed after this evidence is merged.

## Additional QA Checks

### Python Dependency Health Check

**Rationale:** Broken or incompatible Python dependency resolution could silently destabilize backend builds and tests.

**Scope:** Installed Python packages resolved from `backend/requirements.txt`.

**Where to look for the latest result:** The `Additional QA: dependency health check` step in the linked Backend CI run.

**Evidence:** `.github/workflows/backend-ci.yml`; [Backend CI runs](https://github.com/LAMBA-23/LAMBA/actions/workflows/backend-ci.yml?query=branch%3Amain).

**Limitations or follow-up:** This check does not cover Android/Gradle dependencies. A separate mobile dependency or vulnerability check should be added if mobile CI is expanded later.

## Manual Evidence That Does Not Count as QRT

| Evidence | Scope | Result | Follow-up PBI or issue |
|---|---|---|---|
| Smoke-check procedure in `README.md` | Manual verification of local backend startup, auth, vehicle, events, stats, and chat parsing endpoints | Available for repeatable manual regression checks | Course task `#135` for CI setup; future Week 4 report issue if separate evidence packaging is needed |
| Events API manual add/read check | Manual verification that a valid `POST /events?user_id=<id>` creates an event and the next `GET /events?user_id=<id>` returns it | Documented as repeatable evidence in this file | Course task `#146`; PBI `#149` |

## Assignment 4 Repository Documentation Status

| Required document | Status in repository workstream | Notes |
|---|---|---|
| `docs/quality-requirements.md` | Maintained | Current quality requirements remain active for MVP v2 until Sprint 3 behavior requires changes |
| `docs/quality-requirement-tests.md` | Maintained | QRT traceability is present and points to `backend/tests/test_quality_requirements.py` |
| `docs/testing.md` | Maintained | Current canonical testing status artifact |
| `docs/user-acceptance-tests.md` | Maintained separately | UAT evidence remains available for product-level manual verification |

## Assignment 4 Gates Active for Later Work

| Assignment 4 gate | Later-work status |
|---|---|
| Backend linting with Ruff | Remains active through `Backend CI` |
| Backend formatting check with Ruff | Remains active through `Backend CI` |
| Backend automated tests | Remain active through `Backend CI` |
| Backend coverage gate for critical modules | Remains active through `coverage report --include="app/*" --fail-under=30` |
| Automated QRTs | Remain active through `backend/tests/test_quality_requirements.py` and `Backend CI` |
| Additional automated QA check beyond Lychee | Remains active as `python -m pip check` in `Backend CI` |
| Lychee link checking | Remains active through the separate `Link Check` workflow |
| Definition of Done evidence | Remains active through `docs/definition-of-done.md` and issue/PR review evidence |
