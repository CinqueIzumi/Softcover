# SS — Simplicity audit

**Stage:** after S9, before PF and S10, so later stages build on the simplest shapes.
**Delegation:** user (Phase 1 per sub-step), `softcover-implementer` (Phase 2).

**Goal:** everything migrated so far is the simplest shape that fits the contract — the UI model and
everything between the data and the pixels: variants, style tables, internal types, mappers, collectors,
state fields, event handlers and gallery helpers — including all of S9's output. Absorbs the old S11-4
minimal-shape audit. Stages after SS are held to the same smells by the README's `## Decisions`.

## What counts as over-engineering

- A style or metrics table, or an internal type, whose fields only one variant sets off a shared value, or
  that copies what the framework already supplies (`Surface`'s content colour, `LocalContentColor`).
- One layout split between a table and `when` branches. Variants whose anatomies differ get private
  per-variant bodies; one scaffold plus a small style only when the bodies are near-identical.
- Surface-named variants that mostly share one style tuple; a data-free primitive still behind a model
  (→ the § 7.4a data-free-primitive exception).
- A model field no call site sets off its default, a sub-model wrapping one field, a variant no call site
  uses.
- A mapper, helper, handler factory or seam with one caller that adds a layer without removing repetition.
- State fields that duplicate each other, or one derived value written in more than one place.

## Procedure

**SS-0 first, in its own session: regenerate the scope from the code, not from this table.** List every
package under `:core:component` and `:core:uibinding`, and every `*UiModel` / `*Variant` / `*Style` type
in any module (`git grep -nE "(class|interface|object) \w+(UiModel|Variant|Style)\b"`, main source sets).
Assign each to a row below; add rows for anything new (packages, feature-local models) and rebalance so
each row fits one session. SS-0 regenerated the table below on 2026-10-05.

Per sub-step: list each package's files and line counts, name every smell above by symbol, and propose
the cuts with an expected before/after line count. Stop for approval. Build per
[`family-procedure.md`](../family-procedure.md) § Phase 2; tests change only where logic changes. No
visual change unless the user accepts it (log it in [`visual-checks.md`](../visual-checks.md)).

## Sub-steps

| Sub | Scope (`:core:component` packages + their mappers, state and call sites; ~lines) | Status |
|---|---|---|
| SS-0 | Regenerate this table from the code (Procedure) | [x] |
| SS-1 | `chip`, `badge`, `divider`, `selection`, `shape`, `text`, `ComponentAlpha.kt`; uibinding `release` (`UnreleasedBadgeStyle`) — ~1.6k | [x] |
| SS-2 | `header`, `topbar`, `chrome`, `sidebar`; explore `HiddenGroupHeaderUiModel` — ~1.8k | [x] |
| SS-3 | `bookcard`; uibinding `book` — ~1.8k | [x] |
| SS-4 | `cover`, `lists`; uibinding `cover` (all platforms), `lists`; library `ArrangeMakeOrderedUiModel` — ~2.1k | [x] |
| SS-5 | `sheet`, `dialog`, `callout`, `state`; explore `ContinueSeriesEntryUiModel`, orchestration `ReAuthSheetUiModel` — ~2.2k | [x] |
| SS-6 | `control`, `button`; book_detail `ExternalLinksUiModel`, `ShareSheetBodyUiModel`; settings `SettingsToggleRowUiModel`, `SettingsChoiceRowUiModel` — ~1.9k | [x] |
| SS-7 | `field`, `richtext`; uibinding `richtext`; book_detail `BookReviewUiModel` — ~1.8k | [x] |
| SS-8 | `progress`, `deadline`, `mood`, `celebration`; uibinding `progress`, `deadline`; book_detail `InProgressUiModel`, reading `FeaturedProgressUiModel`, session `FocusPageEditorUiModel` — ~2.3k | [x] |
| SS-9 | `statistic`, `chart`; profile `ReadingAtlasUiModel` — ~1.2k | [x] |
| SS-10 | `share`, `verdict`; uibinding `verdict`; book_detail `ShareCardVariant` — ~2.5k | [ ] |
| SS-11 | `gallery`; uibinding `date`, `format`, `theme` — ~1.5k | [ ] |

Out of scope: `core/domain` `BottomBarStyle` and `DateStyle` are stored preferences, not UI types.

## Approved shape

SS-1:
- `chip`: delete `ChipDimensions` (dp inline, padding a private `when` on `ChipSize`), `ChipGapPosition` and the slot `gap` (slots place their own 8 dp `Spacer`), `ChipScaffold`'s `tappable`, `ChipBorder` (`thenChipBorder` branches on tone), Outlined's restated `mutedInk`, `ChipLeading.Icon.description`, private `ChipShape` (→ `PillShape`); dash dp inline.
- `badge`: delete `BadgeDimensions` and `CoverOverlayDimensions`; values inline.
- `selection`: delete `SelectionMarkColors`; one `when` for the mark colour, check tinted `onPrimary`; sizes inline.
- `divider`: no model — `Divider(...)` / `VerticalDivider(...)` with defaulted params, Material's default thickness, no `inset` / `length` (callers use `Modifier`); `DividerInk.ContentTint` / `OnImage` become objects with fixed alpha; `SettingsScreenUiState.versionFooterDivider` goes; `component-contract.md` § 7.4a says a data-free primitive takes defaulted params, no model.
- `text`: `SoftcoverText`'s String overload forwards to the AnnotatedString one; the private layout layer goes.
- uibinding `release`: `UnreleasedBadgeStyle` → three named mappers in `UnreleasedMapper.kt`.
- Kept: Choice chip's Medium → SemiBold on select. Deferred: `ChipSet` + `PillButtonSet` (SS-6); gallery label functions (SS-11); stray pill shapes (SS-6 / SS-4).

SS-2:
- `header`: delete `Section.pulseKey` and the pulse animation; `SectionHeaderEyebrowStyle` + `BarSpec` go — `Label` renders its text directly, `Section` / `Inline` share one private bar-plus-text body, dp inline; `SectionHeaderInline` goes; `Section.tone` goes (bar uses `primary`); `EmphasisTreatment` and `Emphasised.treatment` go; `PageMastheadDimensions` goes (inline `when (size)`, `300.dp` inline).
- Masthead refresh copy: one `:core:uibinding` mapper replaces library `MastheadSnapshot`, reading `ReadingHeaderCopy`, explore `MastheadCollector` and explore's hand-written default.
- `topbar`: `TopBarSurfaceColors` → `when (model.surface)`; `TopBarUiModel.subtitle` and `SearchTopBarUiModel.placeholder` go (literal in the component); `SearchChromeField` / `SearchChromeInputArea` take `model` + `onEvent`; `EditorialTopBarUiModel` / `EditorialTopBarEvent` merge into `TopBarUiModel` / `TopBarEvent`, one `topBar` field per state; desktop ignores `surface`.
- `sidebar`: `SidebarRowMetrics` and the file-level dp vals go; values inline.
- explore: one private `HiddenGroupSection` replaces `HiddenBooksSection` / `HiddenSeriesSection`; `hiddenBooksLoaded` / `hiddenSeriesLoaded` go (read `cards == null`); unused imports go.
- Kept: debug screens (exempt, contract § 7.4a). Deferred: `MastheadRefreshAction` + `ChromeIconButton` (SS-6); gallery labels (SS-11); BookDetail mobile `remember { TopBarUiModel }` (scroll-driven surface); file-level loading stand-in models; R2 still prescribes `*Dimensions` tables.

SS-3:
- Layouts: `BookCardCoverOnlyLayout` / `BookCardColumnLayout` go (`BookCard`'s `when` calls the scaffold); `CoverMenu` + `CoverWithCorner` inline into it; layout and `bookCardInteraction` `isLoading` params go (read `model`).
- `RowMetrics` goes: lead width, title style and title max lines inline.
- `BookCardTrailingControl` + `TrailingControlContent` → one function, one `onClick`; `Bookmark.labelled(enabled)` / `SetProgress.labelled(interaction)` stay — stand-ins build their controls disabled (toggle contract), no click guard.
- Book card stand-ins reach composables only through UiState (reading, library, explore, profile); render files keep no stand-in reads.
- `BookCardSelection.Multiple(selected)`; backdrop from placement (over cover → Image, coverless row → Surface); collector backdrop logic goes; `SelectionMark` → data-free `SelectionMark(selected, backdrop = Image)`, no model.
- `BookCardLine` goes → `supportingLines: ImmutableList<ImmutableList<BookCardLinePart>>`.
- `SeriesStack.front`, `CoverOnly.cover` non-null; the `HiddenSeriesStack` fallback goes.
- Restated defaults go (`menuLabel`, `deadline`, `progress`, `Press.Tap`, stand-in `Press.None` and `isLoading = true`).
- Files merge: KeyId → Key only (ktlint one-type-per-file rejects the rest); `BookCardColumn.kt` folds into `BookCard.kt`.
- Stand-in models leave render files for `mapper/` beside their mapper (reading, library, explore, profile).
- Fixes: `BookCardUiModel` size KDoc; search stand-in eyebrow uppercased; gallery coverless `Edition` fixture on Surface (accepted shift).
- Later: non-book-card stand-ins still in render files — explore `FeaturedCard` badge / cover, `MoodGrid` tile, settings `RoadmapContent`.
- Kept: `BookCardSemantics`, `rememberBookCardEventHandler`, Row's body and insets. Deferred: `maskedCover`, series stack (SS-4); `Bookmark` / `SetProgress` wrappers, menu button (SS-6); `Progress.Value.bar`, `maskedBar` (SS-8); fixture pruning (SS-11).

SS-4:
- Contract R2: one variant per distinct look, named after the look; metrics a private `when` beside the component.
- `cover`: `CoverVariant` → one variant per look (`Flat` default for the seven shared surfaces; `ReadingHeroBackdrop` / `FullScreenViewer` go and use `Flat`; ExploreRail + ProfileLovedBook merge; SheetHeaderJacket + ChooseListsSingleJacket merge); drift merges (HiddenSeriesStack 3 → 4 dp joins `Flat`; Reading tiles 6/8/10 → 8 dp, one variant); ~10 variants.
- `CoverDimensions` → private `when` beside `Cover`, decode cap 600 px on the shelf variant only, `BlackAlpha*` inline; `Cover.kt:171` uses `COVER_ASPECT_RATIO`; `CoverlessTitleCover` → `internal`.
- `bookcard`: `maskedCover` goes; stand-in covers carry `isLoading = true` from their mapper; loading preview fixtures get a loading cover.
- uibinding `cover`: `Book.toCoverUiModel` loses `isLoading`.
- explore: `BookCardModelsCollector` maps covers inline; `trendingCovers` / `becauseYouReadCovers` / `continueSeriesCovers` / `queriedBookCovers`, their snapshot fields, `firstResultLacksCover` and `toPreviewCoverModels` go; `CoverModelsCollector` keeps only `featuredCover`.
- `lists`: `ChooseListsUiModel.emptyState` defaulted, `chooseListsEmptyState()` goes; `onClick` pending guard goes; `NewListRow` → `CircleShape`.
- library: `ArrangeMakeOrderedUiModel` → `arrangeMakeOrderedListId: Int?`, label in the composable; make-ordered link moves into the sheet body; one private chip FlowRow replaces `LayoutChipRow` / `SortChipRow`.
- Kept: `CoverSource`, `resolveCoverSource`, `LocalImageSource` actuals, `MonogramCoverMetrics`, `ListMembership`, both choose-lists mappers. Deferred: chip FlowRow → `ChipSet`, `ToggleRow(titleStyle)` (SS-6); fixture pruning, Reading preview cover builds (SS-11).

SS-5–SS-11 (audited together; built in rounds SS-5 + SS-9, SS-6 + SS-8, SS-7 + SS-10, SS-11):
- SS-5 `sheet`: `SheetLayoutSpec` + `spec()` go; `busyDescription` goes; `SheetDismissal` folds into `SheetChrome`; default-arg `rememberSheetChrome()` + "SheetFooter" gallery entry go, `SheetFooter` private; footer buttons → one `FooterButton(button, primary)`; `dismissesSheetOnSelect` goes; `LoadingSheetContent` inlines; `SheetRow` internal.
- SS-5 `dialog` / `callout` / `state`: date picker labels → literals; `BannerTone` / `BannerStyle` / `icon` / `dismissible` → `BannerVariant { Status, Editorial }`; `CalloutTone` + `CalloutLead` + `title` → `sealed CalloutVariant { Neutral(icon?), Highlight(title) }`, `CalloutStyle` → private `when`; `CalloutUiModel.action` → `callToAction`; `EmptyStateStyle` → private Centered / Inline bodies; `OfflineCopy.banner` loses `visible`.
- SS-5 features: explore `ContinueSeriesEntryUiModel` → `continueSeriesMenuSheets` map, the `isEmpty()` loading derivation and the up-next width vals go; orchestration `reAuthSheet` nullable, `reAuthState` public goes, `toScaffoldUiModel` inlines.
- SS-9: `StatNumber` loses `maxLines` / `color` (debug counter colour shift accepted); `StatPulseText`, `TileEyebrow` inline; `StatNumberFormat.Decimal` → `data object`; tile bodies take `model`; `RankedBarUiModel` + `ProportionSegmentUiModel` → `ChartSliceUiModel`; `ChartLegendRow(slice, isLoading, swatch)`; `ChartTone.ranked` goes; `ReadingAtlasUiModel` kept.
- SS-6 `control`: `ToggleVariant.Switch` goes, `ToggleVariant { Filled, Bare }` with `on` / `off` on the model; `IconToggleStyle`, `SegmentColors`, `ProseWordColors` → private `when` / `if`; `ToggleRow` style params → `ToggleRowVariant { Plain, Inset }` (verdict + library type shift accepted); keyed `ToggleRowUiModel<K>` / theme + palette tiles — `SettingsToggleRowUiModel`, `ThemeChoice`, `PaletteChoice` go.
- SS-6 `shape` / `button`: `PillShape`, `TrackShape`, package → `CircleShape`; `PillButtonDimensions` / `PillButtonStyle` go; `PillButtonInteraction` → `enabled`; `PillButtonSet` + `ExternalLinksUiModel` go; library arrange chips → `ChipSet`; `SetProgress.labelled` loses `label`; `ChromeIconButton.modifier` goes.
- SS-6 settings: one `DisplayToggleRowsCollector`; the toggle feeder fields, `useDynamicColorChecked`, `userDateStyle`, `uiScale` go. Kept: `ChipSet`, `MastheadRefreshAction`, book card wrappers, `SettingsChoiceRowUiModel`, `ShareSheetBodyUiModel`.
- SS-8 `progress`: `ProgressBarUiModel` → `ProgressBar(fraction, isLoading)`; `bar` → `fraction` on book card / InProgress / FeaturedProgress / FocusPageEditor, `maskedBar` goes; `actionButtonSize` goes; Page + Percentage tabs share one body, all tabs one footer; `readAt` to UiState, mapper builds `whenRead`; `withTimeDraft` goes, actions write only `progressTimeDraft` (check fast typing on device).
- SS-8 others: deadline `dateText` + `paceText` → `text`, fixtures 5 → 3; `MoodTileDimensions` goes, `isLoading` on model, explore stand-ins from the collector, crossfade goes (accepted); `MarkAsReadBurstUiModel` → `MarkAsReadBurst(triggerKey, scale)`.
- SS-7 `field`: `TextFieldPillDimensions` / `TextFieldPillTone` → private `when`s; one `submittable`; `TextFieldKeyboard` → `numeric`; `placeholderInk` inlines; scan auto-focus seam → `internal expect val`.
- SS-7 `richtext` / reviews: `ClickableText` + model + event → `LinkAnnotation.Url`; formatting toolbar private in `VerdictSheet.kt`; `plainText()` goes; `revealedSpoilerReviewIds` + `loadingReviews` go (mapper stand-ins, no details gate — accepted); rating formatted in mapper.
- SS-10 `share`: Stat / Quote / YearRecap kept; `ShareCardDimensions` → private `when`; `ShareCardNumerals.kt` goes; Book + Update share one header; `quote` goes; `ReadingUpdateKind` + nullables → sealed `Finished` / `Reading`; month normaliser goes; `ShareCard(model =)`; fix: rating uses the ×2 verdict score.
- SS-10 `verdict` / book_detail: `VerdictBlockUiModel` → sealed `Empty` / `Filled`; one `VerdictScoreLine`; `reviewLabel` goes; `verdictReview` state goes; `ShareSheetCollector` merges into `ShareCardsCollector`, one `shareCard` field.
- SS-11: `UiModelPreviews.previews` → label → fixture map, label functions and `*_FLOOR` consts go; masthead entry uses fixtures; registry filter goes; entry files renamed; duplicate fixtures pruned; unread previews deleted, R5 → "rendered or tested"; `formatCompactDate` private; Reading / Explore previews built through mappers.
- Left over: `ShareBookBottomSheet`'s eight callbacks; `VerdictSheet` spoilers model built in composition.
