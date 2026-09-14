# How to Use AIUP

AI Unified Process Marketplace — Core + Compose/Ktor/Exposed

A monorepo-first walkthrough of the **AI Unified Process**: scope one service, turn its written vision into traceable requirements, then implement and test it with the **Compose / Ktor / Exposed** stack. The same skills also work in a standalone repository.

`Inception` · `Elaboration` · `Construction` · `Transition`

## What is the AI Unified Process?

AIUP is a disciplined, AI-assisted development methodology. Every project starts with a written vision, proceeds through requirements, an entity model, and use case specifications, and *only then* enters implementation. **Nothing gets built without a use case. Nothing reaches production without tests traceable to requirements.**

This prevents the common failure mode of AI-assisted development: jumping straight to code from a vague prompt, producing something that half-works and can't be maintained. The phases mirror the Rational Unified Process — Inception, Elaboration, Construction, Transition — adapted for AI-driven workflows.

> **The contract between steps is files.** Each skill picks up where the previous one left off using artefacts on disk. In a monorepo these live under `<service>/docs/`; in a standalone repository they live under `docs/`. You can inspect or hand-edit any artefact before continuing.

## Prerequisites & Agent Setup

Use a coding agent that can read and edit project files and run the required verification tools. For a new service, prepare `<service>/docs/vision.md` describing its vision, target users, and goals; use `docs/vision.md` in a standalone project. For existing code, start with `aiup-reverse-engineer` and review its recovered baseline.

The core workflow is independent of the agent and implementation stack. The construction examples in this guide use a Kotlin Multiplatform Gradle project with Compose Multiplatform, Ktor, Exposed, Flyway, and PostgreSQL. For another stack, use the same reviewed specifications with that project's implementation and testing workflow.

Choose the setup instructions for your agent:

- [Claude Code plugin installation](installation/claude-code.md)
- [Other agents and manual skill setup](installation/other-agents.md)

### How to request a step

Names such as `aiup-requirements` and `aiup-implement` below identify skills, not universal commands. The `aiup-` prefix distinguishes these skills from similarly named skills in other marketplaces. Use your agent's skill discovery or ask it to follow the relevant `SKILL.md` explicitly. Preserve the whole skill directory, including its bundled references and scripts, and resolve those paths relative to the skill directory.

When upgrading an existing installation, update the skills, replace old AIUP symlinks, and change saved prompts and project instructions to the prefixed names. Generic aliases are not retained; generated artefact filenames stay unchanged.

For example, with this marketplace checked out beside your project:

```text
Follow ../ai-unified-marketplace/aiup-core/skills/aiup-requirements/SKILL.md
for payments-service. Read payments-service/docs/vision.md, write
payments-service/docs/requirements.md, and run the skill's quality checks.
```

For implementation, name the use case and the stack skill:

```text
Use the aiup-implement skill from aiup-compose-ktor-exposed for UC-001 in
payments-service. Read the current specification and bundled references,
reconcile existing code, and run the affected verification tasks.
```

A step is complete when its expected files exist and its checks pass. If the agent cannot access a reference or run a required tool, it should report the missing capability and leave verification outstanding. Different agents can perform successive steps using the same reviewed artefacts.

## Start Here: One Service in a Monorepo

Name the service or stack in every request. Explicit scope wins over the current directory and keeps the complete requirements-to-tests chain under that service. You can instead `cd` into the service and omit `in payments-service`, but root-scoped prompts are clearer and easier to repeat.

**How scope is detected**
The core skills recognise `mise.toml` with `monorepo_root` or namespaced tasks, and repositories with multiple sibling `settings.gradle.kts` builds. Existing service-local `docs/` directories also guide `aiup-reference`.

**How modules are discovered**
Stack skills locate the owning build first, then read that build's `settings.gradle.kts`. They do not assume names such as `server`, `shared`, or `ui`.

**How commands are run**
From the root, verification uses `mise run //<stack>:<task>`. Inside the stack it uses `mise run <task>`; the owning Gradle wrapper is the fallback.

### Recommended service workflow

Run these logical steps in order, reviewing their outputs before continuing. Entries are skill names plus scope, not shell commands. Repeat the use-case steps for each selected UC ID; include UI steps only when the use case needs UI.

