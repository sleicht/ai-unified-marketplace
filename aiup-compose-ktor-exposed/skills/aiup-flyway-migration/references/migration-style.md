# Flyway Migration Style

Prefer the target project's existing migrations. Use these rules when it matches the reference Compose/Ktor/Exposed service.

## Discovery

- In a mise monorepo, discover the owning stack from `mise.toml` and its modules from that stack's `settings.gradle.kts`.
- Locate migrations under the discovered server module; do not infer module names from sibling builds.
- Read existing migrations to determine version padding, naming, ID strategy, audit columns, constraint naming, and trigger conventions.
- Read dependency/toolchain versions from the target build; do not hardcode them.

## PostgreSQL Shape

Use additive Flyway SQL. Match existing conventions before applying these defaults:

```sql
-- V004__create_example_table.sql
-- Adds example table for UC-XXX.
-- Source: docs/entity_model.md (EXAMPLE)

CREATE TABLE example (
    id          BIGSERIAL   PRIMARY KEY,
    status      VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_example_status ON example(status);
CREATE TRIGGER trg_example_updated_at BEFORE UPDATE ON example FOR EACH ROW EXECUTE FUNCTION set_updated_at();
```

- Use `BIGSERIAL` only when existing migrations use it; preserve explicit sequence/identity conventions otherwise.
- Use explicit foreign keys, uniqueness, check constraints, and indexes required by the entity model and query paths.
- Rely on unique-constraint indexes; do not duplicate them.
- Reuse an existing `set_updated_at()` function. Define it once only for an initial schema.
- Create referenced tables before foreign keys.

## Exposed Compatibility

When the project maps `BIGSERIAL` through Exposed DSL, the compatible shape is normally:

```kotlin
object ExampleTable : Table("example") {
    val id = long("id").autoIncrement()
    val status = varchar("status", 20)
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")

    override val primaryKey = PrimaryKey(id)
}
```

Use plain `Table`, not `LongIdTable`, when that is the project's established mapping. Match table/column names, nullability, lengths, ID strategy, and timestamp types exactly.

## Verification

Prefer focused repository migration or Testcontainers tasks. Command shape:

- monorepo root: `mise run //<stack>:<task>`
- inside stack: `mise run <task>`
- no mise task: the owning build's `./gradlew <module>:<task>`

Run broader compile/verification only when the change spans corresponding Kotlin table mappings.
