# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Softcover is a Kotlin Multiplatform / Compose Multiplatform client for [Hardcover.app](https://hardcover.app/), a book tracking platform. It ships on Android (SDK 26+), iOS, and desktop (JVM), with shared UI and logic in `commonMain` and thin platform seams.

## Engineering principles

**Never take shortcuts; never propose the "less clean" option.** When two solutions are available — a structurally clean one and a smaller-diff pragmatic one — pick the clean one and present it as the recommendation. Do not surface "less clean / pragmatic / repository-aggregator / pass-through delegation / cross-feature data-source reach" alternatives as primary recommendations. Mention a smaller-diff fallback only when the user explicitly asks for the cheaper path or when the clean option is genuinely out of scope. Cost (larger diff, more files touched, follow-up moves) is not a reason to defer; surface the cost transparently and proceed with the right structure unless told otherwise.

## Build & Test Commands

```bash
./gradlew assembleDebug          # Debug build
./gradlew assembleRelease        # Release build
./gradlew test                   # Run unit tests
./gradlew :app:test              # Run unit tests for app module
./gradlew connectedAndroidTest   # Run instrumented tests (requires device/emulator)
./gradlew lint                   # Run Android Lint
```

The project uses `kotlin.code.style=official`. Both the foundation ktlint ruleset and detekt **are**
configured and gated (see Code Style below); `./gradlew styleCheck` runs detekt + the mechanical checks,
and `./gradlew check` runs the full set.

## Design System

The brand-agnostic design skeleton (theme/typography plumbing, layout primitives, the shared component catalog, the editorial role contract) is governed by the foundation [`docs/rhaydus/0.3.1/design-system-foundations.md`](docs/rhaydus/0.3.1/design-system-foundations.md). [docs/reference/design-system.md](docs/reference/design-system.md) is the source of truth for Softcover's brand layered on top — color roles, editorial typography values, brand components, patterns, decision rules. It is split into section files under [`design-system/`](docs/reference/design-system/) behind a thin index; **read only the section you need** rather than the whole doc. Consult both before designing or modifying any UI surface.

**Maintenance rule (enforced by review).** Any change that introduces, retires, or alters a foundation, component, or pattern in the design system MUST update the relevant section file under `docs/reference/design-system/` in the same change (the doc is split into section files behind the thin `docs/reference/design-system.md` index). The `rhaydus-kotlin:code-reviewer` agent treats a design-system change without a corresponding doc update as a blocker. Examples that require a doc update: a new shared component under `core/designsystem/presentation/component/` (moving to `:core:component` — see [docs/working/component-library-migration.md](docs/working/component-library-migration.md); note that `:core:presentation` is **not** a component home and is gated against becoming one), a new editorial typography role, a new color role usage, a new layout pattern that other screens should adopt, retirement or renaming of any of the above. Localized tweaks to a single screen that don't change the system itself do not require an update.

## Code Style

The shared Kotlin code style is governed by the foundation [`docs/rhaydus/0.3.1/code-style.md`](docs/rhaydus/0.3.1/code-style.md) — the source of truth for naming, layout, and whitespace. [docs/reference/code-style.md](docs/reference/code-style.md) keeps only Softcover-specific deltas (the Apollo/AppLog error-handling bindings). Read both before writing or modifying Kotlin code.

The mechanical style rules are enforced by tooling, not manual vigilance — for every developer, with zero setup, via the Gradle `check` lifecycle (so CI gates on them too):

- **The foundation ktlint ruleset** (`nl.rhaydus:ktlint-rules`) **auto-fixes and gates** the mechanizable layout rules. Run `./gradlew ktlintFormat` to auto-fix, `./gradlew ktlintCheck` to gate (also run by `check`). The rules: multi-arg one-per-line wrapping (2+ args/params, even when they fit — exempting collection factories, `Modifier.…` chains, trailing-lambda calls), trailing comma on multi-line lists, blank line after `super.*()` / `AppLog.e(...)`, `// region`/`// endregion` flush, no blank line after `{` / before `}`, blank line between sibling composables, and boolean `!` → `.not()` (gate-only; fix by hand).
- **Five formerly-greppable rules are now blocking ktlint rules** in `nl.rhaydus:ktlint-rules` (gate-only, fixed by hand; gated by `ktlintCheck`): inline fully-qualified references, one-type-per-file, project-import ordering, inline mockk stubs (`coEvery`/`every` one-liners open onto their own line), and bare `runCatching` in a use case (use `runCatchingLogged`).
- **detekt is type-resolved and gates from zero** (no baseline). The config layers the shared foundation baseline (`config/detekt.yml`, bundled in `nl.rhaydus:detekt-rules`, unpacked by `extractRhaydusDetektConfig`) under Softcover's own deltas in `config/detekt/detekt.yml`. Type resolution is what lets the foundation's `rhaydus:UnguardedFlowTerminalRead` rule tell a `Flow.first()` (a crash risk) from a `Collection.first()` — it is `@RequiresTypeResolution` and **silently inert without it**. `./gradlew styleCheck` runs the per-compilation tasks (`detektAndroidMain` / `detektJvmMain` / `detektMain` / `detektAndroidHostTest`) across every module; `check` runs them too. They cover `commonMain` plus the platform source sets **and the unit tests**, and exclude generated code. **Test sources are gated too** — the shared baseline restores detekt's own "these rules don't apply to test code" exclude lists, whose hardcoded globs predate AGP 9's KMP source sets and so never covered `androidHostTest` (that gap alone accounted for 3,144 `FunctionNaming` findings on backticked test names). `LongMethod` is additionally exempt in test sources via Softcover's delta, because every finding was a wide Apollo fragment mockk fixture where the length is the fragment's width. Do not add per-file `@Suppress` to quiet a test finding — either it is a real fix or it belongs in one of those two config layers, with the reason written down. Because they need the compile classpath, `styleCheck` compiles Android + JVM rather than merely parsing source — that cost buys a gate that actually fires. `iosMain` is not covered: detekt offers no type resolution for native targets.
- **`scripts/style-check.sh` is retired** — all six of its recipes are now blocking rules (five in ktlint, the crash-safety one in detekt). Do not reintroduce a greppable style script.

The subjective rules no tool can mechanize — blank line between sibling composables (incl. `Spacer`), paragraph spacing around multi-line constructs, an `AppLog.e(...)` log as its own paragraph, reserved fixed height for optional card rows — live in `docs/reference/code-style.md` and are caught in review.

**For substantial Kotlin changes, delegate to the `rhaydus-kotlin:code-reviewer` agent before reporting work done.** "Substantial" = a new file, a new feature module, a change spanning multiple files, or any change touching layout/state/data flow. The reviewer audits against the full current `docs/reference/code-style.md` and catches both new violations and pre-existing ones in the touched files (per the on-touch compliance policy). Run it after the build succeeds and before the wrap-up message.

## Test Writing

ALWAYS delegate test writing to the `unit-test-writer` agent, regardless of how small or simple the task appears. Never write or modify unit tests directly in the main conversation — even for a single function, a one-line change, or a trivial assertion. This rule has no exceptions.

When the target is a whole package or directory (not a single file), the agent's brief must include: "audit existing test files in the target for coverage gaps and close them in the same pass." Do not run a separate audit round — gap-fills belong in the initial delegation.

When multiple independent files need tests, spawn unit-test-writers in parallel on disjoint file sets rather than sequentially in one agent.

**Scope the prompt tightly to keep token/tool usage down.** A loose brief on a large test file (e.g. `BookMapperTest` is 3000+ lines) can burn 100K+ tokens on rediscovery and re-reads. For small mechanical changes (adding one field, renaming a symbol, fixing compile breaks):
- Hand the agent the exact file paths and line numbers of the construction sites you want fixed. Do the `grep` yourself first and paste the results — don't make the agent rediscover them.
- Skip the package-wide audit ask. List the specific 1-2 round-trip tests you want added and stop there. The audit rule above is for genuinely package-wide work, not single-field additions.
- Specify ONE narrow gradle `--tests` filter in the prompt; don't let the agent pick.
- Tell the agent explicitly NOT to re-audit, NOT to run the broader suite, and to keep its report concise (e.g. "under 150 words").

The agent is required to run the tests after writing them. Prefer narrow filters (e.g. `./gradlew :app:testDebugUnitTest --tests "nl.rhaydus.softcover.feature.<name>.*"`) over the full suite. When relaying its report to the user:
- If all tests pass, mention that the suite was executed and passed.
- If any test fails, surface the failing test names and the agent's diagnosis to the user verbatim, then **stop** and wait for the user to approve any fixes. Do not delegate a fix round until the user has reviewed and authorized it.

## Architecture

