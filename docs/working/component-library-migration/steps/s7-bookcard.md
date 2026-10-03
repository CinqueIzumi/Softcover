# S7 — `BookCard`

**Stage:** S7 — the main event, and the risk concentration point. **Delegation:** user (Phase 1),
`softcover-implementer` (Phase 2).

21 declarations across five features collapse onto one `BookCard`. Several participate in
shared-element transitions via `bookCoverTransitionKey`; several sit inside selection modes and lazy
grids where a stability regression is a dropped frame, not a compile error.

After S7-1, the user checks the `BookCard` variants in the Component Gallery before any call site moves.

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S7-1 | `BookCard` models, the component and the fixtures | [x] |
| S7-2 | The library grid and cover-only cells, plus the selection covers | [x] |
| S7-3 | The library rows and dispatchers | [x] |
| S7-4 | Explore rails, featured, series and mood tile | [x] |
| S7-5 | Explore search and hidden suggestions | [ ] |
| S7-6 | Reading | [ ] |
| S7-7 | Profile, book_detail's `EditionItem` and `StackedJackets`, plus the final shared-element and skippability verification | [ ] |
| S7-C | Convergence pass over the family (family-procedure.md § Phase 3) | [ ] |

## Variant mapping (current paths)

| Source | Target variant |
|---|---|
| `GridBookCell` — `feature/library/presentation/screen/section/GridBookCell.kt:108` | `Grid` |
| `CoverOnlyCell` — `feature/library/presentation/screen/section/GridBookCell.kt:79` | `CoverOnly` |
| `CompactRow` — `feature/library/presentation/screen/section/CompactRow.kt:25` | `Row(Compact)` |
| `LargeRow` — `feature/library/presentation/screen/section/LargeRow.kt:43` | `Row(Large)` |
| `LayoutBookEntry` — `feature/library/presentation/screen/section/LayoutBookEntry.kt:36` | dispatcher → replaced by `BookCard` |
| `LayoutEditionEntry` — `feature/library/presentation/screen/section/LayoutEditionEntry.kt:12` | dispatcher → replaced by `BookCard` |
| `LibraryGridCover`, `SelectableCover` — `LayoutBookEntry.kt:178`, `section/SelectableCover.kt:34` | → `Cover` component |
| `DiscoveryRailCard` — `feature/explore/presentation/screen/section/DiscoveryRailCard.kt:45` | `Rail` |
| `FeaturedCard` — `feature/explore/presentation/screen/section/FeaturedCard.kt:53` | `Featured` |
| `TrendingCard` — `feature/explore/presentation/screen/section/DiscoveryRailCard.kt:145` | `Rail` |
| `BecauseYouReadCard` — `feature/explore/presentation/screen/section/DiscoveryRailCard.kt:216` | `Rail` |
| `SeriesCard`, `UnreleasedSeriesCard` — `feature/explore/presentation/screen/section/SeriesCard.kt:45,168` | `Rail` + series badge |
| `SearchResultRow` — `feature/explore/presentation/screen/section/SearchResultRow.kt:46` | `Row(Compact)` |
| `MoodTile` — `feature/explore/presentation/screen/section/MoodGrid.kt:127` | `Tile`; its model takes the label title-cased by `toTitleCaseWords`, as `MoodChipsSnapshot` does since S5-3a; `MoodTileDimensions` (S5-8) folds into the `Tile` metrics |
| `HiddenBookRow`, `HiddenSeriesRow` — `feature/explore/presentation/screen/HiddenSuggestionsShelf.kt:238,324` | `Row(Compact)` + restore trailing |
| `SeriesCoverStack` — `feature/explore/presentation/screen/HiddenSuggestionsShelf.kt:418` | → `Cover(stacked)` |
| `FeaturedBookCard` — `feature/reading/presentation/screen/section/FeaturedBookCard.kt:36` | `Featured` |
| `FeaturedBackdropCard` — `feature/reading/presentation/screen/section/FeaturedBackdropCard.kt:68` | `Featured(backdrop = true)` |
| `FeaturedCover` — `feature/reading/presentation/screen/section/FeaturedBackdropCard.kt:238` | → `Cover` |
| `CompactBookEntry` — `feature/reading/presentation/screen/section/CompactBookEntry.kt:63` | `Row(Compact)` |
| `PickUpNextTile` — `feature/reading/presentation/screen/section/PickUpNextSection.kt:62` | `Tile` |
| `LovedBookCard` — `feature/profile/presentation/screen/section/RecentlyLovedSection.kt:87` | `Rail` |
| `EditionItem` — `feature/book_detail/presentation/component/EditionBottomSheetSelector.kt:274` | `Row(Large)` + selected state |
| `StackedJackets` — `core/component/lists/ChooseListsBottomSheet.kt:224` | → `Cover(stacked)` |
| `CoverOverlay` — `core/component/badge/CoverOverlay.kt:22` | deadline-overlay box; decide in S7-1 whether it folds onto `CoverUiModel` |

