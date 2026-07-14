package nl.rhaydus.softcover.feature.book_detail.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import nl.rhaydus.softcover.core.domain.model.ReviewDocument

private val reviewMonthYearFormat = LocalDate.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    year()
}

data class BookReview(
    val id: Int,
    val reviewDocument: ReviewDocument,
    val hasSpoilers: Boolean,
    val rating: Double?,
    val reviewedAt: String?,
    val likesCount: Int,
    val reviewer: BookReviewer,
) {
    /** `Nov 2023` — month name + year only, no day. `null` when the review carries no date. */
    fun getReviewedMonthYear(): String? {
        val raw = reviewedAt ?: return null

        return runCatching {
            reviewMonthYearFormat.format(LocalDateTime.parse(raw).date)
        }.getOrNull()
    }
}