```text
aiup-requirements in payments-service
aiup-entity-model in payments-service
aiup-use-case-diagram in payments-service
aiup-use-case-spec UC-001 in payments-service
aiup-architecture in payments-service
aiup-flyway-migration in payments-service
aiup-implement UC-001 in payments-service
aiup-implement-ui UC-001 in payments-service
aiup-ktor-test UC-001 in payments-service
aiup-compose-test UC-001 in payments-service
aiup-implementation-status UC-001 in payments-service
aiup-reference in payments-service
```

> **Keep scopes independent.** Use root `docs/` only for cross-cutting or portfolio documentation. Each deployable service should own its vision, requirements, entity model, architecture, use cases, and implementation-status pages.

```text
monorepo-root/
├── docs/                          ← optional cross-cutting documentation
├── mise.toml                      ← monorepo and namespaced task definitions
├── payments-service/
│   ├── docs/
│   │   ├── vision.md
│   │   ├── requirements.md
│   │   ├── entity_model.md
│   │   ├── architecture.md
│   │   ├── REFERENCE.md
│   │   └── use_cases/
│   │       ├── UC-001-charge-card.md
│   │       └── UC-001-implementation-status.md
│   └── settings.gradle.kts
└── billing-service/
    ├── docs/…
    └── settings.gradle.kts
```

## Prepare Prompts for Fresh Sessions

After reviewing the use-case specifications, ask:

```text
Use aiup-implementation-prompts for the Card Payments feature
(UC-001 and UC-002) in payments-service.
```

The skill writes `payments-service/docs/card-payments-implementation-prompts.md` with a shared
session header, an ordered session index, and individual prompts for the required
migration, backend, UI, testing, and status work. It records missing prerequisites
and generates prompts without executing them. For a named feature without IDs, it selects the relevant specifications and asks
if scope is unclear. Omit both feature and IDs to cover all existing specifications
in the selected service, or request backend-only or UI-only scope.

For one UC, the default filename is based on its specification, for example
`UC-001-charge-card-implementation-prompts.md`. Several UCs without a feature name
use their sorted IDs, such as `UC-001-UC-003-implementation-prompts.md`. An explicit
output path or filename takes precedence.

Start a fresh chat for each numbered session. Paste the shared header followed by
that session's prompt, using the same working tree with the previous sessions'
reviewed changes. Complete prerequisites before dependent sessions. The document
contains the file context needed to work without previous chat history.

## The Workflow at a Glance

The forward path, left to right. Each arrow consumes artefacts from the same explicitly scoped service.

```text
Inception        Elaboration                          Construction
─────────────    ──────────────────────────────────   ──────────────────────────────
aiup-requirements →  aiup-entity-model → aiup-use-case-diagram  →  aiup-use-case-spec → aiup-architecture
                                                     ↘  aiup-reference
                                                     ↘  aiup-flyway-migration
                                                     ↘  aiup-implement
                                                     ↘  aiup-implement-ui
                                                     ↘  aiup-ktor-test
                                                     ↘  aiup-compose-test
                                                     ↘  aiup-implementation-status
```

> **Inheriting a legacy codebase?** Start with `aiup-reverse-engineer` — it walks the existing code, configuration, and schema and produces the same use case diagram, use case specs, and entity model the forward workflow would have, giving you a documented baseline.

## Part 1 — Core Methodology (`aiup-core`)

These steps share the same artefact contract regardless of your agent or implementation toolchain. The [core manifest](../aiup-core/.claude-plugin/plugin.json) records the current plugin version. Paths below are relative to the scoped service unless stated otherwise.

1. **`aiup-requirements` — Requirements catalogue**
   Reads `docs/vision.md` and writes `docs/requirements.md` as three separate tables: functional requirements (user stories, `FR-001`…), non-functional requirements (measurable quality attributes, `NFR-001`…), and constraints (`CON-001`…). It also creates the fenced Mermaid block under `## Use Case Diagram` later updated by `aiup-use-case-diagram`. Review before continuing.
   `monorepo` — `aiup-requirements payments-service` → writes `payments-service/docs/requirements.md`

