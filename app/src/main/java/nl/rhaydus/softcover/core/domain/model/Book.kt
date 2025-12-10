package nl.rhaydus.softcover.core.domain.model

data class Book(
    val id: Int,
    val title: String,
    val editions: List<BookEdition>,
)