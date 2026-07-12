# Week 6 Sprint Review and Transition-Readiness Summary
 
**Meeting date:** 2026-07-10

## Participants

- Team representative
- Customer

## Meeting scope

The meeting combined:

- the Sprint 4 / Week 6 Sprint Review;
- customer trial and customer-executed UAT;
- transition-readiness discussion;
- review of the customer-facing documentation and preferred handover format.

## Recording and publication permissions

- **Recording permission:** Granted at the beginning of the meeting.

## Sprint 4 goal and Week 6 trial release

The team described the Sprint 4 goal as completing and stabilizing the main application workflows for a customer trial and handover-readiness review.

The demonstrated trial build included:

- events entered through forms being reflected in Statistics;
- improved frontend layout and adaptation to different screen sizes;
- improved formatting of AI-generated statistics responses;
- local storage of the five most recent chats;
- session persistence after closing and reopening the application;
- explicit logout and local chat-history clearing;
- password hashing;
- removal of the demo account;
- request-rate protections;
- application icon;
- deployment on the server provided by the customer and access outside the university network.

The customer confirmed that the demonstrated chat history and session-persistence behaviour worked during the meeting.

## Customer trial and UAT results

| Area | Result | Evidence from the meeting |
|---|---|---|
| Existing chat history can be opened and continued | Passed | The customer opened the chat history and confirmed that it was visible and usable. |
| Session remains active after closing and reopening the application | Passed | The application returned to the previously authenticated account. |
| Statistics update after adding events through chat | Partially passed | The event count and Statistics changed, but the corresponding items did not appear correctly in History for the customer's older account. |
| Fractional fuel volume | Failed / incomplete | A fractional value could be entered but was not saved correctly. |
| Fractional monetary values | New requirement / gap | The customer explicitly requested kopeck-level monetary precision. |
| Trip entry using starting and ending odometer values | Failed / incomplete | The workflow was reported as not working correctly. |
| Repair and breakdown entry | Failed / incomplete | The workflow was reported as not working correctly. |
| Photo attachment | Not available | The feature was identified as unfinished. |
| AI statistics response formatting | Demonstrated | The customer reacted positively to the updated output. |
| Responsive Statistics UI | Demonstrated | The team reported that adaptation issues on different devices had been fixed. |

The customer estimated the product's current handover readiness at approximately **80%**.

## Transition-readiness findings

The product was **not yet ready for full independent use** at the time of the meeting.

At the same time:

- the backend and product services were already deployed on infrastructure provided and controlled by the customer;
- the customer had not yet started regular independent use because the product had not been finally handed over;
- the customer stated that the intended everyday use is vehicle bookkeeping: trips, refuellings, expenses, and AI-assisted interaction;
- the customer confirmed that the product should be ready for use after the remaining Week 7 work is completed.

The reached Week 6 status is best described as:

- **Handover level:** not yet `Ready for independent use`;
- **Customer confirmation:** `Accepted with follow-up items` for the trial direction, not final acceptance of MVP v3.

Final handover status must be confirmed again during Week 7.

## Customer-facing documentation review

The customer requested the complete documentation set, not only basic usage instructions.

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

## Decisions

1. Rule-based recommendations should be delivered through notifications rather than proactive chat messages.
2. A separate account page is not required within the remaining course scope.
3. AI responses should be made more natural and conversational while preserving the car-assistant concept.
4. The final handover should include complete source archives and the full documentation set.
5. The customer does not need administrator access to the repository.
6. The product should not be made publicly available solely for customer use.

## Required Sprint 5 follow-up

| Feedback or gap | Planned response | Traceability |
|---|---|---|
| Fractional fuel volume is not saved correctly | Support and verify fractional litre values | Add or link Sprint 5 PBI |
| Monetary values need kopeck precision | Support and verify fractional monetary values | Add or link Sprint 5 PBI |
| Odometer-based trip entry is incomplete | Repair and verify the workflow | Add or link Sprint 5 PBI |
| Repair and breakdown entry is incomplete | Repair and verify the workflow | Add or link Sprint 5 PBI |
| Photo attachments are unavailable | Complete or explicitly document as a remaining limitation | Add or link Sprint 5 PBI |
| Recommendations are unfinished | Implement rule-based notification recommendations | Add or link Sprint 5 PBI |
| Customer's older account does not display some new events correctly | Investigate account-data compatibility; reset or migrate the account if necessary | Add or link Sprint 5 PBI |
| AI responses are too dry | Refine the prompt to produce a more natural conversational tone | Add or link Sprint 5 PBI |
| Handover documentation must support a non-technical reader | Expand usage, deployment, configuration, recovery, and troubleshooting guidance | Link documentation PBI |
| Customer wants complete source and documentation archives | Prepare a complete handover package accessible to the customer | Link transition PBI |

## Risks and limitations

- Several customer-critical data-entry workflows were incomplete during the trial.
- The customer's existing account may contain data that is not fully compatible with the current version.
- The customer had not yet independently used the product in normal daily operation.
- Final acceptance and the final handover level were not confirmed because MVP v3 had not yet been delivered.

## Action points

1. Convert every material customer feedback point into a linked Sprint 5 PBI or explicit transition action.
2. Complete and verify the remaining customer-critical workflows.
3. Update `docs/customer-handover.md` and the broader customer-facing documentation based on the request for detailed non-technical instructions.
4. Prepare complete source and documentation archives for final delivery.
5. Obtain separate customer permission for public transcript publication or, if publication is refused, for private instructor sharing.
6. Conduct the Week 7 Sprint Review and final transition-confirmation meeting after MVP v3 is delivered.