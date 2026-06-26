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
- **Test data:**
  - User name: test user name
  - Email: test email address
  - Password: test password that satisfies validation rules
  - Vehicle make: BMW
  - Vehicle model: M4 Competition
  - Year: 2022
  - Current mileage: 12450 km

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Open the LAMBA app. | The welcome screen is shown with options to create an account or log in. |
| 2 | Choose to create an account. | The registration screen is shown with fields for name, email, password, and password confirmation. |
| 3 | Enter valid registration data and continue. | The app accepts the registration data and moves the user to the vehicle setup flow. |
| 4 | Enter the vehicle make, model, year, and current mileage. | The app accepts the vehicle details and enables saving the vehicle profile. |
| 5 | Save the vehicle profile. | The main car screen opens and displays the created vehicle as the active car. |

### Week 4 Execution Result

- **Execution status:** Not executed yet
- **Date:** TODO
- **Executed by:** TODO: customer/stakeholder
- **Evidence:** TODO: private Moodle recording link/timecode, do not commit publicly
- **Result:** TODO: Passed / Failed / Needs changes
- **Customer comments or observed issues:** TODO
- **Resulting PBIs/issues:** TODO: link resulting issue if needed

## UAT-002: Use the AI assistant from the main app flow

- **Scenario ID:** UAT-002
- **Status:** Active
- **User goal:** A user can ask a car-related question and open or use the AI chat.
- **Preconditions:**
  - The user is registered or logged in.
  - A vehicle profile exists.
  - The main car screen is available.
  - Backend services required for the AI assistant or chat flow are available.
- **Test data:**
  - Example question: What does error P0420 mean?
  - Example question: How often should I change oil for this car?

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Open the main car screen. | The app shows the active vehicle and the main interaction area for car-related assistance. |
| 2 | Open the chat or assistant entry point. | The AI assistant screen opens and shows the current vehicle context when available. |
| 3 | Enter a car-related question. | The question appears in the chat as the user's message. |
| 4 | Send the question. | The app submits the request and returns an assistant response or a clear clarification prompt. |
| 5 | Review the response and return to the main flow. | The user can continue the conversation or navigate back without losing the main app context. |

### Week 4 Execution Result

- **Execution status:** Not executed yet
- **Date:** TODO
- **Executed by:** TODO: customer/stakeholder
- **Evidence:** TODO: private Moodle recording link/timecode, do not commit publicly
- **Result:** TODO: Passed / Failed / Needs changes
- **Customer comments or observed issues:** TODO
- **Resulting PBIs/issues:** TODO: link resulting issue if needed

## UAT-003: Add and view a history record

- **Scenario ID:** UAT-003
- **Status:** Active
- **User goal:** A user can create a new car event record and see it in the history or timeline.
- **Preconditions:**
  - The user is registered or logged in.
  - A vehicle profile exists.
  - The history or timeline feature is available.
  - Backend services required for storing and reading vehicle events are available.
- **Test data:**
  - Event type: fuel
  - Description: Refueling
  - Amount: 2450
  - Mileage: 12450 km

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Open the main car screen. | The app shows the active vehicle and available actions. |
| 2 | Choose the option to add a history record or event. | The app opens the event creation flow or accepts the record through the assistant flow. |
| 3 | Enter the event type, description, amount, and mileage. | The app validates the entered event data and allows saving valid input. |
| 4 | Save the event record. | The app confirms that the record was saved or returns to the history flow with the new record available. |
| 5 | Open the history or timeline screen. | The newly created record is visible with the correct important details. |

### Week 4 Execution Result

- **Execution status:** Not executed yet
- **Date:** TODO
- **Executed by:** TODO: customer/stakeholder
- **Evidence:** TODO: private Moodle recording link/timecode, do not commit publicly
- **Result:** TODO: Passed / Failed / Needs changes
- **Customer comments or observed issues:** TODO
- **Resulting PBIs/issues:** TODO: link resulting issue if needed

## UAT-004: View statistics based on car activity

- **Scenario ID:** UAT-004
- **Status:** Active
- **User goal:** A user can open the statistics screen and inspect summarized car-related information.
- **Preconditions:**
  - The user is registered or logged in.
  - A vehicle profile exists.
  - The app has at least one available history record or sample activity data for the selected vehicle.
  - Backend services required for statistics are available.
- **Test data:**
  - Vehicle mileage, expenses, fuel amount, number of records, and average fuel consumption are available or seeded for the test user.

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Open the main car screen. | The app shows the active vehicle and navigation controls. |
| 2 | Open the menu or statistics navigation entry. | The menu shows a statistics option or the app navigates directly to statistics. |
| 3 | Select the statistics screen. | The statistics screen opens for the active vehicle. |
| 4 | Review the displayed summary values. | The app shows summarized car-related information such as mileage, expenses, fuel, records, or average consumption. |
| 5 | Change the available period filter if present. | The statistics view updates or keeps a clear selected period state. |

### Week 4 Execution Result

- **Execution status:** Not executed yet
- **Date:** TODO
- **Executed by:** TODO: customer/stakeholder
- **Evidence:** TODO: private Moodle recording link/timecode, do not commit publicly
- **Result:** TODO: Passed / Failed / Needs changes
- **Customer comments or observed issues:** TODO
- **Resulting PBIs/issues:** TODO: link resulting issue if needed

## Week 4 UAT Summary

- **Scenarios executed:** TODO
- **Passed scenarios:** TODO
- **Failed / needs changes:** TODO
- **Most important feedback points:** TODO
- **Resulting PBIs or issues:** TODO
