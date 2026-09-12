<!--
Copyright 2025-2026 Simon Martinelli and the AI Unified Process contributors.
Part of the AI Unified Process — https://unifiedprocess.ai
Licensed under the Apache License, Version 2.0. See LICENSE and NOTICE.
-->

# aiup-core

> Stack-agnostic core of the [**AI Unified Process**](https://unifiedprocess.ai) — a structured,
> requirements-first workflow for taking a software project from raw vision to use case specifications.

`aiup-core` is the technology-independent foundation plugin. It stops at the specification boundary; implementation
and testing are handled by the retained `aiup-compose-ktor-exposed` plugin or by project-specific tooling for other stacks.

## What it does

The plugin automates the analysis phases of the AI Unified Process, adapted from the
[Rational Unified Process](https://en.wikipedia.org/wiki/Rational_unified_process) — **Inception → Elaboration →
Construction**. Every project starts from a written vision and proceeds through requirements, an entity model, and use
case specifications. Nothing gets built without a use case.

This prevents the most common failure mode of AI-assisted development: jumping straight to code from a vague prompt and
producing something that half-works and can't be maintained.

## Skills

Skills are identified by their `aiup-` names. Invocation depends on the agent; Claude Code adds the plugin namespace, for example `/aiup-core:aiup-requirements`. Skills pick up where the previous one left off by reading the files
written along the way, so you can inspect or edit any artifact before continuing.

| Phase        | Skill                   | Description                                                                                                           |
|--------------|-------------------------|-----------------------------------------------------------------------------------------------------------------------|
| Inception    | `aiup-requirements`     | Generate a structured requirements catalog (user stories, NFRs, constraints) from `docs/vision.md`                    |
| Elaboration  | `aiup-entity-model`     | Create an entity model with a Mermaid ER diagram and attribute tables                                                 |
| Elaboration  | `aiup-use-case-diagram` | Embed a Mermaid use case diagram mapping actors to use cases                                                          |
| Construction | `aiup-use-case-spec`    | Write detailed use case specifications (flows, pre/postconditions, rules)                                             |
| Any          | `aiup-reverse-engineer` | Recover use case diagram, use case specs, and entity model from existing code                                         |
| Construction | `aiup-test-case`        | Create or update a journey specification spanning several use cases; hand off only to compatible installed automation |
| Any          | `aiup-architecture`     | Document observed architecture, decisions and evidence                                                                |
| Any          | `aiup-reference`        | Maintain concise project context, commands and canonical document links                                               |

### Workflow

```
Inception          Elaboration                          Construction
─────────────────  ──────────────────────────────────   ─────────────────
aiup-requirements  →  aiup-entity-model  →  aiup-use-case-diagram  →  aiup-use-case-spec
```

The skills produce and consume a set of artifacts under `docs/`:

- `docs/vision.md` — *input you provide* (product vision, target users, goals)
- `docs/requirements.md` — requirements catalog and embedded Mermaid use case diagram
- `docs/entity_model.md`
- `docs/use_cases/UC-*.md`
- `docs/test_cases/TC-*.md`
- `docs/architecture.md` and `docs/REFERENCE.md`

**Inheriting a legacy codebase?** Start with `aiup-reverse-engineer` — it walks the existing code, configuration, and
schema and produces the same `docs/requirements.md`, `docs/use_cases/UC-*.md`, and `docs/entity_model.md` artifacts the
forward workflow would have produced, giving you a documented baseline to work from.

## Updates and handoffs

Resolve the target service before writing, preserve existing document IDs and
unaffected content, and record evidence or unresolved decisions in the artefacts.
Reverse engineering uses the same FR tables and UC links as the forward path.
A TC document specifies a journey; it is ready for automation only when an
installed test skill explicitly supports that input contract.

## MCP servers

| Server   | Purpose                                                                   |
|----------|---------------------------------------------------------------------------|
| context7 | Fetches current library/framework documentation on demand during analysis |

## Installation

Install from the internal Git marketplace:

```text
/plugin install aiup-core
```

## Prerequisites

- A `docs/vision.md` file at the root of your project describing the product vision, target users, and high-level
  goals. The `aiup-requirements` skill reads this file to derive your requirements catalog — the richer it is, the better
  the results.

## Next step

For Kotlin Multiplatform / Compose / Ktor / Exposed projects, install `aiup-compose-ktor-exposed` to implement and test the specifications. Other stacks may consume the core artefacts with their own project tooling.

## License

Apache-2.0 · © 2025-2026 [Simon Martinelli](https://unifiedprocess.ai) and the AI Unified Process contributors. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
