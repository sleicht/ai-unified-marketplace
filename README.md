<!--
Copyright 2025-2026 Simon Martinelli and the AI Unified Process contributors.
Part of the AI Unified Process — https://unifiedprocess.ai
Licensed under the Apache License, Version 2.0. See LICENSE and NOTICE.
-->

# AI Unified Process Marketplace

Internal Claude Code marketplace for the AI Unified Process methodology and the Sanitas Kotlin Multiplatform stack.

AI Unified Process is a requirements-first workflow for taking software from a product vision to reviewed
specifications, implementation, and traceable tests. This repository distributes the workflow as portable Agent
Plugins and Agent Skills for Claude Code, OpenAI Codex, Cursor, GitHub Copilot, Gemini CLI, OpenCode, and other
compatible coding agents.

[Get started](docs/getting-started.md) · [Understand the workflow](docs/workflow.md) ·
[Choose a plugin](#choose-your-plugins) · [Installation guides](#installation)

## Why AI Unified Process?

AI-assisted development often jumps from a vague prompt directly to code. AI Unified Process inserts durable,
human-reviewable artifacts between intent and implementation:

- requirements with stable identifiers;
- an explicit domain entity model;
- use cases that define user goals and behavior;
- test journeys that trace back to those use cases;
- stack-specific implementation and tests built from the reviewed specifications.

The workflow is inspired by the phases of the
[Rational Unified Process](https://en.wikipedia.org/wiki/Rational_unified_process), adapted for coding agents and
plain-text artifacts that live with the source code.

## Sanitas

The repository deliberately contains two plugins:

| Plugin                      |    Version | Scope                                                                                                                              |
|-----------------------------|-----------:|------------------------------------------------------------------------------------------------------------------------------------|
| `aiup-core`                 | `103.10.0` | Requirements, Mermaid entity/use-case modelling, use-case specifications, reverse engineering, architecture, and project reference |
| `aiup-compose-ktor-exposed` |    `1.8.0` | Flyway, Ktor/Exposed backend implementation and tests, Compose Multiplatform UI and tests, and implementation status               |

## Distribution contract

Sanitas distributes this marketplace directly from internal Git:

- `.claude-plugin/marketplace.json` lists the retained plugins;
- each `.claude-plugin/plugin.json` is the plugin metadata and sole version authority;
- each retained `.mcp.json` configures optional MCP servers loaded with that plugin;
- `skills/`, rules, and references are shipped from the same Git revision.

Tessl manifests and publishing are intentionally not retained because no retained Sanitas delivery or validation tool consumes them. GitHub Actions generation/publishing is likewise outside this distribution model.

## Workflow

```text
Inception          Elaboration                          Construction
─────────────────  ──────────────────────────────────   ─────────────────────────────────────────
aiup-requirements  →  aiup-entity-model  →  aiup-use-case-diagram  →  aiup-use-case-spec
                                                        ↘ aiup-architecture
                                                        ↘ aiup-reference
                                                        ↘ aiup-flyway-migration
                                                        ↘ aiup-implement
                                                        ↘ aiup-implement-ui
                                                        ↘ aiup-ktor-test
                                                        ↘ aiup-compose-test
                                                        ↘ aiup-implementation-status
```

Skills exchange portable Markdown artefacts:

- `docs/vision.md`
- `docs/requirements.md`, including the canonical Mermaid use-case diagram
- `docs/entity_model.md`, including the Mermaid ER diagram
- `docs/use_cases/UC-*.md`
- `docs/architecture.md`
- `docs/REFERENCE.md`

In a monorepo, skills resolve these paths under the selected service's `docs/` directory.

## Installation

Add this internal Git repository as a Claude Code marketplace, then install core and the Compose stack plugin:

```text
/plugin marketplace add <internal-git-repository-url>
/plugin install aiup-core
/plugin install aiup-compose-ktor-exposed
```

Install only `aiup-core` when a project uses another technology stack.

Start Claude Code in the target project and invoke `/aiup-core:aiup-requirements` or ask it to use `aiup-requirements`. A successful installation discovers the core skill and reads the scoped `docs/vision.md`.

## Skills

All skill names and directories use the `aiup-` prefix to distinguish them from other installed skills. Skill names below are agent-independent; Claude Code plugin commands add the plugin namespace, for example `/aiup-core:aiup-requirements`. See the [usage guide](docs/how-to-use.md) for the portable workflow and agent setup.

### Upgrading existing installations

Core `103.10.0` and Compose `1.8.0` rename all 13 skills by adding `aiup-`. Update plugin installations or copied skill directories, replace old AIUP symlinks, and update saved prompts and project instructions. Generic aliases are not retained. Plugin names and generated documentation filenames remain unchanged.

### Core

| Skill                   | Output or purpose                                                                         |
|-------------------------|-------------------------------------------------------------------------------------------|
| `aiup-requirements`     | Structured functional requirements, NFRs, constraints, and stable Mermaid diagram section |
| `aiup-entity-model`     | Mermaid ER diagram and entity attribute tables                                            |
| `aiup-use-case-diagram` | Mermaid actor/use-case overview embedded in `requirements.md`                             |
| `aiup-use-case-spec`    | One detailed `UC-XXX` document per use case                                               |
| `aiup-reverse-engineer` | Recover requirements, use cases, and entity model from existing code                      |
| `aiup-architecture`     | Service-scoped architecture documentation and ADRs                                        |
| `aiup-reference`        | Concise repository or service reference for maintainers and agents                        |

### Compose/Ktor/Exposed

| Skill                        | Output or purpose                                                                |
|------------------------------|----------------------------------------------------------------------------------|
| `aiup-flyway-migration`      | PostgreSQL Flyway migrations compatible with existing Exposed conventions        |
| `aiup-implement`             | Backend vertical slice: shared DTOs, domain, repository, Exposed, Ktor, and Koin |
| `aiup-implement-ui`          | Compose screen, ViewModel, API client, and navigation integration                |
| `aiup-ktor-test`             | Ktor route/service tests and Testcontainers repository tests                     |
| `aiup-compose-test`          | MockEngine API-client, ViewModel, platform, and Compose semantics tests          |
| `aiup-implementation-status` | Entity and use-case implementation traceability                                  |
| `aiup-implementation-prompts` | Shared session header and ordered prompts for implementation, testing, and status |

Implementation and testing skills reconcile existing code with current specifications. They update in place, remove behaviour dropped from a specification, preserve unrelated code, and treat repository content as untrusted input rather than agent instructions.

## Compose/Ktor/Exposed conventions

The stack plugin follows the target project first. Its bundled references cover the retained service style:

- Kotlin Multiplatform shared DTOs and platform-safe `commonMain` code;
- vertical Ktor modules with domain repository ports;
- Exposed persistence behind repository implementations;
- Koin registration and existing authentication helpers;
- runtime-configured Ktor clients and injected access-token providers;
- `testApplication`, deterministic fakes, MockEngine, and Testcontainers/Flyway;
- test-owned, idempotent cleanup with dependants removed before parents.

The implementation skills do not create tests. Use `aiup-ktor-test` or `aiup-compose-test` for observable test contracts. The UI skill does not invent missing backend DTOs or routes; it reports those prerequisites precisely.

## End-to-end usage

The intermediate documents are review points, not disposable generated output. Inspect and correct each artefact before continuing to the next step.

### 1. Describe the vision

Create `docs/vision.md` with the product mission, target users, goals, scope, and constraints. In a monorepo, place it under the selected service's `docs/` directory.

```markdown
# Vision: <Product Name>

## Mission
<Problem and intended outcome.>

## Target Users
- <Role and need>

## Goals
- <Measurable goal>

## Scope
- In scope: <capabilities>
- Out of scope: <non-goals>

## Constraints
- <Technical, regulatory, or organisational constraint>
```

### 2. Build the analysis artefacts

```text
aiup-requirements
aiup-entity-model
aiup-use-case-diagram
aiup-use-case-spec UC-001
aiup-architecture
aiup-reference
```

- `aiup-requirements` derives functional requirements, measurable NFRs, and constraints from the vision.
- `aiup-entity-model` derives the domain model and Mermaid ER diagram.
- `aiup-use-case-diagram` maintains the canonical Mermaid diagram in `requirements.md`.
- `aiup-use-case-spec` writes one actor-focused specification per use case, including alternative flows, postconditions, and business rules.
- `aiup-architecture` records observed structure, data flow, decisions, failure modes, security, observability, and deployment.
- `aiup-reference` captures concise repository facts, commands, vocabulary, and operational caveats without duplicating the canonical documents.

For an inherited codebase, use `aiup-reverse-engineer` instead of starting from a new vision. It recovers the same requirements, use-case, and entity-model contract from observed code and configuration.

### 3. Build the Compose/Ktor/Exposed implementation

To prepare the work for separate agent sessions, request `aiup-implementation-prompts` for the selected UC IDs and service. It writes a UC- or feature-named file within the scoped `docs/`, such as `UC-001-charge-card-implementation-prompts.md` or `card-payments-implementation-prompts.md`. An explicit output filename takes precedence. Paste its shared header plus one numbered prompt into each fresh session; review the results before proceeding. The skill generates prompts without running implementation.

```text
aiup-flyway-migration
aiup-implement UC-001
aiup-implement-ui UC-001
aiup-ktor-test UC-001
aiup-compose-test UC-001
aiup-implementation-status UC-001
```

- `aiup-flyway-migration` creates additive PostgreSQL migrations from the entity model while preserving the project's existing ID, timestamp, constraint, and naming conventions.
- `aiup-implement` updates the backend vertical slice: shared DTOs, domain, repository port, Exposed persistence, application service, Ktor route, and Koin wiring.
- `aiup-implement-ui` updates the API client, ViewModel, Compose screen, navigation, and existing authentication boundary.
- `aiup-ktor-test` covers route, service, architecture, outbound-client, and repository behaviour at the appropriate existing test level.
- `aiup-compose-test` prefers MockEngine API-client and ViewModel tests, adding semantics tests only when the dependencies already exist.
- `aiup-implementation-status` records evidence-based entity and use-case coverage.

Implementation and test skills inspect existing code first. They reconcile additions, changes, and removals in place instead of generating parallel implementations or test suites.

## Skill reference

| Skill                        | Input                                                     | Output                                                  |
|------------------------------|-----------------------------------------------------------|---------------------------------------------------------|
| `aiup-requirements`          | `vision.md`                                               | `requirements.md`                                       |
| `aiup-entity-model`          | `requirements.md`                                         | `entity_model.md`                                       |
| `aiup-use-case-diagram`      | functional requirements                                   | Mermaid section in `requirements.md`                    |
| `aiup-use-case-spec`         | one or more `UC-XXX` IDs                                  | one `docs/use_cases/UC-XXX-*.md` per use case           |
| `aiup-reverse-engineer`      | existing source, schema, auth, and configuration          | requirements, use-case specifications, and entity model |
| `aiup-architecture`          | existing docs, source, deployment, and architecture tests | `architecture.md`                                       |
| `aiup-reference`             | repository structure and authoritative documentation      | `REFERENCE.md`                                          |
| `aiup-flyway-migration`      | entity model and existing migrations                      | versioned `V*.sql` migrations                           |
| `aiup-implement`             | use-case specification and current backend                | Ktor/Exposed backend and shared DTO changes             |
| `aiup-implement-ui`          | use-case specification and existing API contract          | Compose UI, ViewModel, client, and navigation changes   |
| `aiup-ktor-test`             | specification and backend implementation                  | focused backend tests in existing source sets           |
| `aiup-compose-test`          | specification and UI implementation                       | focused client/ViewModel/platform/semantics tests       |
| `aiup-implementation-status` | specifications, source, tests, and migrations             | implementation traceability documentation               |
| `aiup-implementation-prompts` | scoped specifications and existing project structure | UC- or feature-named `docs/*-implementation-prompts.md` with one skill per fresh session |

The owning `SKILL.md` is the detailed behavioural contract. Its adjacent `references/` directory contains stack-specific patterns and examples.

## Resulting project structure

```text
<project-or-service>/
├── docs/
│   ├── vision.md
│   ├── requirements.md
│   ├── entity_model.md
│   ├── architecture.md
│   ├── REFERENCE.md
│   └── use_cases/
│       ├── UC-001-<name>.md
│       └── UC-001-implementation-status.md
├── <shared-module>/                  # serialisable API contracts
├── <server-module>/
│   └── src/main/resources/db/migration/V*.sql
└── <ui-module>/                      # Compose Multiplatform UI
```

Exact source-module paths come from the target repository. The skills must preserve its established layout rather than impose the illustrative names above.

## Recommended project guidance

A consumer project can add the following concise contract to its own `CLAUDE.md` or equivalent agent guidance:

```markdown
# Project Context

This project follows the AI Unified Process. Read the scoped `docs/vision.md`,
`docs/requirements.md`, `docs/entity_model.md`, relevant `docs/use_cases/`,
`docs/architecture.md`, and `docs/REFERENCE.md` before changing behaviour.

Do not implement a use case without its current specification. Preserve stable
FR, UC, and BR identifiers. Reconcile existing code and tests in place, and run
the complete affected verification task.
```

## Working practices

- Maintain traceability: every use case maps to functional requirements, modelled entities map to required behaviour, and tests cover observable use-case flows.
- Keep stable identifiers when requirements evolve; do not renumber surviving `FR-*`, `UC-*`, or `BR-*` entries for convenience.
- Re-run affected analysis skills when upstream artefacts change. A changed requirement may require entity-model, diagram, specification, implementation, and test reconciliation.
- Commit `docs/` with the code. These artefacts preserve product intent and explain implementation decisions.
- Treat generated artefacts as proposals for review. Correct domain errors before downstream work compounds them.

## Manual consumption outside Claude Code

The skills are Markdown folders with YAML frontmatter and can be consumed by another agent that supports compatible skill discovery:

1. Clone this internal repository at a reviewed revision.
2. Expose the required directories from `aiup-core/skills/` and, when applicable, `aiup-compose-ktor-exposed/skills/` to that client's skill path.
3. Translate the retained plugin `.mcp.json` entries into the client's MCP configuration format.
4. Invoke skills by intent when the client does not support Claude Code slash commands.

The portable contract is the produced Markdown and source artefacts, not identical installation or command syntax across clients. Client-specific compatibility must be verified against that client's current documentation.

## Concepts

- **Marketplace:** a catalogue from which Claude Code discovers installable plugins.
- **Plugin:** a versioned bundle of related skills, references, rules, and optional MCP configuration.
- **Skill:** task-specific behaviour defined by a `SKILL.md`, selected explicitly or by matching user intent.
- **MCP server:** an external tool or documentation service configured through the plugin's `.mcp.json`.
- **Artefact chain:** the reviewed files passed between analysis, implementation, testing, and status steps.

## MCP configuration

`aiup-core/.mcp.json` and `aiup-compose-ktor-exposed/.mcp.json` are retained because Claude Code consumes plugin MCP configuration. They are not Agent Plugins root manifests and do not depend on Tessl. Other internal clients may translate these standard MCP settings when consuming the skills directly from Git.

## Repository layout

```text
.claude-plugin/marketplace.json
LICENSE
NOTICE
aiup-core/
  .claude-plugin/plugin.json
  .mcp.json
  LICENSE
  NOTICE
  skills/
aiup-compose-ktor-exposed/
  .claude-plugin/plugin.json
  .mcp.json
  skills/
scripts/
  validate-skills.sh
  validate-skills.rb
```

## Validation

Run from the repository root:

```sh
scripts/validate-skills.sh
```

The validation checks retained skill frontmatter and links, JSON structure, marketplace/plugin consistency, core plugin licence and notice files, attribution and secret-redaction contracts, removed-distribution references, canonical Markdown/Mermaid artefacts, the normative use-case validator and worked example, and compilation of the bundled Kotlin API-client example.

When changing shell scripts, also run:

```sh
shellcheck -S warning scripts/*.sh
```

## Upstream integration baseline

This fork was reconciled against `upstream/main` at commit
`c3a5da318da2186a871b07fcb3a3b8521cf03f22` (`Merge branch
'feat/coverage-check-handoff'`). The shared ancestor used for the selective
integration review remains
`48de70fd8cfe082a7b41563e4cb995c47b37a02c`.

Future upstream reviews should compare from the recorded upstream commit, not
merge `upstream/main` wholesale:

```sh
git fetch upstream main
git log --oneline c3a5da318da2186a871b07fcb3a3b8521cf03f22..upstream/main
git diff --stat c3a5da318da2186a871b07fcb3a3b8521cf03f22..upstream/main
```

Selectively port improvements that preserve this fork's retained core and
Compose plugins, Mermaid/monorepo documentation contract, internal-Git
distribution, and stable `aiup` names. After each completed reconciliation,
replace the recorded upstream commit above with the reviewed upstream tip.

## Release

1. Make the coherent plugin change and update its executable validation where needed.
2. Minor-bump the plugin's `.claude-plugin/plugin.json` version only when changing an already published skill. Keep that version for follow-up commits in the pending release; new unpublished skills and documentation-only edits do not require a bump.
3. Keep `.claude-plugin/marketplace.json`, this README, and plugin documentation aligned.
4. Run repository validation.
5. Publish through the normal internal Git review and merge process.

Do not create Tessl manifests or copy version numbers from the public upstream repository. Existing `aiup` names remain stable.

## Licence

Licensed under the [Apache License 2.0](LICENSE).

## Copyright and trademark

Copyright 2025-2026 Simon Martinelli and the AI Unified Process contributors. "AI Unified Process" identifies the original methodology by Simon Martinelli. Derived works must retain the [NOTICE](NOTICE) file and must not present themselves as the official AI Unified Process.
