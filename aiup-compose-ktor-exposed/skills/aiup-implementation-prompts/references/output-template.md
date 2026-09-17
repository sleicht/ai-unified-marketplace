# Implementation Session Prompts

Service: <resolved service or standalone project>
Documentation: <resolved docs path>
Output: <resolved feature- or UC-based filename>
Feature: <feature name, when supplied>
Use cases: <selected UC IDs and names>
UI/test pair: <detected Compose Multiplatform or Kobweb pair, or Not applicable>

## How to Use

Run the numbered sessions sequentially. Start a fresh agent chat for each session
in the same project working tree. Paste the generic launcher from the final chat
response unchanged each time. It selects the first session not marked Complete
and combines the shared header with that session's prompt. Every session commits
its own changes with the plan update and states its status (Complete, Pending or
Blocked) in the commit message; review that commit and the verification report
before starting a dependent session. A new chat clears conversation context; previous
sessions' commits remain available in the working tree.

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
Save changed files, affected symbols, pending test scenarios, checks and
unresolved prerequisites in this session's Handoff block so the next session can
read them. Distinguish test files present from executed/passing evidence.
Update only this session's index row: Complete with evidence only when its
required outputs and checks are satisfied; otherwise Pending or Blocked with the
reason.
Commit this session's changes together with the plan update, whatever its
status, following repository version-control and commit-message instructions.
Include only files and hunks changed by this session; name the UC ID and session
number; state the session status (Complete, Pending or Blocked) in the commit
subject and, for Pending or Blocked, the reason in the body; do not push. If
unrelated changes cannot be separated, do not commit; report them.
Finish after this session's skill; do not launch the next session automatically.
```

## Session Index

| Session | Use cases | Skill        | Predecessors              | Expected outputs                      | Status  | Evidence                                    |
|---------|-----------|--------------|---------------------------|---------------------------------------|---------|---------------------------------------------|
| 01      | <UC ID>   | <aiup-skill> | <session numbers or None> | <output paths or module/output types> | Pending | <checks and results, handoff link, or None> |

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

#### Handoff

Pending.

<!-- Session 01 replaces "Pending." with:
- Changed files:
- Affected symbols:
- Pending test scenarios:
- Checks run / passed / failed / unrun:
- Unresolved prerequisites:
-->

## Blockers

<Unresolved prerequisites and affected UCs/sessions, or None.>
