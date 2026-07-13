# Implementation-Status Service Discovery

Use this reference to locate evidence. Prefer the target project's actual structure.

## Module Discovery

- In a mise monorepo, identify the owning stack from `mise.toml` and read that stack's `settings.gradle.kts`.
- Common modules are `*-server`, `*-shared`, and `*-ui`, but use discovered names only.
- Server production code is normally under `src/main/kotlin`; shared DTOs under `src/commonMain/kotlin`; UI evidence under the UI module's `commonMain` and platform source sets.
- Migrations normally live in the server module's `src/main/resources/db/migration`.

## Evidence Rules

- Open files before claiming implementation; names and search hits alone are insufficient.
- A table requires migration DDL evidence.
- A domain model requires a usable production class.
- Repository coverage is complete only when the port and implementation both exist; otherwise mark `Partial`.
- Service coverage may be an application service, a route-only implementation, or `Not needed` when evidence supports that design.
- Test evidence must identify an existing test file and its level (route, unit, ArchUnit, Testcontainers, UI).
- Use exact migration versions from filenames and DDL changes.

## Command Shape

Status generation is read-only and normally needs no build. If a focused command is necessary:

- monorepo root: `mise run //<stack>:<task>`
- inside stack: `mise run <task>`
- no mise task: the owning Gradle wrapper and module task

Do not run broad verification merely to produce status documentation.
