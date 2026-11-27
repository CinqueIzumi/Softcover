package nl.rhaydus.softcover.core.profile.domain.model

import kotlinx.datetime.LocalTime

/**
 * A book's share of one day: [pages] is the day's attributed total, [fromPage] to [toPage] its position span;
 * [ending] says whether the book was finished or stopped that day.
 * The update times are those of its counted updates and are null when it has none.
 */
data class ReadingDayBook(
    val bookId: Int,
    val fromPage: Int,
    val toPage: Int,
    val updateCount: Int,
    val pages: Int,
    val ending: ReadingDayEnding,
    val firstUpdateAt: LocalTime?,
    val lastUpdateAt: LocalTime?,
)
