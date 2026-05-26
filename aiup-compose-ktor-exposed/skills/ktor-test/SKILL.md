---
name: ktor-test
description: >
  Creates backend tests for Ktor/Exposed services in the reference service
  style: Ktor testApplication route tests, fake repository
  ports, Company auth test helpers when present, kotlin.test assertions,
  MockEngine for outbound clients, and Testcontainers repository integration
  tests. Use when the user asks to "write API tests", "test the Ktor
  endpoints", "create backend tests", "unit test the routes", or mentions Ktor
  testing, TestHost, repository integration tests, or endpoint tests.
---

# Ktor Test

## Instructions

Create backend tests for use case $ARGUMENTS. Follow the target project's existing test style first. When the project resembles reference service, use `../_references/service-style.md` as the canonical style guide.

Use:
- Ktor `testApplication {}` for route tests
- Fake repository/service implementations for route unit tests
- Company `IntegrationTestHelper` when auth is enabled in routes
- `kotlin.test` assertions if existing tests use them
- Ktor `MockEngine` for outbound HTTP clients
- Testcontainers + Flyway for repository integration tests

Do not start a real HTTP server for route tests.
Do not use a real database for route unit tests.

## Required Reference

Read `../_references/service-style.md` before writing tests. Apply its route-test, fake-dependency, auth-token, and Testcontainers conventions.

## DO NOT

- Use Mockito/MockK for repositories when a small fake object is clearer
- Bypass route authentication unless the route is intentionally public
- Hardcode server ports in route tests
- Use `runBlocking` inside `testApplication` route tests
- Use Kotest assertions in projects that use `kotlin.test`
- Delete all shared data in cleanup outside tables owned by the test
- Put Testcontainers tests in `src/test` when the project has `src/testContainerTest`

## Route Test Pattern

```kotlin
class PatientRoutesTest {

    private val helper =
        IntegrationTestHelper(
            additionalConfig = {
                clients {
                    authorityBinding("ms-service-name") {
                        realm = "microservices"
                        authorities = setOf("MICROSERVICE")
                    }
                }
                roles {
                    additionalAuthority("DL_COMPANY_APP_ADMIN") {
                        authorities = listOf("EMPLOYEE")
                    }
                }
            }
        )

    private val samplePatient = Patient(
        id = 1L,
        partnerContractNumber = "P-1001",
        lastName = "Muster",
        firstName = "Max",
        dateOfBirth = LocalDate.of(1985, 3, 15),
        gender = Gender.MALE,
    )

    private fun fakePatientRepository(patients: List<Patient> = listOf(samplePatient)) =
        object : PatientRepository {
            override suspend fun create(patient: Patient) = patient
            override suspend fun update(patient: Patient) = patient
            override suspend fun findById(id: Long) = patients.find { it.id == id }
            override suspend fun findAll(limit: Int) = patients.take(limit)
        }

    private fun ApplicationTestBuilder.configureTestApp(
        patientRepo: PatientRepository = fakePatientRepository(),
    ) {
        helper.installAuth(this)
        install(Koin) { modules(module { single<PatientRepository> { patientRepo } }) }
        application {
            configureSerialization()
            configureRouting()
        }
    }

    private fun employeeToken() =
        helper.employeeToken(cNumber = "C123456", roles = listOf("DL_COMPANY_APP_ADMIN"))

    @Test
    fun `GET patients returns list`() = testApplication {
        configureTestApp()
        client
            .get("/api/v1/patients") {
                header(HttpHeaders.Authorization, "Bearer ${employeeToken()}")
            }
            .apply { assertEquals(HttpStatusCode.OK, status) }
    }
}
```

Use JSON parsing for response shape assertions when the project already does:

```kotlin
val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
assertEquals("P-1001", obj["partnerContractNumber"]!!.jsonPrimitive.content)
```

## Auth Test Coverage

For protected routes, include:
- Success with allowed employee token or microservice token
- `401 Unauthorized` without token
- `403 Forbidden` when authenticated but lacking required authority, if helper supports it

Use existing helper methods such as `employeeToken()` and `microserviceToken()`.

## Outbound Client Test Pattern

Use Ktor `MockEngine` and capture `HttpRequestData`:

```kotlin
@Test
fun `listPatients sends default POC bearer token`() = runTest {
    lateinit var request: HttpRequestData
    val httpClient =
        HttpClient(
            MockEngine { capturedRequest ->
                request = capturedRequest
                respond(
                    content = Json.encodeToString(emptyList<PatientListItem>()),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        ) { install(ContentNegotiation) { json() } }

    val client = ServiceApiClient(baseUrl = "http://localhost:5600", httpClient = httpClient)
    client.listPatients(limit = 100)

    assertEquals("Bearer $DEFAULT_POC_EMPLOYEE_TOKEN", request.headers[HttpHeaders.Authorization])
}
```

## Repository Integration Test Pattern

Use `src/testContainerTest` when testing Exposed repositories against real PostgreSQL:

```kotlin
@Testcontainers
class RepositoryIntegrationTest {
    companion object {
        @Container private val postgres = TestDatabaseContainer.instance

        @JvmStatic
        @BeforeAll
        fun setupDatabase() {
            val ds = HikariDataSource(
                HikariConfig().apply {
                    jdbcUrl = postgres.jdbcUrl
                    username = postgres.username
                    password = postgres.password
                    driverClassName = "org.postgresql.Driver"
                    maximumPoolSize = 2
                    isAutoCommit = false
                    transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                }
            )
            Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate()
            Database.connect(ds)
        }
    }

    @BeforeEach
    fun cleanTables() {
        transaction {
            exec("DELETE FROM child_table")
            exec("DELETE FROM parent_table")
        }
    }
}
```

Delete child tables before parent tables. Keep helper factories (`aPatient`, `anImportRun`) private and configurable.

## Scenario Coverage

Derive tests from use case flows:

| Flow | Test examples |
|---|---|
| Main success | endpoint returns expected status/body; service persists expected state |
| Validation failure | invalid ID/body returns `400` |
| Not found | unknown ID returns `404` |
| Auth failure | missing token returns `401`, wrong role returns `403` |
| Idempotency | repeated command returns same or safe result |
| Error mapping | thrown domain exception maps to expected status/text |
| Persistence | repository create/update/find round trip in Testcontainers |

## Workflow

1. Read the use case spec and acceptance scenarios.
2. Read `../_references/service-style.md`.
3. Inspect existing tests in the same module and mirror imports, assertions, auth helpers, and naming.
4. Decide test level: route unit test, application service unit test, outbound client test, or Testcontainers repository integration test.
5. Create small fake implementations for ports used by route/service tests.
6. Cover success, validation, not-found, auth, and key alternative flows.
7. Run LSP diagnostics for touched Kotlin test files.
8. Run focused test command: prefer `mise run test <ClassName>` or `mise run tc-test <ClassName>`; fallback to Gradle module test task.
9. Run `mise run format-check` or project formatting check if available.

## Resources

- `../_references/service-style.md` — canonical testing style
- `templates/ExampleRouteTest.kt` — route test skeleton in current style
- KotlinDocs MCP server — Ktor TestHost and Testcontainers reference
