# Step 07 — Design-system docs restructure (D2)

**Branch:** migration. Four sub-steps, **one session each**. The `CLAUDE.md` maintenance rule applies
throughout: this changes the docs' shape, not the design system, so there is no code change and there
must be no loss of meaning.

Today: `components.md` is 99KB (59 entries, one section, no subheadings), `patterns.md` is 88KB (~55
entries, one section), and `component-contract.md` is 26KB (read 27 times). Entries mix the rule, its
anatomy and its history.

## Rules for all sub-steps

- **Keep:** what the thing is, when to use it, its anatomy and behaviour rules.
- **Delete:** history and justification ("replaced…", "that used to…", "three parallel derivations…"),
  and restated cross-references.
- Anything normative that looks like history → keep, and flag it in the report.
- All KDoc goes through the `document-code` skill standard (global rule).

## 07a — `components.md` → index + per-family files (`softcover-implementer`)

- `docs/reference/design-system/components.md` becomes an **index**: one line per component,
  `**Name** — \`path/or/package\` — one-sentence role`, grouped by family (navigation & chrome, buttons &
  controls, chips, covers, states & feedback, sheets, statistics, share, rich text, editorial, platform
  helpers). About 60 lines.
- Components **not yet in `:core:component`** → the full entry moves to
  `design-system/components/<family>.md`, cleaned per the rules above; the index line links to it.
- Components **already in `:core:component`** (packages `badge callout celebration chip control cover
  lists progress richtext share sheet state statistic topbar verdict`) → the full entry is **parked**,
  cleaned, in `components/<family>.md` for 07d to turn into KDoc. The index line points at the package.
- Update the `design-system.md` index.

## 07b — `patterns.md` → per-surface files (`softcover-implementer`)

- Split into `design-system/patterns/` by surface: `editorial.md` (editorial section, hero stat, quote,
  standfirst…), `library.md`, `book-detail.md`, `reading.md`, `settings.md`, `explore-profile.md`,
  `cross-cutting.md` (reserved-row card, tonal grouping, adaptive empty state, list–detail two-pane,
  drag-to-reorder, bulk select…).
- `patterns.md` becomes an index: one line per pattern, with its file.
- Many entries are really screen specs ("Shelves sheet (Library, mobile)"); keep them, in their surface's
  file.

## 07c — `component-contract.md` compaction (main conversation or implementer)

- Restructure it as a **rules block first**: R1–R10 plus the build gates, one short paragraph each, with a
  worked example only where the rule can't be understood without one. Rationale goes last, under a
  heading marked "optional reading", and only where it prevents a known wrong turn.
- **Normative content is not negotiable:** every rule survives with the same meaning. Produce a
  before/after rule list for the user to approve.

## 07d — KDoc for components already migrated (`softcover-implementer`, batches of ~4 packages)

- For each parked entry: move the contract (what the model fields mean, variant semantics, the event
  surface) into KDoc on `<X>UiModel` / `<X>Event` / the composable, following `document-code`. Anatomy
  and styling that only a designer needs stay as ≤10 lines in the family file, or are dropped if the
  gallery shows them.
- Delete each parked entry once it has moved; delete a family file once it is empty.
- Verify each batch: `scripts/gradle-quiet.sh :core:component:compileKotlinJvm :core:component:ktlintCheck`.
- Review: one `softcover-reviewer` pass over the whole of 07d (documentation-only scope).

## Acceptance

- `components.md` and `patterns.md` are each ≤ ~8KB; no family or surface file is over ~20KB.
- `component-contract.md` has its rules block first, and the user approved the before/after list.
- Every `:core:component` UI model has KDoc; no parked entries are left.
- The § 7 checklists (Step 06) carry the "move the family doc into KDoc" item for families not yet
  migrated.
- `grep -rn "components.md#\|patterns.md#" docs .claude` → no broken anchors.
