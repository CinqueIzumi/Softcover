package nl.rhaydus.softcover.core.profile.domain.model

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/** Pages read per day for all time, and the reading streaks those days form. */
data class ReadingActivity(
    /** Days with no pages are absent. */
    val pagesByDay: Map<LocalDate, Int>,
    /** Every maximal run of reading days, oldest first. */
    val streaks: List<ReadingStreak>,
) {
    /** The run ending today, or yesterday: a streak survives until the day is over. */
    fun currentStreak(today: LocalDate): ReadingStreak? {
        val yesterday = today.minus(
            value = 1,
            unit = DateTimeUnit.DAY,
        )

        return streaks.lastOrNull()?.takeIf { it.end == today || it.end == yesterday }
    }

    /** The longest run touching [year], counted whole even across 1 January; ties go to the most recent. */
    fun longestStreakIn(year: Int): ReadingStreak? = streaks
        .filter { it.start.year <= year && it.end.year >= year }
        .fold<ReadingStreak, ReadingStreak?>(null) { longest, streak ->
            if (longest == null || streak.days >= longest.days) streak else longest
        }

    fun streakOn(date: LocalDate): ReadingStreak? = streaks.firstOrNull { date in it.start..it.end }

    fun readingDaysIn(year: Int): Int = pagesByDay.keys.count { it.year == year }
}
