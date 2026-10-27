package nl.rhaydus.softcover.core.profile.domain.mapper

import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.profile.domain.model.DailyPages

/** Sums the attributed pages per day of [year]; see [attributePages] for what counts. */
internal fun toDailyPages(
    updates: List<ProgressUpdate>,
    readBooks: List<Book>,
    year: Int,
    timeZone: TimeZone,
): DailyPages = DailyPages(
    year = year,
    pages = attributePages(
        updates = updates,
        readBooks = readBooks,
        timeZone = timeZone,
    )
        .filter { it.date.year == year }
        .groupBy { it.date }
        .mapValues { (_, entries) -> entries.sumOf { it.pages } },
)
