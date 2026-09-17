#!/usr/bin/env python3
"""Validate implementation session prompt plans.

Checks *-implementation-prompts.md files written by aiup-implementation-prompts
against references/output-template.md: required sections, the session index,
one prompt and handoff per session, predecessors, status evidence, blockers,
leftover template placeholders, and scoped specification paths.

- ERROR = the plan is inconsistent; the reusable launcher may pick or run the
          wrong session.
- WARN  = the plan works but breaks a skill convention.

Exit code 0 when clean, 1 when any ERROR was found (with --strict also when any
WARN was found), 2 on usage errors.

Usage:
    validate_implementation_plan.py [--strict] [--quiet] [--root DIR] FILE...
    validate_implementation_plan.py --self-test

Requires Python 3.9+, standard library only.
"""

import argparse
import os
import re
import subprocess
import sys
import tempfile

ERROR = "ERROR"
WARN = "WARN"

TITLE = "Implementation Session Prompts"
SECTIONS = [
    "How to Use",
    "Shared Session Header Prompt",
    "Session Index",
    "Session Prompts",
    "Blockers",
]
INDEX_COLUMNS = ["Session", "Use cases", "Skill", "Predecessors",
                 "Expected outputs", "Status", "Evidence"]
STATUSES = {"Pending", "Complete", "Blocked"}
SKILLS = {
    "aiup-flyway-migration",
    "aiup-implement",
    "aiup-implement-ui",
    "aiup-ktor-test",
    "aiup-compose-test",
    "aiup-kobweb-ui",
    "aiup-kobweb-test",
    "aiup-implementation-status",
}
MULTI_UC_SKILLS = {"aiup-flyway-migration"}
NO_VALUE = {"", "none", "-", "n/a", "pending", "tbd"}
# Placeholders of references/output-template.md; the self-test keeps them aligned.
PLACEHOLDERS = [
    "<resolved service or standalone project>",
    "<resolved docs path>",
    "<resolved feature- or UC-based filename>",
    "<feature name, when supplied>",
    "<selected UC IDs and names>",
    "<detected Compose Multiplatform or Kobweb pair, or Not applicable>",
    "<project working directory>",
    "<service>",
    "<docs path>",
    "<UC ID>",
    "<aiup-skill>",
    "<session numbers or None>",
    "<output paths or module/output types>",
    "<checks and results, handoff link, or None>",
    "<UC ID and step>",
    "<UC ID and name>",
    "<actual readable SKILL.md path, when known>",
    "<actual specification path>",
    "<other applicable input paths>",
    "<existing inputs, or predecessor session and its expected outputs>",
    "<scoped outputs>",
    "<checks required by this skill, using discovered tasks where available>",
    "<Unresolved prerequisites and affected UCs/sessions, or None.>",
]

UC_ID = re.compile(r"\bUC-\d+[A-Za-z]?\b")
SKILL_NAME = re.compile(r"\baiup-[a-z]+(?:-[a-z]+)*\b")
SESSION_HEADING = re.compile(r"^Session\s+(\d+)\b")
CELL_SPLIT = re.compile(r"(?<!\\)\|")
SPEC_PATH = re.compile(
    r"(?<![\w/.-])((?:[\w.-]+/)*docs/"
    r"(?:use_cases/[\w.-]+|requirements|entity_model|vision|architecture|REFERENCE)\.md)")


class Problem:
    def __init__(self, line, severity, code, message):
        self.line = line
        self.severity = severity
        self.code = code
        self.message = message


class Plan:
    def __init__(self):
        self.problems = []
        self.headings = []  # (level, title, line)
        self.index = []  # dicts
        self.prompts = []  # dicts
        self.blockers = []  # (line, text)

    def add(self, line, severity, code, message):
        self.problems.append(Problem(line, severity, code, message))


def strip_comments(text):
    return re.sub(r"<!--.*?-->", lambda m: "\n" * m.group(0).count("\n"),
                  text, flags=re.S)