2. **`aiup-entity-model` — Entity model**
   Reads the requirements and writes `docs/entity_model.md` with a Mermaid ER diagram plus one attribute table per entity (data type, length/precision, validation rules). Approve before moving on.
   `monorepo` — `aiup-entity-model in payments-service` → writes `payments-service/docs/entity_model.md`

3. **`aiup-use-case-diagram` — Use case diagram**
   Identifies actors and use cases, then updates the single Mermaid block under `## Use Case Diagram` in `requirements.md`. Each use case gets a stable ID (`UC-001`…) tracing back to one or more functional requirements.
   `monorepo` — `aiup-use-case-diagram in payments-service` → updates the diagram in `payments-service/docs/requirements.md`

4. **`aiup-use-case-spec UC-001` — Use case specification**
   Writes one detailed document per use case under `docs/use_cases/`: actors, stakeholders, trigger, preconditions, main success scenario (numbered steps), alternative flows, postconditions, and business rules. Pass several IDs at once: `aiup-use-case-spec UC-001 UC-002 UC-003`. One use case per file — never bundled.
   `monorepo` — `aiup-use-case-spec UC-001 in payments-service` → writes `payments-service/docs/use_cases/UC-001-*.md`

5. **`aiup-architecture` — Architecture documentation**
   Creates or updates `docs/architecture.md` covering context, structure, layering, data flow, ADRs, tech stack, scaling, failure modes, security, observability, deployment, and cross-cutting concerns — with embedded Mermaid diagrams where they clarify structure.
   `monorepo` — `aiup-architecture payments-service` → writes `payments-service/docs/architecture.md`

6. **`aiup-reference` — Project reference**
   Creates or updates `docs/REFERENCE.md`: repository layout, authoritative docs, commands, module boundaries, domain vocabulary, integrations, testing strategy, and operational notes. Links to canonical docs rather than duplicating them, and marks gaps as *Not documented yet*.
   `monorepo` — `aiup-reference payments-service` → writes `payments-service/docs/REFERENCE.md`

7. **`aiup-reverse-engineer` — Existing-code baseline**
   An alternative starting point for an inherited service. It reads source, build configuration, APIs, tests, and schema evidence, then creates the canonical `requirements.md`, one `UC-*.md` specification per use case, and `entity_model.md`. Review the recovered business intent before continuing with implementation.
   `monorepo` — `aiup-reverse-engineer payments-service` → writes the baseline under `payments-service/docs/`

## Part 2 — Implementation: Compose / Ktor / Exposed (`aiup-compose-ktor-exposed`)

The Construction phase for a Kotlin Multiplatform project. These skills are provided by `aiup-compose-ktor-exposed`; its [manifest](../aiup-compose-ktor-exposed/.claude-plugin/plugin.json) records the current version. They discover the scoped stack and its modules before changing code; run implementation and test skills per use case ID.

1. **`aiup-flyway-migration` — PostgreSQL migrations**
   Reads `docs/entity_model.md` and existing migrations and writes versioned Flyway scripts (`V001__create_*.sql`…) under the discovered server module's migration directory. Existing migration conventions win; `BIGSERIAL`, `TIMESTAMPTZ`, constraints, indexes, and `updated_at` triggers are reference defaults only when they match the project. Mappings remain compatible with Exposed.
   `monorepo` — `aiup-flyway-migration in payments-service` → writes `payments-service/<server-module>/src/main/resources/db/migration/V*.sql`

2. **`aiup-implement UC-001` — Backend implementation**
   Reads the use case spec, entity model, migrations, and existing conventions, then implements a vertical slice: shared `@Serializable` DTOs in the KMP module, domain models, repository ports, Exposed tables/repositories, application services, Ktor route functions, and feature-owned Koin DI wiring. Updates existing code in place. Runs the project's compile/verification tasks, existing DI graph and architecture tests, and configured `apiCheck` when a shared public contract changes. Does **not** write tests.
   `monorepo` — `aiup-implement UC-001 in payments-service` → backend + shared DTOs land under `payments-service/` modules

3. **`aiup-implement-ui UC-001` — Compose UI**
   Implements Compose Multiplatform UI: typed suspend functions on the Ktor API client with JSON serialisation, feature transport ports, plain ViewModels depending on those ports, immutable UI state/action contracts, and small accessible Material 3 composables wired into the existing navigation — no global service locators. Dependencies are constructor-injected. It stops and reports missing prerequisites if the backend DTOs or routes do not exist, and runs affected architecture, coverage, and shared-contract gates when configured.
   `monorepo` — `aiup-implement-ui UC-001 in payments-service` → Compose screen, ViewModel & API client under the service's UI module

