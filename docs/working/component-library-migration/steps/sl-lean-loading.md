# SL — Lean loading

**Stage:** SL — app-wide; sub-steps interleave with S7 and S9 (see "Runs after").
**Delegation:** `softcover-implementer`.

Loading branches across the app swap the real layout for a hand-sized skeleton — `SkeletonCrossfade` plus
`.shimmer` boxes in book detail, whole-surface `Skeleton(SkeletonVariant.*)` in explore and the roadmap,
`.shimmer` over hand-sized blocks in profile — so screens jump when data lands. Every surface instead renders
its real layout and masks only the leaves whose value is unknown. The stage ends with the `Skeleton` family
deleted.

## Sub-steps

| Sub | Scope | Runs after | Status |
|---|---|---|---|
| SL-1 | `SoftcoverText` in `:core:component/text` (replaces `SkeletonText`), with fixtures and a gallery entry | — | [x] |
| SL-2 | Swap every `material3.Text` in `:core:component` to `SoftcoverText`, folding loose font arguments into `style` | SL-1 | [x] |
| SL-3 | The same swap in the feature modules, `:app` and `:orchestration` | SL-2 | [x] |
| SL-3b | Enable the foundation import ban on `material3.Text` outside `SoftcoverText.kt` | foundation F34 | [ ] |
| SL-4 | Seed `title`, `seriesText`, `rating` on `BookInitialCover` and its call sites | SL-1 | [x] |
| SL-5 | Book detail's hero title block renders its real layout and masks unknown leaves | SL-4 | [x] |
| SL-6 | `ShelveControlCard` renders its real rows while loading; the 42dp bars go | SL-1 | [x] |
| SL-7 | `AboutSection`, `ReviewsSection` and `BookOverflowMenu` | SL-1 | [x] |
| SL-8 | Roadmap: `RoadmapContent` renders stand-in entries in the real entry layout; `RoadmapUiState.skeleton` goes | SL-1 | [x] |
| SL-9 | `BookCard` and `MoodTile` take `isLoading` and mask their own leaves, with loading fixtures | S7-4 | [x] |
| SL-10a | Explore feed (featured, trending, up next, because-you-read, mood), mobile and desktop, render their real layout while loading; their `*Skeleton` state fields go | SL-9 | [x] |
| SL-10b | Explore search and hidden suggestions, the same; the remaining `*Skeleton` state fields go | SL-10a, S7-5 | [x] |
| SL-11 | Library and Reading: audit how they show loading; convert any loading branch to the shape below | S7-6 | [x] |
| SL-12 | Profile: sections and both layouts move from `.shimmer` blocks to masked real leaves | S9-3 | [ ] |
| SL-13 | Delete `Skeleton`, `SkeletonUiModel`, `SkeletonVariant`, their gallery entry, `SkeletonUiModelTest` and doc lines | SL-8, SL-10 | [ ] |
| SL-14 | Delayed reveal: `skeletonMask` and `Cover`'s loading shimmer wait out a grace period and hold a minimum once shown | SL-11 | [ ] |

SL-1 to SL-7 run before S7-2. A sub-step whose "Runs after" is open waits; the tracker's `## Now` picks the
next one whose prerequisites are done.

## Approved shape

- No loading UI models and no per-field placeholder wrapper; a component takes `isLoading` and masks its own leaves.
- `SoftcoverText(text: String? | AnnotatedString?, modifier, style, color, isLoading = false, standIn = "",
  textAlign, maxLines, minLines, overflow, softWrap, onTextLayout)`: no model, no loose font parameters
  (bold, italic, spacing and the like go through `style`). While loading, or while `text` is null, it lays out
  `standIn` under `skeletonMask`, so the slot keeps its size.
- SL-2 adds `autoSize: TextAutoSize? = null` to both overloads and `inlineContent` to the `AnnotatedString`
  overload only — layout features, not font arguments — so no `material3.Text` caller is left out.
- Non-text leaves (a cover, a bar, a chart body, a toggle) mask with `Modifier.skeletonMask(isLoading)` over
  their real composable, laid out at their real size.
