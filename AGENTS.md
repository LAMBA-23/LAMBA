# AGENTS.md

## Purpose

This file defines how team members and AI agents must work in the LAMBA repository.

The goal is to avoid blocked work, random commits, duplicated implementation, and unfinished MVP integration.

The project language is Russian. User-facing UI text, demo data, screenshots, and MVP demo flow must be in Russian.

---

## Project baseline

LAMBA MVP is not only Android screens.

The MVP v0 must show a small working technical foundation:

```text
Android app -> FastAPI backend -> PostgreSQL storage -> backend response -> UI update
```

For Week 2, the main smoke-check scenario is:

```text
login -> История -> добавить событие -> событие сохранилось -> статистика обновилась
```

Do not add side features until this scenario works.

---

## Current technical decisions

* Mobile app: Android Kotlin.
* Backend: Python FastAPI.
* Storage: PostgreSQL.
* AI provider: Mistral API.
* AI API keys must be stored only on backend in environment variables.
* Android must never call Mistral directly.
* Android communicates only with FastAPI backend.
* Backend communicates with PostgreSQL and Mistral.
* Main API contract file: `docs/api-contract.md`.
* Main MVP report file: `reports/week2/mvp-v0-report.md`.

If repository paths change, update this file before starting work.

---

## Team responsibilities

### @lisa_va_si — Vasilisa Tumakina

Role: Team Lead, integration QA, smoke-check, video demo, Moodle PDF.

MVP responsibilities:

* Coordinate MVP scope.
* Check that all MVP parts connect together.
* Run the final smoke-check.
* Record MVP video demo.
* Prepare Moodle PDF access instructions.
* Check that links and instructions work for another person.

Assignment responsibilities:

* Final review of Week 2 reports.
* `reports/week2/analysis.md`.
* `reports/week2/llm-report.md`.
* Moodle PDF.

---

### @Elisaveta_Ionina — Elizaveta Ionina

Role: Frontend / Android and repository setup.

MVP responsibilities:

* Android project skeleton.
* Demo login screen.
* Main app navigation.
* Tabs: `Чат`, `Автомобиль`, `История`, `Профиль`.
* Basic connection points for backend integration.

Repository responsibilities:

* Public GitHub repo setup.
* MIT `LICENSE`.
* Root `README.md`.
* `.gitignore`.
* `reports/week2/` structure.
* `reports/week2/user-stories.md`.
* PR template.
* Branch protection if access allows.

---

### @MariaLucherino — Maria Chizhikova

Role: UI/UX Designer.

MVP responsibilities:

* Design only.
* Russian UI screens.
* Login screen.
* Main tabs design.
* Timeline / История design.
* Add-event form design.
* Chat error state.
* Screenshots for reports.

Maria is not responsible for backend implementation or Android implementation. Other team members implement MVP according to her design.

Assignment responsibilities:

* Prototype / Figma.
* View-only prototype link.
* Screenshots for `reports/week2/images/`.

---

### @AaMiirRa — Amira Khaliullova

Role: Backend Developer.

MVP responsibilities:

* FastAPI backend.
* PostgreSQL connection.
* Database models / schema.
* API endpoints:

  * `GET /health`
  * `POST /auth/login`
  * `GET /vehicle`
  * `GET /events`
  * `POST /events`
  * `GET /stats`
  * `POST /chat`
* Backend run instructions.
* Backend deploy or runnable artifact.
* `.env.example` without secrets.

Assignment responsibilities:

* Backend section for MVP v0 report.
* Deployment / runnable backend instructions.
* Help with smoke-check evidence.

---

### @yyarunit — Ivan Grishaev

Role: AI Engineer.

MVP responsibilities:

* Mistral API integration through backend only.
* AI service layer for FastAPI.
* Prompt for recognizing car events from Russian user messages.
* Chat endpoint logic together with backend.
* Error handling if Mistral key is missing or invalid.
* Structured AI output for backend, but normal Russian text for user.

Important:

* Never commit real Mistral API keys.
* Never put AI keys into Android.
* Use `.env` locally and `.env.example` in repository.
* If Mistral is not available, implement clear backend error response and document the limitation.

Assignment responsibilities:

* AI/LLM usage notes for `reports/week2/llm-report.md`.
* Short explanation of what AI does in MVP v0.

---

## Branch workflow

Every task must be done in a separate branch.

Never work directly in `main` after initial repository setup.

Branch naming:

```text
feat/android-login
feat/android-history-form
feat/backend-events-api
feat/backend-chat-mistral
feat/docs-user-stories
fix/backend-health
docs/mvp-v0-report
```

Before starting work:

```bash
git checkout main
git pull
git checkout -b <branch-name>
```

Before opening a PR:

```bash
git status
git add .
git commit -m "<clear message>"
git push origin <branch-name>
```

---

## Pull Request rules

Every change must go through PR.

Each PR must include:

