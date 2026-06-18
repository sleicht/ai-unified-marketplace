# Compose/Ktor/Exposed Service Implementation Style

Use this reference when implementing or testing use cases in the Compose/Ktor/Exposed stack. Prefer the target project's existing conventions; use these patterns when the project matches the reference style.

## Project Shape

Projects may be a single Gradle build or a mise monorepo of independent Gradle composite builds. In a monorepo:

- Run commands from the monorepo root as `mise run //<stack>:<task>`.
- Run bare `mise run <task>` only when already inside the stack directory.
- Discover modules from the stack's own `settings.gradle.kts`; do not infer modules from sibling stacks.
- Each stack owns its own Gradle wrapper and `gradle/libs.versions.toml`.
- Kotlin/Ktor projects can expose both a project `libs.*` version catalog and a Ktor-published `ktorLibs.*` catalog; use the catalog names already present in the target build.

Common modules:

| Module | Purpose |
|---|---|
| `service-server` or `*-server` | Ktor backend, persistence, DI, routes, app plugins |
| `service-shared` or `*-shared` | KMP `commonMain` shared `@Serializable` DTOs used by server and UI |
| `service-ui` or `*-ui` | Compose Multiplatform UI and Ktor client |
| `service-thirdparty` or `*-thirdparty` | Generated upstream OpenAPI clients |

Discover actual module names from `settings.gradle.kts`; do not hardcode example module names in generated code.

## Versions and Gotchas

Read versions from the target stack's `gradle/libs.versions.toml` or existing build files. Do not hardcode dependency versions in generated build edits.

Watch for these reference-service constraints:

- Kotlin may be pinned below `2.4` because of Ktor FIR compiler-plugin compatibility.
- Flyway 12+ uses service-provider loading for database support; shadowed apps may need `mergeServiceFiles()` and `DuplicatesStrategy.INCLUDE` so location handlers remain discoverable.
- Exposed v1 imports use `org.jetbrains.exposed.v1.*` packages.
- JVM toolchain version should come from the catalog/build, not from memory.
- Compose UI targets commonly use `jvm()` plus `wasmJs { browser() }`; CI may conditionally disable or adjust wasm browser tasks.
- `ProcessResources` token replacement can provide runtime URLs; prefer existing env-driven config over literals.

## Server Package Architecture

Organize server code as vertical slices under the project base package:

```text
<base-package>/
├── main.kt
├── Routing.kt
├── di/DependencyInjection.kt
├── infrastructure/
│   ├── persistence/
│   │   ├── Database.kt
│   │   └── TransactionRunner.kt
│   └── plugins/
│       ├── Authentication.kt
│       ├── Authorization.kt
│       ├── ApiGateway.kt
│       ├── Serialization.kt
│       └── StatusPages.kt
└── modules/
    └── <feature>/
        ├── api/                         # Port interfaces, upstream DTOs, public service API
        ├── application/                 # Use-case orchestration, mappers between APIs/domains
        ├── domain/
        │   ├── model/                   # Domain models, enums, validation in init blocks
        │   └── repository/              # Repository interfaces
        └── infrastructure/
            ├── persistence/             # Exposed tables + repository implementations
            └── rest/                    # Ktor route functions + route-local response mappers
```

Prefer this clean/hexagonal style over controllers directly accessing Exposed tables.

## Layering = ArchUnit

Layering rules are enforced by `ArchitectureTest.kt` when present. Treat that test as source of truth and update it when adding modules or new architectural boundaries.

Reference rules:

- Domain must not depend on application or infrastructure packages.
- Application must not depend on infrastructure packages.
- Infrastructure may depend inward on application/domain APIs.
- Cross-module access should go through `..api..` packages only.
- New modules need corresponding ArchUnit coverage when the project already enforces module boundaries.

Run `ArchitectureTest` as part of verification for backend changes.

## Domain Models

Create domain models in `modules/<feature>/domain/model`.

Pattern:

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

Use domain models for business rules and persistence. Keep shared DTOs in the shared module for API/UI boundaries.

## Shared DTOs

