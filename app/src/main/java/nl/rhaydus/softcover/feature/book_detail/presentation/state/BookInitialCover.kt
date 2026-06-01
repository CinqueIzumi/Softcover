package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead

data class BookInitialCover(
    val currentEdition: BookEdition?,
    val defaultEdition: BookEdition?,
    val fallbackCoverUrl: String?,
    val userBook: UserBook? = null,
    val userBookRead: UserBookRead? = null,
    /**
     * When set (e.g. opening detail from a barcode scan), the screen shows this specific edition —
     * winning over both the book's default edition and any on-shelf edition — and, if the book is
     * already on a shelf with a different edition, surfaces a banner offering to update it.
     */
    val scannedEditionId: Int? = null,
) {
    companion object {
        fun fromBook(book: Book): BookInitialCover = BookInitialCover(
            currentEdition = book.currentEdition,
            defaultEdition = book.defaultEdition,
            fallbackCoverUrl = book.coverUrl,
            userBook = book.userBook,
            userBookRead = book.userBookRead,
        )

        fun fromEdition(edition: BookEdition): BookInitialCover = BookInitialCover(
            currentEdition = edition,
            defaultEdition = null,
            fallbackCoverUrl = null,
        )
    }
}
