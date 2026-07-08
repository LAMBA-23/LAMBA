# Testing

This document is the maintained testing status artifact for the repository. It keeps the previously established backend, CI, coverage, and documentation gates active and records the current verification evidence for Assignment 6 / Part 6 follow-up work after MVP v3 Sprint 4 and Sprint 5 fixes.

## Contents

- [Critical Modules and Coverage](#critical-modules-and-coverage)
- [Automated Test Status](#automated-test-status)
- [Quality Requirement Test Evidence](#quality-requirement-test-evidence)
- [Assignment 6 / MVP v3 Follow-up Verification](#assignment-6--mvp-v3-follow-up-verification)
- [CI and QA Check Status](#ci-and-qa-check-status)
- [CI and Protected Branch Evidence](#ci-and-protected-branch-evidence)
- [Branch Protection and Rules Evidence](#branch-protection-and-rules-evidence)
- [Additional QA Checks](#additional-qa-checks)
- [Manual Evidence That Does Not Count as QRT](#manual-evidence-that-does-not-count-as-qrt)
- [Repository Documentation Status](#repository-documentation-status)
- [Earlier Gates Still Active](#earlier-gates-still-active)

## Critical Modules and Coverage

| Critical module | Why critical | Required line coverage | Latest verified line coverage | Evidence |
|---|---|---:|---:|---|
| `backend/app/main.py` | Core API routes and orchestration for authentication, vehicle management, event tracking, assistant answers, and statistics workflows. | 30% | 94% | Local coverage gate on 2026-07-04 and backend CI workflow |
| `backend/app/chat_parser.py` | Converts user chat messages into structured product events for a main user workflow. | 30% | 85% | Local coverage gate on 2026-07-04 and backend CI workflow |
| `backend/app/database.py` | Database session and engine configuration underpin all persistent backend behavior. | 30% | 100% | Local coverage gate on 2026-07-04 and backend CI workflow |

## Automated Test Status

| Test type | Scope | Command or CI check | Latest documented evidence | Evidence |
|---|---|---|---|---|
| Backend regression suite | Backend business logic, API behavior, and persistence flows in `backend/tests` | `docker compose run --rm backend pytest tests -q` | Maintained local and CI regression command for full backend verification | `backend/tests` and `.github/workflows/backend-ci.yml` |
| Backend targeted follow-up suite | Follow-up regression around events, statistics, and deterministic assistant answers | `docker compose run --rm backend pytest tests/test_events.py tests/test_stats.py tests/test_chat_ask.py -q` | Maintained targeted command for Assignment 6 follow-up verification | `backend/tests/test_events.py`; `backend/tests/test_stats.py`; `backend/tests/test_chat_ask.py` |
| Backend unit tests | Isolated parser, AI adapter, statistics helper, and model behavior covered by backend pytest files | Included in `docker compose run --rm backend pytest tests -q` and the `Backend CI` pytest step | Active | `backend/tests/test_chat_parse.py`; `backend/tests/test_chat_parser.py`; `backend/tests/test_chat_parser_deepseek.py`; `backend/tests/test_deepseek_chat.py`; `backend/tests/test_stats.py` |
| Backend integration tests | FastAPI routes with test persistence through `TestClient` for authentication, vehicle, event, assistant, statistics, and QRT flows | Included in `docker compose run --rm backend pytest tests -q` and the `Backend CI` pytest step | Active | `backend/tests/test_auth.py`; `backend/tests/test_vehicle.py`; `backend/tests/test_events.py`; `backend/tests/test_chat_ask.py`; `backend/tests/test_quality_requirements.py` |
| Coverage gate | Full backend suite plus critical-module coverage threshold | `docker compose run --rm backend sh -lc "coverage run -m pytest tests && coverage report --include='app/*' --fail-under=30"` | Active | Local coverage output and `.github/workflows/backend-ci.yml` |
| Automated QRTs in CI | Backend quality checks automated in CI: pytest suite, coverage gate, linting, formatting, dependency health | GitHub Actions `Backend CI` workflow | Active in CI on pull requests and `main` | `.github/workflows/backend-ci.yml` |
| Android unit tests | Android repository, mapping, model, and session-restore decision behavior covered by JVM unit tests | `.\gradlew.bat testDebugUnitTest` locally; GitHub Actions `Android CI` workflow | Active | `.github/workflows/android-ci.yml`; `app/src/test/java/com/lamba/app/network/ChatRepositoryTest.kt`; `app/src/test/java/com/lamba/app/network/HistoryRecordEventMapperTest.kt`; `app/src/test/java/com/lamba/app/network/StatsModelTest.kt`; `app/src/test/java/com/lamba/app/SessionRestoreNavigatorTest.kt` |
| Android assembly check | Build verification for debug APK packaging | `.\gradlew.bat assembleDebug` locally; GitHub Actions `Android CI` workflow | Active | `.github/workflows/android-ci.yml` |

## Quality Requirement Test Evidence

The canonical detailed QRT artifact remains `docs/quality-requirement-tests.md`.

| QRT evidence source | Purpose |
|---|---|
| `docs/quality-requirement-tests.md` | Defines the detailed mapping between quality requirements, automated QRTs, and test evidence |
| `docs/quality-requirements.md` | Defines the measurable quality requirements that QRTs verify |
| `backend/tests/test_quality_requirements.py` | Implements QRT-001 through QRT-003 directly |
| `backend/tests/test_chat_ask.py` | Provides automated evidence for QRT-004 |
| `backend/tests/test_stats.py` | Provides automated evidence for QRT-005 |
| `.github/workflows/backend-ci.yml` | Runs the backend pytest suite and coverage gate that enforce QRT-003 through QRT-006 |

## Assignment 6 / MVP v3 Follow-up Verification

This section records the changed-area verification for Assignment 6 / Part 6 follow-up work after Sprint 4 and Sprint 5 fixes. It supplements the earlier baseline gates instead of replacing them.

### Coverage Summary

| Follow-up area | Current evidence | Main files |
|---|---|---|
| `#237` History form -> backend events -> Statistics sync | Automated backend tests cover manual-form event persistence, legacy `condition` event handling, event update/delete behavior, and statistics refresh after create/update/delete. Android JVM tests cover history-form request mapping. | `backend/tests/test_events.py`; `backend/tests/test_stats.py`; `app/src/test/java/com/lamba/app/network/HistoryRecordEventMapperTest.kt` |
| `#241` Deterministic `/chat/ask` answers | Automated backend tests cover deterministic latest-expenses, statistics, and latest-events answers, plus period filtering and category filtering without LLM fallback. | `backend/tests/test_chat_ask.py` |
| `#249` Session restore after app restart | Android JVM test covers the extracted restore decision logic for saved user with vehicle, saved user without vehicle, placeholder vehicle, and backend failure routing. Full end-to-end restore remains a manual verification scenario because activity launch, toast display, and back-stack behavior depend on runtime Android navigation. | `app/src/main/java/com/lamba/app/SessionRestoreNavigator.kt`; `app/src/test/java/com/lamba/app/SessionRestoreNavigatorTest.kt` |

### What Is Verified for `#237`

- History-form compatible event payloads are accepted and persisted through `POST /events`.
- Legacy `condition` rows are ignored by `GET /events` instead of leaking into the visible timeline.
- Existing user events can be updated and deleted through `PUT /events/{id}` and `DELETE /events/{id}`.
- Statistics refresh is covered after create, update, and delete operations so backend event changes remain visible through `GET /stats`.

Relevant automated evidence:

- `backend/tests/test_events.py::test_get_events_ignores_legacy_invalid_event_types`
- `backend/tests/test_events.py::test_post_event_supports_manual_form_event_types_and_fields`
- `backend/tests/test_events.py::test_put_event_updates_existing_user_event`
- `backend/tests/test_events.py::test_delete_event_removes_existing_user_event`
- `backend/tests/test_stats.py::test_get_stats_is_updated_after_creating_event`
- `backend/tests/test_stats.py::test_get_stats_is_updated_after_updating_and_deleting_event`
- `app/src/test/java/com/lamba/app/network/HistoryRecordEventMapperTest.kt`

### What Is Verified for `#241`

- Expense questions are answered deterministically from saved events without LLM calls.
- Statistics questions are answered deterministically from saved backend totals without LLM calls.
- Latest-events questions are answered deterministically from saved timeline data without LLM calls.
- Weekly, monthly, all-time, last-N-days, and category-specific follow-up cases are covered in backend tests.

Relevant automated evidence:

- `backend/tests/test_chat_ask.py::test_chat_ask_returns_latest_five_expenses_without_llm`
- `backend/tests/test_chat_ask.py::test_chat_ask_returns_general_statistics_summary_without_llm`
- `backend/tests/test_chat_ask.py::test_chat_ask_returns_latest_events_as_numbered_list_without_llm`
- `backend/tests/test_chat_ask.py::test_chat_ask_filters_weekly_expenses`
- `backend/tests/test_chat_ask.py::test_chat_ask_filters_monthly_expenses`
- `backend/tests/test_chat_ask.py::test_chat_ask_filters_all_time_expenses`
- `backend/tests/test_chat_ask.py::test_chat_ask_filters_category_specific_expenses`
- `backend/tests/test_chat_ask.py::test_chat_ask_returns_events_for_last_n_days_as_numbered_list`

### What Is Verified for `#249`

- Saved user with a real vehicle is routed to `MainActivity`.
- Saved user without a vehicle is routed to `AddVehicleActivity`.
- Placeholder vehicle data is treated as an incomplete profile and is routed to `AddVehicleActivity`.
- Backend error response is routed through the visible error branch.

Relevant automated evidence:

- `app/src/test/java/com/lamba/app/SessionRestoreNavigatorTest.kt`

Remaining manual verification checklist:

1. Login or register successfully.
2. Close the app completely.
3. Reopen the app.
4. Confirm that a saved user with a vehicle opens `MainActivity`.
5. Confirm that a saved user without a vehicle opens `AddVehicleActivity`.
6. Confirm that no saved session opens `WelcomeActivity`.
7. Confirm that backend unavailability shows a user-visible error.
8. Confirm that the back stack is cleared after restore navigation.

### Automated Tests Covering Backend and Android Behavior

Backend and API behavior:

- `backend/tests/test_events.py`
- `backend/tests/test_stats.py`
- `backend/tests/test_chat_ask.py`
- `backend/tests/test_quality_requirements.py`

Android JVM unit behavior:

- `app/src/test/java/com/lamba/app/network/ChatRepositoryTest.kt`
- `app/src/test/java/com/lamba/app/network/HistoryRecordEventMapperTest.kt`
- `app/src/test/java/com/lamba/app/network/StatsModelTest.kt`
- `app/src/test/java/com/lamba/app/SessionRestoreNavigatorTest.kt`

### Manual-Only Verification That Still Remains

- End-to-end activity launch results from session restore.
- Back-stack clearing after restore navigation.
- Visible error presentation during failed restore.

These are documented as manual release evidence rather than QRTs.

### Commands for Final Evidence

Backend:

```bash
docker compose run --rm backend pytest tests -q
docker compose run --rm backend pytest tests/test_events.py tests/test_stats.py tests/test_chat_ask.py -q
```

Android:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

Diff and whitespace check:

```bash
git diff --check
```

## CI and QA Check Status

| Gate or check | Required for Done? | Current documented status | Evidence |
|---|---|---|---|
| Linting | Yes | Passing in Backend CI protected-branch evidence previously recorded in this repository | [Backend CI runs](https://github.com/LAMBA-23/LAMBA/actions/workflows/backend-ci.yml?query=branch%3Amain) |
| Formatting check | Yes | Passing in Backend CI protected-branch evidence previously recorded in this repository | [Backend CI runs](https://github.com/LAMBA-23/LAMBA/actions/workflows/backend-ci.yml?query=branch%3Amain) |
| Automated backend tests | Yes | Active in Backend CI and in the local commands listed above | [Backend CI runs](https://github.com/LAMBA-23/LAMBA/actions/workflows/backend-ci.yml?query=branch%3Amain) |
| Automated Android unit tests | Yes | Active in Android CI and in the local command listed above | `.github/workflows/android-ci.yml` |
| Coverage reporting | Yes | Active through the backend coverage command and Backend CI workflow | `.github/workflows/backend-ci.yml` |
| Additional QA check | Yes | Active as `python -m pip check` in Backend CI | `.github/workflows/backend-ci.yml` |
| Link checking | Yes | Active through the separate Link Check workflow | [Link Check runs](https://github.com/LAMBA-23/LAMBA/actions/workflows/lychee.yml?query=branch%3Amain) |

## CI and Protected Branch Evidence

| Evidence | Link or reference | Current status note |
|---|---|---|
| Backend CI workflow | `.github/workflows/backend-ci.yml`; [Backend CI runs](https://github.com/LAMBA-23/LAMBA/actions/workflows/backend-ci.yml?query=branch%3Amain) | Use the latest protected-default-branch result together with the local Assignment 6 verification commands in this document |
| Android CI workflow | `.github/workflows/android-ci.yml` | Use the latest protected-default-branch or PR result together with the local Android verification commands in this document |
| Link Check workflow | `.github/workflows/lychee.yml`; [Link Check runs](https://github.com/LAMBA-23/LAMBA/actions/workflows/lychee.yml?query=branch%3Amain) | Use for the latest link-check status |
| All GitHub Actions workflows | [Repository Actions](https://github.com/LAMBA-23/LAMBA/actions) | Use for the latest PR and protected-default-branch status after this branch is reviewed and merged |

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

**Limitations or follow-up:** This check does not cover Android or Gradle dependency health. Mobile regression protection currently relies on Android unit tests and assembly checks.

## Manual Evidence That Does Not Count as QRT

| Evidence | Scope | Result | Follow-up |
|---|---|---|---|
| Smoke-check procedure in `README.md` | Manual verification of local backend startup, auth, vehicle, events, stats, and chat endpoints | Available for repeatable manual regression checks | Useful as manual support evidence only |
| History and statistics manual follow-up checklist | Manual verification of history-form changes and session-restore-visible behavior | Available as repeatable release evidence | Does not count as QRT because it is manual |

Manual smoke checklist for history-form and restore follow-up:

1. Create a chat event and confirm it still appears in History and changes Statistics.
2. Create a fuel record through `History -> Add history record` and confirm fuel expenses, fuel liters, and records count change after reopening Statistics.
3. Create a repair record through the same form and confirm repair expenses and records count change.
4. Create a trip record through the same form and confirm mileage and records count change.
5. Edit a form-created record and confirm History and Statistics both reflect the updated backend values.
6. Delete a form-created record and confirm it disappears from History and Statistics.
7. Save a form record, return to History, and confirm the timeline does not show a duplicate entry.
8. Login or register, close the app completely, and confirm session restore opens the correct screen on the next launch.
9. Confirm the restore flow shows a visible error when backend lookup fails.
10. Confirm restore navigation clears the back stack.

## Repository Documentation Status

| Required document | Status | Notes |
|---|---|---|
| `docs/quality-requirements.md` | Maintained | No QR changes were made as part of this Assignment 6 testing-evidence refresh |
| `docs/quality-requirement-tests.md` | Maintained | No QRT changes were made as part of this Assignment 6 testing-evidence refresh |
| `docs/testing.md` | Maintained | Current canonical testing status artifact for active baseline gates plus Assignment 6 follow-up evidence |
| `docs/definition-of-done.md` | Maintained | Current completion standard aligned with active QR/QRT evidence |

## Earlier Gates Still Active

| Earlier gate | Current status |
|---|---|
| Backend linting with Ruff | Remains active through `Backend CI` |
| Backend formatting check with Ruff | Remains active through `Backend CI` |
| Backend automated tests | Remain active through `Backend CI` and local regression commands |
| Backend coverage gate for critical modules | Remains active through `coverage report --include="app/*" --fail-under=30` |
| Automated QRTs | Remain active through QRT-001 to QRT-006 |
| Additional automated QA check beyond Lychee | Remains active as `python -m pip check` in `Backend CI` |
| Lychee link checking | Remains active through the separate `Link Check` workflow |
| Definition of Done evidence | Remains active through `docs/definition-of-done.md` and issue/branch review evidence |
