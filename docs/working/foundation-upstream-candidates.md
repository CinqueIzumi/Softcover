# Foundation upstream candidates

Findings discovered while building Softcover that belong in the **nl.rhaydus foundation** rather than
in this app — upstream bugs to fix, app-local mechanisms that should become shared foundation
capabilities, and gates that should move from advisory (here) to blocking (in the foundation).

This is an **internal** working doc. It is the queue for the next foundation revision; nothing here is
acted on automatically. Before reaching for a workaround in the app, check whether the right fix is
upstream and record it here.

**Process.** With `foundation.local=true` (includeBuild against `../rhaydus-foundation`), fix it at the
source and bump the foundation. Otherwise file it against the foundation and track it here until a
released version carries the fix, then re-run the `rhaydus-adopt` agent. The capability surface these
entries refer to is indexed in [`../rhaydus/0.3.1/CAPABILITIES.md`](../rhaydus/0.3.1/CAPABILITIES.md).

Each entry: **type** (bug / enhancement / gate), **home** (target foundation module), **status**, and
enough context for whoever picks it up. F-numbers are stable identifiers (referenced from commits and
other docs) and are **never reused or renumbered**.

> **Adopted items are pruned from this doc.** Everything that reached *Implemented & adopted* (F1–F17,
> F19, F22, F23 — the core-common / core-platform / offline-sync / designsystem-core / toad /
> ktlint-rules / detekt-rules work) has landed in the foundation and is live in Softcover, so its
> entries were removed. What remains below is what still needs action: the not-yet-adopted build-logic
> batch and newly-filed open candidates.

## Still pending (context)

Softcover runs on **published foundation `0.3.1`**, resolved from `mavenCentral()` (`foundation.local=false`
in `local.properties`, the committed default). All nine `0.3.1` artifacts are live on Central and verified
resolving. `foundation.local=true` remains available as an inner-loop switch (`includeBuild
"../rhaydus-foundation"`) but is off by default.

**Next up:**
1. **Publish the foundation `build-logic` as Gradle plugins** — the only thing standing between
   F18/F20/F21 and adoption.
2. **Wire the foundation's own `detektCheck` into its `check`** — it is registered but attached to
   nothing, so the foundation does not gate on the config it ships.

Agent, hook and session-loop changes to port into the `rhaydus-kotlin` plugin are tracked separately in
[`foundation-token-hygiene.md`](foundation-token-hygiene.md).

---

# Open candidates

Filed but not yet implemented in the foundation.

### F28 — A `LocalDate`-in/`LocalDate`-out date picker dialog

- **Type:** component
- **Home:** `nl.rhaydus:designsystem-core` (`component/`)
- **Status:** **Open.** `designsystem-core` ships no date picker, so every consumer wires Material 3's
  `DatePickerDialog` itself — and Material 3's `DatePickerState` exposes the selection as **UTC** epoch
  millis (`initialSelectedDateMillis` / `selectedDateMillis`, canonicalised to start-of-day UTC). Passing
  those through the device timezone is the obvious-looking thing to write and is wrong: west of UTC the
  confirmed date lands a **day early**, east of it the dialog opens on the day (and at a month boundary,
  the month) *before* the one it was handed. Softcover shipped exactly that bug in its deadline picker —
  a user west of UTC reported having to pick the following day to get the day they wanted, and it was
  invisible to a developer at UTC+2, where only the initial highlight is wrong. The trap is generic to
  Material 3, not to Softcover, and it is silent: it compiles, reads naturally, and only misbehaves at
  certain offsets.
- **What to build:** `RhaydusDatePickerDialog(initialDate: LocalDate?, onDismiss, onConfirm: (LocalDate) -> Unit)`,
  hosting `rememberDatePickerState` + `DatePickerDialog` and keeping the UTC millis conversion `internal`
  so no consumer can reach it. Confirm/dismiss labels as params (Softcover's are hardcoded "Confirm" /
  "Cancel" today). Optionally a `selectableDates` pass-through for surfaces that must exclude past days.
