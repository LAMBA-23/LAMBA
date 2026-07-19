# Week 7 Report

## 1. Link to Week 6 Report

- [Week 6 Report](../week6/README.md)

## 2. Product Backlog Board

- [Product Backlog — GitHub Projects](https://github.com/orgs/LAMBA-23/projects/1)

## 3. Sprint 5 Backlog Board

- [Sprint 5 Backlog — GitHub Projects](https://github.com/orgs/LAMBA-23/projects/1)

## 4. Sprint 5 Milestone

- [Sprint 5 — Final Transition and Delivery](https://github.com/LAMBA-23/LAMBA/milestone/5)

## 5. Sprint 5 Goal, Dates, and Scope

- **Sprint Goal:** Stabilize MVP v3 after Week 6 customer trial, complete customer-critical fixes, finalize transition documentation, and deliver the final course version.
- **Sprint Dates:** 2026-07-08 — 2026-07-19
- **Scope Summary:** Follow-up maintenance including style parameter fix, documentation updates, customer-handover refinement, retrospective, reflection, and LLM report.

## 6. Total Sprint 5 Size

- **120 Story Points** across 27 closed issues.

## 7. Summary of Week 7 Follow-Up Maintenance and Final MVP v3 Changes

Sprint 5 addressed follow-up items from the Week 6 customer trial:

- Fixed the `style` parameter not being forwarded to `ask_deepseek()` in the chat_ask handler (#353).
- Added dynamic chat style switching with three modes: Friendly, Selfish, Pragmatic (#347).
- Enhanced profile UI with avatar support and local persistence (#351).
- Added vehicle brand and model selection (#349).
- Sorted history by newest events first (#345).
- Updated CHANGELOG, customer-handover, and roadmap documentation.
- Created Sprint Retrospective, Reflection, and LLM Report for Week 7.

## 8. Final Product Access Artifact

- [MVP v3 Release v1.4.0](https://github.com/LAMBA-23/LAMBA/releases/tag/v1.4.0)

## 9. Access and Run Instructions

- [README.md — Setup and Access](../../README.md)
- [Customer Handover — Deployment Instructions](../../docs/customer-handover.md)

## 10. README.md

- [README.md](../../README.md)

## 11. CONTRIBUTING.md

- [CONTRIBUTING.md](../../CONTRIBUTING.md)

## 12. AGENTS.md

- [AGENTS.md](../../AGENTS.md)

## 13. Customer Handover Documentation

- [docs/customer-handover.md](../../docs/customer-handover.md)

## 14. Hosted Documentation Site

- [LAMBA Hosted Documentation](https://lamba-23.github.io/LAMBA/)

## 15. Final Transition Outcome

- **Handover Level:** Ready for independent use
- **Customer Confirmation Status:** Accepted

The customer confirmed acceptance of MVP v3 during the Week 7 meeting. The product is deployed on customer-controlled infrastructure at `http://186.246.27.211:8000`.

## 16. What Was Transferred

- Full source code repository (Android + Backend)
- Docker Compose deployment configuration
- Backend service running on customer server
- PostgreSQL database with all user and vehicle data
- AI integration via Timeweb/DeepSeek
- Complete documentation set (README, CONTRIBUTING, AGENTS, customer-handover, roadmap)

See [docs/customer-handover.md](../../docs/customer-handover.md) for full details.

## 17. Remaining Blockers and Limitations

- No automated deployment pipeline — backend updates require manual SSH access.
- AI responses depend on external Timeweb API availability.
- No production app-store package — only debug APK builds.

## 18. Customer Deployment Evidence

The customer's server at `186.246.27.211` is running the deployed backend. The customer confirmed the product is operational and accepted for independent use.

## 19. Customer Feedback Response Table

| Feedback Point | Response | Status |
|---|---|---|
| Style switching does not work | Fixed `style` parameter forwarding in chat_ask handler (#353) | Resolved |
| Profile UI needs improvement | Added avatar support, card-based style selector (#351) | Resolved |
| Vehicle brand/model selection | Added dropdown lists with popular car brands (#349) | Resolved |
| History ordering | Sorted by newest events first (#345) | Resolved |

## 20. Week 7 UAT Results

The customer executed UAT during the Week 7 meeting. Core features including chat history, session persistence, style switching, and profile management passed validation. See [sprint-review-summary.md](sprint-review-summary.md) for details.

## 21. Final SemVer Release

- [v1.4.0 — MVP v3 Final Version](https://github.com/LAMBA-23/LAMBA/releases/tag/v1.4.0)

## 22. CHANGELOG

- [CHANGELOG.md](../../CHANGELOG.md)

## 23. Public Sanitized Demo Video

- [MVP v3 Demo Video](https://drive.google.com/drive/folders/1JG9VJTtcu_bDr9Q3AGSfieljdY5r3g4y?usp=sharing)

## 24. Demo Day Preparation

The team has completed the required Week 7 lab rehearsal preparation. The slide deck and rehearsed presentation video have been prepared for the Week 7 lab rehearsal and Week 8 Demo Day presentation. The pre-recorded demo video under 2 minutes is included in the public demo video linked above.

## 25. Sprint Review Transcript

- [Sprint Review Transcript](sprint-review-transcript.md)

## 26. Sprint Review Summary

- [Sprint Review Summary](sprint-review-summary.md)

## 27. Reflection

- [Week 7 Reflection](reflection.md)

## 28. Retrospective

- [Week 7 Retrospective](retrospective.md)

## 29. LLM Report

- [Week 7 LLM Report](llm-report.md)

## 30. Final Product Status

MVP v3 is the final course version. The product includes:
- Android application with vehicle profile, chat, history, statistics, and settings
- FastAPI backend with AI integration, event parsing, and expense tracking
- Dynamic chat style switching (Friendly, Selfish, Pragmatic)
- Voice input with Mistral transcription
- Vehicle data export to Excel
- Profile personalization with avatar
- Deployed on customer-controlled infrastructure at `http://186.246.27.211:8000`
- All 168 backend tests passing
- All Android unit tests passing
- CI checks green (backend, Android, link-check)
