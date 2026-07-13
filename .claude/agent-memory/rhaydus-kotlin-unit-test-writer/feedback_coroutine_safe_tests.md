---
name: feedback_coroutine_safe_tests
description: Tests must share ONE TestDispatcher/scheduler across everything async — mismatched schedulers cause intermittent hangs and false-positive passes
metadata:
  type: feedback
---

A test is only trustworthy if every coroutine it exercises runs on the **same** `TestCoroutineScheduler`
as the `runTest` body. Mixing schedulers makes assertions pass by luck and can hang the whole Gradle
worker at 0% CPU (which then wedges the module's `testAndroidHostTest` with "Could not stop all
services").

**The bug that prompted this** (`core/database/.../DismissedContinueSeriesDaoTest`): the DB was built
with `SoftcoverDatabase.build(builder, queryContext = UnconfinedTestDispatcher())` — and
`build` does `.setQueryCoroutineContext(queryContext)`, so Room `Flow`s EMIT on that dispatcher's
scheduler. But each test body was a bare `runTest { }`, which spins up its OWN, different scheduler.
Turbine's `awaitItem()` (driven by `runTest`'s scheduler) then waited on a Room emission dispatched on
a scheduler nothing advanced → intermittent forever-hang (intermittent because `UnconfinedTestDispatcher`
sometimes runs the emission eagerly on the current thread before the await suspends).

**Rules for writing coroutine tests here:**
- Declare ONE dispatcher field: `private val testDispatcher = UnconfinedTestDispatcher()` (or
  `StandardTestDispatcher()`), and use it EVERYWHERE async happens in that test.
- Pass that same field wherever the code-under-test takes a dispatcher/coroutine context: Room's
  `queryContext`, an injected `AppDispatchers`, a repository/use-case constructor dispatcher, etc.
- Run every test body as `runTest(testDispatcher) { ... }` — never bare `runTest { }` when the
  code-under-test dispatches on a dispatcher you configured, or observes a Room/DataStore `Flow`.
- For code that dispatches on `Dispatchers.Main` (ScreenModels via `screenModelScope` / a `mainDispatcher`),
  set it in a shared hook: `@BeforeEach Dispatchers.setMain(testDispatcher)` /
  `@AfterEach Dispatchers.resetMain()` (needs `kotlinx-coroutines-test`). Note `setMain` alone does NOT
  fix a Room-`Flow` test whose emissions go through an explicit `queryContext` — that still needs the
  shared dispatcher + `runTest(testDispatcher)`.
- Collecting a hot/never-completing `Flow` (Room queries, DataStore, `StateFlow`): use Turbine
  `.test { awaitItem(); cancelAndIgnoreRemainingEvents() }` under the shared scheduler; never a bare
  `.first()`/`.collect {}` that can suspend forever off-clock.
- Smell test before finishing: "if the async work were dispatched on a scheduler my `runTest` doesn't
  advance, would this test hang or pass by accident?" If yes, the schedulers aren't shared — fix it.

Verify with a BOUNDED run. A handful of unit tests finish in seconds; if a `--tests` run goes past ~90s
with no result, assume a scheduler-mismatch hang and fix it rather than waiting it out.

See [[project_canonical_test_templates]].
