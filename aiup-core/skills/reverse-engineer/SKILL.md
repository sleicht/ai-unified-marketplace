---
name: reverse-engineer
description: >
  Reverse-engineers an existing software project into AI Unified Process
  artifacts: requirements.html with an embedded Mermaid use case diagram,
  per-use-case specifications, and an entity model with a Mermaid ER diagram.
  Use when the user asks to "reverse engineer this codebase", "extract use
  cases from existing code", "document the system we already have", "generate
  use case specs from controllers", "derive an entity model from the database",
  or onboard an inherited/legacy codebase. Trigger whenever use cases or an ER
  model must be recovered from existing code rather than a new product vision.
---

# Reverse Engineer Project to AIUP Artifacts

## Goal

Recover business intent from code and produce the same canonical artifacts as the forward skills:

1. `docs/requirements.html` with its Mermaid diagram under `#use-case-diagram`
2. `docs/use_cases/UC-XXX-name.md`, one specification per use case
3. `docs/entity_model.md` with Mermaid ER relationships and attribute tables

Resolve the docs directory to `<service>/docs/` when a service/module is in scope or cwd is inside a monorepo service; otherwise use `docs/`.

Read [references/artifact-contract.md](references/artifact-contract.md) before writing artifacts. After detecting the stack, read only the matching section of [references/stack-signals.md](references/stack-signals.md).

## Principles

- Recover intent; do not transcribe implementation.
- Group entry points by the actor's end-to-end goal. A CRUD controller is usually one “Manage X” use case, not one per route.
- Derive only what code, schema, configuration, and tests support. Mark partial or unclear behaviour as `Draft` and report the uncertainty.
- Treat migrations as the persistence source of truth, then use ORM models and DTOs to add relationships and validation.
- Exclude health/metrics/static routes and technical persistence tables from business artifacts.

## Workflow

Use the available planning/task mechanism when useful.

### 1. Discover the project

- Detect stack, framework, modules, and data layer from build/task files.
- List user-facing entry points: controllers, routes, resolvers, views, CLI commands, scheduled jobs, and message consumers.
- Locate authentication/authorisation rules, migrations, ORM models, validation, and tests.
- Prefer target-project conventions and existing documentation over the bundled examples.

For large projects, first list every entry-point file, cluster files by feature, process one cluster at a time, then make one shared data-layer pass.

### 2. Identify actors

- Derive roles from route guards, permissions, security configuration, and tested access rules.
- Distinguish anonymous and authenticated actors only when behaviour differs.
- Model external systems, schedulers, and inbound integrations as actors when they initiate behaviour.
- Use role names from the domain; do not invent finer roles than the code enforces.

### 3. Aggregate use cases

For each entry point, ask what goal the actor achieves. Group all operations serving that goal before assigning IDs.

Example aggregation:

| Entry points | Use case |
|---|---|
| list/get/create/update/delete catalog items | `Manage Catalog` |
| view cart/add item/remove item/checkout | `Place Order` |
| login/logout/current-user | `Authenticate` |

Count entry points and proposed use cases before writing. If counts are close, regroup: the result is probably mirroring the API. Assign stable `UC-001`, `UC-002`, … IDs grouped by actor, then importance.

### 4. Recover scenarios and rules

- Trace happy paths through code and tests, expressing steps as actor/system outcomes.
- Derive alternatives from validation branches, exceptions, conditional UI, and error tests.
- Derive preconditions from guards and required upstream state.
- Derive postconditions from persisted changes, events, notifications, and rollback behaviour.
- Derive business rules from validation, configuration, constants, constraints, and policy branches.
- Keep `BR-XXX` IDs unique across every generated use-case file.

### 5. Recover the entity model

Inspect sources in this order:

1. Schema migrations or DDL
2. ORM models and mappings
3. DTOs/forms only when persistence structure is otherwise unavailable

Map implementation types and validation into the AIUP vocabulary defined in the artifact contract. Derive relationship cardinality from foreign keys, nullability, unique constraints, and ORM associations. Skip framework tables unless they carry domain lifecycle.

### 6. Write and cross-validate

Write `requirements.html`, the scoped use-case files, and `entity_model.md` using the bundled contract. Then verify:

- exactly one `#use-case-diagram` section and Mermaid block exist;
- every diagram actor is primary actor for at least one spec;
- every diagram `UC-XXX` has exactly one correctly named spec;
- every alternative flow references a main step and ends or resumes explicitly;
- `BR-XXX` IDs are globally unique;
- entity tables have exactly five columns and only AIUP types/validation terms;
- every ER entity has an attribute section and every section appears in the ER diagram;
- the use-case count is meaningfully below the entry-point count.

### 7. Report

Report use-case and entity counts, unclassified entry points/files, excluded technical tables, and the uncertain use cases that deserve human review first.

## Do Not

- Invent unsupported use cases, rules, actors, or entities.
- Name use cases after routes, methods, controllers, or tables.
- Put HTTP, SQL, framework, crypto, or protocol details in scenario steps.
- Put attributes in Mermaid ER entity blocks.
- Skip the entity model because migrations already exist.
- Replace unrelated requirements content when inserting the Mermaid use-case diagram.
