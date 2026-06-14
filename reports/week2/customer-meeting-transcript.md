# Interview Transcript

## Project Context

The customer confirmed that the project should function as a digital twin of a vehicle, where users interact with their car through an AI-powered chat interface.

## Key Solutions Made During the Interview

* A mobile application will be developed as the primary platform.
* For the MVP, support for only one mobile platform is required.
* The application should focus on the AI chat experience as the main interaction method.
* User registration and authentication are required for the MVP.
* Each user will be able to manage only one vehicle in the MVP version.
* Vehicle registration should be available during the onboarding process.

### Vehicle Data

The system should store:

* Vehicle information
* Mileage records
* Refueling records
* Repair records
* Installed parts information
* Chat history between the user and the AI assistant

All vehicle-related data should be preserved permanently. Data deletion is not planned.

### AI Assistant

The AI assistant should:

* Communicate with the user through chat.
* Collect vehicle-related information from conversations.
* Store structured vehicle data.
* Answer questions about the vehicle.
* Use historical vehicle information as context during conversations.

The AI should return structured information that can later be stored in the database.

### MVP Scope

The MVP should include:

* User registration
* User login
* Vehicle registration
* AI chat interface
* Basic vehicle statistics screen

The goal of the MVP is to demonstrate the concept and validate the core functionality rather than provide a complete product.

### Technical Decisions

* Backend and frontend should communicate through API endpoints.
* Backend stubs and mock data are acceptable for the MVP.
* Database architecture should remain simple and scalable.
* Advanced recommendation systems and predictive analytics are postponed to future iterations.
* AI-memory and database interaction mechanisms require further research and implementation planning.

## Action Items

* Prepare MVP V0 before Sunday.
* Prioritize AI chat functionality.
* Implement basic frontend screens.
* Create backend endpoints required for demonstration.
* Investigate structured AI responses and data storage approaches.
* Share repository progress with the customer before the next meeting.
