package nl.rhaydus.softcover.core.domain.model

data class BookEdition(
    val id: Int,
    val publisher: String?,
    val title: String?,
    val url: String?,
    val isbn10: String?,
    val pages: Int?,
    val authors: List<Author>,
    val releaseYear: Int,
) {
    val authorString: String
        get() = authors.joinToString(", ") { it.name }
}