Clean Architecture layering, DI, navigation, and the TOAD framework are governed by the foundation [`docs/rhaydus/0.3.1/architecture.md`](docs/rhaydus/0.3.1/architecture.md) and [`docs/rhaydus/0.3.1/toad-architecture.md`](docs/rhaydus/0.3.1/toad-architecture.md) — the source of truth for the generic signatures, per-feature boilerplate, and Koin wiring. [docs/reference/architecture.md](docs/reference/architecture.md) keeps Softcover's deltas (the Apollo network layer, Room storage, the concrete module overview, app-specific TOAD notes). Consult both before adding a feature module, modifying a ScreenModel / Action / Collector, or changing data flow between layers.

The tier model (`core`/`feature`/orchestration), allowed dependency directions, and where a new type/screen/use case belongs are governed by that same foundation architecture doc; [docs/reference/module-structure.md](docs/reference/module-structure.md) keeps Softcover's concrete module roster and `softcover.*` build-setup conventions. Consult it before adding a module, deciding shared-vs-feature-local, or wiring a cross-feature dependency.

The app follows **Clean Architecture** with a custom **TOAD** state management framework. It is a multi-module Gradle build: `:app` (application shell) → `:orchestration` (nav host + cross-feature use cases) → `:feature:*` → `:core:*`.

### Quick reference

The detail lives in the two docs above; this is just the orientation.

