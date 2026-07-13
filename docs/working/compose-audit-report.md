# Jetpack Compose Audit Report

Target: Softcover (repository root)
Date: 2026-06-17
Scope: Compose Multiplatform UI in `commonMain` of `:feature:*` (lists, profile, onboarding, explore, library, book_detail, reading, session, scan, settings, app_update) and `:core:designsystem`; platform leaves in `androidMain` / `jvmMain` / `iosMain` / `mobileMain`
Excluded from scoring: external `nl.rhaydus.*` foundation components (library code, not this repo); test sources; previews
Confidence: High (qualitative); compiler diagnostics are partial — measured for `:feature:explore` only, inferred-but-corroborated elsewhere
Overall Score: 83/100

## Scorecard

| Category | Score | Weight | Status | Notes |
|----------|-------|--------|--------|-------|
| Performance | 8/10 | 35% | solid | SSM on; named-only skippable% = 100%. Capped at 8 by SSM-on table: every feature `UiState` wraps raw `List`/`Set` (no immutable collections), so each is an unstable param carrying a deep-`equals()` cost per recomposition. Everything else (lazy keys, animation phases, typed state) is clean. |
| State management | 9/10 | 25% | excellent | Single immutable `StateFlow<UiState>` per screen via TOAD; clean hoisting, encapsulation, flow shaping in collectors. One minor floating state holder. |
| Side effects | 9/10 | 20% | excellent | Effect keys, `rememberUpdatedState`, `DisposableEffect` cleanup, event-only `rememberCoroutineScope`, `snapshotFlow`-in-effect all correct. No IO or navigation in composition. |
| Composable API quality | 7/10 | 20% | solid | `modifier` contract held by ~15/15 shared components; good slot/variant design. Dinged by one param-order bug, 4 hardcoded UI strings, and thin `@Preview` coverage (2/19 component files). |

## Critical Findings

1. **Performance: every feature `UiState` is compiler-unstable because it wraps raw `List`/`Set`/`Map`**
   - Why it matters: Under Strong Skipping (Kotlin 2.3.21), Compose still skips these composables, but it decides skipping by running `equals()` on each unstable param on every recomposition. A `data class` wrapping raw `List<Book>` does a deep element-wise `equals()` each time — cost scales with list size (e.g. trending/queried book lists). `kotlinx.collections.immutable` is not a project dependency, so the cheap-equality / structural-sharing path is unavailable. This is systemic, not isolated.
   - Evidence (measured): `feature/explore/build/compose_audit/explore-classes.txt` flags `ExploreScreenUiState` `<runtime stability> = Unstable` with `unstable val queriedBooks: List<Book>`, `trendingBooks: List<Book>`, `continueSeriesBooks: List<Book>`, `previousSearchQueries: List<String>`. Same pattern in source: `feature/library/.../LibraryUiState.kt` (15 collection fields), `feature/book_detail/.../BookDetailUiState.kt` (10), `feature/reading/.../ReadingScreenUiState.kt` (7), `feature/explore/.../ExploreScreenUiState.kt:6`.
   - Fix direction: add `kotlinx.collections.immutable` and type these fields as `ImmutableList<T>` / `ImmutableSet<T>` (`persistentListOf` / `toImmutableList()` at the mapping boundary), or annotate the holders `@Immutable`. This makes the UiState stable so the per-recomposition `equals()` becomes a cheap reference check.
   - References: <https://developer.android.com/develop/ui/compose/performance/stability>, <https://developer.android.com/develop/ui/compose/performance/stability/fix>, <https://developer.android.com/develop/ui/compose/performance/stability/strongskipping>

2. **Composable API quality: hardcoded UI strings in reusable design-system components**
   - Why it matters: Shared components in `:core:designsystem` embed literal English copy, which breaks i18n and makes the components brittle to reuse/test.
   - Evidence: `core/designsystem/.../component/ExpandableFlowRow.kt:77` (`Text("Show more")`); `core/designsystem/.../component/ChooseListsBottomSheet.kt:126` (`"No custom lists yet"`), `:271` (`"Create a new list"`); `core/designsystem/.../component/UpdateProgressBottomSheet.kt:190` (`"READING PROGRESS"`).
   - Fix direction: route through string resources or expose the text as parameters with defaults.
   - References: <https://developer.android.com/develop/ui/compose/resources>

