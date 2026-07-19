# Week 7 Reflection

## Learning points

- Dynamic chat style switching required changes across all layers: Android UI, SessionManager, ChatRepository, backend schema, and AI prompt routing. A single missed layer (the `style` parameter not forwarded in `main.py`) caused the entire feature to appear broken.
- Direct code changes on the production server without going through GitHub create deployment blockers for teammates. The correct workflow is: local change → push to GitHub → deploy from server via `git pull`.
- Mock functions in tests must be updated when function signatures change. Adding a parameter to `ask_deepseek()` broke all `fake_ask` mocks that did not accept the new parameter.
- Ruff formatting must be run locally before pushing. The CI formatting check caught unformatted test files twice.

## Validated assumptions

- The customer confirmed that the product is ready for independent use and accepted MVP v3.
- Three communication styles (Friendly, Selfish, Pragmatic) cover the main user preferences.
- The deployed backend at 186.246.27.211 correctly handles all style requests after the fix.
- All 42 backend tests pass after the style parameter fix.

## Friction and gaps

- The deployment process was not documented before Sprint 5. Team members had to ask who has SSH access to the server.
- The `style` parameter was added to the schema and `deepseek_chat.py` but not forwarded in `main.py`. This was a review gap.
- No automated deployment pipeline exists. Backend deployment requires manual SSH access and `git pull && docker compose up --build -d`.

## Planned response

- Document the deployment process in CONTRIBUTING.md so all team members understand the workflow.
- Add a pre-push checklist: run lint, format, and tests locally before pushing.
- Consider setting up GitHub Actions deployment with SSH secrets to automate backend deployment.
