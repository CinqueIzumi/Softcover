# S7 — `BookCard`

**Stage:** S7 — the main event, and the risk concentration point. **Delegation:** user (Phase 1),
`softcover-implementer` (Phase 2).

21 declarations across five features collapse onto one `BookCard`. Several participate in
shared-element transitions via `bookCoverTransitionKey`; several sit inside selection modes and lazy
grids where a stability regression is a dropped frame, not a compile error.

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S7-1 | `BookCard` models, the component and the fixtures | [ ] |
| S7-2 | The library grid and cover-only cells, plus the selection covers | [ ] |
| S7-3 | The library rows and dispatchers | [ ] |
| S7-4 | Explore rails, featured, series and mood tile | [ ] |
| S7-5 | Explore search and hidden suggestions | [ ] |
| S7-6 | Reading | [ ] |
| S7-7 | Profile, book_detail's `EditionItem` and `StackedJackets`, plus the final shared-element and skippability verification | [ ] |

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
| `MoodTile` — `feature/explore/presentation/screen/section/MoodGrid.kt:127` | `Tile` |
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
- Selection: `SelectableCover` and `LibraryGridCover` still wrap `Cover` locally
  (`feature/library/presentation/screen/section/SelectableCover.kt`,
  `feature/library/presentation/screen/section/LayoutBookEntry.kt`) — decide whether selection state
  joins `CoverUiModel` or `BookCardDecorations`. Coordinate with `steps/s5-badges.md` S5-7, which asks
  the same question about `SelectionCircleIndicator`.
- Deadline overlay: shipped as the separate `CoverOverlay` component (S4-5b) — decide whether it folds
  onto `CoverUiModel` or stays beside it as a `BookCardDecorations` entry.

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
