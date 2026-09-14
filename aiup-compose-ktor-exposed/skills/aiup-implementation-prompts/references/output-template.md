# Implementation Session Prompts

Service: <resolved service or standalone project>
Documentation: <resolved docs path>
Output: <resolved feature- or UC-based filename>
Feature: <feature name, when supplied>
Use cases: <selected UC IDs and names>
UI/test pair: <detected Compose Multiplatform or Kobweb pair, or Not applicable>

## How to Use

Run the numbered sessions sequentially. Start a fresh agent chat for each session
in the same project working tree. Paste the shared header below, followed by
exactly one session prompt. Review the resulting files and verification report
before starting a dependent session. A new chat clears conversation context;
previous sessions' reviewed files remain available on disk.

## Shared Session Header Prompt

```text
Work in <project working directory>, scoped to <service>.
Follow the applicable repository instructions. Use <docs path> as the canonical
documentation directory. Read current files from disk; assume no earlier chat
context. Follow the single AIUP skill named in the session prompt and read its
bundled references. Resolve the skill through discovery or the supplied SKILL.md
path; resolve bundled files relative to that skill's directory.

Reconcile existing work with the current specification, preserve stable IDs and
unrelated changes, and stay within the named skill's boundary. If a required
file, skill, or tool is unavailable, report the prerequisite and stop dependent
work. Run the verification required by the named skill using this project's
actual tasks. Report changed files, checks and results, and remaining blockers.
Save affected symbols, pending test scenarios and failed/unrun checks in this plan
or the existing use-case status artefact so the next session can read them.
Distinguish test files present from executed/passing evidence.
Finish after this session's skill; do not launch the next session automatically.
```

## Session Index

| Session | Use cases | Skill | Predecessors | Expected outputs |
|---|---|---|---|---|
| 01 | <UC ID> | <aiup-skill> | <session numbers or None> | <output paths or module/output types> |

## Session Prompts

### Session 01 — <UC ID and step>

```text
Use <aiup-skill> for <UC ID and name> in <service>.
Skill instructions: <actual readable SKILL.md path, when known>.
Read <actual specification path> and <other applicable input paths>.
Prerequisites: <existing inputs, or predecessor session and its expected outputs>.
Create or reconcile <scoped outputs> according to the specification.
Verify <checks required by this skill, using discovered tasks where available>.
Stop after this skill and report its changes, verification results, and blockers.
```

## Blockers

<Unresolved prerequisites and affected UCs/sessions, or None.>
