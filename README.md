# LAMBA - digital twin of a car

LAMBA is an Android application for keeping a digital twin of a car. It helps a
vehicle owner record trips, refueling, repairs, breakdowns, and other vehicle
history events, then inspect them through a timeline, statistics, and an AI
assistant chat.

The current public increment is **MVP v2 / Assignment 5 Sprint 3**. Assignment 6
work is preparing the project for handover, documentation review, and the next
MVP v3 updates.

## Current Product Access

- [GitHub Release: v1.2.0 - Assignment 5 Sprint 3 Increment (MVP v2)](https://github.com/LAMBA-23/LAMBA/releases/tag/v1.2.0)
- [Android APK asset: mvp-v2.apk](https://github.com/LAMBA-23/LAMBA/releases/download/v1.2.0/mvp-v2.apk)
- [Hosted maintained documentation site](https://lamba-23.github.io/LAMBA/)
- Deployed backend API: `http://186.246.27.211:8000`
- Deployed Swagger/API docs: `http://186.246.27.211:8000/docs`
- Demo credentials: `demo` / `demo`

The backend can also be run locally with Docker Compose. See
[Local Setup](#local-setup).

## Product Scope

The current product supports:

- user registration and login;
- vehicle profile setup;
- manual vehicle-history records;
- chat-assisted vehicle-history records;
- vehicle timeline;
- statistics for mileage, expenses, fuel, and records;
- backend API documentation through Swagger UI.

Main follow-up work for MVP v3 includes decimal fuel liters, start/end odometer
trip entry, repair/breakdown wording, photo attachments, improved assistant
statistics answers, and final handover documentation.

## Tech Stack

- Android app: Kotlin, Gradle
- Backend: FastAPI, SQLAlchemy, Pydantic
- Database: PostgreSQL
- Deployment: Docker Compose
- Documentation site: GitHub Pages
- CI: GitHub Actions

## Local Setup

### Backend + PostgreSQL

Requirements:

- Docker
- Docker Compose

Run:

```bash
docker compose up --build
```

The backend will be available at:

```text
http://localhost:8000
```

Local API documentation will be available at:

```text
http://localhost:8000/docs
```

For AI-backed chat endpoints, set these environment variables before startup:

```text
TIMEWEB_API_KEY
TIMEWEB_AGENT_ID
```

The database is seeded automatically and idempotently:

- user: `demo` / `demo`
- temporary Android compatibility login: `demo` / `password`
- car: `BMW M4`, year `2020`, mileage `125000`

Stop the local services:

```bash
docker compose down
```

### Android App

Build a debug APK:

```powershell
.\gradlew.bat assembleDebug
```

Expected local APK path after a successful debug build:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Smoke Checks

### curl

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

### PowerShell

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

## Verification

Backend tests:

```bash
docker compose run --rm backend pytest tests
```

Backend CI checks run in GitHub Actions on pull requests and pushes to `main`.
They cover backend linting, formatting, automated tests, coverage reporting, and
dependency health checking.

Link checking for Markdown files is handled by the repository Link Check
workflow.

## Maintained Documentation

- [Hosted maintained documentation site](https://lamba-23.github.io/LAMBA/)
- [Development process](docs/development-process.md)
- [API contract](docs/api-contract.md)
- [Roadmap](docs/roadmap.md)
- [Definition of Done](docs/definition-of-done.md)
- [Testing overview](docs/testing.md)
- [Quality requirements](docs/quality-requirements.md)
- [Quality requirement tests](docs/quality-requirement-tests.md)
- [User acceptance tests](docs/user-acceptance-tests.md)
- [Architecture overview](docs/architecture/README.md)
- [Week 5 report](reports/week5/README.md)

Assignment 6 also maintains these handover and workflow entry points as they
become available:

- `docs/customer-handover.md`
- [CONTRIBUTING.md](CONTRIBUTING.md)
- `AGENTS.md`
- `reports/week6/README.md`
- `reports/week7/README.md`

## Releases and Reports

- [CHANGELOG.md](CHANGELOG.md)
- [MVP v2 release](https://github.com/LAMBA-23/LAMBA/releases/tag/v1.2.0)
- [Week 5 report](reports/week5/README.md)
- [Week 4 report](reports/week4/README.md)
- [Week 3 report](reports/week3/README.md)
- [Week 2 report](reports/week2/README.md)

## Security and Public Artifacts

Do not commit secrets, `.env` files, private credentials, private customer access
details, or private customer-identifying evidence. Public reports and repository
documentation should use sanitized links and public evidence only.
