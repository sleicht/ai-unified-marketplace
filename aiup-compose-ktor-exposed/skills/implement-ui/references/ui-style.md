# Compose Multiplatform UI Style

Prefer existing UI conventions. Use these patterns when the target matches the reference service.

## Discovery

- Discover the owning stack and UI/shared modules from `mise.toml` and the stack's `settings.gradle.kts`.
- Read platform targets, package layout, version catalog, existing API client, runtime configuration, navigation, and auth package before editing.
- Reuse existing convention plugins and catalog aliases; preserve independently buildable service stacks.
- Preserve `commonMain` portability and existing `jvm()` / `wasmJs { browser() }` targets.
- Verify required shared DTOs and backend routes exist. Missing backend prerequisites are a scope blocker to report, not permission to implement the backend.

## Structure

```text
<ui-module>/src/commonMain/kotlin/<base-package>/ui/
├── api/ServiceApiClient.kt             # transport adapter
├── <feature>/<Feature>DataPort.kt       # feature-owned port
├── <feature>/<Feature>Ui.kt             # state/action contract
├── port/FeaturePorts.kt                 # genuinely shared ports
├── screen/App.kt
├── screen/<Feature>Screen.kt
├── util/PlatformUtil.kt
└── viewmodel/<Feature>ViewModel.kt
```

Use shared DTOs; do not duplicate them in the UI module. Keep platform APIs behind `expect`/`actual`.
Keep feature ports free of Ktor and Compose dependencies.

## API Client and Auth

Use a dedicated Ktor client with an injected token source and runtime-derived URL:

```kotlin
class ServiceApiClient(
    baseUrl: String,
    private val accessTokenProvider: AccessTokenProvider,
    val httpClient: HttpClient = createServiceHttpClient(),
) : RecordDataPort {
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

## Ports, ViewModels, and Screens

- Make API clients transport adapters for narrow feature ports.
- Use plain state-holder classes with constructor-injected ports and a caller-provided `CoroutineScope`.
- Use private setters for state mutated only by ViewModel actions.
- Keep orchestration out of composables.
- Give meaningful screen sections immutable `XxxUiState` and `XxxActions` inputs so they can render and forward events without a ViewModel or HTTP client.
- Split screens into small header, content, empty/loading/error, list/detail, and dialog composables.
- Prefer visible text/content descriptions that semantics tests can query.

```kotlin
@Composable
fun App(recordPort: RecordDataPort) {
    val scope = rememberCoroutineScope()
    val recordVm = remember(recordPort, scope) { RecordViewModel(recordPort, scope) }
    MaterialTheme {
        RecordBrowserScreen(recordVm.searchUiState, recordVm.searchActions)
    }
}
```

Construct `ServiceApiClient` at the platform/application entry point where runtime config and the token provider are available; a no-argument default is invalid for the client contract above.

Keep ports and UI contracts in the existing UI module by default. Split a feature into Gradle
`api`/`impl` modules only when measured ownership, change-frequency, dependency, or build-isolation
needs outweigh the extra configuration and build cost.

## Architecture and Quality Gates

Preserve rules that prevent ViewModels from depending on concrete `*ApiClient` classes and keep
ports independent of Ktor/Compose. Run local coverage verification when configured. New coverage
thresholds must come from a measured passing baseline, not a copied percentage.

## Verification

Use the detected command shape: `mise run //<stack>:compile` from monorepo root, `mise run compile` inside the stack, or the owning UI Gradle task. Run existing architecture and coverage gates affected by the change. Use desktop/browser run tasks only when manual rendering is needed.