- **Scope note:** Softcover's implementation is `SoftcoverDatePickerDialog` +
  `presentation/util/PickerDates.kt` in `:core:designsystem`, catalogued in
  `docs/reference/design-system/components/sheets.md`. It is app-generic already — lifting it upstream is a
  move, not a rewrite, and the app component would then become a thin alias or be dropped.

### F24 — import ordering follows the IDE layout, enforced by ktlint

- **Type:** gate (lint rule) + doc
- **Home:** `nl.rhaydus:ktlint-rules`, foundation `code-style.md` §Import Ordering
- **Status:** **Open.** Softcover uses the IntelliJ / Android Studio Kotlin default layout
  `*,java.**,javax.**,kotlin.**,^` (pinned as `ij_kotlin_imports_layout` in `.editorconfig`), so
  Optimize Imports and the rule agree. The foundation doc still prescribes Android → third-party →
  `nl.rhaydus.*` groups. Nothing enforces either: `ktlint_standard = disabled`, the foundation runner
  loads only its own rules, and `project-import-order` only sorts within `nl.rhaydus.*`.
- **What to build:** replace `project-import-order` with an autocorrecting pure-AST rule that reads
  `ij_kotlin_imports_layout` (default `*,java.**,javax.**,kotlin.**,^`) and sorts the whole block, and
  rewrite §Import Ordering to match. `ktlintFormat` then fixes and `ktlintCheck` gates it. Aliases form
  their own trailing bucket: today `project-import-order` checks every `nl.rhaydus.*` path in one run,
  so 14 files with Apollo accessor aliases (`… .Companion.readingJournalFragment as …`) keep them in place.

### F25 — `trailing-lambda-brace` ktlint rule (no `) }` glomming over a multi-line body)

- **Type:** gate (lint rule) + code-style amendment
- **Home:** `nl.rhaydus:ktlint-rules` (+ a §Argument and Property Layout amendment in the foundation
  `code-style.md`)
- **Status:** **Open.** The foundation §Argument and Property Layout **exempts** a trailing lambda from
  the one-per-line rule and its examples show the glommed inline form
  (`scope.setState { it.copy(link = …) }`, `.onFailure { AppLog.e(…) }`). When the lambda body is a
  *multi-line* call, that exemption produces `) }` — the closing paren of the wrapped call and the
  lambda's closing brace glommed onto one line. Worse, the existing multi-arg wrapping rule (which
  exempts trailing-lambda calls) actively *creates* this shape when it wraps a call inside a trailing
  lambda (e.g. `ktlintFormat` turns `coVerify { call(a, b) }` into `coVerify { call(\n a,\n b,\n) }`).
  Softcover has tightened this as a delta in `docs/reference/code-style.md` §Formatting — see there for
  the exact good/bad forms.
- **What to build:** an autocorrecting ktlint rule that, when a trailing lambda's body is a multi-line
  construct (a wrapped multi-argument call, multi-line `it.copy(...)`, `AppLog.e(...)`,
  `async { call(...) }`, …), places the lambda's `{` and `}` on their own lines rather than glomming
  the closer as `) }`. A single-line lambda body (`x { it.first() }`) and a single-argument call inside
  a trailing lambda stay inline — only the multi-line-body case is affected. This likely means amending
  or coordinating with the existing multi-arg wrapping rule so the two don't fight.
- **Verified safe:** the un-glommed form is **stable** under the current `ktlintFormat` (2026-07-13: the
  formatter never collapses an already-un-glommed block back to `) }`) and passes `ktlintCheck`. So the
  guide amendment + a future rule do not conflict with today's tooling; the rule just makes the
  formatter *produce* the stricter form and gates it.
