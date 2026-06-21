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

## Local Setup

### Backend + PostgreSQL

Requirements:

- Docker and Docker Compose

Run:

```bash
docker compose up --build
```

Backend will be available at:

```text
http://localhost:8000
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
curl "http://localhost:8000/vehicle?user_id=2"
curl http://localhost:8000/events
curl -X POST http://localhost:8000/events -H "Content-Type: application/json" -d "{\"type\":\"fuel\",\"description\":\"Full tank\",\"amount\":60,\"mileage\":125000}"
curl http://localhost:8000/stats
```

Smoke-check in PowerShell:

```powershell
Invoke-RestMethod -Uri http://localhost:8000/health

$registration = @{username='new-user'; password='password123'} | ConvertTo-Json -Compress
$registeredUser = Invoke-RestMethod -Uri http://localhost:8000/auth/register -Method Post -ContentType 'application/json' -Body $registration

$login = @{username='demo'; password='demo'} | ConvertTo-Json -Compress
Invoke-RestMethod -Uri http://localhost:8000/auth/login -Method Post -ContentType 'application/json' -Body $login

Invoke-RestMethod -Uri "http://localhost:8000/vehicle?user_id=$($registeredUser.user_id)"

Invoke-RestMethod -Uri http://localhost:8000/events

$event = @{type='fuel'; description='Full tank'; amount=60; mileage=125000} | ConvertTo-Json -Compress
Invoke-RestMethod -Uri http://localhost:8000/events -Method Post -ContentType 'application/json' -Body $event

Invoke-RestMethod -Uri http://localhost:8000/stats
```

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

The runnable backend artifact is Docker Compose: `docker-compose.yml` starts PostgreSQL and the FastAPI backend.
