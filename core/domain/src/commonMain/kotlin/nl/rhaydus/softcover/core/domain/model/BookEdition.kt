package nl.rhaydus.softcover.core.domain.model

import kotlinx.datetime.LocalDate

data class BookEdition(
    val id: Int,
    val canonicalId: Int?,
    val bookId: Int,
    val publisher: String?,
    val title: String?,
    val url: String?,
    val localImagePath: String?,
    val isbn10: String?,
    val isbn13: String?,
    val pages: Int?,
    val audioSeconds: Int?,
    val authors: List<Author>,
    val releaseYear: Int,
    val releaseDate: LocalDate? = null,
    val format: String,
    val readingFormat: ReadingFormat? = null,
    val owned: Boolean,
) {
    val authorString: String
        get() = authors.joinToString(", ") { it.name }

    val isAudiobook: Boolean
        get() = readingFormat == ReadingFormat.Audio
}
