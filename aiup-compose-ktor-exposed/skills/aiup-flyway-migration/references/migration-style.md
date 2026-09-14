# Flyway Migration Style

Prefer the target project's existing migrations. Use these rules when it matches the reference Compose/Ktor/Exposed service.

## Discovery

- In a mise monorepo, discover the owning stack from `mise.toml` and its modules from that stack's `settings.gradle.kts`.
- Locate migrations under the discovered server module; do not infer module names from sibling builds.
- Read existing migrations to determine version padding, naming, ID strategy, audit columns, constraint naming, and trigger conventions.
- Read dependency/toolchain versions from the target build; do not hardcode them.

## Migration and Exposed Contract

Read [the executable migrations and mapping](../../aiup-implement/references/record-example/README.md). Keep the SQL and Kotlin example in that single checked location.

- Applied versioned migrations are immutable. Add migrations; preserve existing rows with an explicit backfill/constraint plan.
- Preserve existing sequence/identity/BIGSERIAL and Table/ID-table conventions.
- Match names, lengths, nullability, foreign keys and timestamp mappings exactly. Define blank/whitespace semantics consistently with domain validation.
- Define an audit function before the first trigger that invokes it, even when introducing audit fields into an existing schema.
- Choose indexes from query patterns and expected selectivity. Do not blindly index boolean/status columns or duplicate a unique-constraint index.

## Verification

Prefer focused repository migration or Testcontainers tasks. Command shape:

- monorepo root: `mise run //<stack>:<task>`
- inside stack: `mise run <task>`
- no mise task: the owning build's `./gradlew <module>:<task>`

Run broader compile/verification only when the change spans corresponding Kotlin table mappings.

Apply the full chain to an empty disposable PostgreSQL database and upgrade from the previous schema with representative rows. Assert affected constraints, backfills, audit triggers and repository round trips. Compilation and mental syntax checks are not migration execution.
