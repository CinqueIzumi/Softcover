---
name: feedback_check_working_tree_before_starting
description: Always inspect git status/diff before touching a feature — uncommitted prior work may already violate the current brief's scope guard
metadata:
  type: feedback
---

Before making any edits, run `git status` and `git diff --stat` on the target feature. Do not assume
a clean starting point just because the task description implies a fresh pass.

**Why:** uncommitted prior work can already violate the current brief's explicit scope guard (e.g. it
adds a new persisted field, a new use case, or deletes something the brief says to keep). Building on
top of it silently ships whatever it violated; the implementer never touches git history, so it cannot
be discarded unilaterally either.

**How to apply:** When resuming or starting logic work on a feature:
1. `git status --short` and `git diff --stat -- <touched-paths>` first, always — even if the task
   framing suggests "implement X" rather than "continue X."
2. If uncommitted changes already exist in scope and conflict with the current brief's constraints,
   don't silently build on top of them. Inspect what is there, then report the discovery and what it
   violates back to the caller instead of stashing or discarding it — leave the decision to keep,
   rebase onto, or drop that work to the user.
3. Report the discovery plainly in the final summary — don't bury it.
