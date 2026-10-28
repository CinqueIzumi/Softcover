package nl.rhaydus.softcover.core.profile.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/** One finished read; the caller looks up its cover and title by [bookId]. [pages] is null when the length is unknown. */
data class ReadDuration(
    val bookId: Int,
    val readId: Int,
    val startedOn: LocalDate,
    val finishedOn: LocalDate,
    val pages: Int?,
) {
    /** Both ends count, so a read started and finished on the same day is one day. */
    val days: Int
        get() = startedOn.daysUntil(finishedOn) + 1
}
