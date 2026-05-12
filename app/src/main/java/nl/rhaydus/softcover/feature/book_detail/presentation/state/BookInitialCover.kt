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
