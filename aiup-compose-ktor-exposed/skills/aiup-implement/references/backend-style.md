# Backend Implementation Style

Prefer the target project's existing conventions. Use these patterns when it matches the reference Compose/Ktor/Exposed service.

## Discovery and Commands

- In a mise monorepo, discover the owning stack from `mise.toml` and modules from that stack's `settings.gradle.kts`.
- Read module/package names, version catalog, toolchain, nearest feature, auth helpers, DI, routing, and `ArchitectureTest.kt` before editing.
- Do not infer module names from sibling builds or hardcode dependency versions.
- Reuse existing convention plugins and version-catalog aliases instead of copying build rules into a module.
- Preserve independently buildable service roots; shared build logic does not imply one Gradle build.
- Run `mise run //<stack>:<task>` from monorepo root, `mise run <task>` inside the stack, or the owning `./gradlew` task when no mise task exists.

## Server Shape

```text
<base-package>/
├── di/DependencyInjection.kt       # composition root
├── infrastructure/
└── modules/<feature>/
    ├── DependencyInjection.kt      # feature-owned bindings
    ├── api/
    ├── application/
    ├── domain/
    │   ├── model/
    │   └── repository/
    └── infrastructure/
        ├── persistence/
        └── rest/
```

Respect `ArchitectureTest.kt` when present:

- domain does not depend on application or infrastructure;
- application does not depend on infrastructure;
- cross-module access goes through `..api..` packages;
- infrastructure may depend inward.

## Domain, DTOs, and Repositories

- Put invariants in domain models using the project's validation style.
- Put repository interfaces in `domain/repository`, expressed in domain types.
- Put only API/UI boundary fields in shared `@Serializable` DTOs.
- Co-locate cross-layer extension mappers with the target model; keep `ResultRow` mappers private to repositories.

## Executable Persistence Example

Use [the record fixture](record-example/README.md) as the source of complete domain, table, mapper, repository and transaction examples. Do not maintain duplicate Kotlin snippets in this guide.

- Match Flyway types, nullability, lengths and constraints. Required strings are non-null in both Kotlin and SQL; inspect and migrate historical rows before tightening a constraint.
- Map all persisted values, including audit timestamps. Resolve java.time/kotlin.time/kotlinx.datetime and Exposed timestamp APIs from the installed versions.
- JDBC database operations remain blocking in suspend functions. Reuse the project's I/O dispatcher and transaction helper; R2DBC has a different non-blocking API.
- For atomic orchestration, one application transaction owns all repository calls. Nested calls must share that transaction; prove rollback with a failure after the first write.
- Keep ResultRow and insert/update mappers private. Do not introduce a transaction abstraction if an equivalent helper already exists.

## Application, Routing, and DI

- Add an application service only for orchestration across dependencies or a transaction boundary.
- Keep route extensions small and map domain objects to response DTOs at the route boundary.
- Preserve existing authentication/authorisation and API-gateway helpers.
- Register feature repositories, services, and adapters in a Koin module beside the owning feature.
- Keep the service composition root declarative: it includes feature modules and genuinely shared infrastructure only.

The fixture defines feature-owned bindings and a declarative appModule. Adapt its external Database binding to the target host; do not duplicate feature bindings in the root.

Verify the complete `appModule`, not isolated fragments. Existing graph tests may need explicit
framework-provided types or constructor parameters; model those with Koin `extraTypes` and
`injectedParameters` instead of weakening the production graph.

## Shared Contract Gates

Treat public declarations in build-crossing `*-shared` modules as versioned contracts. Run the
configured JVM/KLIB `apiCheck` after changing them. Update API dumps only for intentional changes;
an unexpected dump delta is an implementation defect to resolve, not a baseline to accept.
Also test affected JSON wire names, defaults, nullability and enums with representative old/new payloads. API dumps alone do not prove wire compatibility.

## Verification

Run focused compile/format checks, then the owning stack's verification when the change spans layers. Run existing DI graph and `ArchitectureTest` checks whenever bindings or dependency boundaries change, and `apiCheck` whenever a shared public contract changes.
