# PC — Performance checks

**Stage:** PC — app-wide, any session before S12. **Delegation:** user (PC-1), `softcover-implementer`
(PC-2 onward).

Nothing catches a screen that keeps drawing while idle, or a composable that can no longer skip. Reading,
Library and Explore drew about 60 frames a second at rest: a hidden pull-to-refresh indicator looped
forever, and the wavy `ProgressBar` moved its wave. Both were found by hand with `dumpsys gfxinfo`
and a Perfetto trace. PC turns that check, and a Compose stability check, into repeatable gates.

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| PC-1 | Approved shape (below) — stop for approval | [x] |
| PC-2 | Idle-frame check: dropped; no automated idle check (neither a debug route plus script nor Compose UI idle tests) | [x] |
| PC-3 | One-off `dumpsys gfxinfo` sweep: every screen idles at 0 frames except any `EmptyState` (~60 fps from `quoteGlyphSway()`), kept by choice since empty states are short-lived | [x] |
| PC-4 | Compose stability gate: skydoves analyzer 0.7.5, `androidMain` baselines in 15 modules, `stabilityCheck` in `check` and CI | [x] |
| PC-5 | Triage of the PC-4 baselines: approved shape (below) | [x] |
| PC-5a | Local type fixes: immutable collections in UiStates and models, `ImmutableList` params, concrete types for `Any` params, `@Immutable` on sealed / generic models; re-dump baselines | [ ] |
| PC-5b | Feature-local UI models for leaf composables that take a domain type, built when the state is set; re-dump baselines | [ ] |
| PC-5c | Per screen, sections take the slice of state they render instead of the whole UiState; re-dump baselines | [ ] |

## PC-1 — decisions

- **Idle-frame check:** how it reaches each screen (deep links, a debug-only route list, or `adb input`),
  the settle time, the frame threshold, and whether it runs in CI (needs an emulator) or locally only.
- **Stability gate:** the Compose compiler's `reportsDestination` / `metricsDestination` with a checked-in
  baseline, or the stability-analyzer Gradle plugin; whether it joins `styleCheck`; whether it belongs in
  the foundation (`docs/working/foundation-upstream-candidates.md`).
- **Later, out of PC:** macrobenchmark for scroll and startup jank, plus baseline profiles.

## Approved shape

- Stability: skydoves Compose Stability Analyzer plugin (`stabilityDump` baseline per module, `stabilityCheck` fails on a new unstable parameter); verify KMP / Kotlin 2.3.21 support first, else compiler reports plus a hand-written baseline diff task.
- Checks unstable parameters only, not skippability (strong skipping is on by default).
- Applied through `KmpComposeConventionPlugin` / `AndroidComposeConventionPlugin`; its own task wired into `check` and `ci.yml`, not `styleCheck`.
- Filed in `foundation-upstream-candidates.md` as a gate for build-logic; built here first.
- PC-5a, collections: every plain `List` / `Set` / `Map` field in a UiState or UI model becomes `ImmutableList` / `ImmutableSet` / `ImmutableMap` (Library, Library visibility, Roadmap, `UserTagEditorChipGroup`, `CalloutHeader.key`, Explore's `previousSearchQueries`, Reading's book lists); the contract's R3 extends to UiStates.
- PC-5a, params: `List` params fed from an `ImmutableList` take `ImmutableList` (book-card entry coordinator, reading-life charts, tag editor, roadmap, search focus, mark-as-read, empty Reading).
- PC-5a, `Any` params: tab-reselect effects take `Tab`, `rememberPulseScale` takes its pulse-key type, `ScrollToTopOnVisibleSetChange` takes `Int`.
- PC-5a, annotations: `@Immutable` on `ShareCardUiModel`, `RoadmapRenderUnit`, `RoadmapBlock`, `SettingsChoiceRowUiModel`.
- PC-5a, expected flip: Library and Library visibility entries move from RUNTIME to UNSTABLE once their collections expose `Book` / `Tag` / `BookList`; PC-5b / PC-5c resolve them, not a regression.
- PC-5b, scope: `FeaturedBackdropCard`, `FeaturedBackdropMeta`, `FeaturedBookCard`, `FeaturedSessionButton`, `TrendingTileSection` (`Book`), `FeaturedSection` (`Book?`), `BookOverflowMenuItems` (`Book`), `GeneralBookInfoSection` (`BookEdition`), the four `ActiveSession` composables, the profile breakdown blocks, `rememberMarkAsReadController`, `EmptyCurrentlyReadingScreen`; domain modules stay Compose-free, no annotation workaround.
- PC-5c, scope: every section composable taking a whole `*UiState` in Book Detail, Explore, Reading, Profile, Library and Settings.
- Kept baselined: services (`AppUpdateSimulator`, `NetworkAvailabilityProvider`, `ShareCardCapture`, Voyager `Navigator`), not data; Voyager `Tab` / `Screen` (`NavItemUi`, root and debug `Render` hosts), library interfaces; Compose / Coil API types (`ImageRequest`, `Painter`, `TextAutoSize`, `FiniteAnimationSpec`, `InlineTextContent` map, `ResizeMode`, generic `T`), not ours to annotate.
