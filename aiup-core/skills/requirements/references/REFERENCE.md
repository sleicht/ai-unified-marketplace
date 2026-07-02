# Requirements Reference

## ID Prefixes

| Prefix | Type                       | Example |
|--------|----------------------------|---------|
| FR     | Functional Requirement     | FR-001  |
| NFR    | Non-Functional Requirement | NFR-001 |
| C      | Constraint                 | C-001   |

## Priority

| Priority | Description                                         |
|----------|-----------------------------------------------------|
| High     | Must have. Core functionality or critical quality.  |
| Medium   | Should have. Important but system works without it. |
| Low      | Nice to have. Can be deferred to future releases.   |

### German Priority Values

| Priority | Description |
|----------|-------------|
| Hoch | Must have. Core functionality or critical quality. |
| Mittel | Should have. Important but system works without it. |
| Niedrig | Nice to have. Can be deferred to future releases. |

## Status

| Status      | Description                                    |
|-------------|------------------------------------------------|
| Open        | Requirement defined but not yet implemented.   |
| In Progress | Currently being implemented.                   |
| Implemented | Implementation complete, pending verification. |
| Verified    | Tested and confirmed working.                  |
| Deferred    | Postponed to a future release.                 |
| Rejected    | Removed from scope.                            |

### German Status Values

| Status | Description |
|--------|-------------|
| Offen | Requirement defined but not yet implemented. |
| Teilweise | Implementation has started or covers only part of the requirement. |
| Umgesetzt | Implementation complete, pending verification. |
| Bestätigt | Tested and confirmed working. |
| Abgelehnt | Removed from scope. |
| Ersetzt | Superseded by another requirement. |

## NFR Categories

| Category        | Description                                   |
|-----------------|-----------------------------------------------|
| Performance     | Speed, throughput, response time              |
| Scalability     | Ability to handle growth                      |
| Availability    | Uptime, fault tolerance                       |
| Security        | Authentication, authorization, encryption     |
| Usability       | User experience, accessibility                |
| Maintainability | Code quality, documentation, modularity       |
| Portability     | Platform independence, deployment flexibility |

## Constraint Categories

| Category    | Description                                   |
|-------------|-----------------------------------------------|
| Technical   | Technology stack, platforms, integrations     |
| Business    | Budget, resources, organizational policies    |
| Schedule    | Deadlines, milestones, time constraints       |
| Regulatory  | Legal, compliance, industry standards         |
| Operational | Deployment, maintenance, support requirements |

## Language Variants

English default functional requirement format:

```text
As a [role], I want [goal] so that [benefit].
```

German functional requirement format:

```text
Als [Rolle] möchte ich [Ziel], damit [Nutzen].
```

When writing German docs, keep domain terms untranslated unless existing project docs translate them.