3. **Composable API quality: required parameter placed after an optional one in a shared component**
   - Why it matters: Required-after-optional ordering forces awkward positional calls and violates the component API guideline ordering (required → `modifier` → optional → trailing lambda).
   - Evidence: `core/designsystem/.../component/SoftcoverTopBar.kt:40-41` — required `isLoading: Boolean` declared after optional `placeHolder: String = "Search"`.
   - Fix direction: move `isLoading` ahead of the optional params.
   - References: <https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-component-api-guidelines.md>

## Adjacent Findings

### Android Launch UX

- Android 12+ splash icon status: **not configured**
- Evidence: `core/designsystem/src/androidMain/res/values/themes.xml` defines only `Theme.Softcover` (`parent="Theme.Material3.Light.NoActionBar"`); no `windowSplashScreenAnimatedIcon` / `windowSplashScreenBackground` / `postSplashScreenTheme` items exist anywhere, and there is no `drawable-v31` directory. The app applies `Theme.Softcover` in `app/src/main/AndroidManifest.xml:30` with no splash-screen API usage.
- Finding: none. With no `windowSplashScreenAnimatedIcon` set, the blurry-static-icon failure mode does not apply.
- References: <https://developer.android.com/develop/ui/views/launch/splash-screen>, <https://developer.android.com/reference/androidx/core/splashscreen/SplashScreen>, <https://issuetracker.google.com/issues/520672537>

## Category Details

### Performance — 8/10

**Ceiling check**

- Strong Skipping: on (Kotlin 2.3.21 — default; `explore-module.json` confirms `"StrongSkipping": true`, `IntrinsicRemember`, `OptimizeNonSkippingGroups`, `PausableComposition` all on)
- Ceiling table applied: SSM-on
- Module-wide `skippable%`: 47/69 = 68.1% (`:feature:explore`)
- Named-only `skippable%`: 24/24 = 100% — this is the binding metric. The 68.1% module-wide figure is anchored entirely by zero-argument lambdas that structurally cannot skip; every *named* restartable composable in the module is skippable.
- Unstable shared types from compiler: 16 unstable classes in explore, but only `ExploreScreenUiState` (and the `Action` / `LocalVariables` holders) reach composables as params; the rest are data sources, repositories, use cases, and serializers that never enter composition.
- SSM-on binding evidence: **non-trivial `equals()` cost** on unstable `UiState` params (raw `List`/`Set` fields, systemic). No instance-recreation churn observed in source (no `listOf(...)` / `mapOf(...)` / object literals allocated in composable bodies and passed down). No broken `equals()` (all holders are immutable `data class`es). No unjustified `@NonSkippableComposable` / `@DontMemoize`.
- Qualitative score: 9
- Ceiling: cap at 8 (named-only ≥95% but a set of unstable params carry non-trivial `equals()` cost)
- Applied score: 8

**What is working**

- 100% of lazy lists carry stable `key =`: `feature/library/.../LibraryShelf.kt:247`, `:448`; `feature/reading/.../ReadingShelf.kt:204`; `feature/explore/.../ExploreShelf.kt:508`; `core/designsystem/.../ChooseListsBottomSheet.kt:70`; `feature/book_detail/.../EditionBottomSheetSelector.kt:173`.
- Collection transforms live in `UiState` methods / state holders, not composition; the few in-composable derivations are correctly memoized: `feature/library/.../LibraryShelf.kt:141`, `:350` (`remember(visibleEditions) { ... }`).
- Animation phase discipline: `Animatable` held in `remember`, driven from `LaunchedEffect`, and read through lambda modifiers (`drawBehind` / `graphicsLayer`): `core/designsystem/.../AnimatedStatNumber.kt:25,27-33,39`, `core/designsystem/.../MarkAsReadBurst.kt:37,40-48,62`.
- Typed primitive state factories used (no autoboxing): `feature/book_detail/.../BookDetailScreen.kt:74`, `feature/library/.../LibraryShelf.kt:195-196` (`mutableIntStateOf`).
- First-party APIs only (`androidx.compose.foundation.pager.HorizontalPager`); no Accompanist, no `animateItemPlacement()`, no `@NonSkippableComposable` / `@DontMemoize`.

