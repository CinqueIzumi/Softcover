package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.chart.YearGridCell
import nl.rhaydus.softcover.core.component.chart.YearGridStep
import nl.rhaydus.softcover.core.component.chart.YearGridUiModel
import nl.rhaydus.softcover.core.profile.domain.model.DailyPages
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class YearGridMapperTest {
    private fun day(
        year: Int = 2025,
        month: Int,
        day: Int,
    ): LocalDate = LocalDate(
        year = year,
        month = month,
        day = day,
    )

    private fun daily(
        year: Int = 2025,
        vararg pages: Pair<LocalDate, Int>,
    ): DailyPages = DailyPages(
        year = year,
        pages = pages.toMap(),
    )

    private fun YearGridUiModel.cell(date: LocalDate): YearGridCell = months[date.month.ordinal].days[date.day - 1]

    private fun YearGridUiModel.stepOf(date: LocalDate): YearGridStep? = (cell(date) as? YearGridCell.Read)?.step

    private fun dailyOfJanuaryDays(vararg counts: Int): DailyPages = daily(
        year = 2025,
        pages = counts.mapIndexed { index, count ->
            day(
                month = 1,
                day = index + 1,
            ) to count
        }.toTypedArray(),
    )

    @Nested
    inner class Layout {
        @Test
        fun `holds twelve months of the daily year`() {
            // ----- Arrange -----
            val daily = daily(year = 2024)

            // ----- Act -----
            val result = yearGrid(daily)

            // ----- Assert -----
            result.year shouldBe 2024
            result.months shouldHaveSize 12
        }

        @Test
        fun `gives a leap year a 29 day February`() {
            // ----- Arrange -----
            val daily = daily(year = 2024)

            // ----- Act -----
            val result = yearGrid(daily)

            // ----- Assert -----
            result.months[1].days shouldHaveSize 29
            result.months.sumOf { it.days.size } shouldBe 366
        }

        @Test
        fun `gives a common year a 28 day February`() {
            // ----- Arrange -----
            val daily = daily(year = 2025)

            // ----- Act -----
            val result = yearGrid(daily)

            // ----- Assert -----
            result.months[1].days shouldHaveSize 28
            result.months.sumOf { it.days.size } shouldBe 365
        }

        @Test
        fun `leaves days without pages empty`() {
            // ----- Arrange -----
            val daily = daily(
                year = 2025,
                day(
                    month = 4,
                    day = 14,
                ) to 62,
            )

            // ----- Act -----
            val result = yearGrid(daily)

            // ----- Assert -----
            result.cell(
                day(
                    month = 4,
                    day = 15,
                ),
            ) shouldBe YearGridCell.Empty
        }
    }

    @Nested
    inner class Steps {
        @Test
        fun `splits eight distinct days into quartile steps by nearest rank`() {
            // ----- Arrange -----
            val daily = dailyOfJanuaryDays(
                10,
                20,
                30,
                40,
                50,
                60,
                70,
                80,
            )

            // ----- Act -----
            val result = yearGrid(daily)

            // ----- Assert -----
            val steps = (1..8).map {
                result.stepOf(
                    day(
                        month = 1,
                        day = it,
                    ),
                )
            }
            steps shouldBe listOf(
                YearGridStep.One,
                YearGridStep.One,
                YearGridStep.Two,
                YearGridStep.Two,
                YearGridStep.Three,
                YearGridStep.Three,
                YearGridStep.Four,
                YearGridStep.Four,
            )
        }

        @Test
        fun `gives equal page counts the same step`() {
            // ----- Arrange -----
            val daily = dailyOfJanuaryDays(
                5,
                5,
                5,
                5,
                9,
                9,
                9,
                9,
            )

            // ----- Act -----
            val result = yearGrid(daily)

            // ----- Assert -----
            val steps = (1..8).map {
                result.stepOf(
                    day(
                        month = 1,
                        day = it,
                    ),
                )
            }
            steps.take(4).toSet() shouldBe setOf(YearGridStep.One)
            steps.drop(4).toSet() shouldBe setOf(YearGridStep.Three)
        }

        @Test
        fun `tints every read day the darkest step with fewer than four reading days`() {
            // ----- Arrange -----
            val daily = dailyOfJanuaryDays(
                1,
                50,
                400,
            )

            // ----- Act -----
            val result = yearGrid(daily)

            // ----- Assert -----
            val steps = (1..3).map {
                result.stepOf(
                    day(
                        month = 1,
                        day = it,
                    ),
                )
            }
            steps shouldBe List(3) { YearGridStep.Four }
        }

        @Test
        fun `tints every read day the darkest step when all counts are equal`() {
            // ----- Arrange -----
            val daily = dailyOfJanuaryDays(
                30,
                30,
                30,
                30,
                30,
            )

            // ----- Act -----
            val result = yearGrid(daily)

            // ----- Assert -----
            val steps = (1..5).map {
                result.stepOf(
                    day(
                        month = 1,
                        day = it,
                    ),
                )
            }
            steps shouldBe List(5) { YearGridStep.Four }
        }
    }

    @Nested
    inner class ContentDescriptions {
        @Test
        fun `names the day and the plural page count`() {
            // ----- Arrange -----
            val date = day(
                month = 4,
                day = 14,
            )
            val daily = daily(
                year = 2025,
                date to 62,
            )

            // ----- Act -----
            val result = yearGrid(daily)

            // ----- Assert -----
            (result.cell(date) as YearGridCell.Read).contentDescription shouldBe "14 April, 62 pages"
        }

        @Test
        fun `uses the singular for a single page`() {
            // ----- Arrange -----
            val date = day(
                month = 4,
                day = 14,
            )
            val daily = daily(
                year = 2025,
                date to 1,
            )

            // ----- Act -----
            val result = yearGrid(daily)

            // ----- Assert -----
            (result.cell(date) as YearGridCell.Read).contentDescription shouldBe "14 April, 1 page"
        }
    }
}
