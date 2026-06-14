# MVP v0 Report

## Purpose and Foundation Description

MVP v0 is the technical foundation used to demonstrate that the core LAMBA workflow is feasible before full MVP v1 implementation. Its purpose is to validate the path `Android / API client -> FastAPI -> PostgreSQL -> backend response` and prove that the team can run a minimal but working vehicle-history flow.

At this stage, the system is not presented as a complete production product. Instead, MVP v0 confirms that the selected stack, repository structure, API contract, demo data, deployed backend, and basic UI screens are sufficient to support the next implementation step.

Validated foundation in MVP v0:

- Android client screens for login, chat, history, and statistics
- FastAPI backend with demo endpoints
- PostgreSQL persistence started through Docker Compose
- SQLAlchemy ORM models for users, cars, and events
- Seeded demo user and demo car
- Deployed backend on the university VM
- Repeatable smoke-check flow for health, login, event creation, history, and statistics
- API contract for login, vehicle retrieval, event creation, history, and statistics

## Deployment URL or Runnable Artifact

- Deployed MVP v0 backend URL: [http://10.93.26.193:8000](http://10.93.26.193:8000)
- Deployed health check: [http://10.93.26.193:8000/health](http://10.93.26.193:8000/health)
- Deployed Swagger UI: [http://10.93.26.193:8000/docs](http://10.93.26.193:8000/docs)
- Runnable artifact: [docker-compose.yml](../../docker-compose.yml)
- Local backend entry point after startup: [http://localhost:8000](http://localhost:8000)
- Local Swagger UI: [http://localhost:8000/docs](http://localhost:8000/docs)
- Local setup instructions: [README.md#local-setup](../../README.md#local-setup)

## Backend Endpoints in MVP v0

The deployed backend provides these MVP v0 endpoints:

- `GET /health`
- `POST /auth/login`
- `GET /vehicle`
- `GET /events`
- `POST /events`
- `GET /stats`

The current OpenAPI / Swagger documentation is available at:

```text
http://10.93.26.193:8000/docs
```

## Public Video Demonstration Link

- Public video demonstration: https://drive.google.com/drive/folders/1e6Er7lzkrpa83KdF2lnWaJwzvdv-bmwR?usp=sharing

## Relationship to the Prototype and Proposed MVP v1 Stories

MVP v0 is the implementation foundation behind the Week 2 prototype artifacts. The prototype shows the intended user flow, while MVP v0 verifies that the core parts of this flow can already be executed with backend support and demo data.

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
- `US-03` Send messages: represented by the Android chat screen prototype; backend AI/chat behavior is not part of the current deployed backend scope
- `US-04` Automatically create records: partially supported through structured event creation with `POST /events`
- `US-05` View vehicle timeline: supported through the history flow backed by `GET /events`
- `US-07` View basic statistics: supported through `GET /stats`

## Current Limitations, Placeholders, and Mocks

- MVP v0 uses a seeded demo user and demo vehicle instead of full production account management.
- Registration and JWT-based authentication are not implemented in the backend for this version.
- Demo authentication is intentionally simple for the smoke-check scenario.
- Android compatibility currently allows temporary demo login with `demo` / `password`.
- The implementation is limited to one vehicle per user.
- Event types are limited to `fuel`, `repair`, `trip`, and `issue`.
- AI / Mistral integration is not part of the current deployed backend scope.
- `/chat` is not implemented in the current deployed backend scope.
- Advanced recommendation logic, notifications, multiple vehicles, ownership transfer, voice input, and receipt parsing remain outside MVP v0.

## Local Setup Instructions

- Main local setup guide: [README.md](../../README.md)
- Local setup section: [README.md#local-setup](../../README.md#local-setup)
- API contract: [docs/api-contract.md](../../docs/api-contract.md)

## Repeatable Smoke-Check Scenario

Goal: confirm that the MVP v0 foundation works from backend startup or deployed access to event persistence and statistics refresh.

### Access

Use the deployed backend:

```text
http://10.93.26.193:8000
```

Or run locally:

```bash
docker compose up --build
```

### Steps and Expected Results

1. Open the health endpoint.

   URL:

   ```text
   http://10.93.26.193:8000/health
   ```

   Expected result:

   ```json
   {"status":"ok"}
   ```

2. Open Swagger UI.

   URL:

   ```text
   http://10.93.26.193:8000/docs
   ```

   Expected result: Swagger UI opens and shows the LAMBA Backend API endpoints.

3. Log in with demo credentials.

   Command:

   ```bash
   curl -X POST http://10.93.26.193:8000/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"demo","password":"demo"}'
   ```

   Expected result:

   ```json
   {"success":true,"user_id":1}
   ```

4. Check the vehicle endpoint.

   Command:

   ```bash
   curl http://10.93.26.193:8000/vehicle
   ```

   Expected result: backend returns the seeded demo vehicle, `BMW M4`, year `2020`, mileage `125000`.

5. Check the current event list.

   Command:

   ```bash
   curl http://10.93.26.193:8000/events
   ```

   Expected result: backend returns a JSON array of events. It may be empty on a clean database.

6. Create a fuel event.

   Command:

   ```bash
   curl -X POST http://10.93.26.193:8000/events \
     -H "Content-Type: application/json" \
     -d '{"type":"fuel","description":"Full tank","amount":3000,"mileage":125000}'
   ```

   Expected result:

   ```json
   {
     "id": 1,
     "type": "fuel",
     "description": "Full tank",
     "amount": 3000,
     "mileage": 125000,
     "created_at": "..."
   }
   ```

7. Verify that statistics were updated.

   Command:

   ```bash
   curl http://10.93.26.193:8000/stats
   ```

   Expected result:

   ```json
   {
     "fuel_expenses": 3000,
     "repair_expenses": 0,
     "trip_count": 0,
     "total_recorded_mileage": 125000
   }
   ```

8. Open the Android app in the emulator.

   Expected result: login screen is visible.

9. Log in with demo credentials:

   - primary: `demo` / `demo`
   - temporary Android compatibility login: `demo` / `password`

   Expected result: main screen opens.

10. Open the history/timeline screen, create an event, and check statistics.

    Expected result: Android sends requests to the backend, the event appears in history, and statistics are refreshed.

## Demo Credentials

- Demo user: `demo`
- Demo password: `demo`
- Temporary Android compatibility password: `password`

## Verified Status

Smoke-check date:

```text
June 14, 2026
```

Verified on the university VM:

- `GET /health` returned `{"status":"ok"}`
- `POST /auth/login` with `demo` / `demo` returned success
- `POST /events` saved a fuel event
- `GET /stats` returned updated fuel expenses
- Swagger UI was reachable at `/docs`
