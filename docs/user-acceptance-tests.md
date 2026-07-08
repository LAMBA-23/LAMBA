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
- **User goal:** A user can manually create a vehicle history record, provide required details, and see the saved record reflected in History and Statistics.
- **Preconditions:**
  - The user is registered or logged in.
  - A vehicle profile exists.
  - The History screen and manual record form are available.
  - The Statistics screen is available for the selected vehicle.

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Open the History screen. | The history timeline and manual add button are displayed. |
| 2 | Tap the "+" button to create a new record. | The manual record form opens. |
| 3 | Try to save the form without completing required fields. | The form blocks saving and clearly shows which required fields must be completed. |
| 4 | Select the record type and enter valid required information, for example mileage, amount, description, and fuel liters when applicable. | The form accepts valid input, including decimal fuel values such as 42.7 liters when the selected type is a fuel record. |
| 5 | Save the record. | The application saves the record and returns to the History screen. |
| 6 | Check the History screen. | The new record appears in History with the correct type and important details. |
| 7 | Open the Statistics screen. | Related statistics are updated correctly based on the manually created record. |

### Week 5 Execution Result

- **Execution status:** Completed
- **Date:** 4 July 2026
- **Executed by:** customer
- **Evidence:** private Moodle recording link
- **Result:** Needs changes
- **Customer comments or observed issues:**
  - The customer liked the manual form and confirmed that it follows the previously discussed user story.
  - Fuel amount should support decimal values instead of integers only.
  - Records created through the manual form were not correctly reflected in the Statistics screen, indicating a synchronization bug.
  - The customer suggested replacing the current AI record creation flow with a pre-filled confirmation form, allowing users to review and edit parsed information before saving it.
  - Entering mileage manually is inconvenient. The customer suggested recording both the start and end odometer values to calculate trip distance more accurately.
  - The customer suggested renaming "Repair" to "Repair/Breakdown" to better represent unexpected vehicle problems.
  - The customer requested the ability to attach photos to history records, especially for repair or breakdown events.
- **Traceability:**
  - GitHub Issue #161 - Add a manual form for creating vehicle history records
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
- **User goal:** A user can ask the AI assistant questions about vehicle statistics, expenses, and recent breakdown history and receive meaningful conversational answers.
- **Preconditions:**
  - The user is registered or logged in.
  - A vehicle profile exists.
  - The application contains vehicle history and statistics data.
  - The application contains repair or breakdown records when recent breakdown questions are tested.
  - The AI assistant service is available.

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Open the AI assistant. | The chat screen is displayed. |
| 2 | Ask a statistics-related question, for example, "Show my expenses". | The assistant understands the request and uses the selected vehicle's stored data. |
| 3 | Review the expense summary. | The assistant returns the correct expense totals or a clear explanation if no expense data is available. |
| 4 | Ask another statistics-related question, for example, "Show the latest data". | The assistant returns relevant statistics from the user's vehicle data. |
| 5 | Ask about recent breakdowns when breakdown data exists. | The assistant can summarize recent repair or breakdown history with useful dates or record details. |
| 6 | Review the response format and tone. | The response is cleanly formatted, understandable, relevant, and natural in conversation. |
| 7 | Continue using the application. | The user can continue the conversation or return to the main application flow. |

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

## UAT-007: Receive AI recommendations and notifications

- **Scenario ID:** UAT-007
- **Status:** Planned
- **User goal:** A user can receive useful AI recommendations and see relevant reminders or notifications about the vehicle.
- **Preconditions:**
  - The user is registered or logged in.
  - A vehicle profile exists.
  - Vehicle history or context data is available when recommendations depend on it.
  - The assistant or notification area is available.

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Open the assistant or relevant notification area. | The app displays the available recommendations, reminders, or notification entry point. |
| 2 | Review the displayed recommendation or reminder. | The recommendation is understandable, relevant to the vehicle context, and useful to the user. |
| 3 | Open or select a recommendation or notification. | The app shows the related detail, explanation, or next action. |
| 4 | Return to the main app flow. | The user can return without losing the current vehicle context. |

### Week 6 Execution Result

- **Execution status:** To be completed after the Week 6 customer interview.
- **Date:** To be completed after the Week 6 customer interview.
- **Executed by:** To be completed after the Week 6 customer interview.
- **Evidence:** To be added in the private Moodle submission if applicable.
- **Result:** To be completed after execution.
- **Customer comments or observed issues:** To be completed after the Week 6 customer interview.
- **Traceability:** To be linked to relevant GitHub issues or PBIs after implementation.
- **Resulting PBIs/issues:** To be completed after execution.