`Cover` + `CoverUiModel` + `CoverVariant` (21 entries) + `CoverDimensions` — already done in S4-4, with
`CoverMapper` / `CoverSourceResolver` in `:core:uibinding` and `CoverModelsCollector` in four features.
Coverless-monogram (`CoverlessTitleCover`, `MonogramCoverMetrics`) — also done in S4-4.

## S7-1 — Phase 1 questions

- `BookCardUiModel` / `BookCardVariant` / `BookCardContent` / `BookCardDecorations` / `BookCardEvent` /
  `BookCardKey` — none of these types exist yet; settle the full shape here.
- Stacked covers: `StackedJackets` and `SeriesCoverStack` are not on `Cover` yet — decide whether
  stacking becomes a `CoverVariant` or a `BookCard`-level composition of several `CoverUiModel`s.
- Selection (decided in S5-7): `BookCardDecorations.selection: SelectionMarkUiModel?` (null = not in
  selection mode), not a `CoverUiModel` field. Library's per-book marks are already collector-built
  (`SelectionMarksCollector`); `SelectableCover` still places `SelectionMark` by hand (TopEnd, 8dp) —
  fold that placement into `BookCard`.
- Deadline overlay: shipped as the separate `CoverOverlay` component (S4-5b) — decide whether it folds
  onto `CoverUiModel` or stays beside it as a `BookCardDecorations` entry.

## Approved shape

