package nl.rhaydus.softcover.feature.widget.domain.model

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition

/** A book drawn for the Random pick widget. [listEdition] is the edition a list entry shows, `null` for Want to read. */
internal data class RandomPickBook(
    val book: Book,
    val listEdition: BookEdition?,
) {
    val shownEdition: BookEdition?
        get() = listEdition ?: book.currentEdition

    val title: String
        get() = book.titleIn(shownEdition)

    val author: String
        get() = book.authorsIn(shownEdition)
}