### Week 7 Execution Result

- **Execution status:** To be completed after the Week 7 transition confirmation.
- **Date:** To be completed after the Week 7 transition confirmation.
- **Executed by:** To be completed after the Week 7 transition confirmation.
- **Evidence:** To be added in the private Moodle submission if applicable.
- **Result:** To be completed after execution.
- **Customer comments or observed issues:** To be completed after the Week 7 transition confirmation.
- **Traceability:** To be linked to relevant GitHub issues or PBIs after implementation.
- **Resulting PBIs/issues:** To be completed after execution.

## UAT-008: Keep recent chat history

- **Scenario ID:** UAT-008
- **Status:** Planned
- **User goal:** A user can leave the chat and later return to see recent conversation context.
- **Preconditions:**
  - The user is registered or logged in.
  - A vehicle profile exists.
  - The chat feature is available.
  - Chat history persistence is implemented or limited history behavior is defined.

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Open the chat. | The chat screen opens for the active vehicle context. |
| 2 | Send several messages. | The messages appear in the conversation with assistant responses when available. |
| 3 | Leave the chat or close and reopen the app if supported. | The app allows the user to exit the chat without an error. |
| 4 | Return to the chat. | Recent messages, for example the last 5, are still visible. |
| 5 | Review older history behavior if only limited history is stored. | The app handles older history clearly, such as by showing only the retained recent messages or a clear empty state. |

### Week 6 Execution Result

- **Execution status:** To be completed after the Week 6 customer interview.
- **Date:** To be completed after the Week 6 customer interview.
- **Executed by:** To be completed after the Week 6 customer interview.
- **Evidence:** To be added in the private Moodle submission if applicable.
- **Result:** To be completed after execution.
- **Customer comments or observed issues:** To be completed after the Week 6 customer interview.
- **Traceability:** To be linked to relevant GitHub issues or PBIs after implementation.
- **Resulting PBIs/issues:** To be completed after execution.

### Week 7 Execution Result

- **Execution status:** To be completed after the Week 7 transition confirmation.
- **Date:** To be completed after the Week 7 transition confirmation.
- **Executed by:** To be completed after the Week 7 transition confirmation.
- **Evidence:** To be added in the private Moodle submission if applicable.
- **Result:** To be completed after execution.
- **Customer comments or observed issues:** To be completed after the Week 7 transition confirmation.
- **Traceability:** To be linked to relevant GitHub issues or PBIs after implementation.
- **Resulting PBIs/issues:** To be completed after execution.

## UAT-009: Add fuel record with decimal liters

- **Scenario ID:** UAT-009
- **Status:** Planned
- **User goal:** A user can enter a non-integer fuel amount when adding a fuel record.
- **Preconditions:**
  - The user is registered or logged in.
  - A vehicle profile exists.
  - The manual record form is available.
  - Fuel records are supported by the manual record form.

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Open the manual record form. | The form opens and allows the user to choose a record type. |
| 2 | Select the fuel record type. | Fuel-specific fields are displayed. |
| 3 | Enter a decimal fuel amount, for example 42.7 liters, with other required values. | The form accepts the valid decimal value and keeps it visible without rounding to an integer. |
| 4 | Save the record. | The record is saved successfully. |
| 5 | Open History and Statistics. | History displays the fuel record correctly and Statistics uses the decimal value correctly. |

### Week 6 Execution Result

- **Execution status:** To be completed after the Week 6 customer interview.
- **Date:** To be completed after the Week 6 customer interview.
- **Executed by:** To be completed after the Week 6 customer interview.
- **Evidence:** To be added in the private Moodle submission if applicable.
- **Result:** To be completed after execution.
- **Customer comments or observed issues:** To be completed after the Week 6 customer interview.
- **Traceability:** To be linked to relevant GitHub issues or PBIs after implementation.
- **Resulting PBIs/issues:** To be completed after execution.

### Week 7 Execution Result

- **Execution status:** To be completed after the Week 7 transition confirmation.
- **Date:** To be completed after the Week 7 transition confirmation.
- **Executed by:** To be completed after the Week 7 transition confirmation.
- **Evidence:** To be added in the private Moodle submission if applicable.
- **Result:** To be completed after execution.
- **Customer comments or observed issues:** To be completed after the Week 7 transition confirmation.
- **Traceability:** To be linked to relevant GitHub issues or PBIs after implementation.
- **Resulting PBIs/issues:** To be completed after execution.

