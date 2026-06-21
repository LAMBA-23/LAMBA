# LAMBA MVP v1 API contract

Base URL for local development: `http://localhost:8000`

## GET /health

Checks that backend is running.

Response:

```json
{
  "status": "ok"
}
```

## POST /auth/login

Login with an existing user account. JWT is not used yet.

Primary demo credentials: `demo` / `demo`.

Temporary Android compatibility credentials: `demo` / `password`.

Request:

```json
{
  "username": "demo",
  "password": "demo"
}
```

Success response:

```json
{
  "success": true,
  "user_id": 1
}
```

Error response:

```json
{
  "success": false
}
```

## POST /auth/register

Creates a new user account and initializes an empty default car for that user.

Required fields:

- `username`: 1-64 characters after trimming whitespace.
- `password`: 8-128 characters.

Request:

```json
{
  "username": "new-user",
  "password": "password123"
}
```

Success response (`201 Created`):

```json
{
  "success": true,
  "user_id": 2
}
```

Duplicate username response (`400 Bad Request`):

```json
{
  "detail": "Username is already registered"
}
```

Validation error response (`422 Unprocessable Entity`):

```json
{
  "detail": [
    {
      "loc": ["body", "password"],
      "msg": "String should have at least 8 characters",
      "type": "string_too_short"
    }
  ]
}
```

## GET /vehicle

Returns a user's car.

Query parameters:

- `user_id` optional. If omitted, backend returns the demo user's car for MVP v0 compatibility.

Example:

```text
GET /vehicle?user_id=2
```

Response:

```json
{
  "id": 1,
  "brand": "BMW",
  "model": "M4",
  "production_year": 2020,
  "current_mileage": 125000,
  "created_at": "2026-06-13T12:00:00"
}
```

## POST /vehicle

Creates a vehicle profile for a user. Each user can have only one vehicle in MVP v1.

Request:

```json
{
  "user_id": 1,
  "brand": "Toyota",
  "model": "Camry",
  "production_year": 2023,
  "current_mileage": 10000
}
```

Success response (201):

```json
{
  "id": 1,
  "brand": "Toyota",
  "model": "Camry",
  "production_year": 2023,
  "current_mileage": 10000,
  "created_at": "2026-06-20T12:00:00"
}
```

Error responses:

- 409 — User already has a vehicle:

```json
{
  "detail": "User already has a vehicle. Only one vehicle per user is allowed in MVP v1."
}
```

- 404 — User not found:

```json
{
  "detail": "User not found"
}
```

- 422 — Validation error (empty brand, invalid year, negative mileage, etc.):

```json
{
  "detail": [
    {
      "loc": ["body", "brand"],
      "msg": "brand must not be empty",
      "type": "value_error"
    }
  ]
}
```

## GET /vehicle/{user_id}

Returns the vehicle profile for a given user.

Response:

```json
{
  "id": 1,
  "brand": "BMW",
  "model": "M4",
  "production_year": 2020,
  "current_mileage": 125000,
  "created_at": "2026-06-13T12:00:00"
}
```

Error response (404):

```json
{
  "detail": "Vehicle not found"
}
```

## GET /events

Returns all events for a user's car.

Query parameters:

- `user_id` optional. If omitted, backend returns the demo user's events for MVP v0 compatibility.

Example:

```text
GET /events?user_id=2
```

Response:

```json
[
  {
    "id": 1,
    "type": "fuel",
    "description": "Full tank",
    "amount": 60,
    "mileage": 125000,
    "created_at": "2026-06-13T12:00:00"
  }
]
```

## POST /chat/parse-event

Parses a free-form chat message into a structured event draft. This endpoint does not create an event in the database; it only returns parsed JSON for the next persistence step.

Allowed parsed event types: `fuel`, `repair`, `trip`, `issue`.

Request:

```json
{
  "message": "Заправился на 2500 рублей, пробег 125300"
}
```

Parsed response:

```json
{
  "status": "parsed",
  "parsed_event": {
    "type": "fuel",
    "description": "Заправка на 2500 рублей",
    "amount": 2500,
    "mileage": 125300
  },
  "clarification_question": null
}
```

Clarification response:

```json
{
  "status": "clarification_needed",
  "parsed_event": null,
  "clarification_question": "Вы имеете в виду 1500 километров пробега?"
}
```

## POST /events

Creates an event for a user's car.

Query parameters:

- `user_id` optional. If omitted, backend creates the event for the demo user's car for MVP v0 compatibility.

Example:

```text
POST /events?user_id=2
```

Allowed event types: `fuel`, `repair`, `trip`, `issue`.

If `amount` is missing, backend stores `0`.

If `mileage` is missing, backend uses the car `current_mileage`.

Request:

```json
{
  "type": "fuel",
  "description": "Full tank",
  "amount": 60,
  "mileage": 125000
}
```

Response:

```json
{
  "id": 1,
  "type": "fuel",
  "description": "Full tank",
  "amount": 60,
  "mileage": 125000,
  "created_at": "2026-06-13T12:00:00"
}
```

Validation error response:

```json
{
  "detail": [
    {
      "loc": ["body", "type"],
      "msg": "Input should be 'fuel', 'repair', 'trip' or 'issue'",
      "type": "literal_error"
    }
  ]
}
```

## GET /stats

Returns statistics for a user's car.

Query parameters:

- `user_id` optional. If omitted, backend returns stats for the demo user's car for MVP v0 compatibility.

Example:

```text
GET /stats?user_id=2
```

Rules:

- `fuel_expenses`: sum of `amount` for `fuel` events.
- `repair_expenses`: sum of `amount` for `repair` events.
- `trip_count`: count of `trip` events.
- `total_recorded_mileage`: max event `mileage`, otherwise car `current_mileage`.

Response:

```json
{
  "fuel_expenses": 60,
  "repair_expenses": 0,
  "trip_count": 0,
  "total_recorded_mileage": 125000
}
```
