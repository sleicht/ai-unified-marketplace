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

Create UI-side tests for the use case named or implied by the user's request. Follow the target project's existing UI test style first. When the project resembles the reference service, use `references/ui-testing.md` as the canonical style guide.

Prefer this order:
1. API client tests with Ktor `MockEngine` in `commonTest`
2. ViewModel tests with fake API clients and `runTest`
3. Compose semantics tests with `runComposeUiTest` only when the project has Compose UI test dependencies configured

When UI code uses OIDC/PKCE or platform `expect`/`actual` auth, place tests in the matching source set (`commonTest`, `jvmTest`, or `wasmJsTest`) and use deterministic fake token providers.

## Required Reference

Read `references/ui-testing.md`, resolved relative to this `SKILL.md`, before writing tests. Apply its UI API client, ViewModel, commonTest, platform source-set, OIDC/PKCE, and command conventions.

## DO NOT

- Make real network calls
- Use Android-only `createComposeRule()` in a multiplatform module
- Use `Thread.sleep()` or fixed delays
- Test implementation details hidden from user semantics
- Add Compose UI test dependencies unless the user asked for screen tests or existing project already has them
- Use Koin in tests when UI code uses constructor injection
- Duplicate shared DTOs inside tests
- Put platform-specific auth tests in `commonTest` when the implementation lives in `jvmMain` or `wasmJsMain`
- Assert POC bearer-token headers when the project uses an OIDC token provider

## API Client Test Pattern

Use Ktor `MockEngine` to verify URL, method, headers, query params, body, and JSON decoding:

```kotlin
class ServiceApiClientTest {

    private class FixedAccessTokenProvider(private val token: String?) : AccessTokenProvider {
        override suspend fun currentAccessToken(): String? = token
    }

    @Test
    fun `listRecords sends provided bearer token`() = runTest {
        lateinit var request: HttpRequestData
        val httpClient =
            HttpClient(
                MockEngine { capturedRequest ->
                    request = capturedRequest
                    respond(
                        content =
                            Json.encodeToString(
                                listOf(
                                    RecordListItem(
                                        id = 1,
                                        externalReference = "REC-1001",
                                        displayName = "Example",
                                        category = "Record",
                                        status = "ACTIVE",
                                        active = true,
                                    )
                                )
                            ),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            ) { install(ContentNegotiation) { json() } }

        val client = ServiceApiClient(baseUrl = testBaseUrl, accessTokenProvider = FakeAccessTokenProvider("test-token"), httpClient = httpClient)

        val records = client.listRecords(limit = 100)

        assertEquals("Bearer test-token", request.headers[HttpHeaders.Authorization])
        assertEquals("/api/v1/records", request.url.encodedPath)
        assertEquals("100", request.url.parameters["limit"])
        assertEquals(1, records.size)
    }
}
```

Test error handling by returning non-2xx responses from `MockEngine` and asserting the client or ViewModel behavior expected by the project.

For OIDC/PKCE-backed clients, inject a fake `AccessTokenProvider` or equivalent and assert the resolved bearer token and request shape. Test PKCE generation, callback parsing, token exchange, and browser/desktop adapters in platform source sets only when those components exist.

When the target project uses test traceability annotations such as `@UseCase`, preserve them and populate IDs, scenarios, and business rules from the corresponding specification.

## ViewModel Test Pattern

If the API client is concrete and not interface-based, prefer extracting a small interface only if existing code already uses that style or the change is needed for testability. Otherwise, test API client behavior directly.

When a fake can be injected:

```kotlin
class RecordViewModelTest {
    @Test
    fun `loadRecords stores records`() = runTest {
        val api = FakeServiceApiClient(records = listOf(aRecordListItem()))
        val vm = RecordViewModel(api, this)

        vm.loadRecords()
        testScheduler.advanceUntilIdle()

        assertEquals(1, vm.records.size)
        assertEquals(null, vm.error)
    }
}
```

Use `kotlinx.coroutines.test.runTest` and `advanceUntilIdle()` for coroutine-driven state changes.

## Compose Semantics Test Pattern

Use only when Compose UI testing dependencies exist in `commonTest`:

```kotlin
@OptIn(ExperimentalTestApi::class)
class RecordBrowserScreenTest {

    @Test
    fun `screen displays records`() = runComposeUiTest {
        val vm = RecordViewModel(fakeApiWithRecords(), backgroundScope)
        vm.loadRecords()

        setContent { RecordBrowserScreen(vm) }

        waitUntil(timeoutMillis = 5_000) {
            onAllNodesWithText("Standard", substring = true).fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithText("Record Browser").assertIsDisplayed()
        onNodeWithText("Example Record").assertIsDisplayed()
    }
}
```

Prefer user-visible text and content descriptions. Use test tags only for structural elements with no accessible text.

## Common Assertions

| Target | Assertion style |
|---|---|
| API auth header | `assertEquals("Bearer ...", request.headers[HttpHeaders.Authorization])` |
| API path | `assertEquals("/api/v1/records", request.url.encodedPath)` |
| Query param | `assertEquals("100", request.url.parameters["limit"])` |
| ViewModel state | `assertEquals(expected, vm.records)` |
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

1. Read the use case spec and UI implementation from the resolved docs path/module.
2. Read `references/ui-testing.md`.
3. Inspect `service-ui/src/commonTest`, `src/jvmTest`, and `src/wasmJsTest` or equivalents to identify dependencies and assertion style.
4. Inspect existing API-client auth style: OIDC/PKCE token provider, POC bearer token, or no auth.
5. Choose the lightest useful test level: API client, ViewModel, platform auth helper, or Compose screen.
6. Use Ktor `MockEngine` for API client behavior and no network.
7. Use fake token providers/clients/repositories for ViewModel and screen tests.
8. Use `runTest` and `advanceUntilIdle()` for coroutine state.
9. Use `runComposeUiTest` and semantics only when dependencies exist.
10. Put `expect`/`actual` platform behaviour tests in `jvmTest`/`wasmJsTest` when relevant.
11. If language-server diagnostics are available, run them for touched Kotlin test files.
12. Run the detected focused UI test task. In the reference monorepo use `mise run //<stack>:ui-test <ClassName>` from the root or `mise run ui-test <ClassName>` inside the stack; otherwise use the matching UI source-set or `allTests` Gradle task.

## Resources

- `references/ui-testing.md` — focused UI testing style
- `references/ExampleScreenTest.kt` — UI-side API client test skeleton using Ktor MockEngine
