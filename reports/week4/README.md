# Week 4 Report

## Customer Feedback Response

| Feedback point | Resulting PBI or issue | Status | Response |
|---|---|---|---|
| Replace the mock or hardcoded registration/authentication flow with real backend registration and login. | [#44](https://github.com/LAMBA-23/LAMBA/issues/44), [#68](https://github.com/LAMBA-23/LAMBA/issues/68), [#71](https://github.com/LAMBA-23/LAMBA/issues/71), [#72](https://github.com/LAMBA-23/LAMBA/issues/72) | Addressed in Sprint | Added backend registration and login endpoints, connected Android onboarding to the backend, and removed the hardcoded-only user flow for the MVP v1 foundation. |
| Keep the user logged in instead of requiring login every time where feasible. | [#71](https://github.com/LAMBA-23/LAMBA/issues/71) | Addressed in Sprint | Added Android session persistence through `SessionManager`, storing the current user ID/name locally so the app can continue user-specific flows after login. |
| Passwords should not remain plain raw text in the final implementation; hashing is expected. | Backlog decision: create a security hardening PBI for password hashing and credential handling. | Added to backlog for later | Accepted as product/security work, but not selected for this Sprint because Assignment 4 prioritized MVP stabilization, quality evidence, and Sprint increment release. This remains a documented product risk before production-like use. |
| Backend responses must return data for the authorized or selected user instead of shared demo data. | [#44](https://github.com/LAMBA-23/LAMBA/issues/44), [#67](https://github.com/LAMBA-23/LAMBA/issues/67), [#72](https://github.com/LAMBA-23/LAMBA/issues/72), [#73](https://github.com/LAMBA-23/LAMBA/issues/73), [#81](https://github.com/LAMBA-23/LAMBA/issues/81); follow-up authorization PBI needed. | Partially addressed | Vehicle, event, and statistics endpoints now use `user_id` to retrieve user-specific data. Full token-based authorization is still open and should be refined together with password/security work. |
| Include vehicle registration after user registration. | [#45](https://github.com/LAMBA-23/LAMBA/issues/45), [#67](https://github.com/LAMBA-23/LAMBA/issues/67), [#71](https://github.com/LAMBA-23/LAMBA/issues/71), [#73](https://github.com/LAMBA-23/LAMBA/issues/73) | Addressed in Sprint | Added Android vehicle setup and backend vehicle creation/update support after registration/onboarding. |
| Store vehicle brand, model, production year, and current mileage. | [#45](https://github.com/LAMBA-23/LAMBA/issues/45), [#67](https://github.com/LAMBA-23/LAMBA/issues/67), [#81](https://github.com/LAMBA-23/LAMBA/issues/81) | Addressed in Sprint | Implemented vehicle profile fields and validation for brand, model, production year, and mileage. |
| Manual text input for brand and model is acceptable; a dropdown database is not required for MVP v1. | [#45](https://github.com/LAMBA-23/LAMBA/issues/45), [#73](https://github.com/LAMBA-23/LAMBA/issues/73); decision recorded as MVP scope clarification. | Addressed in Sprint | Kept vehicle brand and model as text fields to avoid unnecessary scope expansion and database complexity. |
| Include basic chat in MVP v1 as a foundation for later AI-agent behavior. | [#46](https://github.com/LAMBA-23/LAMBA/issues/46), [#69](https://github.com/LAMBA-23/LAMBA/issues/69), [#70](https://github.com/LAMBA-23/LAMBA/issues/70), [#79](https://github.com/LAMBA-23/LAMBA/issues/79), [#80](https://github.com/LAMBA-23/LAMBA/issues/80) | Addressed in Sprint | Connected Android chat sending to backend parsing and displayed parsed results or clarification questions. |
| AI-agent features should eventually parse fuel, repairs, routes, statistics, summaries, and clarifying questions. | [#47](https://github.com/LAMBA-23/LAMBA/issues/47), [#49](https://github.com/LAMBA-23/LAMBA/issues/49), [#50](https://github.com/LAMBA-23/LAMBA/issues/50), [#69](https://github.com/LAMBA-23/LAMBA/issues/69), [#75](https://github.com/LAMBA-23/LAMBA/issues/75), [#83](https://github.com/LAMBA-23/LAMBA/issues/83) | Partially addressed | Basic event parsing and clarification handling were implemented. Full AI assistant answers, summaries, and statistics remain planned through [#49](https://github.com/LAMBA-23/LAMBA/issues/49) and [#50](https://github.com/LAMBA-23/LAMBA/issues/50). |
| If the AI cannot confidently interpret a message, it should ask clarification questions and feel like a continuous dialog. | [#69](https://github.com/LAMBA-23/LAMBA/issues/69), [#75](https://github.com/LAMBA-23/LAMBA/issues/75), [#83](https://github.com/LAMBA-23/LAMBA/issues/83) | Addressed in Sprint | Added parser guardrails and clarification responses for ambiguous or unsupported vehicle messages. Continuous long-term AI memory remains outside the current Sprint scope. |
| Ideally, the AI should redirect the user to a pre-filled trip/repair/fuel form or link the created item in the timeline. | [#47](https://github.com/LAMBA-23/LAMBA/issues/47), [#48](https://github.com/LAMBA-23/LAMBA/issues/48); backlog decision: refine chat-to-timeline interaction later. | Deferred with rationale | The team implemented baseline event parsing and persistence first. Redirecting to pre-filled forms or linking created timeline items was deferred because it depends on a stable timeline/event interaction flow. |
| Add vehicle photo upload later. | Backlog decision: create a dedicated vehicle photo upload PBI if the customer confirms priority. | Added to backlog for later | Accepted as possible future scope, but not selected for this Sprint because the customer explicitly said it was not MVP v1 scope. |
| Add achievements later. | Backlog decision: create an achievements PBI if the customer confirms priority. | Added to backlog for later | Accepted as possible future scope, but not selected for this Sprint because it is lower priority than registration, vehicle records, chat, quality automation, and release evidence. |
| The customer needs a way to inspect the product despite university-network deployment limits. | Release/deployment work for the Sprint increment; Week 3 fallback was a video demonstration. | Partially addressed | The team used video demonstration as a temporary workaround. Assignment 4 release work should keep deployment or runnable access available until grading and customer review are complete. |

## Feedback Not Addressed or Only Partially Addressed

- **Password hashing and credential hardening:** added to backlog for later. A dedicated security PBI should be created because this is a customer-raised final-implementation expectation and a product risk.
- **Token-based authorization:** partially addressed by `user_id` based API access and Android session persistence, but not equivalent to secure authorization. This should be refined together with password hashing.
- **Pre-filled form redirect or created-item timeline link from AI chat:** deferred with rationale until the timeline and event interaction flow are stable enough to support it without expanding Sprint risk.
- **Vehicle photo upload:** added to backlog for later because it was explicitly outside MVP v1.
- **Achievements:** added to backlog for later because it was explicitly future scope and not needed for the current Sprint Goal.
- **Full AI memory, summaries, and long-term dialog context:** deferred with rationale to later AI-assistant scope after baseline chat/event parsing and statistics are stable.

## Backlog Refinement Actions

The following follow-up PBIs should be created or refined in GitHub if the team decides to address them in a later Sprint. Each PBI should include a clear expected outcome, acceptance criteria, estimate, implementer, and different reviewer before it is selected for a Sprint.

| Backlog decision | Suggested PBI title | Reason |
|---|---|---|
| Create security hardening PBI | Hash stored passwords and remove plain-text credential handling | Customer explicitly raised password hashing as required for final implementation. |
| Create authorization PBI | Replace `user_id` query access with token-based authorization | Current user-specific access is useful for MVP, but not secure enough for production-like use. |
| Refine timeline/chat interaction PBI | Add created-event confirmation link or pre-filled form flow from chat | Customer described this as ideal behavior, but it was too large for the current MVP baseline. |
| Create optional media PBI | Add vehicle photo upload | Customer mentioned it as future scope, not MVP v1 scope. |
| Create optional engagement PBI | Add achievements system | Customer mentioned it as future scope, not MVP v1 scope. |
# Week 4 Report - CI and Quality Automation

This is the public Week 4 report section for Assignment 4 repository automation evidence. It documents the CI configuration work completed for Part 8 and the public evidence that must be included before submission.

## 1. Assignment scope

- Assignment/week: Week 4 / Assignment 4
- Part covered here: Part 8 - Configure CI
- Related issue: [#135](https://github.com/LAMBA-23/LAMBA/issues/135)
- Related branch: `135-configure-backend-ci`

## 2. CI configuration evidence

- Backend CI workflow: [`.github/workflows/backend-ci.yml`](../../.github/workflows/backend-ci.yml)
- Link-check workflow: [`.github/workflows/lychee.yml`](../../.github/workflows/lychee.yml)
- Testing status artifact: [`docs/testing.md`](../../docs/testing.md)
- Definition of Done update: [`docs/definition-of-done.md`](../../docs/definition-of-done.md)

## 3. Required CI links

Add the public GitHub Actions links here after pushing the branch and getting the first successful runs:

- Pull request CI run: `TODO after PR creation`
- Latest `main` backend CI run: `TODO after merge or protected-branch run`
- Latest `main` Lychee run: `TODO before final submission`

## 4. Branch protection or rules evidence

The report must include inspectable evidence that the protected default branch enforces the required review workflow.

- Existing branch protection screenshot from Week 2: [`reports/week2/images/branch-protection-rule.png`](../week2/images/branch-protection-rule.png)
- Recommended Week 4 evidence: add a fresh screenshot from the repository branch protection or rules settings if anything changed since Week 2

### Embedded branch protection evidence

![Branch protection evidence](../week2/images/branch-protection-rule.png)

## 5. Testing-report screenshots

The Week 4 public report must include screenshots showing the testing status evidence.

Recommended screenshots:

- `docs/testing.md` critical modules and coverage table
- `docs/testing.md` CI and QA status table
- Successful GitHub Actions `Backend CI` run
- Successful GitHub Actions `Link Check` run

Place the screenshots under `reports/week4/images/` and embed them here.

### Screenshot placeholders

- `TODO`: add `reports/week4/images/testing-report-coverage.png`
- `TODO`: add `reports/week4/images/testing-report-ci-status.png`
- `TODO`: add `reports/week4/images/backend-ci-success.png`
- `TODO`: add `reports/week4/images/lychee-success.png`

## 6. Current local verification evidence

The backend CI workflow was verified locally before push using the same command categories as the GitHub Actions job:

- `python -m ruff check backend/app backend/tests`
- `python -m ruff format --check backend/app backend/tests`
- `python -m coverage run -m pytest backend/tests`
- `python -m coverage report --include='backend/app/*'`
- `python -m pip check`

Current local results:

- backend tests: `32 passed`
- backend total coverage: `89%`
- critical module coverage:
  - `backend/app/main.py`: `95%`
  - `backend/app/chat_parser.py`: `59%`
  - `backend/app/database.py`: `100%`
- dependency health check: `No broken requirements found`

## 7. Remaining submission actions

Before final Week 4 submission, replace the `TODO` placeholders in this report with:

1. Real GitHub Actions run links
2. Fresh screenshots from the successful CI runs
3. Branch protection or rules evidence that matches the current repository settings
4. Any updated evidence from teammate-owned Assignment 4 documents once their PRs are merged
