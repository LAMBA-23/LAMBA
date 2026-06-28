# Week 4 Report - LAMBA

## 1. Project name and short description

**LAMBA** is an Android application for creating a digital twin of a car. The product lets a vehicle owner register, add a vehicle profile, record vehicle events through chat, inspect the vehicle timeline, and view basic vehicle statistics.

## 2. Product Backlog board/view

- [Product Backlog board](https://github.com/orgs/LAMBA-23/projects/1)

## 3. Sprint Backlog board/table

- [Sprint Backlog board/table](https://github.com/orgs/LAMBA-23/projects/1/views/3)

## 4. Assignment 4 Sprint milestone

- [Sprint 2 - Chat Event Capture & Assistant & Statistics](https://github.com/LAMBA-23/LAMBA/milestone/2)

## 5. Sprint Goal, Sprint dates, and short scope summary

**Sprint dates:** 2026-06-22 to 2026-06-28

**Sprint Goal:** Enable the vehicle owner to inspect saved vehicle history through the vehicle timeline, ask the assistant questions about that history, and view basic statistics based on recorded vehicle events.

**Scope summary:** Sprint 2 focused on completing the vehicle timeline foundation, adding AI assistant question answering over vehicle history, implementing basic statistics, stabilizing event APIs, improving Android screens, adding quality evidence, and preparing the Assignment 4 Sprint increment release documentation.

## 6. Total Sprint size in Story Points

The total closed Sprint issue size is **48 Story Points**.

Closed selected user-story scope:

- [#48](https://github.com/LAMBA-23/LAMBA/issues/48) - 8 SP
- [#50](https://github.com/LAMBA-23/LAMBA/issues/50) - 3 SP

Closed supporting Sprint PBIs and fixes:

- [#113](https://github.com/LAMBA-23/LAMBA/issues/113) - 3 SP
- [#116](https://github.com/LAMBA-23/LAMBA/issues/116) - 3 SP
- [#118](https://github.com/LAMBA-23/LAMBA/issues/118) - 3 SP
- [#120](https://github.com/LAMBA-23/LAMBA/issues/120) - 3 SP
- [#123](https://github.com/LAMBA-23/LAMBA/issues/123) - 3 SP
- [#124](https://github.com/LAMBA-23/LAMBA/issues/124) - 5 SP
- [#138](https://github.com/LAMBA-23/LAMBA/issues/138) - 3 SP
- [#140](https://github.com/LAMBA-23/LAMBA/issues/140) - 5 SP
- [#149](https://github.com/LAMBA-23/LAMBA/issues/149) - 2 SP
- [#154](https://github.com/LAMBA-23/LAMBA/issues/154) - 5 SP
- [#158](https://github.com/LAMBA-23/LAMBA/issues/158) - 2 SP

Total: `8 + 3 + 3 + 3 + 3 + 3 + 3 + 5 + 3 + 5 + 2 + 5 + 2 = 48 SP`.

Open Sprint issue not counted:

- [#49](https://github.com/LAMBA-23/LAMBA/issues/49) - 5 SP

## 7. Summary of delivered product changes

During Week 4 / Assignment 4, the team delivered the Sprint 2 increment:

- added an Android history/timeline screen for saved vehicle events from `GET /events`;
- added an Android statistics screen connected to backend `GET /stats`;
- added `POST /chat/ask` for assistant answers based on vehicle and recent-event context;
- stabilized event and statistics backend behavior with automated tests;
- added user acceptance test documentation and Week 4 execution results;
- added automated quality requirement tests for event integrity, timeline response time, and backend regression testability;
- prepared release and deployment documentation for the Assignment 4 Sprint increment.

## 8. Link to deployed product, hosted artifact, package, or runnable product

Current runnable/access artifacts for the Assignment 4 Sprint increment:

- [Current GitHub Release package: v1.1.0 - Assignment 4 Sprint 2 Increment](https://github.com/LAMBA-23/LAMBA/releases/tag/v1.1.0)
- [Android APK asset](https://github.com/LAMBA-23/LAMBA/releases/download/v1.1.0/app-debug.apk)
- [Deployed backend API and Swagger docs](http://186.246.27.211:8000/docs)
- Backend health endpoint: `/health` on the same host
- [Runnable backend source: docker-compose.yml](../../docker-compose.yml)

## 9. Link to current access or run instructions

- [Root README - Local Setup](../../README.md#local-setup)
- [Root README - Assignment 4 Sprint Increment Release](../../README.md#assignment-4-sprint-increment-release)

## 10. Customer feedback response table

| Customer feedback point | Resulting PBI or issue | Status | Response |
| --- | --- | --- | --- |
| Replace mock or hardcoded registration/authentication with real backend registration and login. | [#44](https://github.com/LAMBA-23/LAMBA/issues/44), [#68](https://github.com/LAMBA-23/LAMBA/issues/68), [#71](https://github.com/LAMBA-23/LAMBA/issues/71), [#72](https://github.com/LAMBA-23/LAMBA/issues/72) | Addressed | Backend registration/login and Android onboarding were connected to real backend flows. |
| Keep the user logged in where feasible. | [#71](https://github.com/LAMBA-23/LAMBA/issues/71) | Addressed | Android session persistence was added through `SessionManager`. |
| Store vehicle brand, model, production year, and current mileage. | [#45](https://github.com/LAMBA-23/LAMBA/issues/45), [#67](https://github.com/LAMBA-23/LAMBA/issues/67), [#81](https://github.com/LAMBA-23/LAMBA/issues/81) | Addressed | Vehicle profile fields and validation were implemented. |
| Include basic chat as the foundation for later AI-agent behavior. | [#46](https://github.com/LAMBA-23/LAMBA/issues/46), [#69](https://github.com/LAMBA-23/LAMBA/issues/69), [#70](https://github.com/LAMBA-23/LAMBA/issues/70), [#79](https://github.com/LAMBA-23/LAMBA/issues/79), [#80](https://github.com/LAMBA-23/LAMBA/issues/80) | Addressed | Android chat was connected to backend parsing and assistant responses. |
| Allow the user to inspect saved vehicle history. | [#48](https://github.com/LAMBA-23/LAMBA/issues/48), [#113](https://github.com/LAMBA-23/LAMBA/issues/113), [#124](https://github.com/LAMBA-23/LAMBA/issues/124) | Addressed | A timeline/history flow was added for saved vehicle events. |
| Add basic statistics based on vehicle history. | [#50](https://github.com/LAMBA-23/LAMBA/issues/50), [#120](https://github.com/LAMBA-23/LAMBA/issues/120), [#123](https://github.com/LAMBA-23/LAMBA/issues/123), [#138](https://github.com/LAMBA-23/LAMBA/issues/138) | Addressed | Backend statistics and Android statistics UI were implemented. |
| AI assistant should answer questions about vehicle history. | [#49](https://github.com/LAMBA-23/LAMBA/issues/49), [#140](https://github.com/LAMBA-23/LAMBA/issues/140), [#154](https://github.com/LAMBA-23/LAMBA/issues/154) | Partially addressed | `POST /chat/ask` was added and connected to vehicle context. Final product validation and release evidence remain to be completed. |
| Passwords should not remain plain raw text in the final implementation. | Future security hardening PBI | Not addressed in this Sprint | Accepted as a product/security risk, but deferred because this Sprint focused on timeline, assistant, statistics, quality evidence, and release preparation. |
| Add vehicle photo upload later. | Future media PBI | Deferred | Kept outside Sprint scope because it was not required for the current MVP increment. |
| Add achievements later. | Future engagement PBI | Deferred | Kept outside Sprint scope because it was lower priority than the core vehicle-history workflow. |

## 11. Explanation of feedback not addressed

- **Password hashing and credential hardening:** added to backlog for later. A dedicated security PBI should be created because this is a customer-raised final-implementation expectation and a product risk.
- **Token-based authorization:** partially addressed by `user_id` based API access and Android session persistence, but not equivalent to secure authorization. This should be refined together with password hashing.
- **Pre-filled form redirect or created-item timeline link from AI chat:** deferred with rationale until the timeline and event interaction flow are stable enough to support it without expanding Sprint risk.
- **Vehicle photo upload:** added to backlog for later because it was explicitly outside MVP v1.
- **Achievements:** added to backlog for later because it was explicitly future scope and not needed for the current Sprint Goal.
- **Full AI memory, summaries, and long-term dialog context:** deferred with rationale to later AI-assistant scope after baseline chat/event parsing and statistics are stable.

## 12. Link to docs/roadmap.md

- [docs/roadmap.md](../../docs/roadmap.md)

## 13. Link to docs/definition-of-done.md

- [docs/definition-of-done.md](../../docs/definition-of-done.md)

## 14. Link to docs/quality-requirements.md

- [docs/quality-requirements.md](../../docs/quality-requirements.md)

## 15. Link to docs/quality-requirement-tests.md

- [docs/quality-requirement-tests.md](../../docs/quality-requirement-tests.md)

## 16. Link to docs/testing.md

- [docs/testing.md](../../docs/testing.md)

## 17. Link to docs/user-acceptance-tests.md

- [docs/user-acceptance-tests.md](../../docs/user-acceptance-tests.md)

## 18. Summary of the quality model used and selected ISO/IEC 25010 sub-characteristics

The team used ISO/IEC 25010 as the quality model for Assignment 4 and selected sub-characteristics that match the current MVP risk profile:

| Quality requirement | ISO/IEC 25010 sub-characteristic | Reason |
| --- | --- | --- |
| QR-001 — Vehicle event data integrity | Integrity | Invalid event records must not corrupt the vehicle timeline, mileage, cost, or maintenance history. |
| QR-002 — Timeline API response time | Time behaviour | Timeline access is a core user workflow and should remain responsive under normal backend operation. |
| QR-003 — Backend regression testability | Testability | The backend is changing quickly, so regression tests must make future changes safer before merge. |

- [docs/quality-requirements.md](../../docs/quality-requirements.md)
- [docs/quality-requirement-tests.md](../../docs/quality-requirement-tests.md)

## 19. Testing status summary

Current documented backend verification status:

- backend tests: `47 passed`
- backend total line coverage: `89%`
- minimum required line coverage for critical modules: `30%`
- additional QA check: `python -m pip check`, result `No broken requirements found`

| Critical module | Role | Required line coverage | Current line coverage | Status |
| --- | --- | ---: | ---: | --- |
| `backend/app/main.py` | Core API routes and orchestration for auth, vehicles, events, chat, and statistics | 30% | 95% | Pass |
| `backend/app/chat_parser.py` | Converts vehicle chat messages into structured events | 30% | 59% | Pass |
| `backend/app/database.py` | Database session and engine configuration | 30% | 100% | Pass |

- [docs/testing.md](../../docs/testing.md)

## 20. Links to unit tests

Backend unit and focused behavior tests:

- [backend/tests/test_auth.py](../../backend/tests/test_auth.py)
- [backend/tests/test_chat_parser.py](../../backend/tests/test_chat_parser.py)
- [backend/tests/test_deepseek_chat.py](../../backend/tests/test_deepseek_chat.py)
- [backend/tests/test_quality_requirements.py](../../backend/tests/test_quality_requirements.py)

Android unit test:

- [app/src/test/java/com/lamba/app/network/ChatRepositoryTest.kt](../../app/src/test/java/com/lamba/app/network/ChatRepositoryTest.kt)

## 21. Links to integration tests

Integration-style backend API tests use FastAPI `TestClient` with SQLite-backed persistence:

- [backend/tests/test_chat_ask.py](../../backend/tests/test_chat_ask.py)
- [backend/tests/test_chat_parse.py](../../backend/tests/test_chat_parse.py)
- [backend/tests/test_events.py](../../backend/tests/test_events.py)
- [backend/tests/test_stats.py](../../backend/tests/test_stats.py)
- [backend/tests/test_vehicle.py](../../backend/tests/test_vehicle.py)
- [backend/tests/conftest.py](../../backend/tests/conftest.py)

## 22. Links to automated quality requirement tests

Automated QRTs are implemented in:

- [backend/tests/test_quality_requirements.py](../../backend/tests/test_quality_requirements.py)

Traceability:

| QRT | Requirement | Automated evidence |
| --- | --- | --- |
| QRT-001 | Vehicle event data integrity | Invalid event type, empty description, negative amount, negative mileage, and unknown user are rejected and not persisted. |
| QRT-002 | Timeline API response time | `GET /events` responds within 2 seconds under the documented test dataset. |
| QRT-003 | Backend regression testability | The full backend pytest suite is enforced by CI before merge. |

- [docs/quality-requirement-tests.md](../../docs/quality-requirement-tests.md)

## 23. Link to the CI pipeline

- [Backend CI workflow](../../.github/workflows/backend-ci.yml)
- [Link Check workflow](../../.github/workflows/lychee.yml)
- [GitHub Actions — all workflows](https://github.com/LAMBA-23/LAMBA/actions)

## 24. Link to the latest protected-default-branch CI run

- Backend CI run: https://github.com/LAMBA-23/LAMBA/actions (Backend CI workflow)
- Link Check run: https://github.com/LAMBA-23/LAMBA/actions (Lychee workflow)

## 25. Branch protection or rules evidence for the protected default branch

The protected default branch is `main`.

Branch protection evidence:

| Rule | Status |
| --- | --- |
| Required pull request reviews | Enabled |
| Required approving review count | 1 |
| Enforce admins | Enabled |
| Force pushes | Disabled |
| Branch deletions | Disabled |

Evidence links:

- [Protected branch: main](https://github.com/LAMBA-23/LAMBA/tree/main)
- [Repository branches](https://github.com/LAMBA-23/LAMBA/branches)
- Branch protection screenshot: [reports/week2/images/branch-protection-rule.png](../week2/images/branch-protection-rule.png)

## 26. Screenshots or report links for linting, coverage, tests, and the additional QA check

| Evidence type | Report or screenshot link | Status |
| --- | --- | --- |
| Linting | [Backend CI workflow](../../.github/workflows/backend-ci.yml) | Passing |
| Formatting | [Backend CI workflow](../../.github/workflows/backend-ci.yml) | Passing |
| Automated tests | [Backend CI workflow](../../.github/workflows/backend-ci.yml), [docs/testing.md](../../docs/testing.md) | Passing |
| Coverage | [Backend CI workflow](../../.github/workflows/backend-ci.yml), [docs/testing.md](../../docs/testing.md) | 89% total backend coverage |
| Additional QA check | `python -m pip check` in [Backend CI](../../.github/workflows/backend-ci.yml) | Passing |
| Markdown link check | [Link Check workflow](../../.github/workflows/lychee.yml) | Passing |

Screenshot files should be placed in [`reports/week4/images/`](images/).

## 27. How Assignment 4 quality controls govern later work

Assignment 4 turns quality evidence into a continuing project gate. Future PBIs may be marked `Done` only when their acceptance criteria are verified, relevant tests pass, applicable automated quality requirement tests pass, CI is green, and evidence remains traceable through issues, PRs, CI runs, coverage reports, or maintained documentation.

The current Definition of Done requires:

- passing CI checks before merge;
- relevant automated unit and integration tests;
- applicable automated quality requirement tests;
- coverage expectations for critical modules from [docs/testing.md](../../docs/testing.md);
- preserved testing and review evidence;
- documentation and changelog updates when needed;
- no committed secrets or prohibited private artifacts.

This means later work on AI answers, manual history entry, authorization, password hashing, Android coverage, and release packaging must continue to satisfy the same CI, QRT, testing, and review standards.

- [docs/definition-of-done.md](../../docs/definition-of-done.md)
- [docs/testing.md](../../docs/testing.md)
- [docs/quality-requirement-tests.md](../../docs/quality-requirement-tests.md)

## 28. Link to the SemVer release mapped to the Assignment 4 Sprint increment

- [v1.1.0 - Assignment 4 Sprint 2 Increment](https://github.com/LAMBA-23/LAMBA/releases/tag/v1.1.0)

This SemVer release is the current public release artifact available for the Assignment 4 Sprint increment and is mapped to Sprint 2: Chat Event Capture, Assistant, and Statistics.

## Assignment 4 Submission Evidence

This section collects the public repository evidence for the final Week 4 assignment report.

## Changelog

- [CHANGELOG.md](../../CHANGELOG.md)

## Public Sanitized Demo Video

- [Public sanitized demo video](https://drive.google.com/drive/folders/1r27kEbYegLTjaM85RWaCVcfKkgIq-NO9)

## Presentation Materials

- [presentation.pdf](presentation.pdf)

## Public UAT Results Summary

- [docs/user-acceptance-tests.md](../../docs/user-acceptance-tests.md)

## Customer Review Evidence

- [Customer review transcript](customer-review-transcript.md)
- [Customer review summary](customer-review-summary.md)

The customer review transcript is published as a sanitized repository artifact. The private recording is shared only through the approved instructor submission channel.

## Reflection, Retrospective, and LLM Usage

- [Week 4 reflection](reflection.md)
- [Week 4 retrospective](retrospective.md)
- [Week 4 LLM usage report](llm-report.md)

## Current Product Status

The current product increment supports the main MVP flows: registration and login, vehicle setup, vehicle timeline/history, manual event creation, basic statistics, and AI assistant support. The backend API contract, event handling, quality requirements, automated checks, UAT scenarios, customer review materials, retrospective, reflection, and LLM usage report are documented in the repository.

The customer review confirmed that the product is a useful MVP step. The remaining follow-up areas are stronger authentication and authorization, password hashing, better AI answer relevance, clearer manual history entry, and continued frontend quality coverage.

## Next Steps

- Keep the public sanitized demo video link available for review.
- Keep the sanitized presentation artifact available in the repository.
- Use the UAT findings to prioritize AI answer quality and manual history-entry improvements.
- Continue strengthening automated checks, coverage evidence, and default-branch CI stability.
- Keep customer-facing evidence sanitized while sharing private recordings only through the approved instructor channel.

## Contribution Traceability

| Team member | Issues / PRs | Review activity | Testing, quality, automation, or documentation work |
|---|---|---|---|
| [Erusiaaa](https://github.com/Erusiaaa) | [PR #115](https://github.com/LAMBA-23/LAMBA/pull/115), [PR #117](https://github.com/LAMBA-23/LAMBA/pull/117), [PR #147](https://github.com/LAMBA-23/LAMBA/pull/147), [PR #150](https://github.com/LAMBA-23/LAMBA/pull/150), [PR #166](https://github.com/LAMBA-23/LAMBA/pull/166) | Approved [PR #137](https://github.com/LAMBA-23/LAMBA/pull/137) and [PR #139](https://github.com/LAMBA-23/LAMBA/pull/139), covering timeline UI, event cards, loading/empty/error states, statistics integration, `/stats` flow reuse, and UI consistency | Events API contract, events testing evidence, manual add flow, Week 4 reflection |
| [vasilisatumakina29](https://github.com/vasilisatumakina29) | [PR #121](https://github.com/LAMBA-23/LAMBA/pull/121), [PR #125](https://github.com/LAMBA-23/LAMBA/pull/125), [PR #127](https://github.com/LAMBA-23/LAMBA/pull/127), [PR #129](https://github.com/LAMBA-23/LAMBA/pull/129), [PR #132](https://github.com/LAMBA-23/LAMBA/pull/132), [PR #136](https://github.com/LAMBA-23/LAMBA/pull/136), [PR #145](https://github.com/LAMBA-23/LAMBA/pull/145), [PR #155](https://github.com/LAMBA-23/LAMBA/pull/155), [PR #159](https://github.com/LAMBA-23/LAMBA/pull/159), [PR #163](https://github.com/LAMBA-23/LAMBA/pull/163) | Requested changes and later approved [PR #122](https://github.com/LAMBA-23/LAMBA/pull/122) and [PR #141](https://github.com/LAMBA-23/LAMBA/pull/141); approved [PR #153](https://github.com/LAMBA-23/LAMBA/pull/153) and [PR #157](https://github.com/LAMBA-23/LAMBA/pull/157) | Statistics backend, planning docs, quality requirements, Definition of Done, CI setup, fixes, retrospective |
| [vanya630](https://github.com/vanya630) | [PR #141](https://github.com/LAMBA-23/LAMBA/pull/141), [PR #143](https://github.com/LAMBA-23/LAMBA/pull/143), [PR #167](https://github.com/LAMBA-23/LAMBA/pull/167) | Approved [PR #166](https://github.com/LAMBA-23/LAMBA/pull/166), confirming the Week 4 reflection document was ready to merge and that the submitted reflection evidence was acceptable for the assignment report | AI assistant backend, automated quality requirement tests, LLM usage report |
| [mariachizhikova08](https://github.com/mariachizhikova08) | [PR #137](https://github.com/LAMBA-23/LAMBA/pull/137), [PR #139](https://github.com/LAMBA-23/LAMBA/pull/139), [PR #151](https://github.com/LAMBA-23/LAMBA/pull/151) | Approved [PR #115](https://github.com/LAMBA-23/LAMBA/pull/115), [PR #117](https://github.com/LAMBA-23/LAMBA/pull/117), [PR #147](https://github.com/LAMBA-23/LAMBA/pull/147), [PR #150](https://github.com/LAMBA-23/LAMBA/pull/150), [PR #159](https://github.com/LAMBA-23/LAMBA/pull/159), and [PR #163](https://github.com/LAMBA-23/LAMBA/pull/163), covering API contract/testing evidence, manual add flow, frontend network update, and retrospective documentation | Timeline UI, statistics UI, UAT scenarios |
| [Elis-bett](https://github.com/Elis-bett) | [PR #122](https://github.com/LAMBA-23/LAMBA/pull/122), [PR #153](https://github.com/LAMBA-23/LAMBA/pull/153), [PR #157](https://github.com/LAMBA-23/LAMBA/pull/157) | Commented on and approved [PR #121](https://github.com/LAMBA-23/LAMBA/pull/121); approved [PR #125](https://github.com/LAMBA-23/LAMBA/pull/125), [PR #127](https://github.com/LAMBA-23/LAMBA/pull/127), [PR #129](https://github.com/LAMBA-23/LAMBA/pull/129), [PR #132](https://github.com/LAMBA-23/LAMBA/pull/132), [PR #136](https://github.com/LAMBA-23/LAMBA/pull/136), [PR #145](https://github.com/LAMBA-23/LAMBA/pull/145), [PR #151](https://github.com/LAMBA-23/LAMBA/pull/151), and [PR #167](https://github.com/LAMBA-23/LAMBA/pull/167); requested changes and later approved [PR #143](https://github.com/LAMBA-23/LAMBA/pull/143) and [PR #155](https://github.com/LAMBA-23/LAMBA/pull/155) | Chat redesign, customer feedback response, customer review transcript and summary |

## CI, Quality, and Release Evidence Links

- Backend CI workflow: [`.github/workflows/backend-ci.yml`](../../.github/workflows/backend-ci.yml)
- Link-check workflow: [`.github/workflows/lychee.yml`](../../.github/workflows/lychee.yml)
- Testing status artifact: [docs/testing.md](../../docs/testing.md)
- Definition of Done: [docs/definition-of-done.md](../../docs/definition-of-done.md)
- Latest protected default-branch Backend CI run: [GitHub Actions run 28322655676](https://github.com/LAMBA-23/LAMBA/actions/runs/28322655676)
- Latest protected default-branch Link Check run: [GitHub Actions run 28322655667](https://github.com/LAMBA-23/LAMBA/actions/runs/28322655667)
- Current SemVer release: [v1.1.0 - Assignment 4 Sprint 2 Increment](https://github.com/LAMBA-23/LAMBA/releases/tag/v1.1.0)
- Example reviewed issue-linked PR/MR: [PR #151 - Create User Acceptance Test scenarios](https://github.com/LAMBA-23/LAMBA/pull/151)

## Screenshot Evidence

Screenshots are stored under `reports/week4/images/`.

| Required screenshot | Evidence |
|---|---|
| Sprint milestone | ![Sprint milestone](images/sprint-milestone.png) |
| Latest protected-default-branch CI run | ![Latest main CI run](images/latest-main-ci-run.png) |
| Branch protection or rules evidence | ![Branch protection rules list](images/branch-protection-rules-list.png)<br>![Required pull request review](images/branch-protection-review-required.png)<br>![Additional branch protection settings](images/branch-protection-rules-extra.png) |
| Coverage or test report | ![Coverage test report](images/coverage-test-report.png) |
| Additional QA check result | ![Additional QA check result](images/additional-qa-check.png) |
| SemVer release | ![SemVer release](images/semver-release.png) |
| Example reviewed issue-linked PR/MR | ![Reviewed issue-linked PR](images/reviewed-issue-linked-pr.png) |
| Product Backlog screenshot, if public links may not be inspectable by graders | ![Product Backlog](images/product-backlog.png) |
| Sprint Backlog screenshot, if public links may not be inspectable by graders | ![Sprint Backlog](images/sprint-backlog.png) |
| Deployed product or runnable artifact screenshot, if public links may not be inspectable by graders | ![Runnable artifact](images/runnable-artifact.png) |

## Current Verification Evidence

The backend CI workflow was verified with the same command categories as the GitHub Actions job:

- `python -m ruff check backend/app backend/tests`
- `python -m ruff format --check backend/app backend/tests`
- `python -m coverage run -m pytest backend/tests`
- `python -m coverage report --include='backend/app/*'`
- `python -m pip check`

Current documented results:

- backend tests: `47 passed`
- backend total coverage: `89%`
- critical module coverage:
  - `backend/app/main.py`: `95%`
  - `backend/app/chat_parser.py`: `59%`
  - `backend/app/database.py`: `100%`
- dependency health check: `No broken requirements found`
