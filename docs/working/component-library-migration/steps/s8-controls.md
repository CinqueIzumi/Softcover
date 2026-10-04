# S8 — Controls & fields

**Stage:** S8 — Screen states. **Delegation:** user (Phase 1), `softcover-implementer` (Phase 2).

The controls collapse onto `PillButton` + `SegmentedControl` + `Toggle` + `TextField`. Phase 1 ran once for
S8-4–S8-6 together; Phase 2 builds them in the order S8-6 → S8-4 → S8-5 (four files and the gallery
registry are shared, so the builds run one after another; each finished sub-step's tests run alongside the
next build).

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S8-6 | `PillButton` and the pill-button call sites (built first — S8-4 and S8-5 consume it) | [x] |
| S8-4 | `SegmentedControl` + `Toggle`, their call sites, and the rich-text toolbar | [ ] |
| S8-5 | `TextField` and its call sites | [ ] |
| S8-C-controls | Convergence pass over the family (family-procedure.md § Phase 3) | [ ] |

## Approved shape

Approved 2026-10-04.

- **`PillButton`** (`core/component/button/`): `PillButtonUiModel(key, content, tone, size, interaction, indicator: Boolean)`; `content` = `Label(text, leadingIcon?)` | `IconOnly(icon, description)` (icon-only renders as a circle); `tone` = `Tonal` | `Accent` (primary ink on `surfaceContainerHigh`) | `Filled` | `Outlined` | `Quiet` (`surfaceContainer`); `size` = `Regular` 38dp | `Large` 44dp; `interaction` = `Clickable` | `Disabled`; `PillButtonEvent.Clicked(key)`.
- PillButton is an action, not a `Chip` tone — `Chip` stays selection/filter.
- One metric set: 17dp icon, 6dp gap, `labelMedium` SemiBold, 14dp horizontal padding. Accepted shifts: `AddPill` `labelLarge` → `labelMedium`; `ExternalLinkPill` loses its 16dp icon and 10dp vertical padding; the Update pill's icon 15 → 17dp.
- PillButton has no loading state (static labels); `SetProgressChip` stays masked by its card.
- Menus stay at the call site; the pill is only the anchor. Sort's menu-open flag moves from `remember` to `ExploreUiState`.
- Filter's active dot, the Sort label, the external links' labels/icons/URLs move off the render onto the feature `UiState`; external-link events carry a key the collector maps to its URL.
- **`SegmentedControl`** (`core/component/control/`): `SegmentedControlUiModel<K>(options: ImmutableList<SegmentUiModel<K>>, selected: K, variant)`; `SegmentUiModel<K>(key, label, enabled)`; `variant` = `Track` | `Prose(prefix, suffix)`; `SegmentedControlEvent.Selected<K>(key)`.
- One look: every `Track` uses the Lens track (`surfaceContainer` pill, `primary` selected segment). Accepted shifts: share sheet, profile year metric, progress-sheet tabs lose the Material outlined look.
- The lists privacy sentence is the `Prose` variant (same single choice, radio semantics).
- Profile's year metric moves from `remember` to `ProfileUiState` with an action; Lens's "Yours" enabled flag becomes the option's `enabled`.
- No loading state: a segment that is not ready is a disabled option.
- **`Toggle`** (`core/component/control/`): `ToggleUiModel(key, checked, enabled, description, variant)`; `variant` = `Icon(on, off, style: Filled | Bare)` | `Switch`; `ToggleEvent.Toggled(key, checked)`. Every toggle carries switch semantics.
- **`RichTextFormattingToolbar`** moves to `richtext/` (its only consumer is the verdict editor, and it works on `RichTextMarkType`): `RichTextFormattingToolbarUiModel(activeMarks: ImmutableSet<RichTextMarkType>, enabled)`, `RichTextFormattingToolbarEvent.MarkToggled(mark)`. `FormatChip` becomes `Chip` (Tonal, selected, Bold/Italic face); `Chip` gains selected semantics. The verdict editor builds the toolbar model from its own text selection — the contract's unsettled composition-only-value edge, recorded as such.
- **`TextField`** (`core/component/field/`): `TextFieldUiModel(key, text, placeholder, variant, keyboard: Text | Number, enabled, autoFocus)`; `variant` = `Pill(leadingIcon?, trailing: PillButtonUiModel?)` | `Outlined(label)` | `Hero` (display, auto-fit) | `Numeral(charCount)`; `TextFieldEvent.ValueChanged(key, text)` | `Submitted(key)` | `TrailingClicked(key)`.
- `Outlined(label)` keeps the scanner manual entry and the re-auth token field's current Material outlined look and floating label.
- `TagNamingField`'s Add pill is the `Pill` trailing slot, present only when the draft is non-blank (decided by the mapper).
- `NameHeroField`'s `FocusRequester` plumbing becomes `autoFocus`; `TimeField`'s select-all-on-focus is internal to `Numeral`.
- The progress sheet's hours/minutes/seconds drafts move from `remember` into the feature state; clamping moves into the action.
- No loading state: a field holds user input; submitting disables it via `enabled`.
- **Stays out:** `BecauseYouReadGenreControl` (one-off eyebrow sheet trigger with its own loading — stays in explore); the search top bar's input (its own focus contract, § 7.6); the verdict editor's body (multiline rich-text editor).
- **Dropped from the inventory:** `UpdatePillButton` — deleted in `1c89151e`; the update card is a `Callout` with a `RhaydusButton`.