**What is hurting the score**

- Systemic raw-collection `UiState` instability (Critical Finding 1) — the single performance lever in the codebase.
- Heterogeneous `ReadingShelf` list has no `contentType` (header / plan-today / featured card / label / book rows) — minor; Compose can only reuse compositions within a type.

**Evidence**

- `feature/explore/.../state/ExploreScreenUiState.kt:6` — `data class ... : UiState` with raw `List<String>` / `List<Book>` fields; compiler-confirmed `Unstable`. · References: <https://developer.android.com/develop/ui/compose/performance/stability>
- `feature/library/.../LibraryUiState.kt` (15 raw collection fields), `feature/book_detail/.../BookDetailUiState.kt` (10) — same pattern repo-wide; no `kotlinx.collections.immutable` in `gradle/libs.versions.toml`. · References: <https://developer.android.com/develop/ui/compose/performance/stability/fix>
- `feature/reading/.../ReadingShelf.kt:161-224` — heterogeneous lazy list, no `contentType`. · References: <https://developer.android.com/develop/ui/compose/lists>

### State Management — 9/10

**What is working**

- One immutable `StateFlow<UiState>` per screen via the TOAD `ToadScreenModel`; `_state` never leaked. Screens read with `screenModel.state.collectAsStateWithLifecycle()` at the entry point: `feature/book_detail/.../BookDetailScreen.kt:62-68`, `feature/library/.../LibraryScreen.kt:39-40`.
- All flow shaping (`combine`, `flatMapLatest`, `stateIn`) lives in `*Collector` classes, never in composables: `feature/library/.../collector/AllBooksCollector.kt`, `feature/book_detail/.../collector/BookDeadlineCollector.kt`.
- App-scoped state correctly uses `stateIn(..., SharingStarted.Eagerly, ...)`: `core/designsystem/.../session/ActiveSessionController.kt:41-57`.
- Reusable components are stateless and hold only ephemeral UI state (drag preview, interaction source): `core/designsystem/.../StarRatingInput.kt`, `.../ChooseListsBottomSheet.kt`. `remember` blocks that depend on inputs are correctly keyed (`MarkAsReadController.kt:104-105`, `StarRatingInput.kt:57`).

**What is hurting the score**

- Two scroll/list state holders are declared as top-level `private val` properties rather than created in a `remember { }`, so the instance is shared and outlives the composition: `feature/reading/.../ReadingScreenLayout.mobile.kt:56` (`private val booksListState = LazyListState()`), `feature/reading/.../ReadingScreenLayout.jvm.kt:43` (`private val readingScrollState = ScrollState(0)`). Documented as intentional for desktop scrollbar stability, but it is the one ownership smell; on mobile it risks stale scroll position if the screen is recreated.

**Evidence**

- `feature/reading/.../ReadingScreenLayout.mobile.kt:56` — list state held outside composition. · References: <https://developer.android.com/develop/ui/compose/state>
- `feature/book_detail/.../state/BookDetailUiState.kt:17-61` — immutable collections updated via `copy()` (positive). · References: <https://developer.android.com/develop/ui/compose/architecture>
- `core/designsystem/.../session/ActiveSessionController.kt:53-54` — `stateIn` with `Eagerly` for must-cache session state (positive). · References: <https://developer.android.com/develop/ui/compose/architecture>

### Side Effects — 9/10

**What is working**

