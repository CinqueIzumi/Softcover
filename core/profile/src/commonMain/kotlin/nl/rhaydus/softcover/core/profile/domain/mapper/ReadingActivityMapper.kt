package nl.rhaydus.softcover.core.profile.domain.mapper

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.profile.domain.model.ReadingActivity
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStreak

/** Sums the attributed pages per day across all years; see [attributePages] for what counts. */
internal fun toReadingActivity(
    updates: List<ProgressUpdate>,
    readBooks: List<Book>,
    timeZone: TimeZone,
): ReadingActivity {
    val pagesByDay = attributePages(
        updates = updates,
        readBooks = readBooks,
        timeZone = timeZone,
    )
        .groupBy { it.date }
        .mapValues { (_, entries) -> entries.sumOf { it.pages } }
        .filterValues { it > 0 }

    return ReadingActivity(
        pagesByDay = pagesByDay,
        streaks = streaksOf(pagesByDay.keys),
    )
}

private fun streaksOf(readingDays: Set<LocalDate>): List<ReadingStreak> {
    val streaks = mutableListOf<ReadingStreak>()

    for (day in readingDays.sorted()) {
        val last = streaks.lastOrNull()

        if (last != null && last.end.plus(
            1,
            DateTimeUnit.DAY,
        ) == day) {
            streaks[streaks.lastIndex] = last.copy(end = day)
        } else {
            streaks += ReadingStreak(
                start = day,
                end = day,
            )
        }
    }

    return streaks
}
