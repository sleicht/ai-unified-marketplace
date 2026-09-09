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

```kotlin
interface RecordRepository {
    suspend fun create(record: Record): Record
    suspend fun findById(id: Long): Record?
    suspend fun findAll(limit: Int): List<Record>
}
```

## Exposed Persistence

Use the imports and transaction helper already present. Reference services often use Exposed v1 DSL:

```kotlin
object RecordTable : Table("record") {
    val id = long("id").autoIncrement()
    val active = bool("active").default(true)
    val createdAt = timestampWithTimeZone("created_at")
    override val primaryKey = PrimaryKey(id)
}
```

- Match Flyway schema exactly.
- Use plain `Table` when existing migrations use `BIGSERIAL`/`BIGINT` and current mappings do so.
- Use coroutine-safe transactions (`suspendTransaction` or the project helper).
- Keep `ResultRow.toXxx()` and insert/update column helpers private.

## Application, Routing, and DI

- Add an application service only for orchestration across dependencies or a transaction boundary.
- Keep route extensions small and map domain objects to response DTOs at the route boundary.
- Preserve existing authentication/authorisation and API-gateway helpers.
- Register feature repositories, services, and adapters in a Koin module beside the owning feature.
- Keep the service composition root declarative: it includes feature modules and genuinely shared infrastructure only.

```kotlin
// modules/record/DependencyInjection.kt
internal val recordModule = module {
    single<RecordRepository> { ExposedRecordRepository() }
}

// di/DependencyInjection.kt
val appModule = module { includes(infrastructureModule, recordModule) }
```

Verify the complete `appModule`, not isolated fragments. Existing graph tests may need explicit
framework-provided types or constructor parameters; model those with Koin `extraTypes` and
`injectedParameters` instead of weakening the production graph.

## Shared Contract Gates

Treat public declarations in build-crossing `*-shared` modules as versioned contracts. Run the
configured JVM/KLIB `apiCheck` after changing them. Update API dumps only for intentional changes;
an unexpected dump delta is an implementation defect to resolve, not a baseline to accept.

## Verification

Run focused compile/format checks, then the owning stack's verification when the change spans layers. Run existing DI graph and `ArchitectureTest` checks whenever bindings or dependency boundaries change, and `apiCheck` whenever a shared public contract changes.
