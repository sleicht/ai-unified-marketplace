---
name: implementation-status
description: >
  Produces and maintains implementation-status documentation for Compose/Ktor/Exposed projects: entity coverage matrices,
  migration version ranges, and per-use-case minimal HTML status pages. Use when the user asks for implementation status,
  coverage, traceability, "what is implemented", entity-to-code mapping, migration coverage, or status pages for use cases.
---

# Implementation Status

## Instructions

Create or update implementation-status artefacts for $ARGUMENTS by reading the entity model, use case specs, source code, and Flyway migrations. Report what exists; do not mark something implemented unless code or migration evidence is present.

Outputs:

- `entity_model.md` implementation-status matrix, appended or refreshed near the relevant entities
- `<service>/docs/use_cases/UC-XXX-implementation-status.html` for each requested use case in a monorepo service, otherwise `docs/use_cases/UC-XXX-implementation-status.html`

Use minimal HTML for status pages: semantic headings, one small table per section, inline Mermaid only when it clarifies traceability, and no brand-specific CSS.

## Path Resolution

Resolve docs paths before writing:

1. If the user names a service/module, use `<service>/docs/...`.
2. If cwd is inside a service of a monorepo, use that service's `docs/...`.
3. Detect a monorepo by `mise.toml` with stack tasks, `monorepo_root`, or multiple sibling `settings.gradle.kts` builds.
4. Otherwise use repo-root `docs/...`.

## Evidence Sources

Read these before updating status:

- `docs/entity_model.md`
- `docs/use_cases/UC-*.md` for requested use cases
- server module `src/main/kotlin/**/modules/**`
- shared module `src/commonMain/kotlin/**`
- UI module only when UI implementation status is requested
- Flyway migrations under `src/main/resources/db/migration`
- `references/service-style.md` for module/source-set discovery and command shape

## Entity Matrix

Maintain a matrix with these columns:

| Entity | DB Table | Domain Model | Repository | Service | Migrations |
|---|---|---|---|---|---|
| EXAMPLE | `record` | `Record` | `RecordRepository` / `ExposedRecordRepository` | `RecordService` or route-only | `V001-V003` |

Column rules:

- `Entity`: entity heading from `entity_model.md`.
- `DB Table`: table names confirmed in Flyway SQL; use `Missing` when absent.
- `Domain Model`: Kotlin model class found in `domain/model`.
- `Repository`: repository interface and implementation when both exist; mark partial if only one exists.
- `Service`: application service, route, or explicit `Not needed` when the use case is route/repository-only.
- `Migrations`: migration version or range that created/changed the table. Use exact versions when possible (`V001`, `V004-V006`).

Use conservative statuses: `Missing`, `Partial`, `Implemented`, `Not needed`, or exact symbol names. Do not infer implementation from names alone; open the files.

## Per-Use-Case HTML

For each requested use case, create/update:

```text
<docs>/use_cases/UC-XXX-implementation-status.html
```

Required sections:

1. `Implementation Status`
2. `Use Case Scope` — use case ID, title, primary actor, status
3. `Traceability` — requirements/use-case/entity/code links
4. `Entity Coverage` — filtered entity matrix rows
5. `Backend Coverage` — route, service, repository, DTO, migration evidence
6. `Test Coverage` — route/unit/ArchUnit/Testcontainers/UI test evidence if present
7. `Gaps` — missing or partial items with file-level evidence

## DO NOT

- Invent implementation evidence from the entity model alone
- Mark generated or placeholder code as implemented without usable behaviour
- Create company-specific styling, names, or libraries
- Rewrite requirements, use case specs, migrations, or source code
- Run broad builds just to produce a status page

## Workflow

1. Resolve service docs path and stack root.
2. Read `references/service-style.md`.
3. Read `entity_model.md` and requested use case specs.
4. Discover server/shared/UI modules from the owning stack's `settings.gradle.kts`.
5. Read Flyway migration filenames and SQL headers, especially `-- Source:` comments when present.
6. Search source files for entity model classes, repositories, services, routes, DTOs, and tests.
7. Update the entity implementation-status matrix in `entity_model.md`.
8. Write or refresh per-use-case implementation-status HTML.
9. Verify links point to existing relative paths and matrix columns line up.
10. If commands are needed, use detected command shape: `mise run //<stack>:<task>` from monorepo root, bare `mise run <task>` inside a stack, or Gradle fallback.

## Resources

- `references/service-style.md` — canonical Compose/Ktor/Exposed module, source-set, and command style
