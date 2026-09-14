# Implementation-Status Service Discovery

Use this reference to locate evidence. Prefer the target project's actual structure.

## Module Discovery

- In a mise monorepo, identify the owning stack from `mise.toml` and read that stack's `settings.gradle.kts`.
- Common modules are `*-server`, `*-shared`, and `*-ui`, but use discovered names only.
- Server production code is normally under `src/main/kotlin`; shared DTOs under `src/commonMain/kotlin`; UI evidence under the UI module's `commonMain` and platform source sets.
- Kobweb UI lives in the discovered JS target: pages, layouts, app entry, DOM/Silk styles and browser tests. Check shared JS variants and exported-site verification separately from Compose UI targets.
- Migrations normally live in the server module's `src/main/resources/db/migration`.

## Evidence Rules

- Open files before claiming implementation; names and search hits alone are insufficient.
- A table requires migration DDL evidence.
- A domain model requires a usable production class.
- Repository coverage is complete only when the port and implementation both exist; otherwise mark `Partial`.
- Service coverage may be an application service, a route-only implementation, or `Not needed` when evidence supports that design.
- Backend DI evidence distinguishes feature-owned Koin bindings from root composition and requires a complete graph test before marking verification present.
- UI boundary evidence traces `ViewModel -> feature port -> API client`; state/action contracts count only when a screen section consumes them directly.
- Shared API compatibility requires configured validation and the applicable checked-in JVM/KLIB dumps for the affected contract module.
- Coverage gates require repository-local thresholds backed by a recorded or reproducible passing baseline; reports alone are not gates.
- Test evidence identifies scenario, file, level, command, execution date/revision when known and result. File presence is `Present, not run`, never proof of passing. Distinguish Kotlin, browser and exported-site checks; record failed/unrun checks and durable implementation handoffs.
- JSON wire compatibility needs representative payload evidence in addition to API dump validation.
- Use exact migration versions from filenames and DDL changes.

## Command Shape

Status generation is read-only and normally needs no build. If a focused command is necessary:

- monorepo root: `mise run //<stack>:<task>`
- inside stack: `mise run <task>`
- no mise task: the owning Gradle wrapper and module task

Do not run broad verification merely to produce status documentation.
