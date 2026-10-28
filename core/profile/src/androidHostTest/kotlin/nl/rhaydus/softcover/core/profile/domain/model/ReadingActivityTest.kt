package nl.rhaydus.softcover.core.profile.domain.model

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingActivityTest {
    private fun day(iso: String) = LocalDate.parse(iso)

    private fun streak(span: Pair<String, String>) = ReadingStreak(
        start = day(span.first),
        end = day(span.second),
    )

    private fun activity(
        vararg streaks: ReadingStreak,
        pagesByDay: Map<LocalDate, Int> = emptyMap(),
    ) = ReadingActivity(
        pagesByDay = pagesByDay,
        streaks = streaks.toList(),
    )

    @Nested
    inner class ReadingStreakDays {
        @Test
        fun `counts both ends of the run`() {
            // ----- Arrange -----
            val streak = streak("2026-12-30" to "2027-01-02")

            // ----- Act -----
            val result = streak.days

            // ----- Assert -----
            result shouldBe 4
        }
    }

    @Nested
    inner class CurrentStreak {
        private val today = day("2026-06-10")

        @Test
        fun `returns the run ending today`() {
            // ----- Arrange -----
            val earlier = streak("2026-05-01" to "2026-05-03")
            val current = streak("2026-06-08" to "2026-06-10")
            val activity = activity(
                earlier,
                current,
            )

            // ----- Act -----
            val result = activity.currentStreak(today)

            // ----- Assert -----
            result shouldBe current
        }

        @Test
        fun `returns the run ending yesterday`() {
            // ----- Arrange -----
            val current = streak("2026-06-07" to "2026-06-09")
            val activity = activity(current)

            // ----- Act -----
            val result = activity.currentStreak(today)

            // ----- Assert -----
            result shouldBe current
        }

        @Test
        fun `is null when the last run ended before yesterday`() {
            // ----- Arrange -----
            val activity = activity(streak("2026-06-05" to "2026-06-08"))

            // ----- Act -----
            val result = activity.currentStreak(today)

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `is null without any streaks`() {
            // ----- Arrange -----
            val activity = activity()

            // ----- Act -----
            val result = activity.currentStreak(today)

            // ----- Assert -----
            result.shouldBeNull()
        }
    }

    @Nested
    inner class LongestStreakIn {
        @Test
        fun `picks the longest run of the year`() {
            // ----- Arrange -----
            val longest = streak("2026-04-01" to "2026-04-05")
            val activity = activity(
                streak("2026-02-01" to "2026-02-02"),
                longest,
                streak("2026-08-01" to "2026-08-03"),
            )

            // ----- Act -----
            val result = activity.longestStreakIn(2026)

            // ----- Assert -----
            result shouldBe longest
        }

        @Test
        fun `a run crossing 1 January counts whole for both years`() {
            // ----- Arrange -----
            val crossing = streak("2025-12-30" to "2026-01-03")
            val activity = activity(
                crossing,
                streak("2026-05-01" to "2026-05-02"),
            )

            // ----- Act -----
            val forStartYear = activity.longestStreakIn(2025)
            val forEndYear = activity.longestStreakIn(2026)

            // ----- Assert -----
            forStartYear shouldBe crossing
            forEndYear shouldBe crossing
            forEndYear?.days shouldBe 5
        }

        @Test
        fun `a tie goes to the most recent run`() {
            // ----- Arrange -----
            val recent = streak("2026-09-01" to "2026-09-03")
            val activity = activity(
                streak("2026-03-01" to "2026-03-03"),
                recent,
            )

            // ----- Act -----
            val result = activity.longestStreakIn(2026)

            // ----- Assert -----
            result shouldBe recent
        }

        @Test
        fun `is null for a year with no reading`() {
            // ----- Arrange -----
            val activity = activity(streak("2025-03-01" to "2025-03-03"))

            // ----- Act -----
            val result = activity.longestStreakIn(2026)

            // ----- Assert -----
            result.shouldBeNull()
        }
    }

    @Nested
    inner class StreakOn {
        private val run = streak("2026-06-05" to "2026-06-07")
        private val activity = activity(run)

        @Test
        fun `returns the run for its first, middle and last day`() {
            // ----- Arrange -----
            // (run covers 5 to 7 June)

            // ----- Act -----
            val first = activity.streakOn(day("2026-06-05"))
            val middle = activity.streakOn(day("2026-06-06"))
            val last = activity.streakOn(day("2026-06-07"))

            // ----- Assert -----
            first shouldBe run
            middle shouldBe run
            last shouldBe run
        }

        @Test
        fun `is null on the days just outside the run`() {
            // ----- Arrange -----
            // (run covers 5 to 7 June)

            // ----- Act -----
            val before = activity.streakOn(day("2026-06-04"))
            val after = activity.streakOn(day("2026-06-08"))

            // ----- Assert -----
            before.shouldBeNull()
            after.shouldBeNull()
        }
    }

    @Nested
    inner class ReadingDaysIn {
        @Test
        fun `counts only the days of the requested year`() {
            // ----- Arrange -----
            val activity = activity(
                pagesByDay = mapOf(
                    day("2025-12-31") to 10,
                    day("2026-01-01") to 20,
                    day("2026-07-04") to 30,
                ),
            )

            // ----- Act -----
            val result = activity.readingDaysIn(2026)

            // ----- Assert -----
            result shouldBe 2
        }
    }
}