- Effects are correctly keyed; the "run once" `LaunchedEffect(Unit)` instances do not capture mutable values unsafely: `orchestration/.../App.kt:74,113` (one-time flow collection), state-keyed effects at `core/designsystem/.../AnimatedStatNumber.kt:134` (`LaunchedEffect(integerKey)`), `feature/reading/.../MarkAsReadController.kt:119`.
- `rememberUpdatedState` used to avoid stale captures in long-lived effects: `feature/reading/.../MarkAsReadController.kt:107-108`, `feature/library/.../LibraryScreenLayout.mobile.kt:140-141`.
- `DisposableEffect` with full cleanup for camera/scanner/executor: `core/designsystem/.../BarcodeScanner.android.kt:84-139` (`onDispose` unbinds, closes scanner, shuts down executor).
- `rememberCoroutineScope()` used only for event-driven work (click/refresh/gesture): `core/designsystem/.../ShareBookBottomSheet.kt:75`, `feature/library/.../LibraryScreenLayout.mobile.kt:112`. `snapshotFlow` is always collected inside a `LaunchedEffect`: `feature/library/.../LibraryScreenLayout.mobile.kt:144-150`, `orchestration/.../NavDestinations.kt:121-133`.
- No IO in composition (Coil `AsyncImage` / `SubcomposeAsyncImage` is the accepted carve-out); navigation happens in event handlers, not composition bodies; `requestFocus()` is always wrapped in `LaunchedEffect`.

**What is hurting the score**

- Nothing systemic. The category is audit-clean; the score stays at 9 (not 10) only because there is no compensating evidence of effect-level tests to lock the behavior in.

**Evidence**

- `core/designsystem/.../BarcodeScanner.android.kt:84-139` — `DisposableEffect` with complete cleanup. · References: <https://developer.android.com/develop/ui/compose/side-effects>
- `feature/reading/.../MarkAsReadController.kt:107-108,119` — `rememberUpdatedState` + keyed `LaunchedEffect`. · References: <https://developer.android.com/develop/ui/compose/side-effects>
- `orchestration/.../NavDestinations.kt:121-133` — `snapshotFlow` collected inside `LaunchedEffect`. · References: <https://developer.android.com/develop/ui/compose/side-effects>

### Composable API Quality — 7/10

**What is working**

- The `modifier` contract is held across ~15/15 public shared components: `modifier: Modifier = Modifier` as the first optional param, applied to the root node (`PillChip.kt:31-37`, `UnreleasedBadge.kt:45-49`, and peers).
- Good slot APIs and receiver scopes: `DeadlineCoverOverlay.kt:18`, `ExpandableFlowRow.kt:49-52`, `SoftcoverTopBar.kt:154,169` (`@Composable RowScope.() -> Unit`).
- Variants modeled as distinct components / small enums rather than a single `style`-grab-bag; no `Modifier.composed { }`; domain models (`ReviewDocument`, `BookList`, `BookEdition`) passed only where semantically correct; PascalCase Unit-returning composables throughout.

**What is hurting the score**

- Hardcoded UI strings in reusable components (Critical Finding 2).
- Required-after-optional parameter order in `SoftcoverSearchTopBar` (Critical Finding 3).
- `@Preview` coverage on only 2 of 19 component files (`SoftcoverTopBar`, `UpdateProgressBottomSheet`) — reusable components should prove they render with no hidden ambient dependencies.
- Two animated components hard-code timing without exposing `animationSpec` (`AnimatedStatNumber.kt:52-55`, `MarkAsReadBurst.kt:48-54`) — low severity.

**Evidence**

- `core/designsystem/.../ExpandableFlowRow.kt:77`, `.../ChooseListsBottomSheet.kt:126,271`, `.../UpdateProgressBottomSheet.kt:190` — hardcoded strings. · References: <https://developer.android.com/develop/ui/compose/resources>
- `core/designsystem/.../SoftcoverTopBar.kt:40-41` — required `isLoading` after optional `placeHolder`. · References: <https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-component-api-guidelines.md>
- 17/19 component files in `core/designsystem/.../presentation/component/` lack `@Preview`. · References: <https://developer.android.com/develop/ui/compose/tooling/previews>

## Prioritized Fixes