- **Scope note:** Softcover's own feature code (the "Hidden suggestions" slice) was un-glommed
  2026-07-13; the wider ~300-site codebase sweep (52 files, much of it in test files where `ktlintFormat`
  created the glom) is **deferred until this rule exists**, so the fix is applied once, by the tool,
  rather than hand-swept and left ungated.

### F29 — detekt rule: no file-level `.dp` constants

- **Type:** gate (lint rule)
- **Home:** `nl.rhaydus:detekt-rules`
- **Status:** **Open.** Softcover's `docs/reference/code-style.md` § Review-only rules says `.dp` values
  stay inline at the use site. Only review enforces it, and agents keep reintroducing
  `private val FooterGap = 12.dp` above composables.
- **What to build:** a type-resolved detekt rule that flags a top-level (file-level) property whose type
  is `Dp`, or whose initializer is a `.dp` literal. It also flags a top-level holder built only from
  `.dp` arguments (e.g. a private `BarSpec(width = 32.dp, …)`). Locals inside a function stay allowed.
- **Scope note:** Softcover inlines its existing 66 sites (30 files) as a tracked migration step. The rule
  then gates them at zero, with no baseline.

### F26 — `dismissOnEscape` that listens without taking focus

- **Type:** enhancement
- **Home:** `nl.rhaydus:designsystem-core` (`jvmMain`, `nl.rhaydus.designsystem.modifier.DesktopKeyboard`)
- **Status:** **Open.** `Modifier.dismissOnEscape(enabled, onDismiss)` requests focus on the node
  whenever `enabled` flips false → true (`LaunchedEffect(enabled) { … requestFocus() }`). That is right
  for the surfaces it was built for — a mode entered by a gesture (bulk-select, rearrange) or a viewer
  pushed over the page — where nothing is being typed at the moment of the flip. It makes the modifier
  **unusable gated** on any condition a *text field* drives: desktop Explore's Esc-clears-search wants
  `enabled = state.hasActiveSearch`, but that condition flips on the first typed character, so enabling
  it would pull focus out of the very field being typed into and swallow the rest of the word.
- **Workaround in the app:** desktop Explore holds the modifier unconditionally enabled and guards
  inside the callback (`ExploreScreenLayout.jvm.kt`). Correct, but it consumes every Esc keydown on the
  screen — harmless there (no other Esc consumer; the sheets are `Dialog`/`Popup` with their own focus
  scope), and not something to repeat on a surface that has one.
- **What to build:** separate "catch Esc" from "grab focus" — either a `grabsFocus: Boolean = true`
  parameter, or a sibling `Modifier.onEscape(enabled, onEscape)` that installs only the
  `onPreviewKeyEvent` handler and leaves focus alone (it still fires whenever focus sits anywhere inside
  the subtree, which is the persistent-field case). The gated form should also stop consuming the event
  when it does nothing, so a disabled rung leaves Esc for whatever else wants it.
- **Doc impact:** the foundation design-system §"Desktop Esc-to-dismiss" gains the gate-vs-guard rule;
  Softcover's own copy carries it today (`docs/reference/design-system/foundations.md` §2.5, and the
  search-chrome block in `design-system/layout.md` §3.1).

### F27 — `ExpandableFlowRow` composes every item, including the ones it never places

- **Type:** enhancement
- **Home:** `nl.rhaydus:designsystem-core` (`nl.rhaydus.designsystem.layout.ExpandableFlowRow`)
- **Status:** **Open.** `FlowRow`'s `content` slot is composed and measured in full up front — `maxLines`
  and the overflow indicator affect *placement* only, not composition (verified against the Compose
  Foundation measure pass in `FlowLayout.kt`). So a collapsed `ExpandableFlowRow` showing two lines still
  composes and lays out **every** item behind the fold. That was harmless while every call site fed it a
  bounded set, but the tag editor's suggestion cloud (`B.4.25`, 3.1.1) now hands it the user's entire
  per-category vocabulary uncapped, and the cloud re-derives on every keystroke in the naming field — so
  each character recomposes the whole filtered candidate set rather than the two visible lines.
