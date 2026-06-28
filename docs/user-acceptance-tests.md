# User Acceptance Tests

This document contains maintained User Acceptance Test (UAT) scenarios for LAMBA, a mobile app for vehicle owners.

UAT scenarios are stable product assets. Scenario IDs must stay stable over time: update valid scenarios in place, retire obsolete scenarios instead of deleting them, and create new IDs when the user goal changes materially.

Week 4 execution results will be added after the recorded customer UAT session. Private recording links, exact private timecodes, customer names, credentials, and other confidential information must not be committed to the public repository.

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
