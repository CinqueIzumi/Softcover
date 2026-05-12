package nl.rhaydus.softcover.core.presentation.share

data class BookShareContent(
    val coverUrl: String?,
    val title: String,
    val author: String,
    val userRating: Int?,
    val quote: String?,
) : ShareContent
