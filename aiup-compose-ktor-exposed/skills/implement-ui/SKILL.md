---
name: implement-ui
description: >
  Implements Compose Multiplatform UI for use cases in the reference service
  style: Ktor API client, plain Compose-state ViewModels,
  constructor-injected dependencies, small Material 3 composables, shared DTOs,
  and multiplatform-safe utilities. Use when the user asks to "implement the UI",
  "create a screen", "build the Compose view", "wire the frontend", or mentions
  Compose Multiplatform screens, UI implementation, client-side development, or
  frontend for a use case.
---

# Implement Use Case (UI)

## Instructions

Implement the Compose Multiplatform UI for use case $ARGUMENTS. Follow existing UI conventions first. When the project resembles reference service, use `references/service-style.md` as the canonical style guide.

Use:
- `service-ui`/`*-ui` KMP module or discovered UI module
- Shared `@Serializable` DTOs from the shared module
- Ktor Client in a dedicated API client class
- Plain ViewModel classes with Compose `mutableStateOf`
- `rememberCoroutineScope()` passed into ViewModels
- Small private composables and Material 3 components
- Constructor injection via top-level `App(apiClient: ...)`, not Koin inside composables

Do not create backend code. Use `implement` for backend.
Do not create tests. Use `compose-test` for UI tests and Ktor MockEngine tests.

## Required Reference

Read `references/service-style.md` (absolute path: prepend the "Base directory for this skill:" value from your system context) before editing UI code. Apply its UI API client, ViewModel, screen, and verification conventions.

## DO NOT

- Duplicate DTOs in the UI module
- Use `runBlocking` in composables or ViewModels
- Make composables own long-lived HTTP clients directly
- Use Koin injection inside composables when the existing UI uses constructor injection
- Use platform-specific APIs in `commonMain` without `expect`/`actual`
- Put business orchestration into composables; keep it in ViewModels or API clients
- Create Android-only UI test APIs in a multiplatform UI module

## Target UI Architecture

```text
<ui-module>/src/commonMain/kotlin/<base-package>/ui/
├── api/
│   └── ServiceApiClient.kt              # Ktor Client calls, JSON config, auth header
├── screen/
│   ├── App.kt                       # App root, MaterialTheme, navigation/tabs
│   ├── ErrorBanner.kt               # Reusable error display
│   └── <Feature>Screen.kt           # Screen + private composables
├── util/
│   └── PlatformUtil.kt              # expect/actual only when needed
└── viewmodel/
    └── <Feature>ViewModel.kt        # Plain state holder + coroutine actions
```

## API Client Pattern

```kotlin
private val serviceJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

internal fun HttpClientConfig<*>.installServiceContentNegotiation() {
    install(ContentNegotiation) { json(serviceJson) }
}

internal fun createServiceHttpClient(): HttpClient = HttpClient { installServiceContentNegotiation() }

class ServiceApiClient(
    baseUrl: String = "http://localhost:5600",
    private val bearerToken: String = DEFAULT_POC_EMPLOYEE_TOKEN,
    val httpClient: HttpClient = createServiceHttpClient(),
) {
    private val apiBase = "${baseUrl.trimEnd('/')}/api/v1"

    suspend fun listPatients(limit: Int = 50): List<PatientListItem> =
        httpClient
            .get("$apiBase/patients") {
                bearerAuth(bearerToken)
                parameter("limit", limit)
            }
            .body()
}
```

Keep base URL normalization (`trimEnd('/')`) and endpoint prefix (`/api/v1`) consistent with the backend.

## ViewModel Pattern

```kotlin
class PatientViewModel(
    private val api: ServiceApiClient,
    private val scope: CoroutineScope,
) {
    var patients by mutableStateOf<List<PatientListItem>>(emptyList())
        private set

    var selectedPatient by mutableStateOf<PatientDetail?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var searchQuery by mutableStateOf("")

    fun loadPatients() {
        scope.launch {
            isLoading = true
            error = null
            try {
                patients = api.listPatients(limit = 100)
            } catch (e: Exception) {
                error = "Failed to load patients: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
```

Use private setters for state that only ViewModel actions mutate. Keep user input state public only when simple two-way binding is needed.

## App Wiring Pattern

```kotlin
@Composable
fun App(apiClient: ServiceApiClient = ServiceApiClient()) {
    val scope: CoroutineScope = rememberCoroutineScope()
    val patientVm = remember { PatientViewModel(apiClient, scope) }
    val importVm = remember { ImportViewModel(apiClient, scope) }
    var selectedTab by remember { mutableStateOf(Tab.PATIENTS) }

    MaterialTheme {
        AppScaffold(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            patientContent = { PatientBrowserScreen(patientVm) },
            importContent = { ImportDashboardScreen(importVm) },
        )
    }
}
```

Prefer simple tabs/navigation until the project already has a navigation framework.

## Screen Pattern

```kotlin
@Composable
fun PatientBrowserScreen(vm: PatientViewModel) {
    LaunchedEffect(Unit) { vm.loadPatients() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        PatientBrowserHeader(vm)
        PatientBrowserContent(vm)
    }

    vm.selectedPatient?.let { detail ->
        PatientDetailDialog(detail, onDismiss = { vm.clearSelection() })
    }
}
```

Split large screens into private composables:
- Header/search/filter area
- Loading/empty/error content
- List/grid rows
- Dialog/detail panel
- Small reusable rows such as `DetailRow`

Use visible text and content descriptions that can be tested through semantics.

## Error and Loading UX

Use existing reusable components where present:

```kotlin
ErrorBanner(message = vm.error, context = "Patient Browser")
```

Show loading only when there is no existing content, unless the use case requires blocking refresh.

## Workflow

1. Read the use case spec from `docs/use_cases/`.
2. Verify backend DTOs/routes exist in shared/server modules; if missing, run backend implementation first.
3. Read `references/service-style.md` (absolute path: prepend the "Base directory for this skill:" value from your system context).
4. Inspect existing UI module for package names, screen structure, API client style, and platform targets.
5. Add or extend shared DTO usage; do not duplicate DTOs.
6. Add API client methods in the existing client class.
7. Add or extend a ViewModel with Compose state and coroutine actions.
8. Add or extend screens using small private composables and Material 3.
9. Wire the screen into `App.kt` or existing navigation/tabs.
10. Run LSP diagnostics for touched Kotlin files.
11. Verify with project command: prefer `mise run compile`, `mise run run-admin-ui` for manual UI, or UI module Gradle tasks as fallback.

## Resources

- `references/service-style.md` — canonical UI style
