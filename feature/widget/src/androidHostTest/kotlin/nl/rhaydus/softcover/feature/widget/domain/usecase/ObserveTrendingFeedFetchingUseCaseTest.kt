package nl.rhaydus.softcover.feature.widget.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.awaitCancellation
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveTrendingFetchedAtUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveUpcomingFetchedAtUseCase
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.core.domain.model.UpcomingWindow
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeed
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeedFetchStatus
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFetchTracker
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class ObserveTrendingFeedFetchingUseCaseTest {
    private val week = TrendingFeed.Trending(period = TrendingPeriod.Week)
    private val upcoming = TrendingFeed.Upcoming(window = UpcomingWindow.Year)
    private val fetchedAt = Instant.fromEpochMilliseconds(1_000)
    private val observeTrendingFetchedAt = mockk<ObserveTrendingFetchedAtUseCase>()
    private val observeUpcomingFetchedAt = mockk<ObserveUpcomingFetchedAtUseCase>()
    private val tracker = TrendingFetchTracker()
    private val useCase = ObserveTrendingFeedFetchingUseCase(
        observeTrendingFetchedAt = observeTrendingFetchedAt,
        observeUpcomingFetchedAt = observeUpcomingFetchedAt,
        fetchTracker = tracker,
    )

    @Nested
    inner class Invoke {
        @Test
        fun `a feed never fetched with no fetch running is loading`() = runTest {
            // ----- Arrange -----
            every {
                observeTrendingFetchedAt(period = TrendingPeriod.Week)
            } returns flowOf(null)

            // ----- Act -----
            val result = useCase(week).first()

            // ----- Assert -----
            result shouldBe TrendingFeedFetchStatus.Loading
        }

        @Test
        fun `a fetched feed is settled`() = runTest {
            // ----- Arrange -----
            every {
                observeTrendingFetchedAt(period = TrendingPeriod.Week)
            } returns flowOf(fetchedAt)

            // ----- Act -----
            val result = useCase(week).first()

            // ----- Assert -----
            result shouldBe TrendingFeedFetchStatus.Settled
        }

        @Test
        fun `a fetch running does not unsettle a fetched feed`() = runTest {
            // ----- Arrange -----
            every {
                observeTrendingFetchedAt(period = TrendingPeriod.Week)
            } returns flowOf(fetchedAt)
            val job = backgroundScope.launch {
                tracker.track(week) { awaitCancellation() }
            }
            runCurrent()

            // ----- Act -----
            val result = useCase(week).first()

            // ----- Assert -----
            job.isActive shouldBe true
            result shouldBe TrendingFeedFetchStatus.Settled
        }

        @Test
        fun `a failed fetch with nothing cached is failed`() = runTest {
            // ----- Arrange -----
            every {
                observeTrendingFetchedAt(period = TrendingPeriod.Week)
            } returns flowOf(null)
            tracker.track(week) { Result.failure(IllegalStateException("down")) }

            // ----- Act -----
            val result = useCase(week).first()

            // ----- Assert -----
            result shouldBe TrendingFeedFetchStatus.Failed
        }

        @Test
        fun `an upcoming feed reads the fetched time of its window`() = runTest {
            // ----- Arrange -----
            every {
                observeTrendingFetchedAt(any())
            } returns flowOf(null)
            every {
                observeUpcomingFetchedAt(window = UpcomingWindow.Year)
            } returns flowOf(fetchedAt)

            // ----- Act -----
            val result = useCase(upcoming).first()

            // ----- Assert -----
            result shouldBe TrendingFeedFetchStatus.Settled
        }
    }
}
