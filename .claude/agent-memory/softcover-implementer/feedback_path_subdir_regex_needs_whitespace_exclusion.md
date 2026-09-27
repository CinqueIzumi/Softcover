---
name: feedback-path-subdir-regex-needs-whitespace-exclusion
description: A `dir/[^/]+/` style regex for "cites a subdirectory of dir/" false-positives across an entire prose line/paragraph when it doesn't also exclude whitespace
metadata:
  type: feedback
---

A regex meant to catch "text references a subdirectory of `docs/working/`" (or any similar
`base/<subdir>/` shape) must exclude whitespace from the subdir group, not just `/`. `docs/working/[^/]+/`
against a full markdown line will happily match across unrelated later content — e.g. `docs/working/ACTIVE.md`
… `/handoff` on the same line matches, because `[^/]+` swallows every word and space between them looking for
the next slash.

**Why:** hit this exact false positive writing the plan-citation gate for [[softcover-implementer]] — a
`CheckDocBudgetsTask.kt` Kotlin `Regex("""docs/working/[^/]+/""")` flagged a legitimate `CLAUDE.md` line citing
`docs/working/ACTIVE.md` (an allowed top-level file) purely because the same line later contained `/handoff`.
The parallel bash implementation in `doc-guard.sh` did not have the bug, because it was written with
`[^/[:space:]]+` from the start.

**How to apply:** any time a rule needs "cites a path under `X/`", write the middle character class as
`[^/\s]+` (or the ERE equivalent `[^/[:space:]]+`), never bare `[^/]+`, whether the check runs per-line or
over a whole file/paragraph. Test it against a fixture line that has an unrelated slash later in the same
line/paragraph, not just against the exact string you're trying to catch.
