# S8 — Controls & fields

**Stage:** S8 — Screen states. **Delegation:** user (Phase 1), `softcover-implementer` (Phase 2).

14 declarations collapse onto `Toggle` + `SegmentedControl` + `TextField`.

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S8-4 | `Toggle` + `SegmentedControl` design and the toggle/segment call sites | [ ] |
| S8-5 | `TextField` and the rest | [ ] |
| S8-C-controls | Convergence pass over the family (family-procedure.md § Phase 3) | [ ] |

## S8-4 — `Toggle` + `SegmentedControl`

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `LensToggle` | `feature/book_detail/presentation/screen/section/LensToggle.kt:36` | |
| `LensSegment` | `feature/book_detail/presentation/screen/section/LensToggle.kt:90` | |
| `ShareCardVariantToggle` | `feature/book_detail/presentation/component/ShareBookBottomSheet.kt:210` | |
| `SelectCircleControl` | `feature/library/presentation/component/LibraryControlLine.kt:264` | |
| `BookmarkToggle` | `feature/explore/presentation/screen/section/SearchResultRow.kt:168` | |
| `BecauseYouReadGenreControl` | `feature/explore/presentation/screen/section/BecauseYouReadGenreControl.kt:49` | |
| `YearMetricToggle` | `feature/profile/presentation/screen/section/YearColumnHistorySection.kt:116` | |
| `HideUntaggedAuthorsToggle` | `feature/profile/presentation/screen/section/AuthorRepresentationSection.kt:163` | |
| `EyeToggle` | `feature/settings/presentation/screen/section/ReorderableRow.kt:215` | |
| `PrivacyProseToggle` | `feature/lists/presentation/screen/CreateListSheetContent.kt:335` | |

**Phase 1 questions:** `RichTextFormattingToolbar` (`core/component/control/RichTextFormattingToolbar.kt:31`,
moved and renamed from `ReviewFormattingToolbar` in S4-2b) is listed both under § 7.0's rich-text
remaining row and under this family. Settle here whether it belongs in `control/` as a consolidation
target for this sub-step, or in `richtext/` bearing its own `Toggle`/`SegmentedControl` internals — and
close its outstanding R1 debt (model + event) as part of whichever answer wins. Record the placement in
`README.md` § Decisions. Its private `FormatChip` moves onto `Chip` with `ChipVariant.Format` here
(D11), once the toolbar's model can carry the chip models.

## S8-5 — `TextField` and the rest

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `TimeField` | `core/component/progress/TimeField.kt:46` | |
| `TagNamingField` | `feature/book_detail/presentation/component/TagNamingField.kt:49` | |
| `KeyField` | `feature/onboarding/presentation/screen/OnboardingShelf.kt:268` | |
| `NameHeroField` | `feature/lists/presentation/screen/CreateListSheetContent.kt:214` | |
