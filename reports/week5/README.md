# Week 5 Report

## 1. Project name and short description

**LAMBA** is an Android application for creating a digital twin of a car. It lets a vehicle owner register, add a vehicle profile, record vehicle events through chat or a manual form, inspect the vehicle timeline, and view vehicle statistics.

## 2. Product Backlog board/view

- [Product Backlog board](https://github.com/orgs/LAMBA-23/projects/1)

## 3. Sprint Backlog board/table

- [Sprint Backlog board/table](https://github.com/orgs/LAMBA-23/projects/1/views/4)

## 4. Assignment 5 Sprint milestone

- [Sprint 3 - Maintenance Follow-up](https://github.com/LAMBA-23/LAMBA/milestone/3)

## 5. Sprint Goal, Sprint dates, and short scope summary

**Sprint dates:** 2026-06-29 to 2026-07-05

**Sprint Goal:** Deliver a more useful and reliable vehicle history experience by giving users an alternative manual way to add records, improving assistant answers with vehicle statistics, and preparing maintenance follow-up features that help users act on their recorded vehicle data.

**Scope summary:** Sprint 3 focused on MVP v2 maintenance follow-up: manual history record creation, statistics fixes, cleaner timeline behavior, main screen usability updates, assistant statistics support, release evidence, architecture documentation, and Week 5 review/UAT evidence.

## 6. Total Sprint size in Story Points

The total Sprint 3 issue size is **43 Story Points**.

Closed Sprint issues counted in the total:

| Issue | Story Points |
|---|---:|
| [#49](https://github.com/LAMBA-23/LAMBA/issues/49) | 5 |
| [#160](https://github.com/LAMBA-23/LAMBA/issues/160) | 3 |
| [#161](https://github.com/LAMBA-23/LAMBA/issues/161) | 5 |
| [#194](https://github.com/LAMBA-23/LAMBA/issues/194) | 2 |
| [#196](https://github.com/LAMBA-23/LAMBA/issues/196) | 2 |
| [#198](https://github.com/LAMBA-23/LAMBA/issues/198) | 5 |
| [#199](https://github.com/LAMBA-23/LAMBA/issues/199) | 2 |
| [#202](https://github.com/LAMBA-23/LAMBA/issues/202) | 3 |
| [#204](https://github.com/LAMBA-23/LAMBA/issues/204) | 5 |
| [#205](https://github.com/LAMBA-23/LAMBA/issues/205) | 3 |
| [#210](https://github.com/LAMBA-23/LAMBA/issues/210) | 5 |
| [#212](https://github.com/LAMBA-23/LAMBA/issues/212) | 3 |

Total: `5 + 3 + 5 + 2 + 2 + 5 + 2 + 3 + 5 + 3 + 5 + 3 = 43 SP`.

## 7. Summary of delivered product changes

During Week 5 / Assignment 5, the team delivered the Sprint 3 MVP v2 increment:

- added an Android manual history record creation form for fuel, repair, maintenance, and trip records;
- added backend support for manual vehicle history records through the existing event model;
- fixed mileage, fuel liters, expense, period, and record-count behavior in statistics;
- connected chat questions to vehicle-history answers through the backend question endpoint;
- removed non-event chat messages from the timeline so the history view stays focused on vehicle records;
- updated main screen actions and frontend layout behavior for better usability;
- published `v1.2.0` with an Android APK artifact and maintained release evidence;
- added maintained architecture, ADR, quality, testing, UAT, Sprint Review, retrospective, reflection, and documentation-site evidence.

The following planned items were not completed and remain backlog or follow-up work:

| Issue | Reason |
|---|---|
| [#51](https://github.com/LAMBA-23/LAMBA/issues/51) | Maintenance recommendations were deferred until core data-entry and statistics gaps are stable. |
| [#52](https://github.com/LAMBA-23/LAMBA/issues/52) | Notifications depend on stable recommendation behavior and were deferred. |

## 8. Link to deployed product, hosted artifact, package, or runnable product

Current public access artifacts for the Assignment 5 Sprint increment:

- [GitHub Release: v1.2.0 - Assignment 5 Sprint 3 Increment (MVP v2)](https://github.com/LAMBA-23/LAMBA/releases/tag/v1.2.0)
- [Android APK asset: mvp-v2.apk](https://github.com/LAMBA-23/LAMBA/releases/download/v1.2.0/mvp-v2.apk)
- [Hosted maintained documentation site](https://lamba-23.github.io/LAMBA/)
- [Documentation-site task #221](https://github.com/LAMBA-23/LAMBA/issues/221)
- [Deployed backend Swagger UI](http://186.246.27.211:8000/docs)
- Deployed backend API base host: `186.246.27.211:8000`
- Public sanitized demo video: https://drive.google.com/drive/folders/19UU6YERENNanCGjQec7BVcUMXiITHhVT
- [Runnable backend source: docker-compose.yml](../../docker-compose.yml)

## 9. Link to current access or run instructions

- [Root README - Local Setup](../../README.md#local-setup)
- [Root README - Assignment 5 Sprint Increment Release](../../README.md#assignment-5-sprint-increment-release)
- [Root README - Runnable Artifact](../../README.md#runnable-artifact)

## 10. Customer feedback response table

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

## 11. Explanation of feedback not addressed

- **Password hashing and token-based authorization:** deferred because Sprint 3 focused on product-flow stabilization, release evidence, and customer-visible MVP v2 improvements. This remains a security hardening follow-up.
- **Long-term dialog memory and broader AI summaries/routes/fuel/repair support:** partially addressed through baseline parsing, statistics, and assistant work, but not completed as full product behavior.
- **Chat-to-prefilled-form redirect:** deferred because the timeline, manual entry, and event flow needed to be stable before adding a confirmation/redirect workflow.
- **Vehicle photo upload:** kept as later optional media scope, not an MVP v2 priority.
- **Achievements:** kept as later engagement scope because it is lower priority than core vehicle-history and quality risks.
- **Product access limitations:** partially handled through release evidence, APK, deployed backend/Swagger, documentation site, and sanitized demo video, with realistic APK testing still listed as follow-up in the Sprint Review.

## 12. Link to docs/roadmap.md

- [docs/roadmap.md](../../docs/roadmap.md)

## 13. Link to docs/definition-of-done.md

- [docs/definition-of-done.md](../../docs/definition-of-done.md)

## 14. Link to docs/testing.md

- [docs/testing.md](../../docs/testing.md)

## 15. Link to docs/quality-requirements.md

- [docs/quality-requirements.md](../../docs/quality-requirements.md)

## 16. Link to docs/quality-requirement-tests.md

- [docs/quality-requirement-tests.md](../../docs/quality-requirement-tests.md)

## 17. Link to docs/user-acceptance-tests.md

- [docs/user-acceptance-tests.md](../../docs/user-acceptance-tests.md)

## 18. Link to docs/development-process.md

- [docs/development-process.md](../../docs/development-process.md)

## 19. Link to docs/architecture/README.md

- [docs/architecture/README.md](../../docs/architecture/README.md)

## 20. Links to the static, dynamic, and deployment view artifacts

- [Static view: component diagram](../../docs/architecture/static-view/component-diagram.puml)
- [Dynamic view: chat event sequence](../../docs/architecture/dynamic-view/chat-event-sequence.puml)
- [Deployment view: deployment diagram](../../docs/architecture/deployment-view/deployment-diagram.puml)

## 21. Link to the ADR directory or ADR index

- [ADR directory](../../docs/architecture/adr/)
- [ADR index in architecture README](../../docs/architecture/README.md#architecture-decision-records)
