package nl.rhaydus.softcover.feature.widget.domain.model

import app.cash.turbine.test
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QuoteFetchTrackerTest {
    @Nested
    inner class Track {
        @Test
        fun `a fetch in flight is running and a successful one leaves no state`() = runTest {
            // ----- Arrange -----
            val tracker = QuoteFetchTracker()
            val release = CompletableDeferred<Unit>()

            // ----- Act & Assert -----
            tracker.state.test {
                awaitItem().shouldBeNull()
                launch {
                    tracker.track {
                        release.await()
                        Result.success(Unit)
                    }
                }
                awaitItem() shouldBe QuoteFetchTracker.State.Running
                release.complete(Unit)
                awaitItem().shouldBeNull()
            }
        }

        @Test
        fun `a failed fetch is marked failed and the failure is returned`() = runTest {
            // ----- Arrange -----
            val tracker = QuoteFetchTracker()
            val failure = Result.failure<Unit>(IllegalStateException("down"))

            // ----- Act -----
            val result = tracker.track { failure }

            // ----- Assert -----
            result shouldBe failure
            tracker.state.value shouldBe QuoteFetchTracker.State.Failed
        }

        @Test
        fun `a fetch that throws ends failed and returns the exception as a failure`() = runTest {
            // ----- Arrange -----
            val tracker = QuoteFetchTracker()

            // ----- Act -----
            val result = tracker.track { error("boom") }

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull()?.message shouldBe "boom"
            tracker.state.value shouldBe QuoteFetchTracker.State.Failed
        }

        @Test
        fun `a success after a failure clears the failed mark`() = runTest {
            // ----- Arrange -----
            val tracker = QuoteFetchTracker()
            tracker.track { Result.failure(IllegalStateException("down")) }

            // ----- Act -----
            tracker.track { Result.success(Unit) }

            // ----- Assert -----
            tracker.state.value.shouldBeNull()
        }

        @Test
        fun `cancelling a fetch clears the running state and rethrows the cancellation`() = runTest {
            // ----- Arrange -----
            val tracker = QuoteFetchTracker()

            // ----- Act & Assert -----
            tracker.state.test {
                awaitItem().shouldBeNull()
                val job = launch {
                    tracker.track { awaitCancellation() }
                }
                awaitItem() shouldBe QuoteFetchTracker.State.Running
                job.cancel()
                awaitItem().shouldBeNull()
                job.isCancelled shouldBe true
            }
        }
    }

    @Nested
    inner class ClearFailure {
        @Test
        fun `forgets a failure`() = runTest {
            // ----- Arrange -----
            val tracker = QuoteFetchTracker()
            tracker.track { Result.failure(IllegalStateException("down")) }

            // ----- Act -----
            tracker.clearFailure()

            // ----- Assert -----
            tracker.state.value.shouldBeNull()
        }

        @Test
        fun `keeps a fetch that is still running`() = runTest {
            // ----- Arrange -----
            val tracker = QuoteFetchTracker()
            val release = CompletableDeferred<Unit>()
            launch {
                tracker.track {
                    release.await()
                    Result.success(Unit)
                }
            }
            runCurrent()

            // ----- Act -----
            tracker.clearFailure()

            // ----- Assert -----
            tracker.state.value shouldBe QuoteFetchTracker.State.Running
            release.complete(Unit)
        }

        @Test
        fun `a failure while another fetch is running keeps it running until that one ends`() = runTest {
            // ----- Arrange -----
            val tracker = QuoteFetchTracker()
            val release = CompletableDeferred<Unit>()
            val failFirst = CompletableDeferred<Unit>()

            // ----- Act & Assert -----
            tracker.state.test {
                awaitItem().shouldBeNull()
                launch {
                    tracker.track {
                        failFirst.await()
                        Result.failure(IllegalStateException("down"))
                    }
                }
                awaitItem() shouldBe QuoteFetchTracker.State.Running
                launch {
                    tracker.track {
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
            val tracker = QuoteFetchTracker()
            val succeed = CompletableDeferred<Unit>()
            val fail = CompletableDeferred<Unit>()

            // ----- Act & Assert -----
            tracker.state.test {
                awaitItem().shouldBeNull()
                launch {
                    tracker.track {
                        succeed.await()
                        Result.success(Unit)
                    }
                }
                awaitItem() shouldBe QuoteFetchTracker.State.Running
                launch {
                    tracker.track {
                        fail.await()
                        Result.failure(IllegalStateException("down"))
                    }
                }
                runCurrent()
                succeed.complete(Unit)
                runCurrent()
                expectNoEvents()
                fail.complete(Unit)
                awaitItem() shouldBe QuoteFetchTracker.State.Failed
            }
        }
    }
}
