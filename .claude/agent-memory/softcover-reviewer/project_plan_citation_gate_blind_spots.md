---
name: project_plan_citation_gate_blind_spots
description: the plan-citation gate's markdown/Kotlin parity gap is fixed; two other blind spots remain worth re-checking
metadata:
  type: project
---

As of commit `adfe1816`, `check_plan_refs` (doc-guard.sh) and `CheckDocBudgetsTask.kt`'s markdown scan both
check all three patterns — plan directory, `\bD[0-9]{1,2}\b`, and `\bS[0-9]{1,2}-[0-9A-Z]` — at parity with
the Kotlin-side checks (detekt's `ForbiddenComment` and doc-guard's `check_kotlin_comments`). The prior gap
recorded here (markdown checks missing the step-id pattern) is closed; `docs/reference/module-structure.md`'s
stray "S4-5b" was fixed in the same commit. Do not re-flag that gap — verify current state before citing it.

Two things the gate still does not (and structurally cannot, as scoped) catch:

1. **`config/detekt/detekt.yml` itself** still literally contains "S4-5b" and "S4-1" in its own `ForbiddenImport`
   rationale comments (around line 74/78, pre-existing since `adfe1816`, untouched since). Neither gate scans
   `.yml` — doc-guard's `is_permanent_doc` and `CheckDocBudgetsTask`'s `isPermanentMarkdownDoc` both only match
   markdown/specific paths, and `checkDocBudgets`'s `markdownFiles` input is `.md`-only. This is a real,
   live citation of a plan step in a permanent file, just outside the two gates' file-type scope — not a bug
   in this round's fix, but don't assume "permanent config" is covered.

2. **`check_kotlin_comments`' trailing-comment extraction is a naive substring split**: for a line not starting
   with `//`/`*`, it takes everything after the line's *first* `//` (or `/*`) as "the comment", with no string-
   literal awareness. A code line with a URL in a string (`"https://…/D17"` or similar) would have its `//`
   consumed as the split point and the remainder checked against the citation patterns — a false "adds a
   comment citing…" deny on code that has no comment at all. No live instance found in this codebase as of
   this check (`grep` for `https?://` near a `D[0-9]` / `S[0-9]-` token turned up nothing), so this is a latent
   trap, not a current failure — re-grep before flagging it as live.

**Why:** re-verify gate state fresh each time rather than trusting a saved "gap" memory — the gap itself gets
fixed between reviews, and citing a fixed gap as current would be an outdated-memory mistake. [[normative-rules-are-not-negotiable]]

**How to apply:** when reviewing a change to this gate, re-run both parity checks (patterns, and permanent-path
lists) fresh; don't assume last review's list of gaps still holds. Grep for `https?://` + citation-pattern
collisions before calling the trailing-comment risk live.
