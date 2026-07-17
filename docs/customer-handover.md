# Customer Handover

## Current product status and handover scope

The current product is an Android mobile application supported by a FastAPI backend, PostgreSQL database, Docker Compose deployment configuration, and AI integration. This handover document describes the actual state today, not an assumed transfer that has already happened.

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

* **Current Handover Level:** **Not yet confirmed as `Ready for independent use`**
* **Customer Confirmation Status:** **Not yet accepted for final Week 7 handover**

### Status Context:

1. **Trial Validation:** The customer executed a subset of UAT workflows during the Week 6 customer meeting. Core features such as **chat history** and **session persistence** passed validation.
2. **Follow-up Items:** Week 7 follow-up work has been merged into `main` for the previously reported blockers, including decimal numeric values, trip start/end odometer flow, breakdown photos, recommendations, notifications, profile improvements, voice input, and data export.
3. **Release Status:** The latest tagged public release remains `v1.3.0`; the Week 7 follow-up state is currently represented by reviewed merges to `main`, not by a newer SemVer release.
4. **Final Confirmation:** The repository does not yet contain inspectable evidence that the customer accepted the final Week 7 handover document or independently used the final product state.
5. **Operational Status:** The application is already running on the customer's hardware, but the team retains temporary technical responsibility for managing and updating the deployment until final sign-off.

---

## What has been transferred, delegated, or retained

### Infrastructure & Deployment Ownership

* **Hosting Server:** Provided, owned, and controlled exclusively by the **customer**.
* **Deployment Management:** Temporarily maintained by the development team for updates, to be fully transitioned upon archive delivery and customer's confirmation.

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

> **Note:** The final handover format is fully agreed upon, but the repository does
> not yet record final customer confirmation that the archive package and this
> handover document are accepted.

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

* **Current State:** The existing documentation is sufficient for technical verification, running local smoke tests, reviewing the API contract, and finding the main handover status.
* **Target State:** Final acceptance still requires customer confirmation that this handover document is sufficient for the reached transition scope.
* **Required Adjustments:** Before marking the handover accepted, the team must preserve inspectable evidence that customer confirmation was requested and record the customer's response or non-response.

---

## Known limitations and risks

- Android delivery is limited to a debug APK artifact, not a published app.
- AI chat features depend on external Timeweb/DeepSeek credentials.
- Monitoring and support processes are limited to health checks and logs.
- The latest tagged release is still `v1.3.0`; later Week 7 work is present on
  `main` but is not yet represented by a newer SemVer release.
- Final customer confirmation for the Week 7 transition outcome is not recorded
  in the repository.

## Remaining actions


- [x] Complete the currently merged Week 7 follow-up feature and fix scope recorded in GitHub issues and pull requests.
- [ ] Verify all updated User Acceptance Testing (UAT) scenarios with final Week 7 evidence.
- [ ] Publish or tag a newer final SemVer release if the team decides the current `main` state is the final product access artifact.
- [ ] Prepare clean frontend, backend, and documentation archives, ensuring zero leakage of production secrets.
- [ ] Deliver the archive packages to the customer via the designated private storage channel.
- [ ] Transfer production environment secrets securely via a separate channel.
- [ ] Request final customer sign-off on this handover document (`docs/customer-handover.md`) to verify it meets the agreed level of transfer.
- [ ] Update Handover Level and Customer Confirmation Status only after final confirmation evidence is available.


## Related customer-relevant documentation

- `README.md`
- `docs/api-contract.md`
- `docs/testing.md`
- `docs/quality-requirements.md`
- `docs/quality-requirement-tests.md`
- `reports/week7/README.md`
