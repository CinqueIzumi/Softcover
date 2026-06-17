# Jetpack Compose Audit Report

Target: /Users/bartbos/Documents/projects/Softcover
Date: 2026-06-09
Scope: Whole repo — `core/designsystem` (shared component library, `commonMain`), `feature/*` presentation layers (mix of `commonMain` and `src/main/java`), `orchestration` (nav host). Compose Multiplatform, Kotlin 2.3.21.
Excluded from scoring: `*/debug/*` and `*/release/*` debug-routes wiring, `core/designsystem/.../debug/*` (MotionDebugScreen, ShareCardDebugScreen), `ktlint-rules` (build tooling).
Confidence: Medium
Overall Score: 78/100

## Scorecard

| Category | Score | Weight | Status | Notes |
|----------|-------|--------|--------|-------|
| Performance | 8/10 | 35% | solid | Named-only skippable 100% (measured, 1 module); exemplary animation/modifier hygiene. Capped at 8 by unstable `UiState` data classes carrying non-trivial structural `equals()` under Strong Skipping. |
| State management | 8/10 | 25% | solid | Clean TOAD/UDF: single `StateFlow<UiState>` per screen, flow shaping in `flows/` not composables, ScreenModels at screen entry. Minor dual-tracked scroll intent. |
| Side effects | 8/10 | 20% | solid | Strong effect discipline (keyed effects, `rememberUpdatedState`, full `DisposableEffect` cleanup). One isolated `navigator.pop()` in a composition body. |
| Composable API quality | 7/10 | 20% | solid | 24/25 shared components model `modifier` correctly; modern custom modifiers; no `MutableState` params. Held back by a systemic style-enum variant pattern (counter to component guidelines), 2–3 `modifier`-param gaps, and i18n-blocking default strings. |

## Critical Findings

1. **Performance (measured): unstable `*UiState` / state classes carry non-trivial structural `equals()`, evaluated every recomposition under Strong Skipping**
   - Why it matters: Strong Skipping is ON (`StrongSkipping: true` in the explore module report). Under SSM all named composables become skippable (measured named-only skippable% = 100%), and the skip decision is made by running `equals()` on each *unstable* parameter every recomposition. The compiler reports `ExploreScreenUiState`, `ExploreLocalVariables`, and the action types as unstable; these are `data class`es that wrap collections, so each skip check walks the collection. This is the binding ceiling driver — not a correctness bug (TOAD `UiState`s are immutable `data class`es, so equality is correct), but it makes "skipping" cost scale with state size on hot screens.
   - Evidence: `feature/explore/build/compose_audit/explore-classes.txt` (`unstable class …ExploreScreenUiState`, `…ExploreLocalVariables`); `feature/explore/build/compose_audit/explore-module.json` (`StrongSkipping: true`, 16 inferred-unstable classes)
   - Fix direction: keep `UiState` flat and stable — back list-typed fields with `kotlinx.collections.immutable` (`ImmutableList`/`PersistentList`) so `equals()`/`hashCode()` stay cheap and structurally shared, and prefer passing *narrow slices* of `UiState` into child composables rather than the whole object, so the per-recomposition `equals()` runs over the smallest surface. Verify with a full-repo compiler-report pass (see Notes And Limits).
   - References: <https://developer.android.com/develop/ui/compose/performance/stability/strongskipping>, <https://developer.android.com/develop/ui/compose/performance/stability>, <https://developer.android.com/develop/ui/compose/performance/stability/fix>

2. **Side effects: navigation invoked from a composition body**
   - Why it matters: `FocusModeScreen.Content()` calls `navigator.pop()` directly in the composition body when the active session is `null`, then `return`s. Navigation is a side effect; running it during composition (rather than from a `LaunchedEffect` / event handler) is fragile — it fires during the composition pass and re-runs on every recomposition where the guard holds. It is isolated (every other screen routes navigation through the `ObserveAsEvents` event channel or effects), which is why it stands out.
   - Evidence: `feature/session/.../screen/FocusModeScreen.kt:79-83`
   - Fix direction: move it into an effect — `LaunchedEffect(active) { if (active == null) navigator.pop() }` — and emit a neutral placeholder from the body.
   - References: <https://developer.android.com/develop/ui/compose/side-effects>, <https://developer.android.com/develop/ui/compose/navigation>

