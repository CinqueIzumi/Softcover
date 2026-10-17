# Design System — Layout primitives

## 3. Layout primitives

### 3.1 Page scaffold

The scaffold and the bottom-bar padding contract are [fd §5.2](../../rhaydus/0.3.1/design-system-foundations.md#52-page-scaffold-and-bottom-bar-padding). Every nav shell hosts its bottom chrome in `BottomBarScaffold`: the compact shell hosts the docked or floating bar, and the wide shell (rail / sidebar) hosts the flush `SessionPeekBar` as overlay chrome with no breathing gap. A screen never branches on the user's `BottomBarStyle`, which governs only the compact bar.

**Every surface the shell hosts reserves `rememberBottomBarPadding()`.** That includes the empty state, the error / offline state, the search-results list, the desktop sidebars and their pinned footers, the bottom-anchored save bars (through the `DesktopScrollPage` bottom-bar slot), and every `jvmMain` layout (the peek bar overlays them too). Where a surface already carries a bottom margin, the padding is added to it (`bottom = 24.dp + rememberBottomBarPadding()`). A shell-hosted scroll surface without the padding is a review blocker. A full-pane `EmptyState` takes `fillMaxSize()` (plus that padding when shell-hosted) outside any scroll column; an empty state that is one section of a page stays in flow.

Screens **pushed onto the root navigator** (profile, settings sub-pages, hidden suggestions, focus mode, the scanner, onboarding, and book detail on compact) render outside the shell, where `rememberBottomBarPadding()` is `0.dp`. They clear the system navigation bar through their `Scaffold` insets or `BottomNavigationSpacer()` (the navigation-bar inset only). A pushed screen's `Scaffold` `bottomBar` gets no inset from the `Scaffold`, so the bar pads `navigationBars` inside its own container, passed in as a `windowInsets` parameter as with `Banner`.

**`bottomChromePadding()`** (`core/designsystem/…/presentation/layout/`) is for book detail, the one surface rendered both pushed (compact) and in the expanded two-pane detail slot. It returns the maximum of the bar footprint and the navigation-bar inset. A surface that only renders inside the shell reads `rememberBottomBarPadding()`.

Top bars come from the shared top-bar components: `TopBar` (title, back, trailing actions), `SearchTopBar` (Explore's mobile search chrome), and on desktop `EditorialTopBar` and `DesktopScrollPage`. No screen rolls its own, and mobile search input never embeds a Material `TextField` or `AppBarWithSearch` / `SearchBarState`.

**Root tabs use an in-page `pageTitle`** (optionally with a body subtitle) rather than a chrome title; `TopBar` with a `title` is for pushed sub-screens (settings sub-pages, profile, book detail). Library and Settings follow this; Explore has no title (its search bar is the chrome).

### 3.2 Section rhythm

[fd §5.3](../../rhaydus/0.3.1/design-system-foundations.md#53-section-rhythm): eyebrow (all-caps, primary), italic display headline (on-surface), body, then a generous gap.

### 3.3 Hero region

[fd §5.4](../../rhaydus/0.3.1/design-system-foundations.md#54-hero-region). Hero text uses `statHero`, `display`, or a quote (oversized low-alpha `quoteGlyph` behind an italic headline).

### 3.4 Carousels and cards

[fd §5.5](../../rhaydus/0.3.1/design-system-foundations.md#55-cards-and-rows--reserved-row-rule). Horizontal carousels are the default for collections of books, with the 24dp content padding so the clipped card never butts flush. Card anatomy, top to bottom:

1. Cover image (book proportions, subtle radius and a slight shadow).
2. Title in `titleMedium` or `titleSmall`, max two lines.
3. Optional metadata strip in `eyebrowSmall` or `bodySmall`, on-surface-variant.
4. Optional trailing accent (rating, progress fragment) in primary.

Vertical lists use the same anatomy laid horizontally: cover at the leading edge, text block taking the remaining width.

### 3.5 Modal sheets

[fd §5.6](../../rhaydus/0.3.1/design-system-foundations.md#56-modal-sheets) and [fd §5.9](../../rhaydus/0.3.1/design-system-foundations.md#59-adaptive-modal-sheet-sheet-to-panel). The header uses `eyebrow` + `display` / `headlineMedium` + `body`. The one exception to the canonical header is the [title-page sheet](patterns/explore-profile.md#title-page-sheet-create-list).

**Every modal sheet uses `nl.rhaydus.softcover.core.component.sheet.AdaptiveModalSheet`**, never a raw `ModalBottomSheet`. Compact and medium delegate to the foundation bottom sheet. On expanded it is a centered `surfaceContainerLowest` panel: up to 420dp wide, no border, 6dp shadow elevation, 28dp corners, capped at 90% of the window height (content scrolls within), no drag handle, scaling and fading in over its own scrim. `dismissOnTapOutside` / `dismissOnBackPress` cover a blocking loader; every close path plays the panel's exit before `onDismissRequest`.

A body reads `LocalModalSheetForm` (`SHEET` / `PANEL`) to adapt to the form, never the window width: the library filter's facet chips scroll horizontally on the sheet and wrap behind an `ExpandableFlowRow` on the panel. An in-content dismiss calls `LocalModalSheetDismiss`.

A `SheetLayout.Pinned` sheet whose body is a list is built with `LazySheetScaffold`, which owns the `LazyColumn` (the body is `LazyListScope` content, with an optional fixed `pinned` slot above it; `fillHeight` gives the list all the height the sheet leaves) and gives it a `Spacing.md` bottom content padding when a footer is present, so rows scroll right up to the footer divider instead of stopping at a padded band.

### 3.6 Adaptive content width

The two-pane split and width caps are [fd §5.8](../../rhaydus/0.3.1/design-system-foundations.md#58-adaptive-content-width-and-the-two-pane-split). On a wide window (§2.7):

- **Reading surfaces** (book-detail body, settings and other forms, profile, any single reading column) apply `Modifier.cappedContentWidth()` (`ContentMaxWidth.Reading`) to the scrolling column, with the gutter inside the cap. Full-bleed media (cover art, hero backdrops, the cover-to-detail morph target) stays outside the cap.
- **List/grid surfaces** cap at `ContentMaxWidth.Pane` and add columns as width grows.
- `TwoPaneScaffold` backs the expanded **[list–detail two-pane](patterns/cross-cutting.md#list-detail-two-pane)**. The shell also hosts non-pane expanded tabs (desktop Library, Settings) in it with a `null` detail, so they share the same stable list slot.
