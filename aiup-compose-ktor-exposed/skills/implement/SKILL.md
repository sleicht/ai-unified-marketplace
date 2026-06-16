---
name: implement
description: >
  Implements backend use cases in the Compose/Ktor/Exposed stack using the
  current reference service style: vertical server modules, domain
  models, repository ports, Exposed persistence, application services, Ktor
  routes, Koin DI, and shared DTOs. Use when the user asks to "implement a use
  case", "build the backend", "create the API", "write the data access layer",
  or mentions Ktor implementation, Exposed repositories, REST endpoints, or
  backend development.
---

# Implement Use Case (Backend)

## Instructions

Implement the backend for use case $ARGUMENTS. Follow the target project's existing conventions first. When the target project resembles reference service, use `references/service-style.md` as the canonical style guide.

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

Read `references/service-style.md` (absolute path: prepend the "Base directory for this skill:" value from your system context) before editing code. Apply its conventions for:
- Multi-module layout discovery
- Server package architecture
- Domain/repository/persistence/application/rest boundaries
- DTO placement and mapping rules
- Route authorization and API Gateway annotations
- Koin module composition
- Build and verification commands

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
data class Patient(
    val id: Long? = null,
    val partnerContractNumber: String? = null,
    val ahvNumber: String? = null,
    val lastName: String,
    val firstName: String,
    val active: Boolean = true,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    init {
        require(partnerContractNumber != null || ahvNumber != null) {
            "At least one of partnerContractNumber or ahvNumber must be set"
        }
    }
}
```

### Repository Port

```kotlin
interface PatientRepository {
    suspend fun create(patient: Patient): Patient
    suspend fun update(patient: Patient): Patient
    suspend fun findById(id: Long): Patient?
    suspend fun findAll(limit: Int): List<Patient>
}
```

### Exposed Table

```kotlin
object PatientTable : Table("patient") {
    val id = long("id").autoIncrement()
    val partnerContractNumber = varchar("partner_contract_number", 50).nullable()
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
class ExposedPatientRepository : PatientRepository {
    override suspend fun findById(id: Long): Patient? = suspendTransaction {
        PatientTable.selectAll()
            .where { PatientTable.id eq id }
            .map { it.toPatient() }
            .singleOrNull()
    }

    private fun ResultRow.toPatient() =
        Patient(
            id = this[PatientTable.id],
            partnerContractNumber = this[PatientTable.partnerContractNumber],
            active = this[PatientTable.active],
        )
}
```

Use private `ResultRow.toXxx()` repository mappers. Use private column-mapping helpers for insert/update symmetry.

### Route Pattern

```kotlin
fun Route.patientRoutes() {
    val patientRepository by inject<PatientRepository>()

    route("/patients") {
        hasEmployeeOrMicroserviceAuth()
        listPatients { patientRepository }
        getPatientById { patientRepository }
    }
}

private fun Route.getPatientById(patientRepository: () -> PatientRepository) {
    get("/{id}") { respondPatientById(call, patientRepository()) }
}
```

Keep route blocks small. Use route-local mappers when mapping domain models to shared response DTOs.

### DI Pattern

```kotlin
private val repositoryModule = module {
    single<PatientRepository> { ExposedPatientRepository() }
}

private val serviceModule = module {
    single<MassImportService> {
        MassImportServiceImpl(
            patientRepository = get(),
            transactionRunner = get(),
        )
    }
}

val appModule = module { includes(repositoryModule, serviceModule) }
```

## Workflow

1. Read the use case spec from `docs/use_cases/`.
2. Read `docs/entity_model.md` and `docs/architecture.html` when present.
3. Read `references/service-style.md` (absolute path: prepend the "Base directory for this skill:" value from your system context).
4. Discover module names from `settings.gradle.kts` and package names from existing files.
5. Inspect nearest existing feature module and mirror its structure, imports, formatting, auth, and error handling.
6. Create or update shared DTOs only for API/UI boundaries.
7. Create or update domain models and repository interfaces.
8. Create or update Exposed table objects matching Flyway schema.
9. Implement Exposed repositories using `suspendTransaction` and private mappers.
10. Implement application service only when orchestration spans multiple dependencies or transactions.
11. Implement Ktor routes with auth helpers and route-local mappers.
12. Register repositories/services in Koin DI.
13. Wire routes in top-level `Routing.kt` under existing `/api/v1` structure.
14. Run LSP diagnostics for touched Kotlin files.
15. Verify with project command: prefer `mise run compile`, then `mise run verify`; fallback to module Gradle tasks.

## Resources

- `references/service-style.md` — canonical service style
