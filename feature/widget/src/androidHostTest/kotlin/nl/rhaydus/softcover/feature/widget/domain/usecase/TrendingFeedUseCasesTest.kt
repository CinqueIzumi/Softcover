package nl.rhaydus.softcover.feature.widget.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveTrendingBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveUpcomingBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RefreshTrendingBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RefreshUpcomingBooksUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.core.domain.model.UpcomingWindow
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeed
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFetchTracker
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.hours

class TrendingFeedUseCasesTest {
    @Nested
    inner class ObserveTrendingFeed {
        private val trendingBooks = listOf(mockk<Book>())
        private val upcomingBooks = listOf(mockk<Book>(), mockk<Book>())
        private val observeTrendingBooks = mockk<ObserveTrendingBooksUseCase>()
        private val observeUpcomingBooks = mockk<ObserveUpcomingBooksUseCase>()
        private val useCase = ObserveTrendingFeedUseCase(
            observeTrendingBooks = observeTrendingBooks,
            observeUpcomingBooks = observeUpcomingBooks,
        )

        @Test
        fun `a trending feed lists the books of its period`() = runTest {
            // ----- Arrange -----
            every {
                observeTrendingBooks(period = TrendingPeriod.Month)
            } returns flowOf(trendingBooks)

            // ----- Act -----
            val result = useCase(feed = TrendingFeed.Trending(period = TrendingPeriod.Month)).first()

            // ----- Assert -----
            result shouldBe trendingBooks
        }

        @Test
        fun `an upcoming feed lists the books of its window`() = runTest {
            // ----- Arrange -----
            every {
                observeUpcomingBooks(window = UpcomingWindow.Year)
            } returns flowOf(upcomingBooks)

            // ----- Act -----
            val result = useCase(feed = TrendingFeed.Upcoming(window = UpcomingWindow.Year)).first()

            // ----- Assert -----
            result shouldBe upcomingBooks
        }
    }

    @Nested
    inner class RefreshTrendingFeed {
        private val refreshTrendingBooks = mockk<RefreshTrendingBooksUseCase>()
        private val refreshUpcomingBooks = mockk<RefreshUpcomingBooksUseCase>()
        private val useCase = RefreshTrendingFeedUseCase(
            refreshTrendingBooks = refreshTrendingBooks,
            refreshUpcomingBooks = refreshUpcomingBooks,
            fetchTracker = TrendingFetchTracker(),
        )

        @Test
        fun `a trending feed refreshes its period and reports that outcome`() = runTest {
            // ----- Arrange -----
            val failure = Result.failure<Unit>(IllegalStateException("down"))
            coEvery {
                refreshTrendingBooks(
                    period = TrendingPeriod.Week,
                    maxAge = 3.hours,
                )
            } returns failure

            // ----- Act -----
            val result = useCase(
                feed = TrendingFeed.Trending(period = TrendingPeriod.Week),
                maxAge = 3.hours,
            )

            // ----- Assert -----
            result shouldBe failure
        }

        @Test
        fun `an upcoming feed refreshes its window and reports that outcome`() = runTest {
            // ----- Arrange -----
            coEvery {
                refreshUpcomingBooks(
                    window = UpcomingWindow.Month,
                    maxAge = 3.hours,
                )
            } returns Result.success(Unit)

            // ----- Act -----
            val result = useCase(
                feed = TrendingFeed.Upcoming(window = UpcomingWindow.Month),
                maxAge = 3.hours,
            )

            // ----- Assert -----
            result shouldBe Result.success(Unit)
        }
    }
}
