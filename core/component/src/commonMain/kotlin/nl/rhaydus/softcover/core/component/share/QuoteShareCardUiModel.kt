package nl.rhaydus.softcover.core.component.share

/** A pulled [quote] behind a low-alpha quotation glyph, attributed to [sourceTitle] / [sourceAuthor] and an optional [page]. */
data class QuoteShareCardUiModel(
    val quote: String,
    val sourceTitle: String,
    val sourceAuthor: String,
    val page: Int?,
) : ShareCardUiModel
