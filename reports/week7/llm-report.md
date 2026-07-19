# Week 7 LLM Report

## Tools used

The team used multiple AI tools throughout Sprint 5: MiMo Code (mimo-auto), Codex 26.616.6631.0, Codex 5.6 Terra, Chat GPT 5.0, Chat GPT 5.6 Sol, and Gemini 3.5 Flash.

## Team member contributions

### @vanya630

**Model:** MiMo Code (mimo-auto)

- Implemented dynamic chat style switching across Android and backend layers
- Fixed the `style` parameter forwarding bug in `main.py`
- Updated test mock functions to accept the new `style` parameter
- Diagnosed CI failures caused by locale-dependent formatting and mock signature mismatches
- Created Sprint Retrospective, Reflection, and LLM Report artifacts for Week 7
- Updated CHANGELOG, customer-handover, and architecture documentation
- Created issues and PRs following the project template format
- Verified deployed backend accepts the style parameter

### @Erusiaaa

**Model:** Codex 26.616.6631.0

- Clarified the usage of framework methods during implementation
- Helped investigate the causes of several development issues
- Suggested minor code improvements before submitting changes

### @vasilisatumakina29

**Model:** Codex 26.616.6631.0, Chat GPT 5.0

- Checking code for merge conflicts
- Reviewing a teammate's pull request and providing recommendations for fixes if there are any issues
- Finalizing the app interface design, development and generation of an interface for the odometer trip
- Consultation with the problem of unmounting a photo in the breakdown section

### @mariachizhikova08

**Model:** Codex 5.6 Terra, Chat GPT 5.6 Sol, Gemini 3.5 Flash

- Checking code for merge conflicts
- Fixing design issues
- Fixing issue with export to excel in app

### @Elis-bett

**Model:** Codex 26.616.6631.0

- Search for critical bugs in Kotlin code
- Make Kotlin code more structured and clean
- Refining .xml files
- Grammar check for documentation file

## Impact

AI tools accelerated implementation, debugging, and documentation work during Sprint 5. The main contribution was identifying and fixing the one-line bug where `style` was not forwarded to `ask_deepseek()`, which was the root cause of the style feature not working in production.
