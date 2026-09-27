---
name: plan-citation-gate-step-pattern-gap
description: doc-guard.sh + checkDocBudgets ban plan-directory/decision-number citations in permanent markdown docs but never check the S-step pattern there
metadata:
  type: project
---

The token-hygiene "plans are temporary" gate (`.claude/rules/docs.md`, `.claude/hooks/doc-guard.sh`,
`CheckDocBudgetsTask.kt`, `config/detekt/detekt.yml` `ForbiddenComment`) bans citing a plan directory, a
decision number (`D17`) or a step id (`S5-1a`) from permanent code/docs. The step-id regex
(`\bS[0-9]{1,2}-[0-9A-Z]`) is only wired into the **Kotlin** comment paths (detekt's `ForbiddenComment`
`comments:` list and doc-guard's `check_kotlin_comments`). Neither `check_plan_refs` (the hook's permanent
markdown check) nor `CheckDocBudgetsTask`'s markdown scan include it — only the plan-directory and
D-number patterns. Confirmed live: `docs/reference/module-structure.md` still says "S4-5b" in two rows
(pre-existing, untouched by the PR that introduced this gate) and neither gate flags it.

**Why:** this is a real, currently-unenforced hole in a normative rule the project just added — a
permanent doc can cite a plan step forever and nothing catches it. [[normative-rules-are-not-negotiable]]

**How to apply:** when reviewing a change to this gate (doc-guard.sh, CheckDocBudgetsTask.kt, detekt.yml's
`ForbiddenComment`), check whether the markdown-side patterns were brought to parity with the Kotlin-side
ones before calling the gate complete. Re-check `docs/reference/module-structure.md` and other permanent
docs for stray `S#-#` citations even after this gap is closed, since it's exactly the kind of thing that
slips through un-noticed once the gate looks "done".
