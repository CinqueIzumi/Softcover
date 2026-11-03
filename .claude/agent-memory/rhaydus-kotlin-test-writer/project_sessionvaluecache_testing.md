---
name: project_sessionvaluecache_testing
description: Use a real SessionValueCache (never a mock); test keyed caching per key; prove per-key locking with virtual time, where a lock leak shows as a hang, not a failed assertion.
metadata:
  type: project
---

`SessionValueCache<K, V>` (`core/domain/.../util/SessionValueCache.kt`) is a concrete, mutex-guarded class.

## Testing a use case that consumes it

- Never mock it: instantiate a real `SessionValueCache()` in `setUp()` and pass it to the use case.
- It caches success only (a thrown `load()` propagates without storing), so "does not cache a failure" =
  two sequential `coEvery` stubs (throw, then return) around two calls, then `coVerify(exactly = 2)`.
- Unkeyed (`SessionValueCache<Unit, V>` via the `getOrPut(load)` extension): one call gates the session.
- Keyed (e.g. `booksByGenreCache: SessionValueCache<String, List<Book>>` in `GetBecauseYouReadBooksUseCase`):
  the critical test is "visit A, visit B, return to A, assert `fetchX(key = A, ...)` called exactly once."
- For flows that emit a loading placeholder first, see [[feedback_flow_emission_assertions]].

## Testing the cache's own locking

It uses a `registryMutex` guarding only the backing maps (never held across `load()`) plus a per-key
`Mutex` spanning the fetch. A value-only test passes even under a class-wide-lock bug, so prove the
shape with virtual time (`SessionValueCacheTest.GetOrPutKeyed`):

- **Cross-key non-blocking:** key A's `load` does `delay(10_000)`, key B's none. `launch` A, `runCurrent()`,
  then call B's `getOrPut` directly and assert `currentTime shouldBe 0L` while `aJob.isActive`. Then
  `advanceTimeBy(10_000)` + `aJob.join()` and `currentTime shouldBe 10_000L`.
- **Same-key collapse:** already covered by `concurrent callers for the same key collapse into one load invocation`
  — don't duplicate it.
- **Failure releases the lock:** `launch` the first caller (in `runCatching`), `runCurrent()` so it
  deterministically holds the lock inside `load()` behind a `CompletableDeferred` gate, start the second
  via `async`, complete the gate so the first throws, then assert the second got the recovered value and a
  third call hits the cache. A leaked lock shows as a hang (the `await()` never completes), not a failure.

`runCurrent()`, `advanceTimeBy()` and `currentTime` need `@OptIn(ExperimentalCoroutinesApi::class)` on the
test function.
