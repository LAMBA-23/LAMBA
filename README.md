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

## Assignment 6 Sprint Increment Release

- Release: `v1.3.0 - Assignment 6 Sprint 4 Increment (MVP v3 Trial Release)`
- Sprint milestone: [Sprint 4 - Trial Release and Transition Readiness](https://github.com/LAMBA-23/LAMBA/milestone/4)
- Deployed backend URL: `http://186.246.27.211:8000`
- Swagger/API docs URL: `http://186.246.27.211:8000/docs`
- Week 6 report: [reports/week6/README.md](./reports/week6/README.md)
- Customer handover: [docs/customer-handover.md](./docs/customer-handover.md)
- Public sanitized demo video: [TODO: add video link after recording]
- Runnable artifact: Android APK attached to release / Docker Compose instructions

The `v1.3.0` Sprint increment includes security hardening with password hashing and rate limiting, Android logout functionality with local session cleanup, local chat history persistence for the last five dialogs, improved statistics UI for adaptive display, support for decimal fuel liters in events, trip records by odometer start/end values, repair and breakdown event records, application launcher icon, and session restore improvements after app restart.

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

Deployed MVP v2 backend is available at:

```text
http://186.246.27.211:8000
```

Swagger UI for the deployed backend:

```text
http://186.246.27.211:8000/docs
```

Note: Demo credentials have been removed for security. Register a new user using the Android app or POST /auth/register endpoint. A default car will be created automatically for each new user.

Smoke-check with curl:

```bash
curl http://localhost:8000/health
curl -X POST http://localhost:8000/auth/register -H "Content-Type: application/json" -d "{\"username\":\"test-user\",\"password\":\"test-pass123\"}"
curl -X POST http://localhost:8000/auth/login -H "Content-Type: application/json" -d "{\"username\":\"test-user\",\"password\":\"test-pass123\"}"
curl "http://localhost:8000/vehicle?user_id=1"
curl -X POST "http://localhost:8000/events?user_id=1" -H "Content-Type: application/json" -d "{\"type\":\"fuel\",\"description\":\"Full tank\",\"amount\":60,\"mileage\":125000}"
curl "http://localhost:8000/events?user_id=1"
curl "http://localhost:8000/stats?user_id=1"
curl -X POST "http://localhost:8000/chat/ask?user_id=1" -H "Content-Type: application/json" -d "{\"message\":\"What is my vehicle history summary?\"}"
```

Smoke-check in PowerShell:

```powershell
Invoke-RestMethod -Uri http://localhost:8000/health

$registration = @{username='test-user'; password='test-pass123'} | ConvertTo-Json -Compress
$registeredUser = Invoke-RestMethod -Uri http://localhost:8000/auth/register -Method Post -ContentType 'application/json' -Body $registration

$login = @{username='test-user'; password='test-pass123'} | ConvertTo-Json -Compress
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

- [Hosted maintained documentation site](https://lamba-23.github.io/LAMBA/)
- [Development process](docs/development-process.md)
- [API contract](docs/api-contract.md)
- [Week 2 report](reports/week2/README.md)
- [Week 3 report](reports/week3/README.md)
- [Week 4 report](reports/week4/README.md)
- [Week 5 report](reports/week5/README.md)
- [MVP v0](reports/week2/mvp-v0-report.md)

The Week 2 MVP v0 smoke-check scenario, deployed URL, demo credentials, and current limitations are documented in [reports/week2/mvp-v0-report.md](reports/week2/mvp-v0-report.md).

## Runnable Artifact

The runnable backend artifact is Docker Compose: `docker-compose.yml` starts PostgreSQL and the FastAPI backend. The Android runnable artifact for `v1.3.0` should be the debug APK at `app/build/outputs/apk/debug/app-debug.apk` or the APK attached to the GitHub Release after release publication.