1. **Make `UiState` holders stable.** Add `kotlinx.collections.immutable` to `gradle/libs.versions.toml` and convert raw `List`/`Set`/`Map` fields in every feature `UiState` to `ImmutableList`/`ImmutableSet` (convert with `toImmutableList()` at the mapper boundary), or annotate the holders `@Immutable`. Start with `feature/explore/.../ExploreScreenUiState.kt:6`, `feature/library/.../LibraryUiState.kt`, `feature/book_detail/.../BookDetailUiState.kt`. Removes the per-recomposition deep-`equals()` cost on every screen. Ref: <https://developer.android.com/develop/ui/compose/performance/stability/fix>
2. **Externalize hardcoded strings** in `ExpandableFlowRow.kt:77`, `ChooseListsBottomSheet.kt:126,271`, `UpdateProgressBottomSheet.kt:190` to string resources or component parameters. Ref: <https://developer.android.com/develop/ui/compose/resources>
3. **Fix the parameter order** in `SoftcoverTopBar.kt:40-41` (move required `isLoading` before optional `placeHolder`), and add `@Preview` coverage to the high-reuse components. Ref: <https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-component-api-guidelines.md>
4. **Optional follow-up:** add `contentType` to the heterogeneous `ReadingShelf` list (`feature/reading/.../ReadingShelf.kt:161-224`); expose `animationSpec` on `AnimatedStatNumber` / `MarkAsReadBurst`. Ref: <https://developer.android.com/develop/ui/compose/lists>

## Notes And Limits

- The four scored categories were audited across multiple representative modules (explore, library, book_detail, reading, settings, designsystem, orchestration). Confidence is High for State, Side Effects, and API Quality.
- Adjacent coverage notes: **no Compose UI tests** anywhere (`createComposeRule` / `onNodeWith*` / `runComposeUiTest` return zero hits) — a real gap, not scored. Focus/keyboard APIs are present (`focusRequester` in the barcode scanner and search bars; a desktop JVM target exists). The repo is heavily **Compose Multiplatform** (36 `expect`/`actual` files; `androidMain`/`iosMain`/`jvmMain`/`mobileMain` source sets; `AndroidView` interop in `BarcodeScanner.android.kt`; iOS `MainViewController` and JVM `DesktopApp` entry points).
- Android Launch UX resources: not configured (no `windowSplashScreenAnimatedIcon`); no blurry-icon risk.
- Strong Skipping mode: on (Kotlin 2.3.21 default; confirmed in `explore-module.json` feature flags). No module-level opt-outs found.
- `collectAsStateWithLifecycle` in `commonMain` is treated as a platform tradeoff, not a deduction (the lifecycle variant is Android-specific; CMP supplies the multiplatform shim).
- Weight choice: default 35/25/20/20.
- Renormalization: none — all four categories had sufficient surface area.
- Compiler diagnostics used: **yes, but partial**. `assembleRelease` with the bundled `--init-script` succeeded (exit 0); incremental caching meant only `:feature:explore` re-emitted reports (`feature/explore/build/compose_audit/`). Strong Skipping state and the named-only 100% skippable result are measured for explore and confirm the SSM track; the unstable-`UiState` pattern is then corroborated in source across every other feature module (uniform TOAD `UiState` shape), so the stability finding is treated as measured-and-generalized rather than purely inferred. A full clean build would be needed to produce per-module reports for the remaining modules.

## Suggested Follow-Up

- `material-3` skill — ✅ **run**; full MD3 compliance audit in the dedicated section below (overall **84/100**). It scores design/theming, which the four numeric Compose categories deliberately leave out.
- **`compose-agent focus on testing`** — ✅ **run**; results in the Follow-Up Review Findings section below.
- **`compose-agent focus on kmp`** — ✅ **run**; results in the Follow-Up Review Findings section below.
- **`compose-agent focus on focus`** — ✅ **run**; results in the Follow-Up Review Findings section below.

## Follow-Up Review Findings

These are the results of the three `compose-agent` focused reviews recommended above. They are **adjacent coverage** — they do not change the 83/100 score (which covers only the four numeric categories), but they record real risks and confirmations the scored audit deliberately left out of scope.

### Testing (`compose-agent focus on testing`) — largest unaddressed gap

