# Testing

This document is the maintained testing status artifact for the repository. It keeps the previously established backend, CI, coverage, and documentation gates active and records the current verification evidence for Assignment 6 / Part 6 follow-up work after MVP v3 Sprint 5 changes.

## Contents

- [Critical Modules and Coverage](#critical-modules-and-coverage)
- [Automated Test Status](#automated-test-status)
- [Quality Requirement Test Evidence](#quality-requirement-test-evidence)
- [Assignment 6 MVP v3 Follow-up Verification](#assignment-6-mvp-v3-follow-up-verification)
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
| `backend/app/main.py` | Core API routes and orchestration for authentication, vehicle management, event tracking, assistant answers, statistics, and owner-checked photo workflows. | 30% | 87% | Python 3.12 Docker coverage gate on 2026-07-15 and backend CI workflow |
| `backend/app/chat_parser.py` | Converts user chat messages into structured product events for a main user workflow. | 30% | 73% | Python 3.12 Docker coverage gate on 2026-07-15 and backend CI workflow |
| `backend/app/database.py` | Database session and engine configuration underpin all persistent backend behavior. | 30% | 100% | Python 3.12 Docker coverage gate on 2026-07-15 and backend CI workflow |
| `backend/app/photo_processing.py` | Decodes, validates, normalizes, strips metadata, and creates thumbnails for untrusted uploads. | 30% | 92% | Python 3.12 Docker coverage gate on 2026-07-15 and targeted photo tests |
| `backend/app/photo_storage.py` | Keeps local and S3-compatible object operations behind one tested adapter boundary. | 30% | 88% | Python 3.12 Docker coverage gate on 2026-07-15 and targeted storage tests |

## Automated Test Status

| Test type | Scope | Command or CI check | Latest documented evidence | Evidence |
|---|---|---|---|---|
| Vehicle-data Excel export | Owner-scoped XLSX response, Russian workbook structure, event translation, statistics, charts, empty history, and exclusion of other users' or technical data | `docker compose run --rm backend pytest tests/test_data_export.py -q` | Passed locally on 2026-07-16: 3 tests passed | `backend/tests/test_data_export.py` |
| Android Excel export writer | Stream-copy behavior used when saving the backend XLSX response to the user-selected Android document URI | `./gradlew :app:testDebugUnitTest --tests com.lamba.app.ProfileExportWriterTest` | Passed locally on 2026-07-16 | `app/src/test/java/com/lamba/app/ProfileExportWriterTest.kt` |
| Backend regression suite | Backend business logic, API behavior, and persistence flows in `backend/tests` | `python -m coverage run -m pytest tests` from `backend/`; Backend CI equivalent | Passed locally on 2026-07-19: 172 tests passed | `backend/tests` and `.github/workflows/backend-ci.yml` |
| Backend targeted Sprint 5 suite | Security, assistant, style, and regression around authentication, CORS, rate limiting, chat answers, and style switching | `python -m pytest tests/test_auth.py tests/test_cors.py tests/test_rate_limiting.py tests/test_chat_ask.py -q` from `backend/`; included in the full backend suite | Included in the 2026-07-19 full backend suite; `backend/tests/test_rate_limiting.py::test_chat_ask_allows_requests_after_rate_limit_window` now verifies chat rate-limit recovery without a real 60-second wait | `backend/tests/test_auth.py`; `backend/tests/test_cors.py`; `backend/tests/test_rate_limiting.py`; `backend/tests/test_chat_ask.py` |
| Backend unit tests | Isolated parser, AI adapter, statistics helper, and model behavior covered by backend pytest files | Included in `docker compose run --rm backend pytest tests -q` and the `Backend CI` pytest step | Active | `backend/tests/test_chat_parse.py`; `backend/tests/test_chat_parser.py`; `backend/tests/test_chat_parser_deepseek.py`; `backend/tests/test_deepseek_chat.py`; `backend/tests/test_stats.py` |
| Backend integration tests | FastAPI routes with test persistence through `TestClient` for authentication, vehicle, event, assistant, statistics, CORS, rate limiting, and QRT flows | Included in `docker compose run --rm backend pytest tests -q` and the `Backend CI` pytest step | Active | `backend/tests/test_auth.py`; `backend/tests/test_vehicle.py`; `backend/tests/test_events.py`; `backend/tests/test_chat_ask.py`; `backend/tests/test_cors.py`; `backend/tests/test_rate_limiting.py`; `backend/tests/test_quality_requirements.py` |
| Coverage gate | Full backend suite plus critical-module coverage threshold | `python -m coverage run -m pytest tests`; `python -m coverage report --include="app/*" --fail-under=30` from `backend/`; Backend CI equivalent | Passed locally on 2026-07-15 with total `app/*` coverage of 88%; photo processing 92% and photo storage 88% | Local coverage output and `.github/workflows/backend-ci.yml` |
| Automated QRTs in CI | Backend quality checks automated in CI: pytest suite, coverage gate, linting, formatting, dependency health | GitHub Actions `Backend CI` workflow | Active in CI on pull requests and `main`; QRT-007 and QRT-008 are included because Backend CI runs the full backend pytest suite | `.github/workflows/backend-ci.yml`; `docs/quality-requirement-tests.md` |
| Android unit tests | Android repository, mapping, model, session-restore, and local chat history behavior covered by JVM unit tests | `.\gradlew.bat :app:testDebugUnitTest --no-daemon` locally; GitHub Actions `Android CI` workflow | Passed locally on 2026-07-10 during #282 verification | `.github/workflows/android-ci.yml`; `app/src/test/java/com/lamba/app/network/ChatRepositoryTest.kt`; `app/src/test/java/com/lamba/app/network/HistoryRecordEventMapperTest.kt`; `app/src/test/java/com/lamba/app/network/StatsModelTest.kt`; `app/src/test/java/com/lamba/app/SessionRestoreNavigatorTest.kt`; `app/src/test/java/com/lamba/app/chat/LocalChatHistoryRepositoryTest.kt`; `app/src/test/java/com/lamba/app/chat/LocalChatTitleTest.kt` |
| Android assembly check | Build verification for debug APK packaging | `.\gradlew.bat :app:assembleDebug --no-daemon` locally; GitHub Actions `Android CI` workflow | Passed locally on 2026-07-10 during #282 verification after a sequential rerun; the first parallel local run conflicted on KAPT cache deletion | `.github/workflows/android-ci.yml` |
| Voice-input regression tests | Mistral key rotation, transcription endpoint validation/fallback, and Android recording-state transitions | `docker compose run --rm backend pytest tests/test_mistral_transcription.py tests/test_chat_transcribe.py -q`; `./gradlew :app:testDebugUnitTest --no-daemon` | Backend-focused suite passed locally on 2026-07-16: 9 tests passed. Android Gradle verification requires CI or a working local Gradle daemon. | `backend/tests/test_mistral_transcription.py`; `backend/tests/test_chat_transcribe.py`; `app/src/test/java/com/lamba/app/chat/VoiceRecordingStateTest.kt` |

## Quality Requirement Test Evidence

The canonical detailed QRT artifact remains `docs/quality-requirement-tests.md`.

| QRT evidence source | Purpose |
|---|---|
| `docs/quality-requirement-tests.md` | Defines the detailed mapping between quality requirements, automated QRTs, and test evidence |
| `docs/quality-requirements.md` | Defines the measurable quality requirements that QRTs verify |
| `backend/tests/test_quality_requirements.py` | Implements QRT-001 through QRT-003 directly |
| `backend/tests/test_chat_ask.py` | Provides automated evidence for QRT-004 |
| `backend/tests/test_stats.py` | Provides automated evidence for QRT-005 |
| `backend/tests/test_auth.py` | Provides automated evidence for QRT-007 and part of QRT-008 |
| `backend/tests/test_rate_limiting.py` | Provides automated evidence for QRT-008, including chat request recovery after the configured rate-limit window |
| `backend/tests/test_cors.py` | Provides automated regression evidence for restricted default CORS behavior; not currently a QRT |
| `.github/workflows/backend-ci.yml` | Runs the backend pytest suite and coverage gate that enforce QRT-003 through QRT-008 |

## Assignment 6 MVP v3 Follow-up Verification

This section records the changed-area verification for Assignment 6 / Part 6 follow-up work after Sprint 5 changes. It supplements the earlier baseline gates instead of replacing them.

### Coverage Summary

| Follow-up area | Current evidence | Main files |
|---|---|---|
| `#265` Backend auth, rate limiting, and CORS | Automated backend tests cover salted password hash persistence, correct and incorrect login behavior, removal of unsafe demo login, login rate limiting, chat rate limiting, chat rate-limit recovery after the configured window, and restricted default CORS behavior. | `backend/app/security.py`; `backend/app/rate_limit.py`; `backend/app/main.py`; `backend/tests/test_auth.py`; `backend/tests/test_rate_limiting.py`; `backend/tests/test_cors.py`; `backend/tests/test_chat_ask.py` |
| `#271` Android logout and local-session cleanup | Android implementation clears local account data and Room-backed chat history on logout. Automated evidence is indirect through local chat repository tests; full menu, dialog, and navigation behavior remains manual-only. | `app/src/main/java/com/lamba/app/MainActivity.kt`; `app/src/main/java/com/lamba/app/SessionManager.kt`; `app/src/main/java/com/lamba/app/chat/LocalChatHistoryRepository.kt`; `app/src/test/java/com/lamba/app/chat/LocalChatHistoryRepositoryTest.kt` |
| `#277` Room-backed local history for last five chats | Android JVM tests cover local chat creation, message persistence, retrieval, title handling, five-dialog trimming, and user chat clearing. | `app/src/main/java/com/lamba/app/chat/`; `app/src/test/java/com/lamba/app/chat/LocalChatHistoryRepositoryTest.kt`; `app/src/test/java/com/lamba/app/chat/LocalChatTitleTest.kt` |
| `#278` Adaptive Statistics UI | Android debug assembly verifies XML/resource build compatibility after Statistics UI changes. Visual behavior on varied Android devices remains manual-only evidence. | `app/src/main/res/layout/`; `app/src/main/java/com/lamba/app/StatisticsActivity.kt`; `.github/workflows/android-ci.yml` |
| `#329` Account profile and vehicle settings | Backend route tests cover vehicle updates, mileage locking after history, and password changes. Android JVM tests cover profile-form validation; screen navigation and confirmation dialogs remain manual checks. | `backend/tests/test_vehicle.py`; `backend/tests/test_auth.py`; `app/src/test/java/com/lamba/app/ProfileFormValidatorTest.kt`; `app/src/main/java/com/lamba/app/ProfileActivity.kt` |

### Unit Tests

Unit tests remain the evidence for isolated business or client-side logic. They do not count as QRT unless linked to a measurable QR in `docs/quality-requirement-tests.md`.

Current unit-test evidence includes:

- `backend/tests/test_chat_parser.py`
- `backend/tests/test_chat_parse.py`
- `backend/tests/test_deepseek_chat.py`
- `backend/tests/test_stats.py`
- `app/src/test/java/com/lamba/app/network/ChatRepositoryTest.kt`
- `app/src/test/java/com/lamba/app/network/HistoryRecordEventMapperTest.kt`
- `app/src/test/java/com/lamba/app/network/StatsModelTest.kt`
- `app/src/test/java/com/lamba/app/SessionRestoreNavigatorTest.kt`
- `app/src/test/java/com/lamba/app/chat/LocalChatHistoryRepositoryTest.kt`
- `app/src/test/java/com/lamba/app/chat/LocalChatTitleTest.kt`

### Integration Tests

Integration tests cover FastAPI route behavior with test persistence, external-AI test doubles, or runtime middleware behavior:

- `backend/tests/test_auth.py` verifies registration, login, password hashing behavior, removed demo credentials, wrong-password behavior, and login rate limiting.
- `backend/tests/test_cors.py` verifies health access without browser origin, denied untrusted browser origin by default, and rejected preflight by default.
- `backend/tests/test_rate_limiting.py` verifies chat endpoint rate limiting and recovery after the configured rate-limit window using controlled monotonic time.
- `backend/tests/test_chat_ask.py` verifies deterministic assistant answers and chat behavior used by regression and QRT evidence.
- `backend/tests/test_events.py`, `backend/tests/test_vehicle.py`, and `backend/tests/test_stats.py` verify persistence-backed vehicle history and statistics behavior.

### Automated QRTs

Automated QRTs remain defined in `docs/quality-requirement-tests.md` and are included in Backend CI where applicable:

- [QRT-001](quality-requirement-tests.md#qrt-001-vehicle-event-data-integrity) through [QRT-003](quality-requirement-tests.md#qrt-003-backend-regression-testability) are covered by `backend/tests/test_quality_requirements.py` and Backend CI.
- [QRT-004](quality-requirement-tests.md#qrt-004-assistant-context-correctness) is covered by `backend/tests/test_chat_ask.py`.
- [QRT-005](quality-requirement-tests.md#qrt-005-statistics-calculation-correctness) is covered by `backend/tests/test_stats.py`.
- [QRT-006](quality-requirement-tests.md#qrt-006-critical-module-coverage-gate) is covered by the Backend CI coverage command.
- [QRT-007](quality-requirement-tests.md#qrt-007-secure-password-storage) is covered by `backend/tests/test_auth.py`.
- [QRT-008](quality-requirement-tests.md#qrt-008-login-and-chat-request-rate-protection) is covered by `backend/tests/test_auth.py` and `backend/tests/test_rate_limiting.py`, including `test_chat_ask_allows_requests_after_rate_limit_window`.

### CI Checks

CI checks are separated by workflow:

- `Backend CI` runs Ruff linting, Ruff format check, the full backend pytest suite with coverage, `coverage report --include="app/*" --fail-under=30`, coverage XML generation, and `python -m pip check`.
- `Android CI` runs `./gradlew :app:testDebugUnitTest --no-daemon` and `./gradlew :app:assembleDebug --no-daemon`.
- `Link Check` runs Lychee for Markdown links.

### Automated Regression Evidence

The following automated evidence is useful regression protection but is not counted as QRT unless the QRT mapping explicitly says so:

- `backend/tests/test_cors.py`: verifies CORS is restrictive by default. This is not a standalone QRT because the current tests do not verify an allowed origin configured through `CORS_ALLOWED_ORIGINS`.
- `backend/tests/test_auth.py::TestLogin::test_removed_demo_credentials_fail`: verifies the unsafe demo login path no longer works.
- `app/src/test/java/com/lamba/app/chat/LocalChatHistoryRepositoryTest.kt`: verifies Room-backed local chat repository behavior through a fake store, including the five-dialog limit and user chat clearing.
- `app/src/test/java/com/lamba/app/chat/LocalChatTitleTest.kt`: verifies title truncation and fallback behavior for local chat titles.
- `.\gradlew.bat :app:assembleDebug --no-daemon`: verifies debug APK packaging after Android UI/resource changes, including adaptive Statistics UI resources.

### Manual-Only Verification That Still Remains

- Full Android logout menu interaction, confirmation dialog, navigation to `WelcomeActivity`, and back-stack clearing.
- Visual confirmation that Statistics cards do not clip text across real device and emulator screen sizes.
- Visual confirmation that local chat history appears in the side menu, selected dialogs restore without duplicate messages, and logout clears local dialog history from the user-facing UI.

These checks are manual release evidence and do not count as QRTs.

### Commands for Final Evidence

Backend:

```powershell
cd backend
..\venv\Scripts\python.exe -m ruff check app tests
..\venv\Scripts\python.exe -m ruff format --check app tests
..\venv\Scripts\python.exe -m coverage run -m pytest tests
..\venv\Scripts\python.exe -m coverage report --include="app/*" --fail-under=30
..\venv\Scripts\python.exe -m coverage xml -o coverage.xml
..\venv\Scripts\python.exe -m pip check
```

Latest backend evidence on 2026-07-15, reproduced in the clean Python 3.12
Docker image used by the repository configuration:

- Ruff lint: `All checks passed!`
- Ruff format check: `29 files already formatted`
- Backend tests: `143 passed` in the clean Python 3.12 container
- Coverage report for `app/*`: 88% total coverage, including `main.py` 87%, `photo_processing.py` 92%, `photo_storage.py` 88%, `chat_parser.py` 73%, and `database.py` 100%
- Dependency health: `No broken requirements found.`

Excel export follow-up evidence on 2026-07-16:

- Backend image build completed successfully with `openpyxl==3.1.5`.
- Ruff lint and format checks passed, and `backend/tests/test_data_export.py`
  passed with 3 tests.
- The complete backend suite passed with 172 tests.
- Android JVM tests and debug APK assembly passed after adding the profile export
  stream writer and Storage Access Framework integration.

Evidence limitations:

- The pytest run emitted FastAPI and SQLAlchemy deprecation warnings.

Android:

```powershell
.\gradlew.bat :app:testDebugUnitTest --no-daemon
.\gradlew.bat :app:assembleDebug --no-daemon
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
| Android debug APK assembly | Yes | Active in Android CI and in the local command listed above | `.github/workflows/android-ci.yml` |
| Coverage reporting | Yes | Active through the backend coverage command and Backend CI workflow | `.github/workflows/backend-ci.yml` |
| Additional QA check | Yes | Active as `python -m pip check` in Backend CI | `.github/workflows/backend-ci.yml` |
| Link checking | Yes | Active through the separate Link Check workflow | [Link Check runs](https://github.com/LAMBA-23/LAMBA/actions/workflows/lychee.yml?query=branch%3Amain) |

## CI and Protected Branch Evidence

| Evidence | Link or reference | Current status note |
|---|---|---|
| Backend CI workflow | `.github/workflows/backend-ci.yml`; [Backend CI runs](https://github.com/LAMBA-23/LAMBA/actions/workflows/backend-ci.yml?query=branch%3Amain) | Use the latest protected-default-branch result together with the local Assignment 6 verification commands in this document |
| Android CI workflow | `.github/workflows/android-ci.yml` | Runs Android JVM unit tests and debug APK assembly; use the latest protected-default-branch or PR result together with the local Android verification commands in this document |
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
| Sprint 5 Android manual follow-up checklist | Manual verification of logout, local chat history UI, adaptive Statistics UI, profile, avatar, style selector, and vehicle brand/model selection behavior | Available as repeatable release evidence | Does not count as QRT because it is manual |

Manual smoke checklist for Sprint 5 Android follow-up:

1. Log in or register, open the main screen, and confirm the logout action is visible from the side menu.
2. Confirm logout clears the local session, returns to `WelcomeActivity`, and does not restore the previous session after app restart.
3. Create at least two non-empty chat dialogs and confirm they can be selected from the side menu without resending old messages.
4. Create six non-empty chat dialogs and confirm only the five most recent dialogs remain visible.
5. Log out and confirm previous local chat dialogs are no longer shown after logging in again.
6. Open Statistics on narrow and wide Android screens and confirm cards remain readable, same-height where intended, and not clipped.

## Repository Documentation Status

| Required document | Status | Notes |
|---|---|---|
| `docs/quality-requirements.md` | Maintained | QR-007 and QR-008 added for Sprint 4; Sprint 5 features documented |
| `docs/quality-requirement-tests.md` | Maintained | QRT-007 and QRT-008 added with automated backend evidence |
| `docs/testing.md` | Maintained | Current canonical testing status artifact for active baseline gates plus Assignment 6 follow-up evidence |
| `docs/definition-of-done.md` | Maintained | Current completion standard aligned with active QR/QRT evidence |

## Earlier Gates Still Active

| Earlier gate | Current status |
|---|---|
| Backend linting with Ruff | Remains active through `Backend CI` |
| Backend formatting check with Ruff | Remains active through `Backend CI` |
| Backend automated tests | Remain active through `Backend CI` and local regression commands |
| Backend coverage gate for critical modules | Remains active through `coverage report --include="app/*" --fail-under=30` |
| Automated QRTs | Remain active through QRT-001 to QRT-008 |
| Additional automated QA check beyond Lychee | Remains active as `python -m pip check` in `Backend CI` |
| Lychee link checking | Remains active through the separate `Link Check` workflow |
| Definition of Done evidence | Remains active through `docs/definition-of-done.md` and issue/branch review evidence |
