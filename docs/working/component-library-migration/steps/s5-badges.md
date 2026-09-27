# S5 — Badges & cover overlays

**Stage:** S5 — Primitives. **Delegation:** user (Phase 1), `softcover-implementer` (Phase 2).

12 declarations collapse onto `Badge` + `CoverOverlay` (`core/component/badge/`). `DeadlineBadge`,
`DeadlineCoverOverlay`, `UnreleasedBadge` and `DeadlineSummaryLine` already migrated in S4-5b.

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S5-7 | The remaining badge/overlay call sites, `deadlineProgressByBook`, the deadline-trio visual pass | [ ] |
| S5-C-badges | Convergence pass over the family (family-procedure.md § Phase 3) | [ ] |

## S5-7 — inventory

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `BookmarkGlyph` | `core/component/lists/ChooseListsBottomSheet.kt:345` | |
| `LibraryDeadlineCountdownBadge` | `feature/library/presentation/screen/section/LayoutBookEntry.kt:205` | |
| `CoverGridOverlay` | `feature/library/presentation/screen/section/GridBookCell.kt:31` | |
| `SelectionCircleIndicator` | `feature/library/presentation/screen/section/SelectableCover.kt:93` | |
| `OwnedCoverBadge` | `feature/book_detail/presentation/screen/section/GeneralBookInfoSection.kt:426` | |
| `SelectedCheckBadge` | `feature/book_detail/presentation/component/EditionBottomSheetSelector.kt:410` | |
| `FolioIndicator` | `feature/onboarding/presentation/screen/OnboardingScreenLayout.mobile.kt:219` | |
| `GripOrPinGlyph` | `feature/settings/presentation/screen/section/ReorderableRow.kt:117` | |
| `TrackingNowChip` | `feature/book_detail/presentation/component/EditionBottomSheetSelector.kt:433` | a status badge |

## S5-7 — non-symbol tasks

- `deadlineProgressByBook` becomes UI-typed. It is still the domain `DeadlineProgress` map on
  `LibraryUiState` and `ReadingScreenUiState`, fed by each feature's `DeadlineModelsCollector`, and read
  for the library countdown badge (`section/BookList.kt`) and reading's featured-book deadline
  (`section/ReadingBooksColumn.kt`, `ReadingScreenLayout.jvm.kt`).
- Visual pass on the deadline trio (`Badge`, `CoverOverlay`, `DeadlineSummaryLine`) — nobody has
  watched them render from their new models yet.

**Phase 1 questions:** `LibraryDeadlineCountdownBadge`, `CoverGridOverlay` and
`SelectionCircleIndicator` are all cover-adjacent decoration — do they fold onto `CoverOverlay` as
additional `CoverOverlayUiModel` variants, or does selection stay a `CoverUiModel`-level concern (see
`steps/s7-bookcard.md` S7-1, which asks the same question from the `BookCard` side)? Coordinate the
answer with S7-1 before building either.
