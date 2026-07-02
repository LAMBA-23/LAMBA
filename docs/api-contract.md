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

## GET /vehicle

Returns the vehicle profile for a given user.

Query parameters:

- `user_id` (int, required) — the user ID.

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

- `user_id` (int, required) — the ID of the user whose vehicle events should be returned.

Events are returned in ascending ID order.

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

Allowed parsed event types: `fuel`, `repair`, `trip`, `issue`, `condition`.

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

- `user_id` (int, required) — the ID of the user whose vehicle should receive the event.

Example:

```text
POST /events?user_id=2
```

Allowed event types: `fuel`, `repair`, `trip`, `issue`, `condition`.

`description` must not be empty.

`amount` and `mileage` must not be negative when provided.

`fuel_liters` must not be negative when provided.

If `amount` is missing, backend stores `0`.

If `fuel_liters` is missing, backend stores `0`.

If `mileage` is missing, backend uses the car `current_mileage`.

For `trip` events, `mileage` is interpreted as:

- trip distance when the provided value is less than or equal to the previous known mileage;
- an already-updated odometer value when the provided value is greater than the previous known mileage.

When a trip distance is provided, backend stores the new odometer mileage in the event record.

Request:

```json
{
  "type": "fuel",
  "description": "Full tank",
  "amount": 60,
  "fuel_liters": 40,
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
  "fuel_liters": 40,
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
      "msg": "Input should be 'fuel', 'repair', 'trip', 'issue' or 'condition'",
      "type": "literal_error"
    }
  ]
}
```

## GET /stats

Returns statistics for a user's car.

Query parameters:

- `user_id` (int, required) — the ID of the user whose vehicle statistics should be returned.

Example:

```text
GET /stats?user_id=2
```

Rules:

- `week` includes events whose `created_at` is within the last 7 days.
- `month` includes events whose `created_at` is within the last 30 days.
- `all_time` includes all events for the user's car.
- Only `fuel`, `repair`, and `trip` events affect statistics.
- `issue` and `condition` events do not affect expense or mileage aggregates, but they are counted in `records_count`.
- `amount = null` is treated as `0`.
- `fuel_liters = null` is treated as `0`.
- `mileage = null` is treated as `0`.
- `mileage` / `mileage_km`: total traveled distance for `trip` events in the period, calculated as chronological delta between previous known mileage and each trip event's stored odometer mileage.
- `fuel_expenses`: sum of `amount` for `fuel` events in the period.
- `repair_expenses`: sum of `amount` for `repair` events in the period.
- `total_expenses` / `expenses_rub`: `fuel_expenses + repair_expenses`.
- `records_count`: number of all events in the period.
- `fuel_liters`: sum of `fuel_liters` for `fuel` events in the period.
- `avg_fuel_consumption` and `avg_fuel_consumption_l_per_100km`: return `0` until a liters source exists.
- `avg_expense_consumption`: return `0` when it cannot be computed from the available period data.
- Top-level legacy fields remain available: `fuel_expenses`, `repair_expenses`, `trip_count`, `total_recorded_mileage`.
- Empty/no-data case returns numeric zeroes, not `null`.

Response:

```json
{
  "fuel_expenses": 2500,
  "repair_expenses": 7000,
  "trip_count": 1,
  "total_recorded_mileage": 100,
  "week": {
    "mileage": 100,
    "total_expenses": 2500,
    "fuel_expenses": 2500,
    "repair_expenses": 0,
    "avg_fuel_consumption": 0,
    "avg_expense_consumption": 0,
    "mileage_km": 100,
    "expenses_rub": 2500,
    "fuel_liters": 40,
    "records_count": 2,
    "avg_fuel_consumption_l_per_100km": 0
  },
  "month": {
    "mileage": 100,
    "total_expenses": 9500,
    "fuel_expenses": 2500,
    "repair_expenses": 7000,
    "avg_fuel_consumption": 0,
    "avg_expense_consumption": 0,
    "mileage_km": 100,
    "expenses_rub": 9500,
    "fuel_liters": 40,
    "records_count": 4,
    "avg_fuel_consumption_l_per_100km": 0
  },
  "all_time": {
    "mileage": 100,
    "total_expenses": 9500,
    "fuel_expenses": 2500,
    "repair_expenses": 7000,
    "avg_fuel_consumption": 0,
    "avg_expense_consumption": 0,
    "mileage_km": 100,
    "expenses_rub": 9500,
    "fuel_liters": 40,
    "records_count": 5,
    "avg_fuel_consumption_l_per_100km": 0
  }
}
```
