package nl.rhaydus.softcover.core.profile.domain.model

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.Book

/** [since] is the day the book began waiting. */
data class WaitingBook(
    val book: Book,
    val since: LocalDate,
)
