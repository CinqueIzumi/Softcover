package nl.rhaydus.softcover.core.domain.model

data class ListBook(
    val listBookId: Int,
    val listId: Int,
    val bookId: Int,
    val editionId: Int,
    val addedAt: String? = null,
    val book: Book? = null,
    val edition: BookEdition? = null,
)
