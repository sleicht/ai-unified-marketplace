---
name: requirements
description: >
  Gathers, organizes, and documents software requirements into structured
  catalogs with functional requirements (user stories), non-functional
  requirements (measurable quality attributes), and constraints. Use when
  the user asks to "write requirements", "create a PRD", "gather requirements",
  "document feature specs", "write user stories", "define NFRs", "list
  constraints", or mentions requirements catalog, requirements analysis,
  product requirements document, or feature specification. Use use-case-spec
  instead when the request is for detailed actor scenarios, alternative flows,
  or postconditions for an already identified use case.
---

# Requirements

## Instructions

Create or update the requirements catalog based on the project vision. Resolve the output path first: if a service/module is in scope or cwd is inside a monorepo service, write `<service>/docs/requirements.html`; otherwise write `docs/requirements.html`. Detect monorepo services from `mise.toml` (`monorepo_root` or namespaced tasks) or multiple sibling `settings.gradle.kts` builds.
The document contains functional requirements, non-functional requirements, constraints, and the stable use-case-diagram slot as semantic HTML. Detect existing docs language when updating; default to English for new docs unless the user asks for another language.

## HTML Artifact Contract

Use one valid, self-contained HTML document. Preserve these section IDs when updating because downstream skills use them as stable anchors:

```html
<main>
  <h1>Requirements</h1>
  <section id="functional-requirements"><h2>Functional Requirements</h2>...</section>
  <section id="non-functional-requirements"><h2>Non-Functional Requirements</h2>...</section>
  <section id="constraints"><h2>Constraints</h2>...</section>
  <section id="requirements-notes"><h2>Requirements Notes</h2>...</section>
  <section id="use-case-diagram">
    <h2>Use Case Diagram</h2>
    <pre class="mermaid">graph LR
    </pre>
  </section>
</main>
```

Render each catalog as a semantic `<table>` with `<thead>`, `<tbody>`, `<tr>`, `<th>`, and `<td>` elements. Do not place Markdown tables inside the HTML file. Keep the Mermaid source as text inside the single `<pre class="mermaid">` under `#use-case-diagram`; leave it empty when no diagram exists yet.

## DO NOT

- Mix requirement types in a single table
- Skip the user story format for functional requirements
- Use duplicate IDs across requirement types
- Leave the Status column empty

## Requirement Types

### Functional Requirements (FR)

Define what the system should do. Always use the user story format:

**Format:** As a [role], I want [goal] so that [benefit].

```html
<table>
  <thead><tr><th>ID</th><th>Title</th><th>User Story</th><th>Priority</th><th>Status</th></tr></thead>
  <tbody>
    <tr><td>FR-001</td><td>Create Task</td><td>As a project manager, I want to create tasks so that I can track work items.</td><td>High</td><td>Open</td></tr>
  </tbody>
</table>
```

### Non-Functional Requirements (NFR)

Define quality attributes. Must be measurable.

Use columns `ID`, `Title`, `Requirement`, `Category`, `Priority`, and `Status`. Example: `NFR-001 | Response Time | All page loads must complete within 2 seconds. | Performance | High | Open`, expressed as HTML cells.

### Constraints (C)

Define limitations and boundaries imposed on the solution.

Use columns `ID`, `Title`, `Constraint`, `Category`, `Priority`, and `Status`. Example: `C-001 | Runtime Platform | Backend must run on Java 21 LTS. | Technical | High | Open`, expressed as HTML cells.

## Language Variants

Default to English. For German docs, use these vocabulary variants and keep domain terms untranslated:

| Concept | English | German |
|---|---|---|
| Status values | Open / In Progress / Implemented / Verified / Deferred / Rejected | Offen / Teilweise / Umgesetzt / Bestätigt / Abgelehnt / Ersetzt |
| Priority values | High / Medium / Low | Hoch / Mittel / Niedrig |
| Functional story | As a [role], I want [goal] so that [benefit]. | Als [Rolle] möchte ich [Ziel], damit [Nutzen]. |

## Reference

See [references/REFERENCE.md](references/REFERENCE.md) for ID prefixes, priority levels, status values, NFR categories, and constraint
categories.

## Requirement Quality Checks

Every requirement must pass these checks before finalizing:

| Check       | Rule                                 | Bad Example                          | Good Example                  |
|-------------|--------------------------------------|--------------------------------------|-------------------------------|
| Measurable  | NFRs must have a number or threshold | "System should be fast"              | "Pages load within 2 seconds" |
| Singular    | One requirement per row              | "System must log in and export data" | Split into FR-001 and FR-002  |
| Unambiguous | No subjective terms                  | "User-friendly interface"            | "WCAG 2.1 AA compliant"       |
| Testable    | Can write a pass/fail test           | "System is reliable"                 | "99.9% uptime over 30 days"   |
| Unique IDs  | No duplicate IDs across all tables   | Two FR-001 entries                   | Each ID used exactly once     |

## Error Recovery

Record assumptions, rewrites, and unresolved conflicts under `<section id="requirements-notes">`; do not create a separate Markdown requirements or notes file.

- **Incomplete source document**: List what is missing (roles, NFR categories, constraints) and ask the user to clarify
  before proceeding
- **Ambiguous requirement from user**: Rewrite it as a measurable requirement and ask the user to confirm the threshold
- **Conflicting requirements**: Flag the conflict explicitly (e.g., "FR-003 requires real-time sync but C-002 limits to
  batch processing") and ask the user to resolve
- **Missing stakeholder roles**: Default to generic roles (User, Admin, System) and note them for user review

> **Format survives error recovery.** Ambiguity, conflict, and provisional
> status never justify abandoning the user-story form. Every FR row — even one
> you are flagging as conflicting or unconfirmed — must still read
> "As a [role], I want [goal] so that [benefit]." Record the issue in a note or
> in the Status column (e.g., `Conflict`, `Needs review`); never by dropping the
> requirement to a flat statement like "Support real-time sync."

## Workflow

1. Resolve docs path: `<service>/docs/` for a scoped monorepo service, otherwise `docs/`.
2. Detect existing docs language or user-requested language; default to English.
3. Read the vision document or project brief from the resolved docs path.
4. Track each requirement type with the available planning/task mechanism when the environment provides one.
5. Write the document header.
6. For functional requirements:
    - Identify user roles
    - Define user stories with clear goals and benefits
    - Assign priorities based on business value
7. For non-functional requirements:
    - Define measurable quality attributes
    - Categorize by NFR type
    - Ensure requirements are testable
8. For constraints:
    - Document technical and business limitations
    - Categorize by constraint type
9. Validate: run every requirement against the quality checks table above
    - No duplicate IDs across all tables
    - All Status columns filled
    - **Hard gate:** every FR User Story matches the selected language's story
      format (English: "As a [role], I want [goal] so that [benefit]") — scan each
      row; any row missing the role / goal / benefit clauses is rejected and
      rewritten before finalizing, no exceptions
    - All NFRs contain a measurable threshold
10. Validate the HTML structure, unique section IDs, semantic tables, and the single `#use-case-diagram` Mermaid slot.
