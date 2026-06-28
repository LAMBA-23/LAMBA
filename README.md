# LAMBA - digital twin of a car

A mobile app for creating a digital twin of a car. The system collects data on trips, repairs, gas stations and the technical condition of the car, forming a timeline of the car. Interaction with the system goes using an AI agent through a conversational interface.

## Planned Features

- User authentication
- Add a car
- Add data about car using chat with an AI assistant
- View the timeline with car condition

## Tech Stack

- Frontend: Kotlin
- Backend: FastAPI, SQLAlchemy
- Database: PostgreSQL
- Deployment: Docker Compose

## Assignment 4 Sprint Increment Release

- Release: `v1.1.0 - Assignment 4 Sprint 2 Increment`
- Sprint milestone: [Sprint 2 - Chat Event Capture & Assistant & Statistics](https://github.com/LAMBA-23/LAMBA/milestone/2)
- Deployed backend URL: `TODO after deployment`
- Swagger/API docs URL: `TODO after deployment`
- Demo credentials: `TODO if used`
- Public sanitized demo video: `TODO after recording`
- Runnable artifact: Android APK attached to release / Docker Compose instructions

The `v1.1.0` Sprint increment includes the visible vehicle history/timeline flow, statistics screen, `POST /chat/ask`, maintained event/statistics APIs, Week 4 UAT evidence, automated quality requirement tests, and release/deployment documentation. Final server URL, public video, release tag, GitHub Release, and published artifact evidence must be added only after they are available.

## Local Setup

### Backend + PostgreSQL

Requirements:

- Docker and Docker Compose

Run:

```bash
docker compose up --build
```

For AI-backed chat endpoints, set these environment variables before startup:

```text
TIMEWEB_API_KEY
TIMEWEB_AGENT_ID
```

Backend will be available at:

```text
http://localhost:8000
```

Local API documentation will be available at:

```text
http://localhost:8000/docs
```

Deployed MVP v0 backend is available at:

```text
http://10.93.26.193:8000
```

Swagger UI for the deployed backend:

```text
http://10.93.26.193:8000/docs
```

The database is seeded automatically and idempotently:

- user: `demo` / `demo`
- temporary Android compatibility login: `demo` / `password`
- car: `BMW M4`, year `2020`, mileage `125000`

Smoke-check with curl:

```bash
curl http://localhost:8000/health
curl -X POST http://localhost:8000/auth/register -H "Content-Type: application/json" -d "{\"username\":\"new-user\",\"password\":\"password123\"}"
curl -X POST http://localhost:8000/auth/login -H "Content-Type: application/json" -d "{\"username\":\"demo\",\"password\":\"demo\"}"
curl "http://localhost:8000/vehicle?user_id=1"
curl -X POST "http://localhost:8000/events?user_id=1" -H "Content-Type: application/json" -d "{\"type\":\"fuel\",\"description\":\"Full tank\",\"amount\":60,\"mileage\":125000}"
curl "http://localhost:8000/events?user_id=1"
curl "http://localhost:8000/stats?user_id=1"
curl -X POST "http://localhost:8000/chat/ask?user_id=1" -H "Content-Type: application/json" -d "{\"message\":\"What is my vehicle history summary?\"}"
```

Smoke-check in PowerShell:

```powershell
Invoke-RestMethod -Uri http://localhost:8000/health

$registration = @{username='new-user'; password='password123'} | ConvertTo-Json -Compress
$registeredUser = Invoke-RestMethod -Uri http://localhost:8000/auth/register -Method Post -ContentType 'application/json' -Body $registration

$login = @{username='demo'; password='demo'} | ConvertTo-Json -Compress
Invoke-RestMethod -Uri http://localhost:8000/auth/login -Method Post -ContentType 'application/json' -Body $login

Invoke-RestMethod -Uri "http://localhost:8000/vehicle?user_id=$($registeredUser.user_id)"

$event = @{type='fuel'; description='Full tank'; amount=60; mileage=125000} | ConvertTo-Json -Compress
Invoke-RestMethod -Uri "http://localhost:8000/events?user_id=$($registeredUser.user_id)" -Method Post -ContentType 'application/json' -Body $event

Invoke-RestMethod -Uri "http://localhost:8000/events?user_id=$($registeredUser.user_id)"

Invoke-RestMethod -Uri "http://localhost:8000/stats?user_id=$($registeredUser.user_id)"

$question = @{message='What is my vehicle history summary?'} | ConvertTo-Json -Compress
Invoke-RestMethod -Uri "http://localhost:8000/chat/ask?user_id=$($registeredUser.user_id)" -Method Post -ContentType 'application/json' -Body $question
```

Chat parsing smoke-check in PowerShell:

```powershell
$chat = @{message='Заправился на 2500 рублей, пробег 125300'} | ConvertTo-Json -Compress
Invoke-RestMethod -Uri http://localhost:8000/chat/parse-event -Method Post -ContentType 'application/json' -Body $chat
```

Backend tests:

```bash
docker compose run --rm backend pytest tests/test_events.py tests/test_chat_parse.py
```

Android debug APK build:

```powershell
.\gradlew.bat assembleDebug
```

Expected local APK path after a successful debug build:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Smoke-check the deployed backend after deployment by replacing the placeholder URL:

```bash
DEPLOYED_BACKEND_URL="TODO after deployment"
curl "$DEPLOYED_BACKEND_URL/health"
curl "$DEPLOYED_BACKEND_URL/docs"
curl "$DEPLOYED_BACKEND_URL/events?user_id=1"
curl "$DEPLOYED_BACKEND_URL/stats?user_id=1"
curl -X POST "$DEPLOYED_BACKEND_URL/chat/ask?user_id=1" -H "Content-Type: application/json" -d "{\"message\":\"What is my vehicle history summary?\"}"
```

Backend CI checks run in GitHub Actions on pull requests and pushes to `main`, covering backend linting, formatting checks, automated tests, coverage reporting, and dependency health checking.

Stop:

```bash
docker compose down
```

## Documentation

- [API contract](docs/api-contract.md)
- [Week 2 report](reports/week2/README.md)
- [MVP v0](reports/week2/mvp-v0-report.md)

The Week 2 MVP v0 smoke-check scenario, deployed URL, demo credentials, and current limitations are documented in [reports/week2/mvp-v0-report.md](reports/week2/mvp-v0-report.md).

## Runnable Artifact

The runnable backend artifact is Docker Compose: `docker-compose.yml` starts PostgreSQL and the FastAPI backend. The Android runnable artifact for `v1.1.0` should be the debug APK at `app/build/outputs/apk/debug/app-debug.apk` or the APK attached to the GitHub Release after release publication.