- **Zero Compose UI tests** and **zero screenshot tests** in the repo (`createComposeRule` / `runComposeUiTest` / `onNodeWith*` / Paparazzi / Roborazzi all return no hits).
- The infrastructure is *available but unwired*: `androidx-compose-ui-test-junit4` and `ui-test-manifest` exist in `gradle/libs.versions.toml` (BOM `2026.01.01`) but are not added to any feature module's build.
- **State-holder unit tests cover only 2 of 10 ScreenModels** (`SettingsScreenScreenModelTest`, `CreateListScreenModelTest`). Untested: Library, Profile, Reading, Onboarding, BookDetail, Explore, Scan, LibraryVisibilitySettings. Collector/flow logic is well-covered (~19 test files).
- `@Preview` coverage: 46 preview annotations across 13 files (screen layouts are well-covered on `mobileMain`), but only **2 of ~22 `:core:designsystem` components** carry a preview.
- **Cheap, high-value opportunity:** all 26 screens are stateless `XScreenLayout(state, runAction)` composables — ideal for state-driven Compose UI tests (fixed state + captured callbacks, no DI graph). Highest-value untested layouts: `feature/settings/.../SettingsScreenLayout.mobile.kt`, `feature/profile/.../ProfileScreenLayout.mobile.kt`, `feature/reading/.../ReadingScreenLayout.mobile.kt`, `feature/explore/.../ExploreScreenLayout.mobile.kt`, `feature/scan/.../BarcodeScannerScreenLayout.mobile.kt`.
- References: <https://developer.android.com/develop/ui/compose/testing>, <https://developer.android.com/develop/ui/compose/tooling/previews>

### KMP / Compose Multiplatform boundaries (`compose-agent focus on kmp`) — strong, no action needed

- **No platform types leak into `commonMain`** signatures — `android.*` / `platform.UIKit.*` / `java.io.*` are confined to `androidMain` / `iosMain` / `jvmMain`.
- `expect`/`actual` declarations are **semantic and capability-split** (`expect fun isCameraAvailable()`, `expect val platformSessionModule` / `platformDatabaseModule` / `platformBookModule`) rather than one giant `Platform` object; actuals are thin Koin/translation wiring with no domain rules.
- The `expect @Composable BarcodeScanner(...)` correctly takes and forwards `modifier: Modifier = Modifier` (`core/designsystem/.../BarcodeScanner.kt`).
- Native interop is at leaf nodes with full lifecycle cleanup: `AndroidView` camera binding in `BarcodeScanner.android.kt:84-139` wrapped in `DisposableEffect` (unbinds provider, closes scanner, shuts down executor); `ComposeUIViewController { App() }` is the iOS entry point, not mid-tree; no `UIKitView` misuse.
- **Note (not a defect):** `collectAsStateWithLifecycle` is used in `commonMain` across ~11 modules via `androidx.lifecycle:lifecycle-runtime-compose`. This is the JetBrains-published **multiplatform** lifecycle artifact, so it resolves and runs on iOS/JVM (the full release build passes on all targets) — it is *not* an Android-only leak.
- References: <https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform.html>, <https://developer.android.com/develop/ui/compose/interop>

### Focus / keyboard (`compose-agent focus on focus`) — correct and idiomatic

- All **3 `requestFocus()` call sites are inside a `LaunchedEffect`**, never the composition body: `feature/lists/.../CreateListShelf.kt:52-54`, `feature/scan/.../BarcodeScannerShelf.kt:69-71`, `feature/book_detail/.../ReviewEditorBottomSheet.kt:86-88`.
- No `FocusRequester` stored by lazy-list index; no `onPreviewKeyEvent` / `onKeyEvent` handlers that over-consume keys (none exist); no `focusProperties` over-wiring; IME submission routes cleanly through `KeyboardActions` + `LocalFocusManager.clearFocus()`.
- **Desktop-aware:** `orchestration/.../App.kt` uses `detectTapGestures` (not `clickable`) for the tap-to-dismiss scrim, avoiding stealing the space key from focused text fields.
- Minor: `ReviewEditorBottomSheet` keys its focus effect to `Unit` and relies on the parent's `showReviewSheet` visibility for safety — works today, slightly fragile if the parent's conditional changes. Consider keying to the visible/loaded condition.
- Gap: **no focus/keyboard tests** (`performKeyInput` / `pressKey` / `assertIsFocused` absent) — overlaps with the testing gap above.
- References: <https://developer.android.com/develop/ui/compose/touch-input/focus>, <https://developer.android.com/develop/ui/compose/touch-input/keyboard-input>

## MD3 Compliance Audit (`material-3` skill)

Target: `commonMain` of `:feature:*`, `:core:designsystem`, and `:orchestration` (Compose Multiplatform UI)
Date: 2026-06-17
Overall Score: **84/100**