```md
## Summary
What was changed?

## Testing
How was it checked?

## Screenshots / Evidence
Add screenshots, command output, or video if relevant.

## Dependencies
Does this PR depend on another PR or task?

## Risks / Limitations
What is not done yet?
```

Rules:

* The author cannot be the only reviewer.
* Any teammate except the author may review.
* Do not merge broken code.
* Do not merge code that cannot be run or checked.
* Do not mix unrelated tasks in one PR.
* Documentation changes also require PR.

---

## Dependency workflow

No one should wait silently for another person.

Before starting any task, check dependencies.

A dependency can be:

* API contract not written.
* Backend endpoint not implemented.
* Design screen missing.
* Environment variable missing.
* Database schema not ready.
* Previous PR not merged.

If a dependency is missing, write this in the task/PR/chat:

```text
BLOCKED BY:
- <what is missing>
- <who owns it>

INDEPENDENT WORK AVAILABLE:
- <what I can do now without waiting>
```

Then continue with independent work.

---

## API contract rule

Backend and Android must not guess each other's data formats.

Before Android integration, backend owner must create or update:

```text
docs/api-contract.md
```

This file must include:

* endpoint path;
* HTTP method;
* request JSON;
* response JSON;
* error response;
* short description.

Example:

```md
### POST /events

Creates a vehicle event.

Request:
{
  "type": "refueling",
  "date": "2026-06-12",
  "mileage": 82000,
  "cost": 3000,
  "description": "Заправка 40 литров"
}

Response:
{
  "id": 1,
  "type": "refueling",
  "status": "saved"
}
```

Android developers may start from this contract even if the backend is not fully ready.

---

## What to do if another part is not ready

### Case 1: Android needs backend, but backend is not ready

Do not wait.

Android developer must:

* create API interface according to `docs/api-contract.md`;
* use fake/mock data locally;
* build UI and state handling;
* leave backend URL configurable;
* later replace mock repository with real API calls.

Output message:

```text
Backend endpoint is not ready yet.
I will continue with Android API interface and mock repository.
When backend is ready, I will switch the repository to real API.
```

---

### Case 2: Backend needs database, but PostgreSQL is not ready

Do not stop all backend work.

Backend developer must:

* define database models;
* prepare `docker-compose.yml` if needed;
* create repository/service layer;
* make `/health` work first;
* document what is blocked.

Output message:

```text
PostgreSQL setup is not ready yet.
I will implement FastAPI routes and service structure first.
Database connection will be plugged in after PostgreSQL config is ready.
```

---

### Case 3: AI engineer needs Mistral key, but key is not available

Do not put fake secrets in code.

AI engineer must:

* create AI service interface;
* define `.env.example`;
* implement safe error handling;
* document required variable name;
* optionally use local development stub only if clearly marked.

Output message:

```text
Mistral API key is not available yet.
I will implement the backend AI service interface and error handling.
Real Mistral calls will work after MISTRAL_API_KEY is provided in .env.
```

---

### Case 4: Developer needs design, but design is not finished

Do not wait.

Developer must:

* implement functional layout with simple UI;
* use Russian text;
* keep components easy to restyle;
* update UI after design is available.

Output message:

```text
Final design is not ready yet.
I will implement the functional screen with simple layout.
After design is ready, I will adjust styling without changing business logic.
```

---

## MVP v0 scope

The MVP v0 must implement only the minimum working flow.

Required:

* Android app starts.
* Demo login works.
* Main tabs are visible.
* Backend `/health` works.
* Backend connects to PostgreSQL.
* User can add an event in `История`.
* Event is saved through backend.
* Event appears in timeline.
* Statistics are updated.
* Chat endpoint exists.
* Mistral integration is connected through backend if key is available.
* If Mistral is unavailable, the app/backend must show a clear Russian error.

Not required for Week 2:

* Full registration.
* Multiple cars.
* Offline synchronization.
* Edit/delete events.
* Full AI prediction model.
* OBD-II integration.
* Perfect UI.
* Production security.
* Complete ML model.
* DeepSeek integration.

---

## MVP smoke-check

Final MVP smoke-check:

```text
1. Start backend.
2. Open GET /health.
Expected: backend returns ok.

3. Open Android app.
Expected: login screen is visible.

4. Login with demo credentials.
Expected: main screen opens.

5. Open “История”.
Expected: timeline or empty state is visible.

6. Click “+” / add event.
Expected: event form opens.

7. Add event:
   Тип: Заправка
   Дата: сегодня
   Пробег: 82000
   Стоимость: 3000
   Описание: Заправка 40 литров

8. Save event.
Expected: Android sends request to backend.

9. Return to “История”.
Expected: new refueling event is visible.

10. Open statistics block.
Expected: fuel expenses include 3000 ₽.

11. Open “Чат”.
Expected: chat screen is visible.

12. Send a simple message.
Expected:
- If Mistral is configured, assistant answers in Russian.
- If Mistral is not configured, user sees clear Russian error.
```

This scenario must be documented in:

```text
reports/week2/mvp-v0-report.md
```

