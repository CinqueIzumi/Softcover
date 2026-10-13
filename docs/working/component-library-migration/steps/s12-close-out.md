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

Walkthrough findings, 2026-10-13. Rows tagged `PF Sn` / `PF On` come from the premium-feel findings and are
approved by this walkthrough.

- [x] **Library, bulk move offers the current shelf.** On Want to Read, the selection actions include
  "Move to Want to Read". Leave the shelf being viewed out of the bulk move options.
- [x] **Library, selection count lags the cards.** The card border and checkmark update immediately; the
  "N selected" count springs (`StatNumber`); now `StatNumberMotion.Roll`.
- [x] **Library, control-line affordances snap.** (PF S6, O33) The sort-direction arrow swaps glyphs; rotate
  it instead, and rotate the shelves chevron while the sheet is open. The chip, pill and drag handles appear
  via `if` (`LibraryControlLine.kt`, `BookList.kt`); expand or fade them in.
- [x] **Library, shelf title lags the pager.** (PF S4) The title changes at the swipe's midpoint. Drive it
  from the pager offset.
- [x] **Shelf sheet, padding under the "Create new list" divider.** Padding between the divider and the
  content leaves a dark band, so content looks clipped by a padded block instead of scrolling up behind the
  divider. Move the padding inside the scrolling content.
- [ ] **Explore, "Because you read" has no skeleton and rows jump.** The genre-based feed loads with no
  skeleton, and late rows push others down. Add a skeleton, and animate placement so rows move to their new
  spot when the order or content changes.
- [ ] **Explore, Up-next rail jumps on hide and undo.** (PF S29) Hiding a series or book snaps the rest of
  the rail into place, and undo snaps it back. Animate both.
- [ ] **Explore, featured card's "Added to library" button.** (PF S30) Once the book is in the library, the
  primary button reads "Added to library", a status rather than an action. Morph it into a button that
  removes the book from the library.
- [ ] **Explore, rail covers are blank while loading.** Fast-scrolling a rail (e.g. "What everyone's
  reading") shows empty cover slots. Show a shimmer until the image loads; if this goes in the shared cover
  image, make it opt-in unless every screen should get it.
- [ ] **Search, removed rows snap away.** (PF S31) Removing a recent search, or a hidden suggestion
  (`HiddenSuggestionsContent.kt` `HiddenGroupSection`), makes the remaining rows jump into place. Fade and
  collapse the removed row, then crossfade to the empty state.
- [ ] **Book detail, review "Show more" snaps open.** (PF S21) Expanding a review jumps to full height.
  Animate the height change.
- [ ] **Book detail, shelving fill is choppy.** Adding to or switching shelves doesn't play one smooth fill
  from start to end. Profile it first: dropped frames, recompositions or redraws mid-fill, or the animation
  being cancelled and restarted (e.g. by an optimistic state followed by the synced state). Then fix the cause.

## S12-5 — final gate and directory deletion

- `scripts/gradle-quiet.sh check` green, including `styleCheck` and `ktlintCheck` (G6).
- Delete this directory in the same PR, per the lifecycle note in `README.md`.
- Remove this directory's line from `docs/working/ACTIVE.md`.