4. **`aiup-ktor-test UC-001` — Backend tests**
   Creates or reconciles Ktor `testApplication` route tests with fake repository/service ports covering success, validation, not-found, auth, and key alternative flows. Adds `MockEngine` tests for outbound clients, extends `ArchitectureTest.kt` for new modules, and adds Testcontainers + Flyway integration tests under `src/testContainerTest` when persistence behaviour needs a real PostgreSQL. Verifies the complete service DI graph and runs the complete affected test class or source-set task.
   `monorepo` — `aiup-ktor-test UC-001 in payments-service` → tests under the discovered server module's `src/test` and `src/testContainerTest`

5. **`aiup-compose-test UC-001` — UI tests**
   Creates or reconciles UI-side tests in the lightest useful layer: Ktor `MockEngine` API-client tests in `commonTest`, coroutine ViewModel tests with fakes and `runTest`, OIDC/PKCE or `expect`/`actual` platform tests in `jvmTest`/`wasmJsTest`, and Compose semantics tests with `runComposeUiTest` when dependencies exist. Uses no real network calls. Runs the complete affected test class or source-set task and configured architecture and coverage checks.
   `monorepo` — `aiup-compose-test UC-001 in payments-service` → tests under the discovered UI module's `commonTest` or platform source set

6. **`aiup-implementation-status UC-001` — Status & traceability**
   Reads the entity model, use case specs, source, tests, and migrations and maintains an entity implementation-status matrix (Entity / DB Table / Domain Model / Repository / Service / Migrations). Writes per-use-case Markdown status pages under `docs/use_cases/`, cross-referencing migration version ranges and code/test evidence. Records DI graph, architecture, shared API, and coverage-gate evidence where available. Reports missing or partial implementation honestly; a status page does not replace running tests. Request UI coverage explicitly when needed.
   `monorepo` — `aiup-implementation-status UC-001 in payments-service` → writes `payments-service/docs/use_cases/UC-001-implementation-status.md`

## Skills Reference

| Skill                                                                                                   | Phase        | Input                      | Output                              | Plugin               |
|---------------------------------------------------------------------------------------------------------|--------------|----------------------------|-------------------------------------|----------------------|
| [`aiup-requirements`](../aiup-core/skills/aiup-requirements/SKILL.md)                                   | Inception    | `<service>/docs/vision.md` | `requirements.md`                   | core                 |
| [`aiup-entity-model`](../aiup-core/skills/aiup-entity-model/SKILL.md)                                   | Elaboration  | `requirements.md`          | `entity_model.md`                   | core                 |
| [`aiup-use-case-diagram`](../aiup-core/skills/aiup-use-case-diagram/SKILL.md)                           | Elaboration  | `requirements.md`          | same file's Mermaid slot            | core                 |
| [`aiup-use-case-spec`](../aiup-core/skills/aiup-use-case-spec/SKILL.md)                                 | Construction | UC ID(s) + requirements    | `docs/use_cases/UC-*.md`            | core                 |
| [`aiup-architecture`](../aiup-core/skills/aiup-architecture/SKILL.md)                                   | Construction | service docs + source      | `architecture.md`                   | core                 |
| [`aiup-reference`](../aiup-core/skills/aiup-reference/SKILL.md)                                         | Any          | service docs + source      | `REFERENCE.md`                      | core                 |
| [`aiup-reverse-engineer`](../aiup-core/skills/aiup-reverse-engineer/SKILL.md)                           | Any          | existing service source    | requirements + specs + model        | core                 |
| [`aiup-flyway-migration`](../aiup-compose-ktor-exposed/skills/aiup-flyway-migration/SKILL.md)           | Construction | model + use cases          | server module `V*.sql`              | compose-ktor-exposed |
| [`aiup-implement`](../aiup-compose-ktor-exposed/skills/aiup-implement/SKILL.md)                         | Construction | UC ID                      | Ktor backend + shared DTOs          | compose-ktor-exposed |
| [`aiup-implement-ui`](../aiup-compose-ktor-exposed/skills/aiup-implement-ui/SKILL.md)                   | Construction | UC ID                      | Compose screen + ViewModel + client | compose-ktor-exposed |
| [`aiup-ktor-test`](../aiup-compose-ktor-exposed/skills/aiup-ktor-test/SKILL.md)                         | Construction | UC ID + backend            | route / unit / integration tests    | compose-ktor-exposed |
| [`aiup-compose-test`](../aiup-compose-ktor-exposed/skills/aiup-compose-test/SKILL.md)                   | Construction | UC ID + UI                 | client / ViewModel / UI tests       | compose-ktor-exposed |
| [`aiup-implementation-status`](../aiup-compose-ktor-exposed/skills/aiup-implementation-status/SKILL.md) | Construction | UC ID(s)                   | status matrix + Markdown pages      | compose-ktor-exposed |
| [`aiup-implementation-prompts`](../aiup-compose-ktor-exposed/skills/aiup-implementation-prompts/SKILL.md) | Construction planning | scoped specs + project structure | UC- or feature-named `docs/*-implementation-prompts.md` | compose-ktor-exposed |

