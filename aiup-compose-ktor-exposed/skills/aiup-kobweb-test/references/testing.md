# Kobweb Test Examples

Use the [record fixture](../../aiup-implement/references/record-example/README.md)
as one consistent source of production contracts and tests. Its JVM browser
harness is an example choice, not a requirement for applications already using
a TypeScript runner.

## Examples to Adapt

- `RecordViewModelTest`: fake the real port, inspect loading before completion,
  submit different queries, complete obsolete work, preserve content on failure,
  retry and cancel the screen scope.
- `ServiceApiClientTest`: run the production client configuration with MockEngine;
  assert URL/method/query/token/DTOs, non-2xx list-shaped bodies and malformed JSON.
- `RecordBrowserTest`: serve the exported frontend, stub API requests at the
  browser boundary, submit a labelled form using Enter and assert request plus
  visible results. Exercise errors/retry, direct routes, reload and history.

Use each test's full setup and teardown. Keep success and error fixture JSON in
agreement with the shared DTO and API contract. A mocked browser test verifies UI
wiring, not the real service or identity provider; name that boundary accurately.

## Auth and Accessibility

Use a fake provider or existing test auth seam. Cover missing/expired sessions and
the app's visible 401/403 response behaviour. Test actual OIDC callbacks only in
a controlled integration suite when requested; do not fabricate a production auth
implementation to satisfy a browser test.

Use roles, labels, keyboard submission and focus checks. Include narrow viewport
behaviour and meaningful manual keyboard checks when automation cannot establish
it. Do not infer accessibility from HTML rendering or a single audit score.

## Export and Runner

Discover the project's configured runner before adding one. Keep the browser
version compatible with it. Do not reuse Kobweb's internal Playwright installation
as proof that application tests are configured.

Export an anonymous shell without private API calls. Use the chosen production
layout; test direct routes, assets and base paths. Do not swallow API 404/500
responses through the frontend fallback. Close every resource the harness owns.

[Playwright locators](https://playwright.dev/docs/locators) explain role/label
selection; [Kobweb exporting](https://kobweb.varabyte.com/docs/concepts/foundation/exporting)
describes the distinct export lifecycle.