def scan(lines):
    """Yield (line number, text, inside fence, fence language on opening)."""
    fence = None
    for number, line in enumerate(lines, 1):
        stripped = line.strip()
        if fence is None and stripped.startswith("```"):
            fence = stripped[3:].strip()
            yield number, line, True, fence
            continue
        if fence is not None and stripped == "```":
            fence = None
            yield number, line, True, None
            continue
        yield number, line, fence is not None, None


def is_none(value):
    return value.strip().strip(".").lower() in NO_VALUE


def parse_plan(text):
    plan = Plan()
    lines = strip_comments(text).split("\n")
    section = None
    session = None
    part = None
    for number, line, fenced, opening in scan(lines):
        heading = None if fenced else re.match(r"^(#{1,4})\s+(.*?)\s*$", line)
        if heading:
            level, title = len(heading.group(1)), heading.group(2)
            plan.headings.append((level, title, number))
            if level <= 2:
                section, session, part = title, None, None
            elif level == 3 and section == "Session Prompts":
                match = SESSION_HEADING.match(title)
                session = {"line": number, "title": title, "prompts": [],
                           "handoff": None, "number": None}
                part = "prompt"
                if match:
                    session["number"] = int(match.group(1))
                else:
                    plan.add(number, ERROR, "PROMPT_HEADING",
                             "prompt subsection must start with 'Session <number>'")
                plan.prompts.append(session)
            elif level == 4 and session is not None:
                if title == "Handoff":
                    part = "handoff"
                    session["handoff"] = {"line": number, "lines": []}
                else:
                    part = None
            continue
        if section == "Session Index" and not fenced and line.lstrip().startswith("|"):
            cells = [c.strip() for c in CELL_SPLIT.split(line.strip().strip("|"))]
            plan.index.append({"line": number, "cells": cells})
        elif section == "Blockers" and line.strip():
            plan.blockers.append((number, line.strip()))
        elif session is not None and part == "prompt":
            if opening is not None:
                session["prompts"].append({"line": number, "lang": opening, "lines": []})
            elif fenced and session["prompts"] and line.strip() != "```":
                session["prompts"][-1]["lines"].append(line)
        elif session is not None and part == "handoff" and line.strip():
            session["handoff"]["lines"].append((number, line.strip()))
    return plan


def check_structure(plan):
    top = [(title, line) for level, title, line in plan.headings if level == 1]
    if not top or top[0][0] != TITLE:
        plan.add(top[0][1] if top else 1, ERROR, "TITLE",
                 "first heading must be '# " + TITLE + "'")
    seconds = [title for level, title, _ in plan.headings if level == 2]
    for name in SECTIONS:
        if seconds.count(name) != 1:
            plan.add(1, ERROR, "SECTION",
                     "expected exactly one '## " + name + "' section")
    present = [name for name in seconds if name in SECTIONS]
    if len(present) == len(set(present)) and present != [n for n in SECTIONS if n in present]:
        plan.add(1, ERROR, "SECTION_ORDER",
                 "sections must follow the order " + ", ".join(SECTIONS))


