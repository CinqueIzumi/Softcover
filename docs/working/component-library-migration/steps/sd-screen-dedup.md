# SD — Screen de-duplication

**Stage:** after TP, before PF and S10, so later stages change one screen body, not two.
**Delegation:** parallel read-only agents draft Phase 1 and the user approves it; parallel `softcover-implementer`s build Phase 2.

**Goal:** a screen whose mobile (`mobileMain` / `commonMain`) and desktop (`jvmMain`) layouts render the
same sections draws each section from one shared composable; each platform keeps only its container
(lazy list versus scrolling column, panes, gutters). Drift between the copies is the bug class this
removes — desktop Reading rendered stand-in rows forever with one book because its also-reading list sat
outside the guard mobile had.

## Procedure

**SD-0 first, in its own session:** inventory every `jvmMain` / `desktopMain` screen or section file
(`*.jvm.kt`, `Desktop*.kt`) and pair it with its mobile counterpart. Per pair, list the sections both
render and how the bodies differ (guards, ordering, spacing, state reads). Table the pairs below with a
line count, one sub-step per session-sized group.

Per sub-step: propose the shared section composables (parameters = state slices + `onEvent`; no layout
container inside), what stays platform-specific and why, and any behavioural difference between the
copies — each one is a bug to fix or a deliberate platform choice for the user to confirm. No visual
change unless the user accepts it (log it in [`visual-checks.md`](../visual-checks.md)).

SD-1…SD-6 run fanned out, not one per session:

1. **Phase 1, all at once.** One read-only agent per sub-step drafts its proposal in parallel. The user approves
   all six in one review pass, and each approved shape goes under `## Approved shape`.
2. **Core-module sub-steps first, alone.** A sub-step whose approved shape adds to a shared module
   (`:core:component`, `:core:uibinding`, any `:core:*`) builds on its own before the rest.
3. **Phase 2, two or three at a time.** Parallel `softcover-implementer`s, each on a disjoint feature module,
   in the shared checkout; their Gradle runs queue on the project lock. Build per
   [`family-procedure.md`](../family-procedure.md) § Phase 2.
4. **One test and review pass per batch.** After each parallel batch is green, one `softcover-test-writer` and
   one `softcover-reviewer` run over that batch's changes.

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| SD-0 | Inventory mobile / desktop screen pairs; fill this table | [x] |
| SD-P | Desktop page shell in `:core:component` `jvmMain` (core first, alone) | [x] |
| SD-1 | Explore: feed sections, search results, hidden suggestions | [ ] |
| SD-2 | Reading and Library | [x] |
| SD-3 | Book detail and full-screen cover | [x] |
| SD-4 | Profile | [ ] |
| SD-5 | Settings: standalone-screen shell, pane shell, sidebar | [ ] |
| SD-6 | Onboarding, scan, focus mode | [ ] |

## Inventory

Line counts are desktop / mobile. "Drift" marks a difference that is a bug to fix or a platform choice the user
must confirm.

- **SD-1 Explore.**
  - Pairs:
    - `ExploreScreenLayout` (129 / 408)
    - `DesktopDiscovery` ↔ `EditorialContent` (90 / 112): the same six sections in the same order
    - `DesktopFeaturedSection` ↔ `FeaturedSection` (49 / 49)
    - `DesktopUpNextSection` ↔ `ContinueSeriesSection` (66 / 79)
    - `DesktopBecauseYouReadSection` (89 / 102)
    - `DesktopTrendingSection` (68 / 84)
    - `DesktopSearchResults` ↔ `ActiveSearchContent` (154 / 153): the load-more effect and card handler are near-verbatim
    - `HiddenSuggestionsScreenLayout` (84 / 69)
  - Drift:
    - Desktop never renders `searchError`, so a failed search shows no error and no retry.
    - The Featured gutter is 16 on mobile and 24 everywhere else.
    - Rail stagger is mobile-only.
    - Desktop hidden suggestions has no vertical padding.
  - Platform: `FlowRow` vs `LazyRow`, results grid vs column, card widths, stand-in counts 8 / 3, persistent field vs
    the four-phase search, masthead refresh vs pull.
