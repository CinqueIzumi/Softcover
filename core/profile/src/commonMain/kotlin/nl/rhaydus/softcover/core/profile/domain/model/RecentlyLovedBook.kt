package nl.rhaydus.softcover.core.profile.domain.model

import kotlinx.datetime.LocalDate

/** A highly rated finished book; the caller looks up its cover and title by [bookId]. */
data class RecentlyLovedBook(
    val bookId: Int,
    val ratingStars: Double,
    val finishedOn: LocalDate,
)