- **Lists of unknown length:** while loading, a list renders a fixed count of its real items, each with
  `isLoading = true` and stand-in content. The count is the section's typical visible count, written at the
  call site (e.g. 3 rail cards, the mood grid's tile count). No `Skeleton` per list.
- SL-6: a shelve row is masked whole while loading, laid out at its real size.
- `BookInitialCover` seeds `title`, `seriesText` and `rating` from `fromBook`, and `title` from `fromEdition`.
  A null `seriesText` means unknown and masks; blank means the book has no series and renders nothing.
- Author and release year keep coming from `displayedEdition`, which already falls back to `initialCover`.
- `Cover` keeps its own `CoverUiModel.isLoading` shimmer (timing per SL-14).
- `skeletonMask` moves to the foundation later (F32); SL builds on the local copy.
- SL-11 Library: a null tab list, or an empty one while loading, renders the real grid with N
  `BookCard(isLoading = true)` stand-ins — 2-col 6, 3-col 9, cover-only 12, large list 6, compact list 10,
  desktop 12, counts at the call site.
- SL-11 Library: an unmapped card renders a stand-in `BookCard(isLoading = true)` of its layout's shape, not a
  bare `Cover(null)` / nothing.
- SL-11 Library: stand-in cards live feature-local (mirroring `ExploreStandIns.kt`), one per `BookCardVariant`.
- SL-11 Library: the masthead subtitle masks a count-shaped stand-in instead of "Loading your shelf…".
- SL-11 Reading: the `isLoading -> Unit` branches go (mobile and jvm); loading renders masthead, a loading
  `FeaturedBookCard` and 2 stand-in rows.
- SL-11 Reading: `FeaturedBookCard` takes `isLoading` plus a stand-in; text leaves via `SoftcoverText`,
  cover / backdrop / progress bar via `skeletonMask`.
- SL-11 Reading: an unmapped also-reading row renders a stand-in `BookCard(isLoading = true)`.
- SL-11 Reading: the pick-up-next field becomes nullable (null = loading; no separate flag); loading renders
  3 stand-in tiles. The masthead eyebrow copy stays (refresh feedback).
- SL-14: local-cache screens (Library, Reading) load in a few frames, so the shimmer only flashes. The
  timing lives in `skeletonMask`, not per screen: for the first 150ms of loading the leaf draws nothing
  (content hidden, layout kept); then the shimmer shows and, once shown, stays at least 300ms even if
  loading ends sooner. Both values are constants beside the mask. A load that ends inside the grace period
  shows its content with no shimmer.
- SL-14: the timing is a plain function (elapsed time, loading → hidden / shimmer / content), unit-tested;
  the modifier only drives it with a clock. Leaves composed in the same frame share the same start.
- SL-14: `Cover`'s own `isLoading` shimmer follows the same timing.
- SL-14: app-wide, explore and book detail included (accepted: network surfaces show their shimmer 150ms
  later). F32 carries the timing upstream.

## Inventory (current paths)

Paths are under `feature/<module>/src/<sourceSet>/kotlin/nl/rhaydus/softcover/feature/<module>/presentation/`.

| Sub | Symbol | Current path:line | Loading today |
|---|---|---|---|
| SL-2 | `material3.Text` importers | `:core:component`, 40 files | — |
| SL-3 | `material3.Text` importers | book_detail 15, settings 14, explore 12, profile 11, library 10, reading 9, scan 5, onboarding 3, session 2, lists 1 — one implementer per module group | — |
| SL-5 | `GeneralBookInfoSection` title block | book_detail common `screen/section/GeneralBookInfoSection.kt:179` | `SkeletonCrossfade`; separate bar layout for series, title, byline, two stat columns |
| — | hero `Cover` | `GeneralBookInfoSection.kt:131` | already real; shimmers via `CoverUiModel.isLoading` — leave as is |
| SL-6 | `ShelveControlCard` | book_detail common `screen/section/ShelveControlCard.kt:78` | `SkeletonCrossfade`; three 42dp bars in a `surfaceContainerLow` surface |
| SL-7 | `AboutSection` | book_detail common `screen/section/AboutSection.kt:37` | `SkeletonCrossfade`; shimmer box layout |
| SL-7 | `ReviewsSection` | book_detail common `screen/section/ReviewsSection.kt:88` | `SkeletonCrossfade`; review-card-shaped skeleton |
| SL-7 | `BookOverflowMenu` | book_detail common `screen/section/BookOverflowMenu.kt:37` | returns early; the top bar loses its trailing slot |
| SL-4 | `BookInitialCover.fromBook` call sites | `LibraryScreen`, `ReadingScreen`, `ExploreScreen`, `BarcodeScannerScreen` | — |
| SL-8 | `RoadmapContent` | settings common `screen/RoadmapContent.kt:72`; `state/RoadmapUiState.kt:23` | `SkeletonCrossfade` + `Skeleton(RoadmapEntry)` |
| SL-10 | `FeaturedSection` / `DesktopFeaturedSection` | explore mobile `screen/section/FeaturedSection.kt:38`; jvm `DesktopFeaturedSection.kt:37` | `Skeleton(ExploreFeatured)` |
| SL-10 | `TrendingSection` / `DesktopTrendingSection` | explore mobile `TrendingSection.kt:58`; jvm `DesktopTrendingSection.kt:42` | `Skeleton(ExploreRail)` |
| SL-10 | `ContinueSeriesSection` / `DesktopUpNextSection` | explore mobile `ContinueSeriesSection.kt:62`; jvm `DesktopUpNextSection.kt:51` | `Skeleton(ExploreSeries)` |
| SL-10 | `BecauseYouReadSection` / `DesktopBecauseYouReadSection` | explore mobile `BecauseYouReadSection.kt:64,80,96`; jvm `DesktopBecauseYouReadSection.kt:51,66,82` | `Skeleton(ExploreSectionHeader)` + `Skeleton(ExploreRail)` |
| SL-10 | `MoodGrid` | explore common `screen/section/MoodGrid.kt:86` | `Skeleton(ExploreMoodTile)` |
| SL-10 | explore skeleton state | explore common `state/ExploreScreenUiState.kt:63,70,108,117,118,132` | six `SkeletonUiModel` fields |
| SL-11 | library / reading loading | library `LibraryScreenLayout.mobile.kt`, `section/BookList.kt`, `section/EditionList.kt`; reading `ReadingScreenLayout.{mobile,jvm}.kt`, `section/ReadingMasthead.kt` | unknown — audit |
| SL-12 | profile sections | profile common `screen/section/`: `RatingsHistogramSection.kt:80,111`, `GenreRankingSection.kt:83,199`, `YearColumnHistorySection.kt:94`, `ReadingAtlasSection.kt:121,178,227`, `AuthorRepresentationSection.kt:122,135,222,243,395,413,460`, `ShareEntryRow.kt:48` | `.shimmer(isLoading)` over hand-sized blocks |
| SL-12 | profile layouts | profile mobile `ProfileScreenLayout.mobile.kt:272,312`; jvm `ProfileScreenLayout.jvm.kt:414,429` | `.shimmer(isLoading)` |

The book detail loading gate is `state.loadingBookDetails && state.book == null`, at both
`BookDetailScreenLayout` actuals (mobile and jvm). Sections gated on content alone (`TagsSection`,
`ExternalLinksSection`, `InProgressSection`, `YoursLensContent`) stay absent while loading and are out of scope.