- **Assessment:** not a live problem. `PillChip` is a cheap `Surface` + `Text`, and a realistic personal
  vocabulary in one category runs to tens of tags. It is a genuine scaling risk only for a power user with
  100+ tags in a single category. Filed rather than fixed because the right fix is a lazy/windowed
  implementation in the shared component, not a cap at the call site — a cap is exactly what `B.4.25`
  removed, and reintroducing one here would put the ceiling back by another route.
- **What to build:** a windowed `ExpandableFlowRow` that composes only what it will place (plus the
  indicator), so the collapsed cost is bounded by `collapsedLines` rather than by item count. Blocked in
  practice on the same thing as the deprecation cleanup: there is no maintained Compose overflow API to
  build it on (`FlowRowOverflow` is deprecated, `ContextualFlowRow` likewise).

### F31 — `AdaptiveModalSheet` cannot be made non-dismissible by swipe

- **Type:** bug
- **Home:** `nl.rhaydus:designsystem-core` (`nl.rhaydus.designsystem.component.AdaptiveModalSheet`)
- **Status:** **Open.** The bottom-sheet form builds `rememberModalBottomSheetState(skipPartiallyExpanded =
  true)` with no `confirmValueChange` and passes no `sheetGesturesEnabled`, so a drag-down hides the sheet
  and fires `onDismissRequest` even when `dismissOnTapOutside` and `dismissOnBackPress` are both false.
- **Impact in the app:** Softcover's re-auth sheet and `LoadingSheet` are meant to be non-dismissible; a
  swipe closes them, and the re-auth sheet's `Dismissed` resets the re-auth state.
- **What to build:** a `dismissible`-style gate that also refuses the `Hidden` target in
  `confirmValueChange` (and leaves programmatic `LocalModalSheetDismiss` working), or a `dismissOnSwipe`
  flag beside the existing two.

### F32 — A skeleton mask modifier that shimmers over real content

- **Type:** enhancement
- **Home:** `nl.rhaydus:designsystem-core` (`modifier/`, beside `shimmer`)
- **Status:** **Open — built locally first.** `designsystem-core` ships `shimmer` and `SkeletonCrossfade`,
  which only support a separate hand-sized skeleton layout swapped for the real one. That layout drifts
  from the real one and the screen jumps when data lands (Softcover's book detail: the shelve card's 42dp
  bars against taller real rows). Softcover now has a local mask in `:core:component`: it draws the
  shimmer over a leaf's real bounds and hides the content, so a screen renders its normal layout from a
  placeholder model while loading.
- **What to build:** move the local mask into `designsystem-core` unchanged in behaviour (shimmer over
  the measured bounds, content hidden, semantics cleared while masked, a 150ms grace period before the
  shimmer and a 300ms minimum once shown), release, and switch Softcover's call sites to it. Then delete
  the local copy.

### F33 — detekt type resolution cannot read Kotlin 2.3 metadata

- **Type:** bug (gate silently inert)
- **Home:** `nl.rhaydus:detekt-rules` + the detekt version in the shared catalog
- **Status:** **Open.** detekt 1.23.8 embeds the Kotlin 2.0 compiler, which reads library metadata only up
  to 2.1; Softcover builds on Kotlin 2.3.21. A `--debug` run logs 410 "incompatible version of Kotlin"
  errors (stdlib, Compose, every repo module), so calls into them stay unresolved and every type-resolved
  rule skips them silently: a probe's `println` and `material3.Text` went unflagged by `ForbiddenMethodCall`
  while `!!` on a builtin `String?` was caught. `rhaydus:UnguardedFlowTerminalRead` is very likely inert too.
- **What to build:** move to detekt 2 (`dev.detekt`, Kotlin 2.x compiler), port `detekt-rules` to its API,
  then probe each type-resolved rule on a known violation.

