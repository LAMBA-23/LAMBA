# User Acceptance Tests

This document contains maintained User Acceptance Test (UAT) scenarios for LAMBA, a mobile app for vehicle owners.

## UAT-001: Register and create a vehicle profile

- **Scenario ID:** UAT-001
- **Status:** Active
- **User goal:** A new user can register or log in and add basic vehicle data.
- **Preconditions:**
  - The LAMBA mobile app is installed or available in the test environment.
  - The app is opened from a clean or logged-out state.
  - Backend services required for registration, authentication, and vehicle storage are available.

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Open the LAMBA app. | The welcome screen is shown with options to create an account or log in. |
| 2 | Choose to create an account. | The registration screen is shown with fields for name, email, password, and password confirmation. |
| 3 | Enter valid registration data and continue. | The app accepts the registration data and moves the user to the vehicle setup flow. |
| 4 | Enter the vehicle make, model, year, and current mileage. | The app accepts the vehicle details and enables saving the vehicle profile. |
| 5 | Save the vehicle profile. | The main car screen opens and displays the created vehicle as the active car. |

### Week 4 Execution Result

- **Execution status:** Completed
- **Date:** 27 June 2026
- **Executed by:** customer
- **Evidence:** private Moodle recording link
- **Result:** Passed
- **Customer comments or observed issues:** Registration was easy. Vehicle creation took less than a minute.
- **Traceability:**
  - Product Backlog Item: US-01
  - Product Backlog Item: US-02
- **Resulting PBIs/issues:**: No new PBIs or issues were created.

## UAT-002: Use the AI assistant from the main app flow

- **Scenario ID:** UAT-002
- **Status:** Active
- **User goal:** The user can ask the AI assistant a car-related question and receive a response.
- **Preconditions:**
  - The user is registered or logged in.
  - A vehicle profile exists.
  - The main car screen is available.
  - The AI assistant service is available.
| Step | User action | Expected outcome |
|---|---|---|
| 1 | Open the main car screen. | The app shows the active vehicle and the main interaction area for car-related assistance. |
| 2 | Open the chat or assistant entry point. | The AI assistant screen opens and shows the current vehicle context when available. |
| 3 | Enter a car-related question. | The question appears in the chat as the user's message. |
| 4 | Send the question. | The app submits the request and returns a relevant response to the user's question. |
| 5 | Review the response and return to the main flow. | The user can continue the conversation or navigate back without losing the main app context. |

### Week 4 Execution Result

- **Execution status:** Completed
- **Date:** 27 June 2026
- **Executed by:** customer
- **Evidence:** private Moodle recording link
- **Result:** Needs changes
- **Customer comments or observed issues:** The AI assistant needs to provide more accurate and relevant answers, as the current responses are not always satisfactory.
- **Traceability:**
  - US-03: Send messages
  - US-06: Ask AI assistante 
- **Resulting PBIs/issues:** GitHub Issue #160 – Enable AI assistant to use vehicle statistics in responses.

## UAT-003: Add and view a history record

- **Scenario ID:** UAT-003
- **Status:** Active
- **User goal:** A user can create a new car event record and see it in the timeline.
- **Preconditions:**
  - The user is registered or logged in.
  - A vehicle profile exists.
  - The history or timeline feature is available.

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Open the main car screen. | The app shows the active vehicle and available actions. |
| 2 | Choose the option to add a history record or event. | The app opens the event creation flow or accepts the record through the assistant flow. |
| 3 | Enter the event type, description, amount, and mileage. | The app validates the entered event data and allows saving valid input. |
| 4 | Save the event record. | The app confirms that the record was saved or returns to the history flow with the new record available. |
| 5 | Open the history or timeline screen. | The newly created record is visible with the correct important details. |

### Week 4 Execution Result

- **Execution status:** Completed
- **Date:** 27 June 2026
- **Executed by:** customer
- **Evidence:** private Moodle recording link
- **Result:** Needs changes
- **Customer comments or observed issues:** The customer suggested adding a manual event entry form in addition to the AI assistant.
This would allow users to create history records without using the chat.
- **Traceability:**
  - US-05: View vehicle timeline
- **Resulting PBIs/issues:** GitHub Issue #161 – Add a manual form for creating vehicle history

## UAT-004: View statistics based on car activity

- **Scenario ID:** UAT-004
- **Status:** Active
- **User goal:** A user can view statistics based on the vehicle's history.
- **Preconditions:**
  - The user is registered or logged in.
  - A vehicle profile exists.
  - The app has at least one available history record or sample activity data for the selected vehicle.
  - Backend services required for statistics are available.

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Open the main car screen. | The app shows the active vehicle and navigation controls. |
| 2 | Open the menu or statistics navigation entry. | The menu shows a statistics option or the app navigates directly to statistics. |
| 3 | Select the statistics screen. | The statistics screen opens for the active vehicle. |
| 4 | Review the displayed summary values. | The app shows summarized car-related information such as mileage, expenses, fuel, records, or average consumption. |
| 5 | Change the available period filter if present. | The statistics view updates or keeps a clear selected period state. |

### Week 4 Execution Result

