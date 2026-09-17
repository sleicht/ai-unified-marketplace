<!--
Copyright 2025-2026 Simon Martinelli and the AI Unified Process contributors.
Part of the AI Unified Process — https://unifiedprocess.ai
Licensed under the Apache License, Version 2.0. See LICENSE and NOTICE.
-->

# AI Unified Process Marketplace

AI Unified Process is a requirements-first workflow for taking software from a product vision to reviewed
specifications, implementation, and traceable tests. This fork adapts the [upstream marketplace](https://github.com/AI-Unified-Process/marketplace) for the Sanitas
Kotlin Multiplatform stack, with Claude Code plugins and portable Agent Skills for other coding agents.

[Get started](#quick-start) · [Understand the workflow](docs/how-to-use.md) ·
[Choose a plugin](#choose-your-plugins) · [Installation guides](#installation)

## Why AI Unified Process?

AI-assisted development often jumps from a vague prompt directly to code. AI Unified Process inserts durable,
human-reviewable artefacts between intent and implementation:

- requirements with stable identifiers;
- an explicit domain entity model;
- use cases that define user goals and behaviour;
- tests that trace back to those use cases;
- stack-specific implementation and tests built from the reviewed specifications.

The workflow is inspired by the phases of the
[Rational Unified Process](https://en.wikipedia.org/wiki/Rational_unified_process), adapted for coding agents and
plain-text artefacts that live with the source code.

## Workflow

```text
Inception            Elaboration                                   Construction
──────────────────   ───────────────────────────────────────────   ──────────────────────
aiup-requirements  →  aiup-entity-model  →  aiup-use-case-diagram  →  aiup-use-case-spec
                                                                    ↘ migrations
                                                                    ↘ backend and UI
                                                                    ↘ tests and status
```

`aiup-core` owns the stack-independent path from vision to specifications. The Compose/Ktor/Exposed plugin continues
from those specifications into migrations, application code, and tests.

Each step reads the files produced by earlier steps. You can review or edit an artefact before continuing, and every
later result remains traceable to the corresponding requirement or use case.

[Read the complete workflow, skill reference, and verification checks →](docs/how-to-use.md)

## Choose your plugins

Install `aiup-core` in every project. Add `aiup-compose-ktor-exposed` when the project uses that implementation stack.

| Plugin                      | Version    | Stack and responsibility                                                                                                                                        |
|-----------------------------|------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `aiup-core`                 | `103.12.1` | Stack-independent requirements, Mermaid entity/use-case modelling, specifications, journey test cases, reverse engineering, architecture, and project reference |
| `aiup-compose-ktor-exposed` | `1.10.0`   | Kotlin Multiplatform, Compose or Kobweb UI, Ktor/Exposed backend, migrations, tests, status, and implementation prompts                                         |

Use only `aiup-core` when working with another implementation stack. The methodology ends at a documented boundary,
so the specifications can feed a custom implementation workflow.

All skill names use the `aiup-` prefix. Claude Code adds the plugin namespace, for example
`/aiup-core:aiup-requirements`. Existing installations using unprefixed names must update their skill directories,
symlinks, saved prompts, and project instructions; generic aliases are not retained.

## Quick start

Before installing AI Unified Process, create `docs/vision.md` in the target project. It should describe the mission, target users,
goals, scope, and constraints. A copy-ready [vision template](docs/templates/vision.md) is available.
In a monorepo, use the selected service's `docs/` directory throughout the workflow.

### Claude Code

Add this fork's Git repository as the marketplace:

```text
/plugin marketplace add <internal-git-repository-url>
/plugin install aiup-core@ai-unified-process-marketplace
/plugin install aiup-compose-ktor-exposed@ai-unified-process-marketplace
```

Omit the Compose plugin for other implementation stacks. See the [installation guide](docs/installation/claude-code.md)
for local checkout setup and service-scoped invocation.

### Other coding agents

Expose whole skill directories from `aiup-core/skills/` and, when applicable, `aiup-compose-ktor-exposed/skills/`
to the agent. Preserve their references and scripts, and configure any required MCP services separately.
See [manual skill setup](docs/installation/other-agents.md) for details.

### Create the first artefacts

Run the core skills in the target project:

```text
/aiup-core:aiup-requirements
/aiup-core:aiup-entity-model
/aiup-core:aiup-use-case-diagram
/aiup-core:aiup-use-case-spec UC-001
```

Agents that do not expose skills as slash commands can invoke them by name or intent, for example:
"Use aiup-requirements to create the requirements catalogue from `docs/vision.md`" or "Specify UC-001".

### Implement and test

After reviewing the use-case specification, continue with the Compose stack:

```text
/aiup-compose-ktor-exposed:aiup-flyway-migration
/aiup-compose-ktor-exposed:aiup-implement UC-001
/aiup-compose-ktor-exposed:aiup-implement-ui UC-001
/aiup-compose-ktor-exposed:aiup-ktor-test UC-001
/aiup-compose-ktor-exposed:aiup-compose-test UC-001
/aiup-compose-ktor-exposed:aiup-implementation-status UC-001
```

For Kobweb browser UI, use `aiup-kobweb-ui` and `aiup-kobweb-test` instead of the Compose UI/test pair. Keep the same backend and shared contracts; Kobweb needs compatible shared JS variants.

Implementation and testing are separate steps. Both reconcile existing code with the reviewed specifications and
preserve the target project's conventions. To prepare prompts for separate sessions, use
`aiup-implementation-prompts`; it writes a shared header and ordered prompts without running implementation.

[Follow the complete usage guide →](docs/how-to-use.md)

## Generated artefacts

The workflow creates a shared documentation contract:

```text
<project-or-service>/docs/
├── vision.md                         # maintained by the team
├── requirements.md                   # aiup-requirements + Mermaid use-case diagram
├── entity_model.md                   # aiup-entity-model, including Mermaid ER diagram
├── architecture.md                   # aiup-architecture
├── REFERENCE.md                      # aiup-reference
├── use_cases/
│   ├── UC-001-<name>.md               # aiup-use-case-spec
│   └── UC-001-implementation-status.md # aiup-implementation-status
└── test_cases/
    └── TC-001-<journey>.md            # aiup-test-case
```

The stack plugin consumes these files and places generated code and tests according to the conventions of the target
project. In monorepos, all documentation belongs to the selected service's `docs/` directory.

## Existing applications

Start an undocumented application with:

```text
/aiup-core:aiup-reverse-engineer
```

The skill inspects entry points, domain models, schema, authentication, and integrations, then proposes requirements,
an entity model, a use-case diagram, and individual use-case specifications. Review the recovered intent and reported
gaps before adopting the result as a baseline.

## Installation

- [Claude Code](docs/installation/claude-code.md) — add the marketplace and install plugins directly.
- [Other agents and manual setup](docs/installation/other-agents.md) — skill directories, invocation, MCP configuration,
  and verification.

This fork distributes plugins directly from Git. Each plugin contains:

- `.claude-plugin/plugin.json`, its metadata and sole version authority;
- `.mcp.json`, optional MCP server configuration for Claude Code;
- portable Agent Skills and their bundled references under `skills/`.

Tessl packages, root Agent Plugins manifests, and automated publishing workflows are not part of this fork.

## Documentation

| Guide                                          | Contents                                                                                  |
|------------------------------------------------|-------------------------------------------------------------------------------------------|
| [Usage and workflow](docs/how-to-use.md)       | Service scoping, analysis, Compose/Ktor/Exposed implementation, testing, and traceability |
| [Vision template](docs/templates/vision.md)    | Starting point for `docs/vision.md`                                                       |
| [CLAUDE.md template](docs/templates/CLAUDE.md) | Stack-neutral repository instructions for Claude Code                                     |
| [Core plugin](aiup-core/)                      | Stack-independent analysis and specifications                                             |

Detailed skill behaviour is documented in each plugin's `skills/*/SKILL.md`. Those files are the authoritative source
for inputs, outputs, safety constraints, and execution steps; READMEs provide navigation and concise summaries.

## Repository layout

```text
marketplace/
├── .claude-plugin/marketplace.json
├── aiup-core/
├── aiup-compose-ktor-exposed/
├── docs/
└── scripts/
```

## Validation

Run from the repository root:

```sh
scripts/validate-skills.sh
```

This checks plugin metadata and versions, skill contracts and links, attribution, use-case specifications, and the
compiled record example covering shared DTOs, backend persistence, client state, Compose and Kobweb UI. See [maintainer guidance](CLAUDE.md) for distribution and versioning rules.

## Upstream integration baseline

The last completed reconciliation reviewed `upstream/main` at `c3a5da318da2186a871b07fcb3a3b8521cf03f22`;
the shared ancestor was `48de70fd8cfe082a7b41563e4cb995c47b37a02c`. Compare future upstream changes from the
reviewed commit and selectively port improvements that preserve this fork's plugins, Mermaid/monorepo contract,
Git distribution, and `aiup-` skill names. Update this baseline after a completed reconciliation.

## Learn more

Visit [unifiedprocess.ai](https://unifiedprocess.ai) for the broader methodology.

## Licence

Licensed under the [Apache License 2.0](LICENSE).

## Copyright and trademark

Copyright 2025-2026 Simon Martinelli and the AI Unified Process contributors. The skills and documentation
are licensed under Apache 2.0. "AI Unified Process" identifies the original methodology by Simon Martinelli
([unifiedprocess.ai](https://unifiedprocess.ai)). You may fork and modify this work under the license terms,
but derived works must keep the [NOTICE](NOTICE) file and may not present themselves as the official
AI Unified Process. If you build on it, please say so and link to the [upstream repository](https://github.com/AI-Unified-Process/marketplace).
