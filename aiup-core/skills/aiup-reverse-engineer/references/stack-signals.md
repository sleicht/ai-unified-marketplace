<!--
Copyright 2025-2026 Simon Martinelli and the AI Unified Process contributors.
Part of the AI Unified Process — https://unifiedprocess.ai
Licensed under the Apache License, Version 2.0. See LICENSE and NOTICE.
-->

# Stack signals — where to find actors, use cases, and entities

This is a lookup, not a script. Use it after you've identified the project's
stack from build files. Sections are independent — read only the ones that
match the project in front of you.

## Java / Spring Boot

- **Build files**: `pom.xml`, `build.gradle(.kts)`. Look for `spring-boot-starter-*`
  dependencies to confirm modules in use (`-web`, `-security`, `-data-jpa`,
  `-jooq`, `-thymeleaf`, etc.).
- **Entry points**:
  - `@RestController`, `@Controller` classes.
  - Vaadin views: classes annotated `@Route(...)` or extending `Component` /
    `VerticalLayout` and reachable from a router layout.
  - Scheduled jobs: `@Scheduled`.
  - Message listeners: `@KafkaListener`, `@RabbitListener`, `@JmsListener`,
    `@EventListener`.
- **Actors**:
  - `SecurityFilterChain` configuration — `requestMatchers(...).hasRole("X")`,
    `.authenticated()`, `.permitAll()`.
  - Method-level `@RolesAllowed`, `@PreAuthorize`, `@Secured`.
  - Custom `UserDetailsService` and any role/authority enum.
- **Entities**:
  - JPA: `@Entity` classes (relationships from `@OneToMany`, `@ManyToOne`,
    `@OneToOne`, `@ManyToMany`).
  - jOOQ: schema is in Flyway migrations (`src/main/resources/db/migration/V*.sql`)
    rather than annotated classes; the generated classes mirror the DDL.
  - Validation: Bean Validation annotations (`@NotNull`, `@Size`, `@Email`,
    `@Min`, `@Max`, `@Pattern`).
- **Tests**: `@SpringBootTest`, `@WebMvcTest`, Vaadin Browserless / Karibu
  view tests, Playwright tests under `src/test/`. Tests named after a use
  case (e.g. `UC001NameOfUcTest`) are gold — they encode the success
  scenario and alternative flows already.

## Python / Django

- **Build files**: `requirements.txt`, `pyproject.toml`, `manage.py`.
- **Entry points**: `urls.py` (URL conf), view functions and class-based
  views (`View`, `ListView`, `CreateView`, etc.), DRF `ViewSet`s and
  `APIView`s, Celery tasks (`@shared_task`).
- **Actors**:
  - `auth` app's groups and permissions (`Group`, `Permission`).
  - `LoginRequiredMixin`, `PermissionRequiredMixin`, `@login_required`,
    `@permission_required`.
  - DRF permission classes (`IsAuthenticated`, custom `BasePermission`
    subclasses).
- **Entities**: `models.py` files. Relationships from `ForeignKey`,
  `OneToOneField`, `ManyToManyField`. Validation from `validators=[...]`,
  `null=`, `blank=`, `unique=`, `choices=`. Migrations under
  `<app>/migrations/`.
- **Tests**: `tests.py` or `tests/` directory; `TestCase` subclasses.

## Python / Flask or FastAPI

- **Entry points**: `@app.route(...)` (Flask), `@app.get/post/...`
  (FastAPI), Blueprint registrations, `APIRouter` includes.
- **Actors**: Flask-Login `@login_required`, FastAPI dependencies that
  resolve a user (`Depends(get_current_user)`), custom decorators.
- **Entities**: SQLAlchemy `Base` subclasses, Pydantic models if used as
  the persistence layer. Migrations in Alembic (`migrations/versions/`).

## Node.js / TypeScript / Express

- **Build files**: `package.json`. Look for `express`, `koa`, `fastify`,
  `nestjs`, `next`.
- **Entry points**:
  - Express: `app.get/post/...`, `router.use(...)`.
  - NestJS: `@Controller(...)`, `@Get`, `@Post`, etc.; `@MessagePattern`
    for microservices.
  - Next.js: `pages/api/*` (pages router), `app/**/route.ts` (app router),
    server actions in `app/**/page.tsx`.
- **Actors**: middleware that sets `req.user`, NestJS `@UseGuards(...)`
  with `RolesGuard`, NextAuth session callbacks, custom JWT middleware.
- **Entities**:
  - Prisma: `schema.prisma` is the source of truth for entities and
    relationships.
  - TypeORM: `@Entity` classes with `@Column`, `@OneToMany`, etc.
  - Sequelize: `Model.init({...})` calls.
  - Drizzle: `pgTable(...)` calls in `schema.ts`.