3. **Composable API quality: shared components collapse visual variants into a single `style:` enum instead of distinct components**
   - Why it matters: `SoftcoverButton(style: ButtonStyle)`, `SoftcoverToggleButton(style: ToggleButtonStyle)`, `SoftcoverIconToggleButton(style:)`, and `UnreleasedBadge(style:)` each dispatch internally on a style enum. The Compose component API guidelines prefer *distinct components per visual variant* (`TextButton` / `OutlinedButton` / `ElevatedButton`, as Material 3 itself ships them) over one component with a `style` parameter — distinct components give each variant its own parameter surface and defaults, and avoid a god-component. This is a deliberate, internally-consistent design-system choice (noted as a tradeoff, not a defect), but it runs counter to the guideline and is systemic, so it caps the category.
   - Evidence: `core/designsystem/.../component/SoftcoverButton.kt:229` (`style: ButtonStyle`), `…/SoftcoverButton.kt:167` (`ToggleButtonStyle`), `…/SoftcoverButton.kt:54` (`IconToggleButtonStyle`), `…/component/UnreleasedBadge.kt:48`
   - Fix direction: if the surface is ever extracted as a library, split into `SoftcoverFilledButton` / `SoftcoverTonalButton` / `SoftcoverOutlinedButton` / `SoftcoverTextButton` wrappers over a shared private impl. For an app-internal DS this is optional — record the deviation.
   - References: <https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-component-api-guidelines.md>, <https://developer.android.com/develop/ui/compose/api-guidelines>

## Adjacent Findings

### Android Launch UX

- Android 12+ splash icon status: **not configured** (no risk).
- Evidence: `orchestration/.../MainActivity.kt:79` uses AndroidX `installSplashScreen()`; the app theme `core/designsystem/src/androidMain/res/values/themes.xml` is `Theme.Material3.Light.NoActionBar` and sets **no** `windowSplashScreenAnimatedIcon`. With no static splash icon configured via the theme, the Android 12+ `ImmobileIconDrawable` blur path is not triggered.
- Finding: none. (If a `windowSplashScreenAnimatedIcon` is added later, make it resolve to an `<animated-vector>` wrapper on API 31+ — an empty one is enough — referencing a separately-named vector to avoid a self-reference loop.)
- References: <https://developer.android.com/develop/ui/views/launch/splash-screen>, <https://developer.android.com/reference/androidx/core/splashscreen/SplashScreen>, <https://issuetracker.google.com/issues/520672537>

## Category Details

### Performance — 8/10

**Ceiling check**

- Strong Skipping: **on** (`StrongSkipping: true`, Kotlin 2.3.21 / Compose Compiler default).
- Ceiling table applied: **SSM-on**.
- Module-wide `skippable%`: 47/69 = **68.1%** — anchored down by zero-arg/composable lambdas; not the binding metric.
- Named-only `skippable%`: 24/24 = **100%** (from `explore-composables.csv`, `isLambda == 0`). This is the binding metric and confirms every named composable in the measured module skips.
- Unstable shared types from compiler: 16 inferred-unstable classes in the explore module — all data-layer / ScreenModel / `UiState` / action types, none are leaf UI primitives.
- SSM-on binding evidence: **non-trivial `equals()` cost** on a handful of unstable `UiState`/state `data class`es passed through screen-level composables. No widespread instance-recreation churn observed (only minor, localized allocations — see below). No unjustified `@NonSkippableComposable` / `@DontMemoize`.
- Qualitative score: 9/10.
- Ceiling: cap at 8 (named-only ≥95% but a handful of unstable params carry non-trivial `equals()` cost).
- Applied score: **8/10**.

**What is working**

- Animation/modifier hygiene is exemplary and systemic: per-frame animated values are read through lambda modifiers (`graphicsLayer { … }`, `drawBehind`, `drawWithContent`), never piped into non-lambda `Modifier.offset(x.value)` / `Modifier.alpha(value)` forms.
- Every `Animatable` is held in `remember { … }` and driven from a `LaunchedEffect`; no recomposition-restart smell found.
- Lazy lists carry stable `key =` where identity moves; no deprecated `animateItemPlacement()` (modern `Modifier.animateItem()` is used); `enableEdgeToEdge()` is present; `derivedStateOf` is used correctly for a scroll threshold.
- No expensive collection transforms or O(N) string work found inside `@Composable` bodies (that logic lives in pure functions / state holders).

