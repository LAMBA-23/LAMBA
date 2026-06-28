# Week 4 Reflection

## Learning points

- Assignment 4 shifted the team focus from only delivering MVP functionality to preserving verifiable quality evidence. The most useful change was treating tests, CI checks, quality requirements, and testing documentation as maintained project assets instead of one-time submission artifacts.
- Defining quality requirements in [docs/quality-requirements.md](../../docs/quality-requirements.md) made backend risks more concrete. Data integrity, response time, and regression testability became easier to discuss because each requirement had a measurable scenario.
- Linking automated quality requirement tests in [docs/quality-requirement-tests.md](../../docs/quality-requirement-tests.md) showed that ordinary unit or API tests are not enough by themselves; they need traceability to a measurable quality requirement when they are used as quality evidence.
- The testing status document in [docs/testing.md](../../docs/testing.md) helped connect local verification, CI expectations, critical-module coverage, manual evidence, and follow-up work in one place.
- The events API work confirmed that a small endpoint can still need several layers of evidence: validation tests, persistence checks, user-isolation checks, manual add/read verification, API contract updates, and smoke-check instructions.
- CI work made the Definition of Done more concrete. It is easier to decide whether a PBI is complete when tests, coverage, linting, formatting, dependency checks, review, and documentation evidence are all visible in the normal workflow.
- Customer feedback response work in [reports/week4/README.md](./README.md) showed that not every feedback point should become immediate Sprint scope. Some items were addressed, while security hardening, token-based authorization, and richer AI/timeline interaction were intentionally left as follow-up backlog work.

## Validated assumptions

- The backend can support user-specific vehicle, event, and statistics data through the current `user_id`-based API contract, which is sufficient for the current Sprint increment even though it is not a final authorization model.
- The events timeline is an important workflow and needs both automated and manual evidence because it connects persistence, validation, and user-visible history.
- Automated tests for API behavior with SQLite-backed persistence are useful integration evidence for the current backend stack.
- Critical backend modules can meet the Assignment 4 minimum coverage expectation when coverage is measured and reported through the backend test workflow.
- The repository workflow benefits from issue-linked branches, PR evidence, and maintained docs because quality and testing work otherwise becomes hard to inspect later.
- The project still needs a stricter security model before production-like use; plain credential handling and query-parameter user selection are acceptable only as interim MVP-level simplifications.

## Friction and gaps

- Some Assignment 4 evidence depends on successful protected-default-branch CI runs and screenshots, so local verification alone is not enough for final submission.
- Quality requirement tests and testing documentation were created in separate workstreams, which made traceability sensitive to merge order.
- The current backend test setup is useful, but local Windows runs can produce environment-specific friction with temporary SQLite files and missing local tooling when the virtual environment is not synchronized with `backend/requirements.txt`.
- Manual evidence is still necessary for some flows, but it does not count as automated QRT evidence and must be clearly separated in documentation.
- Full token-based authorization and password hashing remain open product risks. They are documented as follow-up work rather than solved in the current Sprint.
- UAT and Sprint Review evidence must remain carefully separated from private recordings and customer-identifying information. This adds reporting overhead, but it is necessary for public repository safety.
- Some Week 4 report placeholders, such as final CI links and screenshots, must be replaced after the relevant PRs are merged and the public evidence is available.

## Planned response

- Keep [docs/testing.md](../../docs/testing.md), [docs/quality-requirements.md](../../docs/quality-requirements.md), [docs/quality-requirement-tests.md](../../docs/quality-requirement-tests.md), and [docs/definition-of-done.md](../../docs/definition-of-done.md) current as maintained project assets in later work.
- After merge, replace Week 4 report placeholders with real CI links, latest protected-branch run evidence, coverage screenshots, and other public artifacts required by [reports/week4/README.md](./README.md).
- Continue extending backend tests around critical workflows, especially event history, statistics, chat parsing, and user-specific data access.
- Refine security-related backlog items for password hashing and token-based authorization before treating the product as production-like.
- Keep manual evidence separate from automated QRT evidence and use it mainly for UAT, customer review, and smoke-check support.
- Preserve issue-to-branch-to-PR traceability for testing and documentation work so future Sprint work can reuse the quality gates instead of recreating evidence from scratch.
