# Compose Multiplatform UI Style

Prefer existing UI conventions. Use these patterns when the target matches the reference service.

## Discovery

- Discover the owning stack and UI/shared modules from `mise.toml` and the stack's `settings.gradle.kts`.
- Read platform targets, package layout, version catalog, existing API client, runtime configuration, navigation, and auth package before editing.
- Preserve `commonMain` portability and existing `jvm()` / `wasmJs { browser() }` targets.
- Verify required shared DTOs and backend routes exist. Missing backend prerequisites are a scope blocker to report, not permission to implement the backend.

## Structure

```text
<ui-module>/src/commonMain/kotlin/<base-package>/ui/
├── api/ServiceApiClient.kt
├── screen/App.kt
├── screen/<Feature>Screen.kt
├── util/PlatformUtil.kt
└── viewmodel/<Feature>ViewModel.kt
```

Use shared DTOs; do not duplicate them in the UI module. Keep platform APIs behind `expect`/`actual`.

## API Client and Auth

Use a dedicated Ktor client with an injected token source and runtime-derived URL:

```kotlin
class ServiceApiClient(
    baseUrl: String,
    private val accessTokenProvider: AccessTokenProvider,
    val httpClient: HttpClient = createServiceHttpClient(),
) {
    private val apiBase = "${baseUrl.trimEnd('/')}/api/v1"

    suspend fun listRecords(limit: Int = 50): List<RecordListItem> =
        httpClient.get("$apiBase/records") {
            authorization()
            parameter("limit", limit)
        }.body()

    private suspend fun HttpRequestBuilder.authorization() {
        val accessToken = accessTokenProvider.currentAccessToken()
        if (accessToken != null) bearerAuth(accessToken)
    }
}
```

- Reuse existing OIDC/PKCE, token-provider, and runtime-config boundaries.
- If no auth stack exists, preserve the project's existing simple token pattern; do not introduce OIDC unasked.
- Never hardcode localhost when runtime/environment configuration exists.

## ViewModels and Screens

- Use plain state-holder classes with constructor-injected API dependencies and a caller-provided `CoroutineScope`.
- Use private setters for state mutated only by ViewModel actions.
- Keep orchestration out of composables.
- Split screens into small header, content, empty/loading/error, list/detail, and dialog composables.
- Prefer visible text/content descriptions that semantics tests can query.

```kotlin
@Composable
fun App(apiClient: ServiceApiClient) {
    val scope = rememberCoroutineScope()
    val recordVm = remember(apiClient, scope) { RecordViewModel(apiClient, scope) }
    MaterialTheme { RecordBrowserScreen(recordVm) }
}
```

Construct `ServiceApiClient` at the platform/application entry point where runtime config and the token provider are available; a no-argument default is invalid for the client contract above.

## Verification

Use the detected command shape: `mise run //<stack>:compile` from monorepo root, `mise run compile` inside the stack, or the owning UI Gradle task. Use desktop/browser run tasks only when manual rendering is needed.
