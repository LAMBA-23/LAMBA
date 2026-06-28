# Week 4 Sprint Retrospective

This retrospective is public and sanitized. It focuses on team process, delivery, and Sprint outcomes without personal details or private conflict information.

## What went well

- The team converted a large part of the previous Sprint Review feedback into concrete backlog items and delivered visible improvements in registration, vehicle setup, history, chat, and statistics.
- User-specific backend flows and Android session persistence made the increment feel closer to a real product instead of a demo-only prototype.
- The Sprint produced stronger quality evidence through backend tests, CI workflow setup, and documented testing status, which improved confidence in the delivered increment.
- Customer review results were clearer than in the previous Sprint: core MVP flows were demonstrable, and several previously requested corrections were confirmed as addressed.

## What did not go well

- AI assistant behavior is still the main weak point. The chat exists and feels integrated, but answer usefulness and clarity are not stable enough yet.
- Some usability issues remained in the delivered increment, especially statistics label readability and confusing prompt blocks on the main screen.
- Quality automation improved mainly on the backend side; frontend or end-to-end quality evidence is still limited.
- A few important concerns were still deferred to backlog work, including stronger authorization, password hashing, and the remaining refinement work for [US-06 / #49](https://github.com/LAMBA-23/LAMBA/issues/49), while [US-05 / #48](https://github.com/LAMBA-23/LAMBA/issues/48) and [US-07 / #50](https://github.com/LAMBA-23/LAMBA/issues/50) were completed in this Sprint.

## What changed compared to the previous Sprint based on the previous Sprint Retrospective

- In the previous Sprint Retrospective, the team identified unfinished MVP foundation work as the main problem, especially authorization, user-specific data flow, and delayed vehicle timeline functionality.
- Compared to that Sprint, the team responded by focusing this Sprint on completing core MVP flows instead of expanding scope too early.
- Registration and login were connected to the backend, user-specific vehicle/event/statistics flows were improved, and the team completed [US-05 / #48](https://github.com/LAMBA-23/LAMBA/issues/48) and [US-07 / #50](https://github.com/LAMBA-23/LAMBA/issues/50), while identifying that [US-06 / #49](https://github.com/LAMBA-23/LAMBA/issues/49) still needs follow-up in the next Sprint.
- The team also improved process discipline by tying more work to explicit issues and by strengthening CI and testing evidence instead of relying only on manual progress reporting.

## Process improvements for the next Sprint

- Add a stricter Sprint rule that each selected user-facing feature must include both implementation work and visible acceptance evidence, such as a demo path, test update, or screenshot-ready validation artifact.
- Reserve explicit Sprint capacity for refinement work on the highest-risk remaining open story, [US-06 / #49](https://github.com/LAMBA-23/LAMBA/issues/49), so it does not compete informally with lower-risk UI or infrastructure tasks.
