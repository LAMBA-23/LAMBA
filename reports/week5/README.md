# Week 5 Report

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
