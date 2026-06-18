---
name: reference
description: >
  Creates or updates a project REFERENCE.md for AI agents and developers: repository layout, core commands, documentation map,
  conventions, domain vocabulary, architecture pointers, testing notes, and operational caveats. Use when the user asks to
  "create REFERENCE.md", "update REFERENCE.md", "document repo context", "write an agent reference", "capture project
  conventions", "summarise project structure", or wants a concise project reference file for future AI-assisted work.
---

# Project Reference Documentation

## Instructions

Create or update `docs/REFERENCE.md` for $ARGUMENTS. Resolve the output path first: if a service/module is in scope or cwd is inside a monorepo service, write `<service>/docs/REFERENCE.md`; otherwise write `<root>/docs/REFERENCE.md`.

`REFERENCE.md` is a durable project reference for future AI agents and maintainers. It should compress stable facts that reduce rediscovery: where things live, how to run checks, what terms mean, which docs are authoritative, and what conventions the codebase follows.

When updating an existing file, preserve useful project-specific content and refresh stale sections. Prefer concise bullets and tables over long prose.

## Path and Scope Resolution

- Detect monorepo services from `mise.toml` (`monorepo_root` or namespaced tasks), multiple sibling `settings.gradle.kts` builds, or existing service-local `docs/` directories.
- If the user names a service, module, or directory, scope the reference to that target.
- If no service is named and the repository is a single project, write `<root>/docs/REFERENCE.md`.
- If both root-level and service-level `docs/REFERENCE.md` files exist, update the one matching the current request and link to the other where helpful.

## Required Sections

Use these sections unless an existing `REFERENCE.md` already has equivalent headings:

1. Purpose
2. Repository Layout
3. Authoritative Documentation
4. Build, Test, and Verification Commands
5. Architecture and Module Boundaries
6. Domain Vocabulary
7. Data Model and Persistence
8. APIs, Integrations, and External Systems
9. Testing Strategy
10. Coding and Documentation Conventions
11. Operational Notes
12. Known Gaps or Follow-Ups

Keep sections empty only when the information is genuinely not discoverable; in that case write a short `Not documented yet` note rather than inventing facts.

## Discovery Sources

Read only what is needed to ground the reference:

- Existing `REFERENCE.md`, `README.md`, `CLAUDE.md`, and files under `docs/`
- AIUP artifacts: `docs/requirements.html`, `docs/entity_model.md`, `docs/use_cases/`, `docs/architecture.html`
- Build and task files: `mise.toml`, `Justfile`, `Makefile`, Gradle/Maven/npm config, CI files
- Source tree layout and package/module names
- Existing tests and architecture tests
- Deployment/config examples when they are already in the repository

Prefer observed facts over inferred intent. Mark uncertain items as `To confirm`.

## Content Rules

- Keep the file concise enough to be loaded by future agents without drowning them in detail.
- Link to canonical docs instead of copying large sections.
- Capture commands exactly as the repository defines them; do not invent preferred commands.
- Document project vocabulary as a glossary: term, meaning, and source when useful.
- Separate stable conventions from temporary workarounds.
- Preserve language used by existing docs; default to English for new files unless the repository is clearly documented in another language.

## DO NOT

- Do not duplicate full requirements, use case specs, entity models, or architecture pages.
- Do not add brand styling, HTML scaffolding, or generated diagrams.
- Do not include secrets, credentials, local-only tokens, or private machine paths.
- Do not include speculative roadmap items unless they are already documented.
- Do not use PlantUML; reference existing Mermaid docs when diagrams are relevant.
- Do not replace project-specific terminology with generic words when documenting a real project; only examples in the skill itself should stay generic.

## Update Strategy

When `docs/REFERENCE.md` or `<service>/docs/REFERENCE.md` exists:

1. Read it first and identify stale, missing, and still-valid sections.
2. Refresh facts from current repository files.
3. Preserve user-authored decisions and warnings unless contradicted by the repository.
4. Remove duplicated or obsolete content that your update makes wrong.
5. Keep heading names stable where downstream tooling or humans may rely on them.

When creating a new file:

1. Build the shortest useful reference from current evidence.
2. Include links to the AIUP artifacts that already exist.
3. Leave explicit `Not documented yet` notes for missing but important areas.

## Workflow

1. Resolve the target project/service and output path.
2. Read any existing `docs/REFERENCE.md` or scoped `<service>/docs/REFERENCE.md`.
3. Read the minimal set of docs and build files needed for grounded facts.
4. Inspect the source tree structure without deep-reading unrelated implementation files.
5. Draft or update the required sections.
6. Verify every command, path, and artifact reference exists or is marked `To confirm`.
7. Search the new/updated file for accidental secrets, machine-local paths, and stale references.
8. Report what changed and any gaps left as `Not documented yet`.

## Output Contract

The resulting `docs/REFERENCE.md` should let a future agent answer quickly:

- What is this project or service?
- Where are the important files and docs?
- Which commands should be used for build, test, lint, and local runs?
- What architecture and module boundaries matter?
- Which domain terms have precise meanings here?
- Which integrations, persistence layers, and operational caveats must not be missed?