def check_index(plan):
    rows = plan.index
    if len(rows) < 2:
        plan.add(1, ERROR, "INDEX_TABLE", "session index table is missing")
        return []
    if rows[0]["cells"] != INDEX_COLUMNS:
        plan.add(rows[0]["line"], ERROR, "INDEX_COLUMNS",
                 "index columns must be: " + " | ".join(INDEX_COLUMNS))
        return []
    sessions = []
    for row in rows[2:]:
        cells, line = row["cells"], row["line"]
        if len(cells) != len(INDEX_COLUMNS):
            plan.add(line, ERROR, "INDEX_ROW", "expected %d cells, found %d"
                     % (len(INDEX_COLUMNS), len(cells)))
            continue
        number, ucs, skill, predecessors, outputs, status, evidence = cells
        if not number.isdigit():
            plan.add(line, ERROR, "SESSION_NUMBER", "session must be a number: " + number)
            continue
        entry = {"line": line, "number": int(number), "ucs": UC_ID.findall(ucs),
                 "skill": skill.strip("`"), "predecessors": [], "status": status,
                 "evidence": evidence}
        if not entry["ucs"]:
            plan.add(line, ERROR, "INDEX_UC", "session %s lists no UC ID" % number)
        elif len(entry["ucs"]) > 1 and entry["skill"] not in MULTI_UC_SKILLS:
            plan.add(line, WARN, "SESSION_SCOPE",
                     "session %s: only a shared migration session may list several UCs" % number)
        if entry["skill"] not in SKILLS:
            plan.add(line, ERROR, "SKILL", "unknown construction skill: " + skill)
        if not is_none(predecessors):
            entry["predecessors"] = [int(n) for n in re.findall(r"\d+", predecessors)]
            if not entry["predecessors"]:
                plan.add(line, ERROR, "PREDECESSOR",
                         "predecessors must be session numbers or None: " + predecessors)
        if is_none(outputs):
            plan.add(line, WARN, "OUTPUTS", "session %s has no expected outputs" % number)
        if status not in STATUSES:
            plan.add(line, ERROR, "STATUS", "status must be Pending, Complete or Blocked: " + status)
        if not evidence:
            plan.add(line, ERROR, "EVIDENCE", "session %s has an empty evidence cell" % number)
        elif status == "Complete" and is_none(evidence):
            plan.add(line, ERROR, "EVIDENCE",
                     "session %s is Complete without evidence" % number)
        sessions.append(entry)

    numbers = [s["number"] for s in sessions]
    for previous, current in zip(sessions, sessions[1:]):
        if current["number"] <= previous["number"]:
            plan.add(current["line"], ERROR, "SESSION_ORDER",
                     "session numbers must be unique and ascending")
    by_number = {s["number"]: s for s in sessions}
    for session in sessions:
        for predecessor in session["predecessors"]:
            if predecessor not in numbers:
                plan.add(session["line"], ERROR, "PREDECESSOR",
                         "session %d depends on missing session %d"
                         % (session["number"], predecessor))
            elif predecessor >= session["number"]:
                plan.add(session["line"], ERROR, "PREDECESSOR",
                         "session %d depends on later session %d"
                         % (session["number"], predecessor))
            elif (session["status"] == "Complete"
                  and by_number[predecessor]["status"] != "Complete"):
                plan.add(session["line"], ERROR, "PREDECESSOR_STATUS",
                         "session %d is Complete but predecessor %d is not"
                         % (session["number"], predecessor))
    return sessions


def check_prompts(plan, sessions):
    by_number = {s["number"]: s for s in sessions}
    prompt_numbers = [p["number"] for p in plan.prompts if p["number"] is not None]
    if prompt_numbers != [s["number"] for s in sessions]:
        plan.add(plan.prompts[0]["line"] if plan.prompts else 1, ERROR, "INDEX_DRIFT",
                 "prompt subsections %s do not match index sessions %s"
                 % (prompt_numbers, [s["number"] for s in sessions]))
    for prompt in plan.prompts:
        number, line = prompt["number"], prompt["line"]
        if len(prompt["prompts"]) != 1 or prompt["prompts"][0]["lang"] != "text":
            plan.add(line, ERROR, "PROMPT_BLOCK",
                     "session %s needs exactly one fenced text prompt" % number)
        if prompt["handoff"] is None:
            plan.add(line, ERROR, "HANDOFF", "session %s has no '#### Handoff' block" % number)
        elif not prompt["handoff"]["lines"]:
            plan.add(prompt["handoff"]["line"], ERROR, "HANDOFF",
                     "session %s handoff is empty; use 'Pending.'" % number)
        else:
            for handoff_line, text in prompt["handoff"]["lines"]:
                if re.match(r"^[-*]?\s*\**Status\**:", text):
                    plan.add(handoff_line, WARN, "HANDOFF_STATUS",
                             "keep status in the session index, not the handoff")
        entry = by_number.get(number)
        if entry is None or not prompt["prompts"]:
            continue
        body = "\n".join(prompt["prompts"][0]["lines"])
        skills = SKILL_NAME.findall(body)
        if entry["skill"] in SKILLS and (not skills or skills[0] != entry["skill"]):
            plan.add(line, ERROR, "PROMPT_SKILL",
                     "session %s prompt must name %s first" % (number, entry["skill"]))
        mentioned = set(UC_ID.findall(body + " " + prompt["title"]))
        for uc in entry["ucs"]:
            if uc not in mentioned:
                plan.add(line, ERROR, "PROMPT_UC",
                         "session %s prompt does not mention %s" % (number, uc))


