# Customer Meeting Summary

## Date

- Exact customer meeting date: June 18, 2026
- Summary preparation basis: [customer-meeting-transcript.md](./customer-meeting-transcript.md), published in the repository on June 14, 2026

## Participants and Roles

- Customer / product stakeholder
- Student development team

Relevant user roles discussed during the meeting:

- New user
- Vehicle owner

See also: [user-stories.md](./user-stories.md)

## Artifacts Demonstrated or Referenced

- Customer interview transcript: [customer-meeting-transcript.md](./customer-meeting-transcript.md)
- User stories and MVP scope draft: [user-stories.md](./user-stories.md)
- Week 2 repository index: [README.md](./README.md)
- MVP v0 technical foundation report: [mvp-v0-report.md](./mvp-v0-report.md)
- API/interface artifact: [docs/api-contract.md](../../docs/api-contract.md)

Prototype and interface evidence referenced by the resulting scope:

- [registration-screen.png](./images/registration-screen.png)
- [login-screen.png](./images/login-screen.png)
- [vehicle-screen.png](./images/vehicle-screen.png)
- [add-vehicle-screen.png](./images/add-vehicle-screen.png)
- [save-vehicle-screen.png](./images/save-vehicle-screen.png)
- [Chat-screen.png](./images/Chat-screen.png)
- [timeline-screen.png](./images/timeline-screen.png)
- [statistics-screen.png](./images/statistics-screen.png)
- [save-notes.png](./images/save-notes.png)

## Discussion Points

- The product should act as a digital twin of a vehicle.
- The main interaction model should be an AI-powered chat rather than form-heavy manual entry.
- A mobile application should be the primary platform for the MVP.
- Only one mobile platform is required for the MVP.
- Users need registration and authentication before accessing core functionality.
- Each user should manage only one vehicle in the MVP.
- Vehicle registration should be part of onboarding.
- The system should preserve vehicle information, mileage, refueling records, repair records, installed parts information, and chat history.
- The AI assistant should both capture structured vehicle data and answer questions using vehicle history as context.
- The MVP should validate the concept and core workflow rather than ship a full production-ready solution.
- Backend stubs and mock/demo data are acceptable at this stage.

## Decisions

- The customer confirmed the core concept of an AI-assisted vehicle digital twin.
- The team and customer aligned on mobile as the primary delivery platform.
- The MVP will support one vehicle per user.
- The AI chat experience will remain the main user interaction surface.
- Vehicle registration is included in onboarding.
- The initial MVP scope includes:
  - user registration
  - user login
  - vehicle registration
  - AI chat interface
  - basic vehicle statistics
- Backend and frontend will communicate through API endpoints.
- Database architecture should stay simple and scalable.
- Advanced recommendation systems and predictive analytics are postponed beyond the current MVP.

## Action Points

- Prepare MVP v0 before Sunday.
- Prioritize AI chat functionality.
- Implement the main frontend screens required for demonstration.
- Create backend endpoints needed for the demo flow.
- Investigate structured AI outputs and data storage.
- Share repository progress with the customer before the next meeting.

## Risks and Open Issues

- The exact level of AI automation for turning chat into structured records still requires implementation planning.
- AI memory and database interaction mechanisms need additional research.
- Response quality and structured extraction reliability are not yet validated in production conditions.
- Backend stubs and mock/demo data reduce delivery risk in Week 2, but they also mean the current implementation is not full production behavior.
- Advanced features remain out of scope for now, including recommendation systems, predictive analytics, and broader integrations.

Related analysis: [analysis.md](./analysis.md)

## Customer Feedback

- The customer emphasized that the application should focus on the chat-based experience.
- The customer accepted a simplified MVP centered on concept validation.
- The customer accepted postponing advanced features in favor of a demonstrable core workflow.
- The customer indicated that support for one vehicle per user is acceptable for the MVP.

## Customer Approvals Confirmed by the Meeting Record

The published meeting record confirms approval or explicit acceptance of the following points:

- mobile app as the primary platform
- AI chat as the main interaction method
- registration and authentication as part of the MVP
- vehicle registration during onboarding
- one-vehicle-per-user constraint for the MVP
- backend stubs and mock data as acceptable for MVP validation
- simplified MVP scope focused on concept demonstration

The repository does not contain evidence of a separate formal sign-off statement beyond these confirmed decisions in the published transcript.

## Resulting Changes

The meeting directly shaped the Week 2 documentation and scope:

- MVP scope was narrowed toward the Must Have flow captured in [user-stories.md](./user-stories.md)
- Initial MVP v1 scope was aligned around `US-01`, `US-02`, `US-03`, `US-04`, and `US-05`
- `US-12` OBD-II integration was removed from the active MVP path
- `US-13` multiple vehicles and `US-14` ownership transfer were kept outside MVP scope
- Week 2 prototype coverage centered on onboarding, vehicle setup, chat, timeline, and statistics
- MVP v0 documentation now reflects the technical foundation `Android -> FastAPI -> PostgreSQL`

Affected stories:

- `US-01` User registration: [user-stories.md](./user-stories.md)
- `US-02` Add a vehicle: [user-stories.md](./user-stories.md)
- `US-03` Send messages: [user-stories.md](./user-stories.md)
- `US-04` Automatically create records: [user-stories.md](./user-stories.md)
- `US-05` View vehicle timeline: [user-stories.md](./user-stories.md)
- `US-07` View basic statistics: [user-stories.md](./user-stories.md)
- `US-12` OBD-II integration removed from MVP path: [user-stories.md](./user-stories.md)

Affected prototype and interface artifacts:

- onboarding and login: [registration-screen.png](./images/registration-screen.png), [login-screen.png](./images/login-screen.png)
- vehicle setup: [vehicle-screen.png](./images/vehicle-screen.png), [add-vehicle-screen.png](./images/add-vehicle-screen.png), [save-vehicle-screen.png](./images/save-vehicle-screen.png)
- chat and AI interaction: [Chat-screen.png](./images/Chat-screen.png)
- history and timeline: [timeline-screen.png](./images/timeline-screen.png), [save-notes.png](./images/save-notes.png)
- statistics: [statistics-screen.png](./images/statistics-screen.png)
- API foundation: [docs/api-contract.md](../../docs/api-contract.md)