- Package `:core:component` `bookcard/`; `BookCard(model, onEvent, modifier, accessory: (@Composable () -> Unit)? = null)` — `accessory` is library's drag handle only; the row gap before it is drawn only when present.
- `BookCardUiModel(key, variant, decorations)`; `BookCardKey` sealed: `Book(bookId)` | `Edition(editionId)` | `Series(seriesId)`.
- Converged (no per-surface tables, no surface enums): `CoverOnly(cover)` | `Stacked(cover, text, menuLabel: String?)` | `Row(size: Compact|Standard|Large, lead, text, trailing, deadline)`; the caller sizes the cover.
- Row sizes: Compact 40dp lead, titleMedium 1 line; Standard 56dp lead, titleMedium 2 lines; Large 74dp lead, titleLarge 2 lines. Stacked: titleSmall, 2 lines always.
- One text rule: eyebrow `eyebrowSmall` primary (red on Error tone); byline "By X" bodySmall; supporting lines bodySmall tabular; rating star first.
- One row rule: top divider on every row; hover highlight on every clickable row; progress = wavy bar filling the width, optional "NN%" beside it; on CoverOnly / Stacked under the cover. `decorations.progress`: null = no slot, `Reserved` = empty slot kept, `Value(fraction, label: String?)`.
- S7-2: library grid → `Stacked` (byline "By X"), cover-only → `CoverOnly`; progress `Reserved`/`Value(label = null)` on every cell; `BookCardModelsCollector` writes `bookCardsByTab` / `editionCardsByTab` off the existing cover / selection / overlay maps (folded in S7-3); press `None` while that tab rearranges (one shared rule with `BookList`), `Tap` in selection, else `TapAndLongPress`; one hoisted handler per list; `SelectableCover` stays for `LargeRow` until S7-3.
- `BookCardContent(eyebrow, title, titleIcon: SoftcoverIcon?, byline, supportingLines)` (titleIcon = edition format glyph); lead `Cover` | `SeriesStack` (row-only), null = coverless row; a line is parts `Text` | `Rating` joined " · ".
- `BookCardDecorations(progress, coverBadge, selection, pressBehaviour: Tap|TapAndLongPress|None, shakeOnError)`; no `emphasized` — edition sheet drops its bordered card (selected → cover ring, owned → badge); trailing `Badge` | `Restore` | `Bookmark(saved)` | `SetProgress`.
- Selection everywhere: dim + ring on the cover, TopEnd mark; a coverless row leads with the mark.
- `BookCardEvent` (all carry the key): `Clicked`, `LongClicked`, `PressStarted` (detail prefetch), `TrailingClicked`, `ShakeEnded` (clears `shakeOnError`).
- Stacking is a card-level lead, not a `CoverVariant`; the sheet's rotated `JacketStack` stays in `SheetScaffold`.
- `SelectableCover` folds into `BookCard` (dim, ring at the cover's own `CoverDimensions` radius, TopEnd mark; coverless rows lead with the mark). `CoverOverlay` stays its own component, carried as `decorations.coverBadge`.
- Transition key stays on `CoverUiModel.sharedTransitionKey` (contract reworded).
- `CoverOnly` ← `CoverOnlyCell`. `Stacked` ← `GridBookCell`, `TrendingCard`, `BecauseYouReadCard`, `SeriesCard`, `UnreleasedSeriesCard`, `PickUpNextTile`, `LovedBookCard`. `Row` ← `CompactRow`, `LargeRow`, `SearchResultRow`, `CompactBookEntry`, `HiddenBookRow`, `HiddenSeriesRow`, `EditionItem`.
- Out of `BookCard`: explore `FeaturedCard` and reading's featured hero stay feature compositions; `MoodTile` becomes its own component (S7-4), `MoodTileDimensions` leaves `bookcard/`.
- `LovedBookCard` moves onto `Cover` with a new `ProfileLovedBook` cover variant, and gains `Tap` → book detail (S7-7 wires the navigation).
- S7-5: `SearchResultRow`'s stats line moves onto `:core:uibinding`'s `bookStatsLine` (grouped readers, 1-decimal rating — the format the user picked in S7-3).
- S7-4 (user, after the diff): `Stacked` reserves 2 title lines everywhere (library grid included);
  `BookCardMenuButton` keeps its 48dp target but draws the 26dp circle 6dp in from the cover corner;
  Because-you-read's author is the "By X" byline. Accepted shifts: rail title `titleMedium` → `titleSmall`,
  gaps 8/8 → 9/2 (series 8/8/8 → 9/2/2), Trending star 14 → 12dp with tabular editorial `bodySmall`, an
  unrated / unnumbered book drops the line instead of reserving it.
- Mappers feature-local per feature; the "year · N readers · ★ rating" line goes to `:core:uibinding` at once; a `BookCardModelsCollector` per feature, one map per surface.

## S7 — non-symbol tasks

- `BookCard` with per-variant private layouts.
- Preview fixtures covering every variant × decoration combination.
- Mappers: `feature:library`, `feature:explore`, `feature:reading`, `feature:profile`,
  `feature:book_detail` (promote to `:core:uibinding` per R6 where two features converge).
- Mapper unit tests (via `softcover-test-writer`).
- Shared-element transition keys verified on library → book detail and explore → book detail (S7-7).
- Skippability verified: no per-item lambda allocation in the library grid (S7-7).

## Risks (S7-specific)

| Risk | Mitigation |
|---|---|
| Compose stability regression — a `List` in a UI model makes every grid item recompose per frame (library grid, explore rails). | R3: `kotlinx-collections-immutable`, `ImmutableList` everywhere. Verify with the compiler metrics report before S12. |
| Lambda-allocation regression — per-item `onClick` defeats skipping. | R1: one hoisted `onEvent`, keyed on the model. |
| Shared-element transitions break. | R7: key resolved by the mapper, carried on `BookCardKey`. Manually verify library → detail and explore → detail (S7-7). |