**What is hurting the score**

- Unstable `UiState`/state `data class`es wrapping collections → per-recomposition `equals()` cost under SSM (the cap driver; see Critical Finding 1).
- A few isolated per-recomposition allocations that should be hoisted (low severity, not churn passed across composable boundaries).

**Evidence**

- `feature/explore/build/compose_audit/explore-classes.txt` — `ExploreScreenUiState`, `ExploreLocalVariables` inferred unstable; structural `equals()` runs each recomposition under SSM · References: <https://developer.android.com/develop/ui/compose/performance/stability/strongskipping>
- `core/designsystem/.../modifier/ModifierExtensions.kt:125` — `shimmerColors = listOf(Color.copy()…)` rebuilt every recomposition inside the `shimmer` modifier (used widely across skeleton states). Consumed in `drawWithCache`, so it doesn't block skipping, but it allocates a list + 3 `Color`s per recomposition; hoist to a top-level `val` / `remember`. · References: <https://developer.android.com/develop/ui/compose/performance/bestpractices>
- `feature/reading/.../component/StreakStrip.kt:166` — `activity.asReversed().forEach { … }` allocates a reversed view each recomposition (line 72 does this correctly via a hoisted `val`); mirror that. · References: <https://developer.android.com/develop/ui/compose/performance/bestpractices>
- `feature/library/.../component/LibraryControlStrip.kt:129` — `listOf(LibrarySortMode.ORDER) + ordered` builds a fresh list per recomposition; wrap in `remember(ordered, customListRanked)`. · References: <https://developer.android.com/develop/ui/compose/performance/stability>
- `feature/session/.../screen/FocusModeScreen.kt:112-122` — the 1-second clock `LaunchedEffect` keeps ticking `now` (and recomposing `FocusModeContent`) while the session `isPaused`; the recompositions are wasted when paused. Gate the loop on `!isPaused`, or read `now` in a lower-scoped child. Minor. · References: <https://developer.android.com/develop/ui/compose/performance/phases>

### State Management — 8/10

**What is working**

- Textbook unidirectional data flow via the TOAD framework: each screen exposes a single `StateFlow<UiState>` with `private val _state = MutableStateFlow(...); val state = _state.asStateFlow()`, and events via `receiveAsFlow()` — verified in `ToadScreenModel`.
- Flow shaping (`combine` / `flatMapLatest` / `collectLatest`) lives entirely in `presentation/flows/` collectors, never inside composable bodies.
- ScreenModels are obtained at screen entry (`Content()`), never deep in the tree; no ViewModel-in-`CompositionLocal`.
- Reusable components are correctly stateless where it matters: `ChooseListsBottomSheet` and `LibraryFilterSheet` derive everything from props; `StarRatingInput` cleanly splits an internal *preview* state from the hoisted source-of-truth `rating`.
- No autoboxing (`mutableStateOf<Int>`); `rememberSaveable` used with stable keys for per-review expansion state.

**What is hurting the score**

- One isolated case of UI mechanics tracked in two places that could be driven from `UiState` (low severity).
- (Investigated and dismissed: `ReviewEditorBottomSheet.kt:74` `remember { documentToEditorBuffer(initialDocument) }` without a key is **deliberate and correct** — it seeds the editor buffer once so an upstream review round-trip does not clobber in-progress edits. Keying it would be the bug.)

**Evidence**

- `core/designsystem/.../toad/ToadScreenModel.kt:20-27` — `_state`/`state.asStateFlow()` + events via `receiveAsFlow()`; correct mutable-API narrowing (positive). · References: <https://developer.android.com/develop/ui/compose/architecture>
- `feature/library/.../flows/AllBooksCollector.kt` — reactive pipeline shaped in a collector, not a composable (positive). · References: <https://developer.android.com/develop/ui/compose/architecture>
- `feature/library/.../screen/LibraryScreen.kt:1432-1453` — `previousKey` / `pendingScrollToTop` dual-track scroll-to-top intent in composition `remember` blocks; this intent is better expressed as a one-shot signal in `UiState`. Isolated refactor opportunity. · References: <https://developer.android.com/develop/ui/compose/state-hoisting>

### Side Effects — 8/10

**What is working**

