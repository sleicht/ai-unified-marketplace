# Backend Testing Style

Prefer existing test conventions. Use these patterns when the target matches the reference Ktor/Exposed service.

## Discovery and Placement

- Discover modules from the owning stack's `settings.gradle.kts`.
- Mirror nearby imports, assertions, auth helpers, fake style, naming, and source sets.
- Put route/application/outbound-client/ArchUnit tests in `src/test`.
- Put deployable-service Koin graph tests in `src/test` beside the composition root package.
- Put PostgreSQL/Flyway repository tests in `src/testContainerTest` when configured.
- Read `ArchitectureTest.kt` before testing new module boundaries.

## Route Tests

Use `testApplication`, fake ports, and real route configuration. Do not start a server or use a database.

```kotlin
@Test
fun `GET records returns list`() = testApplication {
    configureTestApp(recordRepo = fakeRecordRepository())
    client.get("/api/v1/records") {
        header(HttpHeaders.Authorization, "Bearer ${employeeToken()}")
    }.apply { assertEquals(HttpStatusCode.OK, status) }
}
```

- Use a small fake object instead of a mocking library when clearer.
- Install the project's auth test helper when production routes are protected.
- Cover allowed access, missing token (`401`), and insufficient authority (`403`) when supported.
- Parse JSON when response shape matters.

## Outbound Clients

Use Ktor `MockEngine`, capture `HttpRequestData`, and assert URL, method, headers, query/body, and decoding. Inject a deterministic fake token provider; never make a real request.

## Repository Integration

Use Testcontainers, Flyway, the project's datasource configuration, and Exposed `Database.connect`. Apply the real migrations before tests. Clean only owned tables, child before parent. Keep test-data factories private and configurable.

## Architecture Rules

When ArchUnit exists, preserve these directions:

- domain does not depend on application/infrastructure;
- application does not depend on infrastructure;
- cross-module access uses `..api..`.

Do not loosen rules to make implementation pass.

## Dependency Graph Verification

Verify the complete deployable `appModule` with Koin `verify()`. Model runtime-provided types with
`extraTypes` and dynamically supplied constructor parameters with `injectedParameters`. Do not
replace feature-owned modules with duplicate test modules: the purpose is to prove that the real
composition root resolves.

Run this test whenever repository/service/client bindings or `includes(...)` change.

## Commands

- monorepo root: `mise run //<stack>:test <ClassName>`
- inside stack: `mise run test <ClassName>`
- containers: the corresponding `tc-test` task
- no mise task: the owning module's Gradle test task

Run focused tests first, then format/architecture checks relevant to touched files.
