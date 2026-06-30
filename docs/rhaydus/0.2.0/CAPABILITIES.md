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
| `nl.rhaydus:core-ui` | Non-visual UI seams | `AppDispatchers`, `TimeFormat`, `CurrentDate`, `HoursMinutesSeconds`, `NumberFormat` | `architecture.md` |
| `nl.rhaydus:designsystem-core` | Design-agnostic Compose skeleton (no brand tokens) | see **designsystem-core surface** below | `design-system-foundations.md`, `code-style.md` |
| `nl.rhaydus:designsystem-editorial` | Opt-in editorial design language (depends on core) | `EditorialTypography`, `buildEditorialTypography`, `EditorialTheme`, `MaterialTheme.editorialTypography`; components `EditorialSectionHeader`, `HeroStatNumberField` + `EditorialSuffix`, `PullToRefreshEyebrow`, `DropCapText`, `EditorialSearchField` | `design-system-foundations.md` §2 |
| `nl.rhaydus:designsystem-image` | Opt-in async images on Coil (depends on core) | `RhaydusImage` (plain), `RhaydusPlaceholderImage` (placeholder slot), `RhaydusShimmerImage` (shimmer) | `design-system-foundations.md` §8 |
| `nl.rhaydus:catalog` | Shared version catalog | `libs.*` aliases for the shared third-party stack | — |
| `nl.rhaydus:ktlint-rules` | Custom ktlint ruleset (10 rules) | `ktlintCheck` / `ktlintFormat` gates | `code-style.md` |

### designsystem-core surface

- **theme/** — `RhaydusTheme(colorScheme, typography, motionScheme)` (the Material 3 Expressive scaffold; the app supplies tokens), `StandardPreview`.
- **layout/** — `rememberWindowSizeClass()` + `WindowSizeClass` / `WindowWidthClass` (the single breakpoint source: COMPACT < 600dp, MEDIUM 600-840dp, EXPANDED >= 840dp), `TwoPaneScaffold`, `ContentMaxWidth` + `Modifier.cappedContentWidth`, `LocalBottomBarPadding` + `rememberBottomBarPadding()`, `BottomNavigationSpacer`, spacers.
- **modifier/** — `pressScale`, `pressScaleClickable`, `pressScaleCombinedClickable`, `noRippleClickable`, `pointerHandCursor`, `hoverHighlight`, `shimmer`, `grayscale`, `conditional`, `ShakeOnError`; jvm-only `dismissOnEscape`.
- **component/** — `AdaptiveModalSheet` (+ `LocalModalSheetForm` / `LocalModalSheetDismiss`), `DesktopTooltip`, `DesktopContextMenu` (+ `DesktopContextMenuItem`), jvm-only `DesktopBackStrip`, `StaggeredEntry` (`rememberStaggeredEntryCoordinator` + `Modifier.staggeredEntry`), `LazyItemMutationAnimator` (`rememberLazyItemMutationAnimator` + `Modifier.mutationAnimated`), the **button family** `RhaydusButton` / `RhaydusToggleButton` / `RhaydusIconToggleButton` / `RhaydusSplitButton`.
- **model/** — `ButtonSize`, `ButtonStyle`, `ToggleButtonStyle`, `IconToggleButtonStyle`, `IconToggleButtonShape`, `SplitButtonStyle`, `ModalSheetForm`, `RhaydusMenuItem`.
- **motion/** — `playDecorativeMotion()` (reduced-motion gate; every animation routes through it).
- **haptics/** — `Haptics`, `rememberHaptics()`, `LocalHaptics`.
- **util/** — `ObserveAsEvents`, `SnackBarManager`, `SkeletonCrossfade`, `HtmlToAnnotatedString`, `ClipboardReader`.
- **icon/** — `RhaydusIconResource` (the brand-agnostic icon wrapper; the app supplies its own icon catalog).

## Dependency graph

```
designsystem-editorial ──► designsystem-core
designsystem-image     ──► designsystem-core   (+ Coil: coil-compose; the network fetcher is an app choice)
core-ui, toad          (standalone)
```

Adding a foundation dependency: declare the coordinate (or its catalog alias) in the module's
`build.gradle.kts`. With `foundation.local=true` it substitutes to local source automatically. The
editorial and image modules are **opt-in**: a project that wants neither the editorial look nor remote
images depends only on `designsystem-core`.

## Tooling

- **Convention plugins** (`build-logic`, applied by id): `rhaydus.android.library`, `rhaydus.kmp.library`, `rhaydus.android.compose`, `rhaydus.kmp.compose`. The KMP library convention also declares the `mobileMain` / `mobileTest` (Android+iOS shared, desktop branches to `jvmMain`) seam.
- **ktlint-rules** (`nl.rhaydus:ktlint-rules`): auto-fixes + gates the mechanizable layout rules (multi-arg one-per-line wrapping, trailing commas, blank-line rules, region flushing, sibling-composable spacing, boolean `.not()`). `ktlintFormat` fixes, `ktlintCheck` gates.
- **Claude plugin** (`rhaydus-kotlin`): the `rhaydus-adopt` / `rhaydus-logic` / `rhaydus-ui` agents, the `code-reviewer` / `unit-test-writer` agents, the `style-check` skill, and the docs-first hook.

## Which doc governs what

- `architecture.md` — Clean Architecture layering, the `core`/`feature`/orchestration tiers, navigation, dispatchers, build setup.
- `toad-architecture.md` — the TOAD pattern: the five type parameters, data flow, the add-a-feature checklist.
- `code-style.md` — naming, one-declaration-per-file, layout, comments, Compose formatting, visibility, test structure. Mechanizable rules are enforced by `ktlint-rules`.
- `design-system-foundations.md` — the brand-agnostic design skeleton: theme/typography plumbing, color roles, layout primitives (incl. window size, two-pane, adaptive modal), motion, desktop affordances, the editorial role contract (§2), the shared component catalog (§8). **Brand tokens, the editorial tone, and concrete palettes live in each app's own design-system doc, not here.**
