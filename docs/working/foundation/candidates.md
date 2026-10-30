# Foundation candidates

The foundation backlog: findings from building Softcover that belong in the nl.rhaydus foundation, not in this
app. The capability surface is indexed in [`CAPABILITIES.md`](../../../foundation/docs/CAPABILITIES.md).
F-numbers are stable and never reused or renumbered.

**Process.** Fix it in `foundation/` on a branch, following `.claude/rules/foundation.md`, then move the
pointer. Or record it here until it is done.

Each entry: **type** (bug / enhancement / gate), **home** (target foundation module), **status**, and enough
context for whoever picks it up. Adopted items are pruned from this doc.

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

### F34 — Import ban with a per-file allowlist (text-leaf gate)

- **Type:** gate (Konsist assertion)
- **Home:** `nl.rhaydus:konsist-rules` (F67)
- **Status:** **Open — blocks Softcover's text-leaf gate.** Softcover routes every text through one leaf
  (`SoftcoverText`) and needs `androidx.compose.material3.Text` banned everywhere except that leaf's file.
  detekt's `ForbiddenImport` allows one instance per config and is already scoped to the leaf modules.
- **What to build:** an assertion taking `(import, allowed file globs, reason)` entries, also matching
  fully-qualified references; Softcover enables it with `androidx.compose.material3.Text` allowed only in
  `SoftcoverText.kt` (the swap is done, so it passes on enable). The same assertion takes the prose-only
  import rules: Material `Button`, `kotlinx.datetime.Instant` (use `kotlin.time.Instant`) and
  `rememberHaptics`, which retires Softcover's `RememberHapticsOutsideTheme`. A layering assertion alongside
  it: data and domain packages never import presentation.

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

### F43 — The expressive `MotionScheme` has no readable value-change spec

- **Type:** enhancement
- **Home:** `nl.rhaydus:designsystem-core`
- **Status:** **Open.** Softcover defines `Motion.valueChange` itself: a slow, no-bounce spring (critically damped, `StiffnessVeryLow`) that count-ups and chart geometry share. `Motion` is theme-provided: `SoftcoverTheme` supplies a full and a snapping instance, read through `MaterialTheme.motion`.
- **Impact in the app:** the default spring settles 0 to 24,092 in about 250 ms, too fast to read as a count.
- **What to build:** a named slow, no-bounce value-change spring alongside the scheme's specs. Once adopted, drop the app-local one.

### F57 — Gate: a `UiState` getter that builds a UI model

- **Type:** gate (Konsist assertion for the getter; detekt rule for the composable case)
- **Home:** `nl.rhaydus:konsist-rules` (F67), `nl.rhaydus:detekt-rules`
- **Status:** **Open — implemented locally.** Softcover's `UiModelBuiltInRender` flags the getter case as well
  as a UI model built in a composable; the reviewer memory on it is now redundant. The getter case is a
  declaration and moves to Konsist; the composable case inspects call sites and moves upstream as a detekt rule.
- **The trap:** `val header: FooUiModel get() = FooUiModel(...)` on a `UiState` looks like a stored field but
  runs the constructor on every render read, the same as building the model in the composable.
- **What to build:** flag a `get()`-bodied property on a `*UiState` class that constructs a `*UiModel`. The fix is
  a stored field with a default, with collectors `copy()` into it.

### F58 — `ktlintFormat` leaves `),)` when it wraps a nested call

- **Type:** bug (formatter)
- **Home:** `nl.rhaydus:ktlint-rules` (`multi-arg-wrapping`)
- **Status:** **Open.** The test-writer greps for `),)` after every `ktlintFormat` run.
- **Repro:** `outer.set(inner(arg1, arg2))` with `inner`'s arguments over the line limit formats to
  `outer.set(inner(\n arg1,\n arg2,\n),)`.
- **What to build:** the auto-fix also wraps the outer call, so the output is `outer.set(\n inner(\n …\n ),\n)`.

