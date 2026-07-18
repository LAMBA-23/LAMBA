# Week 7 Sprint Review, Customer UAT, and Final Transition Summary

**Meeting date:** 2026-07-18

## Participants

- Team representative
- Customer

## Meeting scope

The meeting combined:

- the Sprint 5 / Week 7 Sprint Review;
- demonstration of the delivered MVP v3 increment;
- customer-executed UAT of customer-critical Week 7 functionality;
- review of resolved Week 6 follow-up items;
- final customer acceptance and transition-status confirmation;
- confirmation of the remaining archive-delivery actions.

## Sprint 5 goal and delivered MVP v3

The Sprint 5 goal was to stabilize MVP v3 after the Week 6 customer trial, complete customer-critical fixes, finalize realistic product use and transition evidence, and prepare the final course delivery.

The demonstrated MVP v3 included:

- decimal values for fuel, expenses, and other applicable numeric fields;
- newest-first History ordering;
- trip recording using start and end odometer values;
- breakdown records with persistent, authorized photo storage and full-image display;
- rule-based recommendations and an in-app notification screen;
- more natural AI responses written from the vehicle's perspective;
- selectable assistant communication styles;
- voice input with editable recognised text;
- account profile, password handling, logout, and local avatar persistence;
- vehicle brand and model selection;
- vehicle-data export to an Excel workbook;
- application-styled logout confirmation;
- backend operation on customer-controlled infrastructure.

Relevant evidence:

- [Sprint 5 milestone](https://github.com/LAMBA-23/LAMBA/milestone/5)
- [MVP v3 release v1.4.0](https://github.com/LAMBA-23/LAMBA/releases/tag/v1.4.0)
- [Roadmap](../../docs/roadmap.md)
- [Customer handover](../../docs/customer-handover.md)
- [User acceptance tests](../../docs/user-acceptance-tests.md)

## Resolved Week 6 follow-up items

| Week 6 gap | Week 7 outcome | Traceability |
|---|---|---|
| Decimal fuel and monetary values were incomplete | Decimal litres and ruble values were entered, saved, and visible in Statistics | [#264](https://github.com/LAMBA-23/LAMBA/issues/264), [#313](https://github.com/LAMBA-23/LAMBA/issues/313), [#314](https://github.com/LAMBA-23/LAMBA/issues/314) |
| Odometer-based trips were incomplete | The customer started and completed a trip using odometer values | [#272](https://github.com/LAMBA-23/LAMBA/issues/272), [#331](https://github.com/LAMBA-23/LAMBA/issues/331) |
| Breakdown photos were incomplete | A photo was attached, persisted, loaded, and displayed without cropping | [#273](https://github.com/LAMBA-23/LAMBA/issues/273), [#323](https://github.com/LAMBA-23/LAMBA/issues/323), [#332](https://github.com/LAMBA-23/LAMBA/issues/332), [#337](https://github.com/LAMBA-23/LAMBA/issues/337), [#340](https://github.com/LAMBA-23/LAMBA/issues/340) |
| Recommendations were unfinished | Rule-based recommendations were shown on a separate notification screen | [#51](https://github.com/LAMBA-23/LAMBA/issues/51), [#52](https://github.com/LAMBA-23/LAMBA/issues/52), [#315](https://github.com/LAMBA-23/LAMBA/issues/315), [#325](https://github.com/LAMBA-23/LAMBA/issues/325), [#328](https://github.com/LAMBA-23/LAMBA/issues/328) |
| AI responses were too dry | The customer verified a more natural vehicle persona and a clearly distinguishable communication style | [#316](https://github.com/LAMBA-23/LAMBA/issues/316), [#341](https://github.com/LAMBA-23/LAMBA/issues/341), [#347](https://github.com/LAMBA-23/LAMBA/issues/347), [#353](https://github.com/LAMBA-23/LAMBA/issues/353) |
| Additional account and handover usability was needed | Profile, password error handling, logout dialog, and Excel export were demonstrated | [#329](https://github.com/LAMBA-23/LAMBA/issues/329), [#339](https://github.com/LAMBA-23/LAMBA/issues/339), [#351](https://github.com/LAMBA-23/LAMBA/issues/351), [#356](https://github.com/LAMBA-23/LAMBA/issues/356) |

## Customer-executed UAT results

The customer stated at the end of the meeting that the tested UAT scenarios were successfully completed. This statement applies to the scenarios actually exercised during the meeting; it does not mean that every active scenario in `docs/user-acceptance-tests.md` was re-executed.

| Scenario | Result | Evidence and observations |
|---|---|---|
| UAT-005 — Manually add a vehicle history record | Passed | A decimal fuel record was created, saved, and checked in Statistics. |
| UAT-006 — Ask the AI assistant about vehicle statistics and context | Passed | The customer verified a natural vehicle persona and a clearly distinguishable selfish communication style. |
| UAT-007 — Receive rule-based recommendations as notifications | Passed | The customer opened the recommendation screen and saw a recommendation produced from the newly created breakdown. |
| UAT-009 — Decimal fuel and monetary values | Passed | The customer entered `5.6` litres and `1288.1` rubles and confirmed that the values were saved. |
| UAT-010 — Record a trip using start and end odometer values | Passed with non-blocking feedback | The trip was started and completed successfully. The customer suggested a button that inserts the current mileage automatically. |
| UAT-011 — Repair/Breakdown record with photo attachment | Passed with minor UX feedback | A breakdown photo was attached and displayed. The customer noted that the loading state was not obvious while the photo was being uploaded or loaded. |
| UAT-013 — Manage account and vehicle settings | Partially executed / passed for tested steps | The customer opened Profile, reviewed local avatar behaviour, verified incorrect-password handling, and reviewed logout. Correct-password change and all vehicle-editing steps were not demonstrated in the supplied transcript. |
| UAT-014 — Dictate an editable chat message | Passed | Voice input recognised a refuelling message and the resulting record was saved. |
| New scenario required — Export vehicle data to Excel | Passed | The customer exported the workbook and confirmed that the feature is useful for sharing vehicle history with a repair shop. A maintained UAT scenario should be added for this feature. |

## Customer feedback and backlog decisions

| Feedback | Decision |
|---|---|
| Provide the final archive through private cloud storage | Required transition action. The archive had not yet been sent at the time of the meeting. |

## Final transition outcome

The customer explicitly confirmed:

- **Customer-confirmation status:** `Accepted`
- **Reached handover level:** `Deployed or operated on customer side`

Supporting facts:

- the backend was already running on infrastructure owned and controlled by the customer;
- the customer accepted the current MVP v3 result;
- the customer accepted the arrangement under which the team would stop managing the server;
- the customer confirmed that the tested UAT scenarios were successful.

The stronger status `Independently used by customer` is not claimed because the supplied meeting evidence shows guided customer testing rather than established independent everyday use.

## Handover package status

At the time of the meeting:

- the customer accepted MVP v3;
- the server-side operational transition was confirmed;
- the final archive **not yet delivered**;
- the team stated that the documentation would be updated and the archive would be sent later through private cloud storage.

Therefore, repository documents must not state that the archive package was already delivered until the transfer has actually occurred and inspectable private evidence exists.

## Remaining risks and limitations

- Photo upload works, but the loading state is not sufficiently visible.
- Repair records do not currently support the same photo-attachment flow demonstrated for breakdown records.
- The trip-start flow does not automatically insert the current mileage.
- Android distribution remains a build or APK delivery rather than a public app-store release.
- AI and voice functionality depend on external service credentials and availability.
- Archive delivery and checksum verification remain outstanding transition actions after the recorded meeting.
- Public transcript publication permission remains unconfirmed in the supplied transcript.

## Decisions and approvals

1. The customer accepted MVP v3.
2. The customer accepted operation on customer-controlled infrastructure and the team's withdrawal from server management.
3. The final archive should be delivered through private cloud storage.
4. The archive must be accompanied by a SHA-256 checksum.
5. Minor usability suggestions do not block final acceptance.
6. The handover status should distinguish operational acceptance from the still-pending archive transfer.

## Action points

1. Update `docs/user-acceptance-tests.md` with the Week 7 execution results and add a maintained Excel-export UAT scenario.
2. Update `reports/week7/README.md` with the UAT result summary, final transition status, Sprint Review links, and remaining actions.
3. Correct `docs/customer-handover.md` so it does not claim that the archive was delivered before the actual transfer.
4. Build the agreed archive package, calculate its SHA-256 checksum, and deliver both through the agreed private channel.
5. Preserve private delivery evidence and include it only in the Week 7 private submission wrapper.
6. Confirm public transcript publication permission before committing the transcript publicly.
7. Record the repair-photo, current-mileage, and photo-loading feedback as explicit backlog decisions or post-course limitations.