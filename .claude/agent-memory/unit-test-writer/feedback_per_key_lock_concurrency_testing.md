---
name: per-key-lock-concurrency-testing
description: How to test SessionValueCache's per-key locking (non-blocking across keys, collapse within a key, no lock leak on failure) with virtual time, plus the ExperimentalCoroutinesApi opt-in it needs
metadata:
  type: project
---

`SessionValueCache<K, V>` (`core/domain/.../util/SessionValueCache.kt`) uses a `registryMutex` that only guards the two backing maps (never held across the suspending `load()`) plus a per-key `Mutex` that spans the fetch. Testing this shape needs virtual-time proof, not just value assertions — a value-only test (both loads eventually return the right value) would pass even under the old class-wide-lock bug, since collapsing eventually finishes either way.

Pattern used (`SessionValueCacheTest.kt`, `GetOrPutKeyed`):
- **Cross-key non-blocking**: give key A's `load` a `delay(10_000)` and key B's `load` none. `launch` A, call `runCurrent()` to drive A up to its suspension (holding A's per-key lock), then call B's `getOrPut` directly (not launched) and assert it resolves with `currentTime shouldBe 0L` while `aJob.isActive shouldBe true` — proving B never waited on A's in-flight delay. Then `advanceTimeBy(10_000)` + `aJob.join()` to let A finish and check `currentTime shouldBe 10_000L`.
- **Same-key collapse under genuine concurrency**: already covered by the pre-existing `concurrent callers for the same key collapse into one load invocation` test — it launches both callers via `launch` *before* completing the shared `CompletableDeferred` gate, so both are real suspended coroutines contending on the lock, not sequential/awaited calls. Don't duplicate this.
- **Failure releases the per-key lock**: `launch` the first caller (wrapped in `runCatching` since it will throw), call `runCurrent()` to force it to acquire the lock and suspend inside `load()` (via a `CompletableDeferred` gate) — this determinism matters, otherwise whichever coroutine happens to run first non-deterministically becomes the one that fails. Then start the second caller (`async`), complete the gate so the first throws, `join()`/`await()` both, and assert the second one got `"recovered"` and a later call also gets the cached value without a third load. If the lock leaked, the second caller's `await()` would simply never complete (test hangs/times out) — that's the failure signature to watch for, not an assertion.

**Gotcha**: `runCurrent()`, `advanceTimeBy()`, and `currentTime` (all `kotlinx.coroutines.test` extensions on `TestScope`) are `@ExperimentalCoroutinesApi` and need `@OptIn(ExperimentalCoroutinesApi::class)` on the test function — otherwise it's just a compiler warning (not an error), but the file already leaves warnings as noise elsewhere so it's cleaner to opt in explicitly.

**Why:** Added while covering a fix that moved `SessionValueCache` from one class-wide `Mutex` to per-key locking (2026-08-23) — the property most at risk was exactly the thing a naive value-based test can't catch.

**How to apply:** Any future concurrency-shape change to `SessionValueCache` (or a similar per-key-lock cache) should get a virtual-time test in this shape, not just a value-returning one. See also [[sessionvaluecache-and-placeholder-settling]] for testing use cases that *consume* this cache.
