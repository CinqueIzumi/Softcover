# S12 — Close out

**Stage:** S12 — final gate values, doc rewrite, and deleting this directory. **Delegation:** user
(Phase 1 items), `softcover-implementer` (Phase 2 items).

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S12-1 | G4 `checkComponentBudget` | [ ] |
| S12-2 | The `docs/reference/design-system/` rewrite, the `foundations.md` budget restore, the `CLAUDE.md` parenthetical | [ ] |
| S12-3 | The open decisions (cover radius, `"tnum"`), the fixture-size measurement, the gallery completeness pass | [ ] |
| S12-4 | G6 (`check` green) and deleting this directory | [ ] |

## S12-1 — G4

Write `checkComponentBudget`: a Gradle task counting `@Composable` declarations outside
`:core:component` (excluding `*Preview` functions and platform `expect`/`actual`s). Set the ceiling to
the measured post-migration value; a rise fails the build. Wire into `check`.

## S12-2 — doc rewrite, budget restore, `CLAUDE.md`

- Rewrite `docs/reference/design-system/` (`components.md`, `patterns.md`, `component-contract.md`)
  against the finished library — every family's `[detail]` entries should already be gone per each
  step's Standard acceptance; this pass is the final sweep, not the first one.
- Restore `foundations.md`'s pinned budget in `docs/doc-budgets.txt` from the token-hygiene-gate value
  back to the generic `docs/reference/**/*.md 30KB`.
- Trim the `(where that file exists)` parenthetical in `CLAUDE.md`'s routing table — every component
  now has a contract, so the qualifier no longer applies.

## S12-3 — open decisions, fixture size, gallery

- Decide the cover-radius drift: `HiddenSeriesStack`'s 3dp vs. every other flat cover's 4dp; Reading's
  three thumbnails at 6/8/10dp.
- Decide the `"tnum"` token (~17 call sites today; one `:core:designsystem` token instead).
- Measure the fixture size in the release binary (`README.md` D6).
- Run the gallery completeness pass: every `:core:component` family has a `GalleryEntry`.
- The § 7.0 rich-text remaining row (`RichTextFormattingToolbar` owing R1) is closed by
  `steps/s8-controls.md` S8-4, not repeated here — confirm it landed before ticking this sub-step.

## S12-4 — final gate and directory deletion

- `scripts/gradle-quiet.sh check` green, including `styleCheck` and `ktlintCheck` (G6).
- Delete this directory in the same PR, per the lifecycle note in `README.md`.
- Remove this directory's line from `docs/working/ACTIVE.md`.
