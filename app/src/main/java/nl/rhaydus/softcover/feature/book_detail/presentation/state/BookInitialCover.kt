package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition

data class BookInitialCover(
    val currentEdition: BookEdition?,
    val defaultEdition: BookEdition?,
    val fallbackCoverUrl: String?,
) {
    companion object {
        fun fromBook(book: Book): BookInitialCover = BookInitialCover(
            currentEdition = book.currentEdition,
            defaultEdition = book.defaultEdition,
            fallbackCoverUrl = book.coverUrl,
        )

        fun fromEdition(edition: BookEdition): BookInitialCover = BookInitialCover(
            currentEdition = edition,
            defaultEdition = null,
            fallbackCoverUrl = null,
        )
    }
}
