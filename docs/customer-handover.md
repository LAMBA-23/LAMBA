# Customer Handover

## Current product status and handover scope

The current product state is a runnable backend application with documented local deployment and a documented hosted endpoint. This handover document describes the actual state today, not an assumed transfer that has already happened.

- Product repository: `LAMBA` contains the backend service, database deployment configuration, API contract, and verification documentation.
- Deployment model: Docker Compose with PostgreSQL and FastAPI.
- Hosted runtime documented in the repository: `http://186.246.27.211:8000`.
- Customer-facing API reference documented in the repository: `http://186.246.27.211:8000/docs`.

Current handover status: **Not yet at `Ready for independent use`**

Reason: no customer access, deployment ownership, hosting account ownership, or repository administration has been transferred yet. The product and documentation exist, but formal handover has not been completed.

## What has been transferred, delegated, or retained

Transferred/delegated to the customer:

- No customer-facing transfer has been completed yet.
- The repository contains instructions for local setup and verification, but those instructions have not been formally handed over as a completed customer transition.

Intentionally retained by the team:

- Hosted backend infrastructure and deployment account.
- GitHub repository administration and branch management.
- Mobile app distribution channel and production APK publication.

## Access and use

### Hosted backend access

- Backend base URL: `http://186.246.27.211:8000`
- Swagger/API docs: `http://186.246.27.211:8000/docs`
- Demo login:
  - username: `demo`
  - password: `demo`

### Local use

Run from the repository root:

```bash
docker compose up --build
```

After startup:

- Backend: `http://localhost:8000`
- Swagger UI: `http://localhost:8000/docs`

### Android artifact

- Local debug APK path: `app/build/outputs/apk/debug/app-debug.apk`
- Note: no production app-store package is published in this repository.

## Required configuration and secrets handling

The backend requires the following environment variables in `docker-compose.yml` or local environment:

- `DATABASE_URL` — PostgreSQL connection string for the backend.
- `TIMEWEB_API_KEY` — AI chat API key.
- `TIMEWEB_AGENT_ID` — AI chat agent identifier.
- `TIMEWEB_MODEL` — optional model name, default `deepseek-chat`.
- `TIMEWEB_TIMEOUT_SECONDS` — optional request timeout, default `20`.

Current local defaults provided by `docker-compose.yml`:

- `DATABASE_URL=postgresql+psycopg2://lamba:lamba@db:5432/lamba`
- `TIMEWEB_AGENT_ID` has a default value set in the compose file.

Important secrets-handling guidance:

- Do not commit `TIMEWEB_API_KEY` or any secret values to Git.
- Store API keys in environment variables or a local `.env` file excluded from version control.
- The customer must supply valid `TIMEWEB_API_KEY` and `TIMEWEB_AGENT_ID` if AI-backed chat features are required.
- Without `TIMEWEB_API_KEY`, chat-related endpoints cannot call the external AI service.

## Setup, deployment, recovery, and verification steps

### Local deployment

1. Install Docker and Docker Compose.
2. Open the repository root.
3. Run:

   ```bash
docker compose up --build
```

4. Wait until both services become healthy.

### Recovery

If services stop:

```bash
docker compose down
docker compose up --build
```

If startup fails:

```bash
docker compose logs backend
docker compose logs db
```

### Verification

Verify the running product with:

```bash
curl http://localhost:8000/health
curl http://localhost:8000/docs
```

Then verify functionality:

- `POST /auth/login` with `demo` / `demo`
- `GET /vehicle?user_id=1`
- Access Swagger UI at `/docs`

## Main customer-facing documentation

The customer should use these primary entry points for normal use, operation, and troubleshooting:

- `README.md` — quick start, smoke checks, and release overview
- `docs/api-contract.md` — backend API contract and endpoint behavior
- `docs/testing.md` — current verification status and CI checks
- `docs/customer-handover.md` — current handover scope and status

## Documentation sufficiency and support needs

This documentation set is sufficient for the customer to evaluate the current backed runtime, run it locally, and verify basic use cases. It is not sufficient for a full customer-managed production deployment because the hosting account and deployment ownership remain team-retained.

Team support still required for:

- hosted backend availability and production endpoint management
- AI chat credential provisioning and secret handling
- Android app distribution beyond the debug APK

## Known limitations and risks

- Hosted backend is team-hosted and not transferred to a customer-owned account.
- No customer-managed deployment or cloud account is documented.
- Android delivery is limited to a debug APK artifact, not a published app.
- AI chat features depend on external Timeweb/DeepSeek credentials.
- Monitoring and support processes are limited to health checks and logs.

## Remaining actions

- Transfer production deployment and hosting account to the customer if full infrastructure handover is required.
- Provide explicit customer GitHub access or a repository transfer path.
- Publish or distribute the Android app artifact through customer-approved channels.
- Add customer-side monitoring and incident response documentation.

## Related customer-relevant documentation

- `README.md`
- `docs/api-contract.md`
- `docs/testing.md`
- `docs/quality-requirements.md`
- `docs/quality-requirement-tests.md`