- **Prisma → AIUP type mapping** (never copy Prisma/SQL types into the entity
  model — translate every column):

  | Prisma type                         | AIUP Data Type | Length/Precision                                                 | Validation Rules                                                         |
  |-------------------------------------|----------------|------------------------------------------------------------------|--------------------------------------------------------------------------|
  | `Int @id @default(autoincrement())` | `Integer`      | 10                                                               | `Primary Key`; resolve Sequence/Identity from the actual provider schema |
  | `Int`                               | `Integer`      | 10                                                               | `Not Null`                                                               |
  | `String`                            | `String`       | Actual bound, Unbounded or Unknown from provider/schema evidence | `Not Null`                                                               |
  | `String @unique`                    | `String`       | Evidenced bound or Unknown                                       | `Not Null, Unique`                                                       |
  | `String?` (optional)                | `String`       | Evidenced bound or Unknown                                       | `Optional`                                                               |
  | `Decimal @db.Decimal(10, 2)`        | `Decimal`      | 10,2                                                             | `Not Null`; no range unless separately constrained                       |
  | `Boolean`                           | `Boolean`      | —                                                                | `Not Null`                                                               |
  | `DateTime @default(now())`          | `DateTime`     | —                                                                | `Not Null`                                                               |
  | relation field `userId Int`         | `Integer`      | 10                                                               | `Not Null, Foreign Key (USER.id)`                                        |

  Use the AIUP vocabulary in attribute cells; preserve source paths and explain
  unsupported mappings separately. Comments suggesting allowed values are clues,
  not enforced constraints: corroborate them with validation/schema/tests or
  record them as unconfirmed. Do not infer a length or non-negative range from
  the examples above.
- **Validation**: class-validator decorators, Zod schemas, Joi schemas,
  Yup schemas — these are the richest source of business rules in the
  Node ecosystem.

## Ruby / Rails

- **Entry points**: `config/routes.rb`, controllers under
  `app/controllers/`, ActionMailer mailers, ActiveJob jobs.
- **Actors**: `before_action :authenticate_user!` (Devise), Pundit
  policies, CanCanCan abilities, custom role columns on `users`.
- **Entities**: `app/models/*.rb`. Relationships from `has_many`,
  `belongs_to`, `has_one`, `has_and_belongs_to_many`. Validation from
  `validates :field, ...`. Schema in `db/schema.rb` (canonical) and
  `db/migrate/`.

## Go

- **Entry points**: HTTP handlers registered with `http.HandleFunc`,
  router libraries (chi, gin, echo, fiber). gRPC services implementing
  generated interfaces.
- **Actors**: middleware that decorates the request context with a user
  identity; role checks usually inline in handlers.
- **Entities**: `sqlc`-generated structs (schema in `query.sql` /
  `schema.sql`), GORM structs with tags, Ent schemas under
  `ent/schema/`.

## C# / .NET

- **Entry points**: `[ApiController]` classes, Razor Pages, Blazor
  components with `@page` directive, minimal-API `app.MapGet(...)`,
  `IHostedService` background services.
- **Actors**: `[Authorize(Roles = "...")]`, ASP.NET Identity roles,
  authorization policies in `Program.cs`.
- **Entities**: EF Core `DbContext` with `DbSet<T>` properties; entity
  classes with `[Key]`, `[Required]`, `[ForeignKey]` attributes; or
  fluent config in `OnModelCreating`. Migrations under `Migrations/`.

## Database-only signals (regardless of stack)

When the ORM doesn't capture everything, fall back to the schema:

- **Migrations directory**: usually authoritative. Look for the latest
  state of each table by walking forward through the migrations.
- **Foreign key constraints**: `REFERENCES` identifies the related key. Derive
  cardinality from nullability, uniqueness and participation constraints.
  `ON DELETE CASCADE` and `ON DELETE SET NULL` describe deletion behaviour, not
  mandatory participation; a nullable cascading FK still permits an unattached child.
- **Unique constraints**: a unique foreign key limits multiplicity to at most one. Derive optional participation separately at both ends from nullability and other enforced rules.
- **CHECK constraints**: directly translate to business rules.
- **Lookup tables**: small tables with `(id, code, label)` shape often
  represent enumerated values; in the entity model these can become a
  `Values: A, B, C` validation on the parent rather than their own
  entity, unless they have lifecycle of their own.

## What's an actor vs. what's just an authenticated user

Don't multiply actors past what the code actually distinguishes:

- If every authenticated route does the same thing regardless of user
  attributes, you have one actor: "User" (or whatever the domain calls
  it — "Customer", "Member", "Tenant").
- If routes branch on `hasRole(...)`, you have multiple actors. Name
  them after the role.
- If anonymous routes exist (signup, public catalog), add "Visitor" or
  "Guest" as an actor.
- If the system processes inbound webhooks, scheduled jobs, or message
  queue events, add an actor for the upstream system or scheduler.
