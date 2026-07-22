---
name: check-working-tree-before-starting
description: Always inspect git status/diff before touching a feature — uncommitted prior work may already violate the current brief's scope guard
metadata:
  type: feedback
---

Before making any edits, run `git status` and `git diff --stat` on the target feature. Do not assume
a clean starting point just because the task description implies a fresh pass.

**Why:** On the Library redesign task (2026-07-17), the working tree already contained a substantial
uncommitted prior attempt at the same brief — but it violated an explicit "IMPORTANT scope guard"
(it deleted `LibraryGridLayout`, added a new `LibraryLayout` enum, added a `showTitles` boolean to
`AppSettingsEntity`/DataStore, and added new preference use cases) despite the brief saying "no new
persisted field, no new use case, KEEP the existing enum." This looked like a previous agent run (or
an earlier part of the same session) that either used stale instructions or ignored the constraint.
Had I built on top of it without checking, I'd have shipped a domain/preferences migration the task
explicitly forbade.

**How to apply:** When resuming or starting logic work on a feature:
1. `git status --short` and `git diff --stat -- <touched-paths>` first, always — even if the task
   framing suggests "implement X" rather than "continue X."
2. If uncommitted changes already exist in scope and conflict with the current brief's constraints,
   don't silently build on top of them or silently discard them. Stash with a descriptive message
   (`git stash push -u -m "..."`) so the work is recoverable, confirm the tree is clean, then rebuild
   cleanly against the current instructions.
3. Before discarding/stashing, skim the stashed diff for anything salvageable and *compliant* with
   the current brief (in this case, three small action files and a pure sort-gating helper were
   clean and reusable even though the surrounding migration wasn't) — `git show 'stash@{0}^3:<path>'`
   reaches untracked-file content inside a stash.
4. Report the discovery and what was done about it plainly in the final summary — don't bury it.

See also [[feedback_no_hardcoded_paths]] for how paths should be written when this kind of note
references specific files.
