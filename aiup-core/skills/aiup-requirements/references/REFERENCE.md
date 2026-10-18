<!--
Copyright 2025-2026 Simon Martinelli and the AI Unified Process contributors.
Part of the AI Unified Process — https://unifiedprocess.ai
Licensed under the Apache License, Version 2.0. See LICENSE and NOTICE.
-->

# Requirements Reference

## ID Prefixes

| Prefix | Type                       | Example |
|--------|----------------------------|---------|
| FR     | Functional Requirement     | FR-001  |
| NFR    | Non-Functional Requirement | NFR-001 |
| CON    | Constraint                 | CON-001 |

## Priority

| Priority   | Description                                         |
|------------|-----------------------------------------------------|
| High       | Must have. Core functionality or critical quality.  |
| Medium     | Should have. Important but system works without it. |
| Low        | Nice to have. Can be deferred to future releases.   |
| To confirm | Business priority has not been established.         |

### German Priority Values

| Priority      | Description                                         |
|---------------|-----------------------------------------------------|
| Hoch          | Must have. Core functionality or critical quality.  |
| Mittel        | Should have. Important but system works without it. |
| Niedrig       | Nice to have. Can be deferred to future releases.   |
| Zu bestätigen | Business priority has not been established.         |

## Status

| Status      | Description                                                        |
|-------------|--------------------------------------------------------------------|
| Open        | Requirement defined but not yet implemented.                       |
| In Progress | Implementation has started or covers only part of the requirement. |
| Implemented | Implementation complete, pending verification.                     |
| Verified    | Tested and confirmed working.                                      |
| Deferred    | Postponed to a future release.                                     |
| Rejected    | Removed from scope.                                                |
| Superseded  | Replaced by another identified requirement.                        |

### German Status Values

| Status         | Description                                                        |
|----------------|--------------------------------------------------------------------|
| Offen          | Requirement defined but not yet implemented.                       |
| Teilweise      | Implementation has started or covers only part of the requirement. |
| Umgesetzt      | Implementation complete, pending verification.                     |
| Bestätigt      | Tested and confirmed working.                                      |
| Zurückgestellt | Postponed to a future release.                                     |
| Abgelehnt      | Removed from scope.                                                |
| Ersetzt        | Superseded by another requirement.                                 |

Translate states by meaning: `Deferred` ↔ `Zurückgestellt`, `Rejected` ↔
`Abgelehnt`, and `Superseded` ↔ `Ersetzt`. Identify the replacement ID for a
superseded requirement. Keep review/conflict notes separate from these states;
proposed thresholds and priorities remain unconfirmed until reviewed.

### Status Derived from Use Cases

Deferred, Rejected and Superseded are scope decisions and are set by hand. Open,
In Progress, Implemented and Verified are progress, and progress follows the use
cases that list the requirement in their `**Requirements:**` (German
`**Anforderungen:**`) line:

| Requirement status      | When                                                                                                   |
|-------------------------|--------------------------------------------------------------------------------------------------------|
| Open / Offen            | No linking use case is Implemented yet (all Draft/Entwurf, Reviewed/Geprüft or Approved/Genehmigt)     |
| In Progress / Teilweise | Some linking use cases are Implemented or beyond, others are not                                       |
| Implemented / Umgesetzt | Every linking use case is Implemented/Implementiert or beyond, but not all are Tested or Done          |
| Verified / Bestätigt    | Every linking use case is Tested/Getestet or Done/Abgeschlossen                                        |

Obsolete/Obsolet use cases do not count. A requirement that no use case links
keeps its status by hand, including one linked only by Obsolete use cases.

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

### Constraint Source

The optional `Source` column sits directly before `Priority`, so `Status` stays
the last column. It names the law, regulation, standard or internal policy a
constraint comes from, down to the article and, where it matters, the version
(`GDPR Art. 17(1)`, `ISO 27001:2022 A.8.24`). When the column is present, fill it
for every `Regulatory` constraint and leave it empty for the others.

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
