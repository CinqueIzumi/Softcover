package nl.rhaydus.softcover.feature.widget.domain.model

import app.cash.turbine.test
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TrendingFetchTrackerTest {
    private val week = TrendingFeed.Trending(period = TrendingPeriod.Week)
    private val year = TrendingFeed.Trending(period = TrendingPeriod.Year)

    @Nested
    inner class Track {
        @Test
        fun `a fetch in flight is running and a successful one leaves no entry`() = runTest {
            // ----- Arrange -----
            val tracker = TrendingFetchTracker()
            val release = CompletableDeferred<Unit>()

            // ----- Act & Assert -----
            tracker.observe(week).test {
                awaitItem().shouldBeNull()
                launch {
                    tracker.track(week) {
                        release.await()
                        Result.success(Unit)
                    }
                }
                awaitItem() shouldBe TrendingFetchTracker.State.Running
                release.complete(Unit)
                awaitItem().shouldBeNull()
            }
        }

        @Test
        fun `a failed fetch is marked failed and the failure is returned`() = runTest {
            // ----- Arrange -----
            val tracker = TrendingFetchTracker()
            val failure = Result.failure<Unit>(IllegalStateException("down"))

            // ----- Act -----
            val result = tracker.track(week) { failure }

            // ----- Assert -----
            result shouldBe failure
            tracker.observe(week).test {
                awaitItem() shouldBe TrendingFetchTracker.State.Failed
            }
        }

        @Test
        fun `a success after a failure clears the failed mark`() = runTest {
            // ----- Arrange -----
            val tracker = TrendingFetchTracker()
            tracker.track(week) { Result.failure(IllegalStateException("down")) }

            // ----- Act -----
            tracker.track(week) { Result.success(Unit) }

            // ----- Assert -----
            tracker.observe(week).test {
                awaitItem().shouldBeNull()
            }
        }

        @Test
        fun `cancelling a fetch mid-flight clears its entry and rethrows the cancellation`() = runTest {
            // ----- Arrange -----
            val tracker = TrendingFetchTracker()

            // ----- Act & Assert -----
            tracker.observe(week).test {
                awaitItem().shouldBeNull()
                val job = launch {
                    tracker.track(week) { awaitCancellation() }
                }
                awaitItem() shouldBe TrendingFetchTracker.State.Running
                job.cancel()
                awaitItem().shouldBeNull()
                job.isCancelled shouldBe true
            }
        }

        @Test
        fun `a fetch that throws ends failed and a later success settles`() = runTest {
            // ----- Arrange -----
            val tracker = TrendingFetchTracker()

            // ----- Act -----
            val result = tracker.track(week) { error("boom") }
            tracker.observe(week).test {
                awaitItem() shouldBe TrendingFetchTracker.State.Failed
            }
            tracker.track(week) { Result.success(Unit) }

            // ----- Assert -----
            result.isFailure shouldBe true
            tracker.observe(week).test {
                awaitItem().shouldBeNull()
            }
        }

        @Test
        fun `a fetch of one feed does not change another feed's state`() = runTest {
            // ----- Arrange -----
            val tracker = TrendingFetchTracker()

            // ----- Act -----
            tracker.track(week) { Result.failure(IllegalStateException("down")) }

            // ----- Assert -----
            tracker.observe(year).test {
                awaitItem().shouldBeNull()
            }
        }

        @Test
        fun `a failure while another fetch of the feed is running keeps it running until that one ends`() = runTest {
            // ----- Arrange -----
            val tracker = TrendingFetchTracker()
            val release = CompletableDeferred<Unit>()
            val failFirst = CompletableDeferred<Unit>()

            // ----- Act & Assert -----
            tracker.observe(week).test {
                awaitItem().shouldBeNull()
                launch {
                    tracker.track(week) {
                        failFirst.await()
                        Result.failure(IllegalStateException("down"))
                    }
                }
                awaitItem() shouldBe TrendingFetchTracker.State.Running
                launch {
                    tracker.track(week) {
                        release.await()
                        Result.success(Unit)
                    }
                }
                runCurrent()
                failFirst.complete(Unit)
                runCurrent()
                expectNoEvents()
                release.complete(Unit)
                awaitItem().shouldBeNull()
            }
        }

        @Test
        fun `a success followed by a failure of overlapping fetches ends failed`() = runTest {
            // ----- Arrange -----
            val tracker = TrendingFetchTracker()
            val succeed = CompletableDeferred<Unit>()
            val fail = CompletableDeferred<Unit>()

            // ----- Act & Assert -----
            tracker.observe(week).test {
                awaitItem().shouldBeNull()
                launch {
                    tracker.track(week) {
                        succeed.await()
                        Result.success(Unit)
                    }
                }
                awaitItem() shouldBe TrendingFetchTracker.State.Running
                launch {
                    tracker.track(week) {
                        fail.await()
                        Result.failure(IllegalStateException("down"))
                    }
                }
                runCurrent()
                succeed.complete(Unit)
                runCurrent()
                expectNoEvents()
                fail.complete(Unit)
                awaitItem() shouldBe TrendingFetchTracker.State.Failed
            }
        }
    }


    @Nested
    inner class ClearFailures {
        @Test
        fun `forgets failed feeds`() = runTest {
            // ----- Arrange -----
            val tracker = TrendingFetchTracker()
            tracker.track(week) { Result.failure(IllegalStateException("down")) }

            // ----- Act -----
            tracker.clearFailures()

            // ----- Assert -----
            tracker.observe(week).test {
                awaitItem().shouldBeNull()
            }
        }

        @Test
        fun `keeps a fetch that is still running`() = runTest {
            // ----- Arrange -----
            val tracker = TrendingFetchTracker()
            val release = CompletableDeferred<Unit>()
            launch {
                tracker.track(week) {
                    release.await()
                    Result.success(Unit)
                }
            }
            runCurrent()

            // ----- Act -----
            tracker.clearFailures()

            // ----- Assert -----
            tracker.observe(week).test {
                awaitItem() shouldBe TrendingFetchTracker.State.Running
            }
            release.complete(Unit)
        }
    }
}
