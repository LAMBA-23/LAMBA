# Sprint Review Summary

**Project:** LAMBA  
**Meeting type:** Sprint Review and customer-executed UAT  
**Date:** 04.07.2026  
**Participants / public labels:** Customer, Team member  
**Transcript:** [sprint-review-transcript.md](./sprint-review-transcript.md)
**Permission note:** Recording and public publication of the sanitized English transcript were permitted by the customer.

## Sprint Goal Reviewed

The Sprint Review inspected Sprint 3 - Maintenance Follow-up for MVP v2.

Reviewed Sprint Goal: deliver a more useful and reliable vehicle history experience by giving users an alternative manual way to add records, improving AI assistant answers with vehicle statistics, and preparing maintenance follow-up features that help users act on their recorded vehicle data.

Traceability:

- Sprint milestone: [Sprint 3 - Maintenance Follow-up](https://github.com/LAMBA-23/LAMBA/milestone/3)
- Roadmap entry: [docs/roadmap.md](../../docs/roadmap.md#sprint-3-maintenance-follow-up)
- Selected Sprint PBIs discussed: [#160 PBI: Enable AI assistant to use vehicle statistics in responses](https://github.com/LAMBA-23/LAMBA/issues/160), [#161 PBI: Add a manual form for creating vehicle history records](https://github.com/LAMBA-23/LAMBA/issues/161)

## Delivered MVP v2 Increment Discussed

The team demonstrated and discussed the MVP v2 increment with the customer. The reviewed increment included:

- Updated frontend layout direction based on previous feedback.
- Manual form for adding vehicle history records from History.
- Improved statistics behavior and period filters.
- AI assistant answers that can use vehicle statistics.
- Cleaner timeline behavior with reduced extra messages.
- Current architecture documentation explaining the Android client, FastAPI backend, PostgreSQL database, Docker Compose runtime, and external AI integration.

The customer confirmed that the previous feedback was generally addressed, especially the frontend layout direction, manual form, and related user-story direction.

## Customer-Executed UAT Results

| UAT area | Result | Evidence from review |
|---|---|---|
| Manual history record | Passed with usability note | The customer understood the form and liked it, but said the entry point through History plus the plus button is not fully intuitive. The customer accepted this because the manual path is a secondary way to add records. |
| Manual form to statistics transfer | Needs changes | During the review, the team and customer found that data from the manual form may not transfer correctly to statistics. |
| AI statistics answers | Partially passed | The customer confirmed that AI statistics answers work, but formatting should be cleaner and some answers showed wrong or incomplete data. |
| Decimal fuel liters | Needs changes / high priority | The customer explicitly prioritized decimal fuel liters because real refueling often uses uneven liter amounts. |
| Mileage input | Accepted for now | The customer said mileage should stay integer for now because the car odometer uses integer values. |
| Realistic APK test | Follow-up needed | The customer said the next version should be tested in a realistic car-use scenario with the APK, driving, refueling, logging, and review. |

Maintained UAT reference: [docs/user-acceptance-tests.md](../../docs/user-acceptance-tests.md)

## Addressed Customer Feedback

The customer confirmed that several previous review points were addressed:

- Frontend layout moved closer to the requested line-based direction.
- Manual history record creation was implemented.
- Statistics behavior and assistant statistics answers improved compared with the previous version.
- Extra timeline messages were reduced.
- The selected user-story direction matched what was agreed in the previous review.

## Architecture and ADR Discussion

The Sprint Review included an architecture walkthrough. The team explained that:

- Android owns the user interface and sends data through the backend API.
- The FastAPI backend owns validation, business rules, persistence, statistics, chat parsing, and AI integration.
- PostgreSQL stores vehicle history data.
- Docker Compose runs the backend and database for the current deployment model.
- The external AI service is reached through the backend, not directly from Android.
- Vehicle history is saved, while chat history persistence remains future work.

The customer used the architecture discussion to clarify the preferred future chat-to-form workflow: the backend should parse a chat message and return pre-filled form data, then the Android app should let the user confirm or edit the data before saving.

Architecture evidence:

- Architecture entry point: [docs/architecture/README.md](../../docs/architecture/README.md)
- Static view: [docs/architecture/static-view/component-diagram.puml](../../docs/architecture/static-view/component-diagram.puml)
- Dynamic view: [docs/architecture/dynamic-view/chat-event-sequence.puml](../../docs/architecture/dynamic-view/chat-event-sequence.puml)
- Deployment view: [docs/architecture/deployment-view/deployment-diagram.puml](../../docs/architecture/deployment-view/deployment-diagram.puml)
- ADR-001: [Use a backend-owned REST API boundary](../../docs/architecture/adr/001-use-backend-owned-rest-api-boundary.md)
- ADR-002: [Use FastAPI, Pydantic, and SQLAlchemy for backend validation and persistence](../../docs/architecture/adr/002-use-fastapi-pydantic-sqlalchemy-backend.md)
- ADR-003: [Use Docker Compose for backend and database deployment](../../docs/architecture/adr/003-use-docker-compose-for-backend-and-database.md)

## Quality Requirement and CI Evidence

The team discussed continuing automated quality evidence. The customer accepted that the system behavior did not appear manually faked and that backend behavior remains under automated control.

Continuing quality evidence:

- Quality requirements: [docs/quality-requirements.md](../../docs/quality-requirements.md)
- Quality requirement tests: [docs/quality-requirement-tests.md](../../docs/quality-requirement-tests.md)
- Testing strategy and CI reference: [docs/testing.md](../../docs/testing.md)
- Backend CI workflow: [.github/workflows/backend-ci.yml](../../.github/workflows/backend-ci.yml)
- Latest checked Backend CI run at review time: [successful main run on 04.07.2026](https://github.com/LAMBA-23/LAMBA/actions/runs/28714432204)

Quality evidence that must continue into later work:

- QR-001 vehicle event data integrity remains important because incorrect manual-form data can corrupt statistics and future recommendations.
- QR-002 timeline response time remains important because users inspect history and breakdown records through the timeline.
- QR-003 backend regression testability remains important because assistant, event, and statistics behavior are still changing.
- Future implementation of recommendations and notifications should add new measurable quality requirements only when implemented behavior creates a concrete quality risk.

## Remaining Gaps and Risks

The Sprint Review identified these remaining gaps:

- Data from the manual form may not transfer correctly to statistics.
- AI statistics answers need cleaner formatting and more reliable data selection.
- Fuel liters must support decimal values.
- Mileage logging should become more precise through odometer start and end readings, while mileage values remain integers for now.
- "Repair" should be renamed or extended to "Repair/Breakdown."
- Optional photo attachment should be added to the form and timeline later; AI photo analysis is not required.
- Chat should preferably parse user input, return pre-filled form data, and let the user confirm or edit before saving.
- The assistant should answer recent breakdown questions as a dated list.
- App polish is still needed, and the notification bell is not ready.
- Recommendations and notifications should not be left too far behind.
- The assistant should feel less like dry statistics and more like natural car-related dialogue.
- The next version should be tested with the APK in a realistic car-use scenario.

## Product Backlog Adaptation Recommendations

No new PBI issues were created from this review because explicit approval was not given. The following backlog updates are recommended for Product Backlog refinement:

1. Fix decimal fuel liters in manual and chat-created refueling records.
2. Fix manual form data transfer into statistics.
3. Improve AI statistics answer formatting and correctness.
4. Add odometer start/end mileage logging while keeping mileage as integer values.
5. Rename or extend Repair to Repair/Breakdown.
6. Add optional photo attachment to form-created timeline records, without AI photo analysis.
7. Add a chat-to-pre-filled-form confirmation flow.
8. Add recent breakdown answers as a dated list.
9. Continue recommendations and notifications after the core data-entry and statistics gaps are stable.
10. Plan realistic APK UAT for the next version.

Recommended priority order from the customer discussion:

1. Decimal fuel liters.
2. Chat-to-form confirmation flow.
3. Manual form/statistics correctness.
4. Cleaner and more reliable AI statistics answers.
5. Recommendations, notifications, and more natural car-related dialogue.

## Sanitization

The public transcript and summary were sanitized as follows:

- Participant names were replaced with `Customer` and `Team member`.
- Account identifiers and private login details were omitted or generalized.
- Sensitive API/service usage details were redacted or generalized.
- No recording links, credentials, exact personal identifiers, or private contact details are included.
- The transcript preserves the meaning of the review while cleaning filler and repeated wording for readability.
