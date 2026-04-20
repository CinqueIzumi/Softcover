package nl.rhaydus.softcover.core.domain.model

data class BookEdition(
    val id: Int,
    val canonicalId: Int?,
    val bookId: Int,
    val publisher: String?,
    val title: String?,
    val url: String?,
    val localImagePath: String?,
    val isbn10: String?,
    val pages: Int?,
    val audioSeconds: Int?,
    val authors: List<Author>,
    val releaseYear: Int,
    val format: String,
    val owned: Boolean,
) {
    val authorString: String
        get() = authors.joinToString(", ") { it.name }

    val isAudiobook: Boolean
        get() = (audioSeconds ?: 0) > 0
}