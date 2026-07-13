---
name: implement
description: >
  Implements backend use cases in the Compose/Ktor/Exposed stack using the
  current reference service style: vertical server modules, domain
  models, repository ports, Exposed persistence, application services, Ktor
  routes, Koin DI, and shared DTOs. Use when the user asks to "implement a use
  case", "build the backend", "create the API", "write the data access layer",
  or mentions Ktor implementation, Exposed repositories, REST endpoints, or
  backend development. This skill is backend-only; use implement-ui for Compose
  screens or client-side work.
---

# Implement Use Case (Backend)

## Instructions

Implement the backend for the use case named or implied by the user's request. Follow the target project's existing conventions first. When the target project resembles the reference service, use `references/backend-style.md` as the canonical style guide.

Use:
- Vertical server slices under `modules/<feature>/`
- Domain models with validation in `domain/model`
- Repository interfaces in `domain/repository`
- Exposed table objects and repository implementations in `infrastructure/persistence`
- Application services in `application` for orchestration
- Ktor routes in `infrastructure/rest`
- Shared `@Serializable` DTOs in the shared KMP module for API/UI boundaries
- Koin registration in `di/DependencyInjection.kt`

Do not create tests. Use `ktor-test` and `compose-test` for tests.
Do not create UI screens. Use `implement-ui` for UI.

## Required Reference

Read `references/backend-style.md`, resolved relative to this `SKILL.md`, before editing code. Apply its conventions for:
- Monorepo/stack module discovery from the owning `settings.gradle.kts`
- Version detection from `libs.versions.toml` and existing build catalogs
- Server package architecture
- Domain/repository/persistence/application/rest boundaries
- DTO placement and mapping rules
- ArchUnit layering rules and `ArchitectureTest` verification
- Route authorization and API Gateway annotations
- Koin module composition
- Namespaced mise or Gradle fallback verification commands

## DO NOT

- Put Exposed table access directly inside routes
- Use Exposed DAO style when project uses DSL repositories
- Put server-only code in `commonMain`
- Use `runBlocking` inside route handlers or repositories
- Inject dependencies inside domain models
- Skip domain validation for business invariants from the use case
- Create one large route function when existing style uses small private helpers
- Bypass route auth helpers in Company-style services
- Return internal domain models when shared response DTOs already exist or are required

## Target Architecture

```text
<server-module>/src/main/kotlin/<base-package>/
├── di/DependencyInjection.kt
└── modules/<feature>/
    ├── api/                         # Service/source ports, public API interfaces
    ├── application/                 # Use-case orchestration, cross-layer mappers
    ├── domain/
    │   ├── model/                   # Domain model + enums + init validation
    │   └── repository/              # Repository interfaces
    └── infrastructure/
        ├── persistence/             # Exposed Table + ExposedXxxRepository
        └── rest/                    # Ktor Route extension + response mappers

<shared-module>/src/commonMain/kotlin/<base-package>/shared/
└── XxxRequest.kt / XxxResponse.kt / XxxListItem.kt
```

## Implementation Patterns

### Domain Model

```kotlin
data class Record(
    val id: Long? = null,
    val externalReference: String? = null,
    val sourceReference: String? = null,
    val category: String,
    val displayName: String,
    val active: Boolean = true,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    init {
        require(!externalReference.isNullOrBlank()) {
            "externalReference must not be blank"
        }
    }
}
```

### Repository Port

```kotlin
interface RecordRepository {
    suspend fun create(record: Record): Record
    suspend fun update(record: Record): Record
    suspend fun findById(id: Long): Record?
    suspend fun findAll(limit: Int): List<Record>
}
```

### Exposed Table

```kotlin
object RecordTable : Table("record") {
    val id = long("id").autoIncrement()
    val externalReference = varchar("external_reference", 50).nullable()
    val active = bool("active").default(true)
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")

    override val primaryKey = PrimaryKey(id)
}
```

Use `Table` plus `long("id").autoIncrement()` when migrations use `BIGSERIAL`. Use Exposed v1 imports when the project already does:

```kotlin
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
```

### Exposed Repository

```kotlin
class ExposedRecordRepository : RecordRepository {
    override suspend fun findById(id: Long): Record? = suspendTransaction {
        RecordTable.selectAll()
            .where { RecordTable.id eq id }
            .map { it.toRecord() }
            .singleOrNull()
    }

    private fun ResultRow.toRecord() =
        Record(
            id = this[RecordTable.id],
            externalReference = this[RecordTable.externalReference],
            active = this[RecordTable.active],
        )
}
```

Use private `ResultRow.toXxx()` repository mappers. Use private column-mapping helpers for insert/update symmetry.

### Route Pattern

```kotlin
fun Route.recordRoutes() {
    val recordRepository by inject<RecordRepository>()

    route("/records") {
        hasEmployeeOrMicroserviceAuth()
        listRecords { recordRepository }
        getRecordById { recordRepository }
    }
}

private fun Route.getRecordById(recordRepository: () -> RecordRepository) {
    get("/{id}") { respondRecordById(call, recordRepository()) }
}
```

Keep route blocks small. Use route-local mappers when mapping domain models to shared response DTOs.

### DI Pattern

```kotlin
private val repositoryModule = module {
    single<RecordRepository> { ExposedRecordRepository() }
}

private val serviceModule = module {
    single<ExampleService> {
        ExampleServiceImpl(
            recordRepository = get(),
            transactionRunner = get(),
        )
    }
}

val appModule = module { includes(repositoryModule, serviceModule) }
```

## Workflow

1. Read the use case spec from the resolved docs path (`<service>/docs/use_cases/` in a monorepo service, otherwise `docs/use_cases/`).
2. Read the resolved `entity_model.md` and `architecture.html` when present.
3. Read `references/backend-style.md`.
4. Discover the owning stack/service, then discover module names from that stack's `settings.gradle.kts` and package names from existing files.
5. Read version and toolchain constraints from `gradle/libs.versions.toml` or existing build files before editing dependencies.
6. Inspect nearest existing feature module and mirror its structure, imports, formatting, auth, and error handling.
7. If `ArchitectureTest.kt` exists, read it before choosing package/module dependencies.
8. Create or update shared DTOs only for API/UI boundaries.
9. Create or update domain models and repository interfaces.
10. Create or update Exposed table objects matching Flyway schema.
11. Implement Exposed repositories using the project's transaction style and private mappers.
12. Implement application service only when orchestration spans multiple dependencies or transactions.
13. Implement Ktor routes with auth helpers and route-local mappers.
14. Register repositories/services in Koin DI.
15. Wire routes in top-level `Routing.kt` under existing `/api/v1` structure.
16. Update or extend `ArchitectureTest.kt` when adding modules or enforced architectural boundaries.
17. If language-server diagnostics are available, run them for touched Kotlin files.
18. Verify with the detected project command: `mise run //<stack>:compile` / `mise run //<stack>:verify` from a monorepo root, bare `mise run compile` / `mise run verify` inside a stack, or module Gradle tasks as fallback. Include a focused `ArchitectureTest` run when present.

## Resources

- `references/backend-style.md` — focused backend implementation style
