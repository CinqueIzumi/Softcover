---
name: architecture_dual_gate_predicate_parity
description: doc-guard.sh and CheckDocBudgetsTask.kt implement the same predicate twice; verify both edges agree and that widening an unconditional scan doesn't retroactively fail untouched files
metadata:
  type: architecture
---

`doc-guard.sh` (write-time hook, bash `case`) and `CheckDocBudgetsTask.kt` (CI gate, Kotlin `startsWith`)
duplicate several predicates (`is_permanent_doc` / `isPermanentMarkdownDoc`, glob-to-regex, first-match-wins
budget lookup). When a change touches one, confirm the other changed identically — a bash `case` pattern's
`*` already matches `/` (no special pathname treatment in case matching), so `.claude/agent-memory/*` and
Kotlin's `startsWith(".claude/agent-memory/")` are the same recursive match; don't assume they need a `**`
variant.

Two gate shapes behave differently under a widened predicate:
- **Ratcheted checks** (size budgets) only fail a file that grew since the merge-base, so widening the glob
  is safe even for untouched pre-existing files over budget — they're grandfathered.
- **Unconditional checks** (the plan/decision/step-citation scan in `CheckDocBudgetsTask.kt`) run against a
  file's *current* full content every time, with no ratchet. Widening `isPermanentMarkdownDoc` to a new
  directory means every existing, untouched file under it is now scanned. Grep the whole newly-covered tree
  for the forbidden pattern before trusting a green gate — a currently-passing gate only proves today's
  content is clean, not that the predicate change is safe in general.

Related: [[project_hook_script_not_registered]] (same family: a gate's wiring is not obvious from reading one
side of it).
