# Definition of Done

This document defines the team's shared minimum completion standard for work in this repository.

A Product Backlog Item (PBI) may be marked `Done` only when:

1. its issue-specific acceptance criteria are satisfied; and
2. this Definition of Done is satisfied.

For supporting or implementation PBIs, `Done` normally also means the linked PR is reviewed, merged into the protected default branch, and supported by verification evidence.

For user stories, `Done` means the story acceptance criteria are satisfied and all linked supporting PBIs required to satisfy those criteria are reviewed, merged, verified, and marked `Done`.

## Required for Every PBI

A PBI can be marked `Done` only when all relevant items below are true:

- [ ] **Acceptance criteria verification:** All issue acceptance criteria have been verified with observable evidence.
- [ ] **Review by another team member:** The work has been reviewed and approved by a reviewer who is different from the implementer.
- [ ] **Passing CI checks:** All CI checks required for the current product stack pass before merge.
- [ ] **Relevant automated tests:** Relevant automated unit and integration tests pass for the changed product area.
- [ ] **Relevant automated quality requirement tests:** All applicable automated quality requirement tests pass, or the issue records why they are not applicable.
- [ ] **Requirement-driven test scope:** New tests are added when changed or newly important product areas need credible automated coverage; work is not considered incomplete only because it did not add a fixed number of tests.
- [ ] **Coverage expectations for critical modules:** Changes affecting critical modules satisfy the required coverage expectations documented in `docs/testing.md`.
- [ ] **Testing evidence preserved:** Testing evidence is preserved in the linked PR, CI runs, coverage reports, or linked maintained documentation.
- [ ] **Architecture documentation applicability:** Relevant architecture documentation and ADRs are satisfied, or the issue or PR explicitly records why they are not applicable.
- [ ] **Traceability preserved:** The linked issue, branch, PR, review, CI evidence, and related documentation remain inspectable.
- [ ] **Documentation updated when needed:** Relevant product, setup, API, workflow, architecture, or testing documentation is updated when the change affects it.
- [ ] **Changelog updated for user-visible changes:** `CHANGELOG.md` is updated for every user-visible change, or the PR explicitly states that no changelog update is required.
- [ ] **Sensitive data check:** The change does not introduce credentials, secrets, private data, or prohibited public artifacts.

## Additional Requirements for User Stories

In addition to the rules above, a user story may be marked `Done` only when:

- [ ] All linked supporting PBIs required to satisfy the story acceptance criteria are completed.
- [ ] The linked supporting PBIs provide the required implementation, review, and verification evidence.
- [ ] Story-to-PBI, PBI-to-PR, and PR-to-verification traceability is preserved.

## Default Evidence Sources

The default evidence sources for `Done` are:

- linked issue and linked PR
- reviewer approval in PR history
- passing CI runs on the PR and protected default branch
- automated test and coverage reports
- maintained documentation such as `docs/testing.md`, `docs/quality-requirements.md`, and `docs/quality-requirement-tests.md`

## Maintenance Rule

If later project work changes the product stack, CI configuration, quality requirements, quality requirement tests, or critical modules, this Definition of Done and the related testing evidence must be updated so the completion standard stays current.

For MVP v2 / Sprint 3 work, `docs/testing.md`, `docs/quality-requirements.md`, and `docs/quality-requirement-tests.md` must be updated when manual event creation, assistant behavior, statistics behavior, maintenance recommendations, notifications, or related architecture decisions introduce new verification needs.

After Assignment 5 architecture documentation is introduced, PBIs and user stories must preserve traceability to the relevant ADRs or explicitly document that no ADR applies.

## MVP v2 / Sprint 3 Completion Standard

As of this update, the implemented MVP v2 work extends product behavior around manual event creation, assistant context handling, and statistics calculations, but it has not introduced a documented architecture change, deployment-model change, repository-workflow change, or CI-configuration change beyond the Assignment 4 baseline.

The current completion standard therefore remains the Assignment 4 Definition of Done plus these active Assignment 5 documentation and evidence expectations:

- applicable QR and QRT traceability must remain current for QR-001 through QR-006
- changed backend behavior must remain covered by the backend regression suite and the critical-module coverage gate
- ADR traceability must be preserved for the quality requirements affected by the change

If later Sprint 3 implementation adds recommendation services, notification workflow logic, new persistence models, deployment changes, workflow changes, or CI changes, this Definition of Done must be updated again so it still describes the current completion standard.
