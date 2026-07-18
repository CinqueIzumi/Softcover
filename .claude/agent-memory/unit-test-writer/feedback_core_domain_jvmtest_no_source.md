---
name: core-domain-jvmtest-no-source
description: core/domain's androidHostTest source set does NOT run under the jvmTest Gradle task — use testAndroidHostTest instead
metadata:
  type: feedback
---

For `:core:domain` (and likely other modules using the `androidHostTest` source set convention), `./gradlew :core:domain:jvmTest --tests "..."` reports `BUILD SUCCESSFUL` but with `compileTestKotlinJvm NO-SOURCE` / `jvmTest NO-SOURCE` — it silently runs zero tests, even for pre-existing files like `DeadlineProgressTest`. This is not specific to a new test file; verified by running the same filter against the existing sibling test.

The correct task that actually compiles and executes `androidHostTest` sources is:
```
./gradlew :core:domain:testAndroidHostTest --tests "nl.rhaydus.softcover.core.domain.model.<ClassName>"
```
Results land in `core/domain/build/test-results/testAndroidHostTest/`.

**Why:** An orchestrating agent's brief assumed jvmTest picks up androidHostTest sources "in this project's build setup" — that assumption was wrong. Blindly running the specified command would have reported a false "0 tests, build successful" pass without ever compiling the new test file.

**How to apply:** When asked to run `:core:domain:jvmTest` (or any module using the `androidHostTest` source set) and the task log shows `NO-SOURCE` for compile/test steps, don't trust the green "BUILD SUCCESSFUL" — cross-check by grepping `build/test-results/<taskName>/` for the expected test class, or just re-run with `testAndroidHostTest` instead and report the real result. See [[project_test_conventions]] for the broader test-directory layout.

Confirmed again for `:feature:settings` (a KMP feature module, not just `:core:domain`): `testDebugUnitTest` doesn't exist at all there either — Gradle fails outright with "task 'testDebugUnitTest' not found", not a silent NO-SOURCE. `./gradlew :<module>:tasks --all | grep -i "^test"` reliably surfaces the real task name (`testAndroidHostTest` + `testAndroid`) before wasting a run on a guessed task name. Treat `testDebugUnitTest` as an app-module-only task (single-variant Android app shell), not something to assume for KMP `core:*`/`feature:*` modules.
