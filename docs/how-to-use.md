# How to Use AIUP

AI Unified Process Marketplace — Core + Compose/Ktor/Exposed

A monorepo-first walkthrough of the **AI Unified Process**: scope one service, turn its written vision into traceable requirements, then implement and test it with the **Compose / Ktor / Exposed** stack. The same skills also work in a standalone repository.

`Inception` · `Elaboration` · `Construction` · `Transition`

## What is the AI Unified Process?

AIUP is a disciplined, AI-assisted development methodology. Every project starts with a written vision, proceeds through requirements, an entity model, and use case specifications, and *only then* enters implementation. **Nothing gets built without a use case. Nothing reaches production without tests traceable to requirements.**

This prevents the common failure mode of AI-assisted development: jumping straight to code from a vague prompt, producing something that half-works and can't be maintained. The phases mirror the Rational Unified Process — Inception, Elaboration, Construction, Transition — adapted for AI-driven workflows.

> **The contract between steps is files.** Each skill picks up where the previous one left off using artifacts on disk. In a monorepo these live under `<service>/docs/`; in a standalone repository they live under `docs/`. You can inspect or hand-edit any artifact before continuing.

## Prerequisites & Installation

**You need**
Claude Code running in your repository, and a `<service>/docs/vision.md` describing the service vision, target users, and goals. Use `docs/vision.md` only for a standalone project.

**For the Compose stack**
A Kotlin Multiplatform Gradle project with Compose Multiplatform, Ktor, Exposed, Flyway, and PostgreSQL conventions already present or planned.

Install the core methodology plus the Compose/Ktor/Exposed stack plugin from the `sanitas-llm-plugins` marketplace — the one that ships the Compose/Ktor/Exposed plugin:

```sh
claude plugin marketplace add "git@gitlab.sanet17.ch:base/sanitas-llm-plugins.git#master"
claude plugin install aiup-core@sanitas-llm-plugins
claude plugin install aiup-compose-ktor-exposed@sanitas-llm-plugins
```

Verify from the monorepo root with `/requirements in payments-service`. It should read `payments-service/docs/vision.md` and write the catalogue beside it.

## Start Here: One Service in a Monorepo

Name the service or stack in every request. Explicit scope wins over the current directory and keeps the complete requirements-to-tests chain under that service. You can instead `cd` into the service and omit `in payments-service`, but root-scoped prompts are clearer and easier to repeat.

**How scope is detected**
The core skills recognise `mise.toml` with `monorepo_root` or namespaced tasks, and repositories with multiple sibling `settings.gradle.kts` builds. Existing service-local `docs/` directories also guide `/reference`.

**How modules are discovered**
Stack skills locate the owning build first, then read that build's `settings.gradle.kts`. They do not assume names such as `server`, `shared`, or `ui`.

**How commands are run**
From the root, verification uses `mise run //<stack>:<task>`. Inside the stack it uses `mise run <task>`; the owning Gradle wrapper is the fallback.

### Recommended service workflow

```sh
/requirements in payments-service
/entity-model in payments-service
/use-case-diagram in payments-service
/use-case-spec UC-001 in payments-service
/architecture in payments-service
/flyway-migration in payments-service
/implement UC-001 in payments-service
/implement-ui UC-001 in payments-service
/ktor-test UC-001 in payments-service
/compose-test UC-001 in payments-service
/implementation-status UC-001 in payments-service
/reference in payments-service
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

## The Workflow at a Glance

The forward path, left to right. Each arrow consumes artifacts from the same explicitly scoped service.

```text
Inception        Elaboration                          Construction
─────────────    ──────────────────────────────────   ──────────────────────────────
/requirements →  /entity-model → /use-case-diagram  →  /use-case-spec → /architecture
                                                     ↘  /reference
                                                     ↘  /flyway-migration
                                                     ↘  /implement
                                                     ↘  /implement-ui
                                                     ↘  /ktor-test
                                                     ↘  /compose-test
                                                     ↘  /implementation-status
