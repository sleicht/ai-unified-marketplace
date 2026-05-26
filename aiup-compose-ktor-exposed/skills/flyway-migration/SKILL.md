---
name: flyway-migration
description: >
  Creates Flyway PostgreSQL migrations for the Compose/Ktor/Exposed stack in
  the current reference service style: BIGSERIAL primary keys, explicit
  constraints, indexes, TIMESTAMPTZ audit columns, updated_at triggers, and
  Exposed table compatibility. Use when the user asks to "create a migration",
  "generate SQL scripts", "set up database tables", "write a Flyway migration",
  or mentions schema migration, DB migration, database versioning, or SQL files.
---

# Flyway Migration

## Instructions

Create Flyway database migrations for PostgreSQL from `docs/entity_model.md` and the target project's existing migrations. Follow existing migration style first. When the project resembles reference service, use `../_references/service-style.md`.

## Required Reference

Read `../_references/service-style.md` before creating migrations. Apply its migration and Exposed compatibility rules.

## DO NOT

- Drop or truncate existing tables without explicit user confirmation
- Invent standalone sequences when existing migrations use `BIGSERIAL`
- Use `SERIAL`/`BIGSERIAL` if the target project already uses explicit sequences instead
- Skip check constraints, uniqueness constraints, or foreign keys from the entity model
- Forget indexes for lookup columns used by repositories/routes
- Forget `created_at` / `updated_at` conventions when existing tables use them
- Create Exposed table definitions in this skill unless the user asks; document compatibility notes instead

## Naming Convention

Use the next available Flyway version under the server module:

```text
<server-module>/src/main/resources/db/migration/V001__create_initial_schema.sql
<server-module>/src/main/resources/db/migration/V002__create_import_run_table.sql
<server-module>/src/main/resources/db/migration/V003__add_language_and_head_contract_number.sql
V004__create_coverage_domain_tables.sql
```

Inspect existing migrations to determine zero padding and description style.

## Reference SQL Style

Use this style when existing migrations match it:

```sql
-- V004__create_example_table.sql
-- Adds example table for UC-XXX.
-- Source: docs/entity_model.md (EXAMPLE)

CREATE TABLE example (
    id                  BIGSERIAL       PRIMARY KEY,
    patient_id          BIGINT          NOT NULL REFERENCES patient(id),
    external_id         VARCHAR(50)     NOT NULL UNIQUE,
    status              VARCHAR(20)     NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE')),
    payload             TEXT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_example_external_id CHECK (external_id <> '')
);

CREATE INDEX idx_example_patient_id ON example(patient_id);
CREATE INDEX idx_example_status ON example(status);

CREATE TRIGGER trg_example_updated_at BEFORE UPDATE ON example FOR EACH ROW EXECUTE FUNCTION set_updated_at();
```

If `set_updated_at()` already exists, reuse it. If creating the initial schema, define it once:

```sql
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

## Column Conventions

| Entity type | SQL convention |
|---|---|
| Primary key | `id BIGSERIAL PRIMARY KEY` when existing service uses BIGSERIAL |
| Foreign key | `patient_id BIGINT NOT NULL REFERENCES patient(id)` |
| Strings | `VARCHAR(n)` with entity-model length |
| Long text / JSON payload snapshots | `TEXT`; use `JSONB` only if existing schema does |
| Boolean | `BOOLEAN NOT NULL DEFAULT ...` |
| Date | `DATE` |
| Timestamp | `TIMESTAMPTZ` |
| Enums | `VARCHAR(n) NOT NULL CHECK (field IN (...))` |
| Audit | `created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`, `updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()` |

## Constraints and Indexes

Create constraints close to table definition:

```sql
CONSTRAINT chk_patient_correlation CHECK (
    partner_contract_number IS NOT NULL OR ahv_number IS NOT NULL
),
CONSTRAINT chk_import_run_records CHECK (
    imported_records + error_records <= total_records
),
CONSTRAINT uq_coverage_patient_product UNIQUE (patient_id, product_type, valid_from)
```

Create indexes after table definition for common query paths:

```sql
CREATE INDEX idx_patient_active ON patient(active);
CREATE INDEX idx_patient_last_name ON patient(last_name);
CREATE INDEX idx_dlq_created_at ON dead_letter_queue_entry(created_at);
```

Rely on implicit unique indexes from `UNIQUE` constraints; do not duplicate them.

## Exposed Compatibility Notes

When documenting corresponding Exposed table definitions, match the migration:

```kotlin
object ExampleTable : Table("example") {
    val id = long("id").autoIncrement()
    val patientId = long("patient_id")
    val externalId = varchar("external_id", 50)
    val status = varchar("status", 20)
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")

    override val primaryKey = PrimaryKey(id)
}
```

Use plain `Table`, not `LongIdTable`, when the project uses `Table("...")` and `long("id").autoIncrement()`.

## Workflow

1. Read `docs/entity_model.md` and relevant `docs/use_cases/UC-*.md`.
2. Inspect existing migrations under `<server-module>/src/main/resources/db/migration`.
3. Determine the next Flyway version and naming format.
4. Identify new/changed entities, columns, constraints, and indexes.
5. Create additive SQL only; avoid destructive changes unless user explicitly approved.
6. Order tables so referenced tables exist before foreign keys reference them.
7. Add `updated_at` triggers for tables with `updated_at` when the project uses trigger-based audit timestamps.
8. Check Exposed compatibility: table names, column names, ID strategy, timestamp types.
9. Validate SQL mentally against PostgreSQL syntax.
10. Run LSP diagnostics for related Kotlin table files if touched.
11. Verify with project command: prefer `mise run compile` and migration/test task if available; fallback to `./gradlew <server-module>:classes` or Flyway task.

## Resources

- `../_references/service-style.md` — canonical migration style
- KotlinDocs MCP server — Exposed table DSL reference
