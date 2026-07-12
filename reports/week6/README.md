# Week 6 Report

## 1. Project name and short description

**LAMBA** is an Android application for creating a digital twin of a car. It lets a vehicle owner register, add a vehicle profile, record vehicle events through chat or a manual form, inspect the vehicle timeline, and view vehicle statistics.

## 2. Product Backlog board/view

- [Product Backlog board](https://github.com/orgs/LAMBA-23/projects/1)

## 3. Sprint 4 Backlog board/view

- [Sprint 4 Backlog board/view](https://github.com/orgs/LAMBA-23/projects/1/views/4)

## 4. Sprint 4 milestone

- [Sprint 4 - Trial Release and Transition Readiness](https://github.com/LAMBA-23/LAMBA/milestone/4)

## 5. Sprint 4 Goal, Sprint dates, and short scope summary

**Sprint dates:** 2026-07-06 to 2026-07-12

**Sprint Goal:** Deliver MVP v3 trial-release readiness by closing the main customer-agreed gaps before final project delivery: recommendations and notifications, short chat-history persistence, decimal fuel liters, AI statistics answers, odometer-based trip records, repair/breakdown records, manual-form photo attachments, removal of unused voice input, application icon, session restore, and logout support and OWASP.

**Scope summary:** Sprint 4 focused on MVP v3 trial-release readiness: security hardening with password hashing and rate limiting, Android logout functionality with local session cleanup, local chat history persistence for the last five dialogs, improved statistics UI for adaptive display, support for decimal fuel liters in events, trip records by odometer start/end values, repair and breakdown event records, application launcher icon, session restore improvements after app restart, deployment on the customer-provided server, and customer-facing documentation review.

## 6. Total Sprint size in Story Points

The total Sprint 4 issue size is **41 Story Points**.

Closed Sprint issues counted in the total:

| Issue | Story Points |
|---|---:|
| [#233](https://github.com/LAMBA-23/LAMBA/issues/233) | 3 |
| [#234](https://github.com/LAMBA-23/LAMBA/issues/234) | 3 |
| [#238](https://github.com/LAMBA-23/LAMBA/issues/238) | 1 |
| [#239](https://github.com/LAMBA-23/LAMBA/issues/239) | 3 |
| [#242](https://github.com/LAMBA-23/LAMBA/issues/242) | 2 |
| [#243](https://github.com/LAMBA-23/LAMBA/issues/243) | 3 |
| [#245](https://github.com/LAMBA-23/LAMBA/issues/245) | 3 |
| [#246](https://github.com/LAMBA-23/LAMBA/issues/246) | 5 |
| [#247](https://github.com/LAMBA-23/LAMBA/issues/247) | 3 |
| [#265](https://github.com/LAMBA-23/LAMBA/issues/265) | 5 |
| [#271](https://github.com/LAMBA-23/LAMBA/issues/271) | 2 |
| [#277](https://github.com/LAMBA-23/LAMBA/issues/277) | 5 |
| [#278](https://github.com/LAMBA-23/LAMBA/issues/278) | 3 |

Total: `3 + 3 + 1 + 3 + 2 + 3 + 3 + 5 + 3 + 5 + 2 + 5 + 3 = 41 SP`.

## 7. Summary of the Week 6 trial-release changes

During Week 6 / Assignment 6, the team delivered the Sprint 4 MVP v3 trial-release increment:

- added security hardening with password hashing and request-rate protections;
- removed the demo account;
- added Android logout functionality with local session and chat-history cleanup;
- added local chat history persistence for the last five dialogs;
- added session restore after application restart;
- added support for decimal fuel liters in events;
- added trip records by odometer start and end values;
- added repair and breakdown event records;
- improved statistics UI for adaptive display on different screen sizes;
- improved AI statistics response formatting;
- added application launcher icon;
- removed unused voice input from the frontend;
- deployed on the customer-provided server with access outside the university network;
- created customer-handover documentation, contributor guidance, and agent guidance;
- published `v1.3.0` with a trial-release SemVer tag and release evidence.

The following planned items were not completed and remain Sprint 5 follow-up work:

| Issue | Reason |
|---|---|
| [#264](https://github.com/LAMBA-23/LAMBA/issues/264) | Decimal fuel liters in the Android event form were not fully verified. |
| [#272](https://github.com/LAMBA-23/LAMBA/issues/272) | Trip recording using start and end odometer values was not fully working. |
| [#273](https://github.com/LAMBA-23/LAMBA/issues/273) | Breakdown records with photo attachment UI were incomplete. |
| [#51](https://github.com/LAMBA-23/LAMBA/issues/51) | Maintenance recommendations were deferred to Sprint 5. |
| [#52](https://github.com/LAMBA-23/LAMBA/issues/52) | Notifications depend on stable recommendation behavior. |

## 8. Link to the Week 6 product access artifact

- [GitHub Release: v1.3.0 - Assignment 6 Sprint 4 Increment (MVP v3 Trial Release)](https://github.com/LAMBA-23/LAMBA/releases/tag/v1.3.0)
- [Deployed backend Swagger UI](http://186.246.27.211:8000/docs)
- Deployed backend API base host: `186.246.27.211:8000`

## 9. Link to current access or run instructions

- [Root README - Local Setup](../../README.md#local-setup)
- [Root README - Current Product Access](../../README.md#current-product-access)
- [Root README - Verification](../../README.md#verification)

## 10. Link to README.md

- [README.md](../../README.md)

## 11. Link to CONTRIBUTING.md

- [CONTRIBUTING.md](../../CONTRIBUTING.md)

## 12. Link to AGENTS.md

- [AGENTS.md](../../AGENTS.md)

## 13. Link to docs/customer-handover.md

- [docs/customer-handover.md](../../docs/customer-handover.md)

## 14. Link to the hosted documentation site

- [LAMBA maintained documentation site](https://lamba-23.github.io/LAMBA/)

## 15. Summary of the customer-facing documentation review

During the Week 6 meeting, the customer requested the complete documentation set, not only basic usage instructions.

The customer specifically asked for:

- detailed instructions written for a non-technical reader;
- product usage instructions;
- installation and deployment instructions;
- configuration and secrets-handling guidance;
- troubleshooting information;
- source archives for all project components;
- all maintained product and process documentation;
- material that would help a future team understand how the product was developed.

The customer did not require repository administrator rights. The preferred handover format is one or more complete archives delivered through customer-accessible storage. The customer did not request public product availability.

## 16. Transition-readiness summary

The product was **not yet ready for full independent use** at the time of the Week 6 meeting.

At the same time:

- the backend and product services were already deployed on infrastructure provided and controlled by the customer;
- the customer had not yet started regular independent use because the product had not been finally handed over;
- the customer stated that the intended everyday use is vehicle bookkeeping: trips, refuellings, expenses, and AI-assisted interaction;
- the customer confirmed that the product should be ready for use after the remaining follow-up work is completed.

The reached Week 6 status:

- **Handover level:** not yet `Ready for independent use`;
- **Customer confirmation:** `Accepted with follow-up items` for the trial direction, not final acceptance of MVP v3.

Final handover status must be confirmed again during the follow-up Sprint.

## 17. Customer feedback response table

| Feedback point | Resulting PBI or issue | Status | Response |
|---|---|---|---|
| Fractional fuel volume is not saved correctly. | [#264](https://github.com/LAMBA-23/LAMBA/issues/264) | Deferred to Sprint 5 | Backend supports it, but the Android event form was not fully verified. |
| Monetary values need kopeck precision. | New Sprint 5 PBI | Deferred to Sprint 5 | Will be addressed in Sprint 5. |
| Trip entry using odometer values does not work. | [#272](https://github.com/LAMBA-23/LAMBA/issues/272) | Deferred to Sprint 5 | Backend supports it, but the Android form was not fully working. |
| Repair and breakdown entry does not work correctly. | [#247](https://github.com/LAMBA-23/LAMBA/issues/247) | Partially addressed | Backend records added, but Android form needs fixes. |
| Photo attachment is unavailable. | [#273](https://github.com/LAMBA-23/LAMBA/issues/273) | Deferred to Sprint 5 | UI was started but not completed. |
| Recommendations are unfinished. | [#51](https://github.com/LAMBA-23/LAMBA/issues/51) | Deferred to Sprint 5 | Will be delivered as rule-based notifications. |
| Customer's older account does not display new events correctly. | New Sprint 5 PBI | Deferred to Sprint 5 | Account-data compatibility issue to investigate. |
| AI responses are too dry. | New Sprint 5 PBI | Deferred to Sprint 5 | Prompt refinement planned for Sprint 5. |
| Handover documentation must support a non-technical reader. | [#267](https://github.com/LAMBA-23/LAMBA/issues/267) | Addressed in the Sprint | Customer-handover documentation created and expanded. |
| Customer wants complete source and documentation archives. | Transition PBI | Deferred to Sprint 5 | Archives will be prepared for final delivery. |

## 18. Explanation of feedback not yet addressed

- **Decimal fuel liters in Android form:** deferred because backend support was added but Android-side verification was incomplete before the trial.
- **Kopeck-level monetary precision:** new requirement discovered during the trial, added to Sprint 5 backlog.
- **Odometer-based trip entry:** backend supports it but Android form was not fully working, deferred to Sprint 5.
- **Photo attachments:** UI was started but not completed, deferred to Sprint 5.
- **Recommendations:** implementation approach clarified during the meeting (rule-based notifications instead of chat messages), deferred to Sprint 5.
- **Older-account data compatibility:** discovered during the trial, needs investigation in Sprint 5.
- **AI response tone:** customer requested more natural conversational responses, deferred to Sprint 5.
- **Source and documentation archives:** customer requested complete archives for handover, will be prepared in Sprint 5.

## 19. Link to docs/roadmap.md

- [docs/roadmap.md](../../docs/roadmap.md)

## 20. Link to the maintained quality, testing, architecture, development-process, and other customer-relevant documentation updated during Sprint 4

- [docs/quality-requirements.md](../../docs/quality-requirements.md)
- [docs/quality-requirement-tests.md](../../docs/quality-requirement-tests.md)
- [docs/testing.md](../../docs/testing.md)
- [docs/user-acceptance-tests.md](../../docs/user-acceptance-tests.md)
- [docs/definition-of-done.md](../../docs/definition-of-done.md)
- [docs/development-process.md](../../docs/development-process.md)
- [docs/architecture/README.md](../../docs/architecture/README.md)
- [docs/architecture/adr/](../../docs/architecture/adr/)
- [docs/architecture/static-view/component-diagram.puml](../../docs/architecture/static-view/component-diagram.puml)
- [docs/architecture/dynamic-view/chat-event-sequence.puml](../../docs/architecture/dynamic-view/chat-event-sequence.puml)
- [docs/architecture/deployment-view/deployment-diagram.puml](../../docs/architecture/deployment-view/deployment-diagram.puml)
- [docs/customer-handover.md](../../docs/customer-handover.md)
- [CONTRIBUTING.md](../../CONTRIBUTING.md)
- [AGENTS.md](../../AGENTS.md)

## 21. Summary of relevant UAT or customer-trial results

Week 6 executed UAT scenarios during the customer trial:

| UAT scenario | Result | Main feedback |
|---|---|---|
| Existing chat history can be opened and continued | Passed | The customer opened chat history and confirmed it was visible and usable. |
| Session remains active after closing and reopening | Passed | The application returned to the previously authenticated account. |
| Statistics update after adding events through chat | Partially passed | Event count changed, but items did not appear correctly in History for the customer's older account. |
| Fractional fuel volume | Failed | A fractional value could be entered but was not saved correctly. |
| Trip entry using odometer values | Failed | The workflow was reported as not working correctly. |
| Repair and breakdown entry | Failed | The workflow was reported as not working correctly. |
| AI statistics response formatting | Passed | The customer reacted positively to the updated output. |
| Responsive Statistics UI | Passed | Adaptation issues on different devices had been fixed. |

## 22. Link to the Week 6 SemVer trial release

- [v1.3.0 - Assignment 6 Sprint 4 Increment (MVP v3 Trial Release)](https://github.com/LAMBA-23/LAMBA/releases/tag/v1.3.0)

## 23. Link to CHANGELOG.md

- [CHANGELOG.md](../../CHANGELOG.md)

## 24. Link to the published Sprint Review transcript

- [Published sanitized Sprint Review transcript](./sprint-review-transcript.md)

The Sprint Review transcript is public and sanitized. Participant names are generalized as `Customer` and `Team representative`.

## 25. Link to reports/week6/sprint-review-summary.md

- [reports/week6/sprint-review-summary.md](./sprint-review-summary.md)

## 26. Link to reports/week6/reflection.md

- [reports/week6/reflection.md](./reflection.md)

## 27. Link to reports/week6/retrospective.md

- [reports/week6/retrospective.md](./retrospective.md)

## 28. Link to reports/week6/llm-report.md

- [reports/week6/llm-report.md](./llm-report.md)

## 29. Summary of the current product status and expected Week 7 follow-up work

The MVP v3 trial-release increment is available as a reviewed Sprint 4 release with deployed backend evidence, customer-provided server hosting, and public Sprint Review/UAT reporting. The product currently supports registration, vehicle profile setup, manual vehicle-history records, timeline display, statistics, assistant questions about vehicle history/statistics, session persistence, chat history, logout, and security hardening.

The customer estimated handover readiness at approximately **80%**. The reached Week 6 handover level is not yet `Ready for independent use`. The customer confirmation status is `Accepted with follow-up items`.

Expected Week 7 follow-up work:

1. Verify and fix decimal fuel liters in the Android event form.
2. Add kopeck-level monetary precision.
3. Fix odometer-based trip entry workflow.
4. Fix repair and breakdown entry workflow.
5. Complete or document photo attachment as a remaining limitation.
6. Implement rule-based notification recommendations.
7. Investigate and fix older-account data compatibility.
8. Refine AI prompt for more natural conversational tone.
9. Prepare complete source and documentation archives for customer handover.
10. Conduct Week 7 Sprint Review and final transition confirmation.

## 30. Contribution traceability table

| Team member | Issues / PRs / evidence | Contribution area |
|---|---|---|
| @Erusiaaa | [#256](https://github.com/LAMBA-23/LAMBA/pull/256), [#260](https://github.com/LAMBA-23/LAMBA/pull/260), [#248](https://github.com/LAMBA-23/LAMBA/pull/248), [#250](https://github.com/LAMBA-23/LAMBA/pull/250), [#289](https://github.com/LAMBA-23/LAMBA/pull/289), [#258](https://github.com/LAMBA-23/LAMBA/issues/258) | Repair/breakdown records, decimal fuel liters, odometer trips, README update, CONTRIBUTING.md. |
| @mariachizhikova08 | [#257](https://github.com/LAMBA-23/LAMBA/pull/257), [#263](https://github.com/LAMBA-23/LAMBA/pull/263), [#270](https://github.com/LAMBA-23/LAMBA/pull/270), [#274](https://github.com/LAMBA-23/LAMBA/pull/274), [#255](https://github.com/LAMBA-23/LAMBA/issues/255) | UAT documentation, AGENTS.md, decimal fuel Android form, breakdown form with photo, Android CI. |
| @vasilisatumakina29 | [#252](https://github.com/LAMBA-23/LAMBA/pull/252), [#254](https://github.com/LAMBA-23/LAMBA/pull/254), [#266](https://github.com/LAMBA-23/LAMBA/pull/266), [#276](https://github.com/LAMBA-23/LAMBA/pull/276), [#279](https://github.com/LAMBA-23/LAMBA/pull/279), [#283](https://github.com/LAMBA-23/LAMBA/pull/283), [#286](https://github.com/LAMBA-23/LAMBA/pull/286), [#288](https://github.com/LAMBA-23/LAMBA/pull/288) | Sprint planning docs, testing evidence, CORS hardening, logout, local chat history, maintained documentation, changelog, Sprint Review transcript and summary, retrospective, reflection, LLM report. |
| @Elis-bett | [#244](https://github.com/LAMBA-23/LAMBA/pull/244), [#275](https://github.com/LAMBA-23/LAMBA/pull/275), [#280](https://github.com/LAMBA-23/LAMBA/pull/280), [#285](https://github.com/LAMBA-23/LAMBA/pull/285) | Application icon, customer-handover document, responsive statistics UI, README and changelog updates. |
| @vanya630 | [#292](https://github.com/LAMBA-23/LAMBA/issues/292), [#296](https://github.com/LAMBA-23/LAMBA/issues/296) | Week 6 retrospective, reflection, and LLM report course tasks. |

## 31. Embedded screenshots from reports/week6/images/

Screenshots will be added to `reports/week6/images/` after the required evidence is captured.
