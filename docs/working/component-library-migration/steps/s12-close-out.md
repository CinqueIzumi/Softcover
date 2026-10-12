# S12 — Close out

**Stage:** S12 — final gate values, doc rewrite, and deleting this directory. Starts once PF-7
has landed. **Delegation:** user
(Phase 1 items), `softcover-implementer` (Phase 2 items).

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S12-2 | The `docs/reference/design-system/` rewrite, the `foundations.md` budget restore, the `CLAUDE.md` parenthetical | [x] |
| S12-3 | The open decisions (cover radius, `"tnum"`), the fixture-size measurement, the gallery completeness pass | [x] |
| S12-4 | G7 — human walkthrough of every screen on device | [ ] |
| S12-5 | G6 (`check` green) and deleting this directory | [ ] |

## S12-3 — open decisions, fixture size, gallery

Done. Outcomes for the PR description:

- Cover radius: nothing to decide; every cover clips through `CoverVariant.shape()` to a `Corners` token.
- `"tnum"`: one `TextStyle.tabularFigures()` in `:core:designsystem`; 14 call sites moved.
- Fixture size: fixtures + gallery ≈ 62 KB of a 48 MB release dex (0.18%); release has R8 off.
- Gallery: every visual public component has fixtures and an entry; one `GalleryFamily` per package.
- Stability: `BookCardUiModel` and all its parts stable; every `BookCard*` composable skippable.
- Labelled-button icon double announcement: foundation entry F51 in `foundation-upstream-candidates.md`.

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
- **The accepted visual shifts** in [`visual-checks.md`](../visual-checks.md), plus the approved rows
  of `copy-audit.md`, already applied by PF-7.

Regressions found so far:

- [x] Book detail entry choppy — content flash, skeletons mismatched, cover not animating. Fixed in code:
  the screen model seeds the hero / backdrop / full-screen covers into the initial state
  (`withCoverModels()`), and `AppDispatchers.main` is `Main.immediate` so collector-derived state lands in
  the same frame. Device confirmation pending, plus a smoke pass of every screen for the dispatcher change.

## S12-5 — final gate and directory deletion

- `scripts/gradle-quiet.sh check` green, including `styleCheck` and `ktlintCheck` (G6).
- Delete this directory in the same PR, per the lifecycle note in `README.md`.
- Remove this directory's line from `docs/working/ACTIVE.md`.
