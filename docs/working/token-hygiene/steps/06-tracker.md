# Step 06 — Migration tracker cleanup (D1)

**Branch:** migration. **Delegation:** two phases, because the promotion decisions need the user.

`docs/working/component-library-migration.md` is 174KB / ~2,200 lines. It was read 55 times, and its
resume step says "read this file top to bottom". About 1,200 lines are the per-stage outcome write-ups
(§ 5b–5n).

## Phase 1 — audit (read-only agent: `general-purpose` or Explore, "very thorough")

Brief:
- For each of § 5b–5n and § 6a, list every **rule, decision or open item** it states (one line each).
- For each item, grep `docs/reference/design-system/component-contract.md`, the § 7 checklists and
  Appendix A, and mark it `present` (with the location) / `missing` / `obsolete`.
- Report: one table, ≤300 words.

Show the `missing` rows to the user. **They approve** which ones get promoted, and where (usually
`component-contract.md`, or the relevant § 7 checklist as an unchecked item).

## Phase 2 — rewrite (`softcover-implementer`)

The target structure, with a line budget per section:

| Section | Budget | Source |
|---|---|---|
| Lifecycle note + `## Now` | ≤45 | replaces the current long **Status** paragraph |
| Local verification caveats | ≤15 | condensed, plus the `gradle-quiet` note from Step 02 |
| § 1 Goal, § 2 Baseline (numbers only), § 4 contract pointer | ≤60 | keep |
| § 3 Target module shape | ≤150 | keep; shorten only the prose |
| § 5 Stages | ≤120 | the stage table/checkboxes; **§ 5b–5n deleted** after the approved promotions |
| § 5a Gallery decision | ≤10 | decision only |
| § 6 Gates | ≤60 | the checklist; § 6a reduced to its still-open items |
| § 7 Family checklists | as is (~330) | keep, and **add two items to every S5–S9 family**: "move the family's section files into `:core:component` (Step 08 split them in place)" and "move the family's doc (`components/<family>.md`) into KDoc and delete it (Step 07)" |
| § 8 Risks | as is | keep |
| § 9 How to resume | ≤8 | "Read `## Now` and the one section it names. `git log --oneline main..HEAD`. Pick up Next." |
| Appendix A Decisions | ≤40 | keep, one line per decision |
| Appendix B (copy of the GitHub issue) | 0 | delete; the issue link at the top is enough |

Also:
- Check the two S5 working plans (`s5-1-chips-plan.md`, `s5-primitives-substages.md`). **Ask the user**
  whether `s5-1-chips-plan.md` can be deleted, given S5-1's state after Step 00. Keep
  `s5-primitives-substages.md` as the S5 detail, named from `## Now` only while S5 is in progress.
- Fix every reference to a deleted section (`grep -n "§ 5[b-n]" docs/ core feature`) so it points at
  where the fact now lives.

## Acceptance

- The tracker is ≤ ~900 lines; `## Now` is ≤40 lines.
- Every approved promotion is present at its new location.
- `grep` finds no dangling `§ 5b`–`§ 5n` references.
- A fresh session on the migration branch can name the next S5 action from the injected Now block alone.