Softcover layers a deliberate **editorial brand** on top of Material 3 (Expressive) via the foundation `RhaydusTheme` + `EditorialTheme`. The MD3 substrate is followed faithfully — generated color scheme, full role set, dynamic-color seam, tonal (not shadowed) elevation, window-size-class adaptivity. The single systemic deviation is **shape**: corner radii are scattered as `RoundedCornerShape(N.dp)` magic numbers rather than centralized as `MaterialTheme.shapes` tokens or a named brand shape scale. This is adjacent coverage and does **not** change the 83/100 four-category Compose score above.

### Scores by Category

| Category | Score | Status | Notes |
|----------|-------|--------|-------|
| Color tokens | 9/10 | pass | Full `lightColorScheme`/`darkColorScheme` from Material Theme Builder; correct `onX`-on-`X` tonal pairing; brand non-theme accents (`RatingGold`, spoiler covers) are deliberate and documented. |
| Typography | 9/10 | pass | M3 `Typography()` with brand families on all 15 roles; richer editorial scale supplied in parallel via `LocalEditorialTypography`. |
| Shape | 6/10 | warn | **73 literal `RoundedCornerShape(N.dp)`** vs **7 `MaterialTheme.shapes` refs**; no brand `Shapes()` object configured — the one real MD3 gap. |
| Elevation | 9/10 | pass | Tonal surfaces, not shadows (1 `.shadow()` repo-wide, 0 `cardElevation`); book-cover shadow is the documented carve-out (DS §2.3). |
| Components | 8/10 | pass | Brand catalog wraps M3 primitives correctly; `NavigationBar`/`Rail`, `SegmentedButton`, M3 Expressive `ContainedLoadingIndicator`/`PullToRefresh` used with correct variants. |
| Layout | 9/10 | pass | Window size classes (COMPACT/MEDIUM/EXPANDED), `TwoPaneScaffold`, width-capped editorial panels, per-platform `mobile`/`jvm` layout splits. |
| Navigation | 9/10 | pass | Bottom `NavigationBar` at compact → `NavigationRail` at medium/expanded → two-pane list-detail at expanded. |
| Motion | 8/10 | pass | Spring/`pressScale`, reduced-motion gate (DS §2.5), M3 Expressive indicators; legacy easing reserved for transitions. |
| Accessibility | 8/10 | pass | 119/121 `Icon`/`Image` carry a real `contentDescription` (2 verified-decorative nulls); 43 `semantics` refs, 38 touch-target refs; desktop `DesktopTooltip` surfaces icon-only labels. |
| Theming | 9/10 | pass | `MaterialTheme` via `RhaydusTheme` with light/dark/dynamic; `dynamicColorSchemeOrNull` `expect`/`actual` (Android 12+ Material You, brand fallback elsewhere). |

### Critical Issues

None. No MD2 (`@material/mdc-*`) usage, no broken tonal pairing, no shadow-for-elevation anti-pattern.

### Warnings

1. **Shape tokens are magic numbers, not a scale (Shape 6/10).**
   - Why it matters: **73** `RoundedCornerShape(N.dp)` literals across feature + designsystem against only **7** `MaterialTheme.shapes` references. The theme never supplies a `shapes =` argument to `RhaydusTheme`, so `MaterialTheme.shapes` is the foundation default and components that *do* read it diverge from the ones using literals. The literal values cluster (20dp ×14, 4dp ×10, 8dp ×7, 6dp ×6, 24dp ×4, 16dp ×4, 10dp ×4, 28dp ×3, 12dp ×3) — a *de facto* scale that lives nowhere as a token, so the editorial corner language (`../reference/design-system.md` §2.3 describes "small on cards, medium on buttons, 28dp on sheets") can't be retuned in one place and can drift per call site.
   - Evidence: `feature/library/.../LibraryShelf.kt`, `feature/book_detail/.../BookDetailShelf.kt`, `core/designsystem/.../component/PillChip.kt`, `.../UpdateProgressBottomSheet.kt`, and ~25 other files; `core/designsystem/.../theme/Theme.kt:107-110` (`RhaydusTheme(colorScheme, typography)` — no `shapes`); no `Shapes(` object defined anywhere in `:core:designsystem`.
   - Fix direction: define a brand `Shapes` (or a named `EditorialShapes` object of `RoundedCornerShape` constants) mapping the cluster to MD3 roles (`extraSmall` 4dp, `small` 8dp, `medium` 12dp, `large` 16/20dp, `extraLarge` 28dp), pass it to the theme, and replace the literals with `MaterialTheme.shapes.*` / the named constants. `PillChip`'s `RoundedCornerShape(percent = 50)` (≈ `shapes`-`full`) is correct and stays.
   - References: <https://developer.android.com/develop/ui/compose/designsystems/material3#shapes>, <https://m3.material.io/styles/shape/overview>

