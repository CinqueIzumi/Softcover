# S9 — Statistics & progress

**Stage:** S9 — Statistics. **Delegation:** user (Phase 1), `softcover-implementer` (Phase 2).

The `/dataviz` skill conventions apply to everything in the chart group.

S9-2's Phase 1 settles the chart model in full, including how S9-3's charts and legends map onto it;
every chart brief names the `/dataviz` conventions it must follow, and the user checks chart colours in
both themes in the gallery.

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S9-1 | Stat tiles | [x] |
| S9-2 | Chart models and the first charts | [ ] |
| S9-3 | The remaining charts and legends | [ ] |
| S9-4 | Progress | [ ] |
| S9-C | Convergence pass over the family (family-procedure.md § Phase 3) | [ ] |

## Approved shape

S9-1 (approved 2026-10-05):

- `StatTile(model, modifier)` + `StatTileUiModel(eyebrow, stat: StatNumberUiModel, variant: StatTileVariant, isLoading)` in `core/component/statistic/`; no event (inert).
- `StatTileVariant`: `Hero(caption)` (primary fill, 28 dp, autosized `statHero`, divider, caption), `Standard(caption)` (`surfaceContainerHigh`, 24 dp, `statLarge`, caption), `Compact(unit)` (`surfaceContainer`, 20 dp, `headlineMedium` + trailing unit); one private scaffold, per-variant style/metrics table.
- Loading (R12): container, eyebrow and caption/unit stay real; only the number masks. `StatNumberUiModel` gains `isLoading`; `StatNumber` hands it to `SoftcoverText` with a per-format stand-in (`Grouped` "0,000", `Plain` "00", `Decimal` "0.0"). Replaces the three `.shimmer` tiles.
- Folds in: `HeroStatCard`, `StatTile`, `SmallStatTile` (Reading atlas).
- Stays: `ReadingLifeFooterStat` — share-card family, different anatomy, no second consumer; nothing to migrate.
- Moves to S9-4: `FeaturedProgressStat` — a progress readout, not a tile.
- Mapper feature-local in profile: `readingAtlasUiModel(profileData: UserProfileData?)` → feature-local `ReadingAtlasUiModel(hero, volumes, rating, streak)`; null builds the loading state. `ProfileUiState.readingAtlas` defaults to it; `UserInformationCollector` rebuilds it. `ReadingAtlasSection` takes `readingAtlas`, drops `userProfileData`.
- The rating tile's "★" stays unit text (UI preserved).

## S9-1 — Stat tiles (7 → `StatTile` + `StatTileUiModel`)

`AnimatedStatNumber` (× 2 overloads) and `StatPulseText` already migrated in S4-5a as `StatNumber` +
`StatNumberUiModel` + `StatNumberFormat` (`core/component/statistic/`) — this sub-step migrates only
the *tile*, which already calls the library `StatNumber` inside each bespoke tile.

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `ReadingLifeFooterStat` | `core/component/share/ReadingLifeShareCardBody.kt:417` | private to the share-card body |
| `HeroStatCard` | `feature/profile/presentation/screen/section/ReadingAtlasSection.kt:105` | already calls `StatNumber` inside |
| `StatTile` | `feature/profile/presentation/screen/section/ReadingAtlasSection.kt:165` | already calls `StatNumber` inside |
| `SmallStatTile` | `feature/profile/presentation/screen/section/ReadingAtlasSection.kt:212` | already calls `StatNumber` inside |

## S9-2 / S9-3 — Charts & legends (11 → `Chart` family + `Legend`)

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `MiniBar` | `core/component/control/PreviewTile.kt:107` | |
| `ReadingLifeRidgeline` | `core/component/share/ReadingLifeShareCardBody.kt:251` | private to the share-card body |
| `ReadingLifeGenreRanking` | `core/component/share/ReadingLifeShareCardBody.kt:354` | private to the share-card body |
| `ReadingLifeGenreRow` | `core/component/share/ReadingLifeShareCardBody.kt:381` | private to the share-card body |
| `GenreRankedBars` | `feature/profile/presentation/screen/section/GenreRankingSection.kt:111` | |
| `GenreRankedBar` | `feature/profile/presentation/screen/section/GenreRankingSection.kt:144` | |
| `GenreBarTrack` | `feature/profile/presentation/screen/section/GenreRankingSection.kt:186` | |
| `YearColumnChart` | `feature/profile/presentation/screen/section/YearColumnHistorySection.kt:154` | |
| `GenderProportionBar` | `feature/profile/presentation/screen/section/AuthorRepresentationSection.kt:209` | |
| `GenderLegend` | `feature/profile/presentation/screen/section/AuthorRepresentationSection.kt:236` | |
| `DemographicProportionBar` | `feature/profile/presentation/screen/section/AuthorRepresentationSection.kt:401` | |
| `DemographicLegend` | `feature/profile/presentation/screen/section/AuthorRepresentationSection.kt:456` | |
| `DemographicLegendRow` | `feature/profile/presentation/screen/section/AuthorRepresentationSection.kt:495` | |
| `RatingsHistogramChart` | `feature/profile/presentation/screen/section/RatingsHistogramSection.kt:202` | |
| `RatingsAverageRow` | `feature/profile/presentation/screen/section/RatingsHistogramSection.kt:136` | |

**Carry into S9-3:** once `RatingsHistogramSection`'s stat model leaves composition, move
`RATING_FRACTION_DIGITS` out of `screen/section/ProfileSectionShared.kt` (`ReadingAtlasMapper` imports it).

**Phase 1 questions:** decide the `Chart` family's split between S9-2 (the model shape plus one or two
representative charts — bar and ranked-bar look like the smallest full-contract pair) and S9-3 (the
rest), so S9-2 fixes the model before the remaining nine call sites are converted.

## S9-4 — Progress (6 → `ProgressIndicator` + `ProgressUiModel`)

`EditorialProgressIndicator` already moved to `:core:component` in S4-3, unchanged.

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `EditorialProgressIndicator` | `core/component/progress/EditorialProgressIndicator.kt:15` | |
| `LibraryWaveProgressRow` | `feature/library/presentation/screen/section/GridBookCell.kt:55` | |
| `ProgressBlock` | `feature/reading/presentation/screen/section/CompactBookEntry.kt:195` | |
| `FocusProgressBar` | `feature/session/presentation/screen/FocusModeShelf.kt:299` | |
| `WavyConnector` | `feature/onboarding/presentation/screen/OnboardingScreenLayout.mobile.kt:375` | |
| `WavySineLine` | `feature/onboarding/presentation/screen/OnboardingScreenLayout.mobile.kt:405` | |
| `FeaturedProgressStat` | `feature/reading/presentation/screen/section/FeaturedProgressStat.kt:35` | moved from S9-1; maps `Book` in composition |
