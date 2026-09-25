---
name: doc-guard-history-regex-false-positives
description: doc-guard.sh's no-history regex flags innocent phrasing like "used to surface X"; also, pinned-budget files need byte-diff-ordered edits
metadata:
  type: feedback
---

`.claude/hooks/doc-guard.sh`'s `check_history` denies any new line under `docs/reference/**` matching
`\b(used to|previously|no longer|was (changed|replaced|renamed)|we decided|(that|this|which) replaced|originally)\b`,
case-insensitively, on the whole line (docs/reference paragraphs are one unwrapped line each).

**Why:** it is a blunt substring match, not semantic. "Used to surface notes" (used *in order to*
surface) trips the same rule as "used to be this section's tail" (genuine history). Any wording that
happens to contain one of these phrases as ordinary English — not just as a change-note — gets denied.

**How to apply:** before writing/editing a `docs/reference/**/*.md` file, grep your draft against that
exact pattern first (draft in a scratch `.txt`, not the target `.md`, so the write-time hook doesn't
also block the scratch write via the bash-redirect guard). Reword to dodge the trigger rather than
reaching for the `<!-- history-ok -->` escape hatch, unless the sentence is genuinely load-bearing
history worth flagging to the user (e.g. "X was tried and abandoned because Y" explaining a real
trap) — then keep it and mark it explicitly.

Separately: a file pinned at (or near) its exact byte budget in `docs/doc-budgets.txt` (e.g.
`foundations.md`) rejects any single `Edit` whose *resulting* file size exceeds the budget, even if
the net change across several edits would land under it — each `Edit` is checked independently and
sequentially. Compute byte diffs for every planned change up front (`printf '%s' "$str" | wc -c`,
never `python -c`/heredoc touching `.md` — that also trips the guard's bash-command matcher), then
apply negative/zero-diff edits first to build headroom before applying positive-diff ones.
