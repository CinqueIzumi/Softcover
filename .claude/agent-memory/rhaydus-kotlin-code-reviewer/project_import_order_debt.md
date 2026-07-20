---
name: project_import_order_debt
description: The hand-authored style guide mandates Android→third-party→nl.rhaydus import grouping; ktlint does NOT enforce it (standard ruleset disabled). Deviations are real violations — FIX them wherever found, not only on touched files.
metadata:
  type: project
---

The style guide (docs/rhaydus/0.3.0/code-style.md §Import Ordering, lines 501-505) **mandates**:
1. Android / AndroidX, 2. third-party, 3. project (`nl.rhaydus.*`). A project import placed above the
Android block is a genuine violation of the hand-authored guide.

**ktlint does NOT catch this.** Verified 2026-07-13: `.editorconfig` sets `ktlint_standard = disabled`,
so ktlint's standard ruleset (which contains the `import-ordering` rule) is deliberately off; only the
custom `nl.rhaydus:ktlint-rules` runs, and it has **no** Android-vs-third-party-vs-project grouping
rule (its project-import rule only orders alphabetically *within* the `nl.rhaydus.*` group). `ktlintCheck`
passes (exit 0) and `ktlintFormat` does nothing on files with the mis-grouped import. So this rule is
enforced by the guide + human review ONLY — never claim `ktlintCheck` will catch it.

**How to apply (owner's rule, stated 2026-07-13):** "if the hand-authored style guide says something,
and non-touched code deviates, change the old code to match the guide — 'it already exists elsewhere'
is a lazy excuse." So do NOT limit this to touched files and do NOT treat existing occurrences as
accepted precedent. Flag/fix the grouping wherever it deviates. Because no tool surfaces it, read the
full import block yourself. (The three instances found in the "Hidden suggestions" review —
`ExploreShelf.kt`, both `SettingsScreenLayout` actuals — were fixed 2026-07-13.)

**Bulk-rewrite corollary (modularization steps):** when a `feature.*` import is mechanically replaced
by its `core.*` equivalent, the replacement tends to land *in the old import's line position* rather
than re-sorted — leaving `core.*` sitting below `feature.*` in the project group. Within the
`nl.rhaydus.*` group the required order is alphabetical, so `core.*` sorts before `feature.*`; the
ktlint project-import rule does order alphabetically within that group, but a review should still
verify the whole block was re-sorted after any rewrite rather than trusting the diff's line-for-line
substitution. This appeared in 26 files during modularization step 3.
