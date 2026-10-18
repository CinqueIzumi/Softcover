package nl.rhaydus.softcover.feature.widget.presentation.refresh

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.core.domain.model.UpcomingWindow
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeed
import nl.rhaydus.softcover.feature.widget.domain.usecase.RefreshTrendingFeedUseCase
import nl.rhaydus.softcover.feature.widget.presentation.glance.TrendingWidget
import nl.rhaydus.softcover.feature.widget.presentation.glance.WidgetRepainter
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.hours

class TrendingFeedFetcherTest {
    private val widget = mockk<TrendingWidget>()
    private val repainter = mockk<WidgetRepainter>()
    private val refreshTrendingFeed = mockk<RefreshTrendingFeedUseCase>()
    private val week = TrendingFeed.Trending(period = TrendingPeriod.Week)
    private val upcoming = TrendingFeed.Upcoming(window = UpcomingWindow.Month)

    private fun TestScope.fetcher(): TrendingFeedFetcher = TrendingFeedFetcher(
        widget = widget,
        repainter = repainter,
        applicationScope = ApplicationScope(CoroutineScope(StandardTestDispatcher(testScheduler))),
        refreshTrendingFeed = refreshTrendingFeed,
    )

    @Nested
    inner class Fetch {
        @Test
        fun `every requested feed is fetched once with the max age`() = runTest {
            // ----- Arrange -----
            coEvery {
                refreshTrendingFeed(
                    feed = any(),
                    maxAge = any(),
                )
            } returns Result.success(Unit)

            // ----- Act -----
            fetcher().fetch(
                feeds = listOf(week, upcoming),
                maxAge = 3.hours,
            )
            runCurrent()

            // ----- Assert -----
            coVerify(exactly = 1) {
                refreshTrendingFeed(
                    feed = week,
                    maxAge = 3.hours,
                )
            }
            coVerify(exactly = 1) {
                refreshTrendingFeed(
                    feed = upcoming,
                    maxAge = 3.hours,
                )
            }
        }

        @Test
        fun `a failing feed does not stop the others from being fetched`() = runTest {
            // ----- Arrange -----
            coEvery {
                refreshTrendingFeed(
                    feed = week,
                    maxAge = any(),
                )
            } returns Result.failure(IllegalStateException("down"))
            coEvery {
                refreshTrendingFeed(
                    feed = upcoming,
                    maxAge = any(),
                )
            } returns Result.success(Unit)

            // ----- Act -----
            fetcher().fetch(
                feeds = listOf(week, upcoming),
                maxAge = 3.hours,
            )
            runCurrent()

            // ----- Assert -----
            coVerify(exactly = 1) {
                refreshTrendingFeed(
                    feed = upcoming,
                    maxAge = 3.hours,
                )
            }
        }

        @Test
        fun `the widget is repainted once after all feeds finish fetching`() = runTest {
            // ----- Arrange -----
            coEvery {
                refreshTrendingFeed(
                    feed = any(),
                    maxAge = any(),
                )
            } returns Result.success(Unit)
            coEvery {
                repainter.repaintAll(widget)
            } returns Unit

            // ----- Act -----
            fetcher().fetch(
                feeds = listOf(week, upcoming),
                maxAge = 3.hours,
            )
            runCurrent()

            // ----- Assert -----
            coVerify(exactly = 1) { repainter.repaintAll(widget) }
        }

        @Test
        fun `the widget is still repainted once when a fetch fails`() = runTest {
            // ----- Arrange -----
            coEvery {
                refreshTrendingFeed(
                    feed = week,
                    maxAge = any(),
                )
            } returns Result.failure(IllegalStateException("down"))
            coEvery {
                refreshTrendingFeed(
                    feed = upcoming,
                    maxAge = any(),
                )
            } returns Result.success(Unit)
            coEvery {
                repainter.repaintAll(widget)
            } returns Unit

            // ----- Act -----
            fetcher().fetch(
                feeds = listOf(week, upcoming),
                maxAge = 3.hours,
            )
            runCurrent()

            // ----- Assert -----
            coVerify(exactly = 1) { repainter.repaintAll(widget) }
        }
    }
}
