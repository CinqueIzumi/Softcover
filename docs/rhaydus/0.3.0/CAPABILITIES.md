# Foundation capabilities

The single index of **what the `nl.rhaydus` foundation makes available** to a consuming project: the
published modules, the components/APIs each exposes, the dependency graph, the tooling, and which
canonical doc governs what. The `rhaydus-*` agents read this **first**, every time, so they reach for
what already exists instead of reinventing it, and so their knowledge tracks the version a project is on
rather than a memorized snapshot.

> **Reuse-first rule.** Before hand-rolling a component, modifier, util, theme seam, or build wiring,
> check this index for an existing foundation equivalent. Reinventing something listed here is a defect,
> not a style nit.

> **Maintenance rule.** Adding, removing, or renaming a published module / component / public API updates
> this file in the same change (the `code-reviewer` enforces it, alongside the per-doc maintenance rules).

## How a project consumes it

- **Default:** depend on published coordinates from `mavenCentral()` (group `nl.rhaydus`), pinned to a
  version. No credentials.
- **Inner loop:** set `foundation.local=true` in `local.properties`; `settings.gradle.kts` then
  `includeBuild("../rhaydus-foundation")` and Gradle substitutes the published coordinates for local
  source - no version bumps, instant cross-repo edits.
- **Catalog:** the shared version catalog publishes as `nl.rhaydus:catalog`; consume it with
  `from("nl.rhaydus:catalog:<v>")`. App-only libraries stay in the app's own catalog.

## Published modules

| Coordinate | Purpose | Key public surface | Governing doc |
|---|---|---|---|
| `nl.rhaydus:toad` | TOAD presentation runtime (KMP, `commonMain`) | `ToadScreenModel`, `UiState`, `UiAction`, `UiEvent`, `Collector`, `ActionDependencies`, `ActionScope`, `LocalVariables` | `toad-architecture.md` |
| `nl.rhaydus:core-common` | Non-visual shared primitives | `AppDispatchers`, `AppLog`, `runCatchingCancellable`, `runCatchingLogged`, `TimeFormat`, `CurrentDate`, `HoursMinutesSeconds`, `NumberFormat` | `architecture.md` |
| `nl.rhaydus:core-platform` | Platform-capability seams (depends on core-common) | `SecureStorage` (Android/iOS/JVM), `NetworkAvailabilityProvider` + `NetworkAvailability` (Android/iOS/JVM) | `architecture.md` |
| `nl.rhaydus:offline-sync` | Offline optimistic-write queue skeleton (depends on core-platform) | `WriteQueue` / `PendingWriteStore` (app-implemented persistence seam), `PendingWrite`, `OfflineWriteDrainer` + `DefaultOfflineWriteDrainer`, `ReplayOutcome`, `DrainPolicy` | `architecture.md` |
| `nl.rhaydus:designsystem-core` | Design-agnostic Compose skeleton (no brand tokens) | see **designsystem-core surface** below | `design-system-foundations.md`, `code-style.md` |
| `nl.rhaydus:designsystem-editorial` | Opt-in editorial design language (depends on core) | `EditorialTypography`, `buildEditorialTypography`, `EditorialTheme`, `MaterialTheme.editorialTypography`; components `EditorialSectionHeader`, `HeroStatNumberField` + `EditorialSuffix`, `PullToRefreshEyebrow`, `DropCapText`, `EditorialSearchField` | `design-system-foundations.md` §2 |
| `nl.rhaydus:designsystem-image` | Opt-in async images on Coil (depends on core) | `RhaydusImage` (plain), `RhaydusPlaceholderImage` (placeholder slot), `RhaydusShimmerImage` (shimmer) | `design-system-foundations.md` §8 |
| `nl.rhaydus:catalog` | Shared version catalog | `libs.*` aliases for the shared third-party stack | — |
| `nl.rhaydus:ktlint-rules` | Custom ktlint ruleset (14 rules) | `ktlintCheck` / `ktlintFormat` gates | `code-style.md` |
| `nl.rhaydus:detekt-rules` | Custom detekt ruleset (type-resolved crash-safety) + shared baseline config | `rhaydus` ruleset (`UnguardedFlowTerminalRead`), bundled `config/detekt.yml`, `detektCheck` gate | `code-style.md` |

### designsystem-core surface

