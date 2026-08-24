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
  `docs/reference/design-system/components.md` §4. It is app-generic already — lifting it upstream is a
  move, not a rewrite, and the app component would then become a thin alias or be dropped.

### F24 — `import-grouping` ktlint rule (Android → third-party → project)

- **Type:** gate (lint rule)
- **Home:** `nl.rhaydus:ktlint-rules`
- **Status:** **Open.** The foundation code-style doc (§Import Ordering) mandates three import groups in
  order — 1) Android / AndroidX, 2) third-party, 3) project (`nl.rhaydus.*`) — but **nothing enforces
  it.** `.editorconfig` sets `ktlint_standard = disabled` (deliberately, so the standard rules don't
  fight the idiosyncratic guide), which turns off ktlint's own `import-ordering` rule; and the existing
  custom `project-import-order` rule only sorts alphabetically *within* the `nl.rhaydus.*` group. So a
  project import placed above the Android block passes `ktlintCheck` clean. Verified 2026-07-13: a stray
  `nl.rhaydus.*` import at the top of `ExploreShelf.kt` / both `SettingsScreenLayout` actuals passed the
  gate, and `ktlintFormat` did not move it.
- **What to build:** an autocorrecting ktlint rule that partitions imports into the three groups in the
  documented order (stable within each group; the existing alpha-sort within `nl.rhaydus.*` composes on
  top). It should be a pure-AST rule (no type resolution needed). Once it lands, `ktlintFormat`
  auto-fixes and `ktlintCheck` gates — replacing the guide-only + review enforcement that stands today.
- **Scope note:** Softcover currently has this as a review-enforced convention. A one-off codebase
  regroup was applied 2026-07-13 (312 files, Android→third-party→`nl.rhaydus.*`), so the app is clean;
  this rule keeps it that way for every consumer with zero setup.

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
