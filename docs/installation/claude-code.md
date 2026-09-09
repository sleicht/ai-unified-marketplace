<!--
Copyright 2025-2026 Simon Martinelli and the AI Unified Process contributors.
Part of the AI Unified Process — https://unifiedprocess.ai
Licensed under the Apache License, Version 2.0. See LICENSE and NOTICE.
-->

# Install with Claude Code

This fork provides `aiup-core` and `aiup-compose-ktor-exposed`. Install core for the methodology; add the Compose plugin when the target project uses Compose Multiplatform, Ktor, and Exposed.

## Add the marketplace

In Claude Code, add your organisation's checkout URL for this repository:

```text
/plugin marketplace add <internal-git-repository-url>
```

Alternatively, add an existing local checkout:

```text
/plugin marketplace add /absolute/path/to/ai-unified-marketplace
```

The placeholder must point to this fork, whose [marketplace manifest](../../.claude-plugin/marketplace.json) declares `ai-unified-process-marketplace`. The marketplace identifier comes from the manifest, not the Git repository's name.

## Install plugins

```text
/plugin install aiup-core@ai-unified-process-marketplace
/plugin install aiup-compose-ktor-exposed@ai-unified-process-marketplace
```

Omit the second command for other implementation stacks. The Compose workflow requires the target project's Kotlin Multiplatform/Gradle setup and its configured PostgreSQL and verification tools.

## Invoke skills

Plugin skills use the plugin namespace. Start Claude Code in the target project and, after preparing `payments-service/docs/vision.md`, request:

```text
/aiup-core:aiup-requirements in payments-service
```

Verify that it reads the scoped vision, writes `payments-service/docs/requirements.md`, and passes the requirements quality checks. This creates or updates documentation; it is not a read-only installation check.

Follow the [agent-independent workflow](../how-to-use.md), using these command forms:

```text
/aiup-core:aiup-entity-model in payments-service
/aiup-core:aiup-use-case-diagram in payments-service
/aiup-core:aiup-use-case-spec UC-001 in payments-service
/aiup-core:aiup-architecture in payments-service
/aiup-compose-ktor-exposed:aiup-flyway-migration in payments-service
/aiup-compose-ktor-exposed:aiup-implement UC-001 in payments-service
/aiup-compose-ktor-exposed:aiup-implement-ui UC-001 in payments-service
/aiup-compose-ktor-exposed:aiup-ktor-test UC-001 in payments-service
/aiup-compose-ktor-exposed:aiup-compose-test UC-001 in payments-service
/aiup-compose-ktor-exposed:aiup-implementation-status UC-001 in payments-service
/aiup-core:aiup-reference in payments-service
```

For an inherited service, start with `/aiup-core:aiup-reverse-engineer in payments-service` and review the recovered documentation. For a standalone project, omit the service qualifier; artefacts live under `docs/`.

See Claude Code's documentation for [plugin invocation](https://code.claude.com/docs/en/plugins) and [marketplace installation](https://code.claude.com/docs/en/plugin-marketplaces).
