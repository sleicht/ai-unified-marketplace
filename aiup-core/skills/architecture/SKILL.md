---
name: architecture
description: >
  Creates or updates minimal architecture.html documentation for a service: context, high-level structure, internal
  layering, data flow, decisions/ADRs, tech stack, scaling, failure modes, security, observability, deployment, and
  cross-cutting concerns. Use when the user asks for architecture documentation, architecture diagrams, ADR summaries,
  system design docs, C4-style overviews, or service architecture pages.
---

# Architecture Documentation

## Instructions

Create or update `architecture.html` for $ARGUMENTS. Resolve the docs path first: if a service/module is in scope or cwd is inside a monorepo service, write `<service>/docs/architecture.html`; otherwise write `docs/architecture.html`.

Output minimal, portable HTML: semantic headings, numbered sections, simple tables, embedded Mermaid where useful, and no brand-specific CSS or external assets.

## Path and Language Resolution

- Detect monorepo services from `mise.toml` (`monorepo_root` or namespaced tasks) or multiple sibling `settings.gradle.kts` builds.
- Read docs from the same resolved docs directory: `vision.md`, `requirements.html`, `entity_model.md`, and `use_cases/` when present.
- Detect existing docs language when updating; default to English for new docs.
- For German docs, translate generic headings but keep domain terms, package names, module names, and technology names unchanged.

## Required Sections

Use these numbered sections unless an existing architecture page already has an equivalent structure:

1. Context
2. High-Level Architecture
3. Internal Architecture and Layering
4. Data Flow
5. Decisions and ADRs
6. Tech Stack
7. Scaling
8. Failure Modes
9. Security
10. Observability
11. Deployment
12. Cross-Cutting Concerns

Include inline ADR subsections when decisions are known:

```html
<section id="adr-001">
  <h3>ADR-001: Decision title</h3>
  <p><strong>Status:</strong> Proposed | Accepted | Superseded | Rejected</p>
  <p><strong>Context:</strong> ...</p>
  <p><strong>Decision:</strong> ...</p>
  <p><strong>Consequences:</strong> ...</p>
</section>
```

## Mermaid Guidance

Use Mermaid only for diagrams that clarify structure or flow:

- Context or C4-style system overview
- Container/module layout
- Internal layer dependencies
- Request/data flow
- Deployment topology

Keep diagrams generic and valid Mermaid. Do not use PlantUML.

## Stack-Specific Discovery

Read the project before writing stack details:

- build files and `settings.gradle.kts` for modules
- dependency catalogs for stack versions
- package/module layout for layering
- `ArchitectureTest.kt` when present; document enforced rules rather than inventing new ones
- deployment files when present (`Dockerfile`, Helm, Kubernetes, CI config)
- ADR files under `docs/adr/` when present

## DO NOT

- Add brand styling, company-specific colours, or external CSS
- Hardcode a stack the project does not use
- Claim a quality attribute is implemented without source/config evidence
- Create a separate ADR skill or ADR workflow; inline only the decisions needed for the architecture page
- Replace existing architecture content wholesale when a targeted update is enough

## Workflow

1. Resolve docs path and service/module scope.
2. Read existing `architecture.html` if present.
3. Read `vision.md`, `requirements.html`, `entity_model.md`, and relevant use cases when present.
4. Inspect build/module layout and dependency files for actual tech stack.
5. Inspect source package layout and architecture tests for layering.
6. Inspect deployment/observability/security config when present.
7. Write or update the numbered minimal HTML sections.
8. Embed Mermaid diagrams only where they add useful structure.
9. Validate links and referenced files exist.
10. If rendering is available, open the HTML in a browser for a quick sanity check.

## Output Contract

The page should answer:

- What system/service is this?
- What external actors and systems interact with it?
- What are the major modules and layers?
- How does data move through it?
- Which decisions shape the architecture?
- What stack, deployment, security, and observability assumptions are visible in the repo?
- What are the known risks, failure modes, and cross-cutting concerns?