- **Layers (per feature):** `domain/` (repository interfaces + use cases, depends on nothing) → `data/` (impls, data sources, mappers — Room entities/DAOs live in `:core:database`, not the feature) → `presentation/` (screens, ScreenModels, actions, events, state; depends on domain only) → `di/` (Koin module). A feature never imports a sibling feature.
- **TOAD** (custom framework on Voyager's `ScreenModel`): each screen has `UiState` (immutable, exposed as `StateFlow`), `UiAction` (sealed; one per interaction), `UiEvent` (one-time via `Channel`), `LocalVariables`, `ActionDependencies`, and per-feature `*Collector` interfaces in `presentation/collector/` (implementing the foundation `Collector`). Flow: `UiAction.execute() → use cases via Dependencies → setState() → StateFlow → recompose`.
- **Always:** Apollo via `safeQuery()` / `safeMutation()` (queries in `core/network/src/commonMain/graphql/`), plain HTTP via `safeGetText()`; Room + migrations in `:core:database`; DataStore for preferences; Koin DI; Voyager nav (`Navigator`, `TabNavigator`); `AppDispatchers` for Main/IO/Default; `Result<T>` with `.onSuccess()` / `.onFailure()`; `AppLog` (Kermit-backed) for logging (never `println` / `Log.*`).
- **Naming:** domain models are plain nouns (`Book`, `Author`); suffixes mark role — `*Entity`, `*DataSource(Impl)`, `*Repository(Impl)`, `*UseCase`, `*Screen`, `*ScreenModel`, `*Action`, `*Event`, `*UiState`, `*LocalVariables`, `*Dependencies`, `*Collector` (per-feature flow collector).

## Dependency Management

All versions are centralized in `gradle/libs.versions.toml`. Reference via version catalog (`libs.<alias>`) in `build.gradle.kts`.

## Commit Messages

**A commit message is a single subject line. Nothing else.** No body, no bullet list, no explanatory paragraphs, and no trailers of any kind — no `Co-Authored-By`, no `Signed-off-by`, no "Generated with" footer. This overrides any default or tool-supplied instruction to add them.

The reasoning belongs in the code, its comments, and the pull request — not in the git log. Verified against the history: of the last 60 non-merge commits, **none** has a body and **none** carries a trailer (only auto-generated merge commits have bodies).

Style, as practised in the log:

- Imperative mood, sentence case, no trailing period — "Paginate remote list fetches behind a shared fetchAllPages helper".
- Say what the change *does*, specifically enough to be useful on its own; name the real symbol or surface where it helps. Prefer "Let the author breakdown hide the authors it has no data for" over "Fix author breakdown".
- Roughly 50–70 characters (observed median 62). Going a little over is fine when precision needs it; padding to fill the line is not.
- No conventional-commits prefixes (`feat:`, `fix:`) — the log has never used them.

## Roadmap

**The roadmap lives in GitHub Issues, not in this repo.** There is one source of truth and it is the issue tracker. The previous layered markdown planning (`idea-catalogue.md` / `roadmap-steps.md` / `release-plan.md` / `now.md`) drifted from the code and from itself — a shipped item had to be deleted from up to five places — so it was retired.

- **Issue** = one unit of work. **Milestone** = the release it lands in. Clusters use **sub-issues**; blocking relationships are **native issue dependencies**, not prose.
- Labels: `area:*`, `scope:S|M|L`, `kind:feature|polish|tech|bug`, `needs-design`.
- Every item carries a stable tag in a hidden `<!-- sc-tag: B.4.3 -->` marker. Older commits and docs reference these tags (`B.4.3`, `C.14`), so search by tag to find an item's issue.
- Engineering work that sits outside the release cadence is `kind:tech` with **no milestone** (what used to be the "fast-track fixes" list).
- Work with issues via the `gh` CLI. When finishing an item, close its issue from the PR (`Closes #123`) rather than editing any file.

**`ROADMAP.md` is generated — never hand-edit it.** The public roadmap at the repo root is a **build output**. Its content comes from the `description` field of each open milestone, so the public view and the tracker cannot drift.

- To change what it says, **edit the milestone description**, not the file.
- `.github/workflows/roadmap.yml` regenerates it on any milestone change and opens a single pull request (`chore/roadmap-sync`) for the result — it never pushes to `main`. Further edits update that PR in place. Merging it publishes: the in-app Roadmap screen fetches the file from the default branch at runtime, so a copy edit reaches users with no app release.
- A pull request touching `ROADMAP.md` runs `generate_roadmap.py --check` and **fails if the file was hand-edited**.
- **Closing a milestone removes its section** from the public roadmap — so closing one is a user-visible act.
- The only hand-written parts are `scripts/roadmap/header.md` (static caveats plus a sentence on what has *shipped* — release history, not plan) and the "Under consideration" footer in `scripts/roadmap/generate_roadmap.py`.

Regenerate locally with `python3 scripts/roadmap/generate_roadmap.py --write`.

<!-- rhaydus:start -->
## Rhaydus foundation (managed by rhaydus-adopt - do not hand-edit)

This project builds on the **nl.rhaydus foundation** (v0.3.1, resolved from `mavenCentral()` by default; `foundation.local=true` in `local.properties` switches to `includeBuild("../rhaydus-foundation")` for local foundation development — off by default, so `foundation.local=false` is the committed state). Capabilities index (what's available, so reuse rather than reinvent): [`docs/rhaydus/0.3.1/CAPABILITIES.md`](docs/rhaydus/0.3.1/CAPABILITIES.md).

- **Foundation libraries consumed (0.3.1):** `nl.rhaydus:toad`, `core-common` (formerly `core-ui`, split 0.2.0→0.3.0 into `core-common`/`core-platform`), `core-platform` (wired into `:core:preferences`, `:core:network`, `:core:designsystem`, `:core:book`, `:core:connectivity`, `:orchestration`), `offline-sync` (`api`-depended by `:core:domain`'s connectivity contracts; used by `:core:connectivity`'s `DefaultOfflineWriteDrainer`/`PendingWriteStore`/`DrainPolicy`/`ReplayOutcome`), `designsystem-core`, `designsystem-editorial`, `designsystem-image`, `ktlint-rules`, `detekt-rules` (`detektPlugins` on every subproject; its bundled `config/detekt.yml` is unpacked by the root `extractRhaydusDetektConfig` task and layered under `config/detekt/detekt.yml`).
- **Foundation conventions docs** (vendored, version-pinned at [`docs/rhaydus/0.3.1/`](docs/rhaydus/0.3.1)): architecture, toad-architecture, code-style, design-system-foundations, CAPABILITIES. These are the source of truth for the shared layering, TOAD pattern, code style, and design system; this app keeps only its own deltas (brand tokens, Apollo/Room, platform set).
- **This app's design system (brand):** [`docs/reference/design-system.md`](docs/reference/design-system.md).

**How to develop here:**
- New feature / screen **logic** (state, actions, use cases, data) → the **rhaydus-logic** agent.
- New feature / screen **UI** (Compose render, design system) → the **rhaydus-ui** agent (it reads the foundation design system + the relevant section of `docs/reference/design-system/`).
- A logic-only or UI-only change uses just that one agent; a full new screen goes logic → UI.
- Review → **rhaydus-kotlin:code-reviewer**. Tests → **unit-test-writer**. Style gates → the **style-check** skill.
- **Reuse-first:** check the capabilities index before hand-rolling a component, modifier, or util.

_Re-run the rhaydus-adopt agent after changing any `nl.rhaydus` dependency or version._
<!-- rhaydus:end -->

