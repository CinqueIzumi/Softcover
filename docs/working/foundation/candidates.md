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

### F34 — Import ban with a per-file allowlist (text-leaf gate)

- **Type:** gate (Konsist assertion)
- **Home:** `nl.rhaydus:konsist-rules` (F67)
- **Status:** **Open — blocks Softcover's text-leaf gate.** Softcover routes every text through one leaf
  (`SoftcoverText`) and needs `androidx.compose.material3.Text` banned everywhere except that leaf's file.
  detekt's `ForbiddenImport` allows one instance per config and is already scoped to the leaf modules.
- **What to build:** an assertion taking `(import, allowed file globs, reason)` entries, also matching
  fully-qualified references; Softcover enables it with `androidx.compose.material3.Text` allowed only in
  `SoftcoverText.kt`, after swapping the six files that still use Material `Text`. The same assertion takes the prose-only
  import rules: Material `Button`, `kotlinx.datetime.Instant` (use `kotlin.time.Instant`) and
  `rememberHaptics`, which retires Softcover's `RememberHapticsOutsideTheme`. A layering assertion alongside
  it: data and domain packages never import presentation.

### F39 — No gate keeps raw dp literals out of spacing calls

- **Type:** gate (lint rule)
- **Home:** `nl.rhaydus:detekt-rules`
- **Status:** **Open.** Softcover's app-local `RawSpacingDp` reports a dp literal passed to `padding`,
  `PaddingValues`, `contentPadding`, `spacedBy`, `offset` or a `Spacer` size.
- **Impact in the app:** without it, spacing drifts back to per-screen numbers after the token sweep.
- **What to build:** the rule in the shared ruleset, with the allowed literals configurable.

### F41 — No gate keeps raw radii out of corner shapes

- **Type:** gate (lint rule)
- **Home:** `nl.rhaydus:detekt-rules`
- **Status:** **Open.** Softcover's app-local `RawCornerDp` reports a dp literal or `percent` argument passed to
  `RoundedCornerShape` or `CutCornerShape`.
- **Impact in the app:** without it, corner radii drift back to per-screen numbers after the token sweep.
- **What to build:** the rule in the shared ruleset.

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
- **Status:** **Open: the module is done, the library is not.** Konsist 0.18.1 is verified on Softcover, and
  `rhaydus.konsist` makes a JVM test module that `check` reaches. `scopeFromProject()` includes `foundation/`,
  so every assertion filters it out.
- **What to build:** a published library of assertion functions that take project facts (package prefix,
  allowlists, bans) as parameters. Its assertions: F34, the F57 getter, one type per file and plumbing
  visibility (F62), `@Nested` grouping (F64), the `action` field name (F66), and serializable Screen
  arguments (F52).

### F52 — Screen-argument rule as a Konsist assertion

- **Type:** gate
- **Home:** `nl.rhaydus:konsist-rules` (F67)
- **Status:** **Open — implemented locally.** The R8 keep-contract plugin, `NoReflection`, `TransientNavArg`
  and the `keepRules/` convention are in the foundation; Softcover's `ScreenConstructorProperty` detekt rule is not.
- **What to build:** `ScreenConstructorProperty` as a Konsist assertion (a `Screen` constructor property is
  serializable or `@TransientNavArg`), with the supertypes as parameters. Then delete the Softcover rule.