### F34 — Import ban with a per-file allowlist (text-leaf gate)

- **Type:** gate
- **Home:** `nl.rhaydus:ktlint-rules` or `nl.rhaydus:detekt-rules` (must not need type resolution, see F33)
- **Status:** **Open — blocks Softcover's text-leaf gate.** Softcover routes every text through one leaf
  (`SoftcoverText`) and needs `androidx.compose.material3.Text` banned everywhere except that leaf's file.
  detekt's `ForbiddenImport` allows one instance per config and is already scoped to the leaf modules.
- **What to build:** a configurable rule taking `(import, allowed file globs, reason)` entries, with a
  fully-qualified-call check alongside the import check; release, then enable it in Softcover with one
  entry: `androidx.compose.material3.Text`, allowed only in `SoftcoverText.kt`. The swap itself is done,
  so the gate passes on enable.

### F35 — `AdaptiveModalSheet`'s panel form has no motion or depth

- **Type:** enhancement
- **Home:** `nl.rhaydus:designsystem-core` (`nl.rhaydus.designsystem.component.AdaptiveModalSheet`)
- **Status:** **Open.** The panel form (expanded width) is a bare `Dialog`: no enter or exit animation, an
  instant dismiss, a 1 dp outline and no elevation, while the compact bottom sheet animates both ways.
- **Impact in the app:** every sheet on tablet and desktop (share, verdict, lists, re-auth, unknown ISBN)
  appears and vanishes in one frame.
- **What to build:** scale and fade the panel in, fade the scrim, and play the exit before
  `onDismissRequest` (Esc included), all gated by `playDecorativeMotion()`; give the panel elevation.

### F36 — Desktop share-card capture reports a cancel as a failure and hides the clipboard copy

- **Type:** bug
- **Home:** `nl.rhaydus:designsystem-core` (`nl.rhaydus.designsystem.share.JvmShareCardCapture`)
- **Status:** **Open.** `saveToGallery` returns `SaveOutcome.Failure("Save cancelled")` when the save
  dialog is cancelled, and `share` copies to the clipboard but returns a plain `ShareOutcome.Shared`.
- **Impact in the app:** cancelling a desktop save shows an error, and Share gives no hint that the
  image is now on the clipboard.
- **What to build:** a `SaveOutcome.Cancelled`, and a share outcome that says the image went to the
  clipboard, so the caller can confirm "Copied to clipboard".

### F37 — ktlint-rules has no unused-imports rule

- **Type:** gate (lint rule)
- **Home:** `nl.rhaydus:ktlint-rules`
- **Status:** **Open.** The runner (`nl.rhaydus.ktlint.MainKt`) hard-codes its rule providers, so
  ktlint's standard `no-unused-imports` never runs, even when `.editorconfig` enables it.
- **Impact in the app:** Softcover gates unused imports with detekt's `UnusedImports`, which reports
  but cannot fix and does not see imports that only resolve on native targets.
- **What to build:** an autocorrecting unused-imports rule in the ruleset, so `ktlintFormat` removes
  them. Once adopted, drop `UnusedImports` from Softcover's detekt config.

### F38 — `designsystem-core` describes a spacing scale but ships none

- **Type:** enhancement
- **Home:** `nl.rhaydus:designsystem-core`
- **Status:** **Open.** § 5.1 describes a small recurring spacing set; no token object backs it.
- **Impact in the app:** Softcover defines `Spacing` itself (2dp hairline plus a 4dp grid up to 48dp).
- **What to build:** a `Spacing` object in designsystem-core. Once adopted, drop the app-local one.

### F39 — No gate keeps raw dp literals out of spacing calls

- **Type:** gate (lint rule)
- **Home:** `nl.rhaydus:detekt-rules`
- **Status:** **Open.** Softcover's app-local `RawSpacingDp` reports a dp literal passed to `padding`,
  `PaddingValues`, `contentPadding`, `spacedBy`, `offset` or a `Spacer` size.
