<!--
Copyright 2025-2026 Simon Martinelli and the AI Unified Process contributors.
Part of the AI Unified Process — https://unifiedprocess.ai
Licensed under the Apache License, Version 2.0. See LICENSE and NOTICE.
-->

# Getting started

AI Unified Process turns a product vision into reviewed specifications, implementation, and traceable tests.
Use `aiup-core` for the stack-independent analysis workflow and add `aiup-compose-ktor-exposed` for implementation.

## 1. Choose your plugins

| Plugin                                                       | Use it for                                                                                        |
|--------------------------------------------------------------|---------------------------------------------------------------------------------------------------|
| [`aiup-core`](../aiup-core/)                                 | Requirements, entity model, use cases, test journeys, architecture, reference, reverse engineering |
| [`aiup-compose-ktor-exposed`](../aiup-compose-ktor-exposed/) | Kotlin Multiplatform with Compose or Kobweb UI, Ktor, Exposed, and Flyway                          |

Install only `aiup-core` when another implementation stack consumes the reviewed specifications.

## 2. Prepare the product vision

Create `docs/vision.md` in the project, or `<service>/docs/vision.md` in a monorepo. Describe the problem, target
users, goals, scope, and constraints. Start from the [vision template](templates/vision.md) if needed.

Prefer concrete goals and explicit non-goals over a long feature wish list.

## 3. Install the plugins

In Claude Code:

```text
/plugin marketplace add <internal-git-repository-url>
/plugin install aiup-core@ai-unified-process-marketplace
/plugin install aiup-compose-ktor-exposed@ai-unified-process-marketplace
```

See the [Claude Code installation guide](installation/claude-code.md). For other agents, see
[manual skill setup](installation/other-agents.md).

## 4. Run the core workflow

Name the service in every request when working in a monorepo:

```text
aiup-requirements in payments-service
aiup-entity-model in payments-service
aiup-use-case-diagram in payments-service
aiup-use-case-spec UC-001 in payments-service
aiup-architecture in payments-service
```

Review every generated file before continuing. The artefacts are plain Markdown and Mermaid, so a team can correct
them in version control without running a skill again.

```text
<project-or-service>/docs/
├── vision.md
├── requirements.md          ← includes the Mermaid use case diagram
├── entity_model.md
├── architecture.md
├── REFERENCE.md
├── use_cases/
│   └── UC-001-*.md
└── test_cases/
    └── TC-001-*.md
```

## 5. Implement and test

Once a use case specification is reviewed, continue with `aiup-compose-ktor-exposed`:

```text
aiup-flyway-migration in payments-service
aiup-implement UC-001 in payments-service
aiup-implement-ui UC-001 in payments-service
aiup-ktor-test UC-001 in payments-service
aiup-compose-test UC-001 in payments-service
aiup-implementation-status UC-001 in payments-service
```

For a browser UI, use `aiup-kobweb-ui` and `aiup-kobweb-test` instead of the Compose UI/test pair. For a journey
across several use cases, create a specification with `aiup-test-case UC-001 UC-004`. To split the work into fresh
agent sessions, use `aiup-implementation-prompts`.

The complete artefact lifecycle is described in [Workflow and artefacts](workflow.md) and the
[usage guide](how-to-use.md).

## Existing projects

When a project already contains working code but lacks AI Unified Process documents, start with
`aiup-reverse-engineer`. It recovers requirements with the Mermaid use case diagram, use case specifications, and an
entity model from the codebase. Review the reported gaps before using those artefacts as the baseline.