## Verify Before Continuing

| Step                          | Completion check                                                                                                                        |
|-------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| Requirements                  | FRs use role/goal/benefit stories; NFRs are measurable; IDs are unique; statuses are filled.                                            |
| Entity model and diagram      | Entity attribute tables are complete; exactly one Mermaid use-case block exists in requirements; UC IDs trace to FRs.                   |
| Use-case specifications       | One specification per requested UC, named from the diagram; run the bundled use-case validator and fix reported errors.                 |
| Architecture and reference    | Claims match repository evidence; relative links resolve; unknowns are explicit.                                                        |
| Migrations and implementation | Changes match the reviewed specification and existing conventions; affected build, migration, and configured compatibility checks pass. |
| Tests                         | Success and relevant alternative/error flows are covered; complete affected test tasks and configured gates pass.                       |
| Implementation status         | Links point to actual code, migrations, and tests; missing coverage remains visible.                                                    |

## What the Scoped Service Looks Like

After the full workflow, one service has a predictable documentation and implementation tree:

```text
payments-service/
├── docs/
│   ├── vision.md                 ← you maintain this
│   ├── requirements.md           ← aiup-requirements + aiup-use-case-diagram
│   ├── entity_model.md           ← aiup-entity-model, updated by aiup-implementation-status
│   ├── architecture.md           ← aiup-architecture
│   ├── REFERENCE.md              ← aiup-reference
│   └── use_cases/                ← aiup-use-case-spec + aiup-implementation-status
│       ├── UC-001-charge-card.md
│       └── UC-001-implementation-status.md
├── <server-module>/src/
│   ├── main/
│   │   ├── kotlin/               ← aiup-implement
│   │   └── resources/db/migration/  ← aiup-flyway-migration  (V001__*.sql …)
│   └── test/                     ← aiup-ktor-test
├── <shared-module>/src/commonMain/  ← shared DTOs from aiup-implement
├── <ui-module>/src/commonMain/      ← aiup-implement-ui
└── settings.gradle.kts
```

## Tips for Success

**Maintain traceability**
Every entity maps to a functional requirement; every use case traces to one or more FRs; every test references a UC ID. Keep the stable IDs the skills produce.

**Edit between steps**
The intermediate documents are designed to be reviewed and corrected by hand. Don't skip the review.

**Reconcile changes through the whole chain**
When intent changes, update the vision or requirements, then reconcile the affected entity model and use-case diagram. Update the affected use-case specifications before migrations, backend/UI implementation, and tests; finally refresh implementation status and any changed architecture/reference facts. Preserve surviving FR, CON, UC, and use-case-scoped BR identifiers. Supply a specification diff when available. Implementation and test skills update existing files in place, including removing behaviour dropped from the specification, while preserving unrelated working code.

**Commit `docs/`**
The vision, requirements, entity model, and specs are your project's institutional memory — they explain *why* the code is the way it is.

---

AI Unified Process Marketplace · Core + Compose/Ktor/Exposed · [unifiedprocess.ai](https://unifiedprocess.ai)
