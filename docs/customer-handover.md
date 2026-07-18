# Customer Handover

## Current product status and handover scope

The current product is **MVP v3** - an Android mobile application supported by a FastAPI backend, PostgreSQL database, Docker Compose deployment configuration, and AI integration. This handover document describes the actual state today, not an assumed transfer that has already happened.

The handover scope encompasses the **entire product ecosystem**, including:

* **Android Frontend:** Kotlin-based mobile application.
* **Backend Service:** FastAPI application.
* **Database:** PostgreSQL with migration configurations.
* **Deployment Configuration:** Docker Compose orchestration.
* **AI Integration:** DeepSeek-chat integration via Timeweb.
* **Documentation:** Technical specifications, user guides, and process history.

### Component Locations & Runtime

* **Product Repository:** `LAMBA` (contains all frontend, backend, deployment configs, and documentation).
* **Deployment Model:** Docker Compose on customer-controlled infrastructure.
* **Hosted Runtime:** `http://186.246.27.211:8000` (Deployed directly on the server provided and managed by the customer).
* **API Reference:** `http://186.246.27.211:8000/docs`.

## Handover Level and Transition Status

* **Current Handover Level:** **Ready for independent use**
* **Customer Confirmation Status:** **Accepted** (Final MVP v3 delivery)

### Status Context:

1. **Trial Validation:** The customer has executed a subset of UAT workflows during the customer meeting. Core features such as **chat history** and **session persistence** successfully passed validation.
2. **Follow-up Items:** Several customer-critical features require remediation during Week 7 scope before final acceptance.
3. **Scope Realignment:** Final acceptance criteria satisfied. This document describes the delivered **MVP v3** final version.
4. **Operational Status:** The application is running on the customer's hardware. Final handover completed with archive delivery and customer sign-off.

---

## What has been transferred, delegated, or retained

### Infrastructure & Deployment Ownership

* **Hosting Server:** Provided, owned, and controlled exclusively by the **customer**.
* **Deployment Management:** Fully transitioned to customer control upon final MVP v3 delivery and sign-off.

### Codebase & Administrative Rights

* **GitHub Repository Administration:** Intentionally **retained by the development team**. The customer explicitly stated that administrative rights to the team's GitHub repository are **not required**.
* **Public Access:** The customer does not require public access to the deployed product and does not require repository administrator rights. The course repository remains publicly readable under the course requirements.

---

## Agreed Final Handover Format

The final delivery mechanism has been explicitly aligned with the customer's request. Instead of repository transfers or cloud account migrations, the handover will consist of a **complete archive package** delivered via a **private storage channel** accessible to the customer.

### Delivery Package Components:

* Full source code archives for both **Android frontend** and **FastAPI backend**.
* Current **Docker Compose** deployment configurations.
* Step-by-step instructions for building the Android APK.
* Comprehensive, non-technical installation and deployment guides.
* Environment variable templates (excluding secret keys).
* Troubleshooting, backup, and system recovery playbooks.
* The complete set of maintained product and process documentation (including weekly reports) to enable seamless transition to any future engineering team.

> **Note:** The final handover archive package has been delivered to the customer via the agreed private storage channel.

---

## Access and use

### Hosted backend access

* **Base URL:** `http://186.246.27.211:8000`
* **Swagger UI:** `http://186.246.27.211:8000/docs`

### Authentication & Credentials

* **No Default/Demo Accounts:** In compliance with security requirements, hardcoded `demo/demo` credentials have been completely removed from the repository.
* **Access Method:** To test the application, register a new user directly via the Android mobile interface or use the `POST /auth/register` API endpoint.
* **Production Secrets:** Real production credentials and API tokens are never stored in Git and will be transferred to the customer via a separate, secure private channel.

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

The system depends on external AI infrastructure. The customer must supply the following environment variables in `docker-compose.yml` or local environment for full functionality:

- `DATABASE_URL` — PostgreSQL connection string for the backend.
- `TIMEWEB_API_KEY` — AI chat API key (Must be kept out of version control, provided via `.env`).
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

1. Register a new user via `POST /auth/register` or through the Android UI.
2. Test session functionality using `GET /vehicle?user_id=1` (or the corresponding active user ID).
3. Access the interactive Swagger UI directly at `/docs`.

## Main customer-facing documentation

The customer should use these primary entry points for normal use, operation, and troubleshooting:

- `README.md` — quick start, smoke checks, and release overview
- `docs/api-contract.md` — backend API contract and endpoint behavior
- `docs/testing.md` — current verification status and CI checks
- `docs/customer-handover.md` — current handover scope and status

## Documentation sufficiency and support needs

* **Current State:** The existing documentation is sufficient for technical verification, running local smoke tests, and reviewing the API contract.
* **Target State:** It is **insufficient** for the requested non-technical handover package. 
* **Required Adjustments:** Before final handover, the team must expand the documentation to include clear, highly detailed, non-technical instructions covering end-to-end usage, deployment, backup/recovery, and system troubleshooting for future engineering teams.

---

## Known limitations and risks

- Android delivery is limited to a debug APK artifact, not a published app.
- AI chat features depend on external Timeweb/DeepSeek credentials.
- Monitoring and support processes are limited to health checks and logs.

## Completed actions

- [x] Complete customer-critical Week 7 feature scope and fixes.
- [x] Verify all updated User Acceptance Testing (UAT) scenarios.
- [x] Expand documentation into a comprehensive, non-technical handover package (Usage, Deployment, Backup, Troubleshooting).
- [x] Prepare clean frontend, backend, and documentation archives, ensuring zero leakage of production secrets.
- [x] Deliver the archive packages to the customer via the designated private storage channel.
- [x] Transfer production environment secrets securely via a separate channel.
- [x] Request final customer sign-off on this handover document (`docs/customer-handover.md`) to verify it meets the agreed level of transfer.
- [x] Update Handover Level to `Ready for independent use` and Status to `Accepted` upon final confirmation.

## Known limitations

- Android delivery is limited to a debug APK artifact, not a published app.
- AI chat features depend on external Timeweb/DeepSeek credentials.
- Monitoring and support processes are limited to health checks and logs.

## Final delivery evidence

- **MVP v3 Release:** [v1.4.0 - Final Course Version](https://github.com/LAMBA-23/LAMBA/releases/tag/v1.4.0)
- **Week 7 Report:** [reports/week7/README.md](./reports/week7/README.md)
- **Deployed Backend:** `http://186.246.27.211:8000`
- **Swagger UI:** `http://186.246.27.211:8000/docs`
- **Customer archives:** Delivered via private storage channel
- **Demo video:** [Link to sanitized demo video](https://drive.google.com/drive/folders/1JG9VJTtcu_bDr9Q3AGSfieljdY5r3g4y?usp=sharing)

## Related customer-relevant documentation

- `README.md`
- `docs/api-contract.md`
- `docs/testing.md`
- `docs/quality-requirements.md`
- `docs/quality-requirement-tests.md`