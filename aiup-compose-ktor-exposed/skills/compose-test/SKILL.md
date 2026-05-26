---
name: compose-test
description: >
  Creates UI-side tests for Compose/Ktor client code in the reference service
  style: commonTest Ktor MockEngine API-client tests,
  coroutine runTest ViewModel tests, and Compose Multiplatform semantics tests
  when screen-test dependencies exist. Use when the user asks to "write Compose
  tests", "test the UI", "create screen tests", "unit test a Compose screen",
  or mentions Compose testing, UI testing, MockEngine, ViewModel testing, or
  runComposeUiTest.
---

# Compose Test

## Instructions

Create UI-side tests for use case $ARGUMENTS. Follow the target project's existing UI test style first. When the project resembles reference service, use `../_references/service-style.md` as the canonical style guide.

Prefer this order:
1. API client tests with Ktor `MockEngine` in `commonTest`
2. ViewModel tests with fake API clients and `runTest`
3. Compose semantics tests with `runComposeUiTest` only when the project has Compose UI test dependencies configured

## Required Reference

Read `../_references/service-style.md` before writing tests. Apply its UI API client, ViewModel, and commonTest conventions.

## DO NOT

- Make real network calls
- Use Android-only `createComposeRule()` in a multiplatform module
- Use `Thread.sleep()` or fixed delays
- Test implementation details hidden from user semantics
- Add Compose UI test dependencies unless the user asked for screen tests or existing project already has them
- Use Koin in tests when UI code uses constructor injection
- Duplicate shared DTOs inside tests

## API Client Test Pattern

Use Ktor `MockEngine` to verify URL, method, headers, query params, body, and JSON decoding:

```kotlin
class ServiceApiClientTest {

    @Test
    fun `listPatients sends default POC bearer token`() = runTest {
        lateinit var request: HttpRequestData
        val httpClient =
            HttpClient(
                MockEngine { capturedRequest ->
                    request = capturedRequest
                    respond(
                        content =
                            Json.encodeToString(
                                listOf(
                                    PatientListItem(
                                        id = 1,
                                        partnerContractNumber = "P-1001",
                                        firstName = "Ada",
                                        lastName = "Lovelace",
                                        dateOfBirth = "1815-12-10",
                                        gender = "FEMALE",
                                        active = true,
                                    )
                                )
                            ),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            ) { install(ContentNegotiation) { json() } }

        val client = ServiceApiClient(baseUrl = "http://localhost:5600", httpClient = httpClient)

        val patients = client.listPatients(limit = 100)

        assertEquals("Bearer $DEFAULT_POC_EMPLOYEE_TOKEN", request.headers[HttpHeaders.Authorization])
        assertEquals("/api/v1/patients", request.url.encodedPath)
        assertEquals("100", request.url.parameters["limit"])
        assertEquals(1, patients.size)
    }
}
```

Test error handling by returning non-2xx responses from `MockEngine` and asserting the client or ViewModel behavior expected by the project.

## ViewModel Test Pattern

If the API client is concrete and not interface-based, prefer extracting a small interface only if existing code already uses that style or the change is needed for testability. Otherwise, test API client behavior directly.

When a fake can be injected:

```kotlin
class PatientViewModelTest {
    @Test
    fun `loadPatients stores patients`() = runTest {
        val api = FakeServiceApiClient(patients = listOf(aPatientListItem()))
        val vm = PatientViewModel(api, this)

        vm.loadPatients()
        testScheduler.advanceUntilIdle()

        assertEquals(1, vm.patients.size)
        assertEquals(null, vm.error)
    }
}
```

Use `kotlinx.coroutines.test.runTest` and `advanceUntilIdle()` for coroutine-driven state changes.

## Compose Semantics Test Pattern

Use only when Compose UI testing dependencies exist in `commonTest`:

```kotlin
@OptIn(ExperimentalTestApi::class)
class PatientBrowserScreenTest {

    @Test
    fun `screen displays patients`() = runComposeUiTest {
        val vm = PatientViewModel(fakeApiWithPatients(), backgroundScope)
        vm.loadPatients()

        setContent { PatientBrowserScreen(vm) }

        waitUntil(timeoutMillis = 5_000) {
            onAllNodesWithText("Muster", substring = true).fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithText("Patient Browser").assertIsDisplayed()
        onNodeWithText("Muster, Max").assertIsDisplayed()
    }
}
```

Prefer user-visible text and content descriptions. Use test tags only for structural elements with no accessible text.

## Common Assertions

| Target | Assertion style |
|---|---|
| API auth header | `assertEquals("Bearer ...", request.headers[HttpHeaders.Authorization])` |
| API path | `assertEquals("/api/v1/patients", request.url.encodedPath)` |
| Query param | `assertEquals("100", request.url.parameters["limit"])` |
| ViewModel state | `assertEquals(expected, vm.patients)` |
| Error state | `assertTrue(vm.error!!.contains("Failed"))` |
| Screen text | `onNodeWithText("...").assertIsDisplayed()` |
| Async UI | `waitUntil(timeoutMillis = 5_000) { ... }` |

## Scenario Coverage

Derive UI tests from use case behavior:

| Use case need | Preferred test |
|---|---|
| API call shape | MockEngine API client test |
| JSON serialization | MockEngine response/body test |
| Loading state | ViewModel coroutine test |
| Error message | ViewModel fake failure test |
| Search/filter | ViewModel pure state test or screen semantics test |
| Button invokes action | Screen semantics test if available |
| Navigation/tab selection | Screen semantics test or extracted state test |

## Workflow

1. Read the use case spec and UI implementation.
2. Read `../_references/service-style.md`.
3. Inspect `service-ui/src/commonTest` or equivalent to identify existing dependencies and assertion style.
4. Choose the lightest useful test level: API client, ViewModel, or Compose screen.
5. Use Ktor `MockEngine` for API client behavior and no network.
6. Use fake clients/repositories for ViewModel and screen tests.
7. Use `runTest` and `advanceUntilIdle()` for coroutine state.
8. Use `runComposeUiTest` and semantics only when dependencies exist.
9. Run LSP diagnostics for touched Kotlin test files.
10. Run focused UI test command: prefer `mise run test <ClassName>` if wired to UI tests; fallback to `./gradlew <ui-module>:allTests` or the existing module test task.

## Resources

- `../_references/service-style.md` — canonical UI testing style
- `templates/ExampleScreenTest.kt` — UI-side API client test skeleton using Ktor MockEngine
- KotlinDocs MCP server — Compose testing, Ktor MockEngine, kotlinx.coroutines.test reference