```

> **Inheriting a legacy codebase?** Start with `/reverse-engineer` — it walks the existing code, configuration, and schema and produces the same use case diagram, use case specs, and entity model the forward workflow would have, giving you a documented baseline.

## Part 1 — Core Methodology (`aiup-core` 103.5.0)

Stack-agnostic. These steps run identically regardless of your implementation toolchain. Paths below are relative to the scoped service unless stated otherwise.

1. **`/requirements` — Requirements catalog**
   Reads `docs/vision.md` and writes `docs/requirements.md` as three separate tables: functional requirements (user stories, `FR-001`…), non-functional requirements (measurable quality attributes, `NFR-001`…), and constraints (`C-001`…). It also creates the stable HTML slot later updated by `/use-case-diagram`. Review before continuing.
   `monorepo` — `/requirements payments-service` → writes `payments-service/docs/requirements.md`

2. **`/entity-model` — Entity model**
   Reads the requirements and writes `docs/entity_model.md` with a Mermaid ER diagram plus one attribute table per entity (data type, length/precision, validation rules). Approve before moving on.
   `monorepo` — `/entity-model in payments-service` → writes `payments-service/docs/entity_model.md`

3. **`/use-case-diagram` — Use case diagram**
   Identifies actors and use cases, then updates the single Mermaid block under `## Use Case Diagram` in `requirements.md`. Each use case gets a stable ID (`UC-001`…) tracing back to one or more functional requirements.
   `monorepo` — `/use-case-diagram in payments-service` → updates the diagram in `payments-service/docs/requirements.md`

4. **`/use-case-spec UC-001` — Use case specification**
   Writes one detailed document per use case under `docs/use_cases/`: actors, stakeholders, trigger, preconditions, main success scenario (numbered steps), alternative flows, postconditions, and business rules. Pass several IDs at once: `/use-case-spec UC-001 UC-002 UC-003`. One use case per file — never bundled.
   `monorepo` — `/use-case-spec UC-001 in payments-service` → writes `payments-service/docs/use_cases/UC-001-*.md`

5. **`/architecture` — Architecture documentation**
   Creates or updates `docs/architecture.md` covering context, structure, layering, data flow, ADRs, tech stack, scaling, failure modes, security, observability, deployment, and cross-cutting concerns — with embedded Mermaid diagrams where they clarify structure.
   `monorepo` — `/architecture payments-service` → writes `payments-service/docs/architecture.md`

6. **`/reference` — Project reference**
   Creates or updates `docs/REFERENCE.md`: repository layout, authoritative docs, commands, module boundaries, domain vocabulary, integrations, testing strategy, and operational notes. Links to canonical docs rather than duplicating them, and marks gaps as *Not documented yet*.
   `monorepo` — `/reference payments-service` → writes `payments-service/docs/REFERENCE.md`

7. **`/reverse-engineer` — Existing-code baseline**
   An alternative starting point for an inherited service. It reads source, build configuration, APIs, tests, and schema evidence, then creates the canonical `requirements.md`, one `UC-*.md` specification per use case, and `entity_model.md`. Review the recovered business intent before continuing with implementation.
   `monorepo` — `/reverse-engineer payments-service` → writes the baseline under `payments-service/docs/`

## Part 2 — Implementation: Compose / Ktor / Exposed (`aiup-compose-ktor-exposed` 1.4.0)

The Construction phase for a Kotlin Multiplatform project. These skills are provided directly by `aiup-compose-ktor-exposed`. They discover the scoped stack and its modules before changing code; run implementation and test skills per use case ID.

1. **`/flyway-migration` — PostgreSQL migrations**
   Reads `docs/entity_model.md` and existing migrations and writes versioned Flyway scripts (`V001__create_*.sql`…) under the discovered server module's migration directory. Existing migration conventions win; `BIGSERIAL`, `TIMESTAMPTZ`, constraints, indexes, and `updated_at` triggers are reference defaults only when they match the project. Mappings remain compatible with Exposed.
   `monorepo` — `/flyway-migration in payments-service` → writes `payments-service/<server-module>/src/main/resources/db/migration/V*.sql`

2. **`/implement UC-001` — Backend implementation**
   Reads the use case spec, entity model, migrations, and existing conventions, then implements a vertical slice: shared `@Serializable` DTOs in the KMP module, domain models, repository ports, Exposed tables/repositories, application services, Ktor route functions, and Koin DI wiring. Runs focused build checks after each layer. Does **not** write tests.
   `monorepo` — `/implement UC-001 in payments-service` → backend + shared DTOs land under `payments-service/` modules

3. **`/implement-ui UC-001` — Compose UI**
   Implements Compose Multiplatform UI: typed suspend functions on the Ktor API client with JSON serialization, plain ViewModel classes holding Compose state with constructor-injected dependencies, and small accessible Material 3 composables wired into the existing navigation — no global service locators. It stops and reports the missing prerequisites if the backend DTOs or routes do not exist.
   `monorepo` — `/implement-ui UC-001 in payments-service` → Compose screen, ViewModel & API client under the service's UI module

