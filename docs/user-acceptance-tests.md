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
  - Redirect assistant-created records to a pre-filled confirmation form before saving.
  - Add trip start/end odometer recording.
  - Rename Repair to Repair/Breakdown.
  - Add photo attachments for history records.
  - Improve discoverability of manual record creation.

### Week 6 Execution Result

- **Execution status:** Completed
- **Date:** 10 July 2026
- **Executed by:** customer with guidance from the team representative
- **Evidence:** private Week 6 meeting recording and sanitized transcript
- **Result:** Needs changes
- **Customer comments or observed issues:**
  - Two events were added during the trial.
  - The Statistics record count changed from 8 to 10, confirming that Statistics received the new data.
  - The newly entered records did not load or display correctly in History.
  - The team suspected compatibility problems with an older customer account, but this is not a confirmed root cause.
  - The customer allowed the team to clear or adapt the account data if necessary.
  - Zero-amount entries are intentionally no longer displayed.
- **Traceability:** Product Backlog Items listed below.
- **Resulting PBIs:**
  - PBI: Fix newly created records not appearing correctly in History.
  - PBI: Verify that manually created and chat-created records appear consistently in both History and Statistics.
  - PBI: Verify migration, compatibility, or reset behavior for accounts created with older application versions.

### Week 7 Execution Result

- **Execution status:** Partially completed
- **Date:** 18 July 2026
- **Executed by:** Customer with guidance from the team representative
- **Evidence:** [Week 7 Sprint Review transcript](../reports/week7/sprint-review-transcript.md)
- **Result:** Passed
- **Customer comments or observed issues:**
  - The customer created and saved a fuel record with decimal litre and expense values and confirmed that the result was visible.
  - The customer confirmed that the completed trip appeared correctly in History and Statistics.
  - Required-field validation and the complete manual flow for every record type were not executed during Week 7.