---

## Backend requirements

Backend must provide:

```text
GET  /health
POST /auth/login
GET  /vehicle
GET  /events
POST /events
GET  /stats
POST /chat
```

Backend must include:

* FastAPI app.
* PostgreSQL connection.
* environment configuration.
* `.env.example`.
* safe handling of missing secrets.
* CORS configuration for Android/dev access if needed.
* run instructions.

Backend must not include:

* real API keys;
* real passwords;
* private customer data;
* raw recordings;
* test credentials that should not be public.

Demo credentials are allowed only if they are intended for public demo.

---

## Android requirements

Android must include:

* Kotlin app.
* Login screen.
* Main navigation.
* Tabs:

  * `Чат`
  * `Автомобиль`
  * `История`
  * `Профиль`
* Add-event form in `История`.
* Timeline display.
* Statistics display.
* API client layer.
* Configurable backend base URL.
* Error states in Russian.

Android must not include:

* Mistral API key.
* Backend database credentials.
* Hardcoded private secrets.
* Direct PostgreSQL access.
* Direct Mistral access.

---

## AI / Mistral requirements

Mistral integration must be backend-only.

Correct flow:

```text
Android -> FastAPI /chat -> Mistral API -> FastAPI response -> Android
```

Wrong flow:

```text
Android -> Mistral API
```

The AI service must:

* accept Russian user messages;
* return Russian user-facing response;
* avoid inventing missing data;
* ask clarification if data is missing;
* identify event type when possible;
* return structured data internally if needed;
* handle missing or invalid API key safely.

Required environment variable:

```text
MISTRAL_API_KEY=
```

This must be shown only in `.env.example` without real value.

---

## Documentation responsibilities

Required Week 2 docs:

```text
reports/week2/README.md
reports/week2/user-stories.md
reports/week2/mvp-v0-report.md
reports/week2/customer-meeting-summary.md
reports/week2/analysis.md
reports/week2/llm-report.md
reports/week2/images/
```

Root README must include:

* project description;
* setup instructions;
* Android run instructions;
* backend run instructions;
* MVP link or runnable artifact;
* report links.

---

## LLM / AI usage rules

AI tools may be used, but the team must disclose usage.

Whenever a team member uses AI for coding, writing, design, research, or debugging, they must record:

```text
Tool:
Used for:
What was accepted:
What was changed manually:
How result was verified:
```

Add final summary to:

```text
reports/week2/llm-report.md
```

AI-generated code must be reviewed by a human before merge.

AI agents must not:

* invent completed work;
* create fake links;
* create fake test results;
* commit secrets;
* change unrelated files;
* ignore failing tests;
* silently rewrite large parts of the project.

---

## Rules for AI coding agents

Before editing files, an AI agent must read:

1. `AGENTS.md`
2. `README.md`
3. `docs/api-contract.md` if the task touches API
4. related files for the task

The agent must first identify:

```text
Task:
Owner:
Files likely to change:
Dependencies:
Independent work possible:
Testing plan:
```

The agent must not modify files outside the task scope unless explicitly asked.

The agent must keep changes small and reviewable.

If blocked, the agent must not stop with only "blocked". It must propose independent work.

Required blocked format:

```text
BLOCKED BY:
- ...

CAN CONTINUE WITH:
- ...

NEEDS FROM TEAM:
- ...
```

---

## Definition of Done for a task

A task is done only when:

* code/docs are committed to a task branch;
* PR is opened;
* PR description is filled;
* testing is described;
* reviewer is assigned;
* screenshots/logs are attached if relevant;
* no secrets are committed;
* task does not break MVP smoke-check.

---

## Definition of Done for MVP v0

MVP v0 is done only when:

* Android app can be opened or installed.
* Backend can be started or accessed.
* PostgreSQL/storage works.
* Login works.
* Event creation works.
* Timeline updates.
* Statistics update.
* Smoke-check is written.
* Video demo is recorded.
* MVP access instructions are clear.
* Links are available in root README and Week 2 report.

---

## Daily status format

Each team member must send status in this format:

```text
Done:
- ...

Doing now:
- ...

Blocked:
- ...

Need from others:
- ...

PR / branch:
- ...
```

If a person is blocked for more than 30 minutes, they must write it in the team chat.

---

## Scope control

Do not work on features outside MVP smoke-check unless approved by the team lead.

Forbidden side quests before MVP smoke-check works:

* extra animations;
* complex profile settings;
* multiple cars;
* advanced charts;
* full registration;
* offline sync;
* edit/delete events;
* new design direction;
* unnecessary refactoring;
* DeepSeek integration;
* OBD-II integration.

Priority order:

```text
1. Working MVP smoke-check
2. Repository/report requirements
3. Customer review materials
4. UI polish
5. Extra features
```

---

## Final rule

If there is confusion, use this rule:

```text
Can this help us pass the MVP smoke-check by Sunday?
```

If yes, do it.

If no, postpone it.
