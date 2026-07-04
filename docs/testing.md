# Testing

This document is the maintained testing status artifact for the repository. It preserves the Assignment 4 testing and CI gates and records how those gates continue to apply to MVP v2 / Sprint 3 work.

## Contents

- [Critical Modules and Coverage](#critical-modules-and-coverage)
- [Automated Test Status](#automated-test-status)
- [Quality Requirement Test Evidence](#quality-requirement-test-evidence)
- [MVP v2 / Sprint 3 Extension Status](#mvp-v2--sprint-3-extension-status)
- [Changed-Area Verification Evidence](#changed-area-verification-evidence)
- [CI and QA Check Status](#ci-and-qa-check-status)
- [CI and Protected Branch Evidence](#ci-and-protected-branch-evidence)
- [Branch Protection and Rules Evidence](#branch-protection-and-rules-evidence)
- [Additional QA Checks](#additional-qa-checks)
- [Manual Evidence That Does Not Count as QRT](#manual-evidence-that-does-not-count-as-qrt)
- [Repository Documentation Status](#repository-documentation-status)
- [Assignment 4 Gates Active for Later Work](#assignment-4-gates-active-for-later-work)

## Critical Modules and Coverage

| Critical module | Why critical | Required line coverage | Latest verified line coverage | Evidence |
|---|---|---:|---:|---|
| `backend/app/main.py` | Core API routes and orchestration for authentication, vehicle management, event tracking, assistant context, and statistics workflows. | 30% | 94% | Local coverage gate on 2026-07-04 and backend CI workflow |
| `backend/app/chat_parser.py` | Converts user chat messages into structured product events for a main user workflow. | 30% | 85% | Local coverage gate on 2026-07-04 and backend CI workflow |
| `backend/app/database.py` | Database session and engine configuration underpin all persistent backend behavior. | 30% | 100% | Local coverage gate on 2026-07-04 and backend CI workflow |

## Automated Test Status

| Test type | Scope | Command or CI check | Latest verified result | Evidence |
|---|---|---|---|---|
| Backend regression suite | Backend business logic, API behavior, and persistence flows in `backend/tests` | `docker compose run --rm backend pytest tests -q` | Local verification on 2026-07-04: `58 passed` | Local Docker pytest output; backend CI reruns the same suite |
| Backend unit tests | Isolated parser, AI adapter, statistics helper, and model behavior covered by backend pytest files | Included in `docker compose run --rm backend pytest tests -q` and the `Backend CI` pytest step | Covered by the full backend verification on 2026-07-04: `58 passed` | `backend/tests/test_chat_parse.py`; `backend/tests/test_chat_parser.py`; `backend/tests/test_chat_parser_deepseek.py`; `backend/tests/test_deepseek_chat.py`; `backend/tests/test_stats.py` |
| Backend integration tests | FastAPI routes with test persistence through `TestClient` for authentication, vehicle, event, assistant, statistics, and QRT flows | Included in `docker compose run --rm backend pytest tests -q` and the `Backend CI` pytest step | Covered by the full backend verification on 2026-07-04: `58 passed` | `backend/tests/test_auth.py`; `backend/tests/test_vehicle.py`; `backend/tests/test_events.py`; `backend/tests/test_chat_ask.py`; `backend/tests/test_quality_requirements.py` |
| Automated QRT subset | QRT suite plus newly relevant assistant and statistics behavior | `docker compose run --rm backend pytest tests/test_quality_requirements.py tests/test_chat_ask.py tests/test_stats.py -q` | Local verification on 2026-07-04: `21 passed` | Local Docker pytest output |
| Coverage gate | Full backend suite plus critical-module coverage threshold | `docker compose run --rm backend sh -lc "coverage run -m pytest tests && coverage report --include='app/*' --fail-under=30"` | Local verification on 2026-07-04: `58 passed`; total backend coverage `95%`; gate passed | Local Docker coverage output; backend CI |
| Automated QRTs in CI | Backend quality checks automated in CI: pytest suite, coverage gate, linting, formatting, dependency health | GitHub Actions `Backend CI` workflow | Active in CI on pull requests and `main` | `.github/workflows/backend-ci.yml` |
| Android unit tests | Android repository/network behavior covered by JVM unit tests | `.\gradlew.bat :app:testDebugUnitTest` locally; GitHub Actions `Android CI` workflow | Configured in CI for pull requests and `main`; protected-default-branch result becomes available after this workflow is merged | `.github/workflows/android-ci.yml`; `app/src/test/java/com/lamba/app/network/ChatRepositoryTest.kt`; `app/src/test/java/com/lamba/app/network/StatsModelTest.kt` |

## Quality Requirement Test Evidence

The canonical detailed QRT artifact is `docs/quality-requirement-tests.md`.

| QRT evidence source | Purpose |
|---|---|
| `docs/quality-requirement-tests.md` | Defines the detailed mapping between quality requirements, automated QRTs, and test evidence |
| `docs/quality-requirements.md` | Defines the measurable quality requirements that QRTs verify |
| `backend/tests/test_quality_requirements.py` | Implements QRT-001 through QRT-003 directly |
| `backend/tests/test_chat_ask.py` | Provides automated evidence for QRT-004 |
| `backend/tests/test_stats.py` | Provides automated evidence for QRT-005 |
| `.github/workflows/backend-ci.yml` | Runs the backend pytest suite and coverage gate that enforce QRT-003 through QRT-006 |

## MVP v2 / Sprint 3 Extension Status

Sprint 3 is scoped around maintenance follow-up, but the repository already contains implemented MVP v2 behavior beyond the Assignment 4 baseline:

- manual vehicle-history record creation through `POST /events`
- assistant question handling through `POST /chat/ask`
- expanded statistics behavior through `GET /stats`

Assignment 5 does not require fixed numeric growth such as a specific number of new unit or integration tests. Test growth is requirement-driven. For the current repository state, the correct action is to keep all Assignment 4 gates active and extend maintained QR/QRT evidence for the changed manual-add, assistant, statistics, and coverage-gate areas that now exist in code.

| MVP v2 / Sprint 3 area | Current repository state | Current verification evidence | Follow-up required |
|---|---|---|---|
| Manual vehicle-history creation | Implemented in backend event creation flow and product scope | `backend/tests/test_events.py`; `backend/tests/test_quality_requirements.py`; backend CI | Extend tests when event contract or Android manual form behavior changes |
| Assistant context handling | Implemented in `POST /chat/ask` | `backend/tests/test_chat_ask.py`; backend regression suite; backend CI | Extend tests if context composition or app-side question flow changes |
| Statistics behavior | Implemented in `GET /stats` and helper logic | `backend/tests/test_stats.py`; backend regression suite; backend CI | Extend tests if new statistics fields or recommendation-driving calculations are added |
| Maintenance recommendations (US-08) | Planned in `docs/roadmap.md` and `docs/user-stories.md`; no dedicated recommendation logic yet | Existing event, assistant, and statistics tests remain active as prerequisites | Add automated tests after implementation |
| Notifications (US-09) | Planned in `docs/roadmap.md` and `docs/user-stories.md`; no notification workflow logic yet | Existing backend and local Android regression checks remain active for surrounding flows | Add automated tests after implementation |

## Changed-Area Verification Evidence

| Area | Scope | Automated evidence | Latest verified result |
|---|---|---|---|
| Event validation and persistence integrity | Invalid event payloads, missing or unknown users, and no-write-on-reject behavior | `backend/tests/test_quality_requirements.py` | Passed on 2026-07-04 as part of `21 passed` targeted verification |
| Timeline response time | `GET /events` response time with 20-event dataset | `backend/tests/test_quality_requirements.py::test_get_events_responds_within_2_seconds` | Passed on 2026-07-04 |
| Assistant context composition | Vehicle profile inclusion, placeholder omission, and 50-event limit | `backend/tests/test_chat_ask.py` | Passed on 2026-07-04 as part of `21 passed` targeted verification |
| Statistics correctness | Mileage deltas, period totals, record counts, and fuel-liter totals | `backend/tests/test_stats.py` | Passed on 2026-07-04 as part of `21 passed` targeted verification |
| Critical-module coverage gate | Full backend suite plus `--fail-under=30` threshold | Full coverage command listed below | Passed on 2026-07-04 with `95%` total coverage |

Recommended local verification commands:

```bash
docker compose run --rm backend pytest tests/test_quality_requirements.py tests/test_chat_ask.py tests/test_stats.py -q
docker compose run --rm backend pytest tests -q
docker compose run --rm backend sh -lc "coverage run -m pytest tests && coverage report --include='app/*' --fail-under=30"
```

Limitations and follow-up:

- Manual evidence does not count as a quality requirement test.
- Android unit tests are now configured in the `Android CI` workflow. The first protected-default-branch result will be available after this workflow is merged to `main`.
- US-08 and US-09 automated tests should be added only after those features have concrete implementation behavior.

## CI and QA Check Status

| Gate or check | Required for Done? | Latest documented protected-branch status | Evidence |
|---|---|---|---|
| Linting | Yes | Passing in Backend CI run `28672228375` on `main` | [Backend CI runs](https://github.com/LAMBA-23/LAMBA/actions/workflows/backend-ci.yml?query=branch%3Amain) |
| Formatting check | Yes | Passing in Backend CI run `28672228375` on `main` | [Backend CI runs](https://github.com/LAMBA-23/LAMBA/actions/workflows/backend-ci.yml?query=branch%3Amain) |
| Automated backend tests | Yes | Passing in Backend CI run `28672228375` on `main` | [Backend CI runs](https://github.com/LAMBA-23/LAMBA/actions/workflows/backend-ci.yml?query=branch%3Amain) |
| Automated Android unit tests | Yes | Configured in this branch; first protected `main` result pending until `.github/workflows/android-ci.yml` is merged | `.github/workflows/android-ci.yml` |
| Coverage reporting | Yes | Passing in Backend CI run `28672228375` on `main` | Backend CI coverage artifact and local verification on 2026-07-04 |
| Additional QA check | Yes | Passing in Backend CI run `28672228375` on `main` | `python -m pip check` step in [Backend CI runs](https://github.com/LAMBA-23/LAMBA/actions/workflows/backend-ci.yml?query=branch%3Amain) |
| Link checking | Yes | Passing in Link Check run `28672228348` on `main` | [Link Check runs](https://github.com/LAMBA-23/LAMBA/actions/workflows/lychee.yml?query=branch%3Amain) |

## CI and Protected Branch Evidence

| Evidence | Link or reference | Latest protected-default-branch result |
|---|---|---|
| Backend CI workflow | `.github/workflows/backend-ci.yml`; [Backend CI runs](https://github.com/LAMBA-23/LAMBA/actions/workflows/backend-ci.yml?query=branch%3Amain) | `success` for run `28672228375` on `main`, commit `727831f`, 2026-07-03T16:18:47Z |
| Android CI workflow | `.github/workflows/android-ci.yml` | Added in this branch; first protected-default-branch result pending until the workflow is merged to `main` |
| Link Check workflow | `.github/workflows/lychee.yml`; [Link Check runs](https://github.com/LAMBA-23/LAMBA/actions/workflows/lychee.yml?query=branch%3Amain) | `success` for run `28672228348` on `main`, commit `727831f`, 2026-07-03T16:18:47Z |
| All GitHub Actions workflows | [Repository Actions](https://github.com/LAMBA-23/LAMBA/actions) | Use for the latest PR and protected-default-branch status after this branch is merged |

## Branch Protection and Rules Evidence

The protected default branch is `main`.

| Rule or evidence | Current documented status | Evidence |
|---|---|---|
| Required pull request review | Enabled | `reports/week4/images/branch-protection-review-required.png` |
| Branch protection rules list | Available as screenshot evidence | `reports/week4/images/branch-protection-rules-list.png` |
| Additional branch protection settings | Available as screenshot evidence | `reports/week4/images/branch-protection-rules-extra.png` |
| Repository branch overview | Public repository branch view | [Repository branches](https://github.com/LAMBA-23/LAMBA/branches) |

## Additional QA Checks

### Python Dependency Health Check

**Rationale:** Broken or incompatible Python dependency resolution could silently destabilize backend builds and tests.

**Scope:** Installed Python packages resolved from `backend/requirements.txt`.

**Where to look for the latest result:** The `Additional QA: dependency health check` step in the linked Backend CI run.

**Evidence:** `.github/workflows/backend-ci.yml`; [Backend CI runs](https://github.com/LAMBA-23/LAMBA/actions/workflows/backend-ci.yml?query=branch%3Amain).

**Limitations or follow-up:** This check does not cover Android/Gradle dependencies. Android unit tests are covered by the separate `Android CI` workflow, but a separate mobile dependency or vulnerability check should be added if mobile CI is expanded later.

## Manual Evidence That Does Not Count as QRT

| Evidence | Scope | Result | Follow-up |
|---|---|---|---|
| Smoke-check procedure in `README.md` | Manual verification of local backend startup, auth, vehicle, events, stats, and chat endpoints | Available for repeatable manual regression checks | Useful as manual support evidence only |
| Events API manual add/read check | Manual verification that `POST /events` creates an event and `GET /events` returns it | Available as repeatable exploratory evidence | Does not count as QRT because it is manual |

## Repository Documentation Status

| Required document | Status | Notes |
|---|---|---|
| `docs/quality-requirements.md` | Maintained | Now includes Assignment 4 QR set plus new QR-004 through QR-006 for implemented MVP v2 behavior |
| `docs/quality-requirement-tests.md` | Maintained | Now includes QRT-004 through QRT-006 for assistant, statistics, and coverage evidence |
| `docs/testing.md` | Maintained | Current canonical testing status artifact for Assignment 4 gates plus MVP v2 extension evidence |
| `docs/definition-of-done.md` | Maintained | Current completion standard aligned with active QR/QRT evidence |

## Assignment 4 Gates Active for Later Work

| Assignment 4 gate | Later-work status |
|---|---|
| Backend linting with Ruff | Remains active through `Backend CI` |
| Backend formatting check with Ruff | Remains active through `Backend CI` |
| Backend automated tests | Remain active through `Backend CI` |
| Backend coverage gate for critical modules | Remains active through `coverage report --include="app/*" --fail-under=30` |
| Automated QRTs | Remain active and are extended through QRT-004 to QRT-006 |
| Additional automated QA check beyond Lychee | Remains active as `python -m pip check` in `Backend CI` |
| Lychee link checking | Remains active through the separate `Link Check` workflow |
| Definition of Done evidence | Remains active through `docs/definition-of-done.md` and issue/PR review evidence |
