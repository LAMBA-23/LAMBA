# Week 7 LLM Report

## Tools used

- **MiMo Code (AI coding assistant)** — used throughout Sprint 5 for code implementation, debugging, testing, and documentation.

## Forms of use

### Coding
- Implemented the dynamic chat style switching feature across Android (SessionManager, ProfileActivity, ChatRepository, Models) and backend (deepseek_chat.py, schemas.py, main.py).
- Fixed the `style` parameter forwarding bug in `main.py`.
- Updated all `fake_ask` mock functions in test files to accept the new `style` parameter.
- Fixed ruff formatting issues in test files.

### Debugging
- Identified that the deployed backend at 186.246.27.211 was running old code without style support.
- Traced the full request chain from Android → Retrofit → ChatRepository → backend → ask_deepseek to find the missing `style` parameter.
- Diagnosed CI failures caused by locale-dependent decimal formatting and mock function signature mismatches.

### Documentation
- Created Sprint Retrospective, Reflection, and LLM Report artifacts for Week 7.
- Updated CHANGELOG.md and docs/customer-handover.md with style feature documentation.
- Created issues and PRs following the project template format.

### Research
- Analyzed the server deployment workflow to understand how to update the backend.
- Verified that the deployed backend accepts the `style` parameter by testing with curl.

## Impact

The AI assistant significantly accelerated the implementation, debugging, and documentation work during Sprint 5. The main contribution was identifying and fixing the one-line bug where `style` was not forwarded to `ask_deepseek()`, which was the root cause of the style feature not working in production.
