package nl.rhaydus.softcover.core.presentation.model

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBook

data class BookInitialCover(
    val currentEdition: BookEdition?,
    val defaultEdition: BookEdition?,
    val fallbackCoverUrl: String?,
    val userBook: UserBook? = null,
    val title: String? = null,
    /** Null when unknown; blank when the book has no series, unlike `Book.seriesText`, which is null then. */
    val seriesText: String? = null,
    val rating: Double? = null,
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
            title = book.title,
            seriesText = book.seriesText.orEmpty(),
            rating = book.rating,
        )

        fun forScannedEdition(
            book: Book,
            editionId: Int?,
        ): BookInitialCover = fromBook(book = book).copy(
            currentEdition = book.editions.firstOrNull { it.id == editionId } ?: book.currentEdition,
            scannedEditionId = editionId,
        )

        fun fromEdition(edition: BookEdition): BookInitialCover = BookInitialCover(
            currentEdition = edition,
            defaultEdition = null,
            fallbackCoverUrl = null,
            title = edition.title,
        )
    }
}
