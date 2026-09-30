# S6 — Sheet chrome

**Stage:** S6 — Rows & sheet chrome. **Delegation:** user (Phase 1, done),
`softcover-implementer` (Phase 2).

Chrome only, extracted from 18 sheets into `SheetScaffold` + `SheetHeader` + `SheetRow` +
`SheetFooter`. Each sheet's **body** stays a feature composable (`component-contract.md` § 7.6).
S6-3 builds the chrome parts and every later sub-step adopts them; a sheet that does not fit the
approved chrome keeps its chrome feature-local, with a `## Deferred` line.

## Approved shape

Phase 1 ran on 2026-09-30, and the user approved every point below.

**Container.** Every sheet already uses the foundation's `AdaptiveModalSheet`. It owns the container:
the sheet or panel form, the colour, the grabber, the insets and the animated dismiss. There is no
container to extract.

**`SheetScaffold` (public, `core/component/sheet/`).**
- Signature: `SheetScaffold(model: SheetScaffoldUiModel, onEvent: (SheetScaffoldEvent) -> Unit, modifier,
  content: @Composable ColumnScope.() -> Unit)`. It wraps `AdaptiveModalSheet`.
- `SheetScaffoldUiModel`: `header: SheetHeaderUiModel`, `topAction: String?`,
  `footer: SheetFooterUiModel?`, `layout: SheetLayout`, `dismissible: Boolean = true`.
- `SheetScaffoldEvent`: `TopActionClicked`, `PrimaryClicked`, `SecondaryClicked`,
  `HeaderActionClicked`, `Dismissed`.
- `SheetLayout.Scrolling`: the whole sheet scrolls, and the footer comes at the end of the content.
- `SheetLayout.Pinned`: the top action and the header stay fixed. The body is `weight(1f)` and owns its
  own scroll or lazy list. The footer sticks, with a `Divider` above it.
- One padding everywhere: h24, top 8, bottom 16. `imePadding` in both layouts.
- `dismissible = false` sets both dismiss flags to false (`LoadingSheet`, `ReAuthDialog`).
- The top action takes the `labelLarge` SemiBold `primary` style, right-aligned. It is used by
  TagEditor ("Done") and Verdict ("Not now"). It always closes the sheet: the scaffold emits
  `TopActionClicked`, then runs the animated dismiss.

