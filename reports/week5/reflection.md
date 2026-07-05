# Week 5 Reflection

## Learning points

- Sprint 3 confirmed that the architecture documentation and ADRs are useful not only as reporting artifacts, but also as a shared explanation of how the product works. The customer had asked about architecture earlier, so the team had already reasoned about the Android, backend, database, deployment, and AI integration boundaries before the Sprint Review.
- The reviewed MVP v2 increment showed that the team can build from the planned architecture instead of treating architecture as something written only after implementation. Manual history records, statistics behavior, backend-owned processing, and database persistence all depend on that shared structure.
- The Assignment workflow helped keep issue, branch, review, release, and evidence expectations visible. This reduced ambiguity because the team had a clearer external standard for what must be tracked and shown.
- Configuration, deployment, and release understanding improved compared with Assignment 4. The team became more aware of the need to connect release artifacts, backend availability, demo evidence, CI evidence, and public reporting in a way that can be inspected later.
- The Sprint Review was a useful learning point because the customer identified concrete product gaps instead of only giving general impressions. The most important priorities were decimal fuel liters, chat-to-prefilled-form confirmation, manual form/statistics correctness, cleaner AI statistics answers, and realistic APK testing.

## Validated assumptions

- The current architecture direction is understandable to the customer and is suitable for the MVP stage: Android owns the interface, the backend owns validation and business logic, PostgreSQL stores vehicle data, and external AI integration goes through the backend.
- Manual history record creation is a valuable secondary input path. The customer accepted it as useful, even though the entry point through History plus the plus button is not fully intuitive.
- Statistics and AI assistant behavior are central to product value, but they must be verified with real stored data because small correctness problems become visible quickly during customer review.
- The customer prefers explicit confirmation before saving AI-parsed data. The chat-to-prefilled-form flow is a better direction than silently writing uncertain AI output into vehicle history.
- Assignment-driven workflow requirements help the team maintain traceability and public evidence when they are followed consistently.

## Friction and gaps

- Some Sprint 3 tasks were not done, especially in the AI/statistics/chat area. This made it harder to complete all planned behavior and verify it before the Sprint Review.
- Merge and integration coordination can still create friction when Android, backend, statistics, and documentation work overlap near the end of the Sprint.
- The manual form and statistics flow needed earlier end-to-end validation. The Sprint Review found that form-created data may not transfer correctly into statistics.
- The release and deployment evidence improved, but realistic APK usage still needs stronger validation in a real car-use scenario instead of only controlled review or demo conditions.
- The product still needs follow-up work on decimal fuel liters, AI answer formatting and correctness, chat-to-form confirmation, recommendations, notifications, and app polish.

## Planned response

- Split large AI, statistics, and chat-flow tasks earlier so that each planned item has a small outcome, clear acceptance criteria, and a realistic verification path.
- Add earlier integration checks for flows that cross Android, backend, persistence, and statistics, especially manual form submission and statistics updates.
- Keep architecture and ADR documentation aligned with implemented behavior before review, not only after release preparation.
- Preserve the issue, branch, review, CI, release, and reporting discipline introduced by the Assignment workflow because it helps make Sprint evidence inspectable.
- Plan realistic APK testing earlier for the next increment so customer-facing issues can be found before the Sprint Review.