- **SD-2 Reading and Library.**
  - Reading `ReadingScreenLayout` (269 / 518, plus `ReadingBooksColumn` 165):
    - Desktop `DesktopReadingContent` forks `ReadingBooksColumn`'s card-event `when`, `FeaturedBookCard` and
      also-reading block.
    - The fork drops `mutationAnimated` and `staggeredEntry`.
    - `ReadingBooksColumn`'s KDoc wrongly says desktop uses it.
    - The one-book stand-in bug is fixed: both platforms guard on `alsoReading.isEmpty`.
  - Library `LibraryScreenLayout` (405 / 536):
    - Verbatim copies: the four sheet blocks, the selection-header wiring, and the search / control-line block.
    - Drift: desktop filter chips lack `AnimatedVisibility`; desktop stand-ins are a flat 12 while mobile varies per
      layout; desktop never reads `topAppBarState`.
  - Platform: sidebar vs pager, a non-lazy Reading scroll (documented), adaptive grid columns.
- **SD-3 Book detail.**
  - `BookDetailScreenLayout` (344 / 361):
    - The desktop single column mirrors the mobile item list.
    - The `BookHero` shake wrapper and the scan-edition `Callout` item are verbatim.
    - Drift: two-column `TwoColumnContent` never renders `scanEditionCallout`.
  - `FullScreenCoverScreenLayout` (93 / 61): the double-tap block is verbatim.
  - Platform: the 720 dp split, static top bar vs the over-media swap, mouse vs touch zoom.
- **SD-4 Profile.**
  - `ProfileScreenLayout` (505 / 363). Section order and state reads match, and every section is already common.
  - Duplicated: the share and log-out overlay block, the share `IconButton`, and `ReaderIdentity` vs `ProfileHeader`.
  - Drift:
    - Desktop has no rule-bullet-rule bio separator.
    - Desktop single-column `widthIn(max = 640.dp)` comes after `fillMaxSize`, so it has no effect.
    - The avatar is 140 / 168.
    - The AccountFoot spacing differs.
- **SD-5 Settings.**
  - Every body is already a shared `*Content`; only the shells are duplicated:
    - The four standalone `*ScreenLayout.jvm.kt` files (About 83, Appearance 73, Gallery 73, Roadmap 76) share one
      shell.
    - `AboutPane`, `AppearancePane` and `RoadmapPane` share another; the two shells disagree on cap placement,
      vertical and bottom-bar padding, and the masthead.
    - `LibraryTabsPane` (62) is a line-for-line copy of `LibraryVisibilitySettingsScreenLayout.jvm.kt` (80).
    - The sidebar is 122 + 65 on desktop; the menu is 248 on mobile.
  - Drift:
    - Profile row label: "Your profile" vs "View user profile".
    - Hidden suggestions sits under Privacy on desktop and Personalisation on mobile.
    - The section is spelled both "Personalise" and "Personalisation".
    - The About icon is `Settings` vs `Info`.
    - Mobile About and Appearance lack `fillMaxSize`.
    - Desktop standalone Roadmap has no header.
    - `LibraryTabsPane` and the desktop standalone screens lack `rememberBottomBarPadding`.
- **SD-6 Small pairs** (mostly platform):
  - Onboarding (143 / 518) collapses the pager into one panel.
    - Drift: it hard-codes the "Welcome" eyebrow instead of reading `pageHeaders`, and it drops the "Powered by
      Hardcover" page and the "Add your key." copy.
  - Scan (83 / 115): desktop is manual entry only, with `ManualEntryContent` shared.
  - Focus mode (102 / 82): only the container, cover size and insets differ.

## Batch close-out (SD-2 / SD-3), done

