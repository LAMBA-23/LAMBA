# Week 7 Sprint Retrospective

**Sprint:** Sprint 5 — Final Transition and Delivery
**Date:** 2026-07-18
**Participants:** Team members

## What went well

- 27 issues closed in Sprint 5, all acceptance criteria satisfied.
- All CI checks passed throughout the Sprint (backend, Android, link-check).
- Dynamic chat style feature was implemented, tested, and deployed to the production server.
- Customer confirmed final acceptance of MVP v3 during the Week 7 meeting.
- Documentation was kept current: CHANGELOG, customer-handover, roadmap, and retrospective all updated.
- The team adapted quickly to the colleague's workflow requirement: code changes go through GitHub first, then deploy to the server.

## What did not go well

- Direct code changes were made on the production server before understanding the deployment workflow. This was caught and reverted, but it caused temporary blockers for the colleague who needed to deploy.
- The `style` parameter was not forwarded to `ask_deepseek()` in `main.py` when the style feature was merged. This was a one-line bug that slipped through review and required a separate fix PR (#355).
- Test `fake_ask` mock functions did not accept the new `style` parameter, causing CI failures after the fix. This required additional test updates.
- The ruff formatter was not run on modified test files before pushing, causing a second CI failure on formatting.

## What the team changed or attempted to change based on the previous Sprint Retrospective, and what results they observed

- The team adopted a stricter PR review process: every change goes through a branch → PR → review → merge → deploy workflow.
- The team started running `ruff check` and `ruff format` locally before pushing to catch CI failures early.
- The team verified all CI checks pass before requesting review, reducing back-and-forth on PRs.

## Action points

1. Add a pre-push checklist to the team workflow: run lint, format, and tests locally before pushing any commit.
2. Document the deployment process clearly in CONTRIBUTING.md or a dedicated deploy guide so all team members understand the workflow: local changes → GitHub → server deploy.