## UAT-010: Record a trip using start and end odometer values

- **Scenario ID:** UAT-010
- **Status:** Planned
- **User goal:** A user can record a trip by entering start and end odometer values instead of manually calculating distance.
- **Preconditions:**
  - The user is registered or logged in.
  - A vehicle profile exists.
  - Trip creation is available through the manual record form or trip flow.
  - History or trip summary is available.

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Open the trip creation flow. | The app shows fields for trip information. |
| 2 | Enter the start odometer value. | The app accepts the start value. |
| 3 | Enter the end odometer value. | The app calculates the trip distance correctly when the end value is greater than or equal to the start value. |
| 4 | Enter an invalid end odometer value lower than the start value. | The app rejects the invalid value and explains that the end odometer cannot be lower than the start odometer. |
| 5 | Save a valid trip. | The trip is saved and appears in History or the trip summary. |
| 6 | Open Statistics if trip statistics are supported. | Related statistics are updated when applicable. |

### Week 6 Execution Result

- **Execution status:** To be completed after the Week 6 customer interview.
- **Date:** To be completed after the Week 6 customer interview.
- **Executed by:** To be completed after the Week 6 customer interview.
- **Evidence:** To be added in the private Moodle submission if applicable.
- **Result:** To be completed after execution.
- **Customer comments or observed issues:** To be completed after the Week 6 customer interview.
- **Traceability:** To be linked to relevant GitHub issues or PBIs after implementation.
- **Resulting PBIs/issues:** To be completed after execution.

### Week 7 Execution Result

- **Execution status:** To be completed after the Week 7 transition confirmation.
- **Date:** To be completed after the Week 7 transition confirmation.
- **Executed by:** To be completed after the Week 7 transition confirmation.
- **Evidence:** To be added in the private Moodle submission if applicable.
- **Result:** To be completed after execution.
- **Customer comments or observed issues:** To be completed after the Week 7 transition confirmation.
- **Traceability:** To be linked to relevant GitHub issues or PBIs after implementation.
- **Resulting PBIs/issues:** To be completed after execution.

## UAT-011: Add Repair/Breakdown record with photo attachment

- **Scenario ID:** UAT-011
- **Status:** Planned
- **User goal:** A user can create a repair or breakdown record and attach photos as evidence.
- **Preconditions:**
  - The user is registered or logged in.
  - A vehicle profile exists.
  - The manual record form is available.
  - Repair/Breakdown records are supported.
  - Photo attachments are supported when this scenario is executed.

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Open the manual record form. | The form opens and allows the user to choose a record type. |
| 2 | Select Repair/Breakdown. | The form displays fields relevant to repair or breakdown information. |
| 3 | Enter description, mileage, amount, or other required fields. | The form accepts valid input and validates required fields. |
| 4 | Attach one or more photos if supported. | The selected photo attachments are shown before saving. |
| 5 | Save the record. | The record is saved successfully. |
| 6 | Open History and the record details. | The record appears with the Repair/Breakdown label and the attached photo is visible or accessible from details. |

### Week 6 Execution Result

- **Execution status:** To be completed after the Week 6 customer interview.
- **Date:** To be completed after the Week 6 customer interview.
- **Executed by:** To be completed after the Week 6 customer interview.
- **Evidence:** To be added in the private Moodle submission if applicable.
- **Result:** To be completed after execution.
- **Customer comments or observed issues:** To be completed after the Week 6 customer interview.
- **Traceability:** To be linked to relevant GitHub issues or PBIs after implementation.
- **Resulting PBIs/issues:** To be completed after execution.

### Week 7 Execution Result

- **Execution status:** To be completed after the Week 7 transition confirmation.
- **Date:** To be completed after the Week 7 transition confirmation.
- **Executed by:** To be completed after the Week 7 transition confirmation.
- **Evidence:** To be added in the private Moodle submission if applicable.
- **Result:** To be completed after execution.
- **Customer comments or observed issues:** To be completed after the Week 7 transition confirmation.
- **Traceability:** To be linked to relevant GitHub issues or PBIs after implementation.
- **Resulting PBIs/issues:** To be completed after execution.

## UAT-012: Stay logged in after closing the app and log out manually

