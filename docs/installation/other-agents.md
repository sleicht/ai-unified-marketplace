<!--
Copyright 2025-2026 Simon Martinelli and the AI Unified Process contributors.
Part of the AI Unified Process — https://unifiedprocess.ai
Licensed under the Apache License, Version 2.0. See LICENSE and NOTICE.
-->

# Other Agents and Manual Skill Setup

The [AIUP workflow](../how-to-use.md) uses reviewed files as the contract between steps. A coding agent can follow it by reading the skill instructions and references, editing the target project, and running the required checks. Native plugin support and slash commands are not required.

## Make the skills available

1. Obtain a checkout of this internal repository at a reviewed revision.
2. Make the required directories from `aiup-core/skills/` available to the agent. For Compose/Ktor/Exposed projects, also expose the required directories from `aiup-compose-ktor-exposed/skills/`.
3. If the agent supports skill discovery, copy or symlink whole skill directories into its documented skill location. Preserve `SKILL.md`, `references/`, and `scripts/` together.
4. Otherwise, give the agent the exact path to the relevant `SKILL.md` and ask it to follow that file. Resolve bundled reference and script paths relative to the skill directory; project artefact paths resolve within the selected project or service.

The retained `.claude-plugin/plugin.json` files are Claude Code metadata. This fork does not provide root Agent Plugins manifests or Tessl packages. Do not assume another agent can install these directories as native plugins.

## Request a step

With the marketplace checkout beside the target project, ask:

```text
Follow ../ai-unified-marketplace/aiup-core/skills/aiup-requirements/SKILL.md
for payments-service. Read payments-service/docs/vision.md, create or update
payments-service/docs/requirements.md, and verify the quality checks.
```

Once the use-case diagram exists:

```text
Follow ../ai-unified-marketplace/aiup-core/skills/aiup-use-case-spec/SKILL.md
for UC-001 in payments-service. Read its bundled references and run its
bundled validator on the specification you write.
```

If the agent already discovers those skills, request them by name and include the same service scope and use-case IDs. Exact invocation syntax and skill locations depend on the client; use its current documentation.

## Tools and MCP

The retained plugin `.mcp.json` files describe MCP services for Claude Code. If a step needs one of those services, translate the relevant entries into the target agent's supported MCP configuration and verify access. Copying a skill directory does not configure MCP automatically.

The agent also needs access to the target project's build, database, and test tools for the requested step. If a required tool is unavailable, record which check remains unverified instead of claiming completion.

## Verify and hand off

Check that outputs match the [workflow's artefact and verification contracts](../how-to-use.md#verify-before-continuing). Keep scope and stable IDs unchanged when switching agents. Pass the current reviewed documents, relevant specification changes, and outstanding checks to the next agent.

The same Markdown, Mermaid, source, and test files remain authoritative across clients. Different agents may produce different proposals; review domain correctness before continuing.