- **Related GitHub Issues:**
  - [#313](https://github.com/LAMBA-23/LAMBA/issues/313) — Support decimal numeric fields.
  - [#314](https://github.com/LAMBA-23/LAMBA/issues/314) — Verify decimal values across persistence, History, and Statistics.
  - [#321](https://github.com/LAMBA-23/LAMBA/issues/321) — Fix History records and notifications.

## UAT-006: Ask the AI assistant about vehicle statistics

- **Scenario ID:** UAT-006
- **Status:** Active
- **User goal:** A user can ask the AI assistant questions about vehicle statistics, expenses, supported periods, and recent breakdown history and receive meaningful conversational answers.
- **Preconditions:**
  - The user is registered or logged in.
  - A vehicle profile exists.
  - The application contains vehicle history and statistics data.
  - The application contains repair or breakdown records when recent breakdown questions are tested.
  - The AI assistant service is available.

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Open the AI assistant. | The chat screen is displayed. |
| 2 | Ask a statistics-related question, for example, "Show my expenses this month". | The assistant understands the request and uses the selected vehicle's stored data. |
| 3 | Review the expense summary. | The assistant returns the correct expense totals or a clear explanation if no expense data is available. |
| 4 | Ask another statistics-related question for a week, a month, or another supported period. | The assistant returns relevant period-based statistics from the user's vehicle data. |
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

### Week 6 Execution Result

- **Execution status:** Completed
- **Date:** 10 July 2026
- **Executed by:** customer with guidance from the team representative
- **Evidence:** private Week 6 meeting recording and sanitized transcript
- **Result:** Passed with follow-up item
- **Customer comments or observed issues:**
  - The customer reviewed a statistics-related AI answer and said it was good.
  - Improved formatting and period-based analysis were accepted.
  - The customer later requested more natural and conversational wording because the product concept is a conversation with the car.
- **Traceability:** Product Backlog Item listed below.
- **Resulting PBIs:**
  - PBI: Improve the AI prompt so responses sound less dry and more natural and conversational.

### Week 7 Execution Result

- **Execution status:** Partially completed
- **Date:** 18 July 2026
- **Executed by:** Customer with guidance from the team representative
- **Evidence:** [Week 7 Sprint Review transcript](../reports/week7/sprint-review-transcript.md)
- **Result:** Passed
- **Customer comments or observed issues:**
  - The customer observed a natural, sarcastic response in the selected Selfish communication style and confirmed that the style was clearly visible in the response.
  - Statistics questions, expense totals, supported periods, and recent breakdown queries were not re-executed during Week 7.
- **Related GitHub Issues:**
  - [#316](https://github.com/LAMBA-23/LAMBA/issues/316) — Improve the assistant's conversational responses.
  - [#341](https://github.com/LAMBA-23/LAMBA/issues/341) — Improve the vehicle persona and chat formatting.
  - [#347](https://github.com/LAMBA-23/LAMBA/issues/347) — Add selectable assistant communication styles.
  - [#353](https://github.com/LAMBA-23/LAMBA/issues/353) — Apply the selected style to assistant responses.

## UAT-007: Receive rule-based vehicle recommendations as notifications

- **Scenario ID:** UAT-007
- **Status:** Active
- **User goal:** A user can receive useful rule-based vehicle recommendations as notifications.
- **Preconditions:**
  - The user is registered or logged in.
  - A vehicle profile exists.
  - Vehicle history or context data is available when recommendations depend on it, such as mileage for an oil-change reminder.
  - The notification area is available.

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Open the relevant notification area. | The app displays available recommendations, reminders, or a notification entry point. |
| 2 | Review the displayed recommendation or reminder. | The recommendation is understandable, relevant to the vehicle context, and useful to the user. Simple rule-based recommendations are acceptable. |
| 3 | Open or select a recommendation or notification. | The app shows the related detail, explanation, or next action. |
| 4 | Open the AI assistant chat. | Recommendations do not appear as unsolicited proactive AI chat messages. |
| 5 | Return to the main app flow. | The user can return without losing the current vehicle context. |

### Week 6 Execution Result

- **Execution status:** Not executed - implementation was incomplete
- **Date:** 10 July 2026
- **Executed by:** Not executed
- **Evidence:** private Week 6 meeting recording and sanitized transcript
- **Result:** Not executed / Needs implementation
- **Customer comments or observed issues:**
  - Recommendations in the chat would be excessive.
  - Rule-based recommendations shown as notifications are sufficient.
- **Traceability:** Product Backlog Items listed below.
- **Resulting PBIs:**
  - PBI: Implement rule-based vehicle recommendations using notifications.
  - PBI: Display recommendations in notifications instead of proactive chat messages.

### Week 7 Execution Result

- **Execution status:** Partially completed
- **Date:** 18 July 2026
- **Executed by:** Customer with guidance from the team representative
- **Evidence:** [Week 7 Sprint Review transcript](../reports/week7/sprint-review-transcript.md)
- **Result:** Passed
- **Customer comments or observed issues:**
  - The customer opened the recommendations screen and saw a recommendation related to the newly created breakdown.
  - An oil-change recommendation based on mileage or elapsed time was discussed but was not executed by the customer.
  - Opening recommendation details and confirming the absence of unsolicited recommendations in chat were not executed.
- **Related GitHub Issues:**
  - [#51](https://github.com/LAMBA-23/LAMBA/issues/51) — Receive maintenance recommendations.
  - [#52](https://github.com/LAMBA-23/LAMBA/issues/52) — Receive notifications.
  - [#315](https://github.com/LAMBA-23/LAMBA/issues/315) — Implement rule-based recommendations.
  - [#325](https://github.com/LAMBA-23/LAMBA/issues/325) — Add in-app notifications.
  - [#328](https://github.com/LAMBA-23/LAMBA/issues/328) — Fix recurring recommendation notifications.

## UAT-008: Keep the five most recent chats locally

- **Scenario ID:** UAT-008
- **Status:** Active
- **User goal:** A user can open one of the five most recent locally stored chats and continue the conversation, and local chat history is cleared after logout.
- **Preconditions:**
  - The user is registered or logged in.
  - A vehicle profile exists.
  - The chat feature is available.
  - Local chat history storage is implemented on the device.

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Open the chat. | The chat screen opens for the active vehicle context. |
| 2 | Create or continue several separate chats until more than five chats exist locally. | The app stores only the five most recent chats on the device. |
| 3 | Leave the chat and open the local chat history. | The five most recent chats are available locally, and older chats outside the local limit are not shown. |
| 4 | Open one of the locally stored chats and send another message. | The previous conversation is shown and can be continued. |
| 5 | Log out and then check local chat history after returning to the application. | Locally stored chat history is cleared after logout. |

### Week 6 Execution Result

- **Execution status:** Completed
- **Date:** 10 July 2026
- **Executed by:** customer with guidance from the team representative
- **Evidence:** private Week 6 meeting recording and sanitized transcript
- **Result:** Passed
- **Customer comments or observed issues:**
  - The customer could see the chat history.
  - The customer opened an existing chat and continued the conversation.
  - No new functional issue was identified.
- **Traceability:** Product Backlog Item coverage for local chat history behavior.
- **Resulting PBIs:** No new PBIs were created.

### Week 7 Execution Result

- **Execution status:** Not executed
- **Date:** 18 July 2026
- **Executed by:** Customer with guidance from the team representative
- **Evidence:** [Week 7 Sprint Review transcript](../reports/week7/sprint-review-transcript.md)
- **Result:** Not executed
- **Customer comments or observed issues:** The five-chat limit, continuing a stored chat, and clearing local chat history after logout were not tested during Week 7.
- **Related GitHub Issues:** No new issue was identified because this scenario was not executed.

## UAT-009: Decimal fuel and monetary values

- **Scenario ID:** UAT-009
- **Status:** Active
- **User goal:** A user can enter decimal fuel volume and decimal monetary values when adding vehicle records.
- **Preconditions:**
  - The user is registered or logged in.
  - A vehicle profile exists.
  - The manual record form is available.
  - Fuel and monetary values are supported by the manual record form.

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Open the manual record form. | The form opens and allows the user to choose a record type. |
| 2 | Select the fuel record type. | Fuel-specific fields are displayed. |
| 3 | Enter a decimal fuel amount, for example 42.7 liters, with other required values. | The form accepts the valid decimal value and keeps it visible without rounding to an integer. |
| 4 | Enter a decimal monetary amount, for example 2549.90 rubles. | The form accepts the valid decimal monetary value and keeps it visible without rounding. |
| 5 | Save the record. | The record is saved successfully. |
| 6 | Open History and Statistics. | History displays the decimal values correctly and Statistics uses the decimal values correctly. |

### Week 6 Execution Result

- **Execution status:** Completed
- **Date:** 10 July 2026
- **Executed by:** customer with guidance from the team representative
- **Evidence:** private Week 6 meeting recording and sanitized transcript
- **Result:** Needs changes
- **Customer comments or observed issues:**
  - A fractional fuel value could be entered but was not saved.
  - Fractional fuel values are a must-have.
  - The customer additionally required monetary values to support kopecks because fuel prices and expenses are not normally whole numbers.
- **Traceability:** Product Backlog Items listed below.
- **Resulting PBIs:**
  - PBI: Fix persistence and display of fractional fuel liters.
  - PBI: Support decimal monetary amounts (rubles and kopecks).
  - PBI: Add regression tests for decimal input, serialization, persistence, History display, and Statistics calculations.

### Week 7 Execution Result

- **Execution status:** Completed
- **Date:** 18 July 2026
- **Executed by:** Customer with guidance from the team representative
- **Evidence:** [Week 7 Sprint Review transcript](../reports/week7/sprint-review-transcript.md)
- **Result:** Passed
- **Customer comments or observed issues:**
  - The customer entered 5.6 litres of fuel and an expense of 1288.1 RUB.
  - The record was saved successfully, and the customer confirmed that the decimal values were visible, including in Statistics.
- **Related GitHub Issues:**
  - [#264](https://github.com/LAMBA-23/LAMBA/issues/264) — Fix fractional fuel-value persistence and display.
  - [#313](https://github.com/LAMBA-23/LAMBA/issues/313) — Support decimal numeric fields.
  - [#314](https://github.com/LAMBA-23/LAMBA/issues/314) — Verify decimal values across persistence, History, and Statistics.

## UAT-010: Record a trip using start and end odometer values

- **Scenario ID:** UAT-010
- **Status:** Active
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

- **Execution status:** Not executed - implementation was incomplete
- **Date:** 10 July 2026
- **Executed by:** Not executed
- **Evidence:** private Week 6 meeting recording and sanitized transcript
- **Result:** Not executed / Needs implementation
- **Customer comments or observed issues:**
  - Odometer-based trip entry was still incomplete.
  - The customer confirmed it should be completed before handover.
- **Traceability:** Product Backlog Item listed below.
- **Resulting PBIs:**
  - PBI: Complete trip recording using start and end odometer values.

### Week 7 Execution Result

- **Execution status:** Partially completed
- **Date:** 18 July 2026
- **Executed by:** Customer with guidance from the team representative
- **Evidence:** [Week 7 Sprint Review transcript](../reports/week7/sprint-review-transcript.md)
- **Result:** Passed
- **Customer comments or observed issues:**
  - The customer successfully completed the start/end odometer trip flow and confirmed that the trip was added.
  - The trip appeared in History and updated Statistics.
  - Validation of an end odometer value lower than the start value was not tested.
  - Adding another record while a trip was active was discussed but was not executed.
- **Related GitHub Issues:**
  - [#272](https://github.com/LAMBA-23/LAMBA/issues/272) — Support trip recording using start and end odometer values.
  - [#331](https://github.com/LAMBA-23/LAMBA/issues/331) — Improve the trip start/end odometer flow.

## UAT-011: Repair/Breakdown record with photo attachment

- **Scenario ID:** UAT-011
- **Status:** Active
- **User goal:** A user can create a Repair/Breakdown record with repair details, decimal repair expense when applicable, and a photo attachment, then see it in History.
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
| 3 | Enter repair details, mileage, decimal repair expense when applicable, or other required fields. | The form accepts valid input and validates required fields. |
| 4 | Attach one or more photos. | The selected photo attachments are shown before saving. |
| 5 | Save the record. | The record is saved successfully. |
| 6 | Open History and the record details. | The record appears with the Repair/Breakdown label and the attached photo is visible or accessible from details. |

### Week 6 Execution Result

- **Execution status:** Partially attempted
- **Date:** 10 July 2026
- **Executed by:** customer with guidance from the team representative
- **Evidence:** private Week 6 meeting recording and sanitized transcript
- **Result:** Needs changes
- **Customer comments or observed issues:**
  - Repair and Breakdown entry was not working correctly.
  - The customer attempted to test a repair expense, but the full scenario was not completed.
  - Photo attachment support was incomplete.
  - Zero-amount entries are intentionally hidden.
- **Traceability:** Product Backlog Items listed below.
- **Resulting PBIs:**
  - PBI: Fix Repair/Breakdown record creation and display.
  - PBI: Complete photo upload, persistence, and display.
  - PBI: Verify decimal repair expenses.
  - PBI: Re-execute the complete scenario during Week 7.

### Week 7 Execution Result

- **Execution status:** Partially completed
- **Date:** 18 July 2026
- **Executed by:** Customer with guidance from the team representative
- **Evidence:** [Week 7 Sprint Review transcript](../reports/week7/sprint-review-transcript.md)
- **Result:** Passed
- **Customer comments or observed issues:**
  - The customer created a breakdown record and attached a sample photo.
  - The photo uploaded and displayed successfully after a short wait.
  - The customer was initially unsure whether the photo had finished loading, but confirmed that the result was good after it appeared.
  - The repair-photo flow and decimal repair expense were not tested.
- **Related GitHub Issues:**
  - [#273](https://github.com/LAMBA-23/LAMBA/issues/273) — Complete Repair/Breakdown records with photo attachments.
  - [#323](https://github.com/LAMBA-23/LAMBA/issues/323) — Complete breakdown photo support.
  - [#332](https://github.com/LAMBA-23/LAMBA/issues/332) — Harden backend photo handling.
  - [#337](https://github.com/LAMBA-23/LAMBA/issues/337) — Fix breakdown photo display.
  - [#340](https://github.com/LAMBA-23/LAMBA/issues/340) — Display the full breakdown photo without cropping.
  - No dedicated issue identified during this analysis for the loading-state observation.

## UAT-012: Stay logged in after closing the app and log out manually

- **Scenario ID:** UAT-012
- **Status:** Active
- **User goal:** A user remains logged in after closing the app, can manually log out when needed, and has local chat history cleared after logout.
- **Preconditions:**
  - The app is installed or available in the test environment.
  - The user has a valid account.
  - Session persistence is implemented.
  - A Log out action is available.

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Log in with valid credentials. | The app opens the authenticated main flow. |
| 2 | Close and reopen the app. | The app opens without asking the user to log in again. |
| 3 | Use the available Log out action. | The app ends the session and returns to the login or welcome screen. |
| 4 | Confirm local chat history after logout. | Locally stored chat history is cleared. |
| 5 | Try to access protected screens after logout. | Protected screens are not accessible until the user logs in again. |

### Week 6 Execution Result

- **Execution status:** Completed
- **Date:** 10 July 2026
- **Executed by:** customer with guidance from the team representative
- **Evidence:** private Week 6 meeting recording and sanitized transcript
- **Result:** Passed
- **Customer comments or observed issues:**
  - Session persistence was successfully demonstrated.
  - Closing and reopening the application did not require another login.
  - Manual logout works.
  - Logout clears locally stored chat history.
- **Traceability:** Product Backlog Item coverage for session persistence and logout behavior.
- **Resulting PBIs:** No new PBIs were created.

### Week 7 Execution Result

- **Execution status:** Not executed
- **Date:** 18 July 2026
- **Executed by:** Customer with guidance from the team representative
- **Evidence:** [Week 7 Sprint Review transcript](../reports/week7/sprint-review-transcript.md)
- **Result:** Not executed
- **Customer comments or observed issues:** Session persistence, completed logout, protected-screen access after logout, and local-chat cleanup were not tested during Week 7.
- **Related GitHub Issues:** No new issue was identified because this scenario was not executed.

## UAT-013: Manage account and vehicle settings

- **Scenario ID:** UAT-013
- **Status:** Active
- **User goal:** A logged-in vehicle owner can view account data, update allowed vehicle data, change a password, and log out from one profile screen.
- **Preconditions:**
  - The user is logged in and has a vehicle.

| Step | Action | Expected result |
|---|---|---|
| 1 | Open the side menu and select the user account block. | The Profile screen opens and displays the username without allowing edits. |
| 2 | Update the vehicle brand, model, and production year, then save. | The app displays the saved vehicle data. |
| 3 | Before adding history, change the mileage and save. | The new initial mileage is saved. |
| 4 | Add any fuel, repair, trip, or breakdown record, then reopen Profile. | Mileage is read-only; brand, model, and year remain editable. |
| 5 | Enter an incorrect current password and submit matching valid new passwords. | The password remains unchanged and the app shows a neutral error. |
| 6 | Enter the correct current password and matching new passwords of 8–128 characters. | The password changes, local session data is cleared, and Login opens. |
| 7 | Log in again, open Profile, select logout, and confirm. | Local session and chats are cleared and the Welcome screen opens. |

- **Traceability:** US-15, GitHub Issue #329.
- **Resulting PBIs/issues:** No new PBIs or issues were created.

### Week 7 Execution Result

- **Execution status:** Partially completed
- **Date:** 18 July 2026
- **Executed by:** Customer with guidance from the team representative
- **Evidence:** [Week 7 Sprint Review transcript](../reports/week7/sprint-review-transcript.md)
- **Result:** Passed
- **Passed steps:**
  - The Profile screen was opened, and the username and local avatar were displayed.
  - The customer entered an incorrect current password, and the validation error was handled correctly.
- **Not executed:**
  - A successful password change with the correct current password.
  - Editing vehicle data.
  - Completing logout and verifying the resulting session and local-chat state.
- **Customer comments or observed issues:** The customer said that the incorrect-password error was handled correctly and that the updated interface looked good.
- **Related GitHub Issues:**
  - [#329](https://github.com/LAMBA-23/LAMBA/issues/329) — Add account and vehicle profile management.
  - [#351](https://github.com/LAMBA-23/LAMBA/issues/351) — Improve the Profile UI and local avatar support.

## UAT-014: Dictate an editable chat message

- **Scenario ID:** UAT-014
- **Status:** Active
- **User goal:** A logged-in user can dictate a chat message, review the recognised text,
  and still type or edit it before sending.
- **Preconditions:**
  - The user is logged in and has opened the chat screen.
  - The device has a working microphone and network access to the backend.
  - A backend Mistral key is configured; no key is present in the Android application.
- **Steps:**
  1. Tap the microphone icon next to the chat input and grant microphone access when prompted.
  2. Speak a short message containing a hesitation sound or repeated word.
  3. Tap the microphone icon again to stop recording.
  4. Confirm the recognised, cleaned text appears in the input field and has not been sent.
  5. Edit the text if desired, then send it with the usual send button.
  6. Repeat after denying microphone access or with the transcription service unavailable.
- **Expected result:**
  - The transcript preserves the message facts while removing obvious hesitation sounds,
    repetitions, and punctuation mistakes.
  - The message is sent only after the user taps the existing send button.
  - Permission and service errors are shown clearly, and manual text input remains usable.
- **Traceability:** US-10, GitHub Issue #53.

### Week 7 Execution Result

- **Execution status:** Partially completed
- **Date:** 18 July 2026
- **Executed by:** Customer with guidance from the team representative
- **Evidence:** [Week 7 Sprint Review transcript](../reports/week7/sprint-review-transcript.md)
- **Result:** Passed
- **Customer comments or observed issues:**
  - The first recognition attempt was inaccurate.
  - The second attempt recognised a refuelling message, and the resulting record was saved.
  - The customer confirmed that voice input worked.
  - Editing recognised text before sending, microphone-permission denial, and transcription-service failure were not tested.
- **Related GitHub Issues:**
  - [#53](https://github.com/LAMBA-23/LAMBA/issues/53) — Use voice messages.

## UAT-015: Export vehicle data to an Excel workbook

- **Scenario ID:** UAT-015
- **Status:** Active
- **User goal:** A logged-in vehicle owner can export vehicle and history data to an Excel workbook and use the exported file outside the application.
- **Preconditions:**
  - The user is logged in and has a vehicle profile.
  - Vehicle history data is available for export.
  - The Profile screen and export action are available.
  - The device provides a location where the workbook can be saved.

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Open the Profile screen. | The Profile screen opens and displays the vehicle-data export action. |
| 2 | Start the vehicle-data export. | The application starts preparing an Excel workbook. |
| 3 | Select a save location. | The user can choose where the workbook will be saved. |
| 4 | Confirm the export and wait for generation to finish. | The workbook is generated and saved without blocking or crashing the application. |
| 5 | Open the workbook and review the exported vehicle data. | The workbook contains the expected vehicle profile data. |
| 6 | Review the exported history. | The workbook contains the expected vehicle-history records and values. |
| 7 | Open, copy, or share the exported file outside the application. | The saved workbook is available to another application or recipient, such as a repair shop. |

### Week 7 Execution Result

- **Execution status:** Partially completed
- **Date:** 18 July 2026
- **Executed by:** Customer with guidance from the team representative
- **Evidence:** [Week 7 Sprint Review transcript](../reports/week7/sprint-review-transcript.md)
- **Result:** Passed
- **Customer comments or observed issues:**
  - The export feature was demonstrated, and the customer considered it useful for sharing vehicle history with a repair shop.
  - The transcript does not confirm that the customer independently selected a save location, generated and opened the workbook, verified all exported data, or shared the file outside the application.
- **Related GitHub Issues:**
  - [#339](https://github.com/LAMBA-23/LAMBA/issues/339) — Export vehicle data to Excel.
  - [#356](https://github.com/LAMBA-23/LAMBA/issues/356) — Fix vehicle-data export after UI refactoring.

## UAT-016: Select and verify an assistant communication style

- **Scenario ID:** UAT-016
- **Status:** Active
- **User goal:** A user can select an assistant communication style and receive factually consistent responses in the selected tone.
- **Preconditions:**
  - The user is logged in and has opened the chat screen.
  - The assistant service is available.
  - More than one communication style is available for selection.

| Step | User action | Expected outcome |
|---|---|---|
| 1 | Select a communication style. | The selected style is visibly active for subsequent assistant responses. |
| 2 | Send a neutral prompt to the assistant. | The assistant returns a relevant response using the selected style. |
| 3 | Review the response tone and facts. | The tone clearly matches the selected style while the factual content remains correct. |
| 4 | Switch to a different communication style. | The new style becomes active without interrupting the chat flow. |
| 5 | Send the same or an equivalent neutral prompt. | The tone changes to match the new style while factual correctness remains unchanged. |
| 6 | Leave and reopen the chat. | The style selection follows the application's defined persistence behaviour. |

### Week 7 Execution Result

- **Execution status:** Partially completed
- **Date:** 18 July 2026
- **Executed by:** Customer with guidance from the team representative
- **Evidence:** [Week 7 Sprint Review transcript](../reports/week7/sprint-review-transcript.md)
- **Result:** Passed
- **Customer comments or observed issues:**
  - The customer observed a sarcastic response in the selected Selfish style and confirmed that the style was clearly visible.
  - Other styles, switching between styles, factual equivalence across styles, and persistence behaviour were not tested.
- **Related GitHub Issues:**
  - [#347](https://github.com/LAMBA-23/LAMBA/issues/347) — Add selectable assistant communication styles.
  - [#353](https://github.com/LAMBA-23/LAMBA/issues/353) — Apply the selected style to assistant responses.

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

- **Scenarios executed:** UAT-005, UAT-006, UAT-008, UAT-009, UAT-011, UAT-012. UAT-007 and UAT-010 were not executed as functional tests.
- **Passed:**
  - UAT-008
  - UAT-012
- **Passed with follow-up item:**
  - UAT-006
- **Needs changes:**
  - UAT-005
  - UAT-009
  - UAT-011
- **Not executed because implementation was incomplete:**
  - UAT-007
  - UAT-010
- **Most important feedback points:**
  - Recommendations should become simple rule-based notifications instead of proactive AI chat messages.
  - Fractional fuel values are mandatory.
  - Monetary values must support rubles and kopecks.
  - Newly created records must appear consistently in both History and Statistics.
  - Compatibility or migration of older customer accounts must be verified.
  - Odometer-based trips must be completed.
  - Repair/Breakdown functionality and photo attachments must be completed.
  - AI responses should become more natural and conversational.
  - The customer estimated overall product readiness at approximately 80%.
- **Resulting Product Backlog Items (PBIs):**
  - PBI: Fix fractional fuel-value persistence and display.
  - PBI: Support decimal monetary amounts (rubles and kopecks).
  - PBI: Fix History display and synchronization for newly created records.
  - PBI: Verify compatibility or migration of older customer accounts.
  - PBI: Complete odometer-based trip recording.
  - PBI: Complete Repair/Breakdown functionality.
  - PBI: Complete photo attachment support.
  - PBI: Implement rule-based recommendation notifications.
  - PBI: Improve the AI prompt to produce more natural conversational responses.

## Week 7 UAT Summary

- **Passed:**
  - UAT-009
- **Partially passed:**
  - UAT-005
  - UAT-006
  - UAT-007
  - UAT-010
  - UAT-011
  - UAT-013
  - UAT-014
  - UAT-015
  - UAT-016
- **Not executed:**
  - UAT-001
  - UAT-002
  - UAT-003
  - UAT-004
  - UAT-008
  - UAT-012
- **Failed:** None confirmed by the Week 7 transcript.
- **Final customer confirmation:** The customer accepted the final product. However, customer acceptance does not imply that every maintained UAT scenario was fully executed during Week 7.
