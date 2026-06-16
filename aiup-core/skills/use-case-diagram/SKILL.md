---
name: use-case-diagram
description: >
  Creates or updates Mermaid use case diagrams defining actors, use cases,
  and their relationships from requirements. Use when the user asks to
  "create a use case diagram", "draw a UML diagram", "map actors to use cases",
  "generate a .puml file", or mentions Mermaid, use case overview, actor
  diagram, or system use cases.
---

# Use Case Diagram

## Instructions

Create or update the Mermaid use case diagram in `docs/requirements.html` based on `docs/requirements.html`.

## DO NOT

- Create diagrams without reading the requirements first
- Use non-standard Mermaid syntax
- Include implementation details in use case names

## Template

```Mermaid
graph LR
    user(("User"))
    admin(("Administrator"))

    subgraph "System Name"
        UC001(["UC-001\nDescription"])
        UC002(["UC-002\nDescription"])
        UC003(["UC-003\nDescription"])
    end

    admin --> UC001
    user --> UC002
    user --> UC003
```

## Conventions

- Each use case has a unique id and a description
- Use Case ID: UC-{3-digit} (UC-001, UC-002, ...)
- Each use case should trace to at least one functional requirement
- Add notes sparingly, only where relationships need clarification

## Workflow

1. Read the requirements at `docs/requirements.html`
2. Read existing diagram in `docs/requirements.puml` (if exists)
3. Identify actors and use cases from requirements
4. Create/update the Mermaid use case diagram
5. Validate the diagram:
    - Each use case traces to at least one functional requirement in `docs/requirements.html`
    - All actors are connected to at least one use case
    - Use case IDs follow the UC-{3-digit} convention
    - Mermaid syntax is valid (no missing `@enduml`, proper arrow syntax)