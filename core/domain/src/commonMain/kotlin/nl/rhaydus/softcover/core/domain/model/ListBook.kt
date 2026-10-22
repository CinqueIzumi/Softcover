package nl.rhaydus.softcover.core.domain.model

/**
 * A `list_books` row. [editionId] and [edition] are the row's own. [shownEdition] is the edition the entry
 * displays: on a regular list the reader's user-book edition when it is cached, else the row's; on Owned
 * always the row's.
 */
data class ListBook(
    val listBookId: Int,
    val listId: Int,
    val bookId: Int,
    val editionId: Int,
    val position: Int? = null,
    val addedAt: String? = null,
    val book: Book? = null,
    val edition: BookEdition? = null,
    val shownEdition: BookEdition? = null,
)
