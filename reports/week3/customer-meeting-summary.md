# Customer Meeting Summary

**Project:** LAMBA
**Meeting type:** Sprint Review with the Customer
**Date:** 18 june 2026
**Participants / roles:**

* Customer
* Team representatives: frontend, backend, AI/backend integration, product/backlog coordination

## Artifacts Demonstrated or Discussed

The team discussed the current mobile frontend implementation connected to a mock backend. The chat UI was reported as ready. The backend status was reviewed, including the current limitation that authorization is not yet fully connected to real backend verification and still relies on hardcoded/test user data.

The team also reviewed the Product Backlog items planned for the current week, mainly registration, authorization, vehicle registration, vehicle display, and basic chat functionality.

## Scope Reviewed

The customer confirmed that MVP v1 should focus on a functional basic product, not a complete final AI-agent system.

Confirmed MVP v1 scope:

* User registration
* User authorization
* Displaying the authorized user’s vehicle data
* Vehicle registration after user registration
* Basic AI chat communication as a foundation for later AI-agent features

Out of MVP v1 scope for now:

* Email verification
* Vehicle photo upload
* Achievements system
* Full AI statistics analysis
* Full AI memory and long-term dialog context
* Advanced route, repair, fuel, and statistics automation

## Implemented Increment Discussed

The frontend side already has the main screens and sends requests, but the registration and authorization flow is not fully backed by real backend logic yet. The current implementation uses hardcoded/test user data.

The backend needs to support real user registration, authorization, and returning vehicle data for the authorized user. Vehicle registration should include brand, model, and year. The customer confirmed that one user can have one vehicle for the current MVP scope.

The chat UI is already prepared. The customer asked to include a basic chat in MVP v1, using a context prompt and simple communication with the AI service.

## Customer Approvals

The customer approved the following decisions:

* Registration should use only login and password.
* Email verification is not required.
* Vehicle model and brand can be entered manually as text.
* A dropdown database of vehicle brands/models is not required.
* One user having one vehicle is acceptable for MVP v1.

## Requested Changes and Clarifications

The customer requested or clarified:

* Passwords should not be sent or stored as plain raw text in the final implementation. Hashing should be used.
* The app should eventually keep the user logged in.
* The backend must return data belonging to the authorized user.
* Vehicle registration should be included in the current weekly scope.
* Basic chat should be included in MVP v1.
* AI-agent features should later support parsing gas, repairs, routes, statistics, summaries, and clarifying questions.

## Risks and Constraints

Main risks:

* Authorization is currently not fully implemented on the backend.
* Password handling is currently insecure and needs improvement.
* Backend access is limited because it currently works only on the university network.
* AI-agent behavior may become too large for MVP v1 if parsing, memory, statistics, and clarification logic are implemented too early.
* Redirecting from AI chat to pre-filled forms may be useful, but could be too complex for the current sprint.

## Action Points

1. Implement real registration with login and password.
2. Implement real authorization instead of hardcoded user data.
3. Add token-based login persistence if feasible.
4. Implement vehicle registration after user registration.
5. Store vehicle brand, model, and year.
6. Display the authorized user’s vehicle data.
7. Connect backend responses to the correct authorized user.
8. Implement basic AI chat communication with a context prompt.
9. Keep advanced AI-agent parsing and statistics for later backlog refinement.
10. Prepare a video demonstration if external deployment is not ready.
11. Continue using the university VM temporarily.

## Resulting Product Backlog / Scope Changes

The MVP v1 scope was refined during the meeting.

Added or confirmed for MVP v1:

* User registration
* User authorization
* Vehicle registration
* Displaying the user’s vehicle
* Basic AI chat

Deferred beyond MVP v1:

* Vehicle photo upload
* Achievements system
* Advanced AI event parsing
* AI statistics and summaries
* AI memory and continuous long-term dialog
* Redirecting users from chat to pre-filled trip/repair/fuel forms

The highest-priority backlog items for the current sprint should therefore be registration, authorization, vehicle registration/display, and basic chat integration.
