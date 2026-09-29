# S5 — Dividers & rules

**Stage:** S5 — Primitives. **Delegation:** user (Phase 1), `softcover-implementer` (Phase 2).

5 declarations collapse onto `Divider` + `DividerUiModel`; every raw Material divider follows.

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S5-9 | All five divider declarations | [x] |
| S5-C-dividers | Convergence: model split, one scaffold + style table, the five existing callers | [x] |
| S5-10 | Absorb the 34 raw `HorizontalDivider(` / `VerticalDivider(` call sites | [x] |

## Approved shape

- `DividerUiModel` is sealed: `Horizontal(surface: HorizontalDividerSurface, center: DividerCenter? = null)`
  and `Vertical(surface: VerticalDividerSurface)`. A vertical rule cannot carry a centre.
- `DividerCenter` is sealed: `QuoteMark` (decorative, cleared from semantics) and `Label(text)`.
- Surfaces are surface-named data objects, one entry per surface (R2); a surface decides style only, so any
  horizontal surface renders any centre. Existing: `DebugRoutesRow`, `ReadingLifeShareCard`,
  `SettingsRoadmap`, `CreateListQuote`, `OnboardingOrTypeIt`.
- One private scaffold (rule, or rule · centre · rule) replaces the five bodies. A per-surface style table
  gives ink, inset and centre gap; ink is outline or content tint at an alpha, resolved from a `ColorScheme` +
  content colour so it is unit-testable. Thickness 1dp everywhere.
- Accepted shifts: centre gap 12dp for both centres (glyph was 14dp); the label uses plain `eyebrowSmall`
  (drops the 10sp / 2sp-tracking overrides).
- **S5-10 rework (supersedes the surface bullets above; approved 2026-09-29).** Surfaces, both style tables
  and `VerticalDividerStyle` go. The model keeps only defaulted, overridable fields:
  `Horizontal(ink: DividerInk = Outline, inset: Dp = 0.dp, center: DividerCenter? = null)` and
  `Vertical(ink: DividerInk = Outline, length: Dp? = null)` (null fills the height). `DividerInk` stays sealed
  (`Outline`, `ContentTint(alpha)`, `OnImage(alpha)`), resolved in the component; centre gap 12dp, 1dp thick.
- Contract: new standing § 7.4a exception to R10 — a data-free UI model (no field derived from data) may be
  built at the call site; `DividerUiModel` is the example. Heading becomes "Known exceptions".
- So every call site builds its model inline (`Divider(DividerUiModel.Horizontal())`); remove every
  passed-down divider field on the UiStates, `SessionPeekBarUiState` and `sessionPeekBarState`. The version
  footer's label is data: `SettingsScreenUiState` keeps `versionFooterDivider`, default derived from
  `appVersionName` / `appVersionCode`.
- Overrides: `BookDetailHero` → `OnImage(0.3f)`; the two tinted cards → `ContentTint(0.25f)`; create-list
  privacy `inset = 40.dp`; debug routes `inset = 20.dp`; ratings average `length = 32.dp`.
- Revert `config/detekt/detekt.yml` (`ignoreSimpleWhenEntries`). Tests shrink to ink resolution and the
  scaffold-facing model defaults; drop the per-surface style tests and the UiState divider-default tests.
- `TagEditorCollection:85` is header anatomy and goes to the headers family, not `Divider`.
- Accepted shift: the version label takes `eyebrowSmall` and the 12dp centre gap.

## S5-10 inventory

The Surface column names the site's role; after the rework it maps to the overrides above.

| Site (symbol) | Surface |
|---|---|
| `ChooseListsBottomSheet` `ChooseListsRow` | `ChooseListsRow` |
| `VerdictBlock` `VerdictBlock` | `VerdictBlock` |
| `VerdictSheet` `VerdictSheet` (footer) | `VerdictSheetFooter` |
| `GeneralBookInfoSection` ×2 (skeleton, loaded) | `BookDetailHero` |
| `HiddenSuggestionsShelf` `HiddenBookRow`, `HiddenSeriesRow` | `HiddenSuggestionsRow` |
| `BecauseYouReadGenreControl` `BecauseYouReadGenreSheetRow` | `BecauseYouReadGenreSheet` |
| `SearchFocusContent` `SearchFocusRecentRow` | `SearchRecentQuery` |
| `LibraryFilterSheet` `FilterSheetFooter` | `LibraryFilterFooter` |
| `LibraryShelvesSheet` `ShelvesSheetRow` | `LibraryShelvesSheet` |
| `ShelfNeighbourRail` ×2 (top, bottom) | `ShelfNeighbourRail` |
| `CompactRow`, `LargeRow` | `LibraryListRow` |
| `CreateListSheetContent` | `CreateListPrivacy` |
| `OnboardingShelf` `WhereToFindItExplainer` | `OnboardingWhereToFindIt` |
| `OnboardingScreenLayout.jvm` | `OnboardingDesktopIntro` |
| `AuthorRepresentationSection` `GenderLegend`, `DemographicLegendRow` | `ProfileLegendRow` |
| `ReadingAtlasSection` `HeroStatCard` | `ReadingAtlasHeroStat` |
| `YearColumnHistorySection` | `YearColumnHistoryBaseline` |
| `SessionPeekBar` `SessionPeekBarContent` | `SessionPeekBar` |
| `DateStyleSection`, `DisplaySection`, `SpineColourSection`, `UiScaleSection` | `SettingsRows` |
| `VersionFooter` ×2 | `SettingsVersionFooter` + `Label` |
| `RatingsHistogramSection` `RatingsAverageRow` (vertical) | `RatingsAverage` |
| `BookDetail` / `Library` / `Profile` / `SettingsScreenLayout.jvm` (vertical) | `WidePaneSeparator` |
