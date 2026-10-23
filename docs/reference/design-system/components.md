# Design System — Components

One line per `:core:component` component, with its package. Its contract is the KDoc on its UI model; the
rules all components follow are in [component-contract.md](component-contract.md).

## Navigation & chrome

- **TopBar** — `topbar` — page title, back and trailing actions, opaque or over media.
- **EditorialTopBar** — `topbar` — desktop page bar with a start-aligned editorial headline.
- **SearchTopBar** — `topbar` — mobile search chrome.
- **ChromeIconButton** — `chrome` — icon-only bar button with a tooltip; `overMediaIconButtonColors()` over media.
- **SectionHeader** — `header` — region opener in three accent-bar registers.
- **PageMasthead** — `header` — a page's title block; `MastheadSubtitle` is plain text or springing counts.
- **MastheadRefreshAction** — `header` — the masthead's trailing refresh button.
- **Sidebar** — `sidebar` — desktop source list of grouped, selectable rows.
- **DesktopScrollPage** — `layout` (jvmMain) — desktop scroll page shell with top bar, masthead and bottom bar slots.

## Buttons & controls

- **PillButton** — `button` — action pill or icon-only circle in five tones.
- **MorphingButton** — `button` — text button that animates between the filled and tonal styles.
- **Toggle** — `control` — filled or bare icon toggle, or a display-only switch inside a row that owns the tap.
- **ToggleRow** — `control` — a title over an optional gloss with a trailing switch; the row is the tap target.
- **TextField** — `field` — pill, outlined, multi-line, hero or numeral input, with an optional error state.
- **SegmentedControl** — `control` — one-of-N choice as a pill track or as words in a sentence.
- **ThemePreviewTile** — `control` — mini-page theme choice.
- **ColorPalettePreviewTile** — `control` — mini-page spine-colour choice.

## Chips

- **Chip** — `chip` — the one chip, in eight tones with leading and trailing slots; a trailing `Disclosure` is a drop-down arrow that turns over while expanded.
- **ChipFlow** — `chip` — wraps a `ChipSet` in one flow row.

## Covers & cards

- **Cover** — `cover` — the only book-cover renderer; falls back to a coverless title jacket, never blank.
- **CoverOverlay** — `badge` — pins a `Badge` to a cover's corner, optionally greying the cover.
- **BookCard** — `bookcard` — the one book or series card: cover-only, column or row.
- **BookCardRail** — `bookcard`- **QuoteCard** — `quote` — one saved quote in the editorial quote pattern, with a Delete menu.
- **MoodTile** — `mood` — a mood's title and book count on one of four inks.

## States & feedback

- **Banner** — `callout` — full-width strip for a screen-level condition or a card-fused nudge.
- **Callout** — `callout` — in-flow card for one condition, optionally with one next step.
- **EmptyState** — `state` — placeholder when a surface has nothing to draw.
- **OfflineEmptyStatePlaceholder** — `state` — the full-pane `EmptyState` for content that cannot load offline.
- **SkeletonMask** — `state` — `Modifier.skeletonMask` shimmers a leaf while loading.
- **RevealedWhenPresent** — `state` — expands a row while its value is non-null.
- **Divider** — `divider` — hairline rule, bare, centred or vertical.
- **Badge** — `badge` — short status or release label in six tones.
- **DeadlineRow** — `deadline` — "Finish by" card with a status `Badge` and goal sentence.
- **DeadlineSummaryLine** — `deadline` — the deadline date beside its pace or status.
- **SelectionMark / SelectionCheck** — `selection` — circle mark on a cover or card; space-keeping check for rows.
- **Modifier.reorderLift** — `reorder` — the one drag-to-reorder lift.
- **MarkAsReadBurst** — `celebration` — the mark-as-read particle burst.
- **SoftcoverSnackbarHost** — `snackbar` — one per window (app root, each sheet); only the topmost draws.

## Sheets & dialogs

