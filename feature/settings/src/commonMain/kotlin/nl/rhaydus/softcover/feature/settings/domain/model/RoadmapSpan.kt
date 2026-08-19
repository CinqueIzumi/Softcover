package nl.rhaydus.softcover.feature.settings.domain.model

/**
 * One inline run of a [RoadmapBlock]'s text. Marks are independent flags rather than a nested tree -
 * "***bold and italic***" is one run with both [bold] and [italic] set - which keeps the parser and
 * the renderer simple at the cost of not modelling arbitrarily nested marks; the roadmap's source
 * (generated milestone descriptions) never needs more than that.
 */
data class RoadmapSpan(
    val text: String,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val code: Boolean = false,
    val url: String? = null,
)
