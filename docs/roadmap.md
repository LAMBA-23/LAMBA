# Roadmap

This roadmap is the team's Sprint-by-Sprint delivery plan. It links to the issue tracker for live PBI details and does not duplicate the full user-story index.

Authoritative references:

- User-story registry: [docs/user-stories.md](./user-stories.md)
- Definition of Done: [docs/definition-of-done.md](./definition-of-done.md)
- Live backlog and Sprint execution: [GitHub issues](https://github.com/LAMBA-23/LAMBA/issues)

## Product Goal

Deliver an Android-first vehicle digital twin that lets a vehicle owner keep a useful history of their car, add structured records, and use an AI-assisted chat experience to understand and update that history.

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

## Sprint 2 - Assistant & Statistics

- Sprint milestone: [Sprint 2 - Assistant & Statistics](https://github.com/LAMBA-23/LAMBA/milestone/2)
- Dates: 2026-06-22 to 2026-06-28
- Sprint Goal: Let the user ask questions about their vehicle history and view basic statistics.
- Focus / expected outcome: The AI assistant can answer questions from saved vehicle data, and the user can see fuel/repair expenses and trip statistics.

Planned items:

- [#49 US-06: Ask AI assistant](https://github.com/LAMBA-23/LAMBA/issues/49)
- [#50 US-07: View basic statistics](https://github.com/LAMBA-23/LAMBA/issues/50)

## Sprint 3 - Maintenance Follow-up

- Sprint milestone: [Sprint 3 - Maintenance Follow-up](https://github.com/LAMBA-23/LAMBA/milestone/3)
- Dates: 2026-06-29 to 2026-07-05
- Sprint Goal: Use the recorded vehicle history to provide useful follow-up signals for maintenance and upcoming service needs.
- Focus / expected outcome: The product starts moving from passive record keeping toward actionable maintenance support.

Planned items:

- [#51 US-08: Receive maintenance recommendations](https://github.com/LAMBA-23/LAMBA/issues/51)
- [#52 US-09: Receive notifications](https://github.com/LAMBA-23/LAMBA/issues/52)

## Backlog Notes

- Later candidates not selected for the current Sprint plan: [#53 US-10: Use voice messages](https://github.com/LAMBA-23/LAMBA/issues/53) and [#54 US-11: Attach repair receipts](https://github.com/LAMBA-23/LAMBA/issues/54).
- Not planned for MVP v1: [#55 US-13: Manage multiple vehicles](https://github.com/LAMBA-23/LAMBA/issues/55) and [#57 US-14: Transfer vehicle history to a new owner](https://github.com/LAMBA-23/LAMBA/issues/57).
- Removed requirement preserved for traceability: [#59 US-12: OBD-II integration](https://github.com/LAMBA-23/LAMBA/issues/59).
