package nl.rhaydus.softcover.core.profile.domain.mapper

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.profile.domain.model.LongestWaiting
import nl.rhaydus.softcover.core.profile.domain.model.WaitingBook
import kotlin.time.Instant

internal fun toLongestWaiting(
    books: List<Book>,
    ownedListBooks: List<ListBook>,
    ownedListId: Int? = null,
): LongestWaiting {
    val onList: WaitingBook? = books
        .mapNotNull { book ->
            book.wantToReadSinceOrNull()?.let { since ->
                WaitingBook(
                    book,
                    since,
                )
            }
        }
        .minWithOrNull(compareBy<WaitingBook> { it.since }.thenBy { it.book.id })

    val libraryById: Map<Int, Book> = books.associateBy { it.id }

    val onShelf: WaitingBook? = ownedListBooks
        .groupBy { it.bookId }
        .mapNotNull { (bookId, rows) ->
            val book: Book = libraryById[bookId] ?: rows.firstNotNullOfOrNull { it.book } ?: return@mapNotNull null

            val since: LocalDate = rows.mapNotNull { it.addedAt.toAddedOnOrNull() }.minOrNull() ?: return@mapNotNull null

            WaitingBook(
                book,
                since,
            ).takeIf { book.id != onList?.book?.id && book.isUnstarted() }
        }
        .minWithOrNull(compareBy<WaitingBook> { it.since }.thenBy { it.book.id })

    return LongestWaiting(
        onList = onList,
        onShelf = onShelf,
        ownedListId = ownedListId,
        hasUndatedOwned = ownedListBooks.any { it.addedAt.toAddedOnOrNull() == null },
    )
}

private fun Book.wantToReadSinceOrNull(): LocalDate? {
    val userBook = userBook?.takeIf { it.status == BookStatus.WantToRead } ?: return null

    val addedOn = runCatching { LocalDate.parse(userBook.dateAdded) }.getOrNull() ?: return null

    return maxOf(
        addedOn,
        userBook.latestReadDateOrNull() ?: addedOn,
    )
}

private fun Book.isUnstarted(): Boolean {
    val userBook = userBook ?: return true

    return userBook.status == BookStatus.WantToRead && userBook.reads.none { it.startedAt != null }
}

/** Accepts the offsetless date-time Hardcover sends, a plain date, or an offset form. */
private fun String?.toAddedOnOrNull(): LocalDate? {
    val text = this ?: return null

    return runCatching { LocalDateTime.parse(text).date }.getOrNull()
        ?: runCatching { LocalDate.parse(text) }.getOrNull()
        ?: runCatching { Instant.parse(text).toLocalDateTime(TimeZone.UTC).date }.getOrNull()
}
