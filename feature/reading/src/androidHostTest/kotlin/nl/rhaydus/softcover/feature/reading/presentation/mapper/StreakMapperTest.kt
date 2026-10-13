package nl.rhaydus.softcover.feature.reading.presentation.mapper

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.statistic.StatNumberSuffix
import nl.rhaydus.softcover.core.domain.model.ReadingDayActivity
import nl.rhaydus.softcover.feature.reading.presentation.state.StreakDayRowUiModel
import nl.rhaydus.softcover.feature.reading.presentation.state.StreakDayUiModel
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StreakMapperTest {
    private fun day(
        dayOfMonth: Int,
        didRead: Boolean,
    ): ReadingDayActivity = ReadingDayActivity(
        date = LocalDate(
            2026,
            5,
            dayOfMonth,
        ),
        didRead = didRead,
    )

    @Nested
    inner class StreakStripFor {
        @Test
        fun `lists days most recent first and marks only the last entry as today`() {
            // ----- Arrange -----
            val activity = listOf(
                day(
                    dayOfMonth = 29,
                    didRead = true,
                ),
                day(
                    dayOfMonth = 30,
                    didRead = false,
                ),
                day(
                    dayOfMonth = 31,
                    didRead = true,
                ),
            )

            // ----- Act -----
            val result = streakStripFor(activity = activity)

            // ----- Assert -----
            result.days shouldBe listOf(
                StreakDayUiModel(
                    didRead = true,
                    isToday = true,
                ),
                StreakDayUiModel(
                    didRead = false,
                    isToday = false,
                ),
                StreakDayUiModel(
                    didRead = true,
                    isToday = false,
                ),
            )
        }

        @Test
        fun `labels a single running day in the singular`() {
            // ----- Arrange -----
            val activity = listOf(
                day(
                    dayOfMonth = 30,
                    didRead = false,
                ),
                day(
                    dayOfMonth = 31,
                    didRead = true,
                ),
            )

            // ----- Act -----
            val result = streakStripFor(activity = activity)

            // ----- Assert -----
            val running = result.running.shouldNotBeNull()
            running.value shouldBe 1.0
            running.suffix shouldBe StatNumberSuffix.Plural(
                one = " day running",
                other = " days running",
            )
        }

        @Test
        fun `labels several running days in the plural and stops at the first missed day`() {
            // ----- Arrange -----
            val activity = listOf(
                day(
                    dayOfMonth = 28,
                    didRead = true,
                ),
                day(
                    dayOfMonth = 29,
                    didRead = false,
                ),
                day(
                    dayOfMonth = 30,
                    didRead = true,
                ),
                day(
                    dayOfMonth = 31,
                    didRead = true,
                ),
            )

            // ----- Act -----
            val result = streakStripFor(activity = activity)

            // ----- Assert -----
            result.running.shouldNotBeNull().value shouldBe 2.0
        }

        @Test
        fun `has no running label when today was missed`() {
            // ----- Arrange -----
            val activity = listOf(
                day(
                    dayOfMonth = 30,
                    didRead = true,
                ),
                day(
                    dayOfMonth = 31,
                    didRead = false,
                ),
            )

            // ----- Act -----
            val result = streakStripFor(activity = activity)

            // ----- Assert -----
            result.running.shouldBeNull()
        }

        @Test
        fun `is empty with no running label for no activity`() {
            // ----- Arrange -----
            val activity = emptyList<ReadingDayActivity>()

            // ----- Act -----
            val result = streakStripFor(activity = activity)

            // ----- Assert -----
            result.days.shouldBeEmpty()
            result.running.shouldBeNull()
        }

        @Test
        fun `passes isLoading through`() {
            // ----- Arrange -----
            val activity = emptyList<ReadingDayActivity>()

            // ----- Act -----
            val loading = streakStripFor(
                activity = activity,
                isLoading = true,
            )
            val settled = streakStripFor(activity = activity)

            // ----- Assert -----
            loading.isLoading shouldBe true
            settled.isLoading shouldBe false
        }

        @Test
        fun `passes isLoading through to the running figure`() {
            // ----- Arrange -----
            val activity = listOf(
                day(
                    dayOfMonth = 31,
                    didRead = true,
                ),
            )

            // ----- Act -----
            val loading = streakStripFor(
                activity = activity,
                isLoading = true,
            )
            val settled = streakStripFor(activity = activity)

            // ----- Assert -----
            loading.running.shouldNotBeNull().isLoading shouldBe true
            settled.running.shouldNotBeNull().isLoading shouldBe false
        }
    }

    @Nested
    inner class StreakSheetFor {
        @Test
        fun `labels rows Today, Yesterday and a formatted date, most recent first`() {
            // ----- Arrange -----
            val activity = listOf(
                day(
                    dayOfMonth = 29,
                    didRead = false,
                ),
                day(
                    dayOfMonth = 30,
                    didRead = true,
                ),
                day(
                    dayOfMonth = 31,
                    didRead = true,
                ),
            )

            // ----- Act -----
            val result = streakSheetFor(activity = activity)

            // ----- Assert -----
            result.rows shouldBe listOf(
                StreakDayRowUiModel(
                    label = "Today",
                    didRead = true,
                ),
                StreakDayRowUiModel(
                    label = "Yesterday",
                    didRead = true,
                ),
                StreakDayRowUiModel(
                    label = "Fri 29 May",
                    didRead = false,
                ),
            )
        }

        @Test
        fun `has no rows for no activity`() {
            // ----- Arrange -----
            val activity = emptyList<ReadingDayActivity>()

            // ----- Act -----
            val result = streakSheetFor(activity = activity)

            // ----- Assert -----
            result.rows.shouldBeEmpty()
        }
    }
}
