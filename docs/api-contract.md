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
  "can_edit_mileage": true,
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
  "can_edit_mileage": true,
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
  "can_edit_mileage": true,
  "created_at": "2026-06-13T12:00:00"
}
```

## POST /auth/change-password

Changes a user's password. The current password is verified by the backend; the
client clears its local session after a successful response.

Query parameters:

- `user_id` (int, required) — the user ID.

Request:

```json
{
  "current_password": "password123",
  "new_password": "new-password123",
  "new_password_confirmation": "new-password123"
}
```

`new_password` must contain 8–128 characters and match
`new_password_confirmation`. The new password may equal the current password.

Success response: `204 No Content`.

Incorrect current password response: `400 Bad Request` with a neutral error
message. Invalid new-password input returns `422 Unprocessable Entity`.

`can_edit_mileage` is `false` after the vehicle has at least one fuel, repair,
trip, or issue record.

## PUT /vehicle

Updates the vehicle profile for a user.

Query parameters:

- `user_id` (int, required) — the user ID.

Request:

```json
{
  "brand": "Toyota",
  "model": "Camry",
  "production_year": 2023,
  "current_mileage": 10000
}
```

The response is the vehicle object returned by `GET /vehicle`.

- `409 Conflict` is returned when `current_mileage` differs after any history
  record exists. Brand, model, and production year can still be updated while
  the current mileage is unchanged.
- `422 Unprocessable Entity` is returned for invalid vehicle field values.

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

## POST /chat/transcribe

Transcribes one recorded chat message and returns editable text. The audio is processed
in memory and is not stored. Android records the audio locally and sends it as multipart
form data; the result is inserted into the chat field and is not sent automatically.

Query parameters:

| Name | Type | Required | Description |
|---|---|---|---|
| `user_id` | integer | Yes | Existing LAMBA user ID, following the chat endpoint convention. |

Multipart form fields:

| Name | Type | Required | Description |
|---|---|---|---|
| `audio` | file | Yes | A non-empty recorded audio file. |

Successful response (`200`):

```json
{
  "text": "Заправился на 30 литров."
}
```

The backend uses automatic language recognition. It removes hesitation sounds and
accidental repetitions, and conservatively improves punctuation and obvious recognition
errors while preserving facts, numbers, names, and the language of the transcript. If
cleanup is unavailable after transcription, the raw transcript is returned.

Errors include `400` for a missing or empty audio file, `404` for an unknown user, and
`413` when the file exceeds the configured upload limit (5 MiB by default), `429` after
10 transcription requests from one client IP in 60 seconds by default, and `503` when
all configured Mistral keys are rate-limited or out of quota. The backend environment
variables `CHAT_TRANSCRIPTION_MAX_BYTES`, `CHAT_TRANSCRIPTION_RATE_LIMIT`, and
`CHAT_TRANSCRIPTION_RATE_LIMIT_WINDOW_SECONDS` configure those defaults. In every error
case, the Android text input remains available.

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

Repair and breakdown records use separate event types:

- `repair` is for completed repair or service work. If `amount > 0`, it is counted in `repair_expenses` and `total_expenses`.
- `issue` is for a breakdown/problem note. It is returned in the timeline and counted in `records_count`, but it does not increase repair expenses, total expenses, mileage, or fuel liters.

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

Event responses include these optional photo fields for backward compatibility:

- `photo_url` - owner-checked API URL for the normalized original;
- `photo_thumbnail_url` - owner-checked API URL for the generated thumbnail;
- `photo_mime_type` - stored JPEG, PNG, or WebP media type;
- `photo_size` - normalized original size in bytes;
- `photo_width` and `photo_height` - normalized original dimensions.

All photo fields are `null` when an event has no photo. One photo is supported
for an `issue` event. `repair`, `fuel`, and `trip` events reject photo uploads.

## POST /events/{event_id}/photo

Uploads or replaces the photo for an `issue` event owned by the selected user.

```text
POST /events/42/photo?user_id=2
Content-Type: multipart/form-data
file=<image>
```

Rules:

- The multipart field name is `file`.
- JPEG, PNG, and WebP input is accepted up to 5 MB.
- Declared MIME type must match a decodable image.
- Width and height must each be at most 8,000 pixels and the image must contain
  at most 40 million pixels.
- The backend applies EXIF orientation, strips embedded metadata, re-encodes the
  original, and creates a thumbnail whose longest side is at most 512 pixels.
- A successful replacement removes the previous original and thumbnail only
  after the new metadata is committed.

The response is the updated event. Common errors are `400` for corrupt or
invalid image content, `404` for a missing/non-owned event, `413` for a file
larger than 5 MB, `415` for an unsupported MIME type, and `503` when photo
storage is unavailable.

## GET /events/{event_id}/photo

Returns normalized original image bytes after verifying event ownership.

```text
GET /events/42/photo?user_id=2
```

The response uses the stored image `Content-Type`, `Cache-Control: private`, and
`X-Content-Type-Options: nosniff`. Missing photos and non-owned events return
`404`. Storage object paths and bucket URLs are never returned.

## GET /events/{event_id}/photo/thumbnail

Returns the owner-checked thumbnail with the same response headers and error
rules as the original photo endpoint.

```text
GET /events/42/photo/thumbnail?user_id=2
```

## DELETE /events/{event_id}/photo

Clears photo metadata while keeping the event, then removes its original and
thumbnail objects. Successful deletion returns `204 No Content`.

```text
DELETE /events/42/photo?user_id=2
```

Deleting the event through `DELETE /events/{event_id}` also attempts immediate
cleanup of both photo objects. A transient object-cleanup failure is logged and
does not restore already deleted database data.

Photo storage defaults to the persistent local Docker volume. Setting
`PHOTO_STORAGE_BACKEND=s3` selects the private S3-compatible adapter configured
with `PHOTO_S3_BUCKET`, `PHOTO_S3_ENDPOINT`, `PHOTO_S3_REGION`,
`PHOTO_S3_PREFIX`, and the standard AWS credential environment variables.

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

## GET /recommendations

Returns lightweight rule-based recommendations for the user's car. The endpoint is
intended for the notifications/recommendations tab and does not call the AI
service.

Query parameters:

- `user_id` (int, required) -- the ID of the user whose vehicle recommendations
  should be returned.

Example:

```text
GET /recommendations?user_id=2
```

Rules:

- Recommendation `id` values contain a stable rule prefix and, when source
  events exist, an occurrence signature separated by `:`. The ID remains stable
  while the same source events keep a rule active and changes when new source
  events create a new occurrence.
- User-facing recommendation titles and messages are returned in Russian;
  calculated days, prices, expenses, and mileage remain part of the message.

- Empty history returns an informational recommendation to add the first vehicle
  record.
- No records for 14 or more days returns an informational history-update
  recommendation.
- Average fuel price for the latest three fuel records above 80 RUB/L returns a
  warning.
- Repair expenses above 20000 RUB during the last 30 days return a warning.
- A breakdown/problem record during the last 30 days returns a follow-up warning.
- More than 500 km since the latest fuel record with mileage returns a fuel-level
  reminder.

Response:

```json
{
  "recommendations": [
    {
      "id": "high_fuel_price:42-41-40",
      "severity": "warning",
      "title": "Высокая стоимость топлива",
      "message": "Средняя цена последних заправок — 100 ₽/л. Сравните цены на АЗС или проверьте введённые сумму и объём топлива.",
      "source": "sum(last_3_fuel.amount) / sum(last_3_fuel.fuel_liters) > 80"
    }
  ]
}
```