- **Scenario ID:** UAT-012
- **Status:** Planned
- **User goal:** A user remains logged in after closing the app and can manually log out when needed.
- **Preconditions:**
  - The app is installed or available in the test environment.
  - The user has a valid account.
  - Session persistence is implemented.
  - The Profile screen and Log out action are available.

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Log in with valid credentials. | The app opens the authenticated main flow. |
| 2 | Close and reopen the app. | The app opens without asking the user to log in again. |
| 3 | Open Profile. | The Profile screen opens for the current user. |
| 4 | Tap Log out. | The app ends the session and returns to the login or welcome screen. |
| 5 | Try to access protected screens after logout. | Protected screens are not accessible until the user logs in again. |

### Week 6 Execution Result

- **Execution status:** To be completed after the Week 6 customer interview.
- **Date:** To be completed after the Week 6 customer interview.
- **Executed by:** To be completed after the Week 6 customer interview.
- **Evidence:** To be added in the private Moodle submission if applicable.
- **Result:** To be completed after execution.
- **Customer comments or observed issues:** To be completed after the Week 6 customer interview.
- **Traceability:** To be linked to relevant GitHub issues or PBIs after implementation.
- **Resulting PBIs/issues:** To be completed after execution.

### Week 7 Execution Result

- **Execution status:** To be completed after the Week 7 transition confirmation.
- **Date:** To be completed after the Week 7 transition confirmation.
- **Executed by:** To be completed after the Week 7 transition confirmation.
- **Evidence:** To be added in the private Moodle submission if applicable.
- **Result:** To be completed after execution.
- **Customer comments or observed issues:** To be completed after the Week 7 transition confirmation.
- **Traceability:** To be linked to relevant GitHub issues or PBIs after implementation.
- **Resulting PBIs/issues:** To be completed after execution.

## UAT-013: Manage profile information

- **Scenario ID:** UAT-013
- **Status:** Planned
- **User goal:** A user can open the Profile screen, view account and vehicle information, update available data, and manage account actions.
- **Preconditions:**
  - The user is registered or logged in.
  - A vehicle profile exists.
  - The Profile screen is available from bottom navigation.
  - Editable profile or vehicle fields are defined when update behavior is tested.

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Open Profile from bottom navigation. | The Profile screen opens instead of the add-vehicle screen. |
| 2 | Review account information. | The user can view account information such as name and email. |
| 3 | Review active vehicle information. | The user can view the active vehicle information. |
| 4 | Edit supported profile or vehicle fields. | The app allows changes only for supported editable fields. |
| 5 | Save changes. | The updated information is saved successfully. |
| 6 | Leave and return to Profile. | The updated information remains visible. |
| 7 | Review account actions. | Logout is available from Profile if session persistence is implemented. |

### Week 6 Execution Result

- **Execution status:** To be completed after the Week 6 customer interview.
- **Date:** To be completed after the Week 6 customer interview.
- **Executed by:** To be completed after the Week 6 customer interview.
- **Evidence:** To be added in the private Moodle submission if applicable.
- **Result:** To be completed after execution.
- **Customer comments or observed issues:** To be completed after the Week 6 customer interview.
- **Traceability:** To be linked to relevant GitHub issues or PBIs after implementation.
- **Resulting PBIs/issues:** To be completed after execution.

### Week 7 Execution Result

- **Execution status:** To be completed after the Week 7 transition confirmation.
- **Date:** To be completed after the Week 7 transition confirmation.
- **Executed by:** To be completed after the Week 7 transition confirmation.
- **Evidence:** To be added in the private Moodle submission if applicable.
- **Result:** To be completed after execution.
- **Customer comments or observed issues:** To be completed after the Week 7 transition confirmation.
- **Traceability:** To be linked to relevant GitHub issues or PBIs after implementation.
- **Resulting PBIs/issues:** To be completed after execution.

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

## Week 6 UAT Summary

- **Scenarios executed:** To be completed after the Week 6 customer interview.
- **Passed scenarios:** To be completed after the Week 6 customer interview.
- **Failed / needs changes:** To be completed after the Week 6 customer interview.
- **Most important feedback points:** To be completed after the Week 6 customer interview.
- **Resulting PBIs or issues:** To be completed after execution.
- **Notes about scenarios not executed yet:** To be completed after the Week 6 customer interview.

## Week 7 UAT Summary

- **Scenarios executed:** To be completed after the Week 7 transition confirmation.
- **Passed scenarios:** To be completed after the Week 7 transition confirmation.
- **Failed / needs changes:** To be completed after the Week 7 transition confirmation.
- **Most important feedback points:** To be completed after the Week 7 transition confirmation.
- **Resulting PBIs or issues:** To be completed after execution.
- **Final customer confirmation notes:** To be completed after the Week 7 transition confirmation.