def check_blockers(plan, sessions):
    if not plan.blockers:
        plan.add(1, ERROR, "BLOCKERS", "Blockers section must list blockers or None")
        return
    text = [t for _, t in plan.blockers]
    none = len(text) == 1 and is_none(text[0])
    for session in sessions:
        if session["status"] != "Blocked":
            continue
        pattern = re.compile(r"(?<![\w-])0*%d(?!\d)" % session["number"])
        if none:
            plan.add(session["line"], ERROR, "BLOCKERS",
                     "session %d is Blocked but Blockers is None" % session["number"])
        elif not any("session" in t.lower() and pattern.search(t) for t in text):
            plan.add(plan.blockers[0][0], WARN, "BLOCKERS",
                     "Blockers does not name blocked session %d" % session["number"])


def check_placeholders(plan, lines):
    for number, line in enumerate(lines, 1):
        for placeholder in PLACEHOLDERS:
            if placeholder in line:
                plan.add(number, ERROR, "PLACEHOLDER",
                         "template placeholder left in plan: " + placeholder)


def check_paths(plan, lines, path, root):
    if root is None:
        return
    root = os.path.abspath(root)
    bases = []
    directory = os.path.dirname(os.path.abspath(path))
    while True:
        bases.append(directory)
        if directory == root or os.path.dirname(directory) == directory:
            break
        directory = os.path.dirname(directory)
    if root not in bases:
        bases.append(root)
    for number, line in enumerate(lines, 1):
        for match in SPEC_PATH.finditer(line):
            relative = match.group(1)
            if relative.endswith("-implementation-status.md"):
                continue
            if not any(os.path.isfile(os.path.join(base, relative)) for base in bases):
                plan.add(number, ERROR, "PATH", "specification path does not exist: " + relative)


def validate_text(text, path="plan.md", root=None):
    plan = parse_plan(text)
    lines = strip_comments(text).split("\n")
    check_structure(plan)
    sessions = check_index(plan)
    check_prompts(plan, sessions)
    check_blockers(plan, sessions)
    check_placeholders(plan, lines)
    check_paths(plan, lines, path, root)
    return plan.problems


def validate_file(path, root=None):
    with open(path, encoding="utf-8") as handle:
        return validate_text(handle.read(), path, root)


# ---------------------------------------------------------------------------
# Self test
# ---------------------------------------------------------------------------

VALID = """\
# Implementation Session Prompts

Service: billing
Documentation: billing/docs
Output: UC-001-charge-card-implementation-prompts.md
Use cases: UC-001 Charge Card

## How to Use

Run the numbered sessions sequentially in fresh chats.

## Shared Session Header Prompt

```text
Work in the repository root, scoped to billing.
```

## Session Index

| Session | Use cases | Skill            | Predecessors | Expected outputs         | Status   | Evidence                 |
|---------|-----------|------------------|--------------|--------------------------|----------|--------------------------|
| 01      | UC-001    | aiup-implement   | None         | server payments module   | Complete | `:server:test` passed    |
| 02      | UC-001    | aiup-ktor-test   | 01           | server payments tests    | Pending  | None                     |

## Session Prompts

### Session 01 — UC-001 backend

```text
Use aiup-implement for UC-001 Charge Card in billing.
Read docs/use_cases/UC-001-charge-card.md.
```

#### Handoff

- Changed files: server payments module
- Checks run / passed / failed / unrun: `:server:test` passed

### Session 02 — UC-001 backend tests

```text
Use aiup-ktor-test for UC-001 Charge Card in billing.
Read docs/use_cases/UC-001-charge-card.md and the Session 01 handoff.
```

#### Handoff

Pending.

<!-- Session 02 replaces "Pending." with its handoff. -->

## Blockers

None.
"""


