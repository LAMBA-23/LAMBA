# Week 3 Reflection

## Learning points

- Product Backlog migration into GitHub issues improved traceability between user stories, PBIs, milestones, and PRs. The split between [docs/user-stories.md](../../docs/user-stories.md) and [docs/roadmap.md](../../docs/roadmap.md) helped separate stable scope from Sprint execution.
- Product Backlog refinement showed that MVP v1 had to stay narrow: registration, authorization, vehicle registration/display, and basic chat. Advanced AI features were correctly moved out of the current scope after the customer review in [customer-review-summary.md](./customer-review-summary.md).
- Estimation was affected by integration dependencies. Registration, onboarding, vehicle creation, and chat flow were not independent tasks because Android and backend changes had to match.
- Sprint Planning for [Sprint 1 - MVP v1 Foundation](https://github.com/LAMBA-23/LAMBA/milestone/1) worked best when tied to concrete issues: [#44](https://github.com/LAMBA-23/LAMBA/issues/44), [#45](https://github.com/LAMBA-23/LAMBA/issues/45), [#46](https://github.com/LAMBA-23/LAMBA/issues/46), [#47](https://github.com/LAMBA-23/LAMBA/issues/47), [#48](https://github.com/LAMBA-23/LAMBA/issues/48), [#67](https://github.com/LAMBA-23/LAMBA/issues/67), [#68](https://github.com/LAMBA-23/LAMBA/issues/68), [#69](https://github.com/LAMBA-23/LAMBA/issues/69), [#70](https://github.com/LAMBA-23/LAMBA/issues/70), and [#71](https://github.com/LAMBA-23/LAMBA/issues/71).
- MVP v1 delivery confirmed that the `Android -> FastAPI -> PostgreSQL` stack is workable, but also showed that a functional MVP still carries technical and process limitations.
- Customer review was useful because it clarified scope, confirmed simplifications, and highlighted remaining security and authorization expectations.
- Release preparation requires not only working code, but also runnable artifacts, smoke checks, and updated documentation such as [README.md](../../README.md) and [../week2/mvp-v0-report.md](../week2/mvp-v0-report.md).
- Workflow enforcement through issue templates, PR templates, and [docs/definition-of-done.md](../../docs/definition-of-done.md) is useful, but only if metadata, review links, and verification evidence are filled consistently.

## Validated assumptions

- MVP v1 should focus on a functional core flow, not a full AI-agent system. This was confirmed by the customer.
- One user having one vehicle is acceptable for MVP v1.
- Manual text input for vehicle brand and model is enough for the current MVP.
- Basic chat belongs in MVP v1, while advanced AI parsing, long-term memory, and automation should be deferred.
- The current stack and repository structure are sufficient for incremental delivery.
- Plain-text password handling is not acceptable for the final implementation and must be replaced with secure handling.
- Backend responses must be tied to the authorized user, not shared demo data.

## Friction and gaps

- Authorization was still not fully connected to real backend verification during the Week 3 review.
- Password security and final credential handling still need full end-to-end verification.
- The boundary between "basic chat" and future AI assistant features is still easy to blur.
- The deployed backend is limited by university-network access, which makes demonstration and verification harder.
- Login persistence was requested by the customer, but its exact MVP status remains unclear.
- Workflow metadata in GitHub is still sometimes incomplete, which weakens traceability and review evidence.
- Work on [#48](https://github.com/LAMBA-23/LAMBA/issues/48) was not completed within Week 3 because effort was redirected to overlapping higher-priority integration tasks in registration, onboarding, backend connection, and chat flow.
- There is still integration risk when Android expectations and backend behavior change separately.

## Planned response

- In the next Sprint or assignment, the team should close the remaining MVP v1 gaps around authorization, user-specific data access, and security hardening while keeping the Sprint 1 scope from [docs/roadmap.md](../../docs/roadmap.md).
- Follow-up work should continue only after the Sprint 1 foundation is stable and verified against [docs/definition-of-done.md](../../docs/definition-of-done.md).
- After stabilizing the delivered integration work, the team should return to [#48](https://github.com/LAMBA-23/LAMBA/issues/48) and complete the vehicle timeline flow that was delayed by overlapping Sprint work.
- After that, the team can continue with planned next items such as [#49](https://github.com/LAMBA-23/LAMBA/issues/49) and [#50](https://github.com/LAMBA-23/LAMBA/issues/50).
- The team should also improve workflow discipline by keeping issue metadata complete, linking PRs to issues, and recording review and verification evidence directly in GitHub and `reports/`.
