# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Issue templates for User Stories, PBIs, Bug Reports, and Course Tasks.
- Pull Request template with acceptance criteria and changelog enforcement.
- Definition of Done (`docs/definition-of-done.md`) and Roadmap (`docs/roadmap.md`).
- US-02: Vehicle creation with validation (brand, model, production year, mileage).
- US-02: One-vehicle-per-user constraint (409 on duplicate).
- US-02: Android `AddVehicleActivity` with form, client-side validation, and API integration.
- Backend tests for vehicle and auth endpoints.
- Chat-to-event parsing: AI-assisted conversion of Russian chat messages into structured vehicle events via Mistral API (`POST /chat/parse-event`).

## [0.1.0] - 2026-06-16

### Added

- MVP v1 Initial release setup.
