<!--
Copyright 2025-2026 Simon Martinelli and the AI Unified Process contributors.
Part of the AI Unified Process — https://unifiedprocess.ai
Licensed under the Apache License, Version 2.0. See LICENSE and NOTICE.
-->

# Entity Model Reference

## Validation Rules

Compose the evidenced rules below in the Validation Rules column. Never leave it
empty; use `Unknown` when evidence is insufficient. Do not infer defaults.

| Meaning | Validation Rules Value |
|---------|------------------------|
| Key membership | Primary Key |
| Sequence-generated key | Primary Key, Sequence |
| Identity-generated key | Primary Key, Identity |
| Application-generated key | Primary Key, Application Generated |
| Database default | Database Default (evidenced expression or safe description) |
| Required field | Not Null |
| Optional field | Optional |
| Unique field | Unique (combine with Not Null or Optional as evidenced) |
| Foreign key | Foreign Key (TABLE.attribute), combined with Not Null or Optional |
| Range | Min: X, Max: Y (include only evidenced bounds) |
| Enumerated values | Values: A, B, C |
| Email format | Format: Email |
| Missing evidence | Unknown |

Examples: `Optional, Foreign Key (USER.id), Unique` and `Primary Key,
Application Generated` for an application-generated UUID. A Primary Key does not
imply a sequence. For composite keys or constraints, name the participating
attributes in `**Constraints:**` after the table. Record unknown generation there
instead of inventing it. Preserve source paths for constraints and generation.
Never copy a credential-bearing default value; describe its purpose and location.

## Data Types

| Data Type | Length/Precision | Usage |
|-----------|------------------|-------|
| Long | 19 | Large integers, numeric IDs when evidenced |
| UUID | - | UUID identifiers and matching foreign keys |
| String | Actual bound, Unbounded, or Unknown | Text and string identifiers |
| Integer | 10 | Whole numbers |
| Decimal | Actual precision,scale or Unknown | Signed or unsigned values as constrained |
| Boolean | 1 | True/false flags |
| Date | - | Date only |
| DateTime | - | Date and time; record timezone semantics in Constraints |

`Unbounded` means the source explicitly establishes no length bound. `Unknown`
means it was not established. Never substitute 255, 10,2 or a positive-only range
for missing evidence. If an observed source type cannot be represented, record
its semantics and the unsupported mapping as an open issue rather than coercing
it to a different type. Resolve that issue before claiming migration readiness.

## Relationship Cardinality

Derive each end independently from FK nullability, uniqueness and participation
constraints. For a child B referencing parent A:

| Evidence | Mermaid relationship |
|----------|----------------------|
| Non-null FK; no uniqueness | A ||--o{ B |
| Nullable FK; no uniqueness | A |o--o{ B |
| Non-null unique FK; no requirement that every parent has a child | A ||--o| B |
| Nullable unique FK; no required participation at either end | A |o--o| B |
| Exactly one child per parent enforced, and non-null unique FK | A ||--|| B |

A unique FK limits how many children share a parent; it does not require a child
to exist for every parent. For domain join entities, show their separate FK
relationships and preserve composite uniqueness independently.
