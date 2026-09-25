---
name: feedback_coroutine_test_scheduling
description: Share ONE TestDispatcher/scheduler across everything async; use runCurrent (not advanceUntilIdle) for backgroundScope + explicit StandardTestDispatcher; plain flowOf DAO stubs need no dispatcher.
metadata:
  type: feedback
---

## One scheduler for everything

A test is only trustworthy if every coroutine it exercises runs on the **same** `TestCoroutineScheduler`
as the `runTest` body. Mixing schedulers makes assertions pass by luck and can hang the Gradle worker at
0% CPU (which then wedges `testAndroidHostTest` with "Could not stop all services").

The bug that prompted this (`core/database/.../DismissedContinueSeriesDaoTest`): the DB was built with
`SoftcoverDatabase.build(builder, queryContext = UnconfinedTestDispatcher())`, so Room `Flow`s emitted on
that dispatcher's scheduler, while each test body was a bare `runTest { }` with its own scheduler.
Turbine's `awaitItem()` waited on an emission nothing advanced → intermittent forever-hang.

- Declare ONE dispatcher field (`UnconfinedTestDispatcher()` or `StandardTestDispatcher()`) and pass it
  wherever the code under test takes a dispatcher/context: Room's `queryContext`, an injected
  `AppDispatchers`, a constructor dispatcher.
- Run every body as `runTest(testDispatcher) { ... }` when the code dispatches on a configured dispatcher
  or observes a Room/DataStore `Flow`.
- For `Dispatchers.Main` (ScreenModels): `@BeforeEach Dispatchers.setMain(testDispatcher)` /
  `@AfterEach Dispatchers.resetMain()`. `setMain` alone does NOT fix a Room-`Flow` test whose emissions
  go through an explicit `queryContext`.
- Collect hot/never-completing flows with Turbine `.test { awaitItem(); cancelAndIgnoreRemainingEvents() }`,
  never a bare `.first()`/`.collect {}`.
- Verify with a bounded run: if a narrow `--tests` run passes ~90s with no result, assume a
  scheduler-mismatch hang and fix it.

## backgroundScope + explicit StandardTestDispatcher → use runCurrent()

When a `start(scope)`-style long-lived collector is launched via `backgroundScope` onto an explicitly
constructed `StandardTestDispatcher(testScheduler)` (e.g. `AppDispatchers(main = d, io = d, default = d)`),
`advanceUntilIdle()` does not drive it — not even its synchronous `onStart {}`. `runCurrent()` does.
Confirmed with a minimal repro (first hit: foundation `DefaultOfflineWriteDrainerTest.Start`). Plain
`launch(explicitDispatcher)` + `advanceUntilIdle()` and `backgroundScope.launch(UnconfinedTestDispatcher(testScheduler))`
both work; only that one combination breaks.

**How to apply:** call `runCurrent()` after `start(...)` and after every state mutation the collector reacts
to. Keep `advanceUntilIdle()`/`advanceTimeBy()` for real virtual-time delays awaited in the test body.

## Plain DAO-passthrough flows need none of this

For a `*LocalDataSourceImpl` that forwards a DAO `Flow` method, stub `every { dao.observe(...) } returns flowOf(entities)`
on a `mockk(relaxed = true)` DAO and assert with `test { awaitItem() shouldBe expected; awaitComplete() }`
inside `runTest`. `flowOf` has no dispatcher, so there is no scheduler to share. Verify multi-call suspend
delegates (e.g. `replaceAll` = `clearForUser` then `upsertAll`) with `coVerifyOrder`.
