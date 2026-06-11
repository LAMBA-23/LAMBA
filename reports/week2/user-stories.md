# Relevant user roles or personas:

   - New user - a person who uses the application for the first time;
   - Vehicle owner - the main user who owns a vehicle.


# User stories:

## US-01: User registration

**Requirement status:** Active
**MoSCoW priority:** Must Have

As a new user,
I want to register and log into my account,
so that I can access the application and store my vehicle data.

### Notes and constraints

A user account is required before accessing the main functionality of the application.

## US-02: Add a vehicle

**Requirement status:** Active
**MoSCoW priority:** Must Have

As a vehicle owner,
I want to add my vehicle's brand, model, production year, and current mileage,
so that the system can create a digital profile of my vehicle.

### Notes and constraints

The MVP supports only one passenger vehicle per user.

## US-03: Send messages

**Requirement status:** Active
**MoSCoW priority:** Must Have

As a vehicle owner,
I want to send messages to the AI assistant,
so that I can interact with the application without filling forms.

### Notes and constraints

The chat is the primary user interface of the application.

## US-04: Automatically create records

**Requirement status:** Active
**MoSCoW priority:** Must Have

As a vehicle owner,
I want to convert my messages into structured vehicle records using the AI assistant,
so that vehicle events are saved automatically.

### Notes and constraints

Supported events include refueling, repairs, trips, issues, and technical condition updates.

## US-05: View vehicle timeline

**Requirement status:** Active
**MoSCoW priority:** Must Have

As a vehicle owner,
I want to see my vehicle timeline in chronological order,
so that I can view the complete history of my car in one place.

### Notes and constraints

The full vehicle history must be stored and displayed to the user.

## US-06: Ask AI assistant

**Requirement status:** Active
**MoSCoW priority:** Must Have

As a vehicle owner,
I want to ask the AI assistant questions about my vehicle history,
so that I can quickly find information without manually searching records.

### Notes and constraints

Example questions include repairs, refueling history, and current mileage.

## US-07: View basic statistics

**Requirement status:** Active
**MoSCoW priority:** Must Have

As a vehicle owner,
I want to view statistics based on my vehicle history,
so that I can monitor expenses and vehicle usage.

### Notes and constraints

Statistics may include fuel expenses, repair expenses, number of trips, and total recorded mileage.

## US-08: Receive maintenance recommendations

**Requirement status:** Active
**MoSCoW priority:** Should Have

As a vehicle owner,
I want to receive maintenance recommendations based on my vehicle history,
so that I can prevent potential problems before they become serious.

### Notes and constraints

Recommendations should be based on the user's recorded vehicle history.

## US-09: Receive notifications

**Requirement status:** Active
**MoSCoW priority:** Should Have

As a vehicle owner,
I want to receive notifications about upcoming maintenance,
so that I do not miss important service dates.

### Notes and constraints

Notifications should be generated based on vehicle history and recorded maintenance events.

## US-10: Use voice messages

**Requirement status:** Active
**MoSCoW priority:** Could Have

As a vehicle owner,
I want to send voice messages to the AI assistant,
so that I can record vehicle events more conveniently.

### Notes and constraints

Requires STT(speech-to-text).

## US-11: Attach repair receipts

**Requirement status:** Active
**MoSCoW priority:** Could Have

As a vehicle owner,
I want to attach a repair receipt in chat,
so that the system can automatically extract repair information.

### Notes and constraints

Relevant repair information should be captured from the receipt.

## US-12: OBD-II integration

**Requirement status:** Removed
**Previous MoSCoW priority:** Could Have

As a vehicle owner,
I want the application to automatically collect vehicle data through an OBD-II device,
so that I do not need to enter information manually.

**Reason:** During customer interviews, OBD-II integration was discussed but later excluded because it is complex to implement.

## US-13: Manage multiple vehicles

**Requirement status:** Active
**MoSCoW priority:** Won't Have

As a vehicle owner,
I want to manage multiple vehicles in a single account,
so that I can maintain the history of all my vehicles in one place.

### Notes and constraints

The MVP supports only one passenger vehicle per user.

## US-14: Transfer vehicle history to a new owner

**Requirement status:** Active
**MoSCoW priority:** Won't Have

As a vehicle owner,
I want to transfer my car's digital history to a new owner,
so that the vehicle records remain available after the car is sold.

### Notes and constraints

The customer stated that it is not required for the MVP.

# Initial proposed MVP v1 scope

Selected stable IDs: US-01, US-02, US-03, US-04, US-05.