- Consistent one-time-event handling through the shared `ObserveAsEvents` helper; navigation, snackbars, and analytics fire from event handlers / effects across `BarcodeScannerScreen`, `BookDetailScreen`, `CreateListScreen`, `OnboardingScreen`.
- Effects are correctly keyed: `AnimatedStatNumber`, `MarkAsReadBurst`, `ShakeOnError` drive `Animatable.animateTo`/`snapTo` from `LaunchedEffect(key)`; `ShakeOnError` uses `rememberUpdatedState(onShakeEnd)` to avoid stale capture.
- `DisposableEffect` cleanup is complete where it counts — `BarcodeScanner.android` unbinds the camera provider, closes the scanner, and shuts down the executor in `onDispose`.
- `snapshotFlow { … }` is always collected from inside a `LaunchedEffect` (`PullToRefreshEyebrow`, `BottomNavigationBar`, `LibraryScreen` pager); `requestFocus()` is always wrapped in `LaunchedEffect(Unit)`.
- `rememberCoroutineScope().launch { … }` is reserved for genuine gesture/event-driven animation (pager scroll on tab click / continue button), not keyed long-lived work.

**What is hurting the score**

- One navigation call in a composition body (see Critical Finding 2).

**Evidence**

- `feature/session/.../screen/FocusModeScreen.kt:79-83` — `navigator.pop()` invoked in the composition body; move to a `LaunchedEffect(active)`. · References: <https://developer.android.com/develop/ui/compose/side-effects>, <https://developer.android.com/develop/ui/compose/navigation>
- `core/designsystem/src/androidMain/.../component/BarcodeScanner.android.kt:84-139` — `DisposableEffect(lifecycleOwner)` with full camera/executor teardown (positive). · References: <https://developer.android.com/develop/ui/compose/side-effects>
- `core/designsystem/.../component/PullToRefreshEyebrow.kt:50-61` — `snapshotFlow { distanceFraction }` collected inside `LaunchedEffect` (positive). · References: <https://developer.android.com/develop/ui/compose/side-effects>

### Composable API Quality — 7/10

**What is working**

- 24 of 25 shared components expose `modifier: Modifier = Modifier` as the first optional parameter, applied once to the root-most emitted node (`SoftcoverButton`, `SoftcoverImage`, `EditorialSectionHeader`, `StarRatingInput`, `DropCapText`, `PillChip`, …).
- No `MutableState<T>` / `State<T>` parameters in any reusable API — uniformly `value: T` + `onValueChange: (T) -> Unit`.
- Custom modifiers use modern patterns (`graphicsLayer`, `drawWithCache`, `drawWithContent`, `rememberInfiniteTransition`) — zero deprecated `Modifier.composed { }`.
- Slot APIs use receiver scopes where they guide layout (`SoftcoverTopBar` `additionalActions: @Composable RowScope.() -> Unit`); 100% PascalCase, Unit-returning composables; strong `@Preview` coverage on the button family; theme-driven motion specs (`LazyItemMutationAnimator` uses `MaterialTheme.motionScheme`).

**What is hurting the score**

- Systemic style-enum variant pattern, counter to the component guidelines (see Critical Finding 3).
- A few `modifier`-contract gaps and an i18n-blocking default-string pattern.

**Evidence**

- `core/designsystem/.../component/ClickableText.kt:18` — reusable component exposes **no** `modifier` parameter; callers cannot apply layout/appearance. · References: <https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-component-api-guidelines.md>
- `core/designsystem/.../component/HeroStatNumberField.kt:36` — `modifier` is the 6th parameter (after several required callbacks) instead of the first optional; reorder to required → `modifier` → optional. · References: <https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-component-api-guidelines.md>
- `core/designsystem/.../component/SoftcoverLoadingDialog.kt:38` (`SoftcoverLoadingSheet`) — no `modifier` parameter on a visual surface. · References: <https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-component-api-guidelines.md>
- `core/designsystem/.../component/ChooseListsBottomSheet.kt:43-48` — human-readable English strings baked into default parameter values (`"Choose lists for N books"`); blocks localization. Prefer `stringResource`-backed defaults or require the caller to supply text. · References: <https://developer.android.com/develop/ui/compose/resources>
- `core/designsystem/.../component/SoftcoverButton.kt:229` — `style: ButtonStyle` single-component variant dispatch vs distinct per-variant components (systemic; deliberate tradeoff). · References: <https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-component-api-guidelines.md>

