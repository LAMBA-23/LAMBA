# Week 5 Report

## Part 1: Sprint 3 — MVP v2 Increment

- Sprint: Sprint 3 - Maintenance Follow-up
- Dates: 2026-06-29 to 2026-07-05
- MVP target: MVP v2
- Release: [v1.2.0 - Assignment 5 Sprint 3 Increment (MVP v2)](https://github.com/LAMBA-23/LAMBA/releases/tag/v1.2.0)
- Sprint milestone: [Sprint 3 - Maintenance Follow-up](https://github.com/LAMBA-23/LAMBA/milestone/3)
- Hosted maintained documentation site: [https://lamba-23.github.io/LAMBA/](https://lamba-23.github.io/LAMBA/)
- Deployed backend URL: `http://186.246.27.211:8000`
- Public sanitized demo video: https://drive.google.com/drive/folders/19UU6YERENNanCGjQec7BVcUMXiITHhVT

## Part 2: Respond to Customer Feedback on MVP v1

We reviewed the feedback from the MVP v1 customer review and recorded what we did with each important point. If we did not take something into Sprint 3, we still wrote down why.

| Feedback point | Resulting PBI or issue | Status | Response |
|---|---|---|---|
| Passwords should not stay as plain text. | Security hardening PBI for password hashing. | Added to the backlog for later | We agree this is important, but Sprint 3 focused first on stabilizing the main product flow and release evidence. |
| Backend should return data for the current user, not shared demo data. | [#44](https://github.com/LAMBA-23/LAMBA/issues/44), [#67](https://github.com/LAMBA-23/LAMBA/issues/67), [#68](https://github.com/LAMBA-23/LAMBA/issues/68), [#71](https://github.com/LAMBA-23/LAMBA/issues/71) | Partially addressed | Registration, vehicle, and event data now use the selected user. Full token-based authorization is still future work. |
| The user should stay logged in if possible. | [#71](https://github.com/LAMBA-23/LAMBA/issues/71) | Addressed in the Sprint | Added local session persistence in the Android app. |
| Vehicle registration should happen after user registration. | [#45](https://github.com/LAMBA-23/LAMBA/issues/45), [#67](https://github.com/LAMBA-23/LAMBA/issues/67), [#71](https://github.com/LAMBA-23/LAMBA/issues/71) | Addressed in the Sprint | Added vehicle setup after registration/onboarding. |
| Vehicle brand, model, year, and mileage should be stored. | [#45](https://github.com/LAMBA-23/LAMBA/issues/45), [#67](https://github.com/LAMBA-23/LAMBA/issues/67) | Addressed in the Sprint | Added these fields to the vehicle profile flow. |
| Basic chat should be included in MVP v1. | [#46](https://github.com/LAMBA-23/LAMBA/issues/46), [#69](https://github.com/LAMBA-23/LAMBA/issues/69), [#70](https://github.com/LAMBA-23/LAMBA/issues/70) | Addressed in the Sprint | Added chat messages, backend parsing, and assistant responses. |
| AI should ask clarification questions when it cannot understand the message. | [#69](https://github.com/LAMBA-23/LAMBA/issues/69), [#70](https://github.com/LAMBA-23/LAMBA/issues/70) | Partially addressed | Added basic clarification responses. Long-term dialog memory is not done yet. |
| AI should later support statistics, summaries, routes, fuel, and repairs. | [#47](https://github.com/LAMBA-23/LAMBA/issues/47), [#49](https://github.com/LAMBA-23/LAMBA/issues/49), [#50](https://github.com/LAMBA-23/LAMBA/issues/50) | Partially addressed | Basic event parsing was started. Statistics and summaries are planned for later PBIs. |
| Chat could redirect to pre-filled repair/fuel/trip forms. | Future chat-to-timeline PBI. | Rejected or deferred with rationale | We postponed this because the timeline and event flow should be stable first. |
| Vehicle photo upload could be added later. | Future optional media PBI. | Added to the backlog for later | This was mentioned as future scope, not MVP v2 priority. |
| Achievements could be added later. | Future optional achievements PBI. | Added to the backlog for later | This is lower priority than core product flow and quality risks. |
| Customer needs a way to inspect the product despite deployment limits. | Release evidence and video demo. | Partially addressed | We used a video demo as fallback and kept release/deployment access as a risk to track. |

Sprint 3 scope was chosen not just by the number of issues we could close, but by customer value, quality improvement, risk reduction, and whether the work could be shown as Done.

## Part 3: Development Process and Configuration Management

The hosted maintained documentation site is available at [https://lamba-23.github.io/LAMBA/](https://lamba-23.github.io/LAMBA/).

The team's development process is documented in [docs/development-process.md](../../docs/development-process.md). That document covers:

- Git branching and review workflow with Mermaid gitGraph diagram
- Issue types, templates, and workflow states
- Board and Sprint milestone configuration
- Configuration and secrets management
- Development environment setup
- CI pipeline and branch protection
- Release and deployment process

## Part 4: Sprint 3 Increment — What Was Delivered

The following items were completed and verified in Sprint 3:

| Issue | Description | Type |
|---|---|---|
| [#161](https://github.com/LAMBA-23/LAMBA/issues/161) | Add a manual form for creating vehicle history records | PBI |
| [#205](https://github.com/LAMBA-23/LAMBA/issues/205) | Backend support for manual vehicle history form | PBI |
| [#208](https://github.com/LAMBA-23/LAMBA/issues/208) | Implement manual history record creation | PBI |
| [#196](https://github.com/LAMBA-23/LAMBA/issues/196) | Change buttons on the main screen | PBI |
| [#199](https://github.com/LAMBA-23/LAMBA/issues/199) | Change expenses field to fuel refueling | PBI |
| [#202](https://github.com/LAMBA-23/LAMBA/issues/202) | Remove non-event chat messages from timeline | PBI |
| [#198](https://github.com/LAMBA-23/LAMBA/issues/198) | Fix backend statistics mileage and fuel liters | Bug |
| [#204](https://github.com/LAMBA-23/LAMBA/issues/204) | Fix statistics mileage and fuel liters | Bug |
| [#194](https://github.com/LAMBA-23/LAMBA/issues/194) | Frontend issues | Bug |

The following items were planned but not completed and moved to Backlog:

| Issue | Reason |
|---|---|
| [#160](https://github.com/LAMBA-23/LAMBA/issues/160) | Not completed before sprint end, moved to Backlog. |
| [#49](https://github.com/LAMBA-23/LAMBA/issues/49) | Full acceptance criteria not met because #160 was not completed. |
| [#51](https://github.com/LAMBA-23/LAMBA/issues/51) | Not started, deferred because #160 was not completed first. |
| [#52](https://github.com/LAMBA-23/LAMBA/issues/52) | Not started, depends on #51. |

## Part 5: Week 5 UAT

### UAT-003: Add and view a history record

- **Execution status:** Completed
- **Date:** TODO after execution
- **Executed by:** TODO
- **Result:** TODO (Passed / Needs changes)
- **Customer comments or observed issues:** TODO
- **Traceability:** US-05: View vehicle timeline, #161
- **Resulting PBIs/issues:** TODO

### Week 5 UAT Summary

- **Scenarios executed:** TODO
- **Passed scenarios:** TODO
- **Failed / needs changes:** TODO
- **Most important feedback points:** TODO

## Part 6: Release and Deployment Evidence

- Release: [v1.2.0](https://github.com/LAMBA-23/LAMBA/releases/tag/v1.2.0)
- Hosted maintained documentation site: [https://lamba-23.github.io/LAMBA/](https://lamba-23.github.io/LAMBA/)
- Deployed backend URL: `http://186.246.27.211:8000`
- Swagger UI: `http://186.246.27.211:8000/docs`
- Android APK: attached to GitHub Release or at `app/build/outputs/apk/debug/app-debug.apk`
- Public sanitized demo video: https://drive.google.com/drive/folders/19UU6YERENNanCGjQec7BVcUMXiITHhVT
- CI run evidence: [GitHub Actions](https://github.com/LAMBA-23/LAMBA/actions)
## Part 9: Conduct the Sprint Review

Sprint Review evidence:

- [Sprint Review transcript](./sprint-review-transcript.md)
- [Sprint Review summary](./sprint-review-summary.md)
- Tracking issue: [#216 Complete Week 5 Sprint Review artifacts](https://github.com/LAMBA-23/LAMBA/issues/216)

The Sprint Review was conducted with the customer on 04.07.2026. Recording and public publication of the sanitized English transcript were permitted by the customer. Public artifacts use only the participant labels `Customer` and `Team member`.

Part 9 evidence checklist:

| Requirement | Evidence |
|---|---|
| Planned Sprint Goal | [Sprint Review summary - Sprint Goal Reviewed](./sprint-review-summary.md#sprint-goal-reviewed) |
| Delivered MVP v2 increment | [Sprint Review summary - Delivered MVP v2 Increment Discussed](./sprint-review-summary.md#delivered-mvp-v2-increment-discussed) |
| Addressed customer feedback | [Sprint Review summary - Addressed Customer Feedback](./sprint-review-summary.md#addressed-customer-feedback) |
| UAT results | [Sprint Review summary - Customer-Executed UAT Results](./sprint-review-summary.md#customer-executed-uat-results) |
| Architecture documentation and ADR updates/discussion | [Sprint Review summary - Architecture and ADR Discussion](./sprint-review-summary.md#architecture-and-adr-discussion), [docs/architecture/README.md](../../docs/architecture/README.md) |
| Quality requirement and CI evidence to continue | [Sprint Review summary - Quality Requirement and CI Evidence](./sprint-review-summary.md#quality-requirement-and-ci-evidence), [docs/quality-requirements.md](../../docs/quality-requirements.md), [docs/quality-requirement-tests.md](../../docs/quality-requirement-tests.md) |
| Remaining gaps, risks, and follow-up PBIs | [Sprint Review summary - Remaining Gaps and Risks](./sprint-review-summary.md#remaining-gaps-and-risks), [Sprint Review summary - Product Backlog Adaptation Recommendations](./sprint-review-summary.md#product-backlog-adaptation-recommendations) |

No new Product Backlog Item issues were created from this review yet. The summary records recommended backlog updates for later refinement and customer/team approval.

## Part 10: Conduct the Sprint Retrospective

Sprint Retrospective evidence:

- [Sprint Retrospective](./retrospective.md)
- Tracking issue: [#220 Complete Week 5 Part 10, Part 12, and Part 14 artifacts](https://github.com/LAMBA-23/LAMBA/issues/220)

Part 10 evidence checklist:

| Requirement | Evidence |
|---|---|
| What went well | [Sprint Retrospective - What went well](./retrospective.md#what-went-well) |
| What did not go well | [Sprint Retrospective - What did not go well](./retrospective.md#what-did-not-go-well) |
| Previous retrospective follow-up | [Sprint Retrospective - What the team changed or attempted to change based on the previous Sprint Retrospective, and what results they observed](./retrospective.md#what-the-team-changed-or-attempted-to-change-based-on-the-previous-sprint-retrospective-and-what-results-they-observed) |
| Action points | [Sprint Retrospective - Action points](./retrospective.md#action-points) |

## Part 12: Reflection

Reflection evidence:

- [Week 5 Reflection](./reflection.md)
- Tracking issue: [#220 Complete Week 5 Part 10, Part 12, and Part 14 artifacts](https://github.com/LAMBA-23/LAMBA/issues/220)

Part 12 evidence checklist:

| Requirement | Evidence |
|---|---|
| Learning points | [Week 5 Reflection - Learning points](./reflection.md#learning-points) |
| Validated assumptions | [Week 5 Reflection - Validated assumptions](./reflection.md#validated-assumptions) |
| Friction and gaps | [Week 5 Reflection - Friction and gaps](./reflection.md#friction-and-gaps) |
| Planned response | [Week 5 Reflection - Planned response](./reflection.md#planned-response) |

## Part 14: LLM Usage Report

LLM usage evidence:

- [Week 5 LLM Usage Report](./llm-report.md)
- Tracking issue: [#220 Complete Week 5 Part 10, Part 12, and Part 14 artifacts](https://github.com/LAMBA-23/LAMBA/issues/220)

Part 14 evidence checklist:

| Requirement | Evidence |
|---|---|
| Models and tools used | [Week 5 LLM Usage Report - Models and Tools Used](./llm-report.md#models-and-tools-used) |
| Usage areas and purposes | [Week 5 LLM Usage Report - How AI/LLM Tools Were Used](./llm-report.md#how-aillm-tools-were-used) |
| Human review and verification | [Week 5 LLM Usage Report - Human Review and Verification](./llm-report.md#human-review-and-verification) |
