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

Create backend tests for use case $ARGUMENTS. Follow the target project's existing test style first. When the project resembles reference service, use `references/service-style.md` as the canonical style guide.

Use:
- Ktor `testApplication {}` for route tests
- Fake repository/service implementations for route unit tests
- Company `IntegrationTestHelper` when auth is enabled in routes
- `kotlin.test` assertions if existing tests use them
- Ktor `MockEngine` for outbound HTTP clients
- Testcontainers + Flyway for repository integration tests
- ArchUnit `ArchitectureTest.kt` updates when adding or changing modules

Do not start a real HTTP server for route tests.
Do not use a real database for route unit tests.

## Required Reference

Read `references/service-style.md` (absolute path: prepend the "Base directory for this skill:" value from your system context) before writing tests. Apply its route-test, fake-dependency, auth-token, Testcontainers, ArchUnit, source-set, and command conventions.

## DO NOT

- Use Mockito/MockK for repositories when a small fake object is clearer
- Bypass route authentication unless the route is intentionally public
- Hardcode server ports in route tests
- Use `runBlocking` inside `testApplication` route tests
- Use Kotest assertions in projects that use `kotlin.test`
- Delete all shared data in cleanup outside tables owned by the test
- Put Testcontainers tests in `src/test` when the project has `src/testContainerTest`
- Put ArchUnit tests outside the existing architecture-test location/style

## Route Test Pattern

```kotlin
class RecordRoutesTest {

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

    private val sampleRecord = Record(
        id = 1L,
        externalReference = "REC-1001",
        category = "Standard",
        displayName = "Example",
        status = RecordStatus.ACTIVE,
    )

    private fun fakeRecordRepository(records: List<Record> = listOf(sampleRecord)) =
        object : RecordRepository {
            override suspend fun create(record: Record) = record
            override suspend fun update(record: Record) = record
            override suspend fun findById(id: Long) = records.find { it.id == id }
            override suspend fun findAll(limit: Int) = records.take(limit)
        }

    private fun ApplicationTestBuilder.configureTestApp(
        recordRepo: RecordRepository = fakeRecordRepository(),
    ) {
        helper.installAuth(this)
        install(Koin) { modules(module { single<RecordRepository> { recordRepo } }) }
        application {
            configureSerialization()
            configureRouting()
        }
    }

    private fun employeeToken() =
        helper.employeeToken(cNumber = "C123456", roles = listOf("DL_COMPANY_APP_ADMIN"))

    @Test
    fun `GET records returns list`() = testApplication {
        configureTestApp()
        client
            .get("/api/v1/records") {
                header(HttpHeaders.Authorization, "Bearer ${employeeToken()}")
            }
            .apply { assertEquals(HttpStatusCode.OK, status) }
    }
}
```

Use JSON parsing for response shape assertions when the project already does:

```kotlin
val obj = Json.parseToJsonElement(bodyAsText()).jsonObject
assertEquals("REC-1001", obj["externalReference"]!!.jsonPrimitive.content)
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
fun `listRecords sends default POC bearer token`() = runTest {
    lateinit var request: HttpRequestData
    val httpClient =
        HttpClient(
            MockEngine { capturedRequest ->
                request = capturedRequest
                respond(
                    content = Json.encodeToString(emptyList<RecordListItem>()),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        ) { install(ContentNegotiation) { json() } }

    val client = ServiceApiClient(baseUrl = testBaseUrl, accessTokenProvider = FakeAccessTokenProvider("test-token"), httpClient = httpClient)
    client.listRecords(limit = 100)

    assertEquals("Bearer test-token", request.headers[HttpHeaders.Authorization])
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

Delete child tables before parent tables. Keep helper factories (`aRecord`, `anImportRun`) private and configurable.

## Architecture Test Pattern

When the backend project uses ArchUnit, create or extend `ArchitectureTest.kt` for new modules and boundaries. Mirror the existing rule style and keep rules generic to the module layout.

Reference rules to preserve:

- domain packages do not depend on application or infrastructure packages
- application packages do not depend on infrastructure packages
- cross-module dependencies go through `..api..` packages only

Run the focused `ArchitectureTest` after backend architecture changes. Do not loosen existing rules to make a new implementation pass; fix the dependency direction instead.

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

1. Read the use case spec and acceptance scenarios from the resolved docs path.
2. Read `references/service-style.md` (absolute path: prepend the "Base directory for this skill:" value from your system context).
3. Inspect existing tests in the same module and mirror imports, assertions, auth helpers, source-set placement, and naming.
4. Inspect `ArchitectureTest.kt` when present; extend it for new modules or boundaries.
5. Decide test level: route unit test, application service unit test, outbound client test, ArchUnit rule, or Testcontainers repository integration test.
6. Place route/unit/ArchUnit tests in `src/test`; place PostgreSQL/Flyway repository tests in `src/testContainerTest` when that suite exists.
7. Create small fake implementations for ports used by route/service tests.
8. Cover success, validation, not-found, auth, and key alternative flows.
9. Run LSP diagnostics for touched Kotlin test files.
10. Run focused test command using detected shape: `mise run //<stack>:test <ClassName>` or bare `mise run test <ClassName>`; for Testcontainers use namespaced/bare `tc-test`. Fallback to Gradle module test tasks.
11. Run `ArchitectureTest` when architecture rules changed.
12. Run `mise run format-check` or project formatting check if available.

## Resources

- `references/service-style.md` — canonical testing style
- `references/ExampleRouteTest.kt` — route test skeleton in current style
