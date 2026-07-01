# Week 5 Report

## Part 2: Respond to Customer Feedback on MVP v1

This section records the team's response to customer feedback on MVP v1 and later feedback already received. It follows the Process Requirements for reusable feedback traceability: every material feedback point is linked to a PBI, issue, roadmap item, or explicit decision record, and every response uses a clear status.

## Customer Feedback Response Table

| Feedback point | Resulting PBI or issue | Status | Response |
|---|---|---|---|
| Passwords should not remain plain raw text in the final implementation; hashing is expected. | Security hardening PBI to be created or refined for password hashing and credential handling. | Added to the backlog for later | The feedback is accepted as a product and quality risk. It was not selected for the current Sprint because MVP v2 scope prioritized stabilizing the delivered flow, evidence, and architecture risks first. |
| Backend responses must return data for the authorized or selected user instead of shared demo data. | [#44](https://github.com/LAMBA-23/LAMBA/issues/44), [#67](https://github.com/LAMBA-23/LAMBA/issues/67), [#68](https://github.com/LAMBA-23/LAMBA/issues/68), [#71](https://github.com/LAMBA-23/LAMBA/issues/71) | Partially addressed | User-specific registration, onboarding, vehicle, and event flows were introduced. Full token-based authorization remains a separate security backlog item because user-specific access is useful for MVP but not sufficient for production-like security. |
| The app should eventually keep the user logged in. | [#71](https://github.com/LAMBA-23/LAMBA/issues/71) | Addressed in the Sprint | Android session persistence was added so the app can continue user-specific flows after login. |
| Vehicle registration should be included after user registration. | [#45](https://github.com/LAMBA-23/LAMBA/issues/45), [#67](https://github.com/LAMBA-23/LAMBA/issues/67), [#71](https://github.com/LAMBA-23/LAMBA/issues/71) | Addressed in the Sprint | The onboarding flow now supports vehicle setup after user registration, including backend vehicle creation or update. |
| Vehicle brand, model, production year, and current mileage should be stored. | [#45](https://github.com/LAMBA-23/LAMBA/issues/45), [#67](https://github.com/LAMBA-23/LAMBA/issues/67) | Addressed in the Sprint | The vehicle profile stores the required vehicle fields with validation in the Android and backend flow. |
| Basic chat should be included in MVP v1 as a foundation for later AI-agent behavior. | [#46](https://github.com/LAMBA-23/LAMBA/issues/46), [#69](https://github.com/LAMBA-23/LAMBA/issues/69), [#70](https://github.com/LAMBA-23/LAMBA/issues/70) | Addressed in the Sprint | The chat flow supports non-empty user messages, backend parsing, assistant responses, and persistence of supported vehicle events. |
| If the AI cannot confidently interpret a message, it should ask clarification questions and feel like a continuous dialog. | [#69](https://github.com/LAMBA-23/LAMBA/issues/69), [#70](https://github.com/LAMBA-23/LAMBA/issues/70) | Partially addressed | Parser guardrails and clarification responses were added for ambiguous or unsupported vehicle messages. Full long-term dialog context remains outside the current Sprint scope. |
| AI-agent features should later support parsing fuel, repairs, routes, statistics, summaries, and clarifying questions. | [#47](https://github.com/LAMBA-23/LAMBA/issues/47), [#49](https://github.com/LAMBA-23/LAMBA/issues/49), [#50](https://github.com/LAMBA-23/LAMBA/issues/50), [#69](https://github.com/LAMBA-23/LAMBA/issues/69) | Partially addressed | Basic event parsing and clarification were implemented. Statistics, summaries, and richer assistant answers remain planned through later AI-assistant and statistics PBIs. |
| Redirecting from AI chat to pre-filled trip, repair, or fuel forms may be useful. | Chat-to-timeline interaction PBI to be created or refined after the timeline flow is stable. | Rejected or deferred with rationale | Deferred because this depends on a stable timeline and event interaction design. Implementing it before the baseline event flow is stable would increase scope and usability risk. |
| Vehicle photo upload could be added later. | Optional vehicle photo upload PBI to be created only if the customer confirms priority. | Added to the backlog for later | The feedback is documented for future reconsideration, but it was not selected because the customer treated it as future scope rather than MVP v1 or MVP v2 core value. |
| Achievements could be added later. | Optional achievements PBI to be created only if the customer confirms priority. | Added to the backlog for later | The feedback is documented as possible future engagement scope, but it was lower priority than product flow, quality, deployment, and architecture risks. |
| The customer needs a way to inspect the product despite university-network deployment limits. | Release and deployment evidence for the Sprint increment; video demonstration as fallback evidence. | Partially addressed | A video demonstration was used as a fallback when deployment access was limited. MVP v2 planning keeps runnable access and release evidence as delivery-risk work. |

## Sprint 3 Backlog Decisions

Feedback selected for Sprint 3 should be represented by PBIs with acceptance criteria, estimate, owner, reviewer, and Sprint milestone assignment. Feedback not selected for Sprint 3 remains traceable through the decision records above and should be converted into GitHub issues when the team decides to make it actionable.

The team should not measure Sprint progress only by the number of completed issues. Sprint 3 scope must be justified by stakeholder value, quality improvement, risk reduction, and evidence that selected work is Done.

## Deferred or Partially Addressed Feedback

| Feedback point | Decision | Rationale |
|---|---|---|
| Password hashing and credential hardening | Add to backlog for later | Required before production-like use, but lower immediate Sprint value than stabilizing MVP v2 flow and evidence. |
| Full token-based authorization | Add to backlog for later | Current `user_id` based behavior supports MVP demonstration but does not fully address secure authorization. |
| Full AI memory, summaries, and long-term dialog context | Defer with rationale | These features require a stable data model, event history, and statistics layer before they can be implemented reliably. |
| Pre-filled form redirect or created-item timeline link from AI chat | Defer with rationale | This should follow a stable timeline and event interaction flow to avoid premature UX complexity. |
| Vehicle photo upload | Add to backlog for later | Customer described it as future scope, not essential MVP value. |
| Achievements | Add to backlog for later | Customer described it as future scope and it does not reduce current product, quality, or deployment risk. |

MVP v2 must address some customer feedback from MVP v1 unless the team documents why higher-priority product, quality, deployment, or architecture risks made a different Sprint scope more valuable. For this Sprint, the selected scope is justified by customer value in the core registration, vehicle, chat, and event-recording flow, plus risk reduction around quality evidence and delivery access.
