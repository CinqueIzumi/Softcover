---
name: app-module-lacks-toad-voyager-koin
description: :app's debugImplementation graph has no toad/voyager-koin until a debug screen first needs a ScreenModel; add both when giving a debug screen a TOAD ScreenModel
metadata:
  type: feedback
---

`:app/build.gradle.kts` debug screens (`app/src/debug/`) historically had no `UiState`/`ScreenModel` —
only `voyager-navigator` was on `debugImplementation`, not `libs.rhaydus.toad` or `libs.voyager.koin`.
Giving one of these screens a TOAD `ScreenModel` (per [[normative-rules-are-not-negotiable]] full-TOAD
consistency) fails `compileDebugKotlin` with "Unresolved reference 'toad'" / `'koin'` /
`'koinScreenModel'` / `'screenModelScope'` until both are added to `debugImplementation`.

**Why:** `:app` cannot depend on `:feature:*` (tier rule), so these debug screens hand-roll TOAD types
locally instead of reusing a feature's; the module's dependency graph was never grown for it.

**How to apply:** when wiring a `ScreenModel`/`UiState` into an `app/src/debug` screen, check
`app/build.gradle.kts`'s `debugImplementation` block first — add `libs.rhaydus.toad` and
`libs.voyager.koin` alongside the existing `libs.voyager.navigator` before writing the Kotlin. Also
update the block's "Only these N are declared" comment count (flag it if you can't touch comments
yourself).

Separately: the detekt task name for `:app` is `detektDebug`/`detektRelease` (Android Gradle plugin
per-variant naming), not `detektAndroidDebug` — the latter is the AGP-provided one for KMP library
modules with an `androidMain` source set. Run `./gradlew :app:tasks --all` (via `gradle-quiet.sh`, which
still prints the full log path even on `BUILD SUCCESSFUL` for a bare `tasks` invocation) if a guessed
detekt task name 404s.