def codes(problems, severity=None):
    return {p.code for p in problems if severity is None or p.severity == severity}


def self_test():
    failures = []

    def expect(name, problems, expected):
        found = codes(problems)
        for code in expected:
            if code not in found:
                failures.append("%s: expected %s, got %s" % (name, code, sorted(found)))

    def expect_clean(name, problems):
        for problem in problems:
            failures.append("%s: unexpected %s %s: %s"
                            % (name, problem.severity, problem.code, problem.message))

    expect_clean("valid", validate_text(VALID))

    cases = [
        ("missing-section", VALID.replace("## Blockers\n\nNone.\n", ""), ["SECTION"]),
        ("unknown-status", VALID.replace("| Pending  |", "| Started  |"), ["STATUS"]),
        ("complete-without-evidence",
         VALID.replace("| Complete | `:server:test` passed    |", "| Complete | None |"),
         ["EVIDENCE"]),
        ("missing-evidence-column",
         VALID.replace("| Status   | Evidence                 |", "| Status   |"),
         ["INDEX_COLUMNS"]),
        ("predecessor-later", VALID.replace("| None         |", "| 02           |"),
         ["PREDECESSOR"]),
        ("predecessor-missing", VALID.replace("| 01           |", "| 07           |"),
         ["PREDECESSOR"]),
        ("complete-before-predecessor",
         VALID.replace("| Pending  | None", "| Complete | ok").replace(
             "| Complete | `:server:test` passed", "| Pending  | `:server:test` passed"),
         ["PREDECESSOR_STATUS"]),
        ("drift", VALID.replace("### Session 02", "### Session 03"), ["INDEX_DRIFT"]),
        ("wrong-skill", VALID.replace("Use aiup-ktor-test", "Use aiup-compose-test"),
         ["PROMPT_SKILL"]),
        ("unknown-skill", VALID.replace("| aiup-ktor-test   |", "| aiup-deploy      |"),
         ["SKILL"]),
        ("missing-uc", VALID.replace("| 02      | UC-001", "| 02      | UC-002"),
         ["PROMPT_UC"]),
        ("missing-handoff", VALID.replace("#### Handoff\n\nPending.\n", ""), ["HANDOFF"]),
        ("handoff-status", VALID.replace("- Changed files:", "- Status: Complete\n- Changed files:"),
         ["HANDOFF_STATUS"]),
        ("two-prompts", VALID.replace("#### Handoff\n\nPending.",
                                      "```text\nextra\n```\n\n#### Handoff\n\nPending."),
         ["PROMPT_BLOCK"]),
        ("blocked-none", VALID.replace("| Pending  | None", "| Blocked  | None"), ["BLOCKERS"]),
        ("placeholder", VALID.replace("Service: billing", "Service: <service>"),
         ["PLACEHOLDER"]),
        ("unordered", VALID.replace("| 01      | UC-001", "| 03      | UC-001"),
         ["SESSION_ORDER"]),
    ]
    for name, text, expected in cases:
        expect(name, validate_text(text), expected)

    blocked = VALID.replace("| Pending  | None", "| Blocked  | None").replace(
        "## Blockers\n\nNone.", "## Blockers\n\n- Sessions 02: UC-001 card gateway contract missing.")
    expect_clean("blocked-named", validate_text(blocked))
    blocked_unnamed = blocked.replace("Sessions 02:", "Gateway:")
    if codes(validate_text(blocked_unnamed), WARN) != {"BLOCKERS"}:
        failures.append("blocked-unnamed: expected BLOCKERS warning only")

    migration = VALID.replace("| 01      | UC-001    | aiup-implement  ",
                              "| 01      | UC-001 UC-002 | aiup-implement  ")
    expect("multi-uc", validate_text(migration), ["SESSION_SCOPE"])

    here = os.path.dirname(os.path.abspath(__file__))
    template = os.path.join(here, "..", "references", "output-template.md")
    with open(template, encoding="utf-8") as handle:
        template_text = handle.read()
    template_placeholders = sorted(set(re.findall(r"<[^<>\n!]+>", strip_comments(template_text))))
    if template_placeholders != sorted(PLACEHOLDERS):
        failures.append("template placeholders changed: %s" % template_placeholders)
    # Only errors caused by the index placeholders themselves are acceptable.
    placeholder_codes = {"PLACEHOLDER", "SKILL", "INDEX_UC", "PREDECESSOR"}
    unexpected = codes(validate_text(template_text), ERROR) - placeholder_codes
    if unexpected:
        failures.append("template structure no longer matches checker: %s" % sorted(unexpected))

    with tempfile.TemporaryDirectory(prefix="aiup plan validation ") as temporary:
        docs = os.path.join(temporary, "billing", "docs")
        os.makedirs(os.path.join(docs, "use_cases"))
        plan_path = os.path.join(docs, "UC-001-charge-card-implementation-prompts.md")
        with open(plan_path, "w", encoding="utf-8") as handle:
            handle.write(VALID)
        expect("path-missing", validate_file(plan_path, temporary), ["PATH"])
        with open(os.path.join(docs, "use_cases", "UC-001-charge-card.md"), "w",
                  encoding="utf-8") as handle:
            handle.write("# Use Case: Charge Card\n")
        expect_clean("path-present", validate_file(plan_path, temporary))
        result = subprocess.run([sys.executable, os.path.abspath(__file__), "--strict",
                                 "--root", temporary, plan_path],
                                capture_output=True, text=True, timeout=10)
        if result.returncode != 0 or "1 file(s) checked" not in result.stdout:
            failures.append("cli: scoped validation failed: " + result.stdout + result.stderr)

    if failures:
        for failure in failures:
            print("SELF-TEST FAIL: " + failure)
        return 1
    print("self-test passed")
    return 0


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------

