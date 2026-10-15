package nl.rhaydus.softcover.feature.settings.domain.model

/**
 * One inline run of a [MarkdownBlock]'s text. Marks are independent flags rather than a nested tree -
 * "***bold and italic***" is one run with both [bold] and [italic] set - which keeps the parser and
 * the renderer simple at the cost of not modelling arbitrarily nested marks; the published documents
 * (generated milestone descriptions, a hand-kept changelog) never need more than that.
 */
data class MarkdownSpan(
    val text: String,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val code: Boolean = false,
    val url: String? = null,
)
