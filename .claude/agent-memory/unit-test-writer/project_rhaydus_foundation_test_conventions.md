---
name: rhaydus-foundation Test Conventions
description: Testing framework, style rules, and gradle task for the rhaydus-foundation library repo (separate repo from Softcover, at ~/Documents/projects/rhaydus-foundation)
type: project
---

The `rhaydus-foundation` repo (KMP library, groupId `nl.rhaydus`, modules like `core-ui`, `designsystem-core`) uses the same
idiom as [[project_test_conventions]] (Softcover): JUnit 5 (`org.junit.jupiter.api.Test`, `@Nested inner class` named after
the function/property under test), kotest `io.kotest.matchers.shouldBe`, kotlinx-coroutines-test `runTest`. AAA markers
(`// ----- Arrange -----` / `// ----- Act -----` / `// ----- Assert -----`, collapsing to `// ----- Act & Assert -----` when
act and assert are the same expression) are also used here — confirmed via
`designsystem-core/src/androidHostTest/kotlin/nl/rhaydus/designsystem/layout/WindowSizeClassTest.kt`.

**No commonTest source set**: KMP modules here test only from `<module>/src/androidHostTest/kotlin/...`, mirroring the
commonMain package path. Gradle task is `./gradlew :<module>:testAndroidHostTest --tests "..."` (same
`testAndroidHostTest` vs `testDebugUnitTest` distinction as Softcover's KMP `core:*` modules, see
[[project_test_conventions]]).

**`ExperimentalCoroutinesApi` opt-in warning is accepted, uncommented**: using `runCurrent()` / `UnconfinedTestDispatcher`
inside `runTest {}` emits a compiler warning without `@OptIn` — build still succeeds and this is left as-is (matches the
Softcover pattern of not annotating this in test files).

**Why:** first unit-test-writer task in this repo (2026-07-03, `core-ui` `NetworkAvailability`/`BaseNetworkAvailabilityProvider`
tests) — confirms the Softcover conventions carry over to this sibling repo rather than being Softcover-specific.

**How to apply:** When asked to write tests in `rhaydus-foundation` (not Softcover), reuse the Softcover test-writing
conventions memory wholesale; only the module-per-package test directory and lack of `commonTest` are repo-specific.
