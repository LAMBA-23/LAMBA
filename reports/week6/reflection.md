# Week 6 Reflection

## Learning points

- The trial release showed that completing all planned issues does not guarantee that the product works correctly under real customer data and real customer expectations. The Sprint 4 backlog was fully closed, but the customer trial immediately revealed gaps in fractional fuel volume, odometer trips, repair entry, and older-account compatibility. The lesson is that issue closure and customer-verified behaviour are different things, and both must be checked before a trial.
- The combined Sprint Review and customer meeting was efficient but created a publication-ambiguity problem because recording consent was captured but public transcript publication consent was not explicitly addressed. Future meetings that serve multiple Assignment purposes need to clarify each permission category at the start.
- Customer-facing documentation is a first-class product deliverable, not an afterthought. The customer explicitly asked for the complete documentation set including process, quality, architecture, development, and evidence materials, not just usage instructions. This confirmed that the maintained documentation approach from earlier Assignments was the right direction, but the scope of what a customer wants is broader than what a developer assumes.
- The recommendation delivery mechanism was clarified during the meeting rather than before it. The team had assumed proactive chat messages, but the customer confirmed that rule-based notifications were sufficient. This shows that implementation-approach uncertainty should be resolved before Sprint commitment, not during the customer review.
- The deployment on the customer-provided server worked and was accessible outside the university network, which validated the Docker Compose deployment model and the backend architecture. However, the older-account data inconsistency showed that the product must be tested against real accumulated data, not only fresh accounts.

## Validated assumptions

- The current architecture direction (Android client, FastAPI backend, PostgreSQL, external AI service) is suitable for the product and was understood by the customer during the transition-readiness discussion.
- Session persistence, local chat history, and explicit logout are expected behaviours that the customer validated during the trial.
- The customer does not need repository administrator access and prefers a complete archive handover, which simplifies the transition model.
- The customer wants detailed non-technical instructions, confirming that the documentation target audience is a non-developer reader who may later hand the product to another team.
- Security hardening (password hashing, rate limiting, demo account removal) is appreciated but not the primary customer concern. The customer's priority is functional data-entry correctness and natural AI interaction quality.

## Friction and gaps

- Several customer-critical workflows were incomplete at trial time despite all Sprint 4 issues being closed, indicating that the Sprint scope did not fully capture the customer's functional expectations for data-entry correctness.
- The older-account data inconsistency was not anticipated by the team, suggesting insufficient testing against real or migrated data.
- Publication consent for the Sprint Review transcript was ambiguous, creating uncertainty about whether public publication was permitted.
- The team had not planned a realistic independent-use window before the Sprint Review, so trial evidence was limited to the guided meeting session.
- Remaining Sprint 5 scope (fractional fuel, kopeck precision, odometer trips, repair entry, photo attachments, recommendations, AI tone, documentation archives) is still significant and must be completed under tighter time constraints.

## Planned response

- Implement a pre-trial verification checklist for every customer-critical data-entry workflow, including fractional values, cross-flow consistency (History, Statistics, Chat), and older-account compatibility.
- For any future meeting that combines Sprint Review, UAT, transition discussion, and documentation review, prepare a permission checklist and confirm each category explicitly at the start of the recording.
- Continue maintaining the full documentation set as a product deliverable, and plan the archive preparation (source + docs) as an explicit Sprint 5 task.
- Resolve implementation-approach uncertainty (e.g., notifications vs. chat messages for recommendations) during Sprint planning, not during the customer meeting.
- Plan realistic independent-use testing earlier in the Sprint, using the actual customer account data where possible, to discover data-compatibility and usability issues before the Sprint Review.
