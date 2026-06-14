# MVP v0 Report

## Purpose / Description

MVP v0 для LAMBA - это запускаемая техническая основа продукта. Его цель - показать, что у проекта есть рабочий backend и слой хранения данных, которые поддерживают минимальный MVP-сценарий Android-приложения.

Текущий MVP v0 включает:

- FastAPI backend
- PostgreSQL storage
- SQLAlchemy ORM модели для пользователей, автомобиля и событий
- Docker Compose runnable artifact
- deployed backend на университетской VM
- повторяемый API smoke-check для login, создания события, получения списка событий и обновления статистики

MVP v0 не является полноценным production-продуктом. Он проверяет техническую цепочку:

```text
Android / API client -> FastAPI backend -> PostgreSQL storage -> backend response
```

## Deployment And Access

Deployed backend base URL:

```text
http://10.93.26.193:8000
```

Health check:

```text
http://10.93.26.193:8000/health
```

Swagger UI:

```text
http://10.93.26.193:8000/docs
```

Local runnable artifact:

- [docker-compose.yml](../../docker-compose.yml)

Local setup instructions:

- [Root README local setup](../../README.md#local-setup)

API contract:

- [docs/api-contract.md](../../docs/api-contract.md)

## Backend Endpoints In MVP v0

Текущий deployed backend предоставляет:

- `GET /health`
- `POST /auth/login`
- `GET /vehicle`
- `GET /events`
- `POST /events`
- `GET /stats`

Swagger UI с актуальным OpenAPI описанием доступен по адресу:

```text
http://10.93.26.193:8000/docs
```

## Demo Credentials

Основные demo credentials:

```text
username: demo
password: demo
```

Временные Android compatibility credentials:

```text
username: demo
password: password
```

Эти credentials используются только для публичного MVP demo. Реальные приватные credentials и API keys в репозиторий не добавлены.

## Public Video Demonstration

Public sanitized video demonstration link:

```text
TODO: add public video link after the integration QA demo is recorded.
```

Owner: `@lisa_va_si` according to the Week 2 responsibility split.

## Relationship To Prototype And MVP v1 Stories

Prototype показывает предполагаемый пользовательский опыт MVP v1: login, экран автомобиля, chat, history и statistics.

MVP v0 уже по объёму. Он фиксирует запускаемую backend-основу для продукта:

- `US-01` authentication foundation: demo login через `POST /auth/login`
- `US-02` vehicle foundation: один demo car хранится в PostgreSQL
- `US-05` history foundation: события сохраняются и возвращаются через `GET /events`
- `US-07` statistics foundation: расходы и пробег считаются через `GET /stats`

Текущий backend не реализует полный production user story. Он даёт техническую основу для smoke-check scenario.

## Repeatable Smoke-Check Scenario

### Access

Использовать deployed backend:

```text
http://10.93.26.193:8000
```

Или запустить локально:

```bash
docker compose up --build
```

### Steps And Expected Results

1. Открыть health endpoint.

   URL:

   ```text
   http://10.93.26.193:8000/health
   ```

   Expected result:

   ```json
   {"status":"ok"}
   ```

2. Открыть Swagger UI.

   URL:

   ```text
   http://10.93.26.193:8000/docs
   ```

   Expected result: Swagger UI открывается и показывает endpoints LAMBA Backend API.

3. Выполнить login с demo credentials.

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

4. Проверить список событий.

   Command:

   ```bash
   curl http://10.93.26.193:8000/events
   ```

   Expected result: backend возвращает JSON array событий. На чистой базе список может быть пустым.

5. Создать событие заправки.

   Command:

   ```bash
   curl -X POST http://10.93.26.193:8000/events \
     -H "Content-Type: application/json" \
     -d '{"type":"fuel","description":"Заправка 40 литров","amount":3000,"mileage":125000}'
   ```

   Expected result:

   ```json
   {
     "id": 1,
     "type": "fuel",
     "description": "Заправка 40 литров",
     "amount": 3000,
     "mileage": 125000,
     "created_at": "..."
   }
   ```

6. Проверить, что статистика обновилась.

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

## Verified Status

Smoke-check date:

```text
June 14, 2026
```

Проверено на университетской VM:

- `GET /health` returned `{"status":"ok"}`
- `POST /auth/login` with `demo` / `demo` returned success
- `POST /events` saved a fuel event
- `GET /stats` returned `fuel_expenses: 3000`
- Swagger UI was reachable at `/docs`

## Current Limitations, Placeholders, And Mocks

- Demo authentication only; JWT и registration не используются.
- Есть один seeded demo user и один seeded demo car.
- PostgreSQL запущен через Docker Compose на университетской VM.
- Event types ограничены значениями `fuel`, `repair`, `trip`, `issue`.
- Android compatibility password `demo` / `password` временный и нужен только для MVP integration.
- AI / Mistral integration не входит в текущий backend deployment.
- `/chat` не реализован в рамках текущего backend scope.
- Нет production-grade security, monitoring, backups или domain name.
- Public video demo link нужно добавить после записи demo.
