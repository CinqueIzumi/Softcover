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
| S9-2 | `ChartTone`, `RankedBars`, `ProportionChart` | [x] |
| S9-3 | `ColumnChart`, the ratings stat model | [ ] |
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

S9-2 / S9-3 chart model (approved 2026-10-05):

- Package `core/component/chart/`, gallery family Statistics. Three components, one per anatomy (§ 7.6 test); no umbrella `Chart`, no separate `Legend`. All inert (no event).
- Out of the family: `MiniBar` (thumbnail text rules, stays in `control/`); `ReadingLifeRidgeline`, `ReadingLifeGenreRanking`, `ReadingLifeGenreRow` (share-card private, already model-fed; `ReadingLifeFooterStat` precedent); `RatingsAverageRow` (stat row, stays in the section — S9-3 moves its `StatNumberUiModel` onto `ProfileUiState`).
- `ChartTone` enum, shared by every chart: `Lead`/`Second`/`Third`/`Fourth`/`Fifth` = `primary` at 1/.82/.64/.48/.34, `Unknown` = `onSurfaceVariant` at .22; resolved in one `when`. The mapper sets a tone per mark; no raw alpha in feature code.
- Accepted tone snaps (visual, log in `visual-checks.md`): demographic "No" and ratings half-stars .55 → `Fourth` (.48); year zero column .24 → `Fifth` (.34).
- `RankedBars(model, modifier)` + `RankedBarsUiModel(rows: ImmutableList<RankedBarUiModel>, isLoading)`, `RankedBarUiModel(label, share: String, fraction: Float, tone)`. Replaces `GenreRankedBars` / `GenreRankedBar` / `GenreBarTrack`. Track stays the full 100%, fill floored at 1.5%.
- `ProportionChart(model, modifier)` — stacked bar plus its legend — + `ProportionChartUiModel(segments: ImmutableList<ProportionSegmentUiModel>, isLoading)`, `ProportionSegmentUiModel(label, share: String, fraction: Float, tone)`. Replaces `GenderProportionBar` / `GenderLegend` / `DemographicProportionBar` / `DemographicLegend` / `DemographicLegendRow`. A zero-fraction segment draws nothing in the bar but keeps its legend row; the mapper always adds Yes/No and adds Unknown only when its count > 0.
- `ColumnChart(model, modifier)` + `ColumnChartUiModel(columns: ImmutableList<ColumnUiModel>, isLoading)`, `ColumnUiModel(fraction: Float, tone, label: String?, value: String?)`. Replaces `YearColumnChart` and `RatingsHistogramChart`. Harmonised metrics (accepted visual shift on ratings): 96 dp plot, 12 dp minimum bar, 3 dp corners, 20 dp bar capped to the slot, zero column = 3 dp `Fifth` stub; the 16 dp value-label row renders only when some column has a `value`. The half-star narrow width is dropped (half-stars read by tone and the missing label) — user confirms in the gallery.
- Loading (R12): `RankedBars` renders 3 loading rows (label stand-in "Genre name", share "00%", masked track); `ProportionChart` a masked full-width bar + 3 legend rows (stand-in text, masked swatches); `ColumnChart` its real columns masked. Loading counts are written in the mapper's loading model. Replaces the `.shimmer` blocks — visual change, log it.
- Mappers feature-local in profile (one feature); all percentages, peak and metric selection move out of composition. New `ProfileUiState` fields `genreRanking`, `genderChart`, `bipocChart`, `lgbtqChart` (S9-2), the year and ratings `ColumnChartUiModel`s (S9-3); each rebuilt wherever `readingLife`, the hide-untagged switch or the year-metric toggle changes — one writer per field.
- `/dataviz`: text in text tokens, legend on the stack, 2 dp segment gaps, bars ≤ 24 dp — already met; no hover/tap layer (none today, mobile). User checks chart colours in both themes in the gallery.
- Split: S9-2 = `ChartTone`, `RankedBars`, `ProportionChart` (genres, gender, BIPOC, LGBTQ+). S9-3 = `ColumnChart` (years, ratings), `RatingsAverageRow`'s stat model, the `RATING_FRACTION_DIGITS` move.

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
