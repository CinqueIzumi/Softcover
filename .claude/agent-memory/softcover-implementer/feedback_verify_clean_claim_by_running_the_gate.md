---
name: feedback-verify-clean-claim-by-running-the-gate
description: A brief's "repo was just cleaned, no violations should remain" claim for a new gate still needs the gate run, not just a manual grep, before trusting it
metadata:
  type: feedback
---

When a brief says a codebase is already clean of the thing a new gate will forbid, treat that as a hypothesis,
not a fact — write the gate, then run it (or an equivalent grep with the exact final regex) over the whole
tree before Verify, and fix what it finds.

**Why:** building the plan-citation gates for [[softcover-implementer]], the brief said the repo had already
been hand-cleaned of `docs/working/` / decision-number / plan-step citations in comments, "confirm with a
grep". A grep with a hand-written pattern missed real violations that the actual detekt/hook regex caught:
a committed `BookDao.kt` comment citing `docs/working/architecture-review.md`, an uncommitted `WhenReadRow.kt`
KDoc citing plan step `S6-1`, and `DocBudgetsConventionPlugin.kt` KDoc citing `docs/working/ACTIVE.md` (forbidden
for Kotlin even though the hook's markdown-specific rule allows top-level `docs/working/*.md` files — the two
mechanisms are intentionally not symmetric).

**How to apply:** after wiring a new comment/doc-content gate, grep the whole tree (`find … -name "*.kt"`,
excluding `build/` and vendored dirs) with the *actual* patterns going into the gate before running
`styleCheck`/`checkDocBudgets`, not a looser ad hoc pattern. Expect and budget time to fix 1-2 genuine hits
even when the brief asserts zero.
