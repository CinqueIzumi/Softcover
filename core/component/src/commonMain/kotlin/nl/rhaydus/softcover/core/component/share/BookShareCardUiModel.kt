package nl.rhaydus.softcover.core.component.share

/**
 * The plain editorial book card — a share of the book itself, not the reader's relationship to it (see
 * [ReadingUpdateShareCardUiModel] for that). Every field but [title] and [author] is nullable so the
 * card renders whatever the caller actually knows about the book, omitting the rest.
 */
data class BookShareCardUiModel(
    val coverUrl: String?,
    val title: String,
    val author: String,
    val communityRating: Double?,
    val userRating: Int?,
    val releaseYear: Int?,
    val pageCount: Int?,
    val description: String?,
    val quote: String?,
) : ShareCardUiModel
