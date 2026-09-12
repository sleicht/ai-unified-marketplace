# Kobweb UI Style

Kobweb is a browser UI choice built on Compose HTML, not a replacement for shared
desktop Compose Multiplatform screens. Keep existing project structure first.

## Canonical Example

Read the [record fixture](../../aiup-implement/references/record-example/README.md):
the shared DTO, client port/state, web app entry, search content and routed pages.
The Compose and Kobweb views use the same client contract. The fixture has no
production identity provider; supply the real application's existing auth flow.

Keep substantial Kotlin examples in compiled source, not duplicated in this file.

## Routing and Presentation

- Pages live in `jsMain` under the discovered pages package. File/package names
  affect routes; preserve public URLs when moving files.
- Use real links and form submission. Bind input labels, meaningful heading
  structure, focus and errors; do not replace semantic elements with clickable divs.
- Keep DOM/CSS and Kobweb/Silk imports out of shared transport ports.
- Use project styles and responsive breakpoints; Material 3 modifiers and Silk
  modifiers are not interchangeable.
- Derive a page's work from route keys and its lifecycle. Cancel obsolete work and
  prevent stale responses from changing the current page.

## Backend and Hosting

Use the existing service, normally through a same-origin gateway or an explicitly
configured API origin. A shared JVM/wasm module needs a compatible JS variant
before Kobweb can consume it; inspect resolved dependencies.

An exported static frontend may call a separate API. Keep CORS, cookie/token
behaviour, callback URLs and the deployed base path consistent. A full-stack
Kobweb host is a separate architecture choice, not a prerequisite.

Export uses a headless browser, not per-request server rendering. Dynamic routes
are not automatically enumerated: configure known public routes or the hosting
fallback as required. Never let frontend fallbacks turn API failures into HTML
successes. Export without user credentials and verify no private data enters assets.

## Verification

Use the actual build's compile, test and export tasks. Open the exported site via
the intended hosting layout and test direct links, reload and back/forward.
Use existing browser tooling, with finite runs. Report unsupported or unrun checks.

Primary references (match APIs to installed versions):
- [Project structure](https://kobweb.varabyte.com/docs/getting-started/kobweb-project)
- [Routing](https://kobweb.varabyte.com/docs/concepts/foundation/routing)
- [Silk](https://kobweb.varabyte.com/docs/concepts/presentation/silk)
- [Exporting](https://kobweb.varabyte.com/docs/concepts/foundation/exporting)
- [Existing backend](https://kobweb.varabyte.com/docs/guides/existing-backend)

