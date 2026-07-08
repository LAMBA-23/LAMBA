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
    "fuel_liters": 40.5,
    "mileage": 125000,
    "odometer_start": null,
    "odometer_end": null,
    "trip_distance": null,
    "created_at": "2026-06-13T12:00:00"
  }
]
```

## POST /chat/parse-event

Parses a free-form chat message into a structured event draft. This endpoint does not create an event in the database; it only returns parsed JSON for the next persistence step.

Allowed parsed event types: `fuel`, `repair`, `trip`, `issue`.

Messages that ask to check vehicle condition or request assistant/statistics analysis are not parsed as timeline events.

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
    "fuel_liters": null,
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

Trip distance phrases with clear kilometer units can be parsed directly:

```json
{
  "message": "поездка 100 километров"
}
```

Parsed response:

```json
{
  "status": "parsed",
  "parsed_event": {
    "type": "trip",
    "description": "Поездка на 100 километров",
    "amount": null,
    "fuel_liters": null,
    "mileage": 100
  },
  "clarification_question": null
}
```

Fuel phrases with liters can also be parsed directly:

```json
{
  "message": "заправилась на 10.5 литров за 1000 рублей"
}
```

Parsed response:

```json
{
  "status": "parsed",
  "parsed_event": {
    "type": "fuel",
    "description": "Заправка на 10.5 литров",
    "amount": 1000,
    "fuel_liters": 10.5,
    "mileage": null
  },
  "clarification_question": null
}
```

## POST /chat/ask

Answers a free-form user question about the vehicle history.

Request:

```json
{
  "message": "Покажи последние расходы"
}
```

Response:

```json
{
  "answer": "Расходы за последние 5 записей: 9500 ₽\n\n..."
}
```

Behavior:

- Backend classifies the message as an expense query, a statistics query, or a general assistant question.
- Expense and statistics queries are answered deterministically from database events and do not call the LLM.
- Other questions continue through the existing LLM flow.

Deterministic expense answers:

- An expense is any event with `amount > 0`, regardless of type: `fuel`, `repair`, `trip`, `issue`.
- Events with `amount = 0`, `amount = null`, or missing amount are not shown in expense answers.
- Category mapping:
  - `fuel` -> `Топливо`
  - `repair` -> `Ремонт`
  - `trip` -> `Поездки`
  - `issue` -> `Проблемы`
- No period -> latest 5 expense events, sorted from newest to oldest.
- `week` / `за неделю` -> last 7 days.
- `month` / `за месяц` -> last 30 days.
- `all time` / `за всё время` -> all expense records.
- `за последние N дней` -> last `N` days.
- Category-specific filters:
  - fuel / бензин / заправки -> `fuel`
  - repair / сервис -> `repair`
  - issue / проблемы / поломки -> `issue`
  - trip / поездки -> `trip`
- Expense answers do not include event date.
- Expense answers do not include mileage.
- For `fuel`, if `fuel_liters > 0`, the response shows liters in the line item.
- If no expenses match, backend returns exactly:

```text
За выбранный период расходов не найдено.
```

Deterministic statistics answers:

- Questions such as `Покажи статистику` return a short summary built from database data.
- The summary includes expenses, mileage, fuel liters, and record count for the selected period.
- If no period is specified for statistics, backend uses all available history.
- Statistics queries also support `за последние N дней`.

Deterministic event answers:

- Questions containing `события` are answered from database events without calling the LLM.
- If no period is specified, backend returns the latest 5 events sorted from newest to oldest.
- Event queries support `за неделю`, `за месяц`, `за всё время`, and `за последние N дней`.
- Event answers are returned as a numbered list.
- Event dates are rendered as plain text `DD.MM.YYYY` without markdown markers.
- Fuel events are rendered without repeating the amount from `description`.

General assistant questions:

- Non-expense and non-statistics questions continue through the LLM flow.
- Backend sends at most 30 latest events to the LLM context.
- Context events are sorted from newest to oldest.
- Context includes `created_at` for each event.
- Context includes `amount` only when `amount > 0`.
- Context includes `fuel_liters` only when `fuel_liters > 0`.
- Context includes `mileage` when `mileage > 0`.

## POST /events

Creates an event for a user's car.

Query parameters:

- `user_id` (int, required) — the ID of the user whose vehicle should receive the event.

Example:

```text
POST /events?user_id=2
```

Allowed event types: `fuel`, `repair`, `trip`, `issue`.

`description` must not be empty.

`amount`, `mileage`, `odometer_start`, and `odometer_end` must not be negative when provided.

`fuel_liters` can be an integer or decimal number and must not be negative when provided.

If `amount` is missing, backend stores `0`.

If `fuel_liters` is missing, backend stores `0`.

If `mileage` is missing for `trip`, backend tries to extract trip distance from `description`.

If `mileage` is missing for non-trip events, backend stores `0`.

For `trip` events, `odometer_start` and `odometer_end` can be provided together.
When they are provided, backend stores `odometer_end` as the event `mileage` and
calculates the trip distance as `odometer_end - odometer_start`.
The calculated distance is returned as `trip_distance`.

For `trip` events, `mileage` is interpreted as:

- trip distance when the provided value is less than or equal to the previous known mileage;
- an already-updated odometer value when the provided value is greater than the previous known mileage.

When a trip distance is provided, backend stores the new odometer mileage in the event record.

Trip by odometer request:

```json
{
  "type": "trip",
  "description": "Trip by odometer",
  "amount": 0,
  "odometer_start": 125000,
  "odometer_end": 125150
}
```

Trip by odometer response:

```json
{
  "id": 2,
  "type": "trip",
  "description": "Trip by odometer",
  "amount": 0,
  "fuel_liters": 0,
  "mileage": 125150,
  "odometer_start": 125000,
  "odometer_end": 125150,
  "trip_distance": 150,
  "created_at": "2026-06-13T12:05:00"
}
```

Request:

```json
{
  "type": "fuel",
  "description": "Full tank",
  "amount": 60,
  "fuel_liters": 40.5,
  "mileage": 125000,
  "odometer_start": null,
  "odometer_end": null,
  "trip_distance": null
}
```

Response:

```json
{
  "id": 1,
  "type": "fuel",
  "description": "Full tank",
  "amount": 60,
  "fuel_liters": 40.5,
  "mileage": 125000,
  "odometer_start": null,
  "odometer_end": null,
  "trip_distance": null,
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
- `issue` events do not affect expense or mileage aggregates, but they are counted in `records_count`.
- `amount = null` is treated as `0`.
- `fuel_liters = null` is treated as `0`.
- `mileage = null` is treated as `0`.
- `week.mileage` / `week.mileage_km`: total traveled distance for `trip` events in the last 7 days. For odometer trips, this is `odometer_end - odometer_start`; otherwise it is calculated as chronological delta between previous known mileage and each trip event's stored odometer mileage.
- `month.mileage` / `month.mileage_km`: total traveled distance for `trip` events in the last 30 days. For odometer trips, this is `odometer_end - odometer_start`; otherwise it is calculated as chronological delta between previous known mileage and each trip event's stored odometer mileage.
- `all_time.mileage` / `all_time.mileage_km`: current total vehicle odometer mileage, calculated as `initial car mileage + cumulative trip distance`.
- `fuel_expenses`: sum of `amount` for `fuel` events in the period.
- `repair_expenses`: sum of `amount` for `repair` events in the period.
- `total_expenses` / `expenses_rub`: `fuel_expenses + repair_expenses`.
- `records_count`: number of all events in the period.
- `fuel_liters`: sum of `fuel_liters` for `fuel` events in the period; decimal values are supported.
- `avg_fuel_consumption` and `avg_fuel_consumption_l_per_100km`: return `0` until a liters source exists.
- `avg_expense_consumption`: return `0` when it cannot be computed from the available period data.
- Top-level legacy fields remain available: `fuel_expenses`, `repair_expenses`, `trip_count`, `total_recorded_mileage`.
- `total_recorded_mileage` matches the all-time odometer value used by Android.
- Empty/no-data case returns numeric zeroes, not `null`.

Response:

```json
{
  "fuel_expenses": 2500,
  "repair_expenses": 7000,
  "trip_count": 1,
  "total_recorded_mileage": 125100,
  "week": {
    "mileage": 100,
    "total_expenses": 2500,
    "fuel_expenses": 2500,
    "repair_expenses": 0,
    "avg_fuel_consumption": 0,
    "avg_expense_consumption": 0,
    "mileage_km": 100,
    "expenses_rub": 2500,
    "fuel_liters": 40.5,
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
    "fuel_liters": 40.5,
    "records_count": 4,
    "avg_fuel_consumption_l_per_100km": 0
  },
  "all_time": {
    "mileage": 125100,
    "total_expenses": 9500,
    "fuel_expenses": 2500,
    "repair_expenses": 7000,
    "avg_fuel_consumption": 0,
    "avg_expense_consumption": 0,
    "mileage_km": 125100,
    "expenses_rub": 9500,
    "fuel_liters": 40.5,
    "records_count": 5,
    "avg_fuel_consumption_l_per_100km": 0
  }
}
```
