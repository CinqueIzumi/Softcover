package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import nl.rhaydus.softcover.core.component.chart.YearGridStep
import nl.rhaydus.softcover.core.profile.domain.model.ReadingActivity
import nl.rhaydus.softcover.core.profile.domain.model.ReadingHistory
import nl.rhaydus.softcover.core.profile.domain.model.YearCount
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingSummaryMapperTest {
    private val today = LocalDate(
        year = 2025,
        month = 6,
        day = 10,
    )

    private fun buildReadingHistory(
        books: List<YearCount> = emptyList(),
        pages: List<YearCount> = emptyList(),
    ): ReadingHistory = ReadingHistory(
        booksByYear = books,
        pagesByYear = pages,
    )

    private fun daysAgo(count: Int): LocalDate = today.minus(
        value = count,
        unit = DateTimeUnit.DAY,
    )

    private fun activityOf(vararg pages: Pair<LocalDate, Int>): ReadingActivity = ReadingActivity(
        pagesByDay = pages.toMap(),
        streaks = emptyList(),
    )

    @Nested
    inner class Stats {
        @Test
        fun `keeps every stat loading while the history, the activity or the date is missing`() {
            // ----- Arrange -----
            val cases = listOf(
                Triple<ReadingHistory?, ReadingActivity?, LocalDate?>(
                    null,
                    activityOf(),
                    today,
                ),
                Triple<ReadingHistory?, ReadingActivity?, LocalDate?>(
                    buildReadingHistory(),
                    null,
                    today,
                ),
                Triple<ReadingHistory?, ReadingActivity?, LocalDate?>(
                    buildReadingHistory(),
                    activityOf(),
                    null,
                ),
            )

            // ----- Act -----
            val results = cases.map { (history, activity, date) ->
                readingSummaryUiModel(
                    history = history,
                    activity = activity,
                    today = date,
                )
            }

            // ----- Assert -----
            results.forEach {
                it.stats.map { stat -> stat.stat.isLoading } shouldBe listOf(true, true, true)
                it.stats.map { stat -> stat.stat.value } shouldBe listOf(0.0, 0.0, 0.0)
            }
        }

        @Test
        fun `picks the current year's books, pages and reading days and is not loading`() {
            // ----- Arrange -----
            val history = buildReadingHistory(
                books = listOf(
                    YearCount(
                        year = 2024,
                        count = 9,
                    ),
                    YearCount(
                        year = 2025,
                        count = 4,
                    ),
                ),
                pages = listOf(
                    YearCount(
                        year = 2024,
                        count = 800,
                    ),
                    YearCount(
                        year = 2025,
                        count = 1_200,
                    ),
                ),
            )
            val activity = activityOf(
                today to 10,
                daysAgo(3) to 20,
                LocalDate(
                    year = 2024,
                    month = 12,
                    day = 31,
                ) to 30,
            )

            // ----- Act -----
            val result = readingSummaryUiModel(
                history = history,
                activity = activity,
                today = today,
            )

            // ----- Assert -----
            result.stats.map { it.stat.value } shouldBe listOf(4.0, 1_200.0, 2.0)
            result.stats.map { it.stat.isLoading } shouldBe listOf(false, false, false)
        }

        @Test
        fun `a current year with no entry counts as zero, not as another year`() {
            // ----- Arrange -----
            val history = buildReadingHistory(
                books = listOf(
                    YearCount(
                        year = 2024,
                        count = 9,
                    ),
                ),
                pages = listOf(
                    YearCount(
                        year = 2024,
                        count = 800,
                    ),
                ),
            )

            // ----- Act -----
            val result = readingSummaryUiModel(
                history = history,
                activity = activityOf(),
                today = today,
            )

            // ----- Assert -----
            result.stats.map { it.stat.value } shouldBe listOf(0.0, 0.0, 0.0)
            result.stats.map { it.stat.isLoading } shouldBe listOf(false, false, false)
        }
    }

    @Nested
    inner class RecentDays {
        @Test
        fun `holds 84 days ending today, oldest first`() {
            // ----- Arrange -----
            val activity = activityOf(
                today to 10,
                daysAgo(83) to 10,
            )

            // ----- Act -----
            val result = readingSummaryUiModel(
                history = buildReadingHistory(),
                activity = activity,
                today = today,
            )

            // ----- Assert -----
            result.recentDays.days shouldHaveSize 84
            result.recentDays.days.first() shouldBe YearGridStep.Four
            result.recentDays.days.last() shouldBe YearGridStep.Four
            result.recentDays.days.count { it != null } shouldBe 2
        }

        @Test
        fun `leaves out a day just before the window`() {
            // ----- Arrange -----
            val activity = activityOf(daysAgo(84) to 10)

            // ----- Act -----
            val result = readingSummaryUiModel(
                history = buildReadingHistory(),
                activity = activity,
                today = today,
            )

            // ----- Assert -----
            result.recentDays.days.count { it != null } shouldBe 0
            result.recentDaysCaption shouldBe "The last 12 weeks. You read on 0 of 84 days."
        }

        @Test
        fun `tints by the quartiles of the window and ignores older days`() {
            // ----- Arrange -----
            val inWindow = (0 until 4).map { daysAgo(it) to (it + 1) * 10 }
            val activity = activityOf(
                *inWindow.toTypedArray(),
                daysAgo(200) to 5_000,
            )

            // ----- Act -----
            val result = readingSummaryUiModel(
                history = buildReadingHistory(),
                activity = activity,
                today = today,
            )

            // ----- Assert -----
            result.recentDays.days.takeLast(4) shouldBe listOf(
                YearGridStep.Four,
                YearGridStep.Three,
                YearGridStep.Two,
                YearGridStep.One,
            )
        }

        @Test
        fun `counts the read days in the caption`() {
            // ----- Arrange -----
            val activity = activityOf(
                today to 10,
                daysAgo(1) to 10,
                daysAgo(40) to 10,
            )

            // ----- Act -----
            val result = readingSummaryUiModel(
                history = buildReadingHistory(),
                activity = activity,
                today = today,
            )

            // ----- Assert -----
            result.recentDaysCaption shouldBe "The last 12 weeks. You read on 3 of 84 days."
        }

        @Test
        fun `masks the grid and drops the count while loading`() {
            // ----- Arrange -----
            val activity: ReadingActivity? = null

            // ----- Act -----
            val result = readingSummaryUiModel(
                history = buildReadingHistory(),
                activity = activity,
                today = today,
            )

            // ----- Assert -----
            result.recentDays.isLoading shouldBe true
            result.recentDaysCaption shouldBe "The last 12 weeks."
        }
    }
}