- [x] `api(project(":core:component"))` in `feature/reading` and `feature/book_detail` build files (`projectHealth`; reading's is pre-existing).
- [x] `document-code` over the new section files and the KDoc they changed.
- [x] Test-writer (`LibraryStandInCount.jvm` dropped: literal table, no jvmTest set): `BookDetailUiState.heroReleaseYear` (year -1 falls back to the book's), `failedHeroEditionId`, `CoverZoomState` (double-tap toggle, `zoomBy` clamps).
- [x] Reviewer over SD-P + SD-2 + SD-3; then rerun `styleCheck`.
- [x] `visual-checks.md`: desktop row stagger, desktop library chip animation, desktop list stand-in counts, desktop two-column scan callout.

## Approved shape

Shared section composables take state slices + `runAction`, the feature's existing convention, and contain no
layout container. Every visual change below goes in `visual-checks.md`. Build order: SD-P alone, then SD-1 + SD-2
+ SD-3, then SD-4 + SD-5 + SD-6.

- **Cross-cutting:** desktop rails and rows adopt `staggeredEntry`, gated on loading like mobile. `mutationAnimated`
  stays mobile-only because it needs a `LazyItemScope`.
- **SD-P:** one desktop scroll-page shell in `:core:component` `jvmMain`, a container with slots and no UI model.
  - Layout: an optional `EditorialTopBar`, then a scroll column with 24 top and bottom padding. Inside it, a capped
    inner column with a 32 gutter, an optional `PageMasthead` with a 28 spacer, then the content. A
    `DesktopVerticalScrollbar` sits on top.
  - Bottom: an optional docked bottom bar. `rememberBottomBarPadding()` applies always, to the bottom bar when there
    is one, otherwise to the scroll column.
  - Consumers: the settings screens and panes (SD-5) and explore hidden suggestions (SD-1). Add a line for it to
    `module-structure.md`.
- **SD-1 Explore:**
  - New commonMain sections: `FeaturedSection`, `UpNextSection` (replaces `ContinueSeriesSection` /
    `DesktopUpNextSection`), `BecauseYouReadSection` and `TrendingSection`. Each takes a
    `rail: @Composable ((BookCardEvent) -> Unit) -> Unit` slot.
  - `DiscoverySections` emits the six sections in order, with an `ExploreRail` enum of
    UP_NEXT / BECAUSE_YOU_READ / TRENDING.
  - `SearchResultsSection` owns the `searchError` guard and the card handler, with `LoadMoreSearchResultsEffect` and
    `LoadingMoreIndicator` beside it.
  - Each platform keeps one `ExploreCardRail`: `LazyRow` on mobile, `FlowRow` on desktop.
  - Delete the four `Desktop*Section` files and the four mobile section files.
  - Fixes: desktop gets the search error with retry; the Featured gutter is 24 on both platforms; hidden suggestions
    moves onto the SD-P shell.
- **SD-2 Reading and Library:**
  - Reading (commonMain sections): `ReadingFeaturedSection`, `AlsoReadingHeader` and
    `rememberReadingCardEventHandler`. Each platform loops the rows itself.
  - `ReadingBooksColumn` moves to mobileMain, and its KDoc is corrected. The prefetcher provider moves up to
    `ReadingScreen.Content`, so the empty state's pick-up-next cards prefetch.
  - Library (commonMain sections): `LibrarySheets`, `LibrarySelectionHeader`, `LibrarySearchAndControls`,
    `LibraryActiveFilterChips` (with mobile's `AnimatedVisibility`) and `LibraryEditModeBackHandlers`.
  - Desktop `LibraryStandInCount` keeps 12 for grids and takes the mobile counts for list layouts.
- **SD-3 Book detail:**
  - commonMain `BookHero` and `ScanEditionCallout`. `BookDetailItems.kt` holds the `LazyListScope` extensions
    `lensItems` and `bookDetailItems`.
  - New `BookDetailUiState` getters `heroReleaseYear` and `failedHeroEditionId` replace values computed in
    composition.
  - The desktop two-column sidebar renders the scan-edition callout below `ShelveControlCard`, with a 20 gap.
  - A `@Stable CoverZoomState` with `rememberCoverZoomState()` goes in `FullScreenCoverShelf.kt` and replaces both
    double-tap and zoom copies.
  - Kept as is: the offline overflow icon on desktop, and each platform's bottom inset.
- **SD-4 Profile:**
  - commonMain `ColumnScope.ProfileReadingLife` (Atlas … RecentlyLoved), `ProfileIdentity` (avatar size and
    alignment as parameters, with the bio separator on both platforms), `ProfileShareButton` and `ProfileSheets`.
  - Mobile moves its 24 gutter onto the column.
  - Delete the ineffective desktop `widthIn(640)`.
  - Spacing: header to atlas is 36 on both platforms; desktop single column uses mobile's 40 + 24 AccountFoot
    spacing; two-column keeps 28.
  - Avatar stays 140 / 168.
- **SD-5 Settings:**
  - The five standalone screens and the panes use SD-P. Delete `AppearancePane`, `LibraryTabsPane` and
    `RoadmapPane`; `AboutPane` keeps only the callout and the debug section.
  - Copy: "Your profile"; hidden suggestions under Personalisation, dropping the Privacy section; "Personalisation"
    everywhere; About icon `Info`.
  - Mobile About and Appearance get `fillMaxSize`.
  - Standalone Roadmap stays without a masthead.
- **SD-6:**
  - `LoadingSheet` moves into `OnboardingScreen.Content()`, and `onInitializingComplete` leaves the expect / actual
    functions.
  - The desktop eyebrow reads `pageHeaders.getValue(IntroScreen.FIRST).eyebrow` with its current styling.
  - Scan and focus mode: no change.
