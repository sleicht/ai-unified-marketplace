# Record example

This is the canonical, dependency-backed example for the construction skills.
Read the files for the layer being changed; do not copy a whole application
or impose this fixture's modules and versions on a target service.

The fixture demonstrates a read/search UI and a route-only detail page. It is
not an authentication product or a complete CRUD application. The backend also
provides create/update repository methods and atomic pair creation to demonstrate
persistence and rollback. Client and backend use one serialisable DTO.

## Contract and source map

| Concern | Canonical files |
|---|---|
| DTO and wire compatibility | [RecordListItem](shared/src/commonMain/kotlin/example/shared/RecordListItem.kt), [contract test](shared/src/commonTest/kotlin/example/shared/RecordContractTest.kt) |
| Domain and repository port | [Record](server/src/main/kotlin/example/server/modules/record/domain/model/Record.kt), [RecordRepository](server/src/main/kotlin/example/server/modules/record/domain/repository/RecordRepository.kt) |
| Schema and audit upgrade | [V001](server/src/main/resources/db/migration/V001__create_record.sql), [V002](server/src/main/resources/db/migration/V002__add_record_audit.sql) |
| Exposed and JDBC execution | [persistence](server/src/main/kotlin/example/server/modules/record/infrastructure/persistence/RecordPersistence.kt) |
| Atomic application operation | [RecordService](server/src/main/kotlin/example/server/modules/record/application/RecordService.kt) |
| Routes and auth seam | [routes](server/src/main/kotlin/example/server/modules/record/infrastructure/rest/RecordRoutes.kt), [route tests](server/src/test/kotlin/example/server/RecordRoutesTest.kt) |
| Feature DI and root | [feature bindings](server/src/main/kotlin/example/server/modules/record/DependencyInjection.kt), [appModule](server/src/main/kotlin/example/server/di/DependencyInjection.kt), [graph test](server/src/test/kotlin/example/server/DependencyInjectionTest.kt) |
| Database proof | [PostgreSQL migration/round-trip/rollback test](server/src/test/kotlin/example/server/RecordRepositoryTest.kt) |
| Client port and adapter | [ports](client/src/commonMain/kotlin/example/client/RecordDataPort.kt), [HTTP client](client/src/commonMain/kotlin/example/client/ServiceApiClient.kt), [MockEngine tests](client/src/commonTest/kotlin/example/client/ServiceApiClientTest.kt) |
| Shared UI state | [state/actions/ViewModel](client/src/commonMain/kotlin/example/client/RecordViewModel.kt), [state tests](client/src/commonTest/kotlin/example/client/RecordViewModelTest.kt) |
| Compose presentation | [screen and app wiring](client/src/jvmMain/kotlin/example/client/RecordBrowserScreen.kt) |
| Kobweb presentation | [app entry](web/src/jsMain/kotlin/example/web/AppEntry.kt), [page](web/src/jsMain/kotlin/example/web/pages/Index.kt), [search content](web/src/jsMain/kotlin/example/web/RecordSearchContent.kt), [route parameter example](web/src/jsMain/kotlin/example/web/pages/records/Record.kt) |
| Browser proof | [exported-site browser test](server/src/test/kotlin/example/server/RecordBrowserTest.kt) |

IDs are positive database-generated Longs. References/category/display names
are required non-blank strings with lengths 50/50/100. SQL checks exercise null,
empty and whitespace boundaries. Audit timestamps are database generated and
mapped as explicit java.time.OffsetDateTime values.

Search is a literal, case-sensitive substring of displayName, ordered by ID,
with a limit from 1 through 100. Search uses latest-submitted-request-wins:
stale completions cannot change records, errors or loading state.

## Run

Run from this directory with the wrapper (JDK 25 used for this fixture):

```sh
./gradlew spotlessCheck :shared:jvmTest :client:jvmTest :server:test :web:compileKotlinJs
./gradlew :server:postgresTest
```

The second command needs Docker. It creates a disposable PostgreSQL database,
migrates an empty schema and upgrades a populated V001 schema, verifies all-field round trips, constraints,
audit columns and rollback, then closes its datasource and container.
For a shared test database, track inserted IDs and delete only owned rows instead.

Browser verification uses a separate explicit task; compilation is not a browser pass:

```sh
./gradlew :server:installBrowser
./gradlew :web:kobwebExport -PkobwebReuseServer=false -PkobwebEnv=DEV -PkobwebRunLayout=FULLSTACK -PkobwebBuildTarget=RELEASE -PkobwebExportLayout=STATIC
./gradlew :server:browserTest
./gradlew :web:kobwebStop
```

The browser test serves the actual exported files on an ephemeral loopback port
and intercepts API requests with deterministic responses. It proves DOM/client
wiring, not the live backend or identity provider. Real OIDC integration and a
full accessibility audit remain application-specific checks. The fixture's web
app is anonymous; production consumers must inject their existing token provider.
The backend accepts a verifier supplied by its host; fixed token comparisons
appear only in route tests.

## Maintenance

Dependencies are a fixture compatibility baseline, not recommended target-project
upgrades: Kotlin 2.3.10, Kobweb 0.24.0, Compose HTML/Multiplatform 1.10.0,
Compose runtime 1.10.2, Ktor 3.4.0 and Exposed 1.1.1.
The Kobweb/Kotlin pairing follows the
[upstream 0.24.0 catalog](https://github.com/varabyte/kobweb/blob/v0.24.0/gradle/libs.versions.toml).
The Gradle 9.6.1 wrapper was generated from the installed official distribution.
Other dependency versions are pinned in the module build scripts.

Run the owning wrapper's dependency reports before changing versions. Use
`spotlessApply` then rerun affected checks. Never accept a broken example by
weakening its tests. Keep examples in these source files; skills link here rather
than maintaining parallel snippets. Distribute the full construction plugin so
cross-skill relative references remain resolvable.

## Verified baseline — 12 September 2026

| Check | Result |
|---|---|
| Spotless and shared/client/server JVM tests; Kobweb JS compilation | Passed |
| PostgreSQL empty migration, populated upgrade, constraints and atomic rollback | Passed with Docker |
| Kobweb production static export | Passed |
| Exported-site Chromium search, errors/retry, focus, narrow layout, deep links and fallback | Passed |
| New skill metadata, reference links and shell syntax | Passed |
| Semgrep on fixture source directories | No findings |

These are fixture results, not evidence for a consuming application. Export emits
upstream Gradle deprecation and webpack bundle-size warnings. Browser tests use
controlled API responses; real authentication and complete accessibility remain
application-specific work.
