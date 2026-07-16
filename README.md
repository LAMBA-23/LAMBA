# LAMBA — Digital Twin of a Car

LAMBA is an Android application for maintaining a digital twin of a car. It allows a vehicle owner to store vehicle information, record trips, refueling, repairs, and breakdowns, review the vehicle history and statistics, and interact with an AI assistant through a conversational interface.

The current public increment is **MVP v3 / Assignment 6 Sprint 4 Trial Release**.

## Current Product Access

- [GitHub Releases](https://github.com/LAMBA-23/LAMBA/releases)
- [Hosted documentation site](https://lamba-23.github.io/LAMBA/)
- Deployed backend API: `http://186.246.27.211:8000`
- Deployed Swagger UI: `http://186.246.27.211:8000/docs`
- [Local setup instructions](#local-setup)
- [Customer handover guidance](docs/customer-handover.md)

Demo credentials are not published. Create a new account through the Android application or the `/auth/register` API endpoint.

## Product Scope

The current product supports:

- user registration, login, logout, and session restoration;
- vehicle profile setup;
- manual vehicle-history records;
- trip records with start and end odometer values;
- fuel records with decimal liter values;
- repair and breakdown records;
- chat-assisted vehicle-history records;
- vehicle timeline;
- mileage, expense, fuel, and record statistics;
- local storage of recent chat history;
- backend API documentation through Swagger UI.

For planned improvements and remaining work, see the [project roadmap](docs/roadmap.md).

## Technology Stack

- **Android application:** Kotlin, Gradle, XML layouts, Retrofit
- **Backend:** Python, FastAPI, SQLAlchemy, Pydantic
- **Database:** PostgreSQL
- **Deployment:** Docker Compose
- **Documentation:** GitHub Pages
- **Continuous integration:** GitHub Actions

## Local Setup

### Backend and PostgreSQL

Requirements:

- Docker
- Docker Compose

From the repository root, start the backend and database:

```bash
docker compose up --build
```

After startup, the following services will be available:

- Backend API: `http://localhost:8000`
- Swagger UI: `http://localhost:8000/docs`

For AI-backed chat functionality, provide the required environment variables:

```text
TIMEWEB_API_KEY
TIMEWEB_AGENT_ID
```

For optional voice input, configure one or more Mistral keys only for the backend.
Keys are comma-separated; when Mistral reports a quota or rate limit, the backend
tries the next key. Do not put these keys in the Android application or commit them.

```text
MISTRAL_API_KEYS
MISTRAL_TRANSCRIPTION_MODEL
MISTRAL_CLEANUP_MODEL
CHAT_TRANSCRIPTION_MAX_BYTES
CHAT_TRANSCRIPTION_RATE_LIMIT
CHAT_TRANSCRIPTION_RATE_LIMIT_WINDOW_SECONDS
```

By default, a voice upload is limited to 5 MiB and each client IP can submit 10
transcriptions per 60-second window. These limits can be changed only through
the backend environment variables above.

Do not commit API keys, credentials, or `.env` files to the repository.

Event photos use the persistent Docker volume by default. The default backend
configuration is:

```text
PHOTO_STORAGE_BACKEND=local
EVENT_PHOTO_DIR=/app/uploads/event_photos
```

For a private S3-compatible bucket, set `PHOTO_STORAGE_BACKEND=s3` and provide:

```text
PHOTO_S3_BUCKET
PHOTO_S3_ENDPOINT
PHOTO_S3_REGION
PHOTO_S3_PREFIX
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
```

The bucket must remain private. Photo bytes are returned only through the
owner-checked event photo API; the backend does not publish storage object URLs.

Create a user through the Android application or through the API:

```bash
curl -X POST http://localhost:8000/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"test-user\",\"password\":\"test-pass123\"}"
```

Check that the backend is running:

```bash
curl http://localhost:8000/health
```

Stop the services:

```bash
docker compose down
```

Detailed setup, recovery, configuration, and verification guidance is available in the [customer handover document](docs/customer-handover.md).

### Android Application

Build a debug APK from the repository root.

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

On macOS or Linux:

```bash
./gradlew assembleDebug
```

After a successful build, the APK will be available at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Verification

Run the backend test suite:

```bash
docker compose run --rm backend pytest tests -q
```

Run Android unit tests:

```powershell
.\gradlew.bat testDebugUnitTest
```

The repository also uses GitHub Actions for:

- backend linting and formatting checks;
- backend automated tests and coverage;
- Android unit tests;
- Markdown link checking;
- hosted documentation publishing.

See the [testing overview](docs/testing.md) for the current verification status and test evidence.

## Documentation

### Customer and Product Documentation

- [Hosted documentation site](https://lamba-23.github.io/LAMBA/)
- [Customer handover](docs/customer-handover.md)
- [API contract](docs/api-contract.md)
- [Architecture overview](docs/architecture/README.md)
- [Testing overview](docs/testing.md)
- [Quality requirements](docs/quality-requirements.md)
- [Quality requirement tests](docs/quality-requirement-tests.md)
- [User acceptance tests](docs/user-acceptance-tests.md)
- [Roadmap](docs/roadmap.md)

### Development and Repository Guidance

- [Contribution guidelines](CONTRIBUTING.md)
- [Agent guidance](AGENTS.md)
- [Development process](docs/development-process.md)
- [Definition of Done](docs/definition-of-done.md)
- [Changelog](CHANGELOG.md)

### Assignment Reports

- [Week 6 report](reports/week6/README.md)
- [Week 5 report](reports/week5/README.md)
- [Week 4 report](reports/week4/README.md)
- [Week 3 report](reports/week3/README.md)
- [Week 2 report](reports/week2/README.md)

## Security and Public Artifacts

Do not commit:

- API keys, tokens, passwords, or other credentials;
- `.env` files or local secret stores;
- private customer access information;
- private recordings or customer-identifying evidence;
- artifacts containing confidential or personal information.

Only sanitized public evidence and documentation should be stored in the repository.
