# Reverse-Engineering Artifact Contract

Use these formats after discovery and aggregation. They mirror the forward AIUP skills.

## Contents

- Requirements HTML and use-case diagram
- Use-case specification files
- Entity model
- Type and relationship mapping

## Requirements HTML and Use-Case Diagram

Create or update `docs/requirements.html` as valid semantic HTML. Preserve existing requirements content. The diagram has one stable location:

```html
<section id="use-case-diagram">
  <h2>Use Case Diagram</h2>
  <pre class="mermaid">graph LR
    customer(("Customer"))
    admin(("Administrator"))

    subgraph "System Name"
        UC001(["UC-001\nPlace Order"])
        UC002(["UC-002\nManage Catalog"])
    end

    customer --> UC001
    admin --> UC002
  </pre>
</section>
```

Keep exactly one element with `id="use-case-diagram"` and one `<pre class="mermaid">` inside it. Connect every actor and use case. Do not create a separate diagram file.

When no prior requirements catalog exists, create minimal provenance sections before the diagram:

```html
<section id="functional-requirements"><h2>Functional Requirements</h2><p>Recovered from existing behaviour; see use-case specifications.</p></section>
<section id="non-functional-requirements"><h2>Non-Functional Requirements</h2><p>Not recovered unless directly evidenced by code or configuration.</p></section>
<section id="constraints"><h2>Constraints</h2><p>Not recovered unless directly evidenced by the repository.</p></section>
```

## Use-Case Specification Files

Name each file `docs/use_cases/UC-XXX-<diagram-name-in-kebab-case>.md`. Use exactly one use case per file.

Required structure:

````markdown
# Use Case: [Name]

## Overview

**Use Case ID:** UC-XXX
**Use Case Name:** [Name]
**Primary Actor:** [Role]
**Goal:** [Observable outcome]
**Status:** Implemented | Draft
**Stakeholders:** [Affected roles/groups]
**Trigger:** [Starting event/action]

## Preconditions

- [Verifiable fact]

## Main Success Scenario

1. [Actor action]
2. [System response]

## Alternative Flows

### A1: [Name]

**Trigger:** [Condition] (step N)
**Flow:**

1. [Divergent behaviour]
2. Use case continues at step N. *(or: Use case ends.)*

## Postconditions

### Success Postconditions

- [Result]

### Failure Postconditions

- [Failure state]

## Business Rules

### BR-XXX: [Name]

[Rule]
````

Use business language. For example, translate `POST /orders` into “System creates the order”, a validation exception into the rejected condition, and a mail call into “System sends a confirmation email”.

## Entity Model

Create `docs/entity_model.md` with relationships only in Mermaid and a five-column table for every entity:

````markdown
# Entity Model

## Entity Relationship Diagram

```mermaid
erDiagram
    AUTHOR ||--o{ BOOK : "writes"
```

### BOOK

A title available in the catalogue.

| Attribute | Description | Data Type | Length/Precision | Validation Rules |
|---|---|---|---|---|
| id | Unique identifier | Long | 19 | Primary Key, Sequence |
| title | Book title | String | 200 | Not Null |
| author_id | Author | Long | 19 | Not Null, Foreign Key (AUTHOR.id) |
````

Allowed data types: `Long`, `String`, `Integer`, `Decimal`, `Boolean`, `Date`, `DateTime`.

Allowed validation forms:

- `Primary Key, Sequence`
- `Not Null`
- `Not Null, Unique`
- `Not Null, Foreign Key (TABLE.id)`
- `Optional`
- `Not Null, Min: X, Max: Y`
- `Not Null, Values: A, B, C`
- `Not Null, Format: Email`

Never leave validation empty. Add `**Constraints:** ...` after a table for multi-column rules.

## Type and Relationship Mapping

Map SQL/ORM primitives rather than copying them:

| Source signal | AIUP representation |
|---|---|
| bigint/autoincrement ID | `Long`, `19`, `Primary Key, Sequence` |
| varchar/string | `String`, actual/default length, nullability validation |
| integer | `Integer`, `10` |
| decimal/numeric(p,s) | `Decimal`, `p,s` |
| timestamp/datetime | `DateTime`, `-` |
| date | `Date`, `-` |
| boolean | `Boolean`, `1` |

Relationship signals:

| Source signal | Mermaid relationship |
|---|---|
| required foreign key / required many-to-one | `A ||--o{ B` |
| nullable foreign key / optional many-to-one | `A |o--o{ B` |
| unique foreign key / one-to-one | `A ||--|| B` |
| many-to-many | `A }o--o{ B` via the domain join entity |
