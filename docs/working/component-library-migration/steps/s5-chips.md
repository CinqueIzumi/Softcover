# S5 — Chips & pills

**Stage:** S5 — Primitives. **Delegation:** user (Phase 1), `softcover-implementer` (Phase 2).

30 declarations collapse onto `Chip` + `ChipUiModel` (`core/component/chip/`).

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S5-1 | Pills already in `:core:component`, plus the `ChipUiModel` extension design | [x] |
| S5-2a | `feature:library` control-line and filter-row chips; `Remove(removeLabel)`, `Quiet` | [x] |
| S5-2b | Arrange and Filter sheet drafts onto `UiState`; `Choice`; `ArrangeChip` | [x] |
| S5-3 | book_detail, explore, reading and settings chips | [ ] |
| S5-C-chips | Convergence pass over the family (family-procedure.md § Phase 3) | [ ] |

## S5-1 — core pills

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `AddFilledPill` | `core/component/lists/ChooseListsBottomSheet.kt:476` | takes a resolved label |
| `AddOutlinePill` | `core/component/lists/ChooseListsBottomSheet.kt:457` | takes a resolved label |
| `MembershipPill` | `core/component/lists/ChooseListsBottomSheet.kt:407` | takes a resolved label |
| `OnListChip` | `core/component/lists/ChooseListsBottomSheet.kt:419` | takes a resolved label |
| date pill in `WhenReadRow` | `core/component/progress/WhenReadRow.kt:73` | inline `clip` + `background` + `pressScaleClickable`; the row itself stays S6-1 |

**Phase 1:** done — the shape is `README.md` D11, and D12 covers the `WhenReadRow` date pill.
`FormatChip` moves in S8-4. `SearchChromeField` is not a chip.

## S5-2 — `feature:library`

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `ActiveFilterChip` | `feature/library/presentation/component/LibraryFilterChipRow.kt:96` | |
| `ClearAllChip` | `feature/library/presentation/component/LibraryFilterChipRow.kt:136` | |
| `LibraryFilterChipRow` | `feature/library/presentation/component/LibraryFilterChipRow.kt:40` | |
| `ArrangeChip` | `feature/library/presentation/component/LibraryArrangeSheet.kt:304` | |
| `LayoutChipRow` | `feature/library/presentation/component/LibraryArrangeSheet.kt:201` | |
| `SortChipRow` | `feature/library/presentation/component/LibraryArrangeSheet.kt:251` | |
| `FilterPillControl` | `feature/library/presentation/component/LibraryControlLine.kt:209` | |
| `RearrangeHintChip` | `feature/library/presentation/component/LibraryControlLine.kt:154` | |
| `SortLabelControl` | `feature/library/presentation/component/LibraryControlLine.kt:107` | |
| `SelectionActionPill` | `feature/library/presentation/screen/section/SelectionHeader.kt:153` | |

**Phase 1:** done — the shape is `README.md` D13. `SortLabelControl` leaves the family (stays
feature-local); `FilterPillControl` and `SelectionActionPill` move in S8 controls.

- **S5-2a:** `Remove` gains `removeLabel` (update `ChooseListsMapper`); add `Quiet(selected)`.
  `ActiveFilterChip` → `Remove` + `Clicked`, `ClearAllChip` → `Quiet()`, `RearrangeHintChip` →
  `Quiet(selected)` + `leadingIcon`. An active-filter collector replaces `FilterChipDescriptor` /
  `toChipDescriptors()` with per-tab chip models and a key → `LibraryFilterValue` map on `LibraryUiState`;
  the rearrange chip's model arrives on `UiState` too.
- **S5-2b:** move `LibraryArrangeSheet`'s and `LibraryFilterSheet`'s `remember` drafts onto `UiState` —
  seeded on open, edited through actions, committed by `OnApplyArrangeAction` / `OnApplyFiltersAction`.
  Add `Choice(selected, trailingIcon)`; a collector derives the layout and sort chip models from the
  draft; `ArrangeChip` goes and `ChipUiModel.selectedFor` is deleted.

## S5-3 — book_detail, explore, reading, settings

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `DashedTagOpenerChip` | `feature/book_detail/presentation/screen/section/UserTagsSection.kt:78` | bespoke `Surface`, not a `Chip` wrapper |
| `ExternalLinkPill` | `feature/book_detail/presentation/screen/section/ExternalLinksSection.kt:126` | bespoke `Surface` |
| `AddPill` | `feature/book_detail/presentation/component/TagNamingField.kt:135` | |
| `TagChip` | `feature/book_detail/presentation/component/TagChip.kt:46` | |
| `TagChipName` | `feature/book_detail/presentation/component/TagChip.kt:134` | |
| `TrackingNowChip` | `feature/book_detail/presentation/component/EditionBottomSheetSelector.kt:433` | |
| `ConcealableTagChip` | `feature/book_detail/presentation/screen/section/TagsSection.kt:140` | reveal state in `rememberSaveable`; a second `clickable` stacked on `Chip` |
| update-progress pill in `InProgressSection` | `feature/book_detail/presentation/screen/section/InProgressSection.kt:77` | inline `Surface`; `surfaceContainerHigh`, `primary` ink, 14/7 padding |
| `RecentSearchChip` | `feature/explore/presentation/screen/section/ExploreSectionShared.kt:22` | |
| `SortChip` | `feature/explore/presentation/screen/section/SortChip.kt:25` | |
| `FlowRowMoodChips` | `feature/explore/presentation/screen/section/SearchFocusContent.kt:163` | |
| `SetProgressChip` | `feature/reading/presentation/screen/section/CompactBookEntry.kt:231` | |
| `UpdatePillButton` | `feature/settings/presentation/screen/section/AppUpdateSection.kt:145` | |

**Phase 1 questions:** `DashedTagOpenerChip` and `ExternalLinkPill` use a dashed-border / bordered-pill
chrome neither existing variant covers — decide whether that is a new `ChipVariant` or stays
feature-local as a deliberate exception (record either way). `ConcealableTagChip` moves its reveal onto
`UiState` (R1, R10): a `ChipEvent.Clicked` swaps that chip out of its concealed treatment in the
ScreenModel.
