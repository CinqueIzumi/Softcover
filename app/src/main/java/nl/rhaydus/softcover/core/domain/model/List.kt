package nl.rhaydus.softcover.core.domain.model

data class BookList(
    val id: Int,
    val name: String,
    val editions: List<BookEdition>,
)