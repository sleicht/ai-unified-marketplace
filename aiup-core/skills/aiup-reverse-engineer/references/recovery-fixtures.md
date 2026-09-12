<!--
Copyright 2025-2026 Simon Martinelli and the AI Unified Process contributors.
Part of the AI Unified Process — https://unifiedprocess.ai
Licensed under the Apache License, Version 2.0. See LICENSE and NOTICE.
-->

# Recovery Acceptance Fixtures

Evaluate recovery in a disposable project using these inputs as evidence. Do not
execute the SQL. Compare the generated artefacts against the expectations; these
fixtures assess agent output, not a deterministic schema conversion program.

## Signed values and key semantics

```sql
CREATE TABLE account (
    id UUID PRIMARY KEY,
    note TEXT NOT NULL
);
CREATE TABLE adjustment (
    id UUID PRIMARY KEY,
    account_id UUID UNIQUE REFERENCES account(id),
    amount NUMERIC(10,2) NOT NULL
);
```

Additional evidence: application code supplies both UUID IDs. An adjustment can
be created without an account, and a test records an amount of `-12.50`.

Expected entity model:

- Both IDs: `UUID`, `-`, `Primary Key, Application Generated`.
- `note`: `String`, `Unbounded`, `Not Null`.
- `account_id`: `UUID`, `-`, `Optional, Foreign Key (ACCOUNT.id), Unique`.
- `amount`: `Decimal`, `10,2`, `Not Null`; no invented `Min: 0`.
- Relationship: `ACCOUNT |o--o| ADJUSTMENT`. Neither side is mandatory.
- Source evidence is recorded without copying credentials.

Variant: making `account_id` non-null gives `ACCOUNT ||--o| ADJUSTMENT`, not
mandatory participation for every account. Removing uniqueness instead yields
an optional many-to-one relationship. Missing string-bound evidence must produce
`Unknown`, not `Unbounded` or `255`.

## Compatible recovered requirements

Input: a clerk can record a signed adjustment; a test verifies the saved amount.
Business priority and performance targets are not documented.

Expected artefacts:

- An FR user-story row with an ID, `To confirm` priority and an evidenced status.
- Canonical NFR/constraint tables, without invented rows or limits.
- Requirements Notes identify evidence and unknowns.
- The UC's Mermaid comment and Overview link to the same existing FR ID.
- Technical schema details remain in the entity model, not scenario steps.
- The generated UC passes the installed validator with explicit absolute paths.

## Repeat recovery after a change

Input: existing `UC-004 Record Adjustment` and a TC that links to its spec; a new
actor goal is discovered and the existing UC is renamed `Record Balance Adjustment`.

Expected result: retain `UC-004`, rename its existing file, reconcile authorised
TC links and allocate a new ID above existing IDs for the new goal. Preserve
unaffected approved content. If business rules are renumbered, record their
qualified old/new mapping and report any consumers outside the task scope.

Two independent CLI commands serving two distinct actor goals may produce two
UCs. Explain the grouping; do not merge them just to reduce the UC count.
