---
paths:
  - "**/*.md"
---

# Writing docs

- **Doc types.** `docs/reference/**` holds present-tense rules and facts only, with no history or
  justification. `docs/working/**` holds state (a `## Now` block) plus checkboxes, and one line per decision.
  KDoc follows the `document-code` skill. Agent memory follows the keep / promote / never-save criteria in each
  `softcover-*` agent's `## Memory` section. `docs/rhaydus/**` is vendored; never edit it.
- **Where content goes:**

  | Content | Goes to |
  |---|---|
  | Component contract | KDoc on its UI model |
  | Component existence and role | one line in `docs/reference/design-system/components.md` |
  | Screen or recipe behaviour | `docs/reference/design-system/patterns/<surface>.md` (one line in `patterns.md`) |
  | Build gotcha | `.claude/rules/build-wiring.md` |
  | Plan decision binding every step | one line in the plan README's `## Decisions` |
  | Approved shape for one step | that step file's `## Approved shape`, deleted with it |
  | Why a change was made | the PR description (never a reference doc) |
  | Reviewer or agent learning | that agent's memory |
  | Something the foundation should adopt | `docs/working/foundation-upstream-candidates.md` |

- **Rewrite, never append.** Rewrite the entry to the current truth and delete the sentence it replaces. Never
  add a change note ("that replaced…", "previously…"). One fact lives in one place; link to it elsewhere.
- **Plans are temporary.** A plan directory under `docs/working/` is deleted when the plan finishes, so code,
  KDoc, permanent config (`config/**`, `*.yml`/`*.yaml`, `*.kts`) and permanent docs never cite one, its steps
  or its decision numbers. A decision is deleted once built; the code is then the record and the reason goes in
  the PR description. Enforced by detekt's `ForbiddenComment` and `doc-guard.sh` at write time, and by
  `checkDocBudgets` in CI.
- **Entry lengths:** a decision is one line; an index line is one sentence; a pattern or component entry is ≤12 lines; a `## Now`
  block is ≤40 lines.
- **Budgets.** Every file has a size budget in `docs/doc-budgets.txt`. A file over budget may shrink but never
  grow. `.claude/hooks/doc-guard.sh` enforces this at write time and `checkDocBudgets` enforces it in CI.
  Changing a budget is a deliberate, reviewed diff, never a way past the gate.
- **Edit markdown with Edit / Write only**, never Bash redirection, heredocs or scripts, so the write-time
  hook sees every change.