**`SheetHeaderUiModel` (model only; no public `SheetHeader`).**
- Fields: `section: SectionHeaderUiModel.Section` and `trailing: SheetHeaderTrailing?`.
- `SheetHeaderTrailing` is one of `Jacket(CoverUiModel)`, `JacketStack(...)` (ChooseLists) or
  `Action(label)` (Shelves' "Manage", which emits `HeaderActionClicked`).
- The scaffold renders it privately, as `SectionHeader` with its `trailing` slot.
- Every sheet has a header with a headline (foundations § 5.6). Gaps come from `SectionHeader`
  (12/8/8). Jackets are 56dp.
- Deleted: `ChooseListsHeader`, `ChangeEditionHeader`, `TagEditorHeader`, `TagEditorTopBar`,
  `EditorialHeader`, `ShelvesSheetHeader`. Remove the stale "SectionHeader has no trailing slot" KDoc
  with them.

**`SectionHeaderUiModel.Section` emphasis.**
- `headline` and `description` each take an optional `TextEmphasis(substring, treatment)`.
- `EmphasisTreatment` is an enum of named treatments that the component resolves to theme tokens. It
  ships with `Accent` → `primary` only; adding a treatment means one entry plus one line in the
  component. No raw `SpanStyle` or `Color`: a collector cannot read the theme.
- Consumers: ChooseLists' headline and TagEditor's description.

**`SheetFooterUiModel`.**
- Fields: `primary: SheetFooterButton?`, `secondary: SheetFooterButton?`, `layout`.
- `SheetFooterButton` has `label`, `enabled`, `loading` and `dismissesSheet` (the scaffold runs the animated dismiss after emitting the click; S6-6/S6-7 adopters use it instead of `LocalModalSheetDismiss` in bodies).
- `layout = Stacked`: a FILLED M primary, full width, with a TEXT M secondary below it.
- `layout = Inline`: the TEXT secondary on the left and the weighted FILLED primary on the right (Filter).
- `FilterSheetFooter` is deleted.

**`SheetRow<K>` (public, `core/component/sheet/`).**
- Model: typed key `K` (as the sidebar has), `label`, `selected`, optional leading icon, optional
  `count: String`.
- Anatomy: top `Divider`, v13, gap 12, italic `titleLarge`, `primary` when selected, trailing 18dp
  check.
- Semantics: `selectable` with `Role.RadioButton`. Emits `SheetRowEvent.Selected(key)`.
- Replaces `ShelvesSheetRow` and `BecauseYouReadGenreSheetRow`.
- `ChooseListsRow` stays separate: its tri-state glyph, caption and chip trailing are a different
  anatomy.

**R10.** Every scaffold model arrives on the feature's `UiState`, built in a collector or action.
Values computed in composition inside a sheet *body* are not fixed here. List each under `## Deferred`
for S10 or S11. The exception is TagEditor's description, which moves into the header model.

**Per sheet.**
- S6-3 ChooseLists: Pinned layout, `JacketStack`. `NewListRow` stays in the body.
- S6-3 UpdateProgress: Scrolling layout. Its buttons stay in the tab bodies, because they submit
  local per-tab drafts. `TabSwitcher` is body content and does not change.
- S6-3 LoadingSheet: `dismissible = false`.
- S6-4 Verdict: Pinned layout, top action, Stacked footer. See the models below.
- S6-5 Filter: Pinned layout, Inline footer. The facet labels become `SectionHeader.Label`.
- S6-5 Arrange: Scrolling layout, Stacked footer.
- S6-5 Shelves: Scrolling layout, `Action("Manage")`, `SheetRow`. Header: "Your shelves" /
  "Jump to a shelf".
- S6-5 BulkRemove: moves from `Dialog` to a scaffold sheet. Header: "Library" / "Remove N books?".
  Footer: primary "Remove" (`loading` from `inProgress`), secondary "Cancel".
- S6-5 ReAuthDialog (`orchestration/.../ReAuthDialog.kt`): becomes a scaffold sheet with
  `dismissible = false`, a header, the token field and error in the body, and a Stacked footer
  (submit, then "Log out"). Delete it from the README's `## Out of scope` list in the same sub-step.
- S6-6 TagEditor: Pinned layout, `Jacket`, top action "Done".
- S6-6 Edition: Pinned layout, `Jacket`, Stacked footer. The search field and card rows stay in the
  body.
- S6-6 ShareBook: header "Share" / "Share this book". Its capture and share footer stays in the body.
- S6-6 UnknownIsbn: Stacked footer.
- S6-6 CreateList: keeps its own chrome. It is the documented title-page exception, with nested
  navigator hosting.
- S6-6 tag category group header: stays local (one consumer). Its raw `HorizontalDivider` becomes
  `Divider`.
- S6-7 Genre: Scrolling layout, `SheetRow`.
- S6-7 ContinueSeries: Stacked footer with the secondary "Cancel" only. The action rows stay local.
- S6-7 Streak: its day rows stay local. Drop the extra `navigationBarsPadding`.
- S6-7 ProfileShare: header "Share" / "Share your profile". Its footer stays in the body.
- S6-7 LogOut: header "Account" / "Log out?", no longer centred. Stacked footer: "Log out", then
  "Stay".

**S6-4 models.**
- `VerdictSheetUiModel(scaffold, cover: CoverUiModel?, initialRating, initialReview,
  initialHasSpoilers)`.
- `VerdictSheetEvent`: `Saved(rating, review, hasSpoilers)`, `DeleteRequested`, `Dismissed`. Events
  carry no key: the sheet is a singleton.
- The signature is `(model, onEvent, modifier)`. The `cover` slot goes.
- The draft (text, marks, rating, spoilers) stays local and is seeded once, as in the progress sheet.
  The score captions stay in the component.
- The mapper goes straight to `:core:uibinding`, because Book Detail and Reading map it identically.
- Delete shows only when deletion applies. Reading's empty `onDelete` goes.
- Reading stops gating the sheet on the cover. The component renders the cover when it is present.
- `VerdictBlock` joins the step: `VerdictBlockUiModel` plus `VerdictBlockEvent.EditRequested`, built
  in `VerdictReviewCollector`.
- `RichTextFormattingToolbar` is not in this step: `steps/s8-controls.md` S8-4 owns it.
- Doc updates, in the same sub-step (G5):
  - `patterns/book-detail.md`: `VerdictSheetContext` becomes the shared mapper's input, and the copy
    rides on the model.
  - `component-contract.md` § 7.4a and the R11 holdout list: drop `VerdictSheet` and `VerdictBlock`.
  - `steps/s11-contract-retrofit.md` S11-2: drop them from its list too.
- `SoftcoverDatePickerDialog` stays an M3 dialog.
  - `DatePickerUiModel(initialDate: LocalDate, confirmLabel, cancelLabel)`.
  - `DatePickerEvent`: `Confirmed(date)`, `Dismissed`.
  - Book Detail gains `deadlinePicker: DatePickerUiModel?`, built in `OnOpenDeadlinePickerAction`. It
    replaces `showDeadlinePicker` and the `currentLocalDate()` read in composition.
  - No date bounds.
  - `component-contract.md` R4 gains one line: `kotlinx.datetime.LocalDate` is an allowed value type.

**Accepted visual shifts**, to eyeball on device afterwards:
- Header gaps become 12/8/8. Jackets become 56dp.
- `EditorialHeader`'s bar moves inline.
- Sheet vertical padding becomes top 8 / bottom 16.
- Edition's footer gains a divider. Filter's footer inset goes from 22 to 24.
- One text-button style for "Stay", "Cancel" and "Delete".
- ShareBook's eyebrow gains a bar.
- Streak's insets: confirm them on device.

## Deferred

- `TextEmphasis(substring, treatment)` stays as built for now (user, 2026-09-30): `SectionHeader`'s `emphasisedText` keeps its substring search. Revisit in S6-C-sheets.
- `SheetRow` adopters (S6-5 Shelves, S6-7 Genre) wrap their rows in `Modifier.selectableGroup()`, because `Role.RadioButton` rows need a group. If both need it, fold it into the component.
- S6-5 `LibraryFilterSheet` body, for S10/S11: the Tags facet's `"$availableCount available"` trailing text is built in composition, and the five facet `SectionHeaderUiModel.Label`s are file-level constants in the sheet file rather than state fields. `LibraryArrangeSheet`'s `ShowTitlesToggleRow` label and `SortChipRow`'s "Make this list ordered" affordance stay raw feature composables.
- S6-C-sheets: no design-system section file covers `SheetScaffold` or `SheetFooterButton.dismissesSheet` yet — add one. Bulk-remove closes without animation when the removal finishes (state-driven close).

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S6-3 | `SheetScaffold`/`SheetHeader`/`SheetRow`/`SheetFooter` design, plus `ChooseListsBottomSheet`, `UpdateProgressBottomSheet`/`TabSwitcher`, `LoadingSheet` | [x] |
| S6-4 | `VerdictSheet` + `VerdictBlock` (owe R1) and `SoftcoverDatePickerDialog` (owes R1 + R11) | [x] |
| S6-5 | The library sheets, the bulk-remove dialog and `ReAuthDialog` | [x] |
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
| `EditorialHeader` | `core/component/progress/EditorialHeader.kt:19` | `UpdateProgressBottomSheet`'s header; bar stacked above the eyebrow |
| `ChooseListsHeader` | `core/component/lists/ChooseListsBottomSheet.kt:141` | takes `ChooseListsVariant` + jacket slot |
| `ChangeEditionHeader` | `feature/book_detail/presentation/component/EditionBottomSheetSelector.kt:213` | |
| `ShelvesSheetHeader` | `feature/library/presentation/component/LibraryShelvesSheet.kt:90` | |
| `TagEditorHeader` | `feature/book_detail/presentation/component/TagEditorHeader.kt:37` | |
| `ShelvesSheetRow` | `feature/library/presentation/component/LibraryShelvesSheet.kt:129` | `SheetRow` candidate, from S6-2: the same anatomy as the genre row, plus a leading glyph and a count |
| `BecauseYouReadGenreSheetRow` | `feature/explore/presentation/screen/section/BecauseYouReadGenreControl.kt:166` | `SheetRow` candidate, from S6-2 |

**Phase 1 questions:** design `SheetScaffold`/`SheetHeader`/`SheetRow`/`SheetFooter` against these four
sheets first, since they are already contract-shaped and expose the chrome boundary cleanly.
`SheetHeader` is a `SectionHeader.Section` with the jacket in `SectionHeader`'s `trailing` slot (added in
S5-C-headers); decide whether a separate `SheetHeader` is still warranted at all. Hold every chrome part
to the S6-1 bar: extract it only where two sheets share the anatomy, not the name.