- **theme/** — `RhaydusTheme(colorScheme, typography, motionScheme)` (the Material 3 Expressive scaffold; the app supplies tokens), `StandardPreview`.
- **layout/** — `rememberWindowSizeClass()` + `WindowSizeClass` / `WindowWidthClass` (the single breakpoint source: COMPACT < 600dp, MEDIUM 600-840dp, EXPANDED >= 840dp), `TwoPaneScaffold`, `ContentMaxWidth` + `Modifier.cappedContentWidth`, `BottomBarScaffold` (the overlay bottom-bar host: measures a caller-supplied bar slot and provides its footprint), `LocalBottomBarPadding` + `rememberBottomBarPadding()` (the paired live read of that footprint), `BottomNavigationSpacer`, `ExpandableFlowRow` (a `FlowRow` that collapses to N lines behind a "show more" affordance and reveals more per tap; the affordance is a replaceable slot), spacers.
- **nav/** — `NavPulse` (a keyed, instance-owned cross-tab pulse signal; the concrete key stays app-side) + `rememberPulseScale(pulse, key)` (the animated icon scale that plays on each pulse, gated by `playDecorativeMotion()`).
- **modifier/** — `pressScale`, `pressScaleClickable`, `pressScaleCombinedClickable`, `noRippleClickable`, `pointerHandCursor`, `hoverHighlight`, `platformModifierClick` (commonMain expect; desktop Ctrl/Cmd-toggle + Shift-range modifier-click selection, no-op on touch), `shimmer`, `grayscale`, `conditional`, `shakeOnError`; jvm-only `dismissOnEscape`.
- **component/** — `AdaptiveModalSheet` (+ `LocalModalSheetForm` / `LocalModalSheetDismiss`), `DesktopTooltip`, `DesktopContextMenu` (+ `DesktopContextMenuItem`), jvm-only `DesktopBackStrip`, jvm-only `DesktopVerticalScrollbar` (themed vertical scrollbar with `LazyGridState` / `LazyListState` / `ScrollState` overloads; thumb keyed to `onSurface`), `StaggeredEntry` (`rememberStaggeredEntryCoordinator` + `Modifier.staggeredEntry`), `LazyItemMutationAnimator` (`rememberLazyItemMutationAnimator` + `Modifier.mutationAnimated`), `StarRatingInput` (interactive half-star rating with tap + drag-scrub and haptics; the star glyph is a `RhaydusIconResource`, the fill/empty tints are params), `InlineErrorState` (in-content load/submit-failure message in the `error` role + a `RhaydusButton` retry; the render side of the TOAD error-slot convention, `toad-architecture.md`), the **button family** `RhaydusButton` / `RhaydusToggleButton` / `RhaydusIconToggleButton` / `RhaydusSplitButton`.
- **model/** — `ButtonSize`, `ButtonStyle`, `ToggleButtonStyle`, `IconToggleButtonStyle`, `IconToggleButtonShape`, `SplitButtonStyle`, `ModalSheetForm`, `RhaydusMenuItem`.
- **motion/** — `playDecorativeMotion()` (reduced-motion gate; every animation routes through it).
- **haptics/** — `Haptics`, `rememberHaptics()`, `LocalHaptics`.
- **util/** — `ObserveAsEvents`, `SnackBarManager`, `SkeletonCrossfade`, `htmlToAnnotatedString`, `ClipboardReader`.
- **icon/** — `RhaydusIconResource` (the brand-agnostic icon wrapper; the app supplies its own icon catalog).
- **share/** — `ShareCardCapture` (`rememberShareCardCapture(config)`) captures a composable to a PNG and saves/shares it (Android MediaStore + share `Intent`, desktop file dialog + clipboard, iOS share sheet); `CapturableShareCard` is the capture host taking the app's card as a `@Composable` slot; `ShareCardCaptureConfig` supplies the file-name prefix / gallery album / Android `FileProvider` authority; `SaveOutcome` / `ShareOutcome` are the result types; `GalleryWritePermissionRequester` gates the Android API ≤ 28 write permission. Self-contained (no Koin/`AppDispatchers`); the Android target pulls `androidx.core` + `activity-compose`.

## Dependency graph

```
designsystem-editorial ──► designsystem-core
designsystem-image     ──► designsystem-core   (+ Coil: coil-compose; the network fetcher is an app choice)
offline-sync           ──► core-platform ──► core-common
core-platform          ──► core-common
core-common, toad      (standalone)
```

Adding a foundation dependency: declare the coordinate (or its catalog alias) in the module's
`build.gradle.kts`. With `foundation.local=true` it substitutes to local source automatically. The
editorial and image modules are **opt-in**: a project that wants neither the editorial look nor remote
images depends only on `designsystem-core`.

## Tooling

- **Convention plugins** (`build-logic`, applied by id): `rhaydus.android.library`, `rhaydus.kmp.library`, `rhaydus.android.compose`, `rhaydus.kmp.compose`. The KMP library convention also declares the `mobileMain` / `mobileTest` (Android+iOS shared, desktop branches to `jvmMain`) seam and the shared Android lint policy (`warningsAsErrors` + the root `lint.xml`, with version-freshness checks held `informational`).
- **Build gates** (`build-logic` + root): `rhaydus.module-graph` (a root-applied convention plugin — the `checkModuleGraph` task fails on any `project(...)` edge breaking the tier DAG configured via `moduleGraph { }`, plus an `api`-visibility allowlist); the `com.autonomousapps.dependency-analysis` policy (fail on unused deps / wrong api-vs-implementation, with the convention bundle excluded; each module's `projectHealth` runs in its `check`, aggregated by `buildHealth`). Both gates wire into every module's `check`.
- **ktlint-rules** (`nl.rhaydus:ktlint-rules`): auto-fixes + gates the mechanizable layout rules (multi-arg one-per-line wrapping, trailing commas, blank-line rules, region flushing, sibling-composable spacing, boolean `.not()`) and bans raw logging (`no-raw-logging`: no `println` / `android.util.Log.*` — use the `AppLog` facade). `ktlintFormat` fixes, `ktlintCheck` gates.
- **Claude plugin** (`rhaydus-kotlin`): the `rhaydus-adopt` / `rhaydus-logic` / `rhaydus-ui` agents, the `code-reviewer` / `unit-test-writer` agents, the `style-check` skill, and the docs-first hook.

## Which doc governs what

- `architecture.md` — Clean Architecture layering, the `core`/`feature`/orchestration tiers, navigation, dispatchers, build setup.
- `toad-architecture.md` — the TOAD pattern: the five type parameters, data flow, the add-a-feature checklist.
- `code-style.md` — naming, one-declaration-per-file, layout, comments, Compose formatting, visibility, test structure. Mechanizable rules are enforced by `ktlint-rules`.
- `design-system-foundations.md` — the brand-agnostic design skeleton: theme/typography plumbing, color roles, layout primitives (incl. window size, two-pane, adaptive modal), motion, desktop affordances, the editorial role contract (§2), the shared component catalog (§8). **Brand tokens, the editorial tone, and concrete palettes live in each app's own design-system doc, not here.**
