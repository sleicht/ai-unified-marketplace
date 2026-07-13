---
name: use-case-diagram
description: >
  Creates or updates Mermaid use case diagrams defining actors, use cases,
  and their relationships from requirements. Use when the user asks to
  "create a use case diagram", "draw a UML diagram", "map actors to use cases",
  or mentions Mermaid, use case overview, actor diagram, or system use cases.
  Use requirements instead when the request is to create the requirements
  catalog itself rather than visualise already documented functional requirements.
---

# Use Case Diagram

## Instructions

Create or update the Mermaid use case diagram embedded in the resolved `requirements.html` based on the requirements catalog. Update only the `<pre class="mermaid">` inside `<section id="use-case-diagram">`; preserve the rest of the document.

## DO NOT

- Create diagrams without reading the requirements first
- Use non-standard Mermaid syntax
- Include implementation details in use case names
- Create a second `use-case-diagram` section or Mermaid block

## Path Resolution

If a service/module is in scope or cwd is inside a monorepo service, read and update `<service>/docs/requirements.html`; otherwise use `docs/requirements.html`. Detect monorepo services from `mise.toml` (`monorepo_root` or namespaced tasks) or multiple sibling `settings.gradle.kts` builds.

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

1. Resolve docs path: `<service>/docs/requirements.html` for a scoped monorepo service, otherwise `docs/requirements.html`.
2. Read the requirements catalog.
3. Locate the single `<section id="use-case-diagram">` and its `<pre class="mermaid">`. Create that stable section before `</main>` only if it is absent.
4. Identify actors and use cases from requirements.
5. Replace only the Mermaid text inside that stable section.
6. Validate the diagram:
    - Each use case traces to at least one functional requirement in `requirements.html`
    - All actors are connected to at least one use case
    - Use case IDs follow the UC-{3-digit} convention
    - Mermaid syntax is valid
    - Exactly one element has `id="use-case-diagram"`
