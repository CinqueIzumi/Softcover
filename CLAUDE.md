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

The brand-agnostic design skeleton (theme/typography plumbing, layout primitives, the shared component catalog, the editorial role contract) is governed by the foundation [`docs/rhaydus/0.3.0/design-system-foundations.md`](docs/rhaydus/0.3.0/design-system-foundations.md). [docs/reference/design-system.md](docs/reference/design-system.md) is the source of truth for Softcover's brand layered on top — color roles, editorial typography values, brand components, patterns, decision rules. Consult both before designing or modifying any UI surface.

**Maintenance rule (enforced by review).** Any change that introduces, retires, or alters a foundation, component, or pattern in the design system MUST update `docs/reference/design-system.md` in the same change. The `code-reviewer` agent treats a design-system change without a corresponding doc update as a blocker. Examples that require a doc update: a new shared component under `core/presentation/component/`, a new editorial typography role, a new color role usage, a new layout pattern that other screens should adopt, retirement or renaming of any of the above. Localized tweaks to a single screen that don't change the system itself do not require an update.

## Code Style

The shared Kotlin code style is governed by the foundation [`docs/rhaydus/0.3.0/code-style.md`](docs/rhaydus/0.3.0/code-style.md) — the source of truth for naming, layout, and whitespace. [docs/reference/code-style.md](docs/reference/code-style.md) keeps only Softcover-specific deltas (the Apollo/AppLog error-handling bindings). Read both before writing or modifying Kotlin code.

The mechanical style rules are enforced by tooling, not manual vigilance — for every developer, with zero setup, via the Gradle `check` lifecycle (so CI gates on them too):

- **The foundation ktlint ruleset** (`nl.rhaydus:ktlint-rules`) **auto-fixes and gates** the mechanizable layout rules. Run `./gradlew ktlintFormat` to auto-fix, `./gradlew ktlintCheck` to gate (also run by `check`). The rules: multi-arg one-per-line wrapping (2+ args/params, even when they fit — exempting collection factories, `Modifier.…` chains, trailing-lambda calls), trailing comma on multi-line lists, blank line after `super.*()` / `AppLog.e(...)`, `// region`/`// endregion` flush, no blank line after `{` / before `}`, blank line between sibling composables, and boolean `!` → `.not()` (gate-only; fix by hand).
- **Five formerly-greppable rules are now blocking ktlint rules** in `nl.rhaydus:ktlint-rules` (gate-only, fixed by hand; gated by `ktlintCheck`): inline fully-qualified references, one-type-per-file, project-import ordering, inline mockk stubs (`coEvery`/`every` one-liners open onto their own line), and bare `runCatching` in a use case (use `runCatchingLogged`).
- **detekt is type-resolved and gates from zero** (no baseline). The config layers the shared foundation baseline (`config/detekt.yml`, bundled in `nl.rhaydus:detekt-rules`, unpacked by `extractRhaydusDetektConfig`) under Softcover's own deltas in `config/detekt/detekt.yml`. Type resolution is what lets the foundation's `rhaydus:UnguardedFlowTerminalRead` rule tell a `Flow.first()` (a crash risk) from a `Collection.first()` — it is `@RequiresTypeResolution` and **silently inert without it**. `./gradlew styleCheck` runs the per-compilation tasks (`detektAndroidMain` / `detektJvmMain` / `detektMain`) across every module; `check` runs them too. They cover `commonMain` plus the platform source sets and exclude generated code. Because they need the compile classpath, `styleCheck` compiles Android + JVM rather than merely parsing source — that cost buys a gate that actually fires. `iosMain` is not covered: detekt offers no type resolution for native targets.
- **`scripts/style-check.sh` is retired** — all six of its recipes are now blocking rules (five in ktlint, the crash-safety one in detekt). Do not reintroduce a greppable style script.

The subjective rules no tool can mechanize — blank line between sibling composables (incl. `Spacer`), paragraph spacing around multi-line constructs, an `AppLog.e(...)` log as its own paragraph, reserved fixed height for optional card rows — live in `docs/reference/code-style.md` and are caught in review.

**For substantial Kotlin changes, delegate to the `code-reviewer` agent before reporting work done.** "Substantial" = a new file, a new feature module, a change spanning multiple files, or any change touching layout/state/data flow. The reviewer audits against the full current `docs/reference/code-style.md` and catches both new violations and pre-existing ones in the touched files (per the on-touch compliance policy). Run it after the build succeeds and before the wrap-up message.

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

Clean Architecture layering, DI, navigation, and the TOAD framework are governed by the foundation [`docs/rhaydus/0.3.0/architecture.md`](docs/rhaydus/0.3.0/architecture.md) and [`docs/rhaydus/0.3.0/toad-architecture.md`](docs/rhaydus/0.3.0/toad-architecture.md) — the source of truth for the generic signatures, per-feature boilerplate, and Koin wiring. [docs/reference/architecture.md](docs/reference/architecture.md) keeps Softcover's deltas (the Apollo network layer, Room storage, the concrete module overview, app-specific TOAD notes). Consult both before adding a feature module, modifying a ScreenModel / Action / Collector, or changing data flow between layers.

The tier model (`core`/`feature`/orchestration), allowed dependency directions, and where a new type/screen/use case belongs are governed by that same foundation architecture doc; [docs/reference/module-structure.md](docs/reference/module-structure.md) keeps Softcover's concrete module roster and `softcover.*` build-setup conventions. Consult it before adding a module, deciding shared-vs-feature-local, or wiring a cross-feature dependency.

The app follows **Clean Architecture** with a custom **TOAD** state management framework. It is a multi-module Gradle build: `:app` (application shell) → `:orchestration` (nav host + cross-feature use cases) → `:feature:*` → `:core:*`.

