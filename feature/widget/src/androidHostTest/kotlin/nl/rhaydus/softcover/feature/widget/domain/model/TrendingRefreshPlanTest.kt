package nl.rhaydus.softcover.feature.widget.domain.model

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.core.domain.model.UpcomingWindow
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TrendingRefreshPlanTest {
    @Nested
    inner class TrendingRefreshPlanFor {
        @Test
        fun `no placed widgets gives no plan`() {
            // ----- Arrange -----
            val placed = emptyList<TrendingSettings>()

            // ----- Act -----
            val result = trendingRefreshPlan(placed)

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `the shortest interval wins and each feed is listed once`() {
            // ----- Arrange -----
            val placed = listOf(
                TrendingSettings(
                    feed = TrendingFeed.Trending(period = TrendingPeriod.Week),
                    cadence = TrendingRefreshCadence.Daily,
                ),
                TrendingSettings(
                    feed = TrendingFeed.Trending(period = TrendingPeriod.Week),
                    cadence = TrendingRefreshCadence.TwiceDaily,
                ),
                TrendingSettings(
                    feed = TrendingFeed.Trending(period = TrendingPeriod.Year),
                    cadence = TrendingRefreshCadence.Daily,
                ),
            )

            // ----- Act -----
            val result = trendingRefreshPlan(placed)

            // ----- Assert -----
            result shouldBe TrendingRefreshPlan(
                interval = TrendingRefreshCadence.TwiceDaily.interval,
                feeds = setOf(
                    TrendingFeed.Trending(period = TrendingPeriod.Week),
                    TrendingFeed.Trending(period = TrendingPeriod.Year),
                ),
            )
        }

        @Test
        fun `a trending and an upcoming widget over the same span are separate feeds`() {
            // ----- Arrange -----
            val placed = listOf(
                TrendingSettings(feed = TrendingFeed.Trending(period = TrendingPeriod.Year)),
                TrendingSettings(feed = TrendingFeed.Upcoming(window = UpcomingWindow.Year)),
            )

            // ----- Act -----
            val result = trendingRefreshPlan(placed)

            // ----- Assert -----
            result?.feeds shouldBe setOf(
                TrendingFeed.Trending(period = TrendingPeriod.Year),
                TrendingFeed.Upcoming(window = UpcomingWindow.Year),
            )
        }
    }
}
