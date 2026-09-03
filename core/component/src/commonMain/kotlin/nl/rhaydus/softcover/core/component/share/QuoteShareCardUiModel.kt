package nl.rhaydus.softcover.core.component.share

data class QuoteShareCardUiModel(
    val quote: String,
    val sourceTitle: String,
    val sourceAuthor: String,
    val page: Int?,
) : ShareCardUiModel
