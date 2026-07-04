---
name: backgroundScope + explicit TestDispatcher + advanceUntilIdle is unreliable — use runCurrent
description: kotlinx-coroutines-test gotcha when testing a start(scope) API that launches onto an explicitly-constructed StandardTestDispatcher via TestScope.backgroundScope
type: feedback
---

When testing a `start(scope: CoroutineScope)`-style API (a long-lived collector job, e.g. `OfflineWriteDrainer.start`) by
calling it with `backgroundScope` inside `runTest {}`, and the production code's own `AppDispatchers`/similar DI forces
launching onto an **explicitly constructed** `StandardTestDispatcher(testScheduler)` (rather than backgroundScope's own
default dispatcher), calling `advanceUntilIdle()` afterward does **not** reliably drive that coroutine — even its synchronous
`onStart {}` block never runs, and the test's assertions see totally untouched state (as if `start()` was never called).

**Confirmed via a minimal repro** (isolated in a throwaway JUnit5 test in the same module): `backgroundScope.launch(explicitDispatcher) { flow.onStart { seen = true }.collect() }` then `advanceUntilIdle()` → `seen` stays `false`. The **same** setup
with `runCurrent()` instead of `advanceUntilIdle()` → works immediately. Also confirmed working: plain `launch(explicitDispatcher)` (not `backgroundScope`) + `advanceUntilIdle()`, and `backgroundScope.launch(UnconfinedTestDispatcher(testScheduler))` +
`advanceUntilIdle()`. So the specific broken combination is `backgroundScope` + an explicit **Standard**TestDispatcher +
`advanceUntilIdle()` specifically (not `runCurrent()`).

**Why:** Root cause not fully diagnosed (likely a `TestScope.backgroundScope`-specific idle-tracking quirk in
kotlinx-coroutines-test when the child coroutine's dispatcher instance differs from the scope's own default one), but the
workaround is cheap and reliable.

**How to apply:** When a test constructs `AppDispatchers(main = d, io = d, default = d)` from one
`StandardTestDispatcher(testScheduler)` (as instructed for testing dispatcher-injected production code) and drives a
`backgroundScope.launch(...)`-based long-lived job, use `runCurrent()` — not `advanceUntilIdle()` — after `start(...)` and
after every subsequent state mutation (e.g. flipping a `MutableStateFlow` the collector reacts to). Reserve
`advanceUntilIdle()`/`advanceTimeBy()` for cases with real virtual-time delays awaited directly in the test body (e.g. a
suspend `drain()` call with in-drain backoff `delay()`s) — those are unaffected and work fine with `advanceUntilIdle()`.
First hit: `rhaydus-foundation` `offline-sync` module, `DefaultOfflineWriteDrainerTest.Start` (2026-07-03).
