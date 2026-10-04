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
| SS-4 | `cover`, `lists`; uibinding `cover` (all platforms), `lists`; library `ArrangeMakeOrderedUiModel` — ~2.1k | [ ] |
| SS-5 | `sheet`, `dialog`, `callout`, `state`; explore `ContinueSeriesEntryUiModel`, orchestration `ReAuthSheetUiModel` — ~2.2k | [ ] |
| SS-6 | `control`, `button`; book_detail `ExternalLinksUiModel`, `ShareSheetBodyUiModel`; settings `SettingsToggleRowUiModel`, `SettingsChoiceRowUiModel` — ~1.9k | [ ] |
| SS-7 | `field`, `richtext`; uibinding `richtext`; book_detail `BookReviewUiModel` — ~1.8k | [ ] |
| SS-8 | `progress`, `deadline`, `mood`, `celebration`; uibinding `progress`, `deadline`; book_detail `InProgressUiModel`, reading `FeaturedProgressUiModel`, session `FocusPageEditorUiModel` — ~2.3k | [ ] |
| SS-9 | `statistic`, `chart`; profile `ReadingAtlasUiModel` — ~1.2k | [ ] |
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
