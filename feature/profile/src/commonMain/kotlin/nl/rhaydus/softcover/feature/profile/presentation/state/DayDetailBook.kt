package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.presentation.model.BookInitialCover

/** What a day-detail row opens: the book, and the cover that seeds its detail. Null for a book no longer in the library. */
internal data class DayDetailBook(
    val bookId: Int,
    val initialCover: BookInitialCover?,
)