## S8-4 open

- Gates: main compile, ktlint, detekt, module graph, projectHealth green; `styleCheck` red only on `compileAndroidHostTest` in explore/library/lists/settings (old tests vs changed models).
- Test-writer: fix those (`Bookmark.labelled`, `.checked` → `.toggle`, `OnPrivacySelectedAction`); cover `lensToggleFor` + `LensToggleCollector`, `yearMetricToggleFor` + `YearMetricToggleCollector`, `OnYearMetricSelectedAction`, `privacyToggleFor`, `hideUntaggedAuthorsToggleFor`, `libraryVisibilityToggleFor`, `ProgressSheetUiModel.tabsFor`, `shareSheetBodyFor.variantToggle`.
- Fix round: `LibraryTabEntry.visibilityToggle` is built in the `orderedEntries` getter (store it); `SettingsToggleRow`'s row `toggleable` and its Switch both toggle (one target).
- `VerdictSheet` builds its toolbar and spoiler-switch models from local editor state — the approved composition-only edge; reviewer confirms only.
- Then reviewer, then S8-5.

## S8-6 — `PillButton`

| Symbol(s) | Current path | Note |
|---|---|---|
| `FilterPillControl` | `feature/library/presentation/component/LibraryControlLine.kt` | `Quiet`, active dot |
| `SelectCircleControl` | `feature/library/presentation/component/LibraryControlLine.kt` | `IconOnly` circle (moved here from S8-4) |
| `SelectionActionPill` | `feature/library/presentation/screen/section/SelectionHeader.kt` | `Large`, disabled state, Move anchors a menu |
| `SelectionExitButton`, `SelectionTrashButton` | `feature/library/presentation/screen/section/SelectionHeader.kt` | `IconOnly` circles (off-inventory, approved) |
| `AddPill` | `feature/book_detail/presentation/component/TagNamingField.kt` | `Filled`; becomes `TextField`'s trailing slot in S8-5 |
| `ExternalLinkPill` | `feature/book_detail/presentation/screen/section/ExternalLinksSection.kt` | `Outlined`, leading icon |
| update-progress pill in `InProgressSection` | `feature/book_detail/presentation/screen/section/InProgressSection.kt` | `Accent`, inline `Surface` |
| `SortChip` | `feature/explore/presentation/screen/section/SortChip.kt` | menu anchor |
| `SetProgressChip` | `core/component/bookcard/BookCardControls.kt` | `IconOnly`, `Accent` |

## S8-4 — `SegmentedControl` + `Toggle`

| Symbol(s) | Current path | Note |
|---|---|---|
| `LensToggle`, `LensSegment` | `feature/book_detail/presentation/screen/section/LensToggle.kt` | `Track`; the reference look |
| `ShareCardVariantToggle` | `feature/book_detail/presentation/component/ShareBookBottomSheet.kt` | `Track` (was M3) |
| `YearMetricToggle` | `feature/profile/presentation/screen/section/YearColumnHistorySection.kt` | `Track` (was M3); state to `ProfileUiState` |
| `TabSwitcher` | `core/component/progress/TabSwitcher.kt` | `Track` (was M3; off-inventory, approved) |
| `PrivacyProseToggle` | `feature/lists/presentation/screen/CreateListSheetContent.kt` | `Prose` |
| `BookmarkToggle` | `core/component/bookcard/BookCardControls.kt` | `Icon(Filled)` |
| `EyeToggle` | `feature/settings/presentation/screen/section/ReorderableRow.kt` | `Icon(Bare)` |
| `HideUntaggedAuthorsToggle` | `feature/profile/presentation/screen/section/AuthorRepresentationSection.kt` | `Switch`; row text stays feature-local |
| M3 `Switch` in `VerdictSheet`, `SettingsSectionShared`, `LibraryArrangeSheet` | — | `Switch` (off-inventory, approved) |
| `RichTextFormattingToolbar`, `FormatChip` | `core/component/control/RichTextFormattingToolbar.kt` | → `richtext/`, model + event, `FormatChip` → `Chip` |

## S8-5 — `TextField`

| Symbol(s) | Current path | Note |
|---|---|---|
| `TimeField` | `core/component/progress/TimeField.kt` | `Numeral` |
| `TagNamingField` | `feature/book_detail/presentation/component/TagNamingField.kt` | `Pill` with trailing Add |
| `KeyField` | `feature/onboarding/presentation/screen/OnboardingShelf.kt` | `Pill` |
| `TagSearchField` | `feature/library/presentation/component/LibraryFilterSheet.kt` | `Pill`, leading search icon (off-inventory, approved) |
| `NameHeroField` | `feature/lists/presentation/screen/CreateListSheetContent.kt` | `Hero` |
| `ManualEntryContent`'s field | `feature/scan/presentation/screen/BarcodeScannerShelf.kt` | `Outlined(label)` |
| `ReAuthSheet`'s field | `orchestration/presentation/ReAuthSheet.kt` | `Outlined(label)` |
