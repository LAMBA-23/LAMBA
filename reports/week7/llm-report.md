# Week 7 LLM Report

## Tools used

Multiple AI tools were used by the team during Sprint 5: MiMo Code (mimo-auto), Codex 26.616.6631.0, Codex 5.6 Terra, Chat GPT 5.0, Chat GPT 5.6 Sol, Gemini 3.5 Flash.

## Forms of use

### Coding
- Implemented dynamic chat style switching across Android and backend layers
- Fixed the `style` parameter forwarding bug in `main.py`
- Updated test mock functions to accept the new `style` parameter
- Fixed ruff formatting issues in test files
- Clarified framework method usage during implementation
- Suggested code improvements before submission

### Debugging
- Identified that the deployed backend at 186.246.27.211 was running old code without style support
- Traced the full request chain to find the missing `style` parameter
- Diagnosed CI failures caused by locale-dependent formatting and mock signature mismatches
- Investigated causes of several development issues
- Fixed design issues and Excel export problems

### Documentation
- Created Sprint Retrospective, Reflection, and LLM Report artifacts for Week 7
- Updated CHANGELOG, customer-handover, and architecture documentation
- Created issues and PRs following the project template format
- Checked code for merge conflicts
- Reviewed teammate pull requests and provided recommendations

### Research
- Analyzed the server deployment workflow
- Verified that the deployed backend accepts the `style` parameter
- Finalized the app interface design and development

## Impact

AI tools accelerated implementation, debugging, and documentation work during Sprint 5. The main contribution was identifying and fixing the one-line bug where `style` was not forwarded to `ask_deepseek()`, which was the root cause of the style feature not working in production.
