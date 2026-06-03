# Compose/Ktor/Exposed Service Implementation Style

Use this reference when implementing use cases in the Compose/Ktor/Exposed stack. It captures reusable conventions for a production-grade KMP service.

## Project Shape

Use a Gradle multi-module layout when present:

| Module | Purpose |
|---|---|
| `service-server` or `*-server` | Ktor backend, persistence, DI, routes, app plugins |
| `service-shared` or `*-shared` | KMP `commonMain` shared `@Serializable` DTOs used by server and UI |
| `service-ui` or `*-ui` | Compose Multiplatform UI and Ktor client |
| `service-thirdparty` or `*-thirdparty` | Generated upstream OpenAPI clients |

Discover actual module names from `settings.gradle.kts`; do not hardcode example module names in generated code.

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

## Domain Models

Create domain models in `modules/<feature>/domain/model`.

Pattern:

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

Use domain models for business rules and persistence. Keep shared DTOs in the shared module for API/UI boundaries.

## Shared DTOs

Create shared DTOs in `<shared-module>/src/commonMain/kotlin/<base-package>/shared`.

Pattern:

```kotlin
@Serializable
data class PatientListItem(
    val id: Long,
    val partnerContractNumber: String?,
    val firstName: String,
    val lastName: String,
    val active: Boolean,
)
```

Expose only fields required by the use case. Do not leak internal fields such as AHV numbers in list DTOs unless explicitly required.

## Repository Interfaces

Define interfaces in `domain/repository`; implement in `infrastructure/persistence`.

Pattern:

```kotlin
interface PatientRepository {
    suspend fun create(patient: Patient): Patient
    suspend fun update(patient: Patient): Patient
    suspend fun findById(id: Long): Patient?
    suspend fun findAll(limit: Int): List<Patient>
}
```

Keep repository interfaces expressed in domain models, not DTOs.

## Exposed Tables

Use Exposed v1 DSL imports:

```kotlin
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone
```

Pattern:

```kotlin
/** Exposed table definition for the `patient` table. Maps to V001 migration schema. */
object PatientTable : Table("patient") {
    val id = long("id").autoIncrement()
    val partnerContractNumber = varchar("partner_contract_number", 50).nullable()
    val active = bool("active").default(true)
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")

    override val primaryKey = PrimaryKey(id)
}
```

Use plain `Table`, not `LongIdTable`, when migrations use `BIGSERIAL`/`BIGINT` IDs and table objects map existing schema directly.

## Exposed Repositories

Use coroutine-safe transactions with `suspendTransaction`.

Pattern:

```kotlin
class ExposedPatientRepository : PatientRepository {
    override suspend fun create(patient: Patient): Patient = suspendTransaction {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val result = PatientTable.insert { mapPatientColumns(it, patient, now, includeCreatedAt = true) }
        val id = result.resultedValues?.firstOrNull()?.get(PatientTable.id)
            ?: error("Insert into patient table returned no rows")
        patient.copy(id = id, createdAt = now.toInstant(), updatedAt = now.toInstant())
    }

    override suspend fun findById(id: Long): Patient? = suspendTransaction {
        PatientTable.selectAll().where { PatientTable.id eq id }.map { it.toPatient() }.singleOrNull()
    }

    private fun ResultRow.toPatient() = Patient(id = this[PatientTable.id], active = this[PatientTable.active])
}
```

Use private `ResultRow.toXxx()` mappers inside repository implementations. Use private `mapXxxColumns()` helpers to avoid duplicating insert/update column assignment.

## Application Services

Put orchestration in `modules/<feature>/application` when a use case spans repositories, upstream sources, notifications, or transactions.

Pattern:

```kotlin
class MassImportServiceImpl(
    private val patientRepository: PatientRepository,
    private val idMappingRepository: IdMappingRepository,
    private val transactionRunner: TransactionRunner,
) : MassImportService {
    override suspend fun execute(): ImportRun {
        // Fetch upstream, batch, validate, persist, update status, notify.
    }

    private suspend fun processPatient(patient: UpstreamPatient) = transactionRunner.run {
        val saved = upsertPatient(patient)
        updateSyncStatus(saved)
    }
}
```

Use `TransactionRunner` for multi-repository atomic operations.

## Mapping Convention

Follow ADR-006:

1. Cross-layer mappers use extension functions on the source type in `*Mappers.kt` co-located with the target type's model package.
2. Repository `ResultRow.toXxx()` mappers stay private inside the Exposed repository.
3. Route-local domain → response DTO mappers can stay in the route file when the DTO is defined or used only there.

## Ktor Routing

Top-level `Routing.kt` wraps `/api/v1` with authentication and API Gateway exposure:

```kotlin
fun Application.configureRouting() {
    routing {
        authenticate(*authProviders, strategy = AuthenticationStrategy.FirstSuccessful) {
            exposeToCompany {
                route("/api/v1") {
                    patientRoutes()
                    resolveRoutes()
                    massImportRoutes()
                }
            }
        }
    }
}
```

Feature routes:

```kotlin
fun Route.patientRoutes() {
    val patientRepository by inject<PatientRepository>()

    route("/patients") {
        hasEmployeeOrMicroserviceAuth()
        listPatients { patientRepository }
        getPatientById { patientRepository }
    }
}
```