- **Execution status:** Completed
- **Date:** 27 June 2026
- **Executed by:** customer
- **Evidence:** private Moodle recording link
- **Result:** Passed
- **Customer comments or observed issues:** The customer confirmed that the statistics screen is clear and easy to understand.
- **Traceability:**
  - US-07: View basic statistics
- **Resulting PBIs/issues:** No new PBIs or issues were created.

## UAT-005: Manually add a vehicle history record

- **Scenario ID:** UAT-005
- **Status:** Active
- **User goal:** A user can manually create a vehicle history record without using the AI assistant.
- **Preconditions:**
  - The user is registered or logged in.
  - A vehicle profile exists.
  - The History screen and manual record form are available.

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Open the History screen. | The history timeline and manual add button are displayed. |
| 2 | Tap the "+" button to create a new record. | The manual record form opens. |
| 3 | Select the record type and enter the required information, for example mileage, liters, amount, description. | The form accepts valid input and validates required fields. |
| 4 | Save the record. | The application saves the record and returns to the History screen. |
| 5 | Check the History and Statistics screens. | The new record appears in History and related statistics are updated correctly. |

### Week 5 Execution Result

- **Execution status:** Completed
- **Date:** 4 July 2026
- **Executed by:** customer
- **Evidence:** private Moodle recording link
- **Result:** Needs changes
- **Customer comments or observed issues:**
  - The customer liked the manual form and confirmed that it follows the previously discussed user story.
  - The "+" button in the History screen is not immediately obvious to first-time users.
  - Fuel amount should support decimal values instead of integers only.
  - Records created through the manual form were not correctly reflected in the Statistics screen, indicating a synchronization bug.
  - The customer suggested replacing the current AI record creation flow with a pre-filled confirmation form, allowing users to review and edit parsed information before saving it.
  - Entering mileage manually is inconvenient. The customer suggested recording both the start and end odometer values to calculate trip distance more accurately.
  - The customer suggested renaming "Repair" to "Repair/Breakdown" to better represent unexpected vehicle problems.
  - The customer requested the ability to attach photos to history records, especially for repair or breakdown events.
  - The customer noted that manually adding a record currently requires several clicks and could be made more accessible.
- **Traceability:**
  - GitHub Issue #161 - Add a manual form for creating vehicle history
- **Resulting PBIs/issues:**
  - Support decimal fuel values.
  - Synchronize manually created records with the Statistics screen.
  - Redirect AI-generated records to a pre-filled confirmation form before saving.
  - Add trip start/end odometer recording.
  - Rename Repair to Repair/Breakdown.
  - Add photo attachments for history records.
  - Improve discoverability of manual record creation.

## UAT-006: Ask the AI assistant about vehicle statistics

- **Scenario ID:** UAT-006
- **Status:** Active
- **User goal:** A user can ask the AI assistant questions about vehicle statistics and receive meaningful answers.
- **Preconditions:**
  - The user is registered or logged in.
  - A vehicle profile exists.
  - The application contains vehicle history and statistics data.
  - The AI assistant service is available.

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Open the AI assistant. | The chat screen is displayed. |
| 2 | Ask a statistics-related question, for example, "Show my expenses". | The assistant understands the request. |
| 3 | Send another statistics-related question, for example, "Show the latest data". | The assistant returns relevant statistics from the user's vehicle data. |
| 4 | Review the response. | The response is understandable, relevant, and based on stored vehicle information. |
| 5 | Continue using the application. | The user can continue the conversation or return to the main application flow. |

### Week 5 Execution Result

- **Execution status:** Completed
- **Date:** 4 July 2026
- **Executed by:** customer
- **Evidence:** private Moodle recording link
- **Result:** Needs changes
- **Customer comments or observed issues:**
  - The customer confirmed that the assistant can now answer questions about vehicle statistics, which is a significant improvement over the previous version.
  - Some responses were incorrectly formatted or returned incomplete statistical information.
  - In one case, the expenses query did not display the expected totals.
  - The customer requested cleaner formatting and more natural, conversational AI responses instead of purely factual output.
  - The customer suggested allowing the AI assistant to provide users with the five most recent breakdowns together with their dates.
  - The customer recommended providing the AI model with richer vehicle context to improve answer quality.
  - The customer confirmed that the assistant behaves like an automated system rather than manually prepared responses.
- **Traceability:**
  - GitHub Issue #160 - Enable AI assistant to use vehicle statistics in responses.
- **Resulting PBIs/issues:**
  - Improve formatting of statistics responses.
  - Fix incorrect expense summary generation.
  - Improve conversational quality of AI responses.
  - Support queries about recent breakdown history.
  - Expand AI context with additional relevant vehicle data.

## Week 4 UAT Summary

- **Scenarios executed:** 4 (UAT-001, UAT-002, UAT-003, UAT-004)
- **Passed scenarios:** UAT-001, UAT-004
- **Failed / needs changes:** UAT-002, UAT-003
- **Most important feedback points:**
  - Improve the accuracy and relevance of AI assistant responses by using vehicle statistics.
  - Add a manual event entry form as an alternative to creating history records through the AI assistant.
  - The registration flow and statistics screen were easy to use and met customer expectations.
- **Resulting PBIs or issues:**
  - GitHub Issue #160 – Enable AI assistant to use vehicle statistics in responses.
  - GitHub Issue #161 – Add a manual form for creating vehicle history records.
