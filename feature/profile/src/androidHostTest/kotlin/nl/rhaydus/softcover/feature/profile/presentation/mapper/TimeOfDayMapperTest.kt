package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.chart.ColumnChartUiModel
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.domain.model.StatsWindow
import nl.rhaydus.softcover.core.profile.domain.model.HourlyPages
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class TimeOfDayMapperTest {
    private fun hourly(
        vararg pages: Pair<Int, Int>,
    ): HourlyPages {
        val hours = MutableList(24) { 0 }
        pages.forEach { hours[it.first] = it.second }

        return HourlyPages(hours)
    }

    private fun chart(
        hourly: HourlyPages?,
    ): ColumnChartUiModel = timeOfDayChartUiModel(hourly)

    @Nested
    inner class ChartFolding {
        @Test
        fun `labels eight columns from 06 around the clock`() {
            // ----- Arrange -----
            val hourly = hourly()

            // ----- Act -----
            val result = chart(hourly)

            // ----- Assert -----
            result.columns.map { it.label } shouldBe listOf("06", "09", "12", "15", "18", "21", "00", "03")
        }

        @Test
        fun `folds hours 6, 7 and 8 into the first column`() {
            // ----- Arrange -----
            val hourly = hourly(
                6 to 10,
                7 to 10,
                8 to 10,
                9 to 10,
            )

            // ----- Act -----
            val result = chart(hourly)

            // ----- Assert -----
            result.columns.map { it.value } shouldBe listOf("75%", "25%", "0%", "0%", "0%", "0%", "0%", "0%")
        }

        @Test
        fun `folds the small hours 0, 1 and 2 into the seventh column`() {
            // ----- Arrange -----
            val hourly = hourly(
                0 to 5,
                1 to 5,
                2 to 5,
            )

            // ----- Act -----
            val result = chart(hourly)

            // ----- Assert -----
            result.columns[6].label shouldBe "00"
            result.columns[6].value shouldBe "100%"
        }

        @Test
        fun `folds hours 3, 4 and 5 into the last column`() {
            // ----- Arrange -----
            val hourly = hourly(
                3 to 4,
                4 to 4,
                5 to 4,
            )

            // ----- Act -----
            val result = chart(hourly)

            // ----- Assert -----
            result.columns[7].label shouldBe "03"
            result.columns[7].value shouldBe "100%"
        }

        @Test
        fun `scales the tallest column to a full fraction`() {
            // ----- Arrange -----
            val hourly = hourly(
                6 to 20,
                12 to 10,
            )

            // ----- Act -----
            val result = chart(hourly)

            // ----- Assert -----
            result.columns.map { it.fraction } shouldBe listOf(1f, 0f, 0.5f, 0f, 0f, 0f, 0f, 0f)
        }

        @Test
        fun `shows the loading chart for null hourly pages`() {
            // ----- Arrange -----
            val hourly: HourlyPages? = null

            // ----- Act -----
            val result = chart(hourly)

            // ----- Assert -----
            result.isLoading shouldBe true
        }
    }

    @Nested
    inner class ShareValues {
        @Test
        fun `rounds a share to a whole percent`() {
            // ----- Arrange -----
            val hourly = hourly(
                21 to 38,
                6 to 62,
            )

            // ----- Act -----
            val result = chart(hourly)

            // ----- Assert -----
            result.columns[5].value shouldBe "38%"
        }

        @Test
        fun `shows an empty block as 0 percent`() {
            // ----- Arrange -----
            val hourly = hourly(6 to 10)

            // ----- Act -----
            val result = chart(hourly)

            // ----- Assert -----
            result.columns[1].value shouldBe "0%"
        }

        @Test
        fun `shows a tiny non-zero share as under 1 percent`() {
            // ----- Arrange -----
            val hourly = hourly(
                6 to 1000,
                9 to 1,
            )

            // ----- Act -----
            val result = chart(hourly)

            // ----- Assert -----
            result.columns[1].value shouldBe "<1%"
        }

        @Test
        fun `shows a dominant share that rounds to 100 beside a tiny one as over 99 percent`() {
            // ----- Arrange -----
            val hourly = hourly(
                6 to 1000,
                9 to 1,
            )

            // ----- Act -----
            val result = chart(hourly)

            // ----- Assert -----
            result.columns[0].value shouldBe ">99%"
        }

        @Test
        fun `shows a block holding every page as 100 percent`() {
            // ----- Arrange -----
            val hourly = hourly(12 to 7)

            // ----- Act -----
            val result = chart(hourly)

            // ----- Assert -----
            result.columns[2].value shouldBe "100%"
        }
    }

    @Nested
    inner class Readouts {
        @Test
        fun `spells out the range and share of a block`() {
            // ----- Arrange -----
            val hourly = hourly(
                21 to 38,
                6 to 62,
            )

            // ----- Act -----
            val result = chart(hourly)

            // ----- Assert -----
            result.columns[5].contentDescription shouldBe "21:00 to 00:00, 38% of your pages"
        }

        @Test
        fun `says no pages for an empty block`() {
            // ----- Arrange -----
            val hourly = hourly(6 to 10)

            // ----- Act -----
            val result = chart(hourly)

            // ----- Assert -----
            result.columns[7].contentDescription shouldBe "03:00 to 06:00, no pages"
        }

        @Test
        fun `says less than 1 percent for a tiny share`() {
            // ----- Arrange -----
            val hourly = hourly(
                6 to 1000,
                9 to 1,
            )

            // ----- Act -----
            val result = chart(hourly)

            // ----- Assert -----
            result.columns[1].contentDescription shouldBe "09:00 to 12:00, less than 1% of your pages"
        }

        @Test
        fun `says more than 99 percent for a dominant share`() {
            // ----- Arrange -----
            val hourly = hourly(
                6 to 1000,
                9 to 1,
            )

            // ----- Act -----
            val result = chart(hourly)

            // ----- Assert -----
            result.columns[0].contentDescription shouldBe "06:00 to 09:00, more than 99% of your pages"
        }
    }

    @Nested
    inner class CaptionMapping {
        @ParameterizedTest
        @CsvSource(
            "6,'early, before 09:00'",
            "9,in the morning",
            "12,around midday",
            "15,in the afternoon",
            "18,in the early evening",
            "21,after 21:00",
            "0,after midnight",
            "3,in the small hours",
        )
        fun `names when you read most by the peak block`(
            hour: Int,
            phrase: String,
        ) {
            // ----- Arrange -----
            val hourly = hourly(hour to 10)

            // ----- Act -----
            val result = timeOfDayCaption(
                hourly = hourly,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result shouldBe "Your pages by time of day. You read most $phrase."
        }

        @Test
        fun `picks the earliest block from 06 on a tie`() {
            // ----- Arrange -----
            val hourly = hourly(
                0 to 5,
                15 to 5,
            )

            // ----- Act -----
            val result = timeOfDayCaption(
                hourly = hourly,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result shouldBe "Your pages by time of day. You read most in the afternoon."
        }

        @Test
        fun `says no pages were read in a scoped range`() {
            // ----- Arrange -----
            val hourly = hourly()

            // ----- Act -----
            val result = timeOfDayCaption(
                hourly = hourly,
                scope = StatsScope.Rolling(StatsWindow.LAST_30_DAYS),
            )

            // ----- Assert -----
            result shouldBe "No pages read in the last 30 days."
        }

        @Test
        fun `says no pages were read in a calendar year`() {
            // ----- Arrange -----
            val hourly = hourly()

            // ----- Act -----
            val result = timeOfDayCaption(
                hourly = hourly,
                scope = StatsScope.CalendarYear(2024),
            )

            // ----- Assert -----
            result shouldBe "No pages read in 2024."
        }

        @Test
        fun `invites progress updates when all time is empty`() {
            // ----- Arrange -----
            val hourly = hourly()

            // ----- Act -----
            val result = timeOfDayCaption(
                hourly = hourly,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result shouldBe "Update your progress and your hours start to show."
        }

        @Test
        fun `matches the loading caption for null hourly pages in any scope`() {
            // ----- Arrange -----
            val hourly: HourlyPages? = null

            // ----- Act -----
            val result = timeOfDayCaption(
                hourly = hourly,
                scope = StatsScope.CalendarYear(2024),
            )

            // ----- Assert -----
            result shouldBe loadingTimeOfDayCaption()
        }
    }

    @Nested
    inner class PagesPresence {
        @Test
        fun `is false for null hourly pages`() {
            // ----- Arrange -----
            val hourly: HourlyPages? = null

            // ----- Act -----
            val result = hasTimeOfDayPages(hourly)

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is false when every hour is zero`() {
            // ----- Arrange -----
            val hourly = hourly()

            // ----- Act -----
            val result = hasTimeOfDayPages(hourly)

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is true when any hour has pages`() {
            // ----- Arrange -----
            val hourly = hourly(23 to 1)

            // ----- Act -----
            val result = hasTimeOfDayPages(hourly)

            // ----- Assert -----
            result shouldBe true
        }
    }
}