def main(argv):
    parser = argparse.ArgumentParser(
        description="Validate AI Unified Process implementation session prompt plans.")
    parser.add_argument("files", nargs="*", help="*-implementation-prompts.md files")
    parser.add_argument("--root",
                        help="project root for resolving specification paths; "
                             "path checks are skipped without it")
    parser.add_argument("--strict", action="store_true", help="treat warnings as failures")
    parser.add_argument("--quiet", action="store_true", help="print problems only, no summary")
    parser.add_argument("--self-test", action="store_true",
                        help="run the built-in fixtures and exit")
    args = parser.parse_args(argv)

    if args.self_test:
        return self_test()
    if not args.files:
        parser.print_usage()
        return 2
    if args.root and not os.path.isdir(args.root):
        print("--root is not a directory: " + args.root)
        return 2

    errors = warnings = 0
    for path in args.files:
        try:
            problems = validate_file(path, args.root)
        except OSError as exc:
            print(path + ": ERROR IO: " + str(exc))
            errors += 1
            continue
        for problem in sorted(problems, key=lambda p: p.line):
            print("%s:%d: %s %s: %s" % (path, problem.line, problem.severity,
                                        problem.code, problem.message))
            if problem.severity == ERROR:
                errors += 1
            else:
                warnings += 1
    if not args.quiet:
        print("%d file(s) checked: %d error(s), %d warning(s)"
              % (len(args.files), errors, warnings))
    if errors or (args.strict and warnings):
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