- **Impact in the app:** without it, spacing drifts back to per-screen numbers after the token sweep.
- **What to build:** the rule in the shared ruleset, with the allowed literals configurable.

### F40 — `designsystem-core` ships no corner scale and `RhaydusTheme` has no `shapes` parameter

- **Type:** enhancement
- **Home:** `nl.rhaydus:designsystem-core`
- **Status:** **Open.** `RhaydusTheme` cannot take app shapes and no token object backs corner radii.
- **Impact in the app:** Softcover defines `Corners` itself (2dp hairline, Material 3 Expressive radii up to 28dp, a pill).
- **What to build:** a `Corners` object and a `shapes` parameter on `RhaydusTheme`. Once adopted, drop the app-local one.

### F41 — No gate keeps raw radii out of corner shapes

- **Type:** gate (lint rule)
- **Home:** `nl.rhaydus:detekt-rules`
- **Status:** **Open.** Softcover's app-local `RawCornerDp` reports a dp literal or `percent` argument passed to
  `RoundedCornerShape` or `CutCornerShape`.
- **Impact in the app:** without it, corner radii drift back to per-screen numbers after the token sweep.
- **What to build:** the rule in the shared ruleset.

### F30 — `checkResourcePackaging`: Compose resources declared but never packaged on Android

- **Type:** gate (custom Gradle task)
- **Home:** `build-logic` convention plugins (alongside `rhaydus.module-graph`, F18)
- **Status:** **Open.** Implemented inline in Softcover's root `build.gradle.kts` as
  `checkResourcePackaging`, wired into every module's `check`. Generic — nothing in it is Softcover-specific.
- **The trap.** A KMP module that declares `compose.resources { packageOfResClass = … }` gets its generated
  `Res` accessor and compiles perfectly whether or not its resources are ever packaged. On Android, CMP
  resources ship as **assets**, and `com.android.kotlin.multiplatform.library` keeps Android resources off by
  default — so without `androidResources.enable = true` in the module's `androidLibrary` block the resources
  silently never reach the APK, and the first `Res` read throws `MissingResourceException` **at runtime**.
  Nothing in the source or the dependency graph is wrong, so no compile, lint, detekt, `projectHealth` or
  `checkModuleGraph` failure is possible. The one-line omission is invisible until a device executes the read.
- **Why it is worth upstreaming rather than remembering.** It bit Softcover **twice**, and neither was found
  by a check. `:core:component` gained its own copy in S4-5a and crashed the app on launch three commits
  later, the first time it ran on a device with no network (the offline banner is the only reader). The
  second, `:feature:settings`' bundled `ROADMAP.md` fallback, had **never worked on Android** since it
  landed — it is read only before the first live fetch, so any machine with a warm cache never touches it,
  and it would have surfaced on a fresh install after release. Both are the same line. Any foundation app
  that gives a module its own `composeResources` inherits the same trap, and the failure mode — latent,
  runtime-only, device-dependent — is the worst kind to leave to review.
- **What to build.** A task iterating subprojects: for each whose Compose `ResourcesExtension` has a
  non-empty `packageOfResClass`, resolve its `KotlinMultiplatformAndroidLibraryExtension` and fail when
  `androidResources.enable` is false. `packageOfResClass` is the right discriminator rather than a
  `composeResources/` directory on disk — declaring a resource package *is* the claim to own resources, and
  it is the only signal that covers a `customDirectory` module, which has no such directory (Softcover's
  `:feature:settings` is exactly that case). Verified by flipping a real module's flag and watching it fail.

---

# Implemented, not adopted

An item is **implemented** once it lands in the foundation. *Implemented, not adopted* means it landed
in the foundation but Softcover still ships its app-local copy and has **not** switched to the
foundation symbol — it arrives on the next `rhaydus-adopt` pass (or once `foundation.local=true` is
flipped and published).