### F59 — Foundation API behaviour that lives only in reviewer memory

- **Type:** doc (KDoc + `toad-architecture.md`)
- **Home:** `nl.rhaydus:designsystem-core`, `nl.rhaydus:toad`, foundation `docs/toad-architecture.md`
- **Status:** **Open.** Three facts about foundation APIs that Softcover's reviewer keeps in memory. Once they
  are documented, delete those memory files.
- **What to document:**
  - `AdaptiveModalSheet` KDoc: a body of pinned rows plus one `weight(1f)` scrolling region fills the available
    height in both forms; `imePadding()` belongs on the outer column.
  - `hoverHighlight` / `pressScale` KDoc: `clickable` and its siblings already emit `HoverInteraction`, so an
    added `hoverable(source)` on the same source is a second emitter.
  - `toad-architecture.md`: a composable embedded in another Screen receives its ScreenModel from the owning
    `Screen`; `koinNavigatorScreenModel` widens the scope to the Navigator's lifetime.

### F61 — A style convention plugin owns the detekt and ktlint wiring

- **Type:** gate (convention plugin)
- **Home:** `build-logic`
- **Status:** **Open — implemented locally.** Softcover's root `build.gradle.kts` holds the generic wiring:
  extracting the baseline config from the `detekt-rules` jar, the type-resolved task set, source repointing,
  the vacuous-pass guard, the ktlint tasks and `styleCheck`. The foundation scans itself in light mode only,
  so `UnguardedFlowTerminalRead` never runs on its own modules.
- **What to build:** one convention plugin that applies detekt (full analysis) and ktlint with the shared
  config layered under an optional project file, creates the Konsist test module (F67), and registers
  `styleCheck`. The foundation applies it to
  itself. Softcover keeps only its config deltas; its `:detekt-rules` module, `softcover-rules.yml` and the
  in-jar `config.yml` go once its rules move upstream.

### F62 — The ktlint ruleset formats; its semantic rules move out

- **Type:** gate
- **Home:** `nl.rhaydus:ktlint-rules`, `nl.rhaydus:detekt-rules`, `nl.rhaydus:konsist-rules` (F67)
- **Status:** **Open.** ktlint carries six report-only semantic rules with scoping hardcoded in the rules.
- **What to build:** delete them from ktlint, which then only formats with auto-fix. Inline mockk stub,
  `runCatchingLogged` in use cases, raw logging and inline fully-qualified references inspect function
  bodies and become detekt rules with config-driven scoping. One type per file and the visibility of
  internal plumbing are declaration rules and become Konsist assertions.

### F63 — `MultiArgWrappingRule` wraps 2+-argument `Modifier` calls too

- **Type:** bug (formatter)
- **Home:** `nl.rhaydus:ktlint-rules` (`multi-arg-wrapping`)
- **Status:** **Open.** The rule exempts `Modifier` chains while the code-style guide has no such carve-out;
  review catches `.padding(horizontal = …, vertical = …)` by hand.
- **What to build:** drop the exemption so the auto-fix wraps them, then delete the review-only line.

### F64 — Gate: `@Nested` grouping and Arrange / Act / Assert markers in tests

- **Type:** gate (Konsist assertion for the grouping; detekt rule for the markers)
- **Home:** `nl.rhaydus:konsist-rules` (F67), `nl.rhaydus:detekt-rules`
- **Status:** **Open.** Enforced by a close-out grep, four agent-prompt copies and agent memory.
- **What to build:** a Konsist assertion that every `@Test` function sits in a `@Nested inner class`, and a
  detekt rule over test sources that each `@Test` body carries the three markers in order, reported at the
  line. Then drop the grep from the close-out gates and the prose copies.

### F65 — Suppressing a contract rule is itself a finding

