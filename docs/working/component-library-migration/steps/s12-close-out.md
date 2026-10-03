# S12 — Close out

**Stage:** S12 — final gate values, doc rewrite, and deleting this directory. **Delegation:** user
(Phase 1 items), `softcover-implementer` (Phase 2 items).

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S12-1 | G4 `checkComponentBudget` | [ ] |
| S12-2 | The `docs/reference/design-system/` rewrite, the `foundations.md` budget restore, the `CLAUDE.md` parenthetical | [ ] |
| S12-3 | The open decisions (cover radius, `"tnum"`), the fixture-size measurement, the gallery completeness pass | [ ] |
| S12-4 | G7 — human walkthrough of every screen on device | [ ] |
| S12-5 | G6 (`check` green) and deleting this directory | [ ] |

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
- Re-pin `components.md`'s budget in `docs/doc-budgets.txt` from 10KB (headroom for the S7–S9 index
  lines) to its post-rewrite size rounded up to the next KB.
- Trim the `(where that file exists)` parenthetical in `CLAUDE.md`'s routing table — every component
  now has a contract, so the qualifier no longer applies.

## S12-3 — open decisions, fixture size, gallery

- Decide the cover-radius drift: `HiddenSeriesStack`'s 3dp vs. every other flat cover's 4dp; Reading's
  three thumbnails at 6/8/10dp.
- Decide the `"tnum"` token (~17 call sites today; one `:core:designsystem` token instead).
- Measure the fixture size in the release binary.
- Run the gallery completeness pass: every `:core:component` family has a `GalleryEntry`.
- Check the Compose compiler metrics report: `BookCardUiModel` and its parts are stable.
- The § 7.0 rich-text remaining row (`RichTextFormattingToolbar` owing R1) is closed by
  `steps/s8-controls.md` S8-4, not repeated here — confirm it landed before ticking this sub-step.

## S12-4 — G7, human walkthrough

**Delegation:** the user, on a real Android device and on desktop. Not delegable to an agent; no
gate or gallery check stands in for it. Walk every screen and every navigation path, comparing against
`main`, and log each regression as a fix item here before S12-5:

- **Navigation and transitions:** every tab, every route into book detail (library, explore rails,
  search, reading, profile, lists), and back. The cover's shared-element animation lands on its detail
  spot; no content flash, jump or choppy frame on enter or exit.
- **Loading:** skeletons match the layout that replaces them (count, size, position); no layout shift
  when data lands; no empty frame before a skeleton.
- **Animations:** press feedback, sheet open / animated close, mark-as-read, progress, list reorder,
  selection mode, error shake.
- **Every sheet and dialog:** opens with content on the first frame, scrolls, dismisses by tap, swipe and
  back.
- **States:** empty, error, offline and loading on every screen; long titles, missing covers, no data.
- **Layouts:** compact, medium and expanded widths; light and dark; each palette; larger font scale.
- **The accepted visual shifts** listed in [`visual-checks.md`](../visual-checks.md).

Regressions found so far:

- [x] Book detail entry choppy — content flash, skeletons mismatched, cover not animating. Fixed in code:
  the screen model seeds the hero / backdrop / full-screen covers into the initial state
  (`withCoverModels()`), and `AppDispatchers.main` is `Main.immediate` so collector-derived state lands in
  the same frame. Device confirmation pending, plus a smoke pass of every screen for the dispatcher change.

## S12-5 — final gate and directory deletion

- `scripts/gradle-quiet.sh check` green, including `styleCheck` and `ktlintCheck` (G6).
- Delete this directory in the same PR, per the lifecycle note in `README.md`.
- Remove this directory's line from `docs/working/ACTIVE.md`.
