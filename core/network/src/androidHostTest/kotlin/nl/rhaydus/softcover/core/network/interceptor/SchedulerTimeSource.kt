package nl.rhaydus.softcover.core.network.interceptor

import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * A [TimeSource] whose readings are driven entirely by a [TestCoroutineScheduler]'s virtual clock,
 * rather than the wall clock. This is what lets a test drive [RateLimitInterceptor]'s refill math
 * with `runTest`'s virtual time: every `delay()` the interceptor performs advances the scheduler,
 * and every [TimeMark] produced here reads that same advanced value — so no separate, manual
 * bookkeeping of elapsed time is needed alongside the coroutine scheduler.
 */
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
