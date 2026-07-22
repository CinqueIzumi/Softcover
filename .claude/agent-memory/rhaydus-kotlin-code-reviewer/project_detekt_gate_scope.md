---
name: project_detekt_gate_scope
description: Only detektAndroidMain/detektJvmMain/detektMain (main source sets) are gated by styleCheck/check — detektAndroidHostTest and other test-source detekt tasks are NOT, so their findings are informational only, not blockers.
metadata:
  type: project
---

Root `build.gradle.kts` defines `val typeResolvedDetektTasks = setOf("detektAndroidMain",
"detektJvmMain", "detektMain")` and the `styleCheck` task (which `check` also runs) only depends on
tasks matching those three names across subprojects. Confirmed by reading the task registration
directly (not inference) — see the `tasks.register("styleCheck")` block and its comment: "runs the
per-compilation tasks ... across every module ... cover commonMain plus the platform source sets".

**How to apply:** when running detekt manually during a review to double-check a diff, only run
`:<module>:detektAndroidMain` / `:<module>:detektJvmMain` (or `:<module>:detektMain` for non-KMP
modules like `:app`/`:desktopApp`) and treat those findings as real, gate-blocking issues. Do **not**
also run `:<module>:detektAndroidHostTest` (or other `detekt<Target><TestSourceSet>` tasks) expecting
gate-equivalent results — **found 2026-07-17**: running it against `:feature:library`'s new/edited
test files surfaced 364 weighted issues (mostly `FunctionNaming` on backtick-quoted `@Test` function
names, plus a few `UnsafeCallOnNullableType` on `!!` in assertions) that are **not** part of any
enforced gate and would be a false alarm if reported as blockers. If mentioning test-source detekt
findings at all, frame them explicitly as non-blocking/informational, distinct from the
`detektAndroidMain`/`detektJvmMain` findings which do fail `./gradlew check`.

See also [[style_one_type_per_file_colocated_support_class]] for a concrete `MatchingDeclarationName`
finding caught this way.