### Quick reference

The detail lives in the two docs above; this is just the orientation.

- **Layers (per feature):** `domain/` (repository interfaces + use cases, depends on nothing) → `data/` (impls, data sources, mappers — Room entities/DAOs live in `:core:database`, not the feature) → `presentation/` (screens, ScreenModels, actions, events, state; depends on domain only) → `di/` (Koin module). A feature never imports a sibling feature.
- **TOAD** (custom framework on Voyager's `ScreenModel`): each screen has `UiState` (immutable, exposed as `StateFlow`), `UiAction` (sealed; one per interaction), `UiEvent` (one-time via `Channel`), `LocalVariables`, `ActionDependencies`, and per-feature `*Collector` interfaces in `flows/` (implementing the foundation `Collector`). Flow: `UiAction.execute() → use cases via Dependencies → setState() → StateFlow → recompose`.
- **Always:** Apollo via `safeQuery()` / `safeMutation()` (queries in `core/network/src/commonMain/graphql/`); Room + migrations in `:core:database`; DataStore for preferences; Koin DI; Voyager nav (`Navigator`, `TabNavigator`); `AppDispatchers` for Main/IO/Default; `Result<T>` with `.onSuccess()` / `.onFailure()`; `AppLog` (Kermit-backed) for logging (never `println` / `Log.*`).
- **Naming:** domain models are plain nouns (`Book`, `Author`); suffixes mark role — `*Entity`, `*DataSource(Impl)`, `*Repository(Impl)`, `*UseCase`, `*Screen`, `*ScreenModel`, `*Action`, `*Event`, `*UiState`, `*LocalVariables`, `*Dependencies`, `*Collector` (per-feature flow collector).

## Dependency Management

All versions are centralized in `gradle/libs.versions.toml`. Reference via version catalog (`libs.<alias>`) in `build.gradle.kts`.

## Roadmap

Planning is layered. **Internal** (engineering source of truth, in `docs/working/`):
- [docs/working/idea-catalogue.md](docs/working/idea-catalogue.md) — the idea catalogue (the *what*), tagged (`B.4.1`).
- [docs/working/roadmap-steps.md](docs/working/roadmap-steps.md) — the sequenced pickup order (the *order*), scoped S/M/L. When a step is finished, **delete it from the file in the same commit** — do not renumber the remaining steps (gaps are intentional so references in commits and docs stay valid). The deletion is part of the step, not a follow-up.
- [docs/working/release-plan.md](docs/working/release-plan.md) — steps bundled into versioned drops (the *when*), each with a user-facing release-note blurb.
- [docs/working/now.md](docs/working/now.md) — the **day-to-day working surface**: the 1–2 topics in active focus (pointers into the steps, never forks) and a flat fast-track-fixes backlog for small things done outside the release cadence. Fixes are deleted when shipped and folded into the next release's notes rather than listed on the public roadmap.

**Public** (user-facing): [ROADMAP.md](ROADMAP.md) at the repo root is a curated, version-labelled projection of `release-plan.md` — plain language, no internal tags/scope/dependencies/"won't do" list. It is **derived, not authored**: app users read it on GitHub, and the in-app Roadmap screen (Step 8.12 / D.11) fetches the same raw file at runtime so the two never drift.

**Maintenance rule (enforced by review).** `ROADMAP.md` is a projection of `release-plan.md`, kept in lockstep. Any change that reorders, cuts, adds, or reshapes a release in `release-plan.md` MUST update the corresponding section of `ROADMAP.md` in the same change. Never add user-facing content to `ROADMAP.md` that isn't backed by a release in `release-plan.md`.

<!-- rhaydus:start -->
## Rhaydus foundation (managed by rhaydus-adopt - do not hand-edit)

This project builds on the **nl.rhaydus foundation** (v0.3.0, resolved locally via `foundation.local=true` in `local.properties` → `includeBuild("../rhaydus-foundation")`; falls back to Maven Central when unset). Capabilities index (what's available, so reuse rather than reinvent): [`docs/rhaydus/0.3.0/CAPABILITIES.md`](docs/rhaydus/0.3.0/CAPABILITIES.md).

- **Foundation libraries consumed (0.3.0):** `nl.rhaydus:toad`, `core-common` (formerly `core-ui`, split 0.2.0→0.3.0 into `core-common`/`core-platform`), `designsystem-core`, `designsystem-editorial`, `designsystem-image`, `ktlint-rules`.
- **Catalog entries available, not yet wired into any module** (`gradle/libs.versions.toml`, for future adoption batches): `nl.rhaydus:core-platform`, `offline-sync`, `detekt-rules`.
- **Foundation conventions docs** (vendored, version-pinned at [`docs/rhaydus/0.3.0/`](docs/rhaydus/0.3.0)): architecture, toad-architecture, code-style, design-system-foundations, CAPABILITIES. These are the source of truth for the shared layering, TOAD pattern, code style, and design system; this app keeps only its own deltas (brand tokens, Apollo/Room, platform set).
- **This app's design system (brand):** [`docs/reference/design-system.md`](docs/reference/design-system.md).

**How to develop here:**
- New feature / screen **logic** (state, actions, use cases, data) → the **rhaydus-logic** agent.
- New feature / screen **UI** (Compose render, design system) → the **rhaydus-ui** agent (it reads the foundation design system + `docs/reference/design-system.md`).
- A logic-only or UI-only change uses just that one agent; a full new screen goes logic → UI.
- Review → **code-reviewer**. Tests → **unit-test-writer**. Style gates → the **style-check** skill.
- **Reuse-first:** check the capabilities index before hand-rolling a component, modifier, or util.

_Re-run the rhaydus-adopt agent after changing any `nl.rhaydus` dependency or version._
<!-- rhaydus:end -->

