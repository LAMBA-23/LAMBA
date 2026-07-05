# Week 5 Sprint Retrospective

This retrospective is public and sanitized. It focuses on team process, delivery, and Sprint outcomes without personal details or private conflict information.

## What went well

- The team delivered the main MVP v2 direction for Sprint 3: manual vehicle history record creation, improved statistics behavior, cleaner timeline behavior, and a more useful reviewed increment.
- Most team members stayed focused on the Sprint deadline and tried to keep the MVP work moving even when availability was not fully predictable.
- The Sprint Review showed that important customer feedback from the previous review was addressed, especially the frontend layout direction, manual form, and statistics-related improvements.
- Architecture and ADR documentation became more visible during the Sprint Review, which helped explain the Android, backend, database, deployment, and AI integration boundaries.

## What did not go well

- Some Sprint scope was still too large or dependent on unstable behavior, especially AI statistics answers, recommendations, notifications, and the chat-to-form direction.
- Merge conflicts and integration coordination created friction, especially where Android, backend, statistics, and documentation changes overlapped.
- An unexpected team availability issue reduced delivery capacity and made it harder to complete all planned work before the Sprint Review.
- The Sprint Review found follow-up gaps that should have been caught earlier, including decimal fuel liters, manual form data transfer into statistics, and cleaner AI statistics answers.

## What the team changed or attempted to change based on the previous Sprint Retrospective, and what results they observed

- The previous Sprint Retrospective proposed that each selected user-facing feature should have visible acceptance evidence. The team partially improved this by preparing a demonstrable manual history form, statistics behavior, Sprint Review summary, and sanitized transcript.
- The previous retrospective also proposed reserving capacity for the highest-risk remaining story. The team attempted to focus on the risky history/statistics/AI area, but the work was still not split finely enough. As a result, several items were delivered, while AI statistics reliability, recommendations, notifications, and chat-to-form behavior stayed incomplete or moved back to the backlog.
- The team improved architecture and process visibility by discussing the current architecture, ADRs, quality evidence, and deployment model during the Sprint Review. This helped clarify future direction, especially the backend-parsed chat-to-prefilled-form flow.
- The observed result is mixed: MVP v2 became more concrete and reviewable, but the Sprint still showed that large AI and integration tasks need earlier decomposition and earlier end-to-end verification.

## Action points

- Split large AI, statistics, and chat-flow tasks earlier, before Sprint commitment, so each Sprint item has a smaller outcome, clear acceptance criteria, and a realistic review path.
- Add an earlier end-to-end check for user-facing data flows, especially manual form to statistics and realistic APK use, before the Sprint Review instead of discovering those gaps during customer review.
