# S6 — Sheet chrome

**Stage:** S6 — Rows & sheet chrome. **Delegation:** user (Phase 1), `softcover-implementer` (Phase 2).

Chrome only, extracted from 18 sheets into `SheetScaffold` + `SheetHeader` + `SheetRow` +
`SheetFooter`. Each sheet's **body** stays a feature composable (`component-contract.md` § 7.6).

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S6-3 | `SheetScaffold`/`SheetHeader`/`SheetRow`/`SheetFooter` design, plus `ChooseListsBottomSheet`, `UpdateProgressBottomSheet`/`TabSwitcher`, `LoadingSheet` | [ ] |
| S6-4 | `VerdictSheet` (owes R1) and `SoftcoverDatePickerDialog` (owes R1 + R11) | [ ] |
| S6-5 | The library sheets and the bulk-remove dialog | [ ] |
| S6-6 | The book_detail, lists and scan sheets | [ ] |
| S6-7 | The explore, reading and profile sheets | [ ] |
| S6-C-sheets | Convergence pass over the family (family-procedure.md § Phase 3) | [ ] |

## S6-3 — chrome design + first movers

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `ChooseListsBottomSheet` | `core/component/lists/ChooseListsBottomSheet.kt:78` | already R1/R2-shaped; owes chrome extraction only |
| `UpdateProgressBottomSheet` | `core/component/progress/UpdateProgressBottomSheet.kt:50` | already R1/R2-shaped; owes chrome only |
| `ProgressBottomSheetContent` | `core/component/progress/UpdateProgressBottomSheet.kt:67` | already R1/R2-shaped; owes chrome only |
| `TabSwitcher` | `core/component/progress/TabSwitcher.kt:15` | owes chrome only |
| `LoadingSheet` | `core/component/sheet/LoadingSheet.kt:29` | model + event already landed (S4-5a); chrome extraction is this sub-step's work |

**Phase 1 questions:** design `SheetScaffold`/`SheetHeader`/`SheetRow`/`SheetFooter` against these four
sheets first, since they are already contract-shaped and expose the chrome boundary cleanly. Cross-check
against `steps/s5-headers.md` S5-6, which asks whether sheet-context headers fold into `SheetHeader`
instead of `SectionHeader`.

## S6-4 — `VerdictSheet` and `SoftcoverDatePickerDialog`

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `VerdictSheet` | `core/component/verdict/VerdictSheet.kt:124` | owes R1 |
| `SoftcoverDatePickerDialog` | `core/component/dialog/SoftcoverDatePickerDialog.kt:29` | + internal `PickerDates.kt` helpers; owes R1 + R11, not just chrome |

## S6-5 — library sheets and bulk-remove dialog

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `LibraryFilterSheet` | `feature/library/presentation/component/LibraryFilterSheet.kt:77` | |
| `FilterSheetFooter` | `feature/library/presentation/component/LibraryFilterSheet.kt:430` | |
| `EmptyFacetMessage` | `feature/library/presentation/component/LibraryFilterSheet.kt:478` | |
| `TagSearchField` | `feature/library/presentation/component/LibraryFilterSheet.kt:345` | |
| `LibraryArrangeSheet` | `feature/library/presentation/component/LibraryArrangeSheet.kt:81` | |
| `LibraryShelvesSheet` | `feature/library/presentation/component/LibraryShelvesSheet.kt:50` | |
| `BulkRemoveConfirmationDialog` | `feature/library/presentation/screen/section/BulkRemoveConfirmationDialog.kt:24` | |

## S6-6 — book_detail, lists, scan sheets

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `TagEditorBottomSheet` | `feature/book_detail/presentation/component/TagEditorBottomSheet.kt:81` | |
| `EditionBottomSheetSelector` | `feature/book_detail/presentation/component/EditionBottomSheetSelector.kt:62` | |
| `EditionBottomSheetContent` | `feature/book_detail/presentation/component/EditionBottomSheetSelector.kt:91` | |
| `ShareBookBottomSheet` | `feature/book_detail/presentation/component/ShareBookBottomSheet.kt:55` | |
| `CreateListSheet` | `feature/lists/presentation/screen/CreateListSheet.kt:15` | |
| `CreateListSheetContent` | `feature/lists/presentation/screen/CreateListSheetContent.kt:83` | |
| `UnknownIsbnSheet` | `feature/scan/presentation/component/UnknownIsbnSheet.kt:28` | |

## S6-7 — explore, reading, profile sheets

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `BecauseYouReadGenreSheet` | `feature/explore/presentation/screen/section/BecauseYouReadGenreControl.kt:111` | |
| `ContinueSeriesDismissSheet` | `feature/explore/presentation/screen/section/ContinueSeriesMenuSheet.kt:94` | |
| `ContinueSeriesMenuSheet` | `feature/explore/presentation/screen/section/ContinueSeriesMenuSheet.kt:44` | |
| `StreakStripSheet` | `feature/reading/presentation/component/StreakStrip.kt:161` | |
| `StreakStripSheetContent` | `feature/reading/presentation/component/StreakStrip.kt:171` | |
| `ProfileShareBottomSheet` | `feature/profile/presentation/screen/section/ProfileShareBottomSheet.kt:62` | |
| `LogOutConfirmBottomSheet` | `feature/profile/presentation/screen/section/LogOutConfirmBottomSheet.kt:30` | |
