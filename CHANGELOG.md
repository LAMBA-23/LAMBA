# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Android dark theme switch in the profile screen; the selected appearance is
  saved locally and restored when the app opens.
- Dedicated Android profile screen with read-only username, vehicle settings,
  password change, and logout (#329).
- Backend vehicle-update and password-change endpoints with history-based
  mileage protection (#329).
- Backend recommendations endpoint with rule-based vehicle notifications for fuel
  price, repair cost, stale records, recent breakdowns, and fuel-level reminders
  (#315).
- Android notification icon now opens a full-screen in-app notification inbox
  with recommendation cards, loading/error/empty states, and a per-user unread
  red-dot indicator.

### Changed

- AI chat assistant now responds to greetings and thanks with friendly, emoji-rich messages instead of asking for event details (#316).
- Android manual trip records now use start and end odometer values instead of
  a single mileage field.

### Fixed

- Android trip history displays traveled distance instead of treating the saved
  trip odometer value as the trip distance.
- Breakdown photo selections are kept with the saved history record and shown
  again from record details/editing.

## [1.3.0] - 2026-07-12

### Added

- Password hashing and secure auth in backend (#265)
- Local chat history persistence for last 5 dialogs (#277)
- Application launcher icon (#242)
- Android side drawer account block opens the profile screen; logout remains
  available there with local session cleanup (#329).

### Changed

- Backend login rate limiting and restricted CORS (#265)
- Statistics UI layout adjusted for adaptive display (#278)
- Session restore improved after app restart (#243)

### Fixed

- Frontend layout issues on statistics screen (#278)

## [1.2.0] - 2026-07-05

### Added

- Android manual event creation form in History screen for adding fuel, repair, maintenance, and trip records without using the AI chat (#161, #205, #208).

### Changed

- Main screen buttons updated for improved usability (#196).
- Statistics screen expenses field renamed to fuel refueling for clarity (#199).
- Non-event chat messages removed from the vehicle timeline so only structured records appear (#202).

### Fixed

- Statistics API now returns all-time mileage as the current odometer value while keeping week and month mileage limited to trip distance inside the selected period (#198, #204).
- Backend statistics now return `fuel_liters` separately from fuel expenses, keep fuel and repair expenses split, and count all period events in `records_count` (#198, #204).
- Frontend display issues on the main screen resolved (#194).
- Chat event parsing and persistence now carry `fuel_liters` from fuel messages such as `заправилась на 10 литров` and keep rubles in `amount`.
- Non-trip events without explicit mileage no longer fall back to stale initial mileage in chat confirmations; Android hides the mileage row when backend returns `0`.
- Android statistics period switching now renders the selected backend period (`week`, `month`, `all_time`) instead of reusing top-level totals.
- Backend statistics now calculate trip mileage as traveled distance deltas instead of summing trip odometer values.
- Backend trip event persistence now stores a new odometer mileage when a trip is submitted as a traveled distance, while keeping timeline responses compatible.
- Backend chat parsing now recognizes short trip-distance phrases such as `поездка 100 километров` and `проехал 100 км` more reliably.

## [1.1.0] - 2026-06-28

### Added

- Android history/timeline screen for viewing saved vehicle events from `GET /events` (#48).
- Android statistics screen connected to backend `GET /stats` with week, month, and all-time vehicle summaries (#50).
- Backend assistant question endpoint (`POST /chat/ask`) with vehicle and recent-event context for DeepSeek/Timeweb responses (#49).
- User acceptance test scenarios and Week 4 execution results for registration, assistant usage, history/event creation, and statistics.
- Automated quality requirement tests for event data integrity, timeline response time, and backend regression testability.
- Assignment 4 release and deployment preparation documentation for the Sprint 2 increment.

### Changed

- Timeweb/DeepSeek chat integration now reports missing configuration and upstream failures without crashing the backend.
- Backend event and statistics tests now cover Sprint 2 history and statistics behavior.

## [0.1.0] - 2026-06-21

### Added

- Backend registration endpoint (`POST /auth/register`) with username/password validation (#68, #72).
- Backend vehicle creation endpoint (`POST /vehicle`) with field validators (brand, model, production year, mileage) and upsert behavior (#67, #81).
- Chat-to-event parsing endpoint (`POST /chat/parse-event`) with Mistral API integration, clarification flow, and guardrails for ambiguous messages (#69, #75).
- Automatic vehicle records from chat messages connected to logged-in user with technical condition event support (#47, #83).
- Android `AddVehicleActivity` with vehicle profile form, client-side validation, and API integration (#45, #73).
- Android `RegisterActivity` with registration form connected to backend (#44, #66).
- Android onboarding flow connected to backend (welcome, login, register, vehicle setup) (#71, #78).
- `SessionManager` for persisting user session across app restarts.
- `ChatRepository` for structured chat-to-event persistence.
- Chat screen sends messages to backend and displays parsed events or clarification questions (#46, #79, #80).
- Vehicle card in `MainActivity` is clickable to open vehicle edit screen (#73).
- `MainActivity` loads user ID from `SessionManager` as fallback (#73).
- Backend tests for vehicle creation, auth, and chat parsing endpoints (#73, #81).
- Android unit tests for `ChatRepository` (#83).
- User stories documentation with issue links (#60).
- Sprint-by-sprint roadmap (#64, #65).
- Customer interview transcript and summary (#76, #77).
- Week 3 reflection report (#84, #85).
- Week 3 retrospective (#86, #87).
- Issue templates for User Stories, PBIs, Bug Reports, and Course Tasks.
- Pull Request template with acceptance criteria and changelog enforcement.
- Definition of Done (`docs/definition-of-done.md`).

### Changed

- Vehicle endpoint updates existing default car instead of rejecting with 409 (#81).
- Removed hardcoded demo data from backend endpoints (#72).
- Android `GET /vehicle` uses query parameter instead of path parameter to match backend contract (#73).
- Chat parsing system prompt enforces single-event-per-message and clarification for ambiguous inputs (#69, #75).

### Fixed

- Android `ChatActivity` escaped Unicode strings replaced with readable Cyrillic (#75).
- Merge conflicts resolved across multiple PRs (#73, #81).
- API documentation updated to reflect current endpoint contracts (#73, #81).
