package nl.rhaydus.softcover.core.profile.domain.mapper

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.profile.domain.model.QuickestRead
import nl.rhaydus.softcover.core.profile.domain.model.ReadDuration

/**
 * The read with the fewest days. A tie goes to the longer read (unknown pages rank lowest), then
 * the later finish, then the higher read id. Null when [this] is empty or the winner's book is not in [books].
 */
internal fun List<ReadDuration>.quickestRead(books: List<Book>): QuickestRead? {
    val winner = minWithOrNull(
        compareBy<ReadDuration> { it.days }
            .thenByDescending { it.pages ?: -1 }
            .thenByDescending { it.finishedOn }
            .thenByDescending { it.readId },
    ) ?: return null

    val book = books.firstOrNull { it.id == winner.bookId } ?: return null

    return QuickestRead(
        book = book,
        duration = winner,
    )
}
