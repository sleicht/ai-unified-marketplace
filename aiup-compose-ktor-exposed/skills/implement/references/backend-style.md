# Backend Implementation Style

Prefer the target project's existing conventions. Use these patterns when it matches the reference Compose/Ktor/Exposed service.

## Discovery and Commands

- In a mise monorepo, discover the owning stack from `mise.toml` and modules from that stack's `settings.gradle.kts`.
- Read module/package names, version catalog, toolchain, nearest feature, auth helpers, DI, routing, and `ArchitectureTest.kt` before editing.
- Do not infer module names from sibling builds or hardcode dependency versions.
- Run `mise run //<stack>:<task>` from monorepo root, `mise run <task>` inside the stack, or the owning `./gradlew` task when no mise task exists.

## Server Shape

```text
<base-package>/
├── di/DependencyInjection.kt
├── infrastructure/
└── modules/<feature>/
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
- Register repositories/services in the existing Koin module composition.

```kotlin
private val repositoryModule = module {
    single<RecordRepository> { ExposedRecordRepository() }
}

val appModule = module { includes(repositoryModule) }
```

## Verification

Run focused compile/format checks, then the owning stack's verification when the change spans layers. Run the focused `ArchitectureTest` whenever modules or dependency boundaries change.
