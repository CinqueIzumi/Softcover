package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.domain.model.StatsWindow
import nl.rhaydus.softcover.core.profile.domain.model.MonthCount
import nl.rhaydus.softcover.core.profile.domain.model.MonthlyPages
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MonthlyPagesMapperTest {
    private val october = LocalDate(
        year = 2026,
        month = 10,
        day = 23,
    )

    private fun year(
        year: Int,
        counts: Map<Int, Int>,
    ) = MonthlyPages.Span(
        months = (1..12).map {
            MonthCount(
                year = year,
                month = it,
                count = counts[it] ?: 0,
            )
        },
    )

    private fun months(
        vararg months: Triple<Int, Int, Int>,
    ) = MonthlyPages.Span(
        months = months.map {
            MonthCount(
                year = it.first,
                month = it.second,
                count = it.third,
            )
        },
    )

    private fun allTime(
        counts: Map<Int, Int>,
    ) = MonthlyPages.AcrossYears(pages = (1..12).map { counts[it] ?: 0 })

    private fun chart(
        pages: MonthlyPages,
        scope: StatsScope = StatsScope.CalendarYear(2026),
        today: LocalDate = october,
    ) = monthlyPagesChartUiModel(
        monthlyPages = pages,
        scope = scope,
        today = today,
    )

    private fun header(
        pages: MonthlyPages,
        scope: StatsScope = StatsScope.CalendarYear(2026),
        today: LocalDate = october,
    ) = monthlyPagesHeader(
        monthlyPages = pages,
        scope = scope,
        today = today,
    )

    private fun descriptionOf(
        pages: MonthlyPages,
        scope: StatsScope = StatsScope.CalendarYear(2026),
        today: LocalDate = october,
    ): String? = header(
        pages = pages,
        scope = scope,
        today = today,
    ).description?.joinToString(separator = "") { it.text }

    @Nested
    inner class Tones {
        @Test
        fun `ranks the busiest Lead, the next two Second, the rest Third and zero Fifth`() {
            // ----- Arrange -----
            val pages = year(
                year = 2026,
                counts = mapOf(
                    3 to 1480,
                    4 to 900,
                    5 to 700,
                    6 to 300,
                    7 to 200,
                ),
            )

            // ----- Act -----
            val result = chart(pages)

            // ----- Assert -----
            result.columns.map { it.tone } shouldBe listOf(
                ChartTone.Fifth,
                ChartTone.Fifth,
                ChartTone.Lead,
                ChartTone.Second,
                ChartTone.Second,
                ChartTone.Third,
                ChartTone.Third,
                ChartTone.Fifth,
                ChartTone.Fifth,
                ChartTone.Fifth,
                ChartTone.Fifth,
                ChartTone.Fifth,
            )
        }

        @Test
        fun `shows a value only on the Lead column`() {
            // ----- Arrange -----
            val pages = year(
                year = 2026,
                counts = mapOf(
                    3 to 1480,
                    4 to 900,
                ),
            )

            // ----- Act -----
            val result = chart(pages)

            // ----- Assert -----
            result.columns.map { it.value } shouldBe List(12) { if (it == 2) "1,480" else null }
        }

        @Test
        fun `gives a tie to the earliest month in chart order`() {
            // ----- Arrange -----
            val pages = months(
                Triple(
                    2025,
                    10,
                    500,
                ),
                Triple(
                    2025,
                    11,
                    0,
                ),
                Triple(
                    2025,
                    12,
                    0,
                ),
                Triple(
                    2026,
                    1,
                    500,
                ),
            )
            val scope = StatsScope.Rolling(StatsWindow.LAST_12_MONTHS)

            // ----- Act -----
            val result = chart(
                pages = pages,
                scope = scope,
                today = LocalDate(
                    year = 2026,
                    month = 1,
                    day = 15,
                ),
            )

            // ----- Assert -----
            result.columns.map { it.tone } shouldBe listOf(
                ChartTone.Lead,
                ChartTone.Fifth,
                ChartTone.Fifth,
                ChartTone.Second,
            )
        }
    }

    @Nested
    inner class Headline {
        @Test
        fun `names the busiest month`() {
            // ----- Arrange -----
            val pages = year(
                year = 2026,
                counts = mapOf(
                    3 to 1480,
                    4 to 100,
                ),
            )

            // ----- Act -----
            val result = header(pages)

            // ----- Assert -----
            result.headlineText shouldBe "A March to remember"
        }

        @Test
        fun `uses An before April, August and October`() {
            // ----- Arrange -----
            val busiestMonths = listOf(
                4,
                8,
                10,
            )

            // ----- Act -----
            val result = busiestMonths.map {
                header(
                    year(
                        year = 2026,
                        counts = mapOf(it to 100),
                    ),
                ).headlineText
            }

            // ----- Assert -----
            result shouldBe listOf(
                "An April to remember",
                "An August to remember",
                "An October to remember",
            )
        }

        @Test
        fun `falls back to A year in pages when no pages were read`() {
            // ----- Arrange -----
            val pages = year(
                year = 2026,
                counts = emptyMap(),
            )

            // ----- Act -----
            val result = header(pages)

            // ----- Assert -----
            result.headlineText shouldBe "A year in pages"
        }
    }

    @Nested
    inner class Description {
        @Test
        fun `names the busiest month and its pages`() {
            // ----- Arrange -----
            val pages = year(
                year = 2026,
                counts = mapOf(3 to 1480),
            )

            // ----- Act -----
            val result = descriptionOf(
                pages = pages,
                today = LocalDate(
                    year = 2026,
                    month = 12,
                    day = 5,
                ),
            )

            // ----- Assert -----
            result shouldBe "Your busiest month was March, at 1,480 pages."
        }

        @Test
        fun `adds the year when the window spans two years`() {
            // ----- Arrange -----
            val pages = months(
                Triple(
                    2025,
                    12,
                    100,
                ),
                Triple(
                    2026,
                    1,
                    200,
                ),
                Triple(
                    2026,
                    3,
                    1480,
                ),
            )
            val scope = StatsScope.Rolling(StatsWindow.LAST_12_MONTHS)

            // ----- Act -----
            val result = descriptionOf(
                pages = pages,
                scope = scope,
                today = LocalDate(
                    year = 2026,
                    month = 3,
                    day = 20,
                ),
            )

            // ----- Assert -----
            result shouldBe "Your busiest month was March 2026, at 1,480 pages."
        }

        @Test
        fun `says across every year for all time`() {
            // ----- Arrange -----
            val pages = allTime(mapOf(3 to 4210))

            // ----- Act -----
            val result = descriptionOf(
                pages = pages,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result shouldBe "Across every year, your busiest month is March, at 4,210 pages."
        }

        @Test
        fun `names the one remaining month of the current year`() {
            // ----- Arrange -----
            val pages = year(
                year = 2026,
                counts = mapOf(3 to 1480),
            )

            // ----- Act -----
            val result = descriptionOf(
                pages = pages,
                today = LocalDate(
                    year = 2026,
                    month = 11,
                    day = 15,
                ),
            )

            // ----- Assert -----
            result shouldBe "Your busiest month was March, at 1,480 pages. December is still to come."
        }

        @Test
        fun `names two remaining months with and`() {
            // ----- Arrange -----
            val pages = year(
                year = 2026,
                counts = mapOf(3 to 1480),
            )

            // ----- Act -----
            val result = descriptionOf(pages)

            // ----- Assert -----
            result shouldBe "Your busiest month was March, at 1,480 pages. November and December are still to come."
        }

        @Test
        fun `spans three or more remaining months with to`() {
            // ----- Arrange -----
            val pages = year(
                year = 2026,
                counts = mapOf(3 to 1480),
            )

            // ----- Act -----
            val result = descriptionOf(
                pages = pages,
                today = LocalDate(
                    year = 2026,
                    month = 3,
                    day = 31,
                ),
            )

            // ----- Assert -----
            result shouldBe "Your busiest month was March, at 1,480 pages. April to December are still to come."
        }

        @Test
        fun `has no tail in December`() {
            // ----- Arrange -----
            val pages = year(
                year = 2026,
                counts = mapOf(3 to 1480),
            )

            // ----- Act -----
            val result = descriptionOf(
                pages = pages,
                today = LocalDate(
                    year = 2026,
                    month = 12,
                    day = 5,
                ),
            )

            // ----- Assert -----
            result shouldBe "Your busiest month was March, at 1,480 pages."
        }

        @Test
        fun `has no tail for a past year`() {
            // ----- Arrange -----
            val pages = year(
                year = 2025,
                counts = mapOf(3 to 1480),
            )

            // ----- Act -----
            val result = descriptionOf(
                pages = pages,
                scope = StatsScope.CalendarYear(2025),
            )

            // ----- Assert -----
            result shouldBe "Your busiest month was March, at 1,480 pages."
        }

        @Test
        fun `leaves a future month that has pages out of the tail`() {
            // ----- Arrange -----
            val pages = year(
                year = 2026,
                counts = mapOf(
                    3 to 1480,
                    11 to 120,
                ),
            )

            // ----- Act -----
            val result = descriptionOf(pages)

            // ----- Assert -----
            result shouldBe "Your busiest month was March, at 1,480 pages. December is still to come."
        }

        @Test
        fun `uses the singular for a single page`() {
            // ----- Arrange -----
            val pages = year(
                year = 2026,
                counts = mapOf(3 to 1),
            )

            // ----- Act -----
            val result = descriptionOf(
                pages = pages,
                today = LocalDate(
                    year = 2026,
                    month = 12,
                    day = 5,
                ),
            )

            // ----- Assert -----
            result shouldBe "Your busiest month was March, at 1 page."
        }

        @Test
        fun `says no pages were read in the scope when empty`() {
            // ----- Arrange -----
            val pages = months(
                Triple(
                    2026,
                    9,
                    0,
                ),
                Triple(
                    2026,
                    10,
                    0,
                ),
            )
            val scope = StatsScope.Rolling(StatsWindow.LAST_30_DAYS)

            // ----- Act -----
            val result = descriptionOf(
                pages = pages,
                scope = scope,
            )

            // ----- Assert -----
            result shouldBe "No pages read in the last 30 days."
        }

        @Test
        fun `invites the reader to update progress when all time is empty`() {
            // ----- Arrange -----
            val pages = allTime(emptyMap())

            // ----- Act -----
            val result = descriptionOf(
                pages = pages,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result shouldBe "Update your progress and the months start filling in."
        }
    }

    @Nested
    inner class Labels {
        @Test
        fun `labels every third column from the first`() {
            // ----- Arrange -----
            val pages = year(
                year = 2026,
                counts = mapOf(3 to 10),
            )

            // ----- Act -----
            val result = chart(pages)

            // ----- Assert -----
            result.columns.map { it.label } shouldBe listOf(
                "Jan",
                null,
                null,
                "Apr",
                null,
                null,
                "Jul",
                null,
                null,
                "Oct",
                null,
                null,
            )
        }

        @Test
        fun `labels every column when there are three or fewer`() {
            // ----- Arrange -----
            val pages = months(
                Triple(
                    2026,
                    1,
                    0,
                ),
                Triple(
                    2026,
                    2,
                    5,
                ),
                Triple(
                    2026,
                    3,
                    0,
                ),
            )
            val scope = StatsScope.Rolling(StatsWindow.LAST_30_DAYS)

            // ----- Act -----
            val result = chart(
                pages = pages,
                scope = scope,
                today = LocalDate(
                    year = 2026,
                    month = 3,
                    day = 1,
                ),
            )

            // ----- Assert -----
            result.columns.map { it.label } shouldBe listOf(
                "Jan",
                "Feb",
                "Mar",
            )
        }
    }

    @Nested
    inner class Readouts {
        @Test
        fun `reads a month with its year and pages`() {
            // ----- Arrange -----
            val pages = year(
                year = 2026,
                counts = mapOf(3 to 1480),
            )

            // ----- Act -----
            val result = chart(pages)

            // ----- Assert -----
            result.columns[2].contentDescription shouldBe "March 2026, 1,480 pages"
        }

        @Test
        fun `uses the singular for one page`() {
            // ----- Arrange -----
            val pages = year(
                year = 2026,
                counts = mapOf(3 to 1),
            )

            // ----- Act -----
            val result = chart(pages)

            // ----- Assert -----
            result.columns[2].contentDescription shouldBe "March 2026, 1 page"
        }

        @Test
        fun `starts a partial first month from its start day`() {
            // ----- Arrange -----
            val pages = months(
                Triple(
                    2025,
                    10,
                    412,
                ),
                Triple(
                    2025,
                    11,
                    0,
                ),
            )
            val scope = StatsScope.Rolling(StatsWindow.LAST_12_MONTHS)

            // ----- Act -----
            val result = chart(
                pages = pages,
                scope = scope,
                today = LocalDate(
                    year = 2026,
                    month = 10,
                    day = 26,
                ),
            )

            // ----- Assert -----
            result.columns.first().contentDescription shouldBe "From 27 October 2025, 412 pages"
        }

        @Test
        fun `reads an empty future month as still to come`() {
            // ----- Arrange -----
            val pages = year(
                year = 2026,
                counts = mapOf(3 to 1480),
            )

            // ----- Act -----
            val result = chart(pages)

            // ----- Assert -----
            result.columns[10].contentDescription shouldBe "November 2026, still to come"
        }

        @Test
        fun `reads an all-time month across every year`() {
            // ----- Arrange -----
            val pages = allTime(mapOf(3 to 4210))

            // ----- Act -----
            val result = chart(
                pages = pages,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.columns[2].contentDescription shouldBe "March, 4,210 pages across every year"
        }
    }

    @Nested
    inner class MonthSetKey {
        @Test
        fun `is shared by two calendar years`() {
            // ----- Arrange -----
            val first = year(
                year = 2025,
                counts = emptyMap(),
            )
            val second = year(
                year = 2026,
                counts = mapOf(3 to 10),
            )

            // ----- Act -----
            val result = monthlyPagesMonthSetKey(first)

            // ----- Assert -----
            result shouldBe monthlyPagesMonthSetKey(second)
        }

        @Test
        fun `is shared by a calendar year and all time`() {
            // ----- Arrange -----
            val calendarYear = year(
                year = 2026,
                counts = emptyMap(),
            )
            val allTime = allTime(emptyMap())

            // ----- Act -----
            val result = monthlyPagesMonthSetKey(calendarYear)

            // ----- Assert -----
            result shouldBe monthlyPagesMonthSetKey(allTime)
        }

        @Test
        fun `differs for a thirteen-month window`() {
            // ----- Arrange -----
            val window = MonthlyPages.Span(
                months = (0..12).map {
                    MonthCount(
                        year = 2025 + (9 + it) / 12,
                        month = (9 + it) % 12 + 1,
                        count = 0,
                    )
                },
            )
            val calendarYear = year(
                year = 2026,
                counts = emptyMap(),
            )

            // ----- Act -----
            val result = monthlyPagesMonthSetKey(window)

            // ----- Assert -----
            result shouldNotBe monthlyPagesMonthSetKey(calendarYear)
        }

        @Test
        fun `is null while loading`() {
            // ----- Arrange -----
            val pages: MonthlyPages? = null

            // ----- Act -----
            val result = monthlyPagesMonthSetKey(pages)

            // ----- Assert -----
            result.shouldBeNull()
        }
    }
}
