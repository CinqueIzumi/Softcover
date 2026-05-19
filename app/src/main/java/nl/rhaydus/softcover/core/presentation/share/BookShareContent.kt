package nl.rhaydus.softcover.core.presentation.share

data class BookShareContent(
    val coverUrl: String?,
    val title: String,
    val author: String,
    val communityRating: Double?,
    val userRating: Int?,
    val releaseYear: Int?,
    val pageCount: Int?,
    val description: String?,
    val quote: String?,
) : ShareContent
