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
- [ ] **Coverage expectations for critical modules:** Changes affecting critical modules satisfy the required coverage expectations documented in `docs/testing.md`.
- [ ] **Testing evidence preserved:** Testing evidence is preserved in the linked PR, CI runs, coverage reports, or linked maintained documentation.
- [ ] **Traceability preserved:** The linked issue, branch, PR, review, CI evidence, and related documentation remain inspectable.
- [ ] **Documentation updated when needed:** Relevant product, setup, API, workflow, or testing documentation is updated when the change affects it.
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
- maintained documentation such as `docs/testing.md`

## Maintenance Rule

If later project work changes the product stack, CI configuration, quality requirements, quality requirement tests, or critical modules, this Definition of Done and the related testing evidence must be updated so the completion standard stays current.