Use small private route helper functions (`listPatients`, `getPatientById`, `respondPatientById`) instead of one large route block. Return `respondText("400: Bad Request", status = HttpStatusCode.BadRequest)` or shared helpers when matching existing service style.

## Authorization

When Company libraries exist, use route-scoped auth helpers:

- `hasEmployeeAuth()` for human/admin endpoints.
- `hasMicroserviceAuth()` for service-to-service endpoints.
- `hasEmployeeOrMicroserviceAuth()` for both.

Also include API Gateway DSL annotations through `allowEmployeeAuth`, `allowMicroservicesAuth`, `authorizedRoles`, and `exposeToCompany` where the project already uses them.

## Dependency Injection

Compose Koin modules in `di/DependencyInjection.kt`:

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

Register application-scoped dependencies using `single<Application> { this@configureDependencyInjection }` if needed for config or lifecycle-bound HTTP clients.

## UI Style

Use constructor injection rather than Koin inside composables:

```kotlin
@Composable
fun App(apiClient: ServiceApiClient = ServiceApiClient()) {
    val scope = rememberCoroutineScope()
    val patientVm = remember { PatientViewModel(apiClient, scope) }
    MaterialTheme { PatientBrowserScreen(patientVm) }
}
```

ViewModels are plain classes with Compose state:

```kotlin
class PatientViewModel(private val api: ServiceApiClient, private val scope: CoroutineScope) {
    var patients by mutableStateOf<List<PatientListItem>>(emptyList())
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun loadPatients() {
        scope.launch {
            try { patients = api.listPatients(limit = 100) }
            catch (e: Exception) { error = "Failed to load patients: ${e.message}" }
        }
    }
}
```

Screens accept ViewModels, use small private composables, and show errors through reusable components such as `ErrorBanner`.

## UI API Client Style

Use Ktor Client with shared JSON configuration:

```kotlin
private val serviceJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

internal fun HttpClientConfig<*>.installServiceContentNegotiation() {
    install(ContentNegotiation) { json(serviceJson) }
}

class ServiceApiClient(
    baseUrl: String = "http://localhost:5600",
    private val bearerToken: String = DEFAULT_POC_EMPLOYEE_TOKEN,
    val httpClient: HttpClient = createServiceHttpClient(),
) {
    private val apiBase = "${baseUrl.trimEnd('/')}/api/v1"

    suspend fun listPatients(limit: Int = 50): List<PatientListItem> =
        httpClient.get("$apiBase/patients") {
            bearerAuth(bearerToken)
            parameter("limit", limit)
        }.body()
}
```

## Testing Style

Unit route tests use Ktor `testApplication`, fake repositories, and Company `IntegrationTestHelper` when auth is enabled.

Use `kotlin.test` assertions in existing style, not Kotest, unless the target project already uses Kotest.

Pattern:

```kotlin
class PatientRoutesTest {
    private val helper = IntegrationTestHelper(additionalConfig = { /* auth setup */ })

    private fun ApplicationTestBuilder.configureTestApp(repo: PatientRepository = fakePatientRepository()) {
        helper.installAuth(this)
        install(Koin) { modules(module { single<PatientRepository> { repo } }) }
        application {
            configureSerialization()
            configureRouting()
        }
    }

    @Test
    fun `GET patients returns list`() = testApplication {
        configureTestApp()
        client.get("/api/v1/patients") {
            header(HttpHeaders.Authorization, "Bearer ${employeeToken()}")
        }.apply { assertEquals(HttpStatusCode.OK, status) }
    }
}
```

Repository integration tests belong in `src/testContainerTest` and use Testcontainers, Flyway migrations, HikariCP, and Exposed `Database.connect(ds)`.

UI API client tests use Ktor `MockEngine` in `commonTest` and `kotlinx.coroutines.test.runTest`.

## Migration Style

Current service uses Flyway SQL with `BIGSERIAL` primary keys, explicit constraints, indexes, and PostgreSQL triggers for `updated_at`.

Pattern:

```sql
CREATE TABLE patient (
    id                      BIGSERIAL       PRIMARY KEY,
    partner_contract_number VARCHAR(50)     UNIQUE,
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_patient_correlation CHECK (
        partner_contract_number IS NOT NULL OR ahv_number IS NOT NULL
    )
);

CREATE INDEX idx_patient_active ON patient(active);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_patient_updated_at BEFORE UPDATE ON patient FOR EACH ROW EXECUTE FUNCTION set_updated_at();
```

Do not create standalone sequences unless the target project already uses them. Match existing migrations.

## Build and Verification Commands

Prefer `mise.toml` tasks when present:

| Purpose | Command |
|---|---|
| Compile backend | `mise run compile` |
| Verify backend | `mise run verify` |
| Unit tests | `mise run test` or `mise run test <ClassName>` |
| Testcontainers | `mise run tc-test` or `mise run tc-test <ClassName>` |
| All tests | `mise run test-all` |
| Format | `mise run format-check` / `mise run format` |
| UI desktop | `mise run run-admin-ui` |
| UI web | `mise run run-admin-ui-web` |

Fallback to Gradle module tasks if no mise file exists.
