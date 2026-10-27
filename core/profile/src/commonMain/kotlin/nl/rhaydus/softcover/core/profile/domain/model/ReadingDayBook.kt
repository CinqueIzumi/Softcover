package nl.rhaydus.softcover.core.profile.domain.model

import kotlinx.datetime.LocalTime

/**
 * A book's share of one day: [pages] is the day's attributed total, [fromPage] to [toPage] its position span.
 * The update times are those of its counted updates and are null when it has none.
 */
data class ReadingDayBook(
    val bookId: Int,
    val fromPage: Int,
    val toPage: Int,
    val updateCount: Int,
    val pages: Int,
    val finished: Boolean,
    val firstUpdateAt: LocalTime?,
    val lastUpdateAt: LocalTime?,
)