4. **`/ktor-test UC-001` — Backend tests**
   Generates Ktor `testApplication` route tests with fake repository/service ports covering success, validation, not-found, auth, and key alternative flows. Adds `MockEngine` tests for outbound clients, extends `ArchitectureTest.kt` for new modules, and adds Testcontainers + Flyway integration tests under `src/testContainerTest` when persistence behaviour needs a real PostgreSQL.
   `monorepo` — `/ktor-test UC-001 in payments-service` → tests under the discovered server module's `src/test` and `src/testContainerTest`

5. **`/compose-test UC-001` — UI tests**
   Generates UI-side tests in the lightest useful layer: Ktor `MockEngine` API-client tests in `commonTest`, coroutine ViewModel tests with fakes and `runTest`, OIDC/PKCE or `expect`/`actual` platform tests in `jvmTest`/`wasmJsTest`, and Compose semantics tests with `runComposeUiTest` when dependencies exist. No real network calls.
   `monorepo` — `/compose-test UC-001 in payments-service` → tests under the discovered UI module's `commonTest` or platform source set

6. **`/implementation-status UC-001` — Status & traceability**
   Reads the entity model, use case specs, source, tests, and migrations and maintains an entity implementation-status matrix (Entity / DB Table / Domain Model / Repository / Service / Migrations). Writes per-use-case Markdown status pages under `docs/use_cases/`, cross-referencing migration version ranges and code/test evidence. Reports missing or partial implementation honestly.
   `monorepo` — `/implementation-status UC-001 in payments-service` → writes `payments-service/docs/use_cases/UC-001-implementation-status.md`

## Skills Reference

| Skill | Phase | Input | Output | Plugin |
|---|---|---|---|---|
| `/requirements` | Inception | `<service>/docs/vision.md` | `requirements.md` | core |
| `/entity-model` | Elaboration | `requirements.md` | `entity_model.md` | core |
| `/use-case-diagram` | Elaboration | `requirements.md` | same file's Mermaid slot | core |
| `/use-case-spec` | Construction | UC ID(s) + requirements | `docs/use_cases/UC-*.md` | core |
| `/architecture` | Construction | service docs + source | `architecture.md` | core |
| `/reference` | Any | service docs + source | `REFERENCE.md` | core |
| `/reverse-engineer` | Any | existing service source | requirements + specs + model | core |
| `/flyway-migration` | Construction | model + use cases | server module `V*.sql` | compose-ktor-exposed |
| `/implement` | Construction | UC ID | Ktor backend + shared DTOs | compose-ktor-exposed |
| `/implement-ui` | Construction | UC ID | Compose screen + ViewModel + client | compose-ktor-exposed |
| `/ktor-test` | Construction | UC ID + backend | route / unit / integration tests | compose-ktor-exposed |
| `/compose-test` | Construction | UC ID + UI | client / ViewModel / UI tests | compose-ktor-exposed |
| `/implementation-status` | Construction | UC ID(s) | status matrix + HTML pages | compose-ktor-exposed |

## What the Scoped Service Looks Like

After the full workflow, one service has a predictable documentation and implementation tree:

```text
payments-service/
├── docs/
│   ├── vision.md                 ← you maintain this
│   ├── requirements.md           ← /requirements + /use-case-diagram
│   ├── entity_model.md           ← /entity-model, updated by /implementation-status
│   ├── architecture.md           ← /architecture
│   ├── REFERENCE.md              ← /reference
│   └── use_cases/                ← /use-case-spec + /implementation-status
│       ├── UC-001-create-reservation.md
│       └── UC-001-implementation-status.md
├── <server-module>/src/
│   ├── main/
│   │   ├── kotlin/               ← /implement
│   │   └── resources/db/migration/  ← /flyway-migration  (V001__*.sql …)
│   └── test/                     ← /ktor-test
├── <shared-module>/src/commonMain/  ← shared DTOs from /implement
├── <ui-module>/src/commonMain/      ← /implement-ui
└── settings.gradle.kts
```

## Tips for Success

**Maintain traceability**
Every entity maps to a functional requirement; every use case traces to one or more FRs; every test references a UC ID. Keep the stable IDs the skills produce.

**Edit between steps**
The intermediate documents are designed to be reviewed and corrected by hand. Don't skip the review.

**Re-run upstream skills**
When a requirement changes, re-run `/entity-model` and `/use-case-diagram` so downstream artifacts stay consistent. Re-running is cheap; fixing drift later is not.

**Commit `docs/`**
The vision, requirements, entity model, and specs are your project's institutional memory — they explain *why* the code is the way it is.

---

AI Unified Process Marketplace · Core + Compose/Ktor/Exposed · [unifiedprocess.ai](https://unifiedprocess.ai)