## S6-4 — `VerdictSheet` and `SoftcoverDatePickerDialog`

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `VerdictSheet` | `core/component/verdict/VerdictSheet.kt:124` | owes R1 |
| `SoftcoverDatePickerDialog` | `core/component/dialog/SoftcoverDatePickerDialog.kt:29` | + internal `PickerDates.kt` helpers; owes R1 + R11, not just chrome |
| `VerdictBlock` | `core/component/verdict/VerdictBlock.kt:51` | owes R1 + R11; call site `YoursLensContent.kt:172` derives from the domain `Book` in composition |

## S6-5 — library sheets and bulk-remove dialog

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `LibraryFilterSheet` | `feature/library/presentation/component/LibraryFilterSheet.kt:77` | |
| `FilterSheetFooter` | `feature/library/presentation/component/LibraryFilterSheet.kt:430` | |
| `EmptyFacetMessage` | `feature/library/presentation/component/LibraryFilterSheet.kt:478` | |
| `TagSearchField` | `feature/library/presentation/component/LibraryFilterSheet.kt:345` | |
| `LibraryArrangeSheet` | `feature/library/presentation/component/LibraryArrangeSheet.kt:81` | |
| `LibraryShelvesSheet` | `feature/library/presentation/component/LibraryShelvesSheet.kt:50` | |
| `BulkRemoveConfirmationDialog` | `feature/library/presentation/screen/section/BulkRemoveConfirmationDialog.kt:24` | dialog → scaffold sheet |
| `ReAuthDialog` | `orchestration/presentation/ReAuthDialog.kt:45` | dialog → non-dismissible scaffold sheet; drop it from the README's `## Out of scope` |

## S6-6 — book_detail, lists, scan sheets

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `TagEditorBottomSheet` | `feature/book_detail/presentation/component/TagEditorBottomSheet.kt:81` | |
| category group header | `feature/book_detail/presentation/component/TagEditorCollection.kt:73` | eyebrow · raw `HorizontalDivider` · count; header anatomy, so a `SectionHeader` variant, not `Divider` |
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
