# MVP v0 Report

## Purpose and Foundation Description

MVP v0 is the technical foundation used to demonstrate that the core LAMBA workflow is feasible before full MVP v1 implementation. Its purpose is to validate the end-to-end path `Android -> FastAPI -> PostgreSQL` and prove that the team can run a minimal but working vehicle-history flow.

At this stage, the system is not presented as a complete product. Instead, MVP v0 confirms that the selected stack, repository structure, API contract, demo data, and basic UI screens are sufficient to support the next implementation step.

Validated foundation in MVP v0:

- Android client screens for login, chat, history, and statistics
- FastAPI backend with demo endpoints
- PostgreSQL persistence started through Docker Compose
- Seeded demo user, demo car, and repeatable smoke-check flow
- API contract for login, vehicle retrieval, event creation, history, and statistics

## Deployment URL or Runnable Artifact

- Public deployment URL: no public deployed MVP URL is documented in the repository as of June 14, 2026
- Runnable artifact: [docker-compose.yml](../../docker-compose.yml)
- Local backend entry point after startup: [http://localhost:8000](http://localhost:8000)
- Local Swagger UI: [http://localhost:8000/docs](http://localhost:8000/docs)

## Public Video Demonstration Link

- Public video demonstration: https://drive.google.com/drive/folders/1e6Er7lzkrpa83KdF2lnWaJwzvdv-bmwR?usp=sharing

## Relationship to the Prototype and Proposed MVP v1 Stories

MVP v0 is the implementation foundation behind the Week 2 prototype artifacts. The prototype shows the intended user flow, while MVP v0 verifies that the core parts of this flow can already be executed with local backend support and demo data.

Prototype alignment:

- Login and onboarding flow are represented by the published UI screenshots
- Chat remains the main interaction concept
- Timeline/history is treated as the central vehicle record view
- Statistics are already represented as a visible follow-up screen

Relevant prototype artifacts:

- [Week 2 repository index](./README.md)
- [Login screen](./images/login-screen.png)
- [Chat screen](./images/Chat-screen.png)
- [Timeline screen](./images/timeline-screen.png)
- [Statistics screen](./images/statistics-screen.png)

Relationship to the initial proposed MVP v1 stories from [user-stories.md](./user-stories.md):

- `US-01` User registration/login: partially supported through demo login foundation via `POST /auth/login`
- `US-02` Add a vehicle: partially supported through the seeded single-vehicle model and vehicle endpoint foundation
- `US-03` Send messages: supported at the interface/foundation level through the Android chat screen and backend integration path
- `US-04` Automatically create records: partially supported through structured event creation with `POST /events`
- `US-05` View vehicle timeline: supported through the history flow backed by `GET /events`

MVP v0 also gives early support for `US-07` View basic statistics through `GET /stats`, even though `US-07` is outside the initial MVP v1 Must Have subset listed in the Week 2 scope.

## Current Limitations, Placeholders, and Mocks

- MVP v0 uses a seeded demo user and demo vehicle instead of full production account management
- Registration and JWT-based authentication are not implemented in the backend for this version
- The backend is documented for local execution; no public hosted deployment is currently published
- The repository does not publish a public video demonstration link yet
- Backend stubs and mock/demo behavior are intentionally accepted for MVP validation according to the customer interview
- Android compatibility currently allows temporary demo login with `demo` / `password`
- The implementation is limited to one vehicle per user
- AI behavior is not yet a full production integration with persistent conversational memory and validated structured extraction
- Advanced recommendation logic, notifications, multiple vehicles, ownership transfer, voice input, and receipt parsing remain outside MVP v0

## Local Setup Instructions

- Main local setup guide: [README.md](../../README.md)
- Local setup section: [README.md#local-setup](../../README.md#local-setup)
- API contract: [docs/api-contract.md](../../docs/api-contract.md)

## Repeatable Smoke-Check Scenario

Goal: confirm that the local MVP v0 foundation works from backend startup to event persistence and statistics refresh.

1. Start PostgreSQL and the FastAPI backend with `docker compose up --build`.
2. Verify backend health at [http://localhost:8000/health](http://localhost:8000/health).
3. Open the Android app in the emulator.
4. Log in with demo credentials:
   - primary: `demo` / `demo`
   - temporary Android compatibility login: `demo` / `password`
5. Open the history/timeline screen.
6. Open the form or flow used to add a vehicle event.
7. Create a fuel event, for example: description `Full tank`, amount `60`, mileage `125000`.
8. Save the event and confirm that the backend accepts it through `POST /events`.
9. Verify that the new record appears in the history/timeline view.
10. Verify that statistics change accordingly on the statistics screen or through `GET /stats`.
11. Open the chat screen.
12. Send a message and confirm that the UI and backend path respond consistently for the demo flow.

## Demo Credentials

- Demo user: `demo`
- Demo password: `demo`
- Temporary Android compatibility password: `password`