- **AdaptiveModalSheet** — `sheet` — bottom sheet, or an animated panel when expanded.
- **SheetScaffold** — `sheet` — chrome every modal sheet shares: header, body, footer and close; `SheetSession` holds its lifetime.
- **SheetRowGroup** — `sheet` — hairline-divided single-choice rows that dismiss the sheet on selection.
- **SheetActionRow** — `sheet` — a plain tappable menu row that acts at once.
- **LoadingSheet** — `sheet` — full-screen blocking wait.
- **SoftcoverDatePickerDialog** — `dialog` — single-day calendar dialog.
- **UpdateProgressBottomSheet** — `progress` — pages, percentage or time reading-progress editor.
- **QuoteSheet** — `quote` — the add-a-quote editor: passage, position, privacy and backdate.
- **ProgressBar** — `progress` — determinate wavy bar.
- **ChooseListsBottomSheet** — `lists` — adds or removes one or many books to the user's lists.

## Statistics

- **StatNumber** — `statistic` — springs or rolls a number into place, with an optional prefix or suffix.
- **StatTile** — `statistic` — a labelled stat on a hero, standard or compact tile.
- **ColumnChart** — `chart` — vertical columns on one baseline with a per-column readout.
- **RankedBars** — `chart` — a bar per category, each on its own 100% track.
- **ProportionChart** — `chart` — one stacked bar of parts of a whole, with its legend.

## Share

- **ShareCard** — `share` — renders any `ShareCardUiModel`; `ScaledShareCardPreview` scales it.
- **ShareCardSheetContent** — `share` — the one share sheet; owns capture, busy and snackbars.

## Text & editorial

- **SoftcoverText** — `text` — the one text leaf; shows a stand-in under the skeleton mask while loading.
- **FadeThroughText** — `text` — `SoftcoverText` that fades when its text changes.
- **RichText** — `richtext` — read-only formatted, spoiler-maskable prose.
- **MarkdownDocument** — `document` — a parsed markdown document as an editorial spread; links leave as `LinkClicked`.
- **VerdictSheet** — `verdict` — the combined rating-and-review editor.
- **VerdictBlock** — `verdict` — book-page rating and review card that opens the verdict sheet.

## Modifiers & helpers

- **`tactileClickable` / `Selectable` / `Toggleable`** — `modifier` — the one ripple-less interaction modifier.
- **`shakeOnError`** — `modifier` — shakes a node and plays *reject* on a failure trigger.
- **`navSharedBounds`** — `modifier` — the one way to make a node a navigation shared element.
- **`rememberPulseScale`** — `modifier` — a one-shot scale pulse each time a trigger changes.
- **`ExitingItems`** — `transition` — keeps removed items of a non-lazy container composed, inert, while a caller-supplied exit (default fade) plays.
- **`rememberReorderHaptics`** — `haptics` — lift, per-target tick and drop haptics for drag-to-reorder.
- **`Haptics.play(Feedback)`** — `haptics` — plays the outcome haptic a `FeedbackEvent` carries.

## From the foundation

Foundation components are listed in [`docs/rhaydus/0.3.1/CAPABILITIES.md`](../../rhaydus/0.3.1/CAPABILITIES.md).
The app's own usage rules:

- `Cover` for book covers; `RhaydusShimmerImage` for every other image.
- `RhaydusButton`: one filled button per region.
- `InlineErrorState` takes the editorial `bodySmall` face through `textStyle`.
- `DropCapText` takes `dropCapFontFamily = displayFontFamily()`.
- `StarRatingInput` and every rating glyph fill with `RatingGold`.
- `HeroStatNumberField` for any number edited in a hero context, never a bordered field.
- `EditorialSearchField` for search inside an editorial header; `SearchTopBar` when search is the app bar.
- `ExpandableFlowRow` for a wrapping chip set that can run long.
- Pull-to-refresh draws `PullToRefreshDefaults.LoadingIndicator`, the one exception to wavy progress, under a `PullToRefreshEyebrow`.
- `DesktopTooltip` on every icon-only desktop control; `DesktopContextMenu` mirrors a touch long-press.
- Haptics go through `LocalHaptics.current`, never `performHapticFeedback`.
- Press feedback goes through `tactileClickable`, never the foundation `pressScale` modifiers (detekt `ForbiddenImport`).
