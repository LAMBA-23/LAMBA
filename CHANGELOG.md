# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Backend registration endpoint (`POST /auth/register`) with username/password validation (#68, #72).
- Backend vehicle creation endpoint (`POST /vehicle`) with field validators (brand, model, production year, mileage) and upsert behavior (#67, #81).
- Chat-to-event parsing endpoint (`POST /chat/parse-event`) with Mistral API integration, clarification flow, and guardrails for ambiguous messages (#69, #75).
- Android `AddVehicleActivity` with vehicle profile form, client-side validation, and API integration (#45, #73).
- Android `RegisterActivity` with registration form connected to backend (#44, #66).
- `SessionManager` for persisting user session across app restarts.
- `ChatRepository` for structured chat-to-event persistence.
- Chat screen sends messages to backend and displays parsed events or clarification questions (#46, #79, #80).
- Vehicle card in `MainActivity` is clickable to open vehicle edit screen (#73).
- `MainActivity` loads user ID from `SessionManager` as fallback (#73).
- Backend tests for vehicle creation, auth, and chat parsing endpoints (#73, #81).
- User stories documentation with issue links (#60).
- Sprint-by-sprint roadmap (#64, #65).
- Customer interview transcript and summary (#76, #77).
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

## [0.1.0] - 2026-06-16

### Added

- MVP v1 Initial release setup.
