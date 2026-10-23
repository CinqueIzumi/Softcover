package nl.rhaydus.softcover.feature.widget.domain.model

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition

/** A book drawn for the Random pick widget. [listEdition] is the edition a list entry shows, `null` for Want to read. */
internal data class RandomPickBook(
    val book: Book,
    val listEdition: BookEdition?,
) {
    val title: String
        get() = listEdition?.let(book::titleIn) ?: book.displayTitle

    val author: String
        get() = listEdition?.let(book::authorsIn) ?: book.authorString
}
