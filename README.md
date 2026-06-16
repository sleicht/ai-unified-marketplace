# AI Unified Process Marketplace

A collection of Claude Code plugins that automate the [**AI Unified Process (AIUP)**](https://unifiedprocess.ai) — a
structured workflow for taking a software project from raw vision to fully implemented, tested code, with requirements
at the center at every step.

## What is the AI Unified Process?

AI Unified Process is a disciplined AI-assisted development methodology where every project starts with a written
vision, proceeds
through requirements, an entity model, and use case specifications, and only then enters implementation. Nothing gets
built without a use case. Nothing reaches production without tests traceable to requirements.

This prevents the common failure mode of AI-assisted development: jumping straight to code from a vague prompt,
producing something that half-works and can't be maintained.

The methodology is based on the phases of
the [Rational Unified Process](https://en.wikipedia.org/wiki/Rational_unified_process) — **Inception, Elaboration,
Construction, Transition** — but adapted for AI-driven workflows.

## AIUP Workflow

```
Inception          Elaboration                          Construction
─────────────────  ──────────────────────────────────   ────────────────────────────────────────────────
/requirements  →  /entity-model  →  /use-case-diagram  →  /use-case-spec  →  /flyway-migration
                                                                          ↘  /implement
                                                                          ↘  /implement-ui  (Compose/Ktor)
                                                                          ↘  /browserless-test or /ktor-test
                                                                          ↘  /playwright-test or /compose-test
```

Each skill picks up where the previous one left off using the files produced along the way (`docs/vision.md`,
`docs/requirements.html`, `docs/entity_model.md`, `docs/use_cases/UC-*.md`). At any point you can
inspect or manually edit these files before continuing.

**Inheriting a legacy codebase?** Start with `/reverse-engineer` — it walks the existing code, configuration, and
schema and produces the same mermaid diagram in `docs/requirements.html`, `docs/use_cases/UC-*.md`, and `docs/entity_model.md` artifacts the
forward workflow would have produced, giving you a documented baseline to work from.

|                               | Inception       | Elaboration                            | Construction                                                                              | Transition |
|-------------------------------|-----------------|----------------------------------------|-------------------------------------------------------------------------------------------|------------|
| **aiup-core**                 | `/requirements` | `/entity-model`<br>`/use-case-diagram` | `/use-case-spec`                                                                          |            |
| **aiup-vaadin-jooq**          |                 |                                        | `/flyway-migration`<br>`/implement`<br>`/browserless-test`<br>`/playwright-test`          |            |
| **aiup-compose-ktor-exposed** |                 |                                        | `/flyway-migration`<br>`/implement`<br>`/implement-ui`<br>`/ktor-test`<br>`/compose-test` |            |

---

## Prerequisites

- [Claude Code](https://claude.ai/code) installed and running in your project
- A `docs/vision.md` file at the root of your project describing the product vision, target users, and high-level
  goals (the `/requirements` skill reads this file to derive your requirements catalog — the richer it is, the better
  the results)
- For the Vaadin/jOOQ plugin: a Maven or Gradle project with Vaadin and jOOQ already on the classpath
- For the Compose/Ktor/Exposed plugin: a Kotlin Multiplatform Gradle project with Compose Multiplatform, Ktor, Exposed,
  Flyway, and PostgreSQL conventions already present or planned

## Installation

```
/plugin marketplace add ai-unified-process/marketplace
/plugin install aiup-core
/plugin install aiup-vaadin-jooq              # Vaadin/jOOQ projects
/plugin install aiup-compose-ktor-exposed    # Compose/Ktor/Exposed projects
```

Install `aiup-core` plus the stack plugin you need. Use `aiup-vaadin-jooq` for Vaadin/jOOQ projects and
`aiup-compose-ktor-exposed` for Kotlin Multiplatform / Compose / Ktor / Exposed projects. Install only `aiup-core` if
using a different tech stack — the methodology skills are stack-agnostic.

### Verify installation

Start Claude Code in your project and run:

```
/requirements
```

If Claude begins reading `docs/vision.md` and proposing a requirements catalog, the skills are installed correctly.

---

## Using AIUP with other AI coding tools

The AI Unified Process is a methodology, not a Claude-only product. Agent Skills (`SKILL.md`) is now an open standard,
and the same skill folders in this marketplace work natively — with auto-triggering by description — in **OpenAI Codex
CLI**, **Cursor**, **GitHub Copilot**, and **Gemini CLI**. Pair them with the
[MCP](https://modelcontextprotocol.io) server configs and the whole workflow runs unchanged.

| Component                                                  | Portable? | Notes                                                                                  |
|------------------------------------------------------------|-----------|----------------------------------------------------------------------------------------|
| MCP servers (`aiup-*/.mcp.json`)                           | Yes       | Standard MCP — reformat the config per host                                            |
| `SKILL.md` skill folders (`aiup-*/skills/*/`)              | Yes       | Native support in Codex CLI, Cursor, Copilot, and Gemini CLI                           |
| Auto-triggering by `description`                           | Yes       | All four tools above match user intent against the YAML frontmatter `description`      |
| Workflow methodology (vision → requirements → … → tests)   | Yes       | The whole point — tool-agnostic                                                        |
| `/plugin marketplace add …` install                        | No        | Claude Code-specific — clone this repo instead                                         |

### Generic adoption recipe

1. `git clone https://github.com/ai-unified-process/marketplace.git` next to your project (or add as a submodule).
2. Make the skill folders visible to your tool — either copy `aiup-core/skills/*/`, `aiup-vaadin-jooq/skills/*/`,
   and/or `aiup-compose-ktor-exposed/skills/*/` into your tool's skills directory, or symlink them
   (e.g. `ln -s /path/to/marketplace/aiup-core/skills/requirements ~/.codex/skills/requirements`).
3. Configure the MCP servers from `aiup-core/.mcp.json` plus the stack plugin you installed
   (`aiup-vaadin-jooq/.mcp.json` or `aiup-compose-ktor-exposed/.mcp.json`) in your tool's MCP config file.
4. Trigger skills the same way you would in Claude Code — say "write requirements" or invoke `/requirements`. The tool
   matches your prompt against each skill's `description` and loads the matching `SKILL.md`. File outputs
   (`docs/requirements.html`, `docs/entity_model.md`, `docs/use_cases/UC-*.md`) are identical
   regardless of tool, so the chain composes even if you mix tools across steps.

### OpenAI Codex CLI

- **Skills**: drop folders into `~/.codex/skills/` (user-global, default `$CODEX_HOME/skills`) or repo-local
  `.agents/skills/`. Codex matches user prompts against each skill's `description` automatically; toggle per skill
  with `allow_implicit_invocation`.
- **MCP**: `~/.codex/config.toml` under `[mcp_servers.<name>]` blocks. Translate `aiup-vaadin-jooq/.mcp.json` like
  this:

```toml
[mcp_servers.Vaadin]
url = "https://mcp.vaadin.com/docs"

[mcp_servers.playwright]
command = "npx"
args = ["@playwright/mcp@latest"]
```

See the [Codex skills docs](https://developers.openai.com/codex/skills) and the
[Codex config reference](https://developers.openai.com/codex/config-reference) for the latest details.

### Cursor

- **Skills**: drop folders into project-local `.cursor/skills/` (Cursor 2.4+). Cursor matches against each skill's
  `description` automatically. Note: there is no global skills directory yet — copy or symlink the marketplace skills
  into each project.
- **MCP**: project-level `.cursor/mcp.json` or global `~/.cursor/mcp.json` — uses `mcpServers` with the same shape
  as Claude's `.mcp.json` (`url` for HTTP servers, `command` / `args` for stdio). Drop in the contents of
  `aiup-vaadin-jooq/.mcp.json` directly.

### GitHub Copilot

- **Skills**: Copilot reads from `.github/skills/`, `.claude/skills/`, and `.agents/skills/` — pick one. Available in
  Copilot for VS Code, Visual Studio 2026, and the cloud agent.
- **MCP**: workspace-level `.vscode/mcp.json` (commit it for your team), or user-level via
  *MCP: Open User Configuration*. Use `"type": "http"` for remote servers and `"command"` / `"args"` for stdio.

```jsonc
// .vscode/mcp.json
{
  "servers": {
    "Vaadin": { "type": "http", "url": "https://mcp.vaadin.com/docs" },
    "playwright": { "command": "npx", "args": ["@playwright/mcp@latest"] }
  }
}
```

### Gemini CLI

- **Skills**: drop folders into `.gemini/skills/` (project) or `~/.gemini/skills/` (global). Gemini CLI matches
  prompts against each skill's `description` automatically.
- **MCP**: `~/.gemini/settings.json` `mcpServers` object — same shape as Claude's `.mcp.json`, so it is a near-direct
  copy.

### Caveats

- **Argument passing** (`/use-case-spec UC-001`) works in all four tools but the syntax varies — Codex, Gemini CLI,
  and Cursor accept positional arguments after the skill name; in Copilot, pass the ID inline in the chat message
  after invoking the skill.
- **HTTP MCP servers**: most stack-plugin documentation servers are HTTP. Every tool listed above supports HTTP MCP. If you use
  a client that is stdio-only, you need an HTTP-to-stdio MCP bridge.
- **Cursor has no global skills directory** — copy or symlink the marketplace skills into each project's
  `.cursor/skills/`.
- **Methodology stays the same**: the file artifacts (`docs/*.md`, `docs/use_cases/UC-*.md`, Flyway migrations) are
  the contract between steps. As long as a step produces the right file, the next step works regardless of which
  tool ran the previous one.

---

## How to use?

Here is a complete end-to-end example of building a hotel reservation system.

### Step 1 — Generate the requirements catalog

```
/requirements
```

Claude reads `docs/vision.md`, identifies functional requirements (as user stories), non-functional requirements (
measurable quality attributes), and constraints, then writes them into `docs/requirements.html` as three separate tables.
Every requirement gets a stable ID (FR-001, NFR-001, CON-001) and a status. Review the catalog before continuing.

---

### Step 2 — Design the entity model

```
/entity-model
```

Claude reads `docs/requirements.html`, identifies the domain entities and their relationships, then writes
`docs/entity_model.md` with a Mermaid ER diagram and one attribute table per entity (data type, length/precision,
validation rules). Approve the model before moving to use cases.

---

### Step 3 — Draw the use case diagram

```
/use-case-diagram
```

Claude reads `docs/requirements.html`, identifies actors and use cases, then writes a Mermaid diagram at
`docs/requirements.html`. Each use case gets a stable ID (UC-001, UC-002, …) that traces back to one or more functional
requirements.

---

### Step 4 — Specify the use cases

```
/use-case-spec UC-001
/use-case-spec UC-001 UC-002 UC-003     # multiple at once
```

Claude writes a detailed specification per use case into `docs/use_cases/` covering actors, preconditions, the main
success scenario as numbered steps, alternative flows for error conditions, postconditions, and business rules. Each
spec is a single document — Claude will not bundle multiple use cases together.

---

### Step 5 — Create the database migrations

```
/flyway-migration
```

Claude reads `docs/entity_model.md` and writes versioned Flyway migrations (`V001__create_*.sql`, `V002__…`) into
the stack's migration directory.

- Vaadin/jOOQ projects use the existing Maven/Gradle layout and jOOQ-compatible SQL.
- Compose/Ktor/Exposed projects use PostgreSQL/Flyway style with `BIGSERIAL` primary keys, explicit constraints,
  indexes, `TIMESTAMPTZ` audit columns, `updated_at` triggers, and Exposed table compatibility.

---

### Step 6 — Implement the use case

```
/implement UC-001
```

Claude reads the use case spec, the entity model, and existing code to learn your conventions, then implements the use
case in the active stack plugin.

- Vaadin/jOOQ: implements the data access layer with jOOQ and the UI with Vaadin.
- Compose/Ktor/Exposed: implements backend code using shared DTOs, domain models, repository ports, Exposed
  persistence, application services, Ktor routes, and Koin wiring.

It compiles after each layer and stops on errors. It does **not** write tests — those have dedicated skills.

---

### Step 7 — Implement Compose UI *(Compose/Ktor/Exposed only)*

```
/implement-ui UC-001
```

Claude implements Compose Multiplatform UI code for the use case: Ktor API client calls, plain ViewModel state,
constructor-injected dependencies, small Material 3 composables, and shared DTO usage.

---

### Step 8 — Write server-side unit tests

```
/browserless-test UC-001   # Vaadin/jOOQ
/ktor-test UC-001          # Compose/Ktor/Exposed
```

For Vaadin/jOOQ, Claude generates server-side Vaadin tests using the official **Vaadin Browserless** framework
(`com.vaadin:browserless-test-junit6`) — no browser required. Tests cover navigation, component interactions, form
validation, grid operations, and notifications. Test data is seeded via Flyway migrations under
`src/test/resources/db/migration`; transaction boundaries are preserved (no `@Transactional` on tests).

For Compose/Ktor/Exposed, Claude generates Ktor `testApplication` tests with fake ports, route auth checks, and
Testcontainers/Flyway integration tests when persistence behavior needs a real PostgreSQL database.

> Browserless Testing is free and open source under Apache 2.0 since Vaadin 25.1. It is the official successor to UI
> Unit Testing (formerly part of the commercial TestBench) and replaces the community Karibu Testing library as the
> recommended server-side testing approach. The legacy `/karibu-test` skill is still installed for existing projects
> but is **no longer recommended** for new code — use `/browserless-test`.

---

### Step 9 — Write UI / end-to-end tests

```
/playwright-test UC-001    # Vaadin/jOOQ
/compose-test UC-001       # Compose/Ktor/Exposed
```

For Vaadin/jOOQ, Claude generates browser-based end-to-end tests against the running application (default:
`http://localhost:8080`) using the Drama Finder library for type-safe, accessibility-first element wrappers. Tests are
written black-box — they do not look at the implementation — and never use raw Playwright locators or `Thread.sleep()`.

For Compose/Ktor/Exposed, Claude generates UI-side tests in the lightest useful layer: Ktor `MockEngine` API-client
tests, coroutine ViewModel tests, and Compose Multiplatform semantics tests when screen-test dependencies exist.

---

## Skills Reference

### `/requirements` — Requirements Catalog

**Purpose:** Turns a `docs/vision.md` document into a structured `docs/requirements.html` catalog with functional
requirements, non-functional requirements, and constraints.

**Usage:**

```
/requirements
```

**What it does:**

1. Reads `docs/vision.md` to understand product mission, users, and goals
2. Extracts functional requirements as user stories (`As a [role], I want [goal] so that [benefit]`) with stable IDs (
   FR-001, FR-002, …), priority, and status
3. Extracts non-functional requirements as measurable quality attributes with category (Performance, Security,
   Availability, …), priority, and status
4. Extracts constraints (technical, regulatory, business) with stable IDs (CON-001, CON-002, …)
5. Writes all three as separate tables in `docs/requirements.html` — never mixing requirement types in one table

**Input:** `docs/vision.md`
**Output:** `docs/requirements.html`
**Plugin:** `aiup-core`

---

### `/entity-model` — Entity Model

**Purpose:** Designs the domain entity model with a Mermaid ER diagram and per-entity attribute tables.

**Usage:**

```
/entity-model
```

**What it does:**

1. Reads `docs/requirements.html` to identify the domain entities implied by the user stories
2. Draws a Mermaid `erDiagram` showing entities and their relationships (cardinality, role names) — without listing
   attributes inside the diagram
3. Produces one attribute table per entity with columns for attribute name, description, data type, length/precision,
   and validation rules (Primary Key, Sequence, NOT NULL, UNIQUE, foreign keys, check constraints)
4. Writes the result to `docs/entity_model.md`

**Input:** `docs/requirements.html`
**Output:** `docs/entity_model.md`
**Plugin:** `aiup-core`

---

### `/use-case-diagram` — Use Case Diagram

**Purpose:** Generates a Mermaid use case diagram showing actors, use cases, and their relationships derived from the
requirements catalog.

**Usage:**

```
/use-case-diagram
```

**What it does:**

1. Reads `docs/requirements.html` to identify actors and the use cases they participate in
2. Assigns stable IDs (UC-001, UC-002, …), each tracing to at least one functional requirement
3. Writes a Mermaid Diagram in `docs/requirements.html` using `left to right direction` and a `rectangle "System Name"`
   boundary
4. Uses standard Mermaid syntax only — no implementation details in use case names

**Input:** `docs/requirements.html`
**Output:** `docs/requirements.html`
**Plugin:** `aiup-core`

---

### `/use-case-spec` — Use Case Specification

**Purpose:** Writes detailed specifications for one or more use cases, each as a separate document under
`docs/use_cases/`.

**Usage:**

```
/use-case-spec UC-001
/use-case-spec UC-001 UC-002 UC-003     # multiple use cases at once
```

**What it does:**

1. Reads `docs/requirements.html` to scope the use case
2. Writes one document per use case under `docs/use_cases/` using a fixed template covering: Overview (ID, name, primary
   actor, goal, status), Preconditions, Main Success Scenario (numbered steps), Alternative Flows (for error
   conditions), Postconditions, and Business Rules
3. Keeps flow steps free of implementation details
4. Refuses to bundle multiple use cases into a single document

**Input:** Use case ID(s) as argument
**Output:** `docs/use_cases/UC-XXX-*.md` (one file per use case)
**Plugin:** `aiup-core`

---

### `/reverse-engineer` — Reverse Engineer Existing Project

**Purpose:** Recovers AIUP artifacts (use case diagram, per-use-case specifications, entity model) from an existing
codebase so legacy projects can join the AIUP workflow without rewriting documentation by hand.

**Usage:**

```
/reverse-engineer
```

**What it does:**

1. Detects the stack and locates entry points (controllers, routes, view classes), the data layer (ORM models or
   schema migrations), and authentication/authorization configuration
2. Identifies actors from role/authority definitions, authentication boundaries, and external system integrations
3. Groups entry points by user goal — not one use case per HTTP endpoint — and assigns stable IDs (`UC-001`, `UC-002`, …)
4. Writes a Mermaid use case diagram, one specification document per use case, and an entity model with a Mermaid ER
   diagram, all in the exact formats produced by `/use-case-diagram`, `/use-case-spec`, and `/entity-model`
5. Cross-validates that the three documents agree (every actor has a spec, every UC ID has a file, every entity
   referenced in a spec exists in the model)
6. Reports gaps honestly — endpoints it couldn't classify, use cases where the success scenario was hard to recover

**Input:** Existing source tree
**Output:** `docs/requirements.html`, `docs/use_cases/UC-XXX-*.md`, `docs/entity_model.md`
**Plugin:** `aiup-core`

---

### `/flyway-migration` — Flyway Database Migrations

**Purpose:** Generates versioned Flyway migration scripts (`V*.sql`) that create the schema described in
`docs/entity_model.md`.

**Usage:**

```
/flyway-migration
```

**What it does:**

1. Reads `docs/entity_model.md` and translates each entity into a `CREATE TABLE` statement
2. Creates a `CREATE SEQUENCE` for every primary key (no auto-increment)
3. Adds NOT NULL, UNIQUE, CHECK, and foreign key constraints from the entity model's validation rules
4. Names files using the Flyway convention `V001__create_<table>_table.sql`, `V002__…`
5. Writes scripts to `src/main/resources/db/migration`
6. Will not drop existing tables without explicit confirmation

**Input:** `docs/entity_model.md`
**Output:** `src/main/resources/db/migration/V*.sql`
**Plugin:** `aiup-vaadin-jooq`

---

### `/flyway-migration` — Flyway Migrations for Compose/Ktor/Exposed

**Purpose:** Generates PostgreSQL Flyway migrations compatible with Exposed table definitions.

**Usage:**

```
/flyway-migration
```

**What it does:**

1. Reads `docs/entity_model.md` and existing migrations to preserve naming and migration style
2. Creates tables with `BIGSERIAL` primary keys, explicit constraints, indexes, and `TIMESTAMPTZ` audit columns
3. Adds `updated_at` trigger functions when the project uses automatic update timestamps
4. Keeps SQL compatible with Exposed `Table("...")`, `long("id").autoIncrement()`, and explicit mapper code
5. Writes migrations under the discovered Flyway migration directory, typically `src/main/resources/db/migration`

**Input:** `docs/entity_model.md`
**Output:** `V*.sql` Flyway migrations
**Plugin:** `aiup-compose-ktor-exposed`

---

### `/implement` — Use Case Implementation

**Purpose:** Implements a use case end-to-end using Vaadin for the UI layer and jOOQ for the data access layer.

**Usage:**

```
/implement UC-001
```

**What it does:**

1. Reads the use case specification from `docs/use_cases/` and the entity model from `docs/entity_model.md`
2. Reads existing code first to match conventions before creating new files
3. Implements the data access layer using jOOQ — verifies it compiles before continuing
4. Implements the Vaadin view, wires it to the data access layer, and verifies the full implementation compiles
5. Consults the Vaadin, jOOQ, and JavaDocs MCP servers for current API documentation
6. Does **not** create test classes — use `/browserless-test` and `/playwright-test` for that

**Input:** Use case ID as argument
**Output:** Vaadin view + jOOQ data access classes
**Plugin:** `aiup-vaadin-jooq`

---

### `/implement` — Backend Implementation for Compose/Ktor/Exposed

**Purpose:** Implements backend use cases using shared DTOs, Ktor routes, Exposed persistence, and Koin DI.

**Usage:**

```
/implement UC-001
```

**What it does:**

1. Reads the use case spec, entity model, migrations, and existing code conventions
2. Creates or updates shared `@Serializable` DTOs in the KMP shared module
3. Adds domain models, repository ports, Exposed tables/repositories, application services, and route functions in a
   vertical-slice module layout
4. Wires dependencies through the existing Koin module and route entry points
5. Runs focused diagnostics/build checks and leaves tests to `/ktor-test` and `/compose-test`

**Input:** Use case ID as argument
**Output:** Ktor backend + shared DTO implementation
**Plugin:** `aiup-compose-ktor-exposed`

---

### `/implement-ui` — Compose Multiplatform UI Implementation

**Purpose:** Implements Compose Multiplatform UI for a specified use case.

**Usage:**

```
/implement-ui UC-001
```

**What it does:**

1. Reads the use case spec and shared DTO/API contract
2. Updates the Ktor API client with typed suspend functions and JSON serialization
3. Adds plain ViewModel classes with Compose state and constructor-injected dependencies
4. Creates small Material 3 composables using accessible text/content descriptions
5. Wires screens into the existing app/navigation structure without introducing global service locators

**Input:** Use case ID as argument
**Output:** Compose UI screen, ViewModel, and API client code
**Plugin:** `aiup-compose-ktor-exposed`

---

### `/browserless-test` — Vaadin Browserless Server-Side Tests *(recommended)*

**Purpose:** Creates server-side unit tests for Vaadin views using the official **Vaadin Browserless** framework
(`com.vaadin:browserless-test-junit6`) — no browser, no WebDriver, no servlet container. Browserless Testing is free
and open source under Apache 2.0 since Vaadin 25.1.

**Usage:**

```
/browserless-test UC-001
```

**What it does:**

1. Reads the use case spec to derive the test scenarios
2. Generates a JUnit 5 test class extending `SpringBrowserlessTest` (annotated `@SpringBootTest`)
3. Uses the `$()` / `$view()` component query API for lookups and the `test()` wrapper for interactions
4. Seeds test data via Flyway migrations under `src/test/resources/db/migration` — never via Mockito, services, or
   `DSLContext`
5. Cleans up only test-created data in `@AfterEach` (does not wipe the schema)
6. Preserves transaction boundaries — tests are not annotated `@Transactional`
7. Reads component state through the component's Java API; reserves `test(...)` for actions

**Input:** Use case ID as argument
**Output:** Browserless test class under `src/test/java`
**Plugin:** `aiup-vaadin-jooq`

---

### `/karibu-test` — Karibu Server-Side Tests *(legacy — no longer recommended)*

> **Use `/browserless-test` instead for new projects.** Since Vaadin 25.1 the official Browserless Testing framework
> is free and open source under Apache 2.0, making the community Karibu Testing library redundant. This skill is
> retained for existing codebases that already use Karibu.

**Purpose:** Creates Karibu unit tests for Vaadin views — server-side tests that exercise the full Vaadin component
tree without launching a browser.

**Usage:**

```
/karibu-test UC-001
```

**What it does:**

1. Reads the use case spec to derive the test scenarios
2. Generates a JUnit 5 test class using Karibu helpers (`LocatorJ`, `GridKt`, `NotificationsKt`, `ConfirmDialogKt`)
3. Seeds test data via Flyway migrations under `src/test/resources/db/migration` — never via Mockito, services, or
   `DSLContext`
4. Cleans up only test-created data in `@AfterEach` (does not wipe the schema)
5. Preserves transaction boundaries — tests are not annotated `@Transactional`
6. Uses the KaribuTesting MCP server for documentation and code generation

**Input:** Use case ID as argument
**Output:** Karibu test class under `src/test/java`
**Plugin:** `aiup-vaadin-jooq`

---

### `/ktor-test` — Ktor Backend Tests

**Purpose:** Creates backend tests for Compose/Ktor/Exposed services.

**Usage:**

```
/ktor-test UC-001
```

**What it does:**

1. Reads the use case spec and backend implementation
2. Creates Ktor `testApplication` route tests with fake repository/service ports
3. Covers success, validation, not-found, auth, and key alternative flows
4. Adds Ktor `MockEngine` tests for outbound clients when needed
5. Adds Testcontainers + Flyway repository integration tests when persistence behavior needs real PostgreSQL

**Input:** Use case ID as argument
**Output:** Kotlin tests under the backend module's test source sets
**Plugin:** `aiup-compose-ktor-exposed`

---

### `/compose-test` — Compose UI Tests

**Purpose:** Creates UI-side tests for Compose/Ktor client code.

**Usage:**

```
/compose-test UC-001
```

**What it does:**

1. Reads the use case spec and UI implementation
2. Creates Ktor `MockEngine` API-client tests in `commonTest`
3. Creates coroutine ViewModel tests with fakes and `runTest`
4. Creates Compose Multiplatform semantics tests with `runComposeUiTest` when dependencies exist
5. Avoids real network calls and Android-only test APIs in multiplatform modules

**Input:** Use case ID as argument
**Output:** Kotlin tests under the UI module's `commonTest` or matching source set
**Plugin:** `aiup-compose-ktor-exposed`

---

### `/playwright-test` — Playwright Integration Tests

**Purpose:** Creates browser-based end-to-end tests for Vaadin views using Playwright with the Drama Finder library for
type-safe, accessibility-first element wrappers.

**Usage:**

```
/playwright-test UC-001
```

**What it does:**

1. Reads the use case spec to derive the test scenarios
2. Generates an integration test extending `AbstractBasePlaywrightIT` (handles browser lifecycle, page creation, and
   Vaadin synchronization automatically)
3. Writes black-box tests against the running application (default: `http://localhost:8080`) — does not consult the
   implementation
4. Uses Drama Finder element wrappers exclusively — never raw Playwright locators, XPath, `Thread.sleep()`, or
   `page.waitForTimeout()`
5. Reuses existing test data from Flyway migrations; cleans up only test-created data in `@AfterEach`
6. Looks up Drama Finder method signatures via the JavaDocs MCP server rather than guessing

**Input:** Use case ID as argument
**Output:** Playwright integration test under `src/test/java` (named `*IT.java`)
**Plugin:** `aiup-vaadin-jooq`

---

## Project Structure

After running the full workflow for a project, your tree will look like this:

```
your-project/
├── docs/
│   ├── vision.md                         ← you maintain this
│   ├── requirements.html                   ← produced by /requirements and /use-case-diagram
│   ├── entity_model.md                   ← produced by /entity-model
│   └── use_cases/                        ← produced by /use-case-spec
│       ├── UC-001-create-reservation.md
│       ├── UC-002-cancel-reservation.md
│       └── ...
├── src/
│   ├── main/
│   │   ├── java/                         ← produced by /implement
│   │   └── resources/
│   │       └── db/migration/             ← produced by /flyway-migration
│   │           ├── V001__create_room_type_table.sql
│   │           └── ...
│   └── test/
│       ├── java/                         ← produced by /browserless-test, /playwright-test
│       └── resources/
│           └── db/migration/             ← test data seeds
└── CLAUDE.md
```

---

## Recommended CLAUDE.md

Create a `CLAUDE.md` at your project root. Claude loads this automatically at the start of every session:

```markdown
# Project Context

This project follows the AI Unified Process. Read `docs/vision.md`, `docs/requirements.html`,
and `docs/entity_model.md` for product context before making decisions.

## AIUP Workflow

1. `/requirements`        → derives `docs/requirements.html` from `docs/vision.md`
2. `/entity-model`        → derives `docs/entity_model.md` from requirements
3. `/use-case-diagram`    → produces Mermaid diagram inside `docs/requirements.html`
4. `/use-case-spec UC-XX` → produces `docs/use_cases/UC-XX-*.md`
5. `/flyway-migration`    → produces `src/main/resources/db/migration/V*.sql`
6. `/implement UC-XX`     → implements the use case backend (Vaadin/jOOQ or Ktor/Exposed)
7. `/implement-ui UC-XX`  → implements Compose UI when using Compose/Ktor/Exposed
8. `/browserless-test UC-XX` or `/ktor-test UC-XX` → server-side tests
9. `/playwright-test UC-XX` or `/compose-test UC-XX` → UI/end-to-end tests

Never skip the spec for a use case before implementing it.
Always read the entity model before writing data access code.
```

---

## Recommended `docs/vision.md` Structure

The `/requirements` skill relies heavily on this file. Include at minimum:

```markdown
# Vision: <Product Name>

## Mission

<One paragraph on what this product does and the problem it solves.>

## Target Users

- <Primary user role and what they need from the system>
- <Secondary user roles>

## Goals

- <Measurable business or product goals>

## Scope

- In scope: <high-level capabilities>
- Out of scope: <explicit non-goals>

## Constraints

- <Regulatory, technical, organizational constraints>
```

---

## Tips

**Maintain traceability.** Every entity should map to at least one functional requirement; every use case should trace
to one or more FRs; every test should reference a use case ID. The skills produce stable IDs (FR-001, UC-001, …) — keep
them.

**Edit between steps.** The intermediate documents (`requirements.html`, `entity_model.md`) are designed
to be reviewed and corrected by hand. Do not skip the review.

**Re-run upstream skills when requirements change.** If a new functional requirement appears, re-run `/entity-model` and
`/use-case-diagram` so the downstream artifacts stay consistent. Re-running is cheap; fixing inconsistencies later is
not.

**Keep `aiup-core` even on non-Vaadin stacks.** The methodology skills are stack-agnostic — only the construction-phase
skills are tied to a stack plugin such as Vaadin/jOOQ or Compose/Ktor/Exposed. You can pair `aiup-core` with any implementation toolchain.

**Commit `docs/` to version control.** The vision, requirements, entity model, and use case specs are your project's
institutional memory — they explain *why* the code is the way it is, which is invaluable for onboarding and debugging
months later.

---

## Learn More

Visit [unifiedprocess.ai](https://unifiedprocess.ai) for the full methodology.

## Key Concepts

### Marketplace

A **marketplace** is a curated repository that hosts and distributes multiple Claude Code plugins. It acts as a central
hub where plugins can be discovered, installed, and managed. When you add a marketplace to Claude Code, you gain access
to all the plugins it contains.

### Plugin

A **plugin** is a self-contained extension that adds new capabilities to Claude Code. Each plugin can include skills,
agents, hooks, and MCP servers. Plugins are technology-specific and encapsulate everything needed to work with a
particular tech stack or methodology.

### Skill

A **skill** is a specialized behavior defined in a `SKILL.md` file. Skills can be invoked explicitly as slash commands (
e.g., `/requirements`) or triggered automatically by Claude when it recognizes a matching task. Skills are namespaced by
their plugin (e.g., `aiup-core:requirements`).

### MCP Server

An **MCP (Model Context Protocol) server** is an external service that provides Claude with access to specialized tools
and documentation. The plugins in this marketplace ship with the following servers:

| Server            | Plugin                      | Description                                          |
|-------------------|-----------------------------|------------------------------------------------------|
| **context7**      | `aiup-core`                 | General library documentation lookup                 |
| **Vaadin**        | `aiup-vaadin-jooq`          | Vaadin component and framework documentation         |
| **KaribuTesting** | `aiup-vaadin-jooq`          | Karibu testing framework documentation               |
| **jOOQ**          | `aiup-vaadin-jooq`          | jOOQ DSL and code generation reference               |
| **JavaDocs**      | `aiup-vaadin-jooq`          | Java API documentation lookup                        |
| **Playwright**    | `aiup-vaadin-jooq`          | Browser automation for integration tests             |

