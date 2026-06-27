# Customer Review Summary

**Project:** LAMBA  
**Meeting type:** Sprint Review and UAT with the Customer  
**Date:** 27.06.2026  
**Participants / roles:**

* Customer: Nikita
* Team representatives: interviewer, frontend, backend, AI/backend integration, quality/testing, and product/backlog coordination

## Sprint Goal Reviewed

The reviewed Sprint Goal was Sprint 2: enable the vehicle owner to inspect saved vehicle history through a visible timeline, ask the assistant questions about vehicle data, and view basic statistics from recorded events.

The customer review focused on whether the current increment is a useful step toward the product goal and whether the customer can complete the main MVP workflows: registration, vehicle setup, AI assistant use, history entry, and statistics review.

## Delivered Increment Discussed

The team demonstrated the current Android application increment for LAMBA. The discussed increment included:

* User registration and login flow.
* Vehicle creation after registration, including brand/model, production year, and mileage.
* User-specific vehicle information displayed in the app.
* AI assistant entry point and chat interaction connected to the selected vehicle.
* Maintenance or expense record creation and history display.
* Basic statistics screen with time filtering and expense categories.
* Backend validation and quality-requirement tests for event integrity, timeline response time, and backend regression checks.

The customer confirmed that the increment is a clear improvement compared with the previous week and is useful progress toward the project goal.

## UAT Results

| UAT scenario | Result | Customer feedback |
|---|---|---|
| Registration and adding a vehicle | Passed | Registration was easy, fields were clear, and adding the vehicle caused no confusion. The customer noticed an existing-account case but completed the flow with another email. |
| AI assistant | Partially passed | The assistant was easy to find and showed the car mark, but the customer did not receive the needed information and said chat handling still needs refinement. |
| Adding records to history | Passed with requested improvement | Adding a record was clear, and the record was reflected in history. The customer requested the option to add records manually. |
| Viewing statistics | Partially passed | The statistics and time filter were generally clear, but labels were hard to read or did not fit well. The customer also wants statistics to feel more alive with real entered data. |

## Quality Evidence Discussed

The team reviewed the agreed quality requirements with the customer.

* **Vehicle event data integrity:** invalid event requests with unsupported event types, empty descriptions, negative values, negative mileage, or missing/unknown user IDs must be rejected and not saved. The customer confirmed this behavior is acceptable.
* **Timeline response time:** the vehicle timeline endpoint should return a successful response within two seconds under normal operation with the demo dataset. The customer confirmed the timeline behavior was acceptable.
* **Backend regression testability:** the team explained that automated backend tests are run on GitHub when developers change code. The customer asked whether the tests cover both frontend and backend; the team clarified that the current automated tests cover the backend only.

The customer accepted the described quality requirements, while also noting that the quality scope still feels minimal and should be expanded as the product matures.

## Feedback

The customer gave the following main feedback:

* Registration and authorization work well.
* Vehicle creation works well, and the entered information is reflected in the app.
* The previous feedback about sidebar, statistics, history, and related screens was generally processed correctly.
* The AI chat exists and feels like it has real AI behind it, but the interaction is still unclear and needs better answers.
* Statistics are generally understandable, but labels need visual improvement.
* Statistics should become more meaningful when real user data is entered.
* Main-screen square prompt blocks are confusing because they look like navigation elements, but actually send template messages to the AI chat.
* Manual record entry would be useful.

## Approvals and Requested Changes

Approved or accepted:

* The registration flow is clear enough for the MVP.
* Vehicle creation and display are acceptable.
* History and statistics are present as agreed in the previous meeting.
* The Sprint increment is a productive step forward.
* The described event-validation and timeline-performance quality requirements are acceptable for the current stage.

Requested changes:

* Refine the AI assistant interaction and improve the correctness/usefulness of chat answers.
* Add or improve manual record creation.
* Improve statistics label readability and layout.
* Make statistics use real entered data so they feel more informative.
* Redesign or clarify the main-screen prompt blocks so users understand they send messages to the chat.
* Expand quality coverage later, including frontend tests in addition to backend tests.

## Risks

* **AI assistant risk:** the highest remaining product risk is unclear or insufficient AI chat handling.
* **Usability risk:** statistics labels and main-screen chat prompt blocks may confuse users.
* **Data usefulness risk:** statistics may not demonstrate value until realistic event data is entered and displayed.
* **Testing risk:** current automated quality evidence is backend-focused; frontend behavior still needs stronger automated or documented verification.
* **Security risk:** password hashing and stronger authorization remain backlog concerns from earlier feedback and should be completed before production-like use.

## Action Points

1. Prioritize AI assistant refinement in the next backlog planning session.
2. Improve AI answers and chat handling for vehicle-specific questions.
3. Add or refine manual record creation for maintenance and expense history.
4. Improve statistics labels so they fit and are easier to understand.
5. Ensure statistics are calculated from real user-entered records where possible.
6. Rework the main-screen prompt blocks so their chat behavior is visually clear.
7. Add frontend or integration quality evidence when feasible.
8. Keep password hashing and token-based authorization in the backlog as security hardening work.

## Resulting Product Backlog or Scope Changes

The review confirmed that the next backlog focus should shift from basic MVP structure toward refinement and product usefulness.

Added or raised in priority:

* Refine AI assistant answers and interaction flow.
* Clarify main-screen chat prompt behavior.
* Improve statistics label layout and readability.
* Make statistics reflect real recorded data more clearly.
* Add manual record entry or make the manual path more visible.
* Add frontend/integration quality evidence beyond backend automated tests.

Still accepted as completed or mostly completed for the current increment:

* Registration and authorization baseline.
* Vehicle creation after registration.
* Displaying user vehicle data.
* History screen availability.
* Basic statistics availability.
* Backend quality checks for invalid event rejection, timeline response time, and regression testability.

Deferred or continuing risks:

* Full AI assistant maturity and richer vehicle-data answers.
* Security hardening, including password hashing and stronger authorization.
* Broader automated frontend coverage.