Create shared DTOs in `<shared-module>/src/commonMain/kotlin/<base-package>/shared`.

Pattern:

```kotlin
@Serializable
data class RecordListItem(
    val id: Long,
    val externalReference: String?,
    val displayName: String,
    val category: String,
    val active: Boolean,
)
```

Expose only fields required by the use case. Do not leak internal fields unless explicitly required.

## Repository Interfaces

Define interfaces in `domain/repository`; implement in `infrastructure/persistence`.

```kotlin
interface RecordRepository {
    suspend fun create(record: Record): Record
    suspend fun update(record: Record): Record
    suspend fun findById(id: Long): Record?
    suspend fun findAll(limit: Int): List<Record>
}
```

Keep repository interfaces expressed in domain models, not DTOs.

## Exposed Tables

Use Exposed v1 DSL imports when the project does:

```kotlin
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone
```

Pattern:

```kotlin
/** Exposed table definition for the `record` table. Maps to V001 migration schema. */
object RecordTable : Table("record") {
    val id = long("id").autoIncrement()
    val externalReference = varchar("external_reference", 50).nullable()
    val active = bool("active").default(true)
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")

    override val primaryKey = PrimaryKey(id)
}
```

Use plain `Table`, not `LongIdTable`, when migrations use `BIGSERIAL`/`BIGINT` IDs and table objects map existing schema directly.

## Exposed Repositories

Use coroutine-safe transactions with the project's existing transaction helper or `suspendTransaction`.

```kotlin
class ExposedRecordRepository : RecordRepository {
    override suspend fun findById(id: Long): Record? = suspendTransaction {
        RecordTable.selectAll().where { RecordTable.id eq id }.map { it.toRecord() }.singleOrNull()
    }

    private fun ResultRow.toRecord() = Record(id = this[RecordTable.id], active = this[RecordTable.active])
}
```

Use private `ResultRow.toXxx()` mappers inside repository implementations. Use private `mapXxxColumns()` helpers to avoid duplicated insert/update assignment.

## Application Services

Put orchestration in `modules/<feature>/application` when a use case spans repositories, upstream sources, notifications, or transactions. Use `TransactionRunner` for multi-repository atomic operations when the project provides one.

## Mapping Convention

1. Cross-layer mappers use extension functions on the source type in `*Mappers.kt` co-located with the target type's model package.
2. Repository `ResultRow.toXxx()` mappers stay private inside the Exposed repository.
3. Route-local domain → response DTO mappers can stay in the route file when the DTO is defined or used only there.

## Ktor Routing

Top-level routing usually wraps `/api/v1` with authentication and API Gateway exposure:

```kotlin
fun Application.configureRouting() {
    routing {
        authenticate(*authProviders, strategy = AuthenticationStrategy.FirstSuccessful) {
            exposeToCompany {
                route("/api/v1") {
                    recordRoutes()
                }
            }
        }
    }
}
```

Feature routes should be small route extensions with route-local helpers and response mappers.

## Authorization

When Company libraries exist, use route-scoped auth helpers such as `hasEmployeeAuth()`, `hasMicroserviceAuth()`, or `hasEmployeeOrMicroserviceAuth()`. Also include API Gateway DSL annotations where the project already uses them.

## Dependency Injection

Compose Koin modules in `di/DependencyInjection.kt`:

```kotlin
private val repositoryModule = module {
    single<RecordRepository> { ExposedRecordRepository() }
}

private val serviceModule = module {
    single<ExampleService> { ExampleServiceImpl(recordRepository = get(), transactionRunner = get()) }
}

val appModule = module { includes(repositoryModule, serviceModule) }
```

Register application-scoped dependencies using `single<Application> { this@configureDependencyInjection }` if needed for config or lifecycle-bound HTTP clients.

## UI Style and Auth

Use constructor injection rather than Koin inside composables. ViewModels are plain classes with Compose state and coroutine actions.

Before adding UI auth code, inspect the existing UI module:

- If an `auth/` stack exists, follow it. Common pieces include `AccessTokenProvider`, PKCE `expect`/`actual`, token exchange, desktop and wasm Keycloak adapters, and authenticated API-client wrappers.
- If no OIDC stack exists, keep the simple POC bearer-token pattern but isolate it in the API client so it can be replaced later.
- Prefer env/runtime-driven base URLs over `localhost` literals. Reuse generated runtime config, resource token replacement, or existing config objects.
- Preserve `jvm()` and `wasmJs { browser() }` target constraints; keep platform APIs behind `expect`/`actual`.

```kotlin
@Composable
fun App(apiClient: ServiceApiClient = ServiceApiClient()) {
    val scope = rememberCoroutineScope()
    val recordVm = remember { RecordViewModel(apiClient, scope) }
    MaterialTheme { RecordBrowserScreen(recordVm) }
}
```

## UI API Client Style

Use Ktor Client with shared JSON configuration and an injectable auth source:

```kotlin
class ServiceApiClient(
    baseUrl: String,
    private val accessTokenProvider: AccessTokenProvider,
    val httpClient: HttpClient = createServiceHttpClient(),
) {
    private val apiBase = "${baseUrl.trimEnd('/')}/api/v1"

    suspend fun listRecords(limit: Int = 50): List<RecordListItem> =
        httpClient.get("$apiBase/records") {
            bearerAuth(accessTokenProvider.accessToken())
            parameter("limit", limit)
        }.body()
}
```

When no auth stack exists, a default POC token constructor parameter is acceptable if the target project already uses that style.

## Testing Style

Test source-set placement:

| Suite | Use |
|---|---|
| `src/test` | Unit, route, application-service, outbound-client, and ArchUnit tests |
| `src/testContainerTest` | Testcontainers + Flyway + real PostgreSQL repository tests |
| `integrationTest` | Pipeline or environment integration checks when already configured |
| `preDeployIntegrationTest` | Pre-deploy smoke/integration checks when already configured |
| `commonTest` / `jvmTest` / `wasmJsTest` | KMP UI/client tests; use platform source sets for `expect`/`actual` code |

Route tests use Ktor `testApplication`, fake repositories, and the existing auth helper when routes are protected. Use `kotlin.test` assertions in existing style, not Kotest, unless the project already uses Kotest.

Repository integration tests belong in `src/testContainerTest` and use Testcontainers, Flyway migrations, HikariCP, and Exposed `Database.connect(ds)`.

UI API client tests use Ktor `MockEngine`; for OIDC/PKCE flows, test token-provider behaviour separately from screen rendering and provide deterministic fake providers.

## Migration Style

Use Flyway SQL with `BIGSERIAL` primary keys, explicit constraints, indexes, and PostgreSQL triggers for `updated_at` when existing migrations match that style.

```sql
-- V004__create_example_table.sql
-- Adds example table for UC-XXX.
-- Source: docs/entity_model.md (EXAMPLE)

CREATE TABLE example (
    id          BIGSERIAL   PRIMARY KEY,
    status      VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_example_status ON example(status);
CREATE TRIGGER trg_example_updated_at BEFORE UPDATE ON example FOR EACH ROW EXECUTE FUNCTION set_updated_at();
```

Do not create standalone sequences unless the target project already uses them. Match existing migrations.

## Build and Verification Commands

Detect command shape before running:

| Context | Command shape |
|---|---|
| Monorepo root with stack namespace | `mise run //<stack>:<task>` |
| Inside stack directory | `mise run <task>` |
| No mise task | `./gradlew <module>:<task>` from the owning Gradle build |

Common task names:

| Purpose | Preferred task |
|---|---|
| Compile backend/UI | `compile` |
| Verify backend | `verify` |
| Unit tests | `test` or `test <ClassName>` |
| ArchUnit | `test ArchitectureTest` or focused Gradle test selector |
| Testcontainers | `tc-test` or `tc-test <ClassName>` |
| All tests | `test-all` |
| Format | `format-check` / `format` |
| UI desktop | `run-admin-ui` or project equivalent |
| UI web | `run-admin-ui-web` or project equivalent |

Prefer focused commands for touched code, then run the broader stack verification when the change spans layers.