### Passing (done well)

- **Color & theming.** `core/designsystem/.../theme/Color.kt` is a complete Material Theme Builder scheme (40 light + 40 dark roles); `Theme.kt` wires both into `lightColorScheme`/`darkColorScheme` and through `RhaydusTheme`. Dynamic color is a proper `expect fun dynamicColorSchemeOrNull(...)` with Android 12+ Material You and a brand fallback on iOS/JVM/old Android. Tonal pairing is respected; the only off-scheme colors are (a) scrims/`Color.White`/`Color.Black` over photographic cover art and the camera feed — a documented light-on-media carve-out (DS §2.1, §6) — and (b) `RatingGold` / spoiler covers, deliberate non-theme brand signals.
- **Elevation is tonal.** Surface-container shades carry depth; essentially no drop shadows (the sole exceptions are the cover/hero shadows the design system explicitly calls out as "physical object" cues). This is exactly MD3's tonal-elevation model.
- **Typography.** The full M3 type scale is populated (Fraunces display on display/headline, Inter body on the rest) and a 15-role editorial scale rides alongside via a `CompositionLocal`, so screens get both the MD3 roles and the brand voice.
- **Adaptive & navigation.** First-class window-size-class handling (`rememberWindowSizeClass().widthClass`), `TwoPaneScaffold` list-detail at expanded width, rail-vs-bar navigation by width, and `mobileMain`/`jvmMain` layout actuals — well beyond a phone-only layout.
- **Components.** M3 primitives are wrapped into a documented brand catalog (`PillChip`, `AdaptiveModalSheet`, `SoftcoverTopBar`, `ChooseListsBottomSheet`) rather than re-skinned ad hoc, and current M3 Expressive components (`ContainedLoadingIndicator`, `PullToRefreshDefaults.IndicatorBox`, `SegmentedButton`) are used where the spec recommends them.
- **Accessibility.** Near-total `contentDescription` coverage on icons/images, real `semantics`/touch-target discipline, and a desktop tooltip seam that gives icon-only pointer controls the visible label their `contentDescription` alone wouldn't surface.

### Recommended Fixes (priority order)

1. **Centralize shape into tokens.** Add a brand `Shapes`/`EditorialShapes` object, pass it to `RhaydusTheme`, and migrate the 73 `RoundedCornerShape(N.dp)` literals to `MaterialTheme.shapes.*` / named constants. Single highest-leverage MD3 fix; makes the editorial corner language tunable in one place. Ref: <https://developer.android.com/develop/ui/compose/designsystems/material3#shapes>
2. **(Carries over from Critical Finding 2 above.)** Externalizing the hardcoded UI strings in shared components also improves MD3 component reusability — same fix, no extra work.

### Notes & Limits

- This MD3 score is **adjacent coverage**, scored independently of the 83/100 four-category Compose audit; the two do not roll up into a single number.
- Off-scheme colors over imagery (cover scrims, camera-feed chrome) are treated as a **documented carve-out**, not a deduction — text over photographic content needs fixed-contrast white/black regardless of theme, and `../reference/design-system.md` §2.1/§6 sanctions it as "the one place light-on-media copy is allowed."
- `RatingGold` and the spoiler-cover colors are intentional brand signals outside the Material scheme (documented in `Color.kt`), not token violations.
- Method: source inspection across `:feature:*`, `:core:designsystem`, `:orchestration` (theme files read in full; token/shape/color/component/accessibility usage measured by repo-wide grep). No running-app or browser inspection (Compose Multiplatform, no web target).
