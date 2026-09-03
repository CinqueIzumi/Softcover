package nl.rhaydus.softcover.core.component.share

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
