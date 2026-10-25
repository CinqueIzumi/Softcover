---
name: feedback_app_module_testdebug_source_set
description: :app has no unit-test source set or JUnit5 wiring by default — add both before testing a debug-only TOAD ScreenModel
metadata:
  type: feedback
---

`:app` uses the plain `android.application` + `kotlin.android` plugins, not the `softcover.kmp.library`
convention plugin that wires JUnit5/MockK/Kotest/coroutines-test onto `androidHostTest` for `:core:*` /
`:feature:*` modules (see [[project_test_conventions]]). The first unit test for anything under
`app/src/debug/` (e.g. a debug-only TOAD `ScreenModel`) needs, added by hand in `app/build.gradle.kts`:

- A `testDebug` source set at `app/src/testDebug/java/...` (mirrors the `debug/java/...` package layout;
  AGP's `testDebug` compile classpath already includes `debugImplementation` deps transitively, so
  debug-only production classes are visible with no extra wiring).
- `testDebugImplementation`/`testDebugRuntimeOnly` for `libs.junit.api`, `libs.junit.params`,
  `libs.junit.engine`, `libs.junit.platform.launcher`, `libs.mockk`, `libs.kotest`, `libs.coroutines.test`
  — the same aliases `KmpLibraryConventionPlugin` wires for `androidHostTest`.
- `tasks.withType<Test> { useJUnitPlatform() }` at the bottom of the file — AGP defaults `Test` tasks to
  JUnit4, so JUnit5 `@Test`/`@Nested` annotations silently no-op (0 tests run, still `BUILD SUCCESSFUL`)
  without this.
- The Gradle task is `:app:testDebugUnitTest`, not `:app:testAndroidHostTest` (that task name only exists
  on KMP library modules).

**Why:** the user's brief for this module explicitly asked for a debug ScreenModel test; the module's
Gradle wiring had never needed a test source set before, unlike every `:core:*`/`:feature:*` module.

**How to apply:** before writing a test under `app/src/debug/**`, check `app/build.gradle.kts` for a
`testDebugImplementation` block; add it (and `useJUnitPlatform()`) if missing, rather than assuming the
convention plugin already covers it.

Separately: a TOAD `ScreenModel` built with `initializers = emptyList()` and no `startInitializers()` call
in its body has no dynamic behaviour at all — state is exactly the constructor's default `UiState`. Test it
the way `SettingsScreenScreenModelTest`'s `InitialState` nested class does: `Dispatchers.setMain`/
`resetMain` in `@BeforeEach`/`@AfterEach`, construct the real `ScreenModel` with a test `AppDispatchers`,
and assert `screenModel.state.value` field-by-field (plus one assertion against the bare default instance).
