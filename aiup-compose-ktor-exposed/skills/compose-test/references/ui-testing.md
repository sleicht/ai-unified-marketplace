# Compose Multiplatform UI Testing Style

Prefer existing UI test conventions. Use these patterns when the target matches the reference service.

## Test Selection and Placement

Use the lightest level that proves the behaviour:

1. Ktor `MockEngine` API-client test in `commonTest`
2. `runTest` ViewModel test with injected fakes
3. `runComposeUiTest` semantics test only when Compose test dependencies already exist

Place `expect`/`actual` auth and platform behaviour tests in the matching `jvmTest` or `wasmJsTest` source set. Do not use Android-only rules in a multiplatform module.

## API Client Tests

- Inject `HttpClient(MockEngine { ... })` and a deterministic token provider.
- Assert URL, method, bearer header, query/body, and JSON decoding.
- Return non-2xx responses to test the project's error behaviour.
- Never assert an obsolete POC token when the application uses OIDC/PKCE.
- Preserve target-project traceability annotations such as `@UseCase` when present.

```kotlin
val client =
    ServiceApiClient(
        baseUrl = "https://service.invalid",
        accessTokenProvider = FixedAccessTokenProvider("test-token"),
        httpClient = httpClient,
    )
```

## ViewModel Tests

Use `runTest`, pass the test scope, call the action, and use `advanceUntilIdle()` before state assertions. Extract an API interface only when existing code already follows that style or testability requires the production change.

## Semantics Tests

Use `runComposeUiTest` only when configured. Prefer user-visible text and content descriptions. Use test tags only for otherwise inaccessible structure. Use test-clock/wait primitives, never `Thread.sleep()`.

## Auth Tests

Inject fake token providers for common API-client behaviour. Test PKCE generation, callback parsing, token exchange, and browser/desktop adapters only in their platform source sets and only when those components exist.

## Commands

- reference monorepo root: `mise run //<stack>:ui-test <ClassName>`
- inside the reference stack: `mise run ui-test <ClassName>`
- no mise task: the matching UI source-set or `allTests` Gradle task

Run focused tests first, then the project's formatting check.
