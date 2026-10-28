package nl.rhaydus.softcover.feature.reading.presentation.mapper

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.profile.domain.model.ReadingActivity
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStreak
import nl.rhaydus.softcover.feature.reading.presentation.state.StreakDayRowUiModel
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StreakMapperTest {
    private val today = LocalDate(
        2026,
        5,
        31,
    )

    private fun may(dayOfMonth: Int): LocalDate = LocalDate(
        2026,
        5,
        dayOfMonth,
    )

    private fun streak(
        from: Int,
        to: Int = from,
    ): ReadingStreak = ReadingStreak(
        start = may(from),
        end = may(to),
    )

    private fun activityOf(vararg streaks: ReadingStreak): ReadingActivity = ReadingActivity(
        pagesByDay = streaks.flatMap { streak ->
            (streak.start.day..streak.end.day).map { may(it) to 10 }
        }.toMap(),
        streaks = streaks.toList(),
    )

    @Nested
    inner class StreakStripFor {
        @Test
        fun `lists a 21 day window newest first and marks only the first entry as today`() {
            // ----- Arrange -----
            val activity = activityOf(
                streak(29),
                streak(31),
            )

            // ----- Act -----
            val result = streakStripFor(
                activity = activity,
                today = today,
            )

            // ----- Assert -----
            result.days shouldHaveSize 21
            result.days.take(3).map { it.didRead } shouldBe listOf(
                true,
                false,
                true,
            )
            result.days.map { it.isToday } shouldBe List(21) { it == 0 }
            result.days.drop(3).none { it.didRead } shouldBe true
        }

        @Test
        fun `reaches back 20 days before today`() {
            // ----- Arrange -----
            val activity = activityOf(
                streak(11),
            )

            // ----- Act -----
            val result = streakStripFor(
                activity = activity,
                today = today,
            )

            // ----- Assert -----
            result.days.last().didRead shouldBe true
            result.days.dropLast(1).none { it.didRead } shouldBe true
        }

        @Test
        fun `ignores a day older than the window`() {
            // ----- Arrange -----
            val activity = activityOf(
                streak(10),
            )

            // ----- Act -----
            val result = streakStripFor(
                activity = activity,
                today = today,
            )

            // ----- Assert -----
            result.days.none { it.didRead } shouldBe true
        }

        @Test
        fun `counts the whole current run and stops at the first missed day`() {
            // ----- Arrange -----
            val activity = activityOf(
                streak(27),
                streak(
                    29,
                    31,
                ),
            )

            // ----- Act -----
            val result = streakStripFor(
                activity = activity,
                today = today,
            )

            // ----- Assert -----
            result.running.shouldNotBeNull().value shouldBe 3.0
        }

        @Test
        fun `has no running label when the last read day is before yesterday`() {
            // ----- Arrange -----
            val activity = activityOf(
                streak(
                    27,
                    28,
                ),
            )

            // ----- Act -----
            val result = streakStripFor(
                activity = activity,
                today = today,
            )

            // ----- Assert -----
            result.running.shouldBeNull()
        }

        @Test
        fun `is empty with no running label when the activity or today is not known yet`() {
            // ----- Arrange -----
            val activity = activityOf(streak(31))

            // ----- Act -----
            val withoutActivity = streakStripFor(
                activity = null,
                today = today,
            )
            val withoutToday = streakStripFor(
                activity = activity,
                today = null,
            )

            // ----- Assert -----
            withoutActivity.days.shouldBeEmpty()
            withoutActivity.running.shouldBeNull()
            withoutToday.days.shouldBeEmpty()
            withoutToday.running.shouldBeNull()
        }
    }

    @Nested
    inner class StreakSheetFor {
        @Test
        fun `labels rows Today, Yesterday and a formatted date, newest first`() {
            // ----- Arrange -----
            val activity = activityOf(
                streak(
                    30,
                    31,
                ),
            )

            // ----- Act -----
            val result = streakSheetFor(
                activity = activity,
                today = today,
            )

            // ----- Assert -----
            result.rows shouldHaveSize 21
            result.rows.take(3) shouldBe listOf(
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
        fun `has no rows while the activity or today is unknown`() {
            // ----- Arrange -----
            val activity = activityOf(
                streak(31),
            )

            // ----- Act -----
            val withoutActivity = streakSheetFor(
                activity = null,
                today = today,
            )
            val withoutToday = streakSheetFor(
                activity = activity,
                today = null,
            )

            // ----- Assert -----
            withoutActivity.rows.shouldBeEmpty()
            withoutToday.rows.shouldBeEmpty()
        }

        @Test
        fun `builds the scaffold from the read days in the window`() {
            // ----- Arrange -----
            val activity = activityOf(
                streak(
                    10,
                    11,
                ),
                streak(
                    30,
                    31,
                ),
            )

            // ----- Act -----
            val result = streakSheetFor(
                activity = activity,
                today = today,
            )

            // ----- Assert -----
            result.scaffold.header.shouldNotBeNull().section.headlineText shouldBe "You read on 3 of the last 21 days"
        }
    }
}
