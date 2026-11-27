package nl.rhaydus.softcover.core.profile.domain.model

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.Book

/** [since] is the day the book began waiting. */
data class WaitingBook(
    val book: Book,
    val since: LocalDate,
)

/**
 * The book waiting longest on Want to Read, and the owned, unstarted one waiting longest on the shelf.
 * [hasUndatedOwned] is true when a row of the Owned list [ownedListId] has no parseable `addedAt`, so the shelf
 * result may be missing books.
 */
data class LongestWaiting(
    val onList: WaitingBook? = null,
    val onShelf: WaitingBook? = null,
    val ownedListId: Int? = null,
    val hasUndatedOwned: Boolean = false,
)
