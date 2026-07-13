---
name: project-softcover-foundation-0.3.0-local
description: Softcover switched from published nl.rhaydus 0.2.0 to local nl.rhaydus 0.3.0 via foundation.local includeBuild
metadata:
  type: project
---

As of 2026-07-03, Softcover consumes the `nl.rhaydus` foundation from the local sibling checkout
`../rhaydus-foundation` (branch `release/0.3.0`, `foundation.version=0.3.0`) via `foundation.local=true`
in `local.properties` (gitignored, so each dev sets it individually). `settings.gradle.kts` already had
the `includeBuild("../rhaydus-foundation")` switch from the original 0.2.0 adoption; this pass only
flipped the flag and updated coordinates/imports, no switch-mechanism changes.

**Why:** the foundation split its old `core-ui` module into `core-common` (non-visual seams —
`AppDispatchers`, date/number formatters, package `nl.rhaydus.common`) + `core-platform`, and added two
new modules Softcover doesn't consume yet: `offline-sync`, `detekt-rules`.

**How to apply:** `gradle/libs.versions.toml` now has `rhaydus-coreCommon` (replaces the retired
`rhaydus-coreUi` alias), plus unused-for-now catalog entries `rhaydus-corePlatform`, `rhaydus-offlineSync`,
`rhaydus-detektRules` for future adoption batches — do not remove these as "unused," they are pre-wired
for later steps. All Kotlin imports of `nl.rhaydus.ui.common.*` were mechanically renamed to
`nl.rhaydus.common.*` (pure package move, same symbols). Docs are vendored version-pinned at
`docs/rhaydus/0.3.0/` (the old `docs/rhaydus/0.2.0/` was deleted, fully superseded). The managed CLAUDE.md
block and README.md foundation table were refreshed to the 0.3.0/local-build framing.

This was a **wiring-only** pass — see [[feedback-rhaydus-adopt-scope]]: the project's
`docs/working/foundation-upstream-candidates.md` separately tracks F4/F5/F6 (result-helpers landing in
`core-common`) as "implemented, not adopted" — adopting those (deleting the app-local `:core:domain`
`RunCatchingCancellable.kt`/`RunCatchingLogged.kt` forks and re-pointing imports) is explicitly a
**separate, later** step, not part of a foundation-version wiring refresh.
