<!--
Copyright 2025-2026 Simon Martinelli and the AI Unified Process contributors.
Part of the AI Unified Process — https://unifiedprocess.ai
Licensed under the Apache License, Version 2.0. See LICENSE and NOTICE.
-->

# Workflow and artifacts

The AI Unified Process is a requirements-first development workflow adapted from the phases of the
[Rational Unified Process](https://en.wikipedia.org/wiki/Rational_unified_process). AI Unified Process keeps product intent in
versioned, human-reviewable artifacts that every later step consumes.

## Workflow

```text
Inception           Elaboration                                   Construction
─────────────────   ───────────────────────────────────────────   ──────────────────────────────
aiup-requirements → aiup-entity-model → aiup-use-case-diagram  →  aiup-use-case-spec → aiup-flyway-migration
                                                                                     ↘ aiup-implement / aiup-implement-ui
                                                                                     ↘ aiup-ktor-test / aiup-compose-test
                                                                                     ↘ aiup-implementation-status
```

`aiup-core` owns the stack-independent steps. `aiup-compose-ktor-exposed` owns migrations, implementation, tests, and
implementation status. The boundary between them is the set of files under `docs/`, not a specific coding agent. In a
monorepo, every path below resolves under the selected service's `docs/` directory.

## Artifact flow

| Artefact                  | Produced by                                     | Consumed by                                 |
|---------------------------|-------------------------------------------------|---------------------------------------------|
| `docs/vision.md`          | Product team                                    | `aiup-requirements`                         |
| `docs/requirements.md`    | `aiup-requirements`, `aiup-use-case-diagram`    | Entity model, use case specifications       |
| `docs/entity_model.md`    | `aiup-entity-model`                             | Migrations and implementations              |
| `docs/use_cases/UC-*.md`  | `aiup-use-case-spec`                            | Implementations, tests, test cases          |
| `docs/test_cases/TC-*.md` | `aiup-test-case`                                | End-to-end journey tests                    |
| `docs/architecture.md`    | `aiup-architecture`                             | Implementations and reviewers               |
| `docs/REFERENCE.md`       | `aiup-reference`                                | Agents and developers                       |
| `docs/open_questions.md`  | `aiup-open-questions`, specification and construction skills | Product owner, specification skills |

The use case diagram is a Mermaid block under `## Use Case Diagram` in `requirements.md`, not a separate file.

Every artefact is a review point. Correcting an intermediate document is expected and is safer than compensating for
an incorrect assumption in generated code.

## Traceability

AI Unified Process uses stable identifiers to preserve the path from intent to tests:

- Functional requirements use `FR-XXX`; non-functional requirements use `NFR-XXX`; constraints use `CON-XXX`.
- Use cases use `UC-XXX` and reference the functional requirements they realize.
- Test cases use `TC-XXX` and reference the use cases in their journey.
- Generated tests retain the applicable `UC-*` or `TC-*` identifier in their name or metadata.

Do not reuse an identifier for a different concern after it has been committed. When a requirement changes, update it
and rerun or reconcile the downstream artifacts that depend on it.

## Implementation status

`aiup-implementation-status` checks traceability from the entity model and use case specifications to code,
migrations, and tests. It writes per-use-case status pages under `docs/use_cases/` and keeps missing or partial
coverage visible. A status page records evidence; it does not replace running the tests.

Use case and test case statuses, their meanings, and who sets each one are defined in the
[use case format](../aiup-core/skills/aiup-use-case-spec/references/format-spec.md) and the
[test case skill](../aiup-core/skills/aiup-test-case/SKILL.md). Construction and test skills never change a
`**Status:**` line; `aiup-implementation-status` only suggests `Implemented`, `Tested`, or `Automated` from evidence.

## Core skills

| Skill                                                                          | Result                                                     |
|--------------------------------------------------------------------------------|------------------------------------------------------------|
| [`aiup-requirements`](../aiup-core/skills/aiup-requirements/SKILL.md)         | Requirements catalogue derived from `docs/vision.md`       |
| [`aiup-entity-model`](../aiup-core/skills/aiup-entity-model/SKILL.md)         | Mermaid ER diagram and attribute tables                    |
| [`aiup-use-case-diagram`](../aiup-core/skills/aiup-use-case-diagram/SKILL.md) | Mermaid use case diagram embedded in `requirements.md`     |
| [`aiup-use-case-spec`](../aiup-core/skills/aiup-use-case-spec/SKILL.md)       | One detailed specification per use case                    |
| [`aiup-test-case`](../aiup-core/skills/aiup-test-case/SKILL.md)               | End-to-end journey specification across use cases          |
| [`aiup-architecture`](../aiup-core/skills/aiup-architecture/SKILL.md)         | Service architecture documentation                         |
| [`aiup-reference`](../aiup-core/skills/aiup-reference/SKILL.md)               | Project reference for agents and developers                |
| [`aiup-open-questions`](../aiup-core/skills/aiup-open-questions/SKILL.md)     | Log of open specification questions and their answers      |
| [`aiup-reverse-engineer`](../aiup-core/skills/aiup-reverse-engineer/SKILL.md) | Baseline recovered from an existing codebase               |

Construction skills are listed in the [usage guide](how-to-use.md#skills-reference). The linked `SKILL.md` files are the authoritative descriptions of inputs, outputs, and behavior.

## Working with changes

When product intent changes:

1. Update `docs/vision.md` or the relevant requirement.
2. Reconcile `docs/requirements.md` and keep existing identifiers stable.
3. Revisit the entity model and use case diagram if the domain or user goals changed.
4. Update affected use case and test case documents.
5. Reconcile migrations, implementation, and tests through `aiup-compose-ktor-exposed`, then refresh implementation status.

Commit `docs/` with the source code. These files explain why the implementation exists and make reviews, onboarding,
and later regeneration reproducible.

## Existing codebases

`aiup-reverse-engineer` inspects entry points, data models, authorization, and integrations to recover the same requirements, entity
model, and use case artefacts produced by the forward workflow. It groups behavior by user goal rather than by endpoint and
reports code it cannot classify. Treat the result as a proposed baseline: resolve gaps and contradictions before
continuing with construction skills.

## Project guidance

Agent instruction files should tell the coding agent to read the AI Unified Process artifacts before making product or architecture
decisions. See [Project setup](guides/project-setup.md) for a generic project tree and reusable templates.
