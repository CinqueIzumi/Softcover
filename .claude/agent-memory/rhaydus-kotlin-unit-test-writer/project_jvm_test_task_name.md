---
name: project_jvm_test_task_name
description: The correct Gradle task for running commonMain-targeting unit tests on androidHostTest source sets is testAndroidHostTest, not jvmTest.
metadata:
  type: project
---

`./gradlew :<module>:jvmTest --tests "..."` returns NO-SOURCE for at least `:core:domain` and `:feature:book_detail` — confirmed as a pre-existing, module-wide wiring quirk (also reproduced against the sibling `DeadlineProgressTest`), not something caused by any single test file.

The tests actually live under `src/androidHostTest/kotlin/...` in these modules, and the working task is:

```
./gradlew :<module>:testAndroidHostTest --tests "<FQCN>"
```

**Why:** the module's `androidHostTest` source set isn't wired to a task literally named `jvmTest`, even though CLAUDE.md's example commands reference `./gradlew test` / `:app:test`. This was hit independently while writing tests for `ReadingPaceForecast`, `ReadingJournalEntryMapper`, and `OnLensSelectedAction` in `:core:domain` and `:feature:book_detail`. Also confirmed directly (not just by analogy) for `:feature:library` — `./gradlew :feature:library:testAndroidHostTest --tests "..."` is the correct task; `tasks --all` for that module lists `testAndroidHostTest` with no `jvmTest` equivalent.

**How to apply:** when a test-run instruction in a prompt specifies `jvmTest` for one of these modules and it returns NO-SOURCE, don't treat it as a real failure — retry with `testAndroidHostTest` before reporting a build problem. Worth flagging to the user if the mismatch should be documented in CLAUDE.md/build files, but do not touch build config as part of a test-writing task without being asked.