## Prioritized Fixes

1. **Keep `*UiState` cheap to compare under Strong Skipping.** Back list fields in screen `UiState`/state classes with `kotlinx.collections.immutable` (`ImmutableList`/`PersistentList`) and pass narrow slices into child composables rather than the whole object. Start with `feature/explore/.../presentation/state/ExploreScreenUiState` (measured unstable) and apply the pattern across features. Verify with a full-repo compiler-report pass. — <https://developer.android.com/develop/ui/compose/performance/stability/fix>
2. **Move navigation out of composition.** In `feature/session/.../screen/FocusModeScreen.kt:79-83`, replace the body-level `navigator.pop()` with `LaunchedEffect(active) { if (active == null) navigator.pop() }`. — <https://developer.android.com/develop/ui/compose/side-effects>
3. **Close the shared-component `modifier` contract gaps.** Add `modifier: Modifier = Modifier` to `ClickableText.kt:18` and `SoftcoverLoadingSheet` (`SoftcoverLoadingDialog.kt:38`), and reorder `HeroStatNumberField.kt:36` so `modifier` is the first optional parameter. — <https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-component-api-guidelines.md>
4. **Optional follow-up:** hoist the per-recomposition allocations (`ModifierExtensions.kt:125` shimmer colors, `StreakStrip.kt:166`, `LibraryControlStrip.kt:129`) and gate the `FocusModeScreen` clock loop on `!isPaused`. Low individual impact; cheap cleanup.

## Notes And Limits

- The whole repo was mapped; four category scans plus targeted manual verification were run. Two agent-reported "critical" items were investigated and **downgraded**: the `FocusModeScreen` 1-second clock is an idiomatic timer (not frame-thrashing — minor only while paused), and `ReviewEditorBottomSheet.kt:74`'s keyless `remember` is the deliberately-correct way to seed an editor buffer.
- **Confidence is Medium** primarily because compiler diagnostics are partial (see below); source coverage itself was broad.
- Adjacent coverage notes: **KMP/CMP** surface is large and the repo is mid-migration (`refactor/kmp` branch) — several features are already in `commonMain`, others still in `src/main/java`. Platform interop (camera, clipboard, haptics, share, notifications) is correctly isolated to `expect`/`actual` leaf composables with lifecycle handled in effects. **Testing**: not audited in depth this pass. **Focus/keyboard**: `FocusRequester`/`requestFocus` patterns are present and correctly effect-wrapped.
- Android Launch UX resources: **not configured** (AndroidX `installSplashScreen()` with no theme `windowSplashScreenAnimatedIcon`) — no blurry-icon risk.
- Strong Skipping mode: **on** (Kotlin 2.3.21 default; `StrongSkipping: true` in the explore module report). No module-level opt-outs observed.
- Weight choice: default **35/25/20/20**. No deviation.
- Renormalization: none (no `N/A` categories).
- Compiler diagnostics used: **yes, but partial.** `./gradlew :app:assembleDebug` with the audit init-script produced Compose Compiler reports for **one** module only — `feature/explore` (other modules were up-to-date in the incremental build and did not re-emit reports). Module-wide skippable% (68.1%) and named-only skippable% (100%) and the unstable-classes list are measured **for explore**; stability claims for other modules are **inferred from source** and from the SSM-on framework pattern. Treat the 100% named-only figure as a representative, not a repo-wide, ground truth. To get full coverage, re-run after `./gradlew clean` (or against `:app:assembleRelease`) so every Compose module recompiles: `./gradlew clean :app:assembleDebug --init-script ~/.claude/skills/jetpack-compose-audit/scripts/compose-reports.init.gradle --no-daemon`.

## Suggested Follow-Up

- Run `material-3` audit — the app has a rich custom editorial design system (`EditorialTypography`, custom theme, expressive Material 3 alpha); design-system/theming compliance is out of scope here and worth a dedicated pass.
- Run `compose-agent focus on kmp` — the active KMP migration is the highest-leverage place for a boundary review (`expect`/`actual` shape, Android-only APIs in `commonMain`, platform interop leaves).
- Run `compose-agent focus on testing` — UI/screenshot test coverage was not assessed in this pass.
