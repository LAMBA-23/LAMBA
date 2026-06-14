# LAMBA MVP v0 API contract

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

Demo login. JWT and registration are not used in MVP v0.

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

## GET /vehicle

Returns the demo user's car.

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

## GET /events

Returns all events for the demo user's car.

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

## POST /events

Creates an event for the demo user's car. Android does not send `car_id`; backend finds the demo user and the demo user's car itself.

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

Returns statistics for the demo user's car.

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
