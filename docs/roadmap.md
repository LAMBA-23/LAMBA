# Roadmap

This roadmap is the team's Sprint-by-Sprint delivery plan. It links to the issue tracker for live PBI details and does not duplicate the full user-story index.

Authoritative references:

- User-story registry: [docs/user-stories.md](./user-stories.md)
- Definition of Done: [docs/definition-of-done.md](./definition-of-done.md)
- Live backlog and Sprint execution: [GitHub issues](https://github.com/LAMBA-23/LAMBA/issues)
- Product Backlog and Sprint Backlog board: [GitHub Project 1](https://github.com/orgs/LAMBA-23/projects/1)

## Product Goal

Deliver an Android-first vehicle digital twin that lets a vehicle owner maintain a trustworthy vehicle profile and history, record trips, fuel, repairs and breakdowns, inspect statistics, and use an AI-assisted chat to understand that history.

The implemented MVP v3 direction is a practical single-vehicle owner experience: structured manual and chat-assisted records, decimal numeric input, odometer-based trips, breakdown photos, profile settings, export, rule-based in-app recommendations, and a more conversational assistant. The current course work is to verify this delivered increment with the customer and complete the remaining course evidence; it does not introduce a post-course product version.

## Sprint 1 - MVP v1 Foundation

- Sprint milestone: [Sprint 1 - MVP v1 Foundation](https://github.com/LAMBA-23/LAMBA/milestone/1)
- Dates: 2026-06-15 to 2026-06-21
- Sprint Goal: Establish the core MVP v1 foundation: user registration, vehicle profile, chat-to-event parsing, onboarding-to-backend connection, and timeline visibility.
- Focus / expected outcome: A user can register, add a vehicle, send chat messages that get parsed into structured events, and view the vehicle timeline. All supporting backend endpoints and Android integrations are in place.

Planned items:

User Stories:
- [#44 US-01: User registration](https://github.com/LAMBA-23/LAMBA/issues/44)
- [#45 US-02: Add a vehicle](https://github.com/LAMBA-23/LAMBA/issues/45)
- [#46 US-03: Send messages](https://github.com/LAMBA-23/LAMBA/issues/46)
- [#47 US-04: Automatically create records](https://github.com/LAMBA-23/LAMBA/issues/47)
- [#48 US-05: View vehicle timeline](https://github.com/LAMBA-23/LAMBA/issues/48)

Supporting PBIs:
- [#67 PBI - Implement vehicle creation endpoint](https://github.com/LAMBA-23/LAMBA/issues/67)
- [#68 PBI - Implement backend registration endpoint](https://github.com/LAMBA-23/LAMBA/issues/68)
- [#69 PBI - Implement chat-to-event parsing baseline](https://github.com/LAMBA-23/LAMBA/issues/69)
- [#70 PBI - Connect Android chat send flow to backend](https://github.com/LAMBA-23/LAMBA/issues/70)
- [#71 PBI - Connect Android onboarding to backend](https://github.com/LAMBA-23/LAMBA/issues/71)

Planning:
- [#64 Create roadmap](https://github.com/LAMBA-23/LAMBA/issues/64)

## Sprint 2 - Chat Event Capture & Assistant & Statistics

- Sprint milestone: [Sprint 2 - Chat Event Capture & Assistant & Statistics](https://github.com/LAMBA-23/LAMBA/milestone/2)
- Dates: 2026-06-22 to 2026-06-28
- Sprint Goal: Enable the vehicle owner to inspect saved vehicle history through a visible timeline, ask the assistant questions about vehicle data, and view basic statistics from recorded events.
- Focus / expected outcome: A vehicle owner can open the vehicle timeline, inspect saved fuel, repair, trip, and issue records, ask the assistant questions about the saved history, and view basic statistics generated from recorded events.

Selected Sprint PBIs:

User Stories:
- [#48 US-05: View vehicle timeline](https://github.com/LAMBA-23/LAMBA/issues/48)
- [#49 US-06: Ask AI assistant](https://github.com/LAMBA-23/LAMBA/issues/49)
- [#50 US-07: View basic statistics](https://github.com/LAMBA-23/LAMBA/issues/50)

Supporting PBIs:
- [#113 PBI - Stabilize GET /events and POST /events API](https://github.com/LAMBA-23/LAMBA/issues/113)
- [#116 PBI - Stabilize events API with unit tests and testing draft](https://github.com/LAMBA-23/LAMBA/issues/116)
- [#118 BBl - Chat redesign](https://github.com/LAMBA-23/LAMBA/issues/118)
- [#120 PBI - Update backend /stats response contract for period-based statistics](https://github.com/LAMBA-23/LAMBA/issues/120)
- [#123 Implement statistics logic end-to-end](https://github.com/LAMBA-23/LAMBA/issues/123)
- [#124 PBI - Implement vehicle timeline UI](https://github.com/LAMBA-23/LAMBA/issues/124)
- [#138 PBI - Implement redesigned Statistics screen](https://github.com/LAMBA-23/LAMBA/issues/138)
- [#140 PBI - Add POST /chat/ask endpoint with DeepSeek connection](https://github.com/LAMBA-23/LAMBA/issues/140)
- [#149 PBI - Finalize events manual add flow and tests](https://github.com/LAMBA-23/LAMBA/issues/149)
- [#154 AI chat does not support knowledge-base questions from the Android app and still uses the old event-only flow](https://github.com/LAMBA-23/LAMBA/issues/154)
- [#158 Сhange network frontend](https://github.com/LAMBA-23/LAMBA/issues/158)

### Continuing Quality and Automation Work

The team will continue improving product quality and delivery reliability during later Sprints. Current follow-up work includes:

- Add or improve automated tests for backend event, assistant, and statistics logic.
- Keep Android-backend integration checks current as the API evolves.
- Improve validation and error handling for incomplete or inconsistent vehicle event data.
- Keep docs/definition-of-done.md, docs/roadmap.md, and maintained product documentation aligned with the current implementation.
- Preserve traceability between user stories, supporting PBIs, pull requests, and verification evidence.

### Scope Justification

The Sprint 2 scope is selected for product value and risk reduction, not for the number of issues. The selected PBIs extend the MVP from basic data capture toward useful vehicle-history inspection and interpretation.

- Customer value: the vehicle owner can inspect saved records, ask questions about vehicle history, and view basic statistics.
- Quality improvement: the selected work requires clearer verification of timeline display, assistant responses, and statistics calculations.
- Risk reduction: the scope focuses on integration points between saved backend data and Android-facing user workflows.
- Done evidence: selected work will be considered complete only when acceptance criteria are satisfied, required review is completed, verification evidence is preserved, and the team Definition of Done is met.

## Sprint 3 - Maintenance Follow-up

- Sprint milestone: [Sprint 3 - Maintenance Follow-up](https://github.com/LAMBA-23/LAMBA/milestone/3)
- Dates: 2026-06-29 to 2026-07-05
- MVP target: MVP v2
- Sprint Goal: Deliver a more useful and reliable vehicle history experience by giving users an alternative manual way to add records, improving AI assistant answers with vehicle statistics, and preparing maintenance follow-up features that help users act on their recorded vehicle data.
- Focus / expected outcome: A vehicle owner can add vehicle history records manually when chat is not convenient, receive AI answers that use available vehicle statistics, and see the foundation for maintenance recommendations and notifications.

Selected Sprint PBIs:

User Stories:
- [#49 US-06: Ask AI assistant](https://github.com/LAMBA-23/LAMBA/issues/49)
- [#51 US-08: Receive maintenance recommendations](https://github.com/LAMBA-23/LAMBA/issues/51)
- [#52 US-09: Receive notifications](https://github.com/LAMBA-23/LAMBA/issues/52)

Supporting PBIs:
- [#160 PBI: Enable AI assistant to use vehicle statistics in responses](https://github.com/LAMBA-23/LAMBA/issues/160)
- [#161 PBI: Add a manual form for creating vehicle history records](https://github.com/LAMBA-23/LAMBA/issues/161)

### Sprint 3 Outcome

The following items were completed and included in the MVP v2 release:

- [#49 US-06: Ask AI assistant](https://github.com/LAMBA-23/LAMBA/issues/49)
- [#160 PBI: Enable AI assistant to use vehicle statistics in responses](https://github.com/LAMBA-23/LAMBA/issues/160)
- [#161 PBI: Add a manual form for creating vehicle history records](https://github.com/LAMBA-23/LAMBA/issues/161)
- [#194 Bug: frontend issues](https://github.com/LAMBA-23/LAMBA/issues/194)
- [#196 PBI: Change buttons on the main screen](https://github.com/LAMBA-23/LAMBA/issues/196)
- [#198 Fix backend statistics mileage and fuel liters](https://github.com/LAMBA-23/LAMBA/issues/198)
- [#199 PBI: Change expenses field to fuel refueling](https://github.com/LAMBA-23/LAMBA/issues/199)
- [#202 PBI: Remove non-event chat messages from timeline](https://github.com/LAMBA-23/LAMBA/issues/202)
- [#204 Fix statistics mileage and fuel liters](https://github.com/LAMBA-23/LAMBA/issues/204)
- [#205 PBI: Backend support for manual vehicle history form](https://github.com/LAMBA-23/LAMBA/issues/205)
- [#208 Implement manual history record creation](https://github.com/LAMBA-23/LAMBA/issues/208)

The following items were not completed in Sprint 3 and moved to Backlog:

- [#51 US-08: Receive maintenance recommendations](https://github.com/LAMBA-23/LAMBA/issues/51) — not started, deferred until the assistant and history flow were stabilized.
- [#52 US-09: Receive notifications](https://github.com/LAMBA-23/LAMBA/issues/52) — not started, depends on #51.

### MVP v2 Scope Justification

The planned MVP v2 scope is selected for customer value, quality improvement, maintainability, and realistic completion against the current Definition of Done.

- Customer value: users get a manual alternative to AI-based record creation and more useful AI answers based on their own vehicle history and statistics.
- Quality improvement: the selected work reduces dependence on the chat-only flow and makes assistant behavior easier to verify against stored vehicle data.
- Maintainability: the Sprint keeps the milestone, Project board, issue metadata, acceptance criteria, reviewers, and roadmap aligned as traceable planning artifacts.
- Done evidence: each selected PBI has acceptance criteria, Story Points, implementer and reviewer information, Work Status, and must be reviewed and verified before being marked Done.

## Sprint 4 - MVP v3 Trial Release and Transition Readiness

- Sprint milestone: [Sprint 4 - Trial Release and Transition Readiness](https://github.com/LAMBA-23/LAMBA/milestone/4)
- Dates: 2026-07-06 to 2026-07-12
- MVP target: MVP v3 trial release
- Sprint Goal: Improve MVP v3 transition readiness by stabilizing customer-facing behavior, closing priority security gaps, and preparing the product for more independent customer use.

### Planned Scope

The milestone description planned customer-agreed readiness work around recommendations and notifications, chat continuity, decimal fuel, AI statistics answers, odometer trips, repair/breakdown records and photo attachments, voice-input removal, icon, session restore, logout, and OWASP hardening. The authoritative execution set is the 13 issues actually assigned to the milestone:

- [#233 Statistics do not update after history form records](https://github.com/LAMBA-23/LAMBA/issues/233)
- [#234 Bug: fix frontend issues](https://github.com/LAMBA-23/LAMBA/issues/234)
- [#238 Bug: unnecessary microphone icon](https://github.com/LAMBA-23/LAMBA/issues/238)
- [#239 Chat expense answers are unstructured and can report incorrect expense data](https://github.com/LAMBA-23/LAMBA/issues/239)
- [#242 PBl: add application icon](https://github.com/LAMBA-23/LAMBA/issues/242)
- [#243 Fix session restore after app restart](https://github.com/LAMBA-23/LAMBA/issues/243)
- [#245 PBI - allow decimal fuel liters in events](https://github.com/LAMBA-23/LAMBA/issues/245)
- [#246 PBI - support trip records by odometer start and end](https://github.com/LAMBA-23/LAMBA/issues/246)
- [#247 PBI - support repair and breakdown event records](https://github.com/LAMBA-23/LAMBA/issues/247)
- [#265 Harden backend auth and rate limiting](https://github.com/LAMBA-23/LAMBA/issues/265)
- [#271 Bug: add logout from Android account](https://github.com/LAMBA-23/LAMBA/issues/271)
- [#277 Save local chat history for the last five dialogs](https://github.com/LAMBA-23/LAMBA/issues/277)
- [#278 Bug: fix frontend issues](https://github.com/LAMBA-23/LAMBA/issues/278)

### Actual Sprint 4 Outcome

All 13 milestone issues are closed. The resulting MVP v3 trial increment fixed the manual-record statistics refresh and other Android defects, removed the unused microphone control, improved structured expense answers, added the launcher icon and session restoration, enabled decimal fuel input, supported odometer-trip and repair/breakdown records, hardened authentication and rate limiting, added logout, retained recent chat dialogs locally, and made Statistics more adaptive. The inspectable trial release is [v1.3.0](https://github.com/LAMBA-23/LAMBA/releases/tag/v1.3.0), published as the Sprint 4 MVP v3 trial release.

## Sprint 5 - Final Transition and Delivery

- Sprint milestone: [Sprint 5 - Final Transition and Delivery](https://github.com/LAMBA-23/LAMBA/milestone/5)
- Dates: 2026-07-13 to 2026-07-19
- MVP target: MVP v3 final delivery increment
- Sprint Goal: Prepare the final course delivery by stabilizing MVP v3 after Sprint 4 feedback, completing realistic APK validation, updating release/deployment evidence, finalizing transition documentation, and deciding what recommendation or notification scope is product-ready.
The [Sprint 5 milestone](https://github.com/LAMBA-23/LAMBA/milestone/5) is the authoritative record of selected PBIs. It contains 27 closed issues. The first follow-up set carried forward from the trial roadmap was [#51](https://github.com/LAMBA-23/LAMBA/issues/51), [#52](https://github.com/LAMBA-23/LAMBA/issues/52), [#264](https://github.com/LAMBA-23/LAMBA/issues/264), [#272](https://github.com/LAMBA-23/LAMBA/issues/272), and [#273](https://github.com/LAMBA-23/LAMBA/issues/273); customer-trial follow-up then refined and expanded the Sprint scope below.

### Selected Sprint 5 PBIs by Direction

Numeric fields:

- [#264 Decimal fuel liters in the event form](https://github.com/LAMBA-23/LAMBA/issues/264), [#313 Decimal values in applicable numeric fields](https://github.com/LAMBA-23/LAMBA/issues/313), and [#314 Decimal values across vehicle and event numeric fields](https://github.com/LAMBA-23/LAMBA/issues/314).

Recommendations and notifications:

- [#51 US-08: Receive maintenance recommendations](https://github.com/LAMBA-23/LAMBA/issues/51), [#52 US-09: Receive notifications](https://github.com/LAMBA-23/LAMBA/issues/52), [#315 Rule-based recommendations endpoint](https://github.com/LAMBA-23/LAMBA/issues/315), [#325 In-app notifications screen](https://github.com/LAMBA-23/LAMBA/issues/325), and [#328 Recommendation localization and recurring unread state](https://github.com/LAMBA-23/LAMBA/issues/328).

Trips and breakdown photos:

- [#272 Odometer start/end trip recording](https://github.com/LAMBA-23/LAMBA/issues/272), [#273 Breakdown records with photo attachment UI](https://github.com/LAMBA-23/LAMBA/issues/273), [#323 End-to-end breakdown photo feature](https://github.com/LAMBA-23/LAMBA/issues/323), [#331 Trip start/end flow](https://github.com/LAMBA-23/LAMBA/issues/331), [#332 Hardened photo storage and authorized access](https://github.com/LAMBA-23/LAMBA/issues/332), [#337 Photo shown in record details](https://github.com/LAMBA-23/LAMBA/issues/337), and [#340 Full breakdown-photo display](https://github.com/LAMBA-23/LAMBA/issues/340).

Profile:

- [#329 Account profile and vehicle settings](https://github.com/LAMBA-23/LAMBA/issues/329), [#349 Vehicle brand and model selection](https://github.com/LAMBA-23/LAMBA/issues/349), and [#351 Profile UI and local avatar persistence](https://github.com/LAMBA-23/LAMBA/issues/351).

Export:

- [#339 Export vehicle data to Excel](https://github.com/LAMBA-23/LAMBA/issues/339) and [#356 Fix export after UI refactoring](https://github.com/LAMBA-23/LAMBA/issues/356).

Chat improvements:

- [#316 Humanize AI assistant responses](https://github.com/LAMBA-23/LAMBA/issues/316), [#341 Digital-friend persona and markdown-free chat](https://github.com/LAMBA-23/LAMBA/issues/341), [#347 Dynamic AI chat style selector](https://github.com/LAMBA-23/LAMBA/issues/347), and [#353 Pass chat style to AI](https://github.com/LAMBA-23/LAMBA/issues/353).

Usability and bug fixes:

- [#321 Manual-history and notification fixes](https://github.com/LAMBA-23/LAMBA/issues/321), [#326 Trip, notification-page, and attachment UX fixes](https://github.com/LAMBA-23/LAMBA/issues/326), and [#345 Sort History newest first](https://github.com/LAMBA-23/LAMBA/issues/345).

### Actual Sprint 5 Outcome

All 27 issues assigned to the closed milestone are closed. Twenty-three of them have a direct closing pull-request reference; the changes are present on the current `main` tip, including the final export fix in merged [PR #357](https://github.com/LAMBA-23/LAMBA/pull/357). The product outcome is decimal input support, rule-based in-app recommendations and notification inbox, usable odometer-trip and secured breakdown-photo flows, account/profile improvements, Excel export, more natural configurable chat responses, and targeted usability fixes. The repository also has a published [v1.4.0 MVP v3 release](https://github.com/LAMBA-23/LAMBA/releases/tag/v1.4.0); this release artifact does not by itself establish final customer acceptance or handover completion.

## Current MVP v3 Status and Remaining Course Work

Current `main` contains the merged Sprint 5 changes and matches `origin/main` at the time this roadmap was updated. MVP v3 now provides registration and session handling, vehicle setup and settings, manual and chat-assisted history records, timeline and statistics, decimal values, odometer trips, repair/breakdown photos, local chat continuity, recommendations and notifications, profile personalization, vehicle-data export, and configurable assistant style.

The remaining work is course-completion evidence, not another product version: execute and record the Week 7 customer review/UAT and final transition confirmation; prepare the Week 7 report; and complete the agreed final handover archive and confirmation. These are deliberately listed as remaining because the repository has no `reports/week7/` artifact, the Week 7 UAT fields remain unexecuted, and the existing handover document says the comprehensive archive has not yet been delivered. No post-course roadmap is planned here.

## Continuing Architecture, Quality, and Process Work

Assignment 6 requires maintained project assets that must stay current as the product evolves:

- Document the current architecture in `docs/architecture/`, including static, dynamic, and deployment views.
- Record architecture decisions in `docs/architecture/adr/` and link them to relevant quality requirements.
- Maintain `docs/development-process.md` as the development process and configuration-management reference.
- Keep `docs/testing.md`, `docs/quality-requirements.md`, `docs/quality-requirement-tests.md`, and `docs/definition-of-done.md` aligned with MVP v3 and later maintained documentation changes.
- Preserve CI, testing, review, release, and hosted documentation evidence for Assignment 6.

## Backlog Notes

- Later candidates not selected for the current Sprint plan: [#53 US-10: Use voice messages](https://github.com/LAMBA-23/LAMBA/issues/53) and [#54 US-11: Attach repair receipts](https://github.com/LAMBA-23/LAMBA/issues/54).
- Not planned for MVP v1: [#55 US-13: Manage multiple vehicles](https://github.com/LAMBA-23/LAMBA/issues/55) and [#57 US-14: Transfer vehicle history to a new owner](https://github.com/LAMBA-23/LAMBA/issues/57).
- Removed requirement preserved for traceability: [#59 US-12: OBD-II integration](https://github.com/LAMBA-23/LAMBA/issues/59).
