# Testing notes

## Day 2 BE2 events API testing draft

Scope:

- `GET /events`
- `POST /events`
- events API success cases
- events API validation and not-found cases

Out of scope:

- `/stats`
- CI setup
- deployment and release work
- frontend behavior

Checks covered by `backend/tests/test_events.py`:

- `GET /events` returns only events for the requested user.
- `GET /events` returns events in stable ascending ID order.
- `POST /events` saves a valid event.
- A saved event appears in the next `GET /events` response for the same user.
- Events created for one user do not appear in another user's timeline.
- Missing `amount` is stored as `0`.
- Missing `mileage` uses the user's current vehicle mileage.
- `condition` is accepted as an event type.
- Missing `user_id` returns validation error `422`.
- Unknown `user_id` returns not-found error `404`.
- Invalid event type returns validation error `422`.
- Empty event description returns validation error `422`.
- Negative `amount` returns validation error `422`.
- Negative `mileage` returns validation error `422`.

Expected verification command:

```bash
pytest tests/test_events.py
```

Current Day 2 result:

- Events API behavior is covered by focused unit tests.
- Events API input validation was tightened for empty descriptions and negative numeric values.
- The API contract was updated for the tested events behavior.
