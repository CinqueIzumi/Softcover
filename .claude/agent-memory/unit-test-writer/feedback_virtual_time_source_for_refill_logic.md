---
name: virtual-time-source-for-refill-logic
description: How to test a class that takes an injectable kotlin.time.TimeSource and computes waits from it (e.g. token-bucket rate limiters) under kotlinx-coroutines-test virtual time
metadata:
  type: project
---

When a class under test takes an injectable `kotlin.time.TimeSource` (e.g. `RateLimitInterceptor` in
`core/network`) and calls real `delay()` based on durations it computes from that time source, do NOT
use `kotlin.time.TestTimeSource` (stdlib's manually-advanced fake) — it is a clock independent of the
coroutine scheduler, so a real `delay()` under `runTest` auto-advances virtual time while the fake stays
frozen, and the code under test will loop forever recomputing the same wait (test hangs).

Instead, write a small `TimeSource` whose `markNow()` reads `TestCoroutineScheduler.currentTime`
(from `kotlinx.coroutines.test`) at read time, e.g.:

```kotlin
internal class SchedulerTimeSource(
    private val scheduler: TestCoroutineScheduler,
) : TimeSource {
    override fun markNow(): TimeMark {
        val startMillis = scheduler.currentTime
        return object : TimeMark {
            override fun elapsedNow(): Duration = (scheduler.currentTime - startMillis).milliseconds
        }
    }
}
```

`kotlin.time.TimeMark` is an interface but only `elapsedNow()` is truly abstract from Kotlin's point of
view — `plus`/`minus`/`hasPassedNow`/`hasNotPassedNow` have default bodies in the interface itself (they
appear "abstract" in `javap` output only because the JVM bytecode uses the `$DefaultImpls` pattern; the
Kotlin compiler still treats them as defaulted via metadata), so an anonymous `object : TimeMark { override
fun elapsedNow() = ... }` compiles fine without implementing the rest.

Pass this fake as the constructor's `timeSource` param; inside `runTest { }`, real `delay()` calls the
interceptor makes now correctly advance `testScheduler.currentTime`, and the next `elapsedNow()` read
reflects that advance — no manual clock bookkeeping needed. Assert behavior by reading
`testScheduler.currentTime` after each `intercept()`/acquire call (e.g. `shouldBe 1_000L` for a 1-request
refill wait at 1 token/sec).

**Why:** Discovered writing `RateLimitInterceptorTest` for a token-bucket `ApolloInterceptor`
(`core/network/src/commonMain/.../interceptor/RateLimitInterceptor.kt`) whose `refill()` logic depends on
`timeSource.markNow().elapsedNow()`. `TestTimeSource` looked like the obvious fit but silently deadlocks
the test; the scheduler-backed fake is the only one that keeps refill math and virtual delay in sync.

**How to apply:** Any future test of refill/backoff/rate-limit logic driven by an injected `TimeSource`
in this codebase should reuse this `SchedulerTimeSource` pattern (kept as a small helper file alongside
the test, one type per file) rather than `TestTimeSource` or the real wall clock.
