---
name: project_component_library_migration_s1
description: Multi-stage component-library migration (branch component-library-migration) — S1 gate review findings, to check again at S2+
metadata:
  type: project
---

Tracked in `docs/working/component-library-migration.md` (delete-on-completion working doc, NOT a
roadmap doc). One branch, one PR, staged commits S1–S11. Work happens as **uncommitted changes** on
`component-library-migration` — `HEAD` stayed equal to `release/3.2.0` through S1, so
`git diff release/3.2.0...HEAD` was empty; the real diff was the working tree (`git diff` / `git status`).
Check this again each stage — don't assume the branch has real commits yet.

**S1 (module scaffolding + G1 gate) reviewed 2026-08-21.** New empty modules `:core:component`,
`:core:uibinding`, `:core:presentation`; `checkModuleGraph` extended with a component-library ban list
(`componentLibraryAllowedProjects`, `componentLibraryBannedGroups`, `conventionProvidedCoordinates`).

Open items carried forward, re-check as the relevant stage lands:

1. **Koin service-locator hole is real, not closed.** `conventionProvidedCoordinates` skips
   `io.insert-koin:koin-core` (injected into every KMP module's commonMain by
   `KmpLibraryConventionPlugin`). Verified live: explicitly declaring
   `implementation(libs.koin.core)` in `:core:component` still passes `checkModuleGraph` with zero
   violations — `koin-core` alone is enough to do `GlobalContext.get()` / `KoinComponent` service-locator
   DI, entirely outside the banned-group check (which only catches `koin-compose`/`koin-android`/
   `koin-androidx-compose`). The build-script comment's "an unused koin-core on the classpath is inert"
   is true only if nobody writes code against it — Gradle-graph gating can't see usage. No
   `ForbiddenImport`-style detekt rule exists today to close it (checked `config/detekt/detekt.yml`).
   Not a blocker at S1 (no source files in `:core:component` yet to violate it), but **re-check at S2/S5**
   once real component source lands — flag any `KoinComponent`/`GlobalContext` usage in
   `:core:component` on sight, since the gate will not catch it.
2. **Test-config asymmetry in the banned-groups check, undocumented.** The pre-existing api-edge check
   in the same task explicitly excludes test configs (`configuration.name.contains("test").not()`) with
   a comment explaining why (an api edge can't leak to consumers via test configs). The new
   `componentLibraryBannedGroups` check has no such exclusion and no comment explaining the divergence.
   Confirmed no current convention-plugin-injected test dependency triggers a false positive
   (`KmpLibraryConventionPlugin`'s test source sets only add kotest/turbine/coroutines-test/mockk/junit5
   — none of the banned groups). Low risk today (per §6 "Test posture," Compose UI tests are explicitly
   out of scope for this migration), but worth a one-line comment if this file is touched again.
3. `docs/reference/module-structure.md`'s "T0 core" roster line and "What each core:* module holds"
   table don't mention the three new modules yet — drifted from `settings.gradle.kts` starting at S1
   rather than waiting for S3/S4 content moves. Not a CLAUDE.md G5 blocker (that rule is scoped to
   `docs/reference/design-system.md`), just a cheap doc-sync nit.
4. `checkModuleGraph`'s task `description` string still only describes the tier-DAG check, predates
   both the api-visibility rule and this new component-library gate.

Verified clean at S1: `checkModuleGraph` (231 edges, correctly rejects a deliberately introduced
`:core:domain` + `voyager-navigator` + `koin-compose` violation), `ktlintCheck`, and `projectHealth` on
all three new empty modules (confirms §5b finding 2 — zero declared project deps is required because
`onUnusedDependencies` is `severity("fail")`).

See also [[project_rhaydus_foundation_upstream_migration]] for the sibling-repo foundation pattern this
migration is unrelated to (this is app-local, not a foundation upstream port).