- **Type:** gate (detekt config)
- **Home:** the shared detekt baseline config
- **Status:** **Open.** "No `@Suppress`" is prose only; 13 suppressions exist in Softcover.
- **What to build:** enable the built-in `ForbiddenSuppress` for the contract rules (`NoReflection`,
  `ScreenConstructorProperty`, `UnguardedFlowTerminalRead`, `UnsafeCallOnNullableType`) and audit the existing
  suppressions.

### F66 — Pure-AST rules for the remaining review-only layout and naming rules

- **Type:** gate (detekt rules)
- **Home:** `nl.rhaydus:detekt-rules`
- **Status:** **Open.** Review or agent prose only: every multi-line construct is its own paragraph,
  guard-clause spacing, no `/*` inside KDoc, a `*UiModel` field never named `action`, and blank lines between
  sibling composables inside nested layout scopes.
- **What to build:** the `action` field name as a Konsist assertion (F67); the four layout rules as detekt
  rules. Then shrink the review-only list in the code-style docs to the rules that need judgement.


### F67 — A Konsist layer for declaration and architecture rules

- **Type:** gate (library + convention plugin)
- **Home:** new `nl.rhaydus:konsist-rules`, `build-logic` (F61)
- **Status:** **Open.** Declaration rules (imports, layering, naming, placement) live in detekt rules,
  ktlint rules or agent prose; detekt makes each one a visitor class, and its built-in import ban takes
  one entry.
- **What to build:** first verify Konsist on Softcover: it reads every KMP source set, parses the
  Kotlin 2.4.21 sources without errors, one real assertion (F34) passes and fails correctly, and its run
  time inside `check` is acceptable. If any of these fails, the Konsist-homed rules go to detekt instead.
  Then a published library of assertion functions taking project facts (package prefix, allowlists, bans)
  as parameters, and a `:konsist` JVM test module created by F61's plugin and wired into `check`. Its
  assertions: F34, the F57 getter, one type per file and plumbing visibility (F62), `@Nested` grouping
  (F64), the `action` field name (F66), and serializable Screen arguments (F52).

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

### F50 — Compose stability gate in the convention plugins

- **Type:** gate
- **Home:** `build-logic` Compose convention plugins (Android and KMP)
- **Status:** **Open — implemented locally.** Softcover's two Compose convention plugins apply the skydoves
  Compose Stability Analyzer through one shared helper (`includeTests = false`, `failOnStabilityChange = true`),
  and `stabilityCheck` compares each module with its checked-in `<module>/stability/*.stability` baseline in CI.
  The foundation's convention plugins should adopt it so every consumer gets the gate.
- **What to build:** apply the plugin from the shared Compose convention helper. The plugin version must track the
  Kotlin version (Softcover runs analyzer 0.9.0 on Kotlin 2.4.21). On the AGP
  KMP Android target the analyzer's per-variant tasks look for `compile<Variant>Kotlin` output and find none, so
  repoint their `stabilityInputFiles` at `stability/compileAndroidMain/stability-info.json` once the task graph is
  ready (see `StabilityGate.kt`), or fix it upstream in the analyzer.

### F52 — R8 keep-contract plugin, reflection and Screen-argument rules, and the `keepRules/` convention

- **Type:** gate
- **Home:** `build-logic` convention plugins, `nl.rhaydus:detekt-rules`, and the module convention plugins
- **Status:** **Open — implemented locally.** The `softcover.android.r8-keep-contract` plugin, the
  `NoReflection` and `ScreenConstructorProperty` detekt rules, and the `keepRules/` convention are described in
  `docs/reference/r8.md`.
- **What to build:** move the plugin and the two rules into the foundation, with the project package prefix
  and the supertypes to check as configuration (`NoReflection` as a detekt rule, `ScreenConstructorProperty`
  as a Konsist assertion, F67), and ship `TransientNavArg` with the navigation contract.
  Document `keepRules/` as the consumer-rule convention. No Softcover build file registers these directories, so the foundation's
  convention plugins only need to keep AGP's default pickup intact.
