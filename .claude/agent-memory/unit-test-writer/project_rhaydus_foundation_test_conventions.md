---
name: rhaydus-foundation Test Conventions
description: Testing framework, style rules, and gradle task for the rhaydus-foundation library repo (separate repo from Softcover, at ~/Documents/projects/rhaydus-foundation)
type: project
---

The `rhaydus-foundation` repo (KMP library, groupId `nl.rhaydus`, modules like `core-ui`, `designsystem-core`, `offline-sync`)
uses the same idiom as [[project_test_conventions]] (Softcover): JUnit 5 (`org.junit.jupiter.api.Test`, `@Nested inner class`
named after the function/property under test), kotest `io.kotest.matchers.shouldBe`, kotlinx-coroutines-test `runTest`. AAA
markers (`// ----- Arrange -----` / `// ----- Act -----` / `// ----- Assert -----`) are also used here.

**`commonTest` now exists (as of `release/0.3.0`), but only carries kotest + coroutines-test + turbine — no test-annotation
framework.** `KmpLibraryConventionPlugin` (`build-logic/src/main/kotlin/KmpLibraryConventionPlugin.kt`) wires
`kotest-assertions-core` + `kotlinx-coroutines-test` + `turbine` into `commonTest`, but JUnit5 (`junit-jupiter-api/engine`) and
MockK are wired **only** into `androidHostTest` (`mobileTest.dependsOn(commonTest)`, `androidHostTest.dependsOn(mobileTest)`).
`kotlin("test")` is **not** declared anywhere, so `kotlin.test.Test` does not resolve either. Net effect: **no annotation
resolves across every commonTest-consuming target simultaneously** without a build.gradle.kts change.

**Confirmed consequence:** a `commonTest` file using `org.junit.jupiter.api.Test`/`@Nested` compiles and runs fine via
`:<module>:testAndroidHostTest` (which inherits commonTest's sources plus androidHostTest's own JUnit5 dep), but **fails to
compile** for `:<module>:jvmTest` and `:<module>:iosSimulatorArm64Test` (JUnit5 absent from those classpaths) — and since
`:<module>:check` depends on all three (`allTests`/`testAndroid` fan out to `jvmTest` + `iosSimulatorArm64Test` +
`testAndroidHostTest`), adding such a file **breaks `:<module>:check`/`build`** for that module, even though the narrow
`testAndroidHostTest` run is green. First hit in `offline-sync` (2026-07-03, `DefaultOfflineWriteDrainerTest`) — verified via
`./gradlew :offline-sync:check --dry-run` showing `jvmTest`/`iosSimulatorArm64Test` as real dependencies of `check`.

**How to apply:** If a task explicitly directs the test file into `commonTest` and forbids build.gradle.kts edits, write it
with JUnit5 (matching house style) and run/report via `testAndroidHostTest` — but flag to the user that `jvmTest` /
`iosSimulatorArm64Test` / the module's `check` will not compile until either (a) `implementation(kotlin("test"))` is added to
commonTest (enabling `kotlin.test.Test`, which KGP auto-maps to `kotlin-test-junit5` for the JVM target since `useJUnitPlatform()`
is already set repo-wide) or (b) the test file is moved to `androidHostTest` instead (matching the older, pre-0.3.0 pattern
this repo used before commonTest existed — see history in this file). Do not silently pick a fix; this is a build-config
decision for the user. See [[feedback_backgroundscope_advanceuntilidle]] for a related coroutines-test gotcha hit in the same
session.

**Why:** `offline-sync`'s `DefaultOfflineWriteDrainerTest` (F8 batch, 2026-07-03) is the first real commonTest test file in
this repo — earlier modules (`designsystem-core`, `core-ui`) simply had no commonTest directory at all and put tests
directly in `androidHostTest`, so this gap was never exercised before.
