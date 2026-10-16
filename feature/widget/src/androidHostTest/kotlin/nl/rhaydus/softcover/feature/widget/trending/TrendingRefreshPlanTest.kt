package nl.rhaydus.softcover.feature.widget.trending

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
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
        fun `the shortest interval wins and each period is listed once`() {
            // ----- Arrange -----
            val placed = listOf(
                TrendingSettings(
                    period = TrendingPeriod.Week,
                    cadence = TrendingRefreshCadence.Daily,
                ),
                TrendingSettings(
                    period = TrendingPeriod.Week,
                    cadence = TrendingRefreshCadence.TwiceDaily,
                ),
                TrendingSettings(
                    period = TrendingPeriod.Year,
                    cadence = TrendingRefreshCadence.Daily,
                ),
            )

            // ----- Act -----
            val result = trendingRefreshPlan(placed)

            // ----- Assert -----
            result shouldBe TrendingRefreshPlan(
                interval = TrendingRefreshCadence.TwiceDaily.interval,
                periods = setOf(TrendingPeriod.Week, TrendingPeriod.Year),
            )
        }
    }
}
