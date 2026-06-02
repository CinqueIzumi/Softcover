package nl.rhaydus.softcover.core.designsystem.presentation.share

data class QuoteShareContent(
    val quote: String,
    val sourceTitle: String,
    val sourceAuthor: String,
    val page: Int?,
) : ShareContent
