# Implementation Session Prompts

Service: <resolved service or standalone project>
Documentation: <resolved docs path>
Output: <resolved feature- or UC-based filename>
Feature: <feature name, when supplied>
Use cases: <selected UC IDs and names>
UI/test pair: <detected Compose Multiplatform or Kobweb pair, or Not applicable>

## How to Use

Run the numbered sessions sequentially. Start a fresh agent chat for each session
in the same project working tree and send this start prompt unchanged, with any
coding agent:

```text
Read <absolute plan path> and follow its Session Launcher section to run the next implementation session.
```

Where the Claude Code command was written, `/<plan-slug>-session` sends the same
prompt. The launcher selects the first session not marked Complete
and combines the shared header with that session's prompt. Every session commits
its own changes with the plan update and states its status (Complete, Pending or
Blocked) in the commit message; review that commit and the verification report
before starting a dependent session. A new chat clears conversation context; previous
sessions' commits remain available in the working tree.

## Session Launcher

```text
Follow applicable repository instructions. Read this whole plan: the session
index, blockers, shared header and session prompts. Then check the current
project files and saved verification evidence. Do not rely on previous chat
history. Do not edit this launcher or the shared header.

Check that the session index and the session prompt subsections list the same
session numbers, UC IDs and skills. If they differ, record the mismatch under
Blockers and stop.

Select the first session in plan order that is not Complete. Apply the Shared
Session Header Prompt and execute only that session's prompt. Respect its skill,
scope and prerequisites. If a predecessor or prerequisite is incomplete, record
the blocker and stop; do not skip ahead or execute another session.

Save changed files, verification results and the handoff in this session's
Handoff block. Update this session's index row: set Complete with evidence only
when its required outputs and checks are satisfied; otherwise leave it Pending
or mark it Blocked with the reason.

Commit this session's changes together with the plan update, whatever its
status, following repository version-control and commit-message instructions.
Include only files and hunks changed by this session; name the UC ID and session
number; state the session status (Complete, Pending or Blocked) in the commit
subject and, for Pending or Blocked, the reason in the body; do not push. If
unrelated changes cannot be separated, do not commit; report them.

Report the result and any commit, then stop after this session.
If all sessions are Complete, report completion without running more work. If the
plan contains no sessions, report its blockers without inventing a session.
```

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
