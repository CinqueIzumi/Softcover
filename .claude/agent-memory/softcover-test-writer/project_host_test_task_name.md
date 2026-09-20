---
name: project_host_test_task_name
description: KMP core/feature modules run androidHostTest sources under testAndroidHostTest — jvmTest reports a false green NO-SOURCE and testDebugUnitTest does not exist.
metadata:
  type: project
---

Unit tests in `core:*` / `feature:*` KMP modules live under `src/androidHostTest/kotlin/...` and run with:

```
./gradlew :<module>:testAndroidHostTest --tests "<FQCN or *Pattern*>"
```

Two wrong task names keep turning up in briefs:

- **`jvmTest`** — reports `BUILD SUCCESSFUL` with `compileTestKotlinJvm NO-SOURCE` / `jvmTest NO-SOURCE`
  and runs zero tests, even for pre-existing files. A green result here is meaningless. Confirmed in
  `:core:domain`, `:feature:book_detail`, `:feature:library`.
- **`testDebugUnitTest`** — only exists on the single-variant `:app` module; in KMP modules Gradle fails
  with "task not found" (confirmed in `:core:database`, `:core:designsystem`, `:feature:settings`).

**How to apply:** if a suggested task returns NO-SOURCE or 404s, rerun with `testAndroidHostTest` before
reporting a failure. When unsure, `./gradlew :<module>:tasks --all | grep -i AndroidHostTest` gives the
real name. Confirm tests actually ran by checking `build/test-results/testAndroidHostTest/` for the
class, not just the "BUILD SUCCESSFUL" line. Don't edit build config to "fix" the mismatch inside a
test-writing task.