Only the **build-logic** batch remains. F18, F20 and F21 landed on the foundation `release/0.3.0` branch
in `build-logic` (the convention plugins + root); each extracts a reusable gate mechanism from
Softcover's inline build config, with the concrete app data left configurable. They are **hard-blocked
on publishing**: the plugins resolve under `foundation.local=true` via
`pluginManagement { includeBuild(...) }`, but `foundation.local` is gitignored, so a CI run or fresh
clone would fail to *configure* — `build-logic` is not in the foundation's `mavenPublishing` set.

### F18 — `checkModuleGraph` tier-DAG + api-visibility enforcement

- **Type:** gate (custom Gradle task)
- **Home:** `build-logic` convention plugins
- **Status:** **Implemented, not adopted.** Landed as the root-applied `rhaydus.module-graph` convention plugin
  (`build-logic` `ModuleGraphConventionPlugin` + `ModuleGraphExtension`): the `checkModuleGraph` task derives
  each module's tier from its path and fails the build on any `project(...)` edge breaking the tier DAG, plus
  the api-visibility allowlist (an `api` edge to a data-area module must be allowlisted). The mechanism is
  generic — the concrete `tierOf` mapping, `allowedTargetTiers`, `dataAreaModules`, and `allowedApiDataEdges`
  are supplied per build via the `moduleGraph { }` extension; the foundation configures its own graph
  (core→{core}, designsystem→{designsystem, core}, toad→{}, tooling excluded), and it wires into every module's
  `check` like Softcover's inline version. Verified to fail on an injected illegal edge. Softcover still has
  the equivalent inline in its root `build.gradle.kts`; adoption applies `rhaydus.module-graph` and moves the
  Softcover `dataAreaModules` / `allowedApiDataEdges` / tier mapping into the `moduleGraph { }` block.

### F20 — `dependencyAnalysis` (buildHealth) gating policy

- **Type:** gate (policy)
- **Home:** `build-logic` (root)
- **Status:** **Implemented, not adopted.** Landed as the root `dependencyAnalysis { }` policy (plugin
  `com.autonomousapps.dependency-analysis` 3.14.1, applied at root + every subproject): `onUnusedDependencies`
  and `onIncorrectConfiguration` gate to `fail`; `onUsedTransitiveDependencies` / `onRuntimeOnly` /
  `onRedundantPlugins` are `ignore`. The exclusion list mirrors the foundation's own convention bundle (the
  junit/kotest/turbine/mockk test stack, koin + coroutines, the Compose Multiplatform + androidx-compose +
  desktop-host artifacts, `androidx.core:core-ktx`) plus `eu.anifantakis:ksafe` kept `implementation` (no
  KSafe type leaks into SecureStorage's public surface). Triaged to a green `buildHealth`; verified to fail
  when an exclusion is removed. Softcover keeps its own inline `dependencyAnalysis { }` (its exclusions cover
  its app libraries — voyager-koin, work-runtime-ktx, camera, mlkit, coil); adoption keeps only the
  app-specific exclusions and inherits the shared policy shape.

### F21 — Shared `lint.xml` + `warningsAsErrors` policy

- **Type:** gate (policy + config)
- **Home:** `build-logic` convention plugins + the shared `lint.xml`
- **Status:** **Implemented, not adopted.** The duplicated `lint { warningsAsErrors; abortOnError; lintConfig }`
  block in the two library convention plugins was consolidated into one `build-logic` helper
  (`Lint.applyRhaydusLintPolicy(project)`), and the root `lint.xml` gained the version-freshness policy
  (`NewerVersionAvailable` / `GradleDependency` / `AndroidGradlePluginVersion` = `informational`) so a newer
  upstream release never breaks a build pinned to the foundation catalog. Softcover already had the freshness
  policy in its own `lint.xml`; adoption is a no-op on the app side beyond inheriting the shared helper.
