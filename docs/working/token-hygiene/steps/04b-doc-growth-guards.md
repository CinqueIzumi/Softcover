# Step 04b — Doc-growth guards (approved, D11: ratchet)

**Branch:** tooling. **Depends on:** Step 04 (the rule files exist) and Steps 01–02 (hook patterns).
**Delegation:** main conversation for the contract; `softcover-implementer` for the gate task.

## How the docs grew (from the 2026-09-20 research)

1. **Append instead of rewrite.** The maintenance rule says "update the doc in the same change", so each
   change adds a sentence ("That replaced…") instead of rewriting the entry to the current truth.
2. **No doc types.** Rationale, history, state and rules all end up in the same file. The tracker became a
   stage diary (§ 5b–5n); catalogue entries became changelogs.
3. **No destination rule.** Nothing says where a kind of fact belongs, so the same fact lands in 2–3 places
   (the reviewer's `doc_drift_pattern` memory) or in the file that happened to be open.
4. **No budget.** Nothing fails when a file doubles.
5. **Writes bypass tools.** 125 main-conversation edits to `.md` files were done with Bash heredoc scripts,
   so any hook placed only on Edit/Write would miss them.

## Setup, in four layers

### 1. Contract — `.claude/rules/docs.md` (`paths: ["**/*.md"]`, ≤40 lines)

- **Doc types:**
  - `docs/reference/**`: present-tense rules and facts only; no history or justification.
  - `docs/working/**`: state (a `## Now` block) plus checkboxes; decisions one line each.
  - KDoc: the `document-code` skill.
  - Agent memory: the FU-7 criteria.
- **Where content goes:**

  | Content | Goes to |
  |---|---|
  | Component contract | KDoc on its UI model |
  | Component existence and role | one line in `components.md` |
  | Screen or recipe behaviour | `patterns/<surface>.md` |
  | Build gotcha | `.claude/rules/build-wiring.md` |
  | Migration decision | one line in the tracker's Appendix A |
  | Why a change was made | the PR description / commit (never a reference doc) |
  | Reviewer or agent learning | agent memory |
  | Something the foundation should adopt | `foundation-upstream-candidates.md` |

- **Editing rules:** rewrite the entry to the current truth and delete the sentence it replaces; never
  append a change note; one fact in one place, referenced elsewhere by link; stay within the entry-length
  limits (index line: 1 sentence; pattern / component entry: ≤12 lines; Now block: ≤40 lines).
- Edit markdown with Edit/Write only, never Bash heredocs or scripts, so the write-time hook sees every
  change.
- Also reword the `CLAUDE.md` design-system maintenance rule: "update" → "rewrite the affected entry to
  the current truth".

### 2. Budgets — `docs/doc-budgets.txt` (checked in)

One line per glob and its limit, for example:

```
CLAUDE.md                                   8500B
.claude/rules/*.md                          40L
docs/reference/design-system/components.md  8KB
docs/reference/design-system/components/*.md 20KB
docs/reference/design-system/patterns/*.md  20KB
docs/reference/**/*.md                      30KB
docs/working/**/*.md                        40KB
.claude/agent-memory/*/MEMORY.md            40L
## Now blocks                               40L
```

Changing a budget is a deliberate diff the reviewer sees; it is never done silently to get past the gate.

### 3. Write-time hook — `.claude/hooks/doc-guard.sh` (PreToolUse, matcher `Edit|Write`)

- For `*.md` targets, compute the resulting file (for Write, the content; for Edit, apply the replacement
  in memory).
- **Ratchet (D11):** a file already over budget may be edited only if the edit does not grow it; a
  file under budget may not be pushed over it.
- **Deny** with a reason when the result breaks the budget or the ratchet, naming the budget and the size and
  suggesting where the content should go instead (from the routing table).
- **Deny** when an added line in `docs/reference/**` matches history phrasing (`\b(used to|previously|no
  longer|was (changed|replaced|renamed)|we decided|that replaced|originally)\b`), unless the line carries
  `<!-- history-ok -->`, which is visible in review. The phrase list lives in the script, in one place.
- Also: a Bash command that writes to a `.md` path (redirection or a script with `.md` in the command) is
  denied with "edit markdown with Edit/Write". This goes into the existing Bash hook chain.

### 4. Backstop gate — `checkDocBudgets` (root `build.gradle.kts`, wired into `check`)

- Checks the same budgets file, and the Now-block limit for every file listed in `docs/working/ACTIVE.md`.
- CI catches whatever the hook missed (non-Claude edits, other tools).

### Plus

- The `softcover-reviewer` review-only checklist gains: a reference-doc diff that adds history, a fact
  duplicated in a second place, or content in the wrong file per the routing table → 🟡.
- **Optional:** a `/write-docs` skill mirroring `document-code` for markdown (type → destination →
  rewrite, not append → budget check). Only worth it if the rule file plus the hook prove insufficient;
  decide in Step 09.

## Acceptance

- An Edit that grows the already over-budget `components.md` is denied; one that shrinks it passes.
- An Edit that would push a file under budget `components.md` past its budget is denied with the routing suggestion.
- An added line "This replaced the old X" in `docs/reference/` is denied; with `<!-- history-ok -->` it
  goes through.
- `echo x >> docs/reference/foo.md` via Bash is denied.
- `scripts/gradle-quiet.sh checkDocBudgets` passes on the tree after Steps 06–07. (If it runs before them,
  the budgets for the tracker and design docs are introduced in Step 07's final session instead, still
  with no baseline.)
- `foundation-upstream.md` FU-12 → "done locally".
