package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.chart.YearGridCell
import nl.rhaydus.softcover.core.component.chart.YearGridStep
import nl.rhaydus.softcover.core.component.chart.YearGridUiModel
import nl.rhaydus.softcover.core.profile.domain.model.ReadingActivity
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

    private fun daily(vararg pages: Pair<LocalDate, Int>): ReadingActivity = ReadingActivity(
        pagesByDay = pages.toMap(),
        streaks = emptyList(),
    )

    private fun YearGridUiModel.cell(date: LocalDate): YearGridCell = months[date.month.ordinal].days[date.day - 1]

    private fun YearGridUiModel.stepOf(date: LocalDate): YearGridStep? = (cell(date) as? YearGridCell.Read)?.step

    @Nested
    inner class Layout {
        @Test
        fun `holds twelve months of the daily year`() {
            // ----- Arrange -----
            val daily = daily()

            // ----- Act -----
            val result = yearGrid(
                activity = daily,
                year = 2024,
            )

            // ----- Assert -----
            result.year shouldBe 2024
            result.months shouldHaveSize 12
        }

        @Test
        fun `gives a leap year a 29 day February`() {
            // ----- Arrange -----
            val daily = daily()

            // ----- Act -----
            val result = yearGrid(
                activity = daily,
                year = 2024,
            )

            // ----- Assert -----
            result.months[1].days shouldHaveSize 29
            result.months.sumOf { it.days.size } shouldBe 366
        }

        @Test
        fun `gives a common year a 28 day February`() {
            // ----- Arrange -----
            val daily = daily()

            // ----- Act -----
            val result = yearGrid(
                activity = daily,
                year = 2025,
            )

            // ----- Assert -----
            result.months[1].days shouldHaveSize 28
            result.months.sumOf { it.days.size } shouldBe 365
        }

        @Test
        fun `leaves days without pages empty`() {
            // ----- Arrange -----
            val daily = daily(
                day(
                    month = 4,
                    day = 14,
                ) to 62,
            )

            // ----- Act -----
            val result = yearGrid(
                activity = daily,
                year = 2025,
            )

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
    inner class YearFilter {
        @Test
        fun `ignores days outside the year, in both the cells and the quartiles`() {
            // ----- Arrange -----
            val inYear = daily(
                day(
                    month = 1,
                    day = 1,
                ) to 10,
                day(
                    month = 1,
                    day = 2,
                ) to 20,
                day(
                    month = 1,
                    day = 3,
                ) to 30,
                day(
                    month = 1,
                    day = 4,
                ) to 40,
                day(
                    year = 2024,
                    month = 1,
                    day = 5,
                ) to 5_000,
            )

            // ----- Act -----
            val result = yearGrid(
                activity = inYear,
                year = 2025,
            )

            // ----- Assert -----
            result.months.sumOf { month -> month.days.count { it is YearGridCell.Read } } shouldBe 4
            result.stepOf(
                day(
                    month = 1,
                    day = 4,
                ),
            ) shouldBe YearGridStep.Four
            result.stepOf(
                day(
                    month = 1,
                    day = 1,
                ),
            ) shouldBe YearGridStep.One
        }

        @Test
        fun `is empty when every day falls outside the year`() {
            // ----- Arrange -----
            val outside = daily(
                day(
                    year = 2024,
                    month = 6,
                    day = 1,
                ) to 40,
            )

            // ----- Act -----
            val result = yearGrid(
                activity = outside,
                year = 2025,
            )

            // ----- Assert -----
            result.months.sumOf { month -> month.days.count { it is YearGridCell.Read } } shouldBe 0
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
                date to 62,
            )

            // ----- Act -----
            val result = yearGrid(
                activity = daily,
                year = 2025,
            )

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
                date to 1,
            )

            // ----- Act -----
            val result = yearGrid(
                activity = daily,
                year = 2025,
            )

            // ----- Assert -----
            (result.cell(date) as YearGridCell.Read).contentDescription shouldBe "14 April, 1 page"
        }
    }
}
