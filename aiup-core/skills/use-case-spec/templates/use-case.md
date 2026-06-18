# Use Case: [Use Case Name]

## Overview

**Use Case ID:** UC-XXX   
**Use Case Name:** [Descriptive Name]   
**Primary Actor:** [Role]   
**Goal:** [What the actor wants to achieve]   
**Status:** Draft | Reviewed | Approved | Implemented | Tested | Done | Obsolete   
**Stakeholders:** [Stakeholder roles or groups affected by the use case]   
**Trigger:** [Event or actor action that starts the use case]

## Preconditions

- [Condition that must be true before the use case starts]

## Main Success Scenario

1. [Actor action or system response]
2. [Next step]
3. [Continue until goal is achieved]

## Alternative Flows

### A1: [Alternative Flow Name]

**Trigger:** [Condition that triggers this flow]
**Flow:**

1. [Step that diverges from main flow]
2. [Continuation or return to main flow]

## Postconditions

### Success Postconditions

- [State of the system after successful completion]

### Failure Postconditions

- [State of the system if the use case fails]

## Business Rules

### BR-XXX: [Rule Name]

[Description of the business rule that applies to this use case]

---

## Reference

### Status Values

| Status      | Description                                      |
|-------------|--------------------------------------------------|
| Draft       | Initial version, still being written.            |
| Reviewed    | Complete, awaiting stakeholder review.           |
| Approved    | Reviewed and approved for implementation.        |
| Implemented | Implementation complete, pending testing.        |
| Tested      | All tests pass, pending final acceptance.        |
| Done        | Fully implemented, tested, and accepted.         |
| Obsolete    | No longer valid, superseded by another use case. |

### German Section Names

| English | German |
|---------|--------|
| Overview | Überblick |
| Stakeholders | Stakeholder |
| Trigger | Auslöser |
| Preconditions | Vorbedingungen |
| Main Success Scenario | Standardablauf |
| Alternative Flows | Alternative Abläufe |
| Postconditions | Nachbedingungen |
| Business Rules | Geschäftsregeln |

### Step Writing Guidelines

| Do                                  | Don't                                         |
|-------------------------------------|-----------------------------------------------|
| "User clicks Save button"           | "User triggers onClick handler"               |
| "System validates the email format" | "System runs regex /^[\w]+@[\w]+$/"           |
| "System displays error message"     | "System throws ValidationException"           |
| "User enters check-in date"         | "User populates dateField component"          |
| "System stores the reservation"     | "System executes INSERT INTO reservations..." |
