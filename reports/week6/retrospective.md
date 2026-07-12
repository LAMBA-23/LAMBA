# Week 6 Sprint Retrospective

This retrospective is public and sanitized. It focuses on team process, delivery, and Sprint outcomes without personal details or private conflict information.

## What went well

- The team completed all 14 Sprint 4 issues and delivered the v1.3.0 trial release on time, covering security hardening, session persistence, chat history, logout, application icon, statistics improvements, and deployment on the customer-provided server.
- The combined Sprint Review, customer trial, transition-readiness meeting, and documentation review was conducted in a single session, which saved time and gave the team direct customer feedback on both product behaviour and handover expectations.
- The customer confirmed that chat history, session persistence, and AI statistics formatting worked during the live trial, validating several Sprint 4 deliverables under real conditions.
- The backend and product services were already deployed on infrastructure provided and controlled by the customer, which meant the team could demonstrate the product outside the university network.
- Customer-facing documentation, contributor guidance, and agent guidance were created or updated during the Sprint, giving the team a visible handover-ready documentation set before the customer meeting.
- Architecture and ADR documentation remained current and helped explain the product boundaries during the transition-readiness discussion.

## What did not go well

- Several customer-critical data-entry workflows were incomplete or broken during the trial: fractional fuel volume was not saved correctly, odometer-based trip entry was not functional, and repair and breakdown entry did not work.
- Photo attachment was identified as unavailable, even though it had been listed as planned scope.
- The customer's existing older account showed inconsistencies where newly entered events did not appear correctly in History, even though Statistics updated. This suggested an account-data compatibility issue that was not anticipated.
- The customer explicitly requested kopeck-level monetary precision, which was not included in the original Sprint scope and represented a missed requirement.
- Some Sprint scope was still too large or insufficiently decomposed. The team had planned recommendations, notifications, and chat-to-form behaviour but had to clarify the implementation approach during the customer meeting rather than arriving with a clear decision.
- Publication consent for the Sprint Review transcript was not captured at the meeting, which created ambiguity about whether the transcript could be published publicly or only shared privately through Moodle.
- The team had limited opportunity for realistic independent customer use before the Sprint Review, so most trial evidence came from the guided meeting session rather than from days of independent operation.

## What the team changed or attempted to change based on the previous Sprint Retrospective, and what results they observed

- The previous retrospective proposed splitting large AI, statistics, and chat-flow tasks earlier with smaller outcomes and clearer acceptance criteria. The team partially improved this by delivering security hardening, session persistence, chat history, logout, and statistics formatting as distinct items. However, the remaining data-entry workflows (fractional fuel, trips, repairs) still showed insufficient decomposition or verification before the trial.
- The previous retrospective proposed earlier end-to-end integration checks for user-facing data flows. The team improved deployment readiness by deploying to the customer-provided server, but the manual form to statistics flow and older-account compatibility were still discovered as gaps during the customer trial rather than during pre-Sprint-Review verification.
- The team maintained architecture and ADR documentation alignment, which helped during the transition-readiness discussion. The customer-facing documentation set was created or updated, which was an improvement over previous Sprints where documentation was primarily internal.
- The observed result is that Sprint 4 delivered more concrete customer-facing value than previous Sprints, but the trial exposed that several workflows needed verification under real customer data and real customer expectations rather than only under controlled test conditions.

## Action points

- Before the next Sprint Review, verify every customer-critical data-entry workflow end-to-end using realistic data, including edge cases like fractional values, older accounts, and cross-flow consistency between History, Statistics, and Chat.
- Capture recording and publication permissions explicitly at the beginning of any future customer meeting that may serve dual purposes (Sprint Review, UAT, transition confirmation).